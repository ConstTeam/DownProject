package net;

public interface IServer {
	/** ��ӹ����� */
	public void setFilter(IFilter filter);

	/** ��Ӽ����� */
	public void setHandler(INetHandler hander);

	/** �󶨷������˿� */
	public void bind(String host, int port);

}
