package net;

public interface IServer {
	/** 添加过滤器 */
	public void setFilter(IFilter filter);

	/** 添加监听器 */
	public void setHandler(INetHandler hander);

	/** 绑定服务器端口 */
	public void bind(String host, int port);

}
