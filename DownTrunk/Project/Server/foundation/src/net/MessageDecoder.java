/**
 * ��������ʱ����
 */
package net;

public interface MessageDecoder {

	public abstract IByteBuffer decode(IByteBuffer bytebuffer);
}
