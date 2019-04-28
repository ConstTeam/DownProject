package net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NIOClientConnection implements Runnable, ISession, ISendData {
	/** ��д���ݵĻ�������С */
	public static final int RW_BUFFER_SIZE = 4 * 1024;

	/** ����ͨ�� */
	private SocketChannel channel;

	/** �Ƿ��ڻ */
	private boolean active;

	/** �������Ӷ˿� */
	private int port;

	/** ������Э���ַ */
	private InetAddress inetAddress;

	private Selector selector;

	private Socket socket;

	/** nio���ݶ�ջ */
	private ByteBuffer buffer;

	/** ���ݴ���ֽ����� */
	private byte data[];

	/** pingʱ�� */
	protected int pingTime;

	/** ��ʱʱ�� */
	protected int timeout;

	/** ���յ��������� */
	protected int receivedMessageCount;

	/** �ܹ����ܵ����ݴ�С */
	protected int receivedDataLength;

	/** �ϴν�������ʱ�� */
	protected long lastReceiveTime;

	/** ��������ʱ�Ľ����� */
	protected MessageDecoder messageDecoder;

	/** ��������ʱ�ı����� */
	protected MessageEncoder messageEncdoer;

	/** �������ݻ��� */
	protected IByteBuffer readBuffer;

	/** ������ݻ��� */
	protected IByteBuffer sendBuffer;

	/** ���buffer���� */
	protected int maxBufferSize;

	// /** ���ӵĹ۲������� */
	// protected ArrayList<ClientListener> listeners = new
	// ArrayList<ClientListener>();
	protected ClientListener listener;

	/** �������� */
	protected Object source;

	public static long clientCount = 0;

	/** ��ȡ����Ļ����� */
	private ByteBuffer writer = null;

	public volatile boolean isbusy = false;

	public static final int SEND_BUFFER_SIZE = 128 * 1024;

	protected static NioWriteDelay nioWritedelay = new NioWriteDelay();

	public NIOClientConnection(InetAddress inetAddress, int port) {
		maxBufferSize = 0x7fffffff;
		readBuffer = ByteBufferFactory.getNewByteBuffer(RW_BUFFER_SIZE);
		sendBuffer = ByteBufferFactory.getNewByteBuffer(16384);
		writer = ByteBuffer.allocate(RW_BUFFER_SIZE);
		this.inetAddress = inetAddress;
		this.port = port;
		data = new byte[RW_BUFFER_SIZE];
		buffer = ByteBuffer.wrap(data, 0, data.length);
	}

	public boolean start() {
		boolean flag = bind();

		if (!flag) {
			active = false;
			return false;
		} else {
			clientCount++;
			active = true;
			new Thread(this, "NIONetClientThread").start();
			return true;
		}
	}

	/** ��ӹ۲��� */
	public void addListener(ClientListener netconnectionlistener) {
		// if (!listeners.contains(netconnectionlistener))
		// {
		// log.debug("add Listener " + netconnectionlistener);
		// listeners.add(netconnectionlistener);
		// }
		listener = netconnectionlistener;
	}

	protected boolean bind() {
		try {
			channel = SocketChannel.open();
			socket = channel.socket();
			socket.setReceiveBufferSize(RW_BUFFER_SIZE);
			socket.setSendBufferSize(SEND_BUFFER_SIZE);
			socket.setPerformancePreferences(0, 2, 1);
			InetSocketAddress isa = new InetSocketAddress(getAddress(),
					getPort());
			socket.setReuseAddress(true);
			channel.connect(isa);
			channel.configureBlocking(false);
			selector = Selector.open();
			channel.register(selector, SelectionKey.OP_READ);
			return true;
		} catch (IOException ioexception) {
			close();
			ioexception.printStackTrace();
		}
		return false;
	}

	public void close() {
		if (!active)
			return;
		active = false;
		clientCount--;

		try {
			channel.socket().close();
		} catch (IOException ioexception) {
			ioexception.printStackTrace();
		}
		this.dispatchClose();

	}

	public InetAddress getAddress() {
		return inetAddress;
	}

	public String getHost() {
		return inetAddress.getHostAddress();
	}

	public byte[] getHostIP() {
		return inetAddress.getAddress();
	}

	public int getLocalPort() {
		return socket.getLocalPort();
	}

	public int getPort() {
		return this.port;
	}

	public boolean isActive() {
		return this.active;
	}

	public int sendDataImpl(byte[] abyte0, int offset, int len) {
		int total = len;
		if (!active)
			return len;
		try {
			if (channel.socket().isClosed()) {
				close();
				return len;
			}
		} catch (Exception e) {
			// active = false;
			close();
			e.printStackTrace();
		}
		try {
			int limit = 0, n = 0, r = 0;
			writer.clear();
			int i = RW_BUFFER_SIZE;
			for (; len > 0;) {
				if (i > len)
					i = len;
				writer.put(abyte0, offset, i);
				writer.flip();
				limit = writer.limit();
				r = channel.write(writer);
				n += r;
				offset += r;
				len -= r;
				writer.clear();
				if (r < limit) {
					return n;
				}
			}
		} catch (Exception e) {
			close();
			e.printStackTrace();
		}
		
		return total;
	}

	public boolean isClosed() {
		return !this.active;
	}

	public void send(IByteBuffer data) {
		this.sendAndFlushMessage(data);
	}

	/** ����������Ϣ */
	public void sendAndFlushMessage(IByteBuffer message) {
		synchronized (sendBuffer) {
			messageEncdoer.encode(message, sendBuffer);
		}
		flush();
	}

	/** ����select��Ӧ */
	private void processSelection(long l) {
		Set<SelectionKey> set = selector.selectedKeys();
		Iterator<SelectionKey> iterator = set.iterator();
		do {
			if (!iterator.hasNext())
				break;
			SelectionKey selectionkey = iterator.next();
			iterator.remove();
			if (selectionkey.isReadable()) {
				// �����ݶ�
				try {
					buffer.clear();
					int i = this.channel.read(buffer);
					if (i > 0) {
						// log.debug("receive data " + i);
						this.onDataRead(data, 0, i, l);
						// log.debug("total data " + totalBytesRead);
					}
				} catch (Exception ioexception1) {
					ioexception1.printStackTrace();
					selectionkey.cancel();
					// �д���ر�
					close();
				}
			}
		} while (true);
	}

	/** �õ���Ϣ������ */
	public MessageDecoder getMessageDecoder() {
		return messageDecoder;
	}

	/** �õ���Ϣ������ */
	public void setMessageDecoder(MessageDecoder messagedecoder) {
		messageDecoder = messagedecoder;
	}

	/** �������ݣ�������Ϣ��֪ͨ���ӵĹ۲��ߣ������Ӧ�Ķ������� */
	protected void onDataRead(byte abyte0[], int i, int j, long l) {
		synchronized (readBuffer) {
			lastReceiveTime = l;
			receivedDataLength += j;
			if (messageDecoder != null) {

				readBuffer.writeBytes(abyte0, i, j);

				IByteBuffer message = null;
				// log.debug("read data from " + getHost() + " " + getPort()
				// + " size:" + j);
				while ((message = messageDecoder.decode(readBuffer)) != null) {
					// log.debug("get data total " + getHost() + " " + getPort()
					// + " size:" + message.length());
					dispatchMessage(message);
					readBuffer.pack();
				}

			}
			if (readBuffer.available() > maxBufferSize) {

				readBuffer.clear();
				close();
			}
		}
	}

	/** �õ���Ϣʱ֪ͨ */
	public void dispatchMessage(IByteBuffer message) {
		receivedMessageCount++;
		// for (int i = 0; i < listeners.size(); i++)
		// {
		// ClientListener netconnectionlistener = listeners.get(i);
		// netconnectionlistener.messageArrived(this, message);
		// }
		listener.messageArrived(this, message);

	}

	/** �õ���Ϣʱ֪ͨ */
	public void dispatchClose() {
		// for (int i = 0; i < listeners.size(); i++)
		// {
		//
		// ClientListener netconnectionlistener = listeners.get(i);
		// log.debug("close notice " + netconnectionlistener);
		// netconnectionlistener.onClosed(this);
		// }
		listener.onClosed(this);

	}

	public void run() {
		int i = 0;
		// ��ʱ��������Ƿ��ڻ״̬
		while (active) {

			long l = System.currentTimeMillis();

			try {
				i = selector.selectNow();
			} catch (IOException ioexception) {
				ioexception.printStackTrace();
			}
			try {
				if (i > 0)
					processSelection(l);
			} catch (Exception ioexception) {
				ioexception.printStackTrace();
			}
			try {
				update(l);
			} catch (Exception ioexception) {
				ioexception.printStackTrace();
			}
			// ����5����
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	/** �ӻ����з�����Ϣ */
	public void flush() {

		synchronized (sendBuffer) {

			if (isbusy) {
				return;
			}
			int i = sendBuffer.length();
			if (i > 0) {
				// i = Math.min(i, 8092);
				int sendlen = sendDataImpl(sendBuffer.getRawBytes(), 0, i);
				sendBuffer.setReadPos(sendlen);
				sendBuffer.pack();

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

	/** �õ������� */
	public MessageEncoder getMessageEncoder() {
		return messageEncdoer;
	}

	/** ���ý����� */
	public void setMessageEncoder(MessageEncoder messageencoder) {
		messageEncdoer = messageencoder;
	}

	/** ��Ӹ������� */
	public void attach(Object ob) {
		this.source = ob;
	}

	/** ���ӳ�ʱ��� */
	public void update(long l) {
		if (lastReceiveTime == 0L)
			lastReceiveTime = l;
	}

	/** �õ���ʱʱ�� */
	public int getTimeout() {
		return timeout;
	}

	/** ���ó�ʱʱ�� */
	public void setTimeout(int i) {
		timeout = i;
	}

	/** �õ��������� */
	public Object attachment() {
		return this.source;
	}

	public boolean isIsbusy() {
		return isbusy;
	}

	public void setIsbusy(boolean isbusy) {
		this.isbusy = isbusy;
	}

}
