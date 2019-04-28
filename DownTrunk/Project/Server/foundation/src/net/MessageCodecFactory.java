/**数据过滤工厂类*/
package net;

public interface MessageCodecFactory {

	public abstract MessageEncoder createEncoder();

	public abstract MessageDecoder createDecoder();
}
