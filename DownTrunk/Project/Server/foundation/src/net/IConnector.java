package net;

public interface IConnector {
	/** ����һ���µ����� */
	public ISession createConnect(ServerAddress address);

}
