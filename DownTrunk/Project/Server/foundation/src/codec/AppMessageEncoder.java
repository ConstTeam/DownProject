package codec;

import net.IByteBuffer;
import net.MessageEncoder;

public class AppMessageEncoder implements MessageEncoder {

	public AppMessageEncoder() {
	}

	public void encode(IByteBuffer message, IByteBuffer bytebuffer) {
		int j = message != null ? message.available() : 0;
		bytebuffer.writeInt(j);
		if (message != null && j > 0) {
			int k = message.getReadPos();
			bytebuffer.writeByteBuffer(message, j);
			message.setReadPos(k);
		}
	}
}
