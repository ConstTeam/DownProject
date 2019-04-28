package codec;

import net.MessageCodecFactory;
import net.MessageDecoder;
import net.MessageEncoder;

public class LKMessageCodecFactory implements MessageCodecFactory {

	public MessageEncoder createEncoder() {
		return new LKMessageEncoder();
	}

	public LKMessageCodecFactory() {
	}

	public MessageDecoder createDecoder() {
		return new LKMessageDecoder();
	}
}
