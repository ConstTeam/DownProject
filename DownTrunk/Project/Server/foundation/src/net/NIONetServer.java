/**
 * ������nio������
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
	/** ��д���ݵĻ�������С */
	public static final int RW_BUFFER_SIZE = 4 * 1024;

	/** ����������ͨ�� */
	private ServerSocketChannel serverChannel;

	/** ��������Ӧ��socket */
	private ServerSocket serverSocket;

	private Selector selector;

	private SelectionKey serverKey;

	/** �Ƿ��Ѿ����� */
	private boolean running;

	/** ���ݴ���ֽ����� */
	private byte data[];

	/** nio���ݶ�ջ */
	private ByteBuffer buffer;

	/** ���е��������� */
	private ArrayList<NetConnection> connectionList;

	public static final int SEND_BUFFER_SIZE = 128 * 1024;

	protected NioWriteDelay nioWritedelay;

	public NIONetServer() {
		data = new byte[RW_BUFFER_SIZE];
		nioWritedelay = new NioWriteDelay();
		buffer = ByteBuffer.wrap(data, 0, data.length);
		connectionList = new ArrayList<NetConnection>(1000);
	}

	/** �õ��Ĵ�С */
	@Override
	public int getConnectionCount() {
		return connectionList.size();
	}

	/**
	 * ��ʱ��Ȿ���������м���߳��������г�ʱҲ�����ڴ����Ựʱ���á�
	 * ���ø�
	 */
	public void run() {
		running = true;
		int i = 0;
		// ��ʱ��������Ƿ��ڻ״̬
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
				Thread.sleep(5);// ����5����
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/** ���������� */
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

	/** ֹͣ������ */
	@Override
	public void stop() {
		running = false;

		serverClosed();
	}

	/** �������������󶨷���˿� */
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

	/** ����select��Ӧ */
	private void processSelection(long l) {
		Set<SelectionKey> set = selector.selectedKeys();
		Iterator<SelectionKey> iterator = set.iterator();

		while (iterator.hasNext()) {
			SelectionKey selectionkey = iterator.next();
			iterator.remove();
			// ���ӽ���
			if (selectionkey == serverKey) {
				if (selectionkey.isAcceptable())
					try {
						SocketChannel socketchannel = serverChannel.accept();
						socketchannel.configureBlocking(false);
						socketchannel.socket().setSendBufferSize(
								SEND_BUFFER_SIZE);
						// socketchannel.socket().setTcpNoDelay(false);
						// ע���
						SelectionKey selectionkey1 = socketchannel.register(
								selector, 1);
						// �½�һ�����Ӷ���
						NIOConnection nioconnection2 = new NIOConnection(
								socketchannel, this);
						nioconnection2.setNioWritedelay(nioWritedelay);
						// ��������ʱ��
						nioconnection2.setCreatedTime(System
								.currentTimeMillis());
						selectionkey1.attach(nioconnection2);
						// ��ӵ������б�
						connectionList.add(nioconnection2);
						// ���ý�������֪ͨ�۲��������ӽ���
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
				// �����ݶ�
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
					// �д���ر�
					nioconnection1.close();
					selectionkey.cancel();
				}
			}
		}
	}

}
