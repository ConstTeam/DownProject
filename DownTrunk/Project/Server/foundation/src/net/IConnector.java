package net;

public interface IConnector {
	/** 创建一个新的连接 */
	public ISession createConnect(ServerAddress address);

}
