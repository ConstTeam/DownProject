package codec;

import net.ByteBuffer;
import net.IByteBuffer;
import net.MessageDecoder;

public class EratingMessageDecoder implements MessageDecoder {
	public EratingMessageDecoder() {
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
			// 版本号
			message.readByte();

			// 读位置
			int readP = message.getReadPos();
			// 写位置
			int writeP = message.getWritePos();

			// 后续包
			message.readByte();

			message.setWritePos(1);

			message.writeByte(100);
			// // 协议
			// int cmd = message.readInt();

			// //
			// // // 消息id
			// int sequeue = message.readInt();
			// //
			// message.setWritePos(2 + 3);
			// // //
			// message.writeByte(cmd);
			// message.writeInt(sequeue);
			// //
			message.setReadPos(readP);
			message.setWritePos(writeP);
			//
			return message;
		}
	}
}