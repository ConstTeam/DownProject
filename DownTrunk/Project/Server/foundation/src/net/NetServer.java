/**
 * ������
 */

package net;

import java.net.InetAddress;
import java.util.ArrayList;

public abstract class NetServer {

	/** ��Ϸ�����˿� */
	private int port;

	/** ��Ϸ������ip */
	private String localHost;

	/** �������Ĺ۲��� */
	public ArrayList<NetServerListener> listeners;

	/** ��������� */
	private int maxConnections;

	/** �ܹ��������ֽ��� */
	protected long totalBytesSended;

	/** �ܹ��յ����ֽ��� */
	protected long totalBytesRead;

	/** �������ͱ��������� */
	private MessageCodecFactory factory;

	/** encrypt message from client */
	protected IEncrypt encrypt;

	/** ��ÿ�������Ƿ�˳����ӵ������� */
	protected boolean order = false;

	public void setOrder(boolean order) {
		this.order = order;
	}

	public IEncrypt getEncrypt() {
		return encrypt;
	}

	public void setEncrypt(IEncrypt encrypt) {
		this.encrypt = encrypt;
	}

	/** �رշ����� */
	public abstract void stop();

	/** �õ������� */
	public abstract int getConnectionCount();

	/** ��չ۲��� */
	public synchronized void removeAllNetServerListeners() {
		listeners.clear();
	}

	/** ���캯�� */
	public NetServer() {
		listeners = new ArrayList<NetServerListener>();
	}

	/** ���ý���ͱ��빤�� */
	public void setMessageCodecFactory(MessageCodecFactory messagecodecfactory) {
		factory = messagecodecfactory;
	}

	/** �õ�����ͱ��빤�� */
	public MessageCodecFactory getMessageCodecFactory() {
		return factory;
	}

	/** �������� */
	public abstract boolean start();

	/** �Ƴ�һ���۲��� */
	public synchronized void removeNetServerListener(
			NetServerListener netserverlistener) {
		listeners.remove(netserverlistener);
	}

	/** �����Ѷ����� */
	public void addReadBytes(int i) {
		totalBytesRead += i;
	}

	/** �����ѷ����� */
	public void addSendedBytes(int i) {
		totalBytesSended += i;
	}

	/** ����һ���۲��� */
	public synchronized void addNetServerListener(
			NetServerListener netserverlistener) {
		listeners.add(netserverlistener);
	}

	public synchronized NetServerListener[] getNetServerListener() {
		NetServerListener[] arrs = new NetServerListener[listeners.size()];
		listeners.toArray(arrs);
		return arrs;
	}

	/** ������������� */
	public void setMaxConnections(int i) {
		maxConnections = i;
	}

	/** �õ���������� */
	public int getMaxConnections() {
		return maxConnections;
	}

	/** ��ʼ���˿� */
	public void init(int i) {
		port = i;
	}

	/** �õ��ܹ��յ����ֽ��� */
	public long getTotalBytesRead() {
		return totalBytesRead;
	}

	/** �õ��ܹ��������ֽ��� */
	public long getTotalBytesSended() {
		return totalBytesSended;
	}

	/** �������ӽ���ʱ�����ý�������֪ͨ��Ӧ�Ĺ۲��� */
	protected void connectionOpened(NetConnection netconnection) {
		if (factory != null) {
			netconnection.setMessageDecoder(factory.createDecoder());
			netconnection.setMessageEncoder(factory.createEncoder());
		}
		netconnection.setOrder(this.order);
		int i = listeners.size();
		for (int j = 0; j < i; j++) {
			NetServerListener netserverlistener = listeners.get(j);
			netserverlistener.connectionOpened(netconnection);
		}
		if (encrypt != null) {
			netconnection.setMessageEncrypt(encrypt);

			String codeKey = String.valueOf(netconnection.hashCode());
			netconnection.setCodeKey(codeKey);
		}
	}

	/** �������� */
	protected void serverOpened() {
		int i = listeners.size();
		for (int j = 0; j < i; j++) {
			NetServerListener netserverlistener = listeners.get(j);
			netserverlistener.serverOpened();
		}
	}

	/** ����ر� */
	protected void serverClosed() {
		int i = listeners.size();
		for (int j = 0; j < i; j++) {
			NetServerListener netserverlistener = listeners.get(j);
			netserverlistener.serverClosed();
		}
	}

	/** �������ӹر�ʱ��֪ͨ��Ӧ�Ĺ۲��� */
	protected void connectionClosed(NetConnection netconnection) {
		int i = listeners.size();
		for (int j = 0; j < i; j++) {
			NetServerListener netserverlistener = listeners.get(j);
			netserverlistener.connectionClosed(netconnection);
		}

	}

	/** �õ��������˿� */
	public int getPort() {
		return port;
	}

	/** ������ӷ������ĵ�ַ */
	public InetAddress getAddress() {
		InetAddress address = null;
		try {
			if (localHost == null) {
				address = InetAddress.getByName("0.0.0.0");
				// address = InetAddress.getLocalHost();
			} else {
				address = InetAddress.getByName(localHost);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return address;
	}

	/** ���÷������˿� */
	public void setPort(int i) {
		port = i;
	}

	/** �õ�������������ip */
	public String getLocalHost() {
		return localHost;
	}

	/** ���÷�����������ip */
	public void setLocalHost(String localHost) {
		this.localHost = localHost;
	}

}
