package net;

public class PingMessage {
	/** ping消息 */
	protected static IByteBuffer PING_MESSAGE;

	/** ping消息的id */
	public static byte PING = 1;

	// 初始化
	static {
		PING_MESSAGE = new ByteBuffer();
		PING_MESSAGE.writeInt(4);
		PING_MESSAGE.writeByte(PING);
		PING_MESSAGE.pack();
	}

}
