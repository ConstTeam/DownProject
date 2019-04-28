/**
 * 发出数据时编码
 */
package net;

public interface MessageEncoder {
	/**
	 * 编码数据，然后发出
	 * 
	 * @param message
	 *            发出的消息
	 * @param bytebuffer
	 *            发出消息缓存堆栈
	 */
	public abstract void encode(IByteBuffer message, IByteBuffer bytebuffer);
}
