package net;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import util.Deque;

public abstract class NetConnection implements ISession, Runnable, ISendData {

	/** ���ݻ�������С */
	public static final int DATA_BUFFER_SIZE = 64;

	public static boolean NET_DEBUG = true;

	/** ��󻺴����� */
	public static final int MAX_STORED_BYTES = 0x10000;

	/** ��������� */
	public static final int MAX_SEND_SIZE = 4 * 1024;

	/** ���ӵĹ۲������� */
	protected ArrayList<NetConnectionListener> listeners = new ArrayList<NetConnectionListener>();

	// /** ���Ӷ�Ӧ������ */
	// protected Object attachment;
	/** ���buffer���� */
	protected int maxBufferSize;

	/** �������ݻ��� */
	protected IByteBuffer readBuffer;

	/** ������ݻ��� */
	protected IByteBuffer sendBuffer;

	/** ����ʱ�� */
	protected long createdTime;

	/** �ϴν�������ʱ�� */
	protected long lastReceiveTime;

	/** pingʱ�� */
	protected int pingTime;

	/** ���յ��������� */
	protected int receivedMessageCount;

	/** �ܹ����ܵ����ݴ�С */
	protected int receivedDataLength;

	/** �ܹ������������� */
	protected int sendedMessageCount;

	/** �ܹ��������ݳ��� */
	protected int sendedDataLength;

	/** ��������ʱ�Ľ����� */
	protected MessageDecoder messageDecoder;

	/** ��������ʱ�ı����� */
	protected MessageEncoder messageEncdoer;

	/** message encrypt */
	protected IEncrypt messageEncrypt;

	/** message encrypt codekey */
	protected String codeKey;
	
	/**
	 * ���ӿ��г�ʱʱ��
	 */
	protected long idle;

	
	/** ��ÿ�������Ƿ�˳����ӵ������� */
	protected boolean order = false;

	protected MessageInfo info1 = new MessageInfo();

	protected MessageInfo info2 = new MessageInfo();

	protected MessageInfo cInfo = new MessageInfo();

	public static IByteBuffer data = new ByteBuffer();

	/** �Ƿ���������� */
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

	/** ��������� */
	protected ServiceManager serviceManager;

	/** �������� */
	protected Object source;

	/** �Ѷ�ȡ�����ݶ��� */
	protected Deque deque = new Deque(DATA_BUFFER_SIZE);

	protected long id;

	protected SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSS");

	/** �Ƿ����ڴ��� */
	protected boolean isProcessing = false;

	protected volatile boolean isbusy = false;

	protected NioWriteDelay nioWritedelay;

	/** ��ӹ۲��� */
	public void addListener(NetConnectionListener netconnectionlistener) {
		listeners.add(netconnectionlistener);
	}

	// /** ��Ӹ��Ӱ����� */
	// public void setAttachment(Object obj) {
	// attachment = obj;
	// }
	//
	// /** �õ����Ӱ����� */
	// public Object getAttachment() {
	// return attachment;
	// }

	/** �õ����й۲������� */
	public NetConnectionListener[] getAllListeners() {
		NetConnectionListener anetconnectionlistener[] = new NetConnectionListener[listeners
				.size()];
		listeners.toArray(anetconnectionlistener);
		return anetconnectionlistener;
	}

	/** �õ��������ݳ��� */
	public int getReceivedDataLength() {
		return receivedDataLength;
	}

	/** �õ���Ϣ������ */
	public MessageDecoder getMessageDecoder() {
		return messageDecoder;
	}

	/** �õ���Ϣ������ */
	public void setMessageDecoder(MessageDecoder messagedecoder) {
		messageDecoder = messagedecoder;
	}

	/** ������Ϣ */
	public void sendMessage(IByteBuffer message) {
		synchronized (sendBuffer) {
			messageEncdoer.encode(message, sendBuffer);
		}
		if (sendBuffer.length() > 4000)
			flush();
		sendedMessageCount++;
	}

	/** ����������Ϣ */
	public void sendAndFlushMessage(IByteBuffer message) {
		synchronized (sendBuffer) {
			messageEncdoer.encode(message, sendBuffer);
		}
		flush();
		sendedMessageCount++;
	}

	/** �õ�������Ϣ���� */
	public int getSendedDataLength() {
		return sendedDataLength;
	}

	/** �õ���Ϣʱ֪ͨ */
	public void dispatchMessage(IByteBuffer message) {
		receivedMessageCount++;
		for (int i = 0; i < listeners.size(); i++) {
			NetConnectionListener netconnectionlistener = listeners.get(i);
			netconnectionlistener.messageArrived(this, message);
		}

	}

	/** �Ƴ��۲��� */
	public void removeListener(NetConnectionListener netconnectionlistener) {
		listeners.remove(netconnectionlistener);
	}

	/** �ӻ����з�����Ϣ */
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

	/** �õ�Զ�̶˿� */
	public abstract int getPort();

	/** �õ����ض˿� */
	public abstract int getLocalPort();

	/** ��nio�з�����Ϣ */
	public abstract int sendDataImpl(byte abyte0[], int i, int j);

	public abstract InetAddress getAddress();

	/** �ر����� */
	public abstract void close();

	/** �õ�������Ϣ���� */
	public int getSendedMessageCount() {
		return sendedMessageCount;
	}

	/** �������ݣ�������Ϣ��֪ͨ���ӵĹ۲��ߣ������Ӧ�Ķ������� */
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
				// ��¼ǰ����Ϣ��ʱ��
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

	/** ���캯�� */
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

	/** �õ�����ʱ�� */
	public long getCreatedTime() {
		return createdTime;
	}

	/** ���ô���ʱ�� */
	public void setCreatedTime(long l) {
		createdTime = l;
	}

	/** �õ�pingʱ�� */
	public int getPingTime() {
		return pingTime;
	}

	/** ����pingʱ�� */
	public void setPingTime(int i) {
		pingTime = i;
	}
	
	/**
	 * �Ự�Ƿ���м��
	 * 
	 * ����Ự����ʱδ���ó�ʱʱ�����������Զ���ᳬʱ
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

	/** �õ����buffer���� */
	public int getMaxBufferSize() {
		return maxBufferSize;
	}

	/** �������buffer */
	public void setMaxBufferSize(int i) {
		maxBufferSize = i;
	}

	/** ������ַ */
	public abstract String getHost();

	/** ����ping��Ϣ */
	protected void sendPingMessage() {
		sendAndFlushMessage(PingMessage.PING_MESSAGE);
	}

	/** ��չ۲��� */
	public void removeAllListeners() {
		listeners.clear();
	}

	/** �õ������� */
	public MessageEncoder getMessageEncoder() {
		return messageEncdoer;
	}

	/** ���ý����� */
	public void setMessageEncoder(MessageEncoder messageencoder) {
		messageEncdoer = messageencoder;
	}

	/** �õ����յ���Ϣ������ */
	public int getReceivedMessageCount() {
		return receivedMessageCount;
	}

	/** �õ�Ŀ��ip���ֽ��� */
	public abstract byte[] getHostIP();

	/** �����Ƿ� */
	public abstract boolean isActive();

	/** ��Ӹ������� */
	public void attach(Object ob) {
		this.source = ob;
	}

	/** �õ��������� */
	public Object attachment() {
		return this.source;
	}

	/** ȡ���µ����� */
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

	/** ������Ϣ���� */
	public void run() {
		IByteBuffer data;
		if (!continueReadData) {
			return;
		}
		while ((data = popData()) != null) {
			int id = 0;
			// ���ݰ����ܴ���
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
