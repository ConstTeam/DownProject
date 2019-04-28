/**
 * 接受数据时解码
 */
package net;

public interface MessageDecoder {

	public abstract IByteBuffer decode(IByteBuffer bytebuffer);
}
