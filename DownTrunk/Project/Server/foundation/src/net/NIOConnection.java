/**
 * nioserver对应的nio连接
 */
package net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NIOConnection extends NetConnection {
	/** 读写数据的缓冲区大小 */
	public static final int RW_BUFFER_SIZE = 4 * 1024;

	/** 连接通道 */
	private SocketChannel channel;

	/** 是否处于活动 */
	private boolean active;

	/** 服务连接端口 */
	private int port;

	/** 服务连接ip */
	private String host;

	/** 本地连接的端口 */
	private int localPort;

	/** 服务器连接ip的字节形式 */
	private byte address[];

	/** 服务器 */
	private NetServer netServer;

	/** 互联网协议地址 */
	private InetAddress inetAddress;

	/** 读取输出的缓冲区 */
	private ByteBuffer writer = null;

	/** 构造函数 */
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

	/** 得到服务ip */
	@Override
	public String getHost() {
		return host;
	}

	/** 得到服务ip的字节形式 */
	@Override
	public byte[] getHostIP() {
		return address;
	}

	/** 得到服务端口 */
	@Override
	public int getPort() {
		return port;
	}

	/** 得到本地连接端口 */
	@Override
	public int getLocalPort() {
		return localPort;
	}

	/** 该连接是否活动 */
	@Override
	public boolean isActive() {
		return active;
	}

	/** 得到连接通道 */
	public SocketChannel getSocketChannel() {
		return channel;
	}

	/** 关闭连接 */
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

	/** 设置连接为不活跃状态 */
	void onClosed() {
		active = false;
	}

	/** 发出数据 */
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
