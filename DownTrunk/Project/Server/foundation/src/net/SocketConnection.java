/**
 * socket对应的连接
 */
package net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class SocketConnection extends NetConnection implements Runnable {
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

	private byte[] readbytes;

	public SocketConnection(Socket socket) {
		super();
		active = true;
		readbytes = new byte[1];
		this.socket = socket;
		this.host = socket.getInetAddress().getHostAddress();
		this.address = socket.getInetAddress().getAddress();
		this.port = socket.getPort();
		this.localPort = socket.getLocalPort();
		this.inetAddress = socket.getInetAddress();
		open();
	}

	@Override
	public InetAddress getAddress() {

		return this.inetAddress;
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

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public int sendDataImpl(byte abyte0[], int i, int j) {
		if (!active)
			return j;
		try {
			// log.debug("send data to " + getHost() + ":" + getPort() + " at "
			// + " port :" + socket.getLocalPort() + " datasize:" + j);
			dos.write(abyte0, i, j);
			dos.flush();
			return j;
		} catch (IOException e) {
			e.printStackTrace();
			close();
		}
		return j;
	}

	@Override
	public void close() {
		if (!active)
			return;
		Thread.dumpStack();
		active = false;
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getHost() {
		return this.host;
	}

	@Override
	public byte[] getHostIP() {
		return this.address;
	}

	@Override
	public int getLocalPort() {
		return this.localPort;
	}

	@Override
	public int getPort() {
		return this.port;
	}

	public InetAddress getInetAddress() {
		return inetAddress;
	}

	public void setInetAddress(InetAddress inetAddress) {
		this.inetAddress = inetAddress;
	}

	/** 从流中读取数据，阻塞方法 */
	protected IByteBuffer receive() {
		// try {
		//
		// int len = dis.readInt();
		// System.out.println("get data " +len);
		// if (len <= 0 || len > 400 * 1024)
		// throw new IOException(getClass().getName()
		// + " receive, Connection close");
		// byte[] data = new byte[len];
		// dis.readFully(data);
		// lastReceiveTime = System.currentTimeMillis();
		// this.receivedDataLength += (len + 4);
		// return new ByteBuffer(data);
		// } catch (Exception e) {
		// e.printStackTrace();
		// close();
		// }
		try {
			int l = 1;
			dis.readFully(readbytes);
			this.onDataRead(readbytes, 0, l, System.currentTimeMillis());

		} catch (Exception e) {
			e.printStackTrace();
			close();
		}
		return null;
	}

	/** 读入数据，发出消息，通知连接的观察者，处理对应的读入数据 */
	@Override
	protected void onDataRead(byte abyte0[], int i, int j, long l) {
		lastReceiveTime = l;
		receivedDataLength += j;
		if (messageDecoder != null) {
			readBuffer.writeBytes(abyte0, i, j);
			IByteBuffer message = null;
			while ((message = messageDecoder.decode(readBuffer)) != null) {
				// log.debug("get Data from " + getHost() + " " + getPort()
				// + " id:" + id + " size:" + deque.size());
				dispatchMessage(message);
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

	@Override
	public void run() {
		while (isActive()) {
			// IByteBuffer message = receive();
			// System.out.println("get message");
			// if (message != null) {
			// System.out.println("dispatchMessage message");
			// dispatchMessage(message);
			// }
			receive();
		}
	}

	@Override
	public boolean isClosed() {
		return !isActive();
	}

	@Override
	public void send(IByteBuffer data) {
		this.sendAndFlushMessage(data);
	}

}
