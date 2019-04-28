package net;

/** 网络层监听端口 */
public interface INetHandler {
	/** 开始连接时 */
	public void sessionOpened(ISession session);

	/** 连接关闭时 */
	public void sessionClosed(ISession session);

	/** 当收到消息时 */
	public void messageReceived(ISession session, IByteBuffer messgae);
}
