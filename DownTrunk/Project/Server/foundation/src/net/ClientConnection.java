package net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class ClientConnection implements Runnable {
	/** ���ӵ����������� */
	protected DataInputStream dis = null;

	/** ���ӵ���������� */
	protected DataOutputStream dos = null;

	/** �Ƿ��ڻ */
	private boolean active;

	/** socket���� */
	private Socket socket;

	/** Զ��������ַ */
	private String host;

	/** �������Ӷ˿� */
	private int port;

	/** �������ӵĶ˿� */
	private int localPort;

	/** ����������ip���ֽ���ʽ */
	private byte address[];

	/** ������Э���ַ */
	private InetAddress inetAddress;

	/** �ϴν������ݵ�ʱ�� */
	protected long lastReceiveTime;

	/** ���ӵĹ۲������� */
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

	/** �����ӣ�������Ӧ���� */
	private void open() {
		try {
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			close();
		}
	}

	/** ����������Ϣ */
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

	/** �õ���Ϣʱ֪ͨ */
	public void dispatchMessage(IByteBuffer message) {
		for (int i = 0; i < listeners.size(); i++) {
			NetConnectionListener netconnectionlistener = listeners.get(i);
			netconnectionlistener.messageArrived(null, message);
		}

	}

	/** ��ӹ۲��� */
	public void addListener(NetConnectionListener netconnectionlistener) {
		listeners.add(netconnectionlistener);
	}

	/** �����ж�ȡ���ݣ��������� */
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
