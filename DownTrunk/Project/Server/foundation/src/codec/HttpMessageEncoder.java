package codec;

import net.IByteBuffer;
import net.MessageEncoder;

public class HttpMessageEncoder implements MessageEncoder {

	public HttpMessageEncoder() {
	}

	public void encode(IByteBuffer message, IByteBuffer bytebuffer) {
		int j = message != null ? message.length() : 0;
		if (message != null) {
			int k = message.getReadPos();
			bytebuffer.writeByteBuffer(message, j);
			message.setReadPos(k);
		}
	}
}
