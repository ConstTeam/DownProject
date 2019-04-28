package codec;

import net.MessageCodecFactory;
import net.MessageDecoder;
import net.MessageEncoder;

public class HttpMessageCodecFactory implements MessageCodecFactory {

	public MessageEncoder createEncoder() {
		return new HttpMessageEncoder();
	}

	public HttpMessageCodecFactory() {
	}

	public MessageDecoder createDecoder() {
		return new HttpMessageDecoder();
	}
}
