package net;


public interface IContext {
	/** ���Ե����� */
	public int attributeSize();

	/** ���ȫ������������ */
	public String[] getAttributeNames();

	/** ����ָ�������ϵĶ��� */
	public Object getAttribute(String name);

	/** ����ָ�������ϵĶ��� */
	public void setAttribute(String name, Object object);

	/** �Ƴ�ָ�������ϵĶ��� */
	public Object removeAttribute(String name);

	/** �õ�һ��Զ������ */
	public ISession getConnection(ServerAddress address);

	/** ���ȫ�������� */
	public void clear();

	/** �õ����ݷ��ʶ��� */
	public DataAccess getDataAccess();
}
