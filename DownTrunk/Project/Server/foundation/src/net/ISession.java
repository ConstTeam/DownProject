package net;

import java.net.InetAddress;

public interface ISession {
	/** �������� */
	public void send(IByteBuffer data);

	/** �ر����� */
	public void close();

	/** �Ƿ��Ѿ��ر� */
	public boolean isClosed();

	/** �������� */
	public void attach(Object ob);

	/** ��ø������� */
	public Object attachment();

	/** �õ����ӵĵ�ַ */
	public InetAddress getAddress();

	/** �õ��˿� */
	public int getPort();

}
