package codec;

import net.MessageCodecFactory;
import net.MessageDecoder;
import net.MessageEncoder;

public class EratingMessageCodecFactory implements MessageCodecFactory {
	public MessageEncoder createEncoder() {
		return new EratingMessageEncoder();
	}

	public EratingMessageCodecFactory() {
	}

	public MessageDecoder createDecoder() {
		return new EratingMessageDecoder();
	}

}
