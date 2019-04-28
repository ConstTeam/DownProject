package net;


public interface IContext {
	/** 属性的数量 */
	public int attributeSize();

	/** 获得全部的属性名称 */
	public String[] getAttributeNames();

	/** 返回指定属性上的对象 */
	public Object getAttribute(String name);

	/** 设置指定属性上的对象 */
	public void setAttribute(String name, Object object);

	/** 移除指定属性上的对象 */
	public Object removeAttribute(String name);

	/** 得到一个远程连接 */
	public ISession getConnection(ServerAddress address);

	/** 清除全部的属性 */
	public void clear();

	/** 得到数据访问对象 */
	public DataAccess getDataAccess();
}
