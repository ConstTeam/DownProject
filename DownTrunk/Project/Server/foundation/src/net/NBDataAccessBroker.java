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

	/** Ĭ�ϵĳ�ʱ������30�� */
	public static final int TIMEOUT = 10000;

	/** ���ӱ� */
	protected ArrayList<ISession> connectList = new ArrayList<ISession>();

	protected SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSS");

	/** �̷߳��ʶ��� */
	ThreadAccess threadAccess = new ThreadAccessBroker();

	/** ping��Ϣ���ʱ�� */
	protected int pingTime = 15000;

	/** ping��Ϣ������ */
	protected ByteBuffer pingdata = new ByteBuffer();

	protected AppMessageCodecFactory factory = new AppMessageCodecFactory();

	/** item��map */
	protected Map<ISession, List<Item>> handerMap = new ConcurrentHashMap<ISession, List<Item>>();

	public NBDataAccessBroker() {
		Thread thread = new Thread(this, "NBDataAccessBroker");
		thread.setDaemon(true);
		thread.start();

	}

	/**
	 * session�ر�ʱ֪ͨ���еļ���
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
	 * ����session��Ӧ��handler��
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

	/** �Ƴ�session��Ӧ��handler */
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
	 * ͨ���Ѿ���õ����ӣ���������
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

		// �����ѷ�����������Ϣ���ͻ��˶Ͽ�����ʱ��Ҫ����
		if (connnect instanceof NIOClientConnection) {
			NIOClientConnection clientConnection = (NIOClientConnection) connnect;
			clientConnection.addListener(this);
			this.addHanderwithSession(connnect, item);// ��������Ӧ����ʱ���ͻ��˶Ͽ��Զ��Ƴ�
		}

		// ͬ������
		Object result = threadAccess.access(item, TIMEOUT);

		// ��Ӧ��ʱ���Ƴ��ȴ�����
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

		// �����ѷ�����������Ϣ���ͻ��˶Ͽ�����ʱ��Ҫ����
		if (connnect instanceof NIOClientConnection) {
			NIOClientConnection clientConnection = (NIOClientConnection) connnect;
			clientConnection.addListener(this);
			this.addHanderwithSession(connnect, item);// ��������Ӧ����ʱ���ͻ��˶Ͽ��Զ��Ƴ�
		}

		// ͬ������
		Object result = threadAccess.access(item, TIMEOUT);

		// ��Ӧ��ʱ���Ƴ��ȴ�����
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

	/** ��Ϣ��Ӧ����������connect�Ǵ�����Ϣ�����ӣ�����data�ǽ��յ���Ϣ */
	public void service(ISession connnect, IByteBuffer data) {
		int id = data.readByte();
		if (id == 0) {
			id = data.readInt();
			// �õ���Ӧ�Ƴ����߶���
			if (connnect instanceof NIOClientConnection) {
				removeHandlerwithSession(connnect, id);
			}
			// ���Ѷ�Ӧ�������߳�
			threadAccess.notify(id, data);
		} else {
			data.setReadPos(data.getReadPos() - 1);
			doService(connnect, data);
		}
	}

	protected void doService(ISession connnect, IByteBuffer data) {
		// int id = data.readByte();
	}

	/** �������ص���Ӧ��Ϣ���� */
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

	/** ����url�õ���Ӧ�����ӣ��������Ϊ�������´���һ������ */
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
					// �Ƴ�����ǰ
					connectList.remove(i);
					// �������
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
	 * ����һ���µ�����
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

	/** ��½ */
	public void login(ISession connect, ServerAddress url)
			throws DataAccessException {
		// doLogin(connect, url);
	}

	/** ��½ָ�������� */
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

	/** ��˵��:������Ŀ�� */
	class Item extends ThreadAccessHandler {

		/* fields */
		/** �������� */
		protected ISession connect;

		/** ���ʵ�ַ */
		protected ServerAddress url;

		protected InetAddress netAddress;

		/** ͨѶ���� */
		protected IByteBuffer data;

		/** ͨѶģ��id */
		protected int id;

		/** ��Ӧ */
		protected Response response;

		/* constructors */
		/** ����һ��������Ŀ����������handle()���� */
		Item(ISession connect, ServerAddress url, int id, IByteBuffer data) {
			this.connect = connect;
			this.url = url;
			this.data = data;
			this.id = id;
		}

		/** ����һ��������Ŀ����������handle()���� */
		Item(ISession connect, InetAddress netAddress, int id, IByteBuffer data) {
			this.connect = connect;
			this.netAddress = netAddress;
			this.data = data;
			this.id = id;
		}
		
		/** ����һ��������Ŀ����������handle()���� */
		Item(ISession connect, InetAddress netAddress, IByteBuffer data) {
			this.connect = connect;
			this.netAddress = netAddress;
			this.data = data;
			this.id = 0;
		}
		
		/* method */
		/** �̷߳��ʴ����� */
		@Override
		public void handle() {
			ByteBuffer message = new ByteBuffer();

			// ģ��id
			message.writeByte(id);
			int id = getAccessId();
			// ����id
			message.writeInt(id);
			int k = data.getReadPos();
			// ���͵�����
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
