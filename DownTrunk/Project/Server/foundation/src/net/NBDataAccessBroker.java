package net;

import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import codec.AppMessageCodecFactory;

public class NBDataAccessBroker implements DataAccess, ClientListener,
		Runnable, IConnector {

	/** 默认的超时常量，30秒 */
	public static final int TIMEOUT = 10000;

	/** 连接表 */
	protected ArrayList<ISession> connectList = new ArrayList<ISession>();

	protected SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSS");

	/** 线程访问对象 */
	ThreadAccess threadAccess = new ThreadAccessBroker();

	/** ping消息间隔时间 */
	protected int pingTime = 15000;

	/** ping消息的数据 */
	protected ByteBuffer pingdata = new ByteBuffer();

	protected AppMessageCodecFactory factory = new AppMessageCodecFactory();

	/** item的map */
	protected Map<ISession, List<Item>> handerMap = new ConcurrentHashMap<ISession, List<Item>>();

	public NBDataAccessBroker() {
		Thread thread = new Thread(this, "NBDataAccessBroker");
		thread.setDaemon(true);
		thread.start();

	}

	/**
	 * session关闭时通知所有的监听
	 * 
	 * @param session
	 */
	protected synchronized void callHanderIsessionClosed(ISession session) {
		List<Item> items = handerMap.remove(session);
		if (items == null) {
			return;
		}
		Item item = null;
		for (int i = 0; i < items.size(); i++) {
			item = items.get(i);
			if (item == null) {
				continue;
			}
			threadAccess.removeHandler(item);
			synchronized (item) {
				item.notify();
			}
		}
	}

	/**
	 * 加入session对应的handler；
	 * 
	 * @param session
	 * @param handler
	 */
	protected synchronized void addHanderwithSession(ISession session,
			Item handler) {

		if (!handerMap.containsKey(session)) {
			handerMap.put(session, new ArrayList<Item>());
		}
		List<Item> items = handerMap.get(session);
		synchronized (items) {
			items.add(handler);
			// log.debug("add wait item: " + handler.getAccessId());
		}

	}

	/** 移除session对应的handler */
	private void removeHandlerwithSession(ISession connnect, int id) {
		List<Item> lists = handerMap.get(connnect);
		if (lists != null) {
			synchronized (lists) {
				for (int j = 0, n = lists.size(); j < n; j++) {
					Item item = lists.get(j);
					if (item != null && item.accessId == id) {
						lists.remove(j);
						// log.debug("remove wait item:" + item.getAccessId());
						break;
					}
				}
			}
		}
	}

	public IByteBuffer access(ServerAddress url, int id, IByteBuffer data)
			throws DataAccessException {
		ISession connnect;
		try {
			connnect = getConnect(url);
		} catch (IOException e) {
			throw new DataAccessException(
					DataAccessException.CLIENT_ACCESS_ERROR,
					"system.getConnectionTimeout", url.getAddress()
							.getHostAddress() + ":" + url.getPort());
		}
		return access(connnect, id, data);
	}

	/**
	 * 通过已经获得的连接，访问数据
	 * 
	 * @param connnect
	 * @param id
	 * @param data
	 * @return
	 * @throws DataAccessException
	 */
	public IByteBuffer access(ISession connnect, int id, IByteBuffer data)
			throws DataAccessException {
		Item item = new Item(connnect, connnect.getAddress(), id, data);

		// 记忆已发出的阻塞消息，客户端断开连接时需要清理
		if (connnect instanceof NIOClientConnection) {
			NIOClientConnection clientConnection = (NIOClientConnection) connnect;
			clientConnection.addListener(this);
			this.addHanderwithSession(connnect, item);// 服务器响应、超时、客户端断开自动移除
		}

		// 同步访问
		Object result = threadAccess.access(item, TIMEOUT);

		// 响应超时，移除等待对象
		if (result == ThreadAccess.NONE) {
			removeHandlerwithSession(connnect, item.accessId);
			throw new DataAccessException(DataAccessException.SERVER_TIMEOUT,
					"system.getdataTimeout", connnect.getAddress()
							.getHostAddress() + ":" + connnect.getPort());
		}
		return parseData((ByteBuffer) result, connnect.getAddress());
	}

	public IByteBuffer access(ISession connnect, IByteBuffer data)
			throws DataAccessException {
		Item item = new Item(connnect, connnect.getAddress(), data);

		// 记忆已发出的阻塞消息，客户端断开连接时需要清理
		if (connnect instanceof NIOClientConnection) {
			NIOClientConnection clientConnection = (NIOClientConnection) connnect;
			clientConnection.addListener(this);
			this.addHanderwithSession(connnect, item);// 服务器响应、超时、客户端断开自动移除
		}

		// 同步访问
		Object result = threadAccess.access(item, TIMEOUT);

		// 响应超时，移除等待对象
		if (result == ThreadAccess.NONE) {
			removeHandlerwithSession(connnect, item.accessId);
			throw new DataAccessException(DataAccessException.SERVER_TIMEOUT,
					"system.getdataTimeout", connnect.getAddress()
							.getHostAddress() + ":" + connnect.getPort());
		}
		return parseData((ByteBuffer) result, connnect.getAddress());
	}
	
	public void access(ServerAddress url, int id, IByteBuffer data,
			Response response) throws DataAccessException {
		ISession connnect;
		try {
			connnect = getConnect(url);
		} catch (IOException e) {
			throw new DataAccessException(
					DataAccessException.CLIENT_ACCESS_ERROR,
					"system.getConnectionTimeout", url.getAddress()
							.getHostAddress() + ":" + url.getPort());
		}
		access(connnect, id, data, response);
	}

	public void access(ISession connnect, int id, IByteBuffer data,
			Response response) throws DataAccessException {
		if (response != null) {
			Item item = new Item(connnect, connnect.getAddress(), id, data);
			if (connnect instanceof NIOClientConnection) {
				NIOClientConnection clientConnection = (NIOClientConnection) connnect;
				clientConnection.addListener(this);
				this.addHanderwithSession(connnect, item);
			}
			item.response = response;
			threadAccess.access4nonwait(item);
		} else {
			IByteBuffer message = ByteBufferFactory.getNewByteBuffer();
			message.writeByte(id);
			message.writeByteBuffer(data);
			connnect.send(message);
		}
	}

	/** 消息响应方法，参数connect是传递消息的连接，参数data是接收的消息 */
	public void service(ISession connnect, IByteBuffer data) {
		int id = data.readByte();
		if (id == 0) {
			id = data.readInt();
			// 得到响应移除休眠对象
			if (connnect instanceof NIOClientConnection) {
				removeHandlerwithSession(connnect, id);
			}
			// 唤醒对应的休眠线程
			threadAccess.notify(id, data);
		} else {
			data.setReadPos(data.getReadPos() - 1);
			doService(connnect, data);
		}
	}

	protected void doService(ISession connnect, IByteBuffer data) {
		// int id = data.readByte();
	}

	/** 分析返回的响应消息数据 */
	ByteBuffer parseData(ByteBuffer data, InetAddress url)
			throws DataAccessException {
		try {
			int t = data.readUnsignedShort();
			if (t == OK) {
				return data;
			}

			throw LanguageKit.readException(data, t);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new DataAccessException(
					DataAccessException.SERVER_DATA_ERROR, "system.dcFault");
		}
	}

	/** 根据url得到对应的连接，如果连接为空则重新创建一个连接 */
	public ISession getConnect(ServerAddress url) throws IOException,
			DataAccessException {
		// log.debug("start get connect " + url.toString() + " at "
		// + dateFormat.format(new Date()));
		if (url == null)
			throw new IllegalArgumentException("getConnect, null url");
		InetAddress addr = url.getAddress();
		if (addr == null)
			throw new IllegalArgumentException("getConnect, invalid address:"
					+ url.toString());
		ISession connect;
		synchronized (connectList) {
			for (int i = 0; i < connectList.size(); i++) {
				connect = (connectList.get(i));
				if (!connect.isClosed()) {
					if (connect.getAddress().equals(url.address)
							&& connect.getPort() == url.getPort()) {
						// log.debug("return  exist connect " + url.toString()
						// + " at " + dateFormat.format(new Date()));
						return connect;
					}
				} else {
					// 移除掉当前
					connectList.remove(i);
					// 继续检测
					i--;
				}
			}
		}

		ISession newConnect = createConnect(url);

		login(newConnect, url);

		synchronized (connectList) {
			connectList.add(newConnect);
		}
		return newConnect;
	}

	/**
	 * 创建一个新的连接
	 * 
	 * @param url
	 * @return
	 */
	public ISession createConnect(ServerAddress url) {
		NIOClientConnection newConnect = null;
		try {
			newConnect = newConnection(url.getAddress(), url.getPort());
			if (factory != null) {
				newConnect.setMessageDecoder(factory.createDecoder());
				newConnect.setMessageEncoder(factory.createEncoder());
			}
			
			newConnect.start();
			
			if (!newConnect.isActive())
				throw new IOException("getConnect, create connect fail, url:"
						+ url.toString());
			newConnect.addListener(this);
			newConnect.setTimeout(180000);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return newConnect;
	}

	protected NIOClientConnection newConnection(InetAddress address, int port) {
		return new NIOClientConnection(address, port);
	}

	/** 登陆 */
	public void login(ISession connect, ServerAddress url)
			throws DataAccessException {
		// doLogin(connect, url);
	}

	/** 登陆指定的连接 */
	public void doLogin(ISession connect, ServerAddress url)
			throws DataAccessException {
		ByteBuffer data = new ByteBuffer();
		data.writeByte(6);
		data.writeUTF(url.getName());
		data.writeUTF(url.getPassword());
		try {
			connect.send(data);
		} catch (Exception e) {
			e.printStackTrace();
			throw new DataAccessException(DataAccessException.SERVER_IO_ERROR,
					"login to server error", url.toString());
		}
	}

	/** 类说明:访问条目类 */
	class Item extends ThreadAccessHandler {

		/* fields */
		/** 访问连接 */
		protected ISession connect;

		/** 访问地址 */
		protected ServerAddress url;

		protected InetAddress netAddress;

		/** 通讯数据 */
		protected IByteBuffer data;

		/** 通讯模块id */
		protected int id;

		/** 响应 */
		protected Response response;

		/* constructors */
		/** 构造一个访问条目，参数用于handle()调用 */
		Item(ISession connect, ServerAddress url, int id, IByteBuffer data) {
			this.connect = connect;
			this.url = url;
			this.data = data;
			this.id = id;
		}

		/** 构造一个访问条目，参数用于handle()调用 */
		Item(ISession connect, InetAddress netAddress, int id, IByteBuffer data) {
			this.connect = connect;
			this.netAddress = netAddress;
			this.data = data;
			this.id = id;
		}
		
		/** 构造一个访问条目，参数用于handle()调用 */
		Item(ISession connect, InetAddress netAddress, IByteBuffer data) {
			this.connect = connect;
			this.netAddress = netAddress;
			this.data = data;
			this.id = 0;
		}
		
		/* method */
		/** 线程访问处理方法 */
		@Override
		public void handle() {
			ByteBuffer message = new ByteBuffer();

			// 模块id
			message.writeByte(id);
			int id = getAccessId();
			// 阻塞id
			message.writeInt(id);
			int k = data.getReadPos();
			// 发送到数据
			message.writeByteBuffer(data, data.available());
			data.setReadPos(k);
			connect.send(message);
		}

		@Override
		public void doNotify() {
			if (response != null) {
				try {
					parseResponse(response, (IByteBuffer) accessResult);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			} else {
				synchronized (this) {
					this.notify();
				}
			}
		}
	}

	protected void ping(long l) {
		Object[] aconnect = connectList.toArray();

		for (int j = aconnect.length - 1; j >= 0; j--) {

			ISession connect = (ISession) aconnect[j];
			if (!connect.isClosed()) {
				try {
					this.access(connect, 1, pingdata);
				} catch (DataAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void parseResponse(Response resp, IByteBuffer data) {
		int t = data.readUnsignedShort();
		if (t == OK) {
			resp.respondOK(data);
		} else {
			resp.respondFail(data);
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				long l = System.currentTimeMillis();
				ping(l);
				Thread.sleep(pingTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void messageArrived(NIOClientConnection netconnection,
			IByteBuffer message) {
		try {
			service(netconnection, message);
		} catch (Exception e) {
//			e.printStackTrace();
		}
	}

	@Override
	public void onClosed(NIOClientConnection netconnection) {
		this.callHanderIsessionClosed(netconnection);
	}
}
