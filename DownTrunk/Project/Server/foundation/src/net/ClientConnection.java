package net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class ClientConnection implements Runnable {
	/** 连接的数据输入流 */
	protected DataInputStream dis = null;

	/** 连接的数据输出流 */
	protected DataOutputStream dos = null;

	/** 是否处于活动 */
	private boolean active;

	/** socket连接 */
	private Socket socket;

	/** 远程主机地址 */
	private String host;

	/** 服务连接端口 */
	private int port;

	/** 本地连接的端口 */
	private int localPort;

	/** 服务器连接ip的字节形式 */
	private byte address[];

	/** 互联网协议地址 */
	private InetAddress inetAddress;

	/** 上次接受数据的时间 */
	protected long lastReceiveTime;

	/** 连接的观察者数组 */
	private ArrayList<NetConnectionListener> listeners = new ArrayList<NetConnectionListener>();

	public ClientConnection(Socket socket) {
		active = true;
		this.socket = socket;
		this.host = socket.getInetAddress().getHostAddress();
		this.address = socket.getInetAddress().getAddress();
		this.port = socket.getPort();
		this.localPort = socket.getLocalPort();
		this.inetAddress = socket.getInetAddress();
		open();
	}

	/** 打开连接，建立对应的流 */
	private void open() {
		try {
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			close();
		}
	}

	/** 立即发出消息 */
	public void sendAndFlushMessage(IByteBuffer message) {
		try {
			dos.writeInt(message.length());
			dos.write(message.getRawBytes(), message.getReadPos(),
					message.length());
			dos.flush();
		} catch (Exception e) {
			close();
		}
	}

	public void close() {
		if (!active)
			return;
		active = false;
		try {
			socket.close();
		} catch (IOException ioexception) {
		}
	}

	public String getHost() {
		return this.host;
	}

	public byte[] getHostIP() {
		return this.address;
	}

	public int getLocalPort() {
		return this.localPort;
	}

	public int getPort() {
		return this.port;
	}

	public boolean isActive() {
		return this.active;
	}

	protected int sendDataImpl(byte[] abyte0, int i, int j) {
		try {
			dos.write(abyte0, i, j);
			dos.flush();
		} catch (IOException e) {
			close();
		}

		return j;
	}

	public InetAddress getInetAddress() {
		return inetAddress;
	}

	public void setInetAddress(InetAddress inetAddress) {
		this.inetAddress = inetAddress;
	}

	public void run() {
		while (isActive()) {
			IByteBuffer message = receive();

			if (message != null) {
				dispatchMessage(message);
			}
		}
	}

	/** 得到消息时通知 */
	public void dispatchMessage(IByteBuffer message) {
		for (int i = 0; i < listeners.size(); i++) {
			NetConnectionListener netconnectionlistener = listeners.get(i);
			netconnectionlistener.messageArrived(null, message);
		}

	}

	/** 添加观察者 */
	public void addListener(NetConnectionListener netconnectionlistener) {
		listeners.add(netconnectionlistener);
	}

	/** 从流中读取数据，阻塞方法 */
	protected IByteBuffer receive() {
		try {
			int len = dis.readInt();
			if (len <= 0 || len > 400 * 1024)
				throw new IOException(getClass().getName()
						+ " receive, Connection close");
			byte[] data = new byte[len];
			dis.readFully(data);
			lastReceiveTime = System.currentTimeMillis();
			return new ByteBuffer(data);
		} catch (Exception e) {
			close();
		}
		return null;
	}

}
