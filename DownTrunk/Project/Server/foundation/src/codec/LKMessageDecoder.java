package codec;

import net.ByteBuffer;
import net.IByteBuffer;
import net.MessageDecoder;

public class LKMessageDecoder implements MessageDecoder {
	public LKMessageDecoder() {
	}

	public IByteBuffer decode(IByteBuffer bytebuffer) {

		if (bytebuffer.available() < 2)
			return null;
		int i = bytebuffer.position();
		int len = (((bytebuffer.readByte() & 0xff) << 8) | (bytebuffer
				.readByte() & 0xff)) - 2;
		if (bytebuffer.available() < len) {
			bytebuffer.setReadPos(i);
			return null;
		} else {
			ByteBuffer message = new ByteBuffer(len);
			message.setHighEndian(true);
			message.writeByteBuffer(bytebuffer, len);
			// �汾��
			message.readByte();
			// ������
			message.readByte();
			// ��λ��
			int readP = message.getReadPos();
			// дλ��
			int writeP = message.getWritePos();
			// Э��
			int cmd = message.readInt();

			// ��Ϣid
			int sequeue = message.readInt();

			message.setWritePos(2);
			//
			message.writeInt(sequeue);
			message.writeInt(cmd);
			//
			message.setReadPos(readP);
			message.setWritePos(writeP);
			//
			return message;
		}
	}
}