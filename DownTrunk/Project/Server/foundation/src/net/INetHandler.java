package net;

/** ���������˿� */
public interface INetHandler {
	/** ��ʼ����ʱ */
	public void sessionOpened(ISession session);

	/** ���ӹر�ʱ */
	public void sessionClosed(ISession session);

	/** ���յ���Ϣʱ */
	public void messageReceived(ISession session, IByteBuffer messgae);
}
