package codec;

import net.ByteBuffer;
import net.IByteBuffer;
import net.MessageDecoder;

public class AppMessageDecoder implements MessageDecoder {

	protected ByteBuffer xmlreq = new ByteBuffer(0);

	public AppMessageDecoder() {
	}

	public IByteBuffer decode(IByteBuffer bytebuffer) {

		if (bytebuffer.available() < 4)
			return null;
		int i = bytebuffer.position();
		int len = bytebuffer.readInt();
		if (len == 1014001516) {
			return xmlreq;
		}
		if (bytebuffer.available() < len) {
			bytebuffer.setReadPos(i);
			return null;
		} else {
			ByteBuffer message = new ByteBuffer(len);
			message.writeByteBuffer(bytebuffer, len);
			return message;
		}
	}

}
