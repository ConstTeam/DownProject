package net;

public interface NetServerListener {

	/** 有连接进入时 */
	public abstract void connectionOpened(NetConnection netconnection);

	/** 有连接关闭时 */
	public abstract void connectionClosed(NetConnection netconnection);

	/** 服务启动时 */
	public abstract void serverOpened();

	/** 服务关闭时 */
	public abstract void serverClosed();
}
