package codec;

import net.MessageCodecFactory;
import net.MessageDecoder;
import net.MessageEncoder;

public class AppMessageCodecFactory implements MessageCodecFactory {

	public MessageEncoder createEncoder() {
		return new AppMessageEncoder();
	}

	public AppMessageCodecFactory() {
	}

	public MessageDecoder createDecoder() {
		return new AppMessageDecoder();
	}
}
