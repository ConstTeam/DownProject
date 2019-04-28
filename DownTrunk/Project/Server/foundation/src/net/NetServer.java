/**
 * 服务器
 */

package net;

import java.net.InetAddress;
import java.util.ArrayList;

public abstract class NetServer {

	/** 游戏启动端口 */
	private int port;

	/** 游戏启动绑定ip */
	private String localHost;

	/** 服务器的观察者 */
	public ArrayList<NetServerListener> listeners;

	/** 最大连接数 */
	private int maxConnections;

	/** 总共发出的字节数 */
	protected long totalBytesSended;

	/** 总共收到的字节数 */
	protected long totalBytesRead;

	/** 解码器和编码器工厂 */
	private MessageCodecFactory factory;

	/** encrypt message from client */
	protected IEncrypt encrypt;

	/** 对每个连接是否顺序处理接到的请求 */
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

	/** 关闭服务器 */
	public abstract void stop();

	/** 得到连接数 */
	public abstract int getConnectionCount();

	/** 清空观察者 */
	public synchronized void removeAllNetServerListeners() {
		listeners.clear();
	}

	/** 构造函数 */
	public NetServer() {
		listeners = new ArrayList<NetServerListener>();
	}

	/** 设置解码和编码工厂 */
	public void setMessageCodecFactory(MessageCodecFactory messagecodecfactory) {
		factory = messagecodecfactory;
	}

	/** 得到解码和编码工厂 */
	public MessageCodecFactory getMessageCodecFactory() {
		return factory;
	}

	/** 启动服务 */
	public abstract boolean start();

	/** 移除一个观察者 */
	public synchronized void removeNetServerListener(
			NetServerListener netserverlistener) {
		listeners.remove(netserverlistener);
	}

	/** 增加已读数据 */
	public void addReadBytes(int i) {
		totalBytesRead += i;
	}

	/** 增加已发数据 */
	public void addSendedBytes(int i) {
		totalBytesSended += i;
	}

	/** 增加一个观察者 */
	public synchronized void addNetServerListener(
			NetServerListener netserverlistener) {
		listeners.add(netserverlistener);
	}

	public synchronized NetServerListener[] getNetServerListener() {
		NetServerListener[] arrs = new NetServerListener[listeners.size()];
		listeners.toArray(arrs);
		return arrs;
	}

	/** 设置最大连接数 */
	public void setMaxConnections(int i) {
		maxConnections = i;
	}

	/** 得到最大连接数 */
	public int getMaxConnections() {
		return maxConnections;
	}

	/** 初始化端口 */
	public void init(int i) {
		port = i;
	}

	/** 得到总共收到的字节数 */
	public long getTotalBytesRead() {
		return totalBytesRead;
	}

	/** 得到总共发出的字节数 */
	public long getTotalBytesSended() {
		return totalBytesSended;
	}

	/** 当有连接进入时，设置解码器，通知对应的观察者 */
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

	/** 服务启动 */
	protected void serverOpened() {
		int i = listeners.size();
		for (int j = 0; j < i; j++) {
			NetServerListener netserverlistener = listeners.get(j);
			netserverlistener.serverOpened();
		}
	}

	/** 服务关闭 */
	protected void serverClosed() {
		int i = listeners.size();
		for (int j = 0; j < i; j++) {
			NetServerListener netserverlistener = listeners.get(j);
			netserverlistener.serverClosed();
		}
	}

	/** 当有连接关闭时，通知对应的观察者 */
	protected void connectionClosed(NetConnection netconnection) {
		int i = listeners.size();
		for (int j = 0; j < i; j++) {
			NetServerListener netserverlistener = listeners.get(j);
			netserverlistener.connectionClosed(netconnection);
		}

	}

	/** 得到服务器端口 */
	public int getPort() {
		return port;
	}

	/** 获得连接服务器的地址 */
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

	/** 设置服务器端口 */
	public void setPort(int i) {
		port = i;
	}

	/** 得到服务器启动绑定ip */
	public String getLocalHost() {
		return localHost;
	}

	/** 设置服务器启动绑定ip */
	public void setLocalHost(String localHost) {
		this.localHost = localHost;
	}

}
