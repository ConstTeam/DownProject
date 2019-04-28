package net;

/**
 * ������ҵ��ģ��ķ�����
 * 
 * @author water
 * 
 */
public interface IService {

	/** �������ҵ���߼� */
	public void doMessage(ISession session, IByteBuffer data);

	/** �õ������� */
	public IContext getContext();

	/** ��ʼ�� */
	public void init(IContext context);

	/** ���� */
	public void destroy();
}
