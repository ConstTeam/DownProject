package net;

public class PingMessage {
	/** ping��Ϣ */
	protected static IByteBuffer PING_MESSAGE;

	/** ping��Ϣ��id */
	public static byte PING = 1;

	// ��ʼ��
	static {
		PING_MESSAGE = new ByteBuffer();
		PING_MESSAGE.writeInt(4);
		PING_MESSAGE.writeByte(PING);
		PING_MESSAGE.pack();
	}

}
