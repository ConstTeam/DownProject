package codec;

import net.ByteBufferFactory;
import net.IByteBuffer;
import net.ISession;

public class CorrespondMessage {

	public static IByteBuffer getEncryptKeySendMessage(String key) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		data.writeByte(3);
		data.writeByte(1);
		data.writeUTF(key);
		return data;
	}

	public static void sendColseMessage(ISession session) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		data.writeByte(0);
		data.writeByte(5);
		session.send(data);
	}
}
