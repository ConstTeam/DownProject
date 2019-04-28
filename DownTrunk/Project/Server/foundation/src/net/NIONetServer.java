/**
 * 非阻塞nio服务器
 */
package net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class NIONetServer extends NetServer implements Runnable {
	/** 读写数据的缓冲区大小 */
	public static final int RW_BUFFER_SIZE = 4 * 1024;

	/** 服务器连接通道 */
	private ServerSocketChannel serverChannel;

	/** 服务器对应的socket */
	private ServerSocket serverSocket;

	private Selector selector;

	private SelectionKey serverKey;

	/** 是否已经启动 */
	private boolean running;

	/** 数据存放字节数组 */
	private byte data[];

	/** nio数据堆栈 */
	private ByteBuffer buffer;

	/** 所有的连接数组 */
	private ArrayList<NetConnection> connectionList;

	public static final int SEND_BUFFER_SIZE = 128 * 1024;

	protected NioWriteDelay nioWritedelay;

	public NIONetServer() {
		data = new byte[RW_BUFFER_SIZE];
		nioWritedelay = new NioWriteDelay();
		buffer = ByteBuffer.wrap(data, 0, data.length);
		connectionList = new ArrayList<NetConnection>(1000);
	}

	/** 得到的大小 */
	@Override
	public int getConnectionCount() {
		return connectionList.size();
	}

	/**
	 * 定时检测本该启动空闲检测线程做；空闲超时也不该在创建会话时设置。
	 * 懒得改
	 */
	public void run() {
		running = true;
		int i = 0;
		// 定时检测连接是否处于活动状态
		while (running) {
			long l = System.currentTimeMillis();
			
			Iterator<NetConnection> iterator = connectionList.iterator();
			while (iterator.hasNext()) {
				NetConnection next = iterator.next();
				if (next.isActive()) {
					if (next.idle(l)) {
						iterator.remove();
						connectionClosed(next);
					}
				} else {
					iterator.remove();
					connectionClosed(next);
				}
			}

			try {
				i = selector.selectNow();
				if (i > 0) {
					processSelection(l);
				}
			} catch (Exception ioexception) {
				ioexception.printStackTrace();
			}

			try {
				Thread.sleep(5);// 休眠5毫秒
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/** 启动服务器 */
	@Override
	public boolean start() {
		boolean flag = bindServer();
		if (!flag) {
			return false;
		} else {
			(new Thread(this, "NIONetServerThread")).start();
			serverOpened();
			return true;
		}
	}

	/** 停止服务器 */
	@Override
	public void stop() {
		running = false;

		serverClosed();
	}

	/** 开启服务器，绑定服务端口 */
	private boolean bindServer() {
		try {
			serverChannel = ServerSocketChannel.open();
			serverSocket = serverChannel.socket();
			serverSocket.setReceiveBufferSize(RW_BUFFER_SIZE);
			serverSocket.setPerformancePreferences(0, 2, 1);
			serverSocket.bind(new InetSocketAddress(getAddress(), getPort()));
			serverChannel.configureBlocking(false);
			selector = Selector.open();
			serverKey = serverChannel.register(selector, 16);
			return true;
		} catch (IOException ioexception) {
			ioexception.printStackTrace();
		}
		return false;
	}

	/** 处理select响应 */
	private void processSelection(long l) {
		Set<SelectionKey> set = selector.selectedKeys();
		Iterator<SelectionKey> iterator = set.iterator();

		while (iterator.hasNext()) {
			SelectionKey selectionkey = iterator.next();
			iterator.remove();
			// 连接进入
			if (selectionkey == serverKey) {
				if (selectionkey.isAcceptable())
					try {
						SocketChannel socketchannel = serverChannel.accept();
						socketchannel.configureBlocking(false);
						socketchannel.socket().setSendBufferSize(
								SEND_BUFFER_SIZE);
						// socketchannel.socket().setTcpNoDelay(false);
						// 注册读
						SelectionKey selectionkey1 = socketchannel.register(
								selector, 1);
						// 新建一个连接对象
						NIOConnection nioconnection2 = new NIOConnection(
								socketchannel, this);
						nioconnection2.setNioWritedelay(nioWritedelay);
						// 设置连接时间
						nioconnection2.setCreatedTime(System
								.currentTimeMillis());
						selectionkey1.attach(nioconnection2);
						// 添加到连接列表
						connectionList.add(nioconnection2);
						// 设置解码器，通知观察者有连接进入
						connectionOpened(nioconnection2);
					} catch (IOException ioexception) {
						try {
							Thread.sleep(100);
						} catch (Exception e) {
							e.printStackTrace();
						}
						ioexception.printStackTrace();
					}
			} else if (!selectionkey.isValid()) {
				NIOConnection nioconnection = (NIOConnection) selectionkey
						.attachment();
				if (nioconnection != null) {
					nioconnection.onClosed();
				}
			} else if (selectionkey.isReadable()) {
				// 有数据读
				NIOConnection nioconnection1 = (NIOConnection) selectionkey
						.attachment();
				try {
					SocketChannel socketchannel1 = (SocketChannel) selectionkey
							.channel();
					buffer.clear();
					int i = socketchannel1.read(buffer);
					if (i > 0) {
						nioconnection1.onDataRead(data, 0, i, l);
						totalBytesRead += i;
					} else if (i == 0) {
					}
				} catch (Exception ioexception1) {
//					ioexception1.printStackTrace();
					// 有错误关闭
					nioconnection1.close();
					selectionkey.cancel();
				}
			}
		}
	}

}
