/**
 * nioserver��Ӧ��nio����
 */
package net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NIOConnection extends NetConnection {
	/** ��д���ݵĻ�������С */
	public static final int RW_BUFFER_SIZE = 4 * 1024;

	/** ����ͨ�� */
	private SocketChannel channel;

	/** �Ƿ��ڻ */
	private boolean active;

	/** �������Ӷ˿� */
	private int port;

	/** ��������ip */
	private String host;

	/** �������ӵĶ˿� */
	private int localPort;

	/** ����������ip���ֽ���ʽ */
	private byte address[];

	/** ������ */
	private NetServer netServer;

	/** ������Э���ַ */
	private InetAddress inetAddress;

	/** ��ȡ����Ļ����� */
	private ByteBuffer writer = null;

	/** ���캯�� */
	public NIOConnection(SocketChannel socketchannel, NetServer netserver) {
		super();
		active = true;
		channel = socketchannel;
		Socket socket = socketchannel.socket();
		host = socket.getInetAddress().getHostAddress();
		address = socket.getInetAddress().getAddress();
		port = socket.getPort();
		localPort = socket.getLocalPort();
		netServer = netserver;
		this.inetAddress = socket.getInetAddress();
		lastReceiveTime = System.currentTimeMillis();
		writer = ByteBuffer.allocate(RW_BUFFER_SIZE);
	}

	/** �õ�����ip */
	@Override
	public String getHost() {
		return host;
	}

	/** �õ�����ip���ֽ���ʽ */
	@Override
	public byte[] getHostIP() {
		return address;
	}

	/** �õ�����˿� */
	@Override
	public int getPort() {
		return port;
	}

	/** �õ��������Ӷ˿� */
	@Override
	public int getLocalPort() {
		return localPort;
	}

	/** �������Ƿ� */
	@Override
	public boolean isActive() {
		return active;
	}

	/** �õ�����ͨ�� */
	public SocketChannel getSocketChannel() {
		return channel;
	}

	/** �ر����� */
	@Override
	public void close() {
		if (!active)
			return;
		active = false;

		try {
			channel.socket().close();
		} catch (IOException ioexception) {
			ioexception.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return (new StringBuilder()).append("NIOConnection[host=").append(host)
				.append(";port=").append(port).append("]").toString();
	}

	/** ��������Ϊ����Ծ״̬ */
	void onClosed() {
		active = false;
	}

	/** �������� */
	@Override
	public int sendDataImpl(byte abyte0[], int offset, int len) {
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
		}
		try {
			int limit = 0, n = 0, r = 0;
			writer.clear();
			int i = RW_BUFFER_SIZE;
			for (; len > 0; offset += i, len -= i) {
				if (i > len)
					i = len;
				writer.put(abyte0, offset, i);
				writer.flip();
				limit = writer.limit();
				r = channel.write(writer);
				n += r;

				if (netServer != null) {
					netServer.addSendedBytes(r);
				}
				writer.clear();
				if (r < limit) {
					return n;
				}
			}
		} catch (Exception e) {
			// active = false;
			close();
			e.printStackTrace();
		}
		// ByteBuffer bytebuffer = ByteBuffer.wrap(abyte0, offset, len);
		// try
		// {
		// // log.debug(getHost() + " send data to client at "
		// // + dateFormat.format(new Date()));
		// len = channel.write(bytebuffer);
		// if (netServer != null)
		// netServer.addSendedBytes(len);
		// }
		// catch (IOException ioexception)
		// {
		// // active = false;
		// close();
		// ioexception.printStackTrace();
		//
		// }
		return total;
	}

	@Override
	public boolean isClosed() {
		return !isActive();
	}

	@Override
	public void send(IByteBuffer data) {
		this.sendAndFlushMessage(data);
	}

	@Override
	public InetAddress getAddress() {
		return this.inetAddress;
	}

}
