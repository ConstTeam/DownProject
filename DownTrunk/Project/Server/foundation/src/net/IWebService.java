package net;

import io.netty.channel.ChannelHandlerContext;

/**
 * ������ҵ��ģ��ķ�����
 * 
 * @author water
 * 
 */
public interface IWebService {

	/** �������ҵ���߼� */
	public void doMessage(ChannelHandlerContext channel, IByteBuffer data);

	/** �õ������� */
	public IContext getContext();

	/** ��ʼ�� */
	public void init(IContext context);

	/** ���� */
	public void destroy();
}
