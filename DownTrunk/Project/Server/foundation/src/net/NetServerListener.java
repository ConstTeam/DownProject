package net;

public interface NetServerListener {

	/** �����ӽ���ʱ */
	public abstract void connectionOpened(NetConnection netconnection);

	/** �����ӹر�ʱ */
	public abstract void connectionClosed(NetConnection netconnection);

	/** ��������ʱ */
	public abstract void serverOpened();

	/** ����ر�ʱ */
	public abstract void serverClosed();
}
