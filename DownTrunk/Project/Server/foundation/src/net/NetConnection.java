package net;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import util.Deque;

public abstract class NetConnection implements ISession, Runnable, ISendData {

	/** 数据缓冲区大小 */
	public static final int DATA_BUFFER_SIZE = 64;

	public static boolean NET_DEBUG = true;

	/** 最大缓存数据 */
	public static final int MAX_STORED_BYTES = 0x10000;

	/** 最大发送数据 */
	public static final int MAX_SEND_SIZE = 4 * 1024;

	/** 连接的观察者数组 */
	protected ArrayList<NetConnectionListener> listeners = new ArrayList<NetConnectionListener>();

	// /** 连接对应的数据 */
	// protected Object attachment;
	/** 最大buffer长度 */
	protected int maxBufferSize;

	/** 输入数据缓冲 */
	protected IByteBuffer readBuffer;

	/** 输出数据缓冲 */
	protected IByteBuffer sendBuffer;

	/** 创建时间 */
	protected long createdTime;

	/** 上次接受数据时间 */
	protected long lastReceiveTime;

	/** ping时间 */
	protected int pingTime;

	/** 接收到数据条数 */
	protected int receivedMessageCount;

	/** 总共接受到数据大小 */
	protected int receivedDataLength;

	/** 总共发出数据条数 */
	protected int sendedMessageCount;

	/** 总共发出数据长度 */
	protected int sendedDataLength;

	/** 接受数据时的解码器 */
	protected MessageDecoder messageDecoder;

	/** 发出数据时的编码器 */
	protected MessageEncoder messageEncdoer;

	/** message encrypt */
	protected IEncrypt messageEncrypt;

	/** message encrypt codekey */
	protected String codeKey;
	
	/**
	 * 连接空闲超时时间
	 */
	protected long idle;

	
	/** 对每个连接是否顺序处理接到的请求 */
	protected boolean order = false;

	protected MessageInfo info1 = new MessageInfo();

	protected MessageInfo info2 = new MessageInfo();

	protected MessageInfo cInfo = new MessageInfo();

	public static IByteBuffer data = new ByteBuffer();

	/** 是否继续读数据 */
	protected boolean continueReadData = true;

	public static class MessageInfo {
		public long time;

		public byte[] data;
	}

	public boolean isOrder() {
		return order;
	}

	public void setOrder(boolean order) {
		this.order = order;
	}

	public String getCodeKey() {
		return codeKey;
	}

	public void setCodeKey(String codeKey) {
		this.codeKey = codeKey;
	}

	public IEncrypt getMessageEncrypt() {
		return messageEncrypt;
	}

	public void setMessageEncrypt(IEncrypt messageEncrypt) {
		this.messageEncrypt = messageEncrypt;
	}

	/** 服务管理类 */
	protected ServiceManager serviceManager;

	/** 附加数据 */
	protected Object source;

	/** 已读取的数据队列 */
	protected Deque deque = new Deque(DATA_BUFFER_SIZE);

	protected long id;

	protected SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSS");

	/** 是否正在处理 */
	protected boolean isProcessing = false;

	protected volatile boolean isbusy = false;

	protected NioWriteDelay nioWritedelay;

	/** 添加观察者 */
	public void addListener(NetConnectionListener netconnectionlistener) {
		listeners.add(netconnectionlistener);
	}

	// /** 添加附加绑定数据 */
	// public void setAttachment(Object obj) {
	// attachment = obj;
	// }
	//
	// /** 得到附加绑定数据 */
	// public Object getAttachment() {
	// return attachment;
	// }

	/** 得到所有观察者数组 */
	public NetConnectionListener[] getAllListeners() {
		NetConnectionListener anetconnectionlistener[] = new NetConnectionListener[listeners
				.size()];
		listeners.toArray(anetconnectionlistener);
		return anetconnectionlistener;
	}

	/** 得到发送数据长度 */
	public int getReceivedDataLength() {
		return receivedDataLength;
	}

	/** 得到消息解码器 */
	public MessageDecoder getMessageDecoder() {
		return messageDecoder;
	}

	/** 得到消息编码器 */
	public void setMessageDecoder(MessageDecoder messagedecoder) {
		messageDecoder = messagedecoder;
	}

	/** 发出消息 */
	public void sendMessage(IByteBuffer message) {
		synchronized (sendBuffer) {
			messageEncdoer.encode(message, sendBuffer);
		}
		if (sendBuffer.length() > 4000)
			flush();
		sendedMessageCount++;
	}

	/** 立即发出消息 */
	public void sendAndFlushMessage(IByteBuffer message) {
		synchronized (sendBuffer) {
			messageEncdoer.encode(message, sendBuffer);
		}
		flush();
		sendedMessageCount++;
	}

	/** 得到发出消息长度 */
	public int getSendedDataLength() {
		return sendedDataLength;
	}

	/** 得到消息时通知 */
	public void dispatchMessage(IByteBuffer message) {
		receivedMessageCount++;
		for (int i = 0; i < listeners.size(); i++) {
			NetConnectionListener netconnectionlistener = listeners.get(i);
			netconnectionlistener.messageArrived(this, message);
		}

	}

	/** 移除观察者 */
	public void removeListener(NetConnectionListener netconnectionlistener) {
		listeners.remove(netconnectionlistener);
	}

	/** 从缓存中发出消息 */
	public void flush() {

		synchronized (sendBuffer) {
			if (isbusy) {
				return;
			}
			int i = sendBuffer.available();
			if (i > 0) {
				// i = Math.min(i, 8092);
				int sendlen = sendDataImpl(sendBuffer.getRawBytes(), 0, i);
				sendBuffer.setReadPos(sendlen);
				sendBuffer.pack();
				// sendBuffer.clear();
				sendedDataLength += sendlen;
				if (sendlen < i) {
					isbusy = true;
					nioWritedelay.sendDealy(this, sendBuffer);
					return;

				}

			}
			if (sendBuffer.length() >= 0x10000) {
				sendBuffer.clear();
			}
		}
	}

	/** 得到远程端口 */
	public abstract int getPort();

	/** 得到本地端口 */
	public abstract int getLocalPort();

	/** 从nio中发出消息 */
	public abstract int sendDataImpl(byte abyte0[], int i, int j);

	public abstract InetAddress getAddress();

	/** 关闭连接 */
	public abstract void close();

	/** 得到发出消息条数 */
	public int getSendedMessageCount() {
		return sendedMessageCount;
	}

	/** 读入数据，发出消息，通知连接的观察者，处理对应的读入数据 */
	protected void onDataRead(byte abyte0[], int offset, int length, long time) {
		lastReceiveTime = time;
		receivedDataLength += length;
		if (!this.continueReadData) {
			return;
		}
		if (messageDecoder != null) {
			readBuffer.writeBytes(abyte0, offset, length);
			IByteBuffer message = null;
			while ((message = messageDecoder.decode(readBuffer)) != null) {
				// log.debug("get Data from " + getHost() + " " + getPort()
				// + " id:" + id + " size:" + deque.size());
				synchronized (deque) {
					if (deque.isFull()) {
						continue;
					}

					deque.pushTail(message);

				}
				// 记录前后消息和时间
				if (info1.data == null) {
					// first time
					info1.data = message.getRawBytes().clone();
					info1.time = System.currentTimeMillis();
				}
				
				if (isOrder()) {
					if (!isProcessing) {
						isProcessing = true;
						dispatchMessage(message);
					}
				} else {
					isProcessing = true;
					dispatchMessage(message);
				}

				readBuffer.pack();

			}
		}
		if (readBuffer.available() > maxBufferSize) {
			synchronized (readBuffer) {
				readBuffer.clear();
				close();
			}
		}
	}

	protected static byte[] s843;

	/** 构造函数 */
	public NetConnection() {
		listeners = new ArrayList<NetConnectionListener>();
		maxBufferSize = 0x7fffffff;
		readBuffer = new ByteBuffer(MAX_SEND_SIZE);
		sendBuffer = new ByteBuffer(16384);
		pingTime = 0;

		this.id = System.currentTimeMillis();
		if (s843 == null) {
			String xml = "<cross-domain-policy> "
					+ "<allow-access-from domain=\"*\" to-ports=\"*\"/>"
					+ "</cross-domain-policy>\0";
			try {
				s843 = xml.getBytes("utf8");
			} catch (UnsupportedEncodingException e) {
			}
		}
	}

	/** 得到创建时间 */
	public long getCreatedTime() {
		return createdTime;
	}

	/** 设置创建时间 */
	public void setCreatedTime(long l) {
		createdTime = l;
	}

	/** 得到ping时间 */
	public int getPingTime() {
		return pingTime;
	}

	/** 设置ping时间 */
	public void setPingTime(int i) {
		pingTime = i;
	}
	
	/**
	 * 会话是否空闲检测
	 * 
	 * 如果会话创建时未设置超时时长，则代表永远不会超时
	 * 
	 * @param l
	 * @return
	 */
	public boolean idle(long l) {
		if (idle != 0) {
			if (l - lastReceiveTime > idle) {
				return true;
			}
		}
		
		return false;
	}

	/** 得到最大buffer长度 */
	public int getMaxBufferSize() {
		return maxBufferSize;
	}

	/** 设置最大buffer */
	public void setMaxBufferSize(int i) {
		maxBufferSize = i;
	}

	/** 主机地址 */
	public abstract String getHost();

	/** 发送ping消息 */
	protected void sendPingMessage() {
		sendAndFlushMessage(PingMessage.PING_MESSAGE);
	}

	/** 清空观察者 */
	public void removeAllListeners() {
		listeners.clear();
	}

	/** 得到解码器 */
	public MessageEncoder getMessageEncoder() {
		return messageEncdoer;
	}

	/** 设置解码器 */
	public void setMessageEncoder(MessageEncoder messageencoder) {
		messageEncdoer = messageencoder;
	}

	/** 得到接收到消息的条数 */
	public int getReceivedMessageCount() {
		return receivedMessageCount;
	}

	/** 得到目标ip的字节数 */
	public abstract byte[] getHostIP();

	/** 连接是否活动 */
	public abstract boolean isActive();

	/** 添加附加数据 */
	public void attach(Object ob) {
		this.source = ob;
	}

	/** 得到附加数据 */
	public Object attachment() {
		return this.source;
	}

	/** 取出新的任务 */
	private ByteBuffer popData() {
		synchronized (deque) {
			if (!deque.isEmpty()) {
				ByteBuffer popData = (ByteBuffer) (deque.popHead());
				this.cInfo.data = popData.getRawBytes().clone();
				this.cInfo.time = System.currentTimeMillis();
				return popData;
			}
			isProcessing = false;
			return null;
		}
	}

	/** 处理消息队列 */
	public void run() {
		IByteBuffer data;
		if (!continueReadData) {
			return;
		}
		while ((data = popData()) != null) {
			int id = 0;
			// 数据包解密处理
			IEncrypt encrypt = this.getMessageEncrypt();
			if (encrypt != null) {
				codeKey = data.readUTF();
				encrypt.coding(data.getRawBytes(), 1,
						data.getRawBytes().length - 1, codeKey);
			}
			try {
//				Desencryption.decrypt(data.getRawBytes());
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if (data.available() == 0) {
				try {
					continueReadData = false;
					// this.sendAndFlushMessage(data843);
					synchronized (sendBuffer) {
						sendBuffer.writeBytes(s843);
					}
					flush();
				} catch (Exception e) {
				}
				break;
			}
			try {
				id = data.readByte();
				if (id > 0) {
//					Desencryption.decrypt(data.getRawBytes(), 1);
				}
			} catch (Exception e) {
				this.close();

			}
			IService service = serviceManager.getService(id);
			
			if (service == null) {
				continue;
			}
			try {
				service.doMessage(this, data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static String getTimeString(long time) {
		SimpleDateFormat dataFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss SSS");
		return dataFormat.format(new Date(time));
	}

	public ServiceManager getServiceManager() {
		return serviceManager;
	}

	public void setServiceManager(ServiceManager serviceManager) {
		this.serviceManager = serviceManager;
	}

	public NioWriteDelay getNioWritedelay() {
		return nioWritedelay;
	}

	public void setNioWritedelay(NioWriteDelay nioWritedelay) {
		this.nioWritedelay = nioWritedelay;
	}

	public boolean isIsbusy() {
		return isbusy;
	}

	public void setIsbusy(boolean isbusy) {
		this.isbusy = isbusy;
	}
	public void setIdle(long idle) {
		this.idle = idle;
	}

}
