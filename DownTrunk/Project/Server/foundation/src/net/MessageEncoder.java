/**
 * ��������ʱ����
 */
package net;

public interface MessageEncoder {
	/**
	 * �������ݣ�Ȼ�󷢳�
	 * 
	 * @param message
	 *            ��������Ϣ
	 * @param bytebuffer
	 *            ������Ϣ�����ջ
	 */
	public abstract void encode(IByteBuffer message, IByteBuffer bytebuffer);
}
