package codec;

import net.ByteBuffer;
import net.IByteBuffer;
import net.MessageEncoder;

public class LKMessageEncoder implements MessageEncoder {
	protected byte VERSION = 0x10;

	public LKMessageEncoder() {
	}

	public void encode(IByteBuffer message, IByteBuffer bytebuffer) {
		int j = message != null ? message.length() : 0;
		ByteBuffer sendData = new ByteBuffer(j + 8);
		sendData.setHighEndian(true);
		sendData.writeShort(j + 8);
		sendData.writeByte(VERSION);
		sendData.writeByte(0);

		if (message != null) {
			int k = message.getReadPos();
			sendData.writeByteBuffer(message, j);
			message.setReadPos(k);
		}
		bytebuffer.writeBytes(sendData.getRawBytes(), 0, j + 8);
	}
}
