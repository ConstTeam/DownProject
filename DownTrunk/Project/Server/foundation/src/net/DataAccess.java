package net;

import java.io.IOException;

public interface DataAccess {

	/** �����ɹ��ķ����볣�� */
	public static final int OK = 200;

	/** ����ָ���ĵ�ַ,ָ��ģ������� */
	IByteBuffer access(ServerAddress url, int cmd, IByteBuffer data)
			throws DataAccessException;
	
	IByteBuffer access(ISession session, IByteBuffer data)
			throws DataAccessException;
	/** ͨ���Ѿ���õ����ӣ�����ָ��ģ�飬������� */
	IByteBuffer access(ISession session, int cmd, IByteBuffer data)
			throws DataAccessException;

	/** �첽����ָ���ĵ�ַ,ָ��ģ������ݣ�responseΪ��ʱ���ͷ�������Ϣ */
	void access(ServerAddress url, int cmd, IByteBuffer data, Response response)
			throws DataAccessException;

	/** ͨ���Ѿ���õ����ӣ��첽����ָ��ģ�飬������ݣ�responseΪ��ʱ���ͷ�������Ϣ */
	void access(ISession session, int cmd, IByteBuffer data, Response response)
			throws DataAccessException;

	/**
	 * ����һ���µ�����
	 * 
	 * @param url
	 * @return
	 */
	public ISession createConnect(ServerAddress url);

	/** �õ�һ��Զ������ */
	public ISession getConnect(ServerAddress address) throws IOException,
			DataAccessException;

}
