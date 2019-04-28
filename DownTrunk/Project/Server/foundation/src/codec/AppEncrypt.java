package codec;

import net.IEncrypt;
import util.CodeKit;

public class AppEncrypt implements IEncrypt {
	public static final byte[] CODE = { 99, 104, 114, 100, 119 };

	@Override
	public void coding(byte[] data, int start, int lenght, String codeKey) {
		CodeKit.coding(data, start, lenght, codeKey);
	}

	@Override
	public void coding(byte[] data) {
		CodeKit.coding(data, CODE);
	}
}
