package net;

import java.net.InetAddress;

public interface ISession {
	/** 发出数据 */
	public void send(IByteBuffer data);

	/** 关闭连接 */
	public void close();

	/** 是否已经关闭 */
	public boolean isClosed();

	/** 附加数据 */
	public void attach(Object ob);

	/** 获得附加数据 */
	public Object attachment();

	/** 得到连接的地址 */
	public InetAddress getAddress();

	/** 得到端口 */
	public int getPort();

}
