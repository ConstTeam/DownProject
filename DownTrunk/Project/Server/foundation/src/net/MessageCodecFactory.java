/**���ݹ��˹�����*/
package net;

public interface MessageCodecFactory {

	public abstract MessageEncoder createEncoder();

	public abstract MessageDecoder createDecoder();
}
