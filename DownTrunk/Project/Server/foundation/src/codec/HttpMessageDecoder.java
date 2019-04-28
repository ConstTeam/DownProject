package codec;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.ByteBuffer;
import net.IByteBuffer;
import net.MessageDecoder;

public class HttpMessageDecoder implements MessageDecoder {
	private static Charset requestCharset = Charset.forName("GBK");

	/*
	 * �趨���ڽ���HTTP������ַ���ƥ��ģʽ������������ʽ��HTTP����
	 * 
	 * GET /dir/file HTTP/1.1 Host: hostname
	 * 
	 * ����������:
	 * 
	 * group[1] = "GET" group[2] = "/dir/file" group[3] = "1.1" group[4] =
	 * "hostname"
	 */
	private static Pattern requestPattern = Pattern.compile(
			"\\A([A-Z]+) +([^ ]+) +HTTP/([0-9\\.]+)$"
					+ ".*^Host: ([^ ]+)$.*\r\n\r\n\\z", Pattern.MULTILINE
					| Pattern.DOTALL);

	public HttpMessageDecoder() {
	}

	public IByteBuffer decode(IByteBuffer bytebuffer) {
		int len = bytebuffer.available();
		int read = bytebuffer.getReadPos();
		java.nio.ByteBuffer data = java.nio.ByteBuffer.wrap(
				bytebuffer.getRawBytes(), bytebuffer.getReadPos(), len);
		if (isComplete(data)) {
			// ByteBuffer message = new ByteBuffer(len);
			// message.writeByteBuffer(bytebuffer, len);
			bytebuffer.setReadPos(read + len);
			return parse(data);
		} else {
			return null;
		}
	}

	/* ����HTTP���󣬴�����Ӧ��Request���� */
	public ByteBuffer parse(java.nio.ByteBuffer bb)

	{
		CharBuffer cb = requestCharset.decode(bb); // ����
		String str = cb.toString();
		Matcher m = requestPattern.matcher(str); // �����ַ���ƥ��
		// ���HTTP������ָ�����ַ���ģʽ��ƥ�䣬˵���������ݲ���ȷ
		if (!m.matches()) {
			return null;
		}
		if (str == null || str.length() < 1)
			return null;
		if (!str.startsWith("GET /"))
			return null;
		// if (!str.endsWith("HTTP/1.1"))
		// return null;
		str = m.group(2).substring(1);

		String name = str;
		String cmd = null;
		int i = str.indexOf("?");
		if (i >= 0) {
			name = str.substring(0, i);
			cmd = str.substring(i + 1, str.length());
		}

		if (name.length() < 1 || name.length() > 254)
			return null;
		ByteBuffer data = new ByteBuffer();
		data.writeByte(Integer.parseInt(name));
		// data.writeByte(Integer.parseInt(cmd));
		data.writeUTF(cmd);
		data.setReadPos(0);
		return data;
	}

	// /*
	// * ɾ���������ģ������ӽ�֧��GET��HEAD����ʽ������HTTP�����е����Ĳ���
	// */
	// private static java.nio.ByteBuffer deleteContent(java.nio.ByteBuffer bb)
	// {
	// java.nio.ByteBuffer temp = bb.asReadOnlyBuffer();
	// String data = requestCharset.decode(temp).toString();
	// if (data.indexOf("\r\n\r\n") != -1)
	// {
	// data = data.substring(0, data.indexOf("\r\n\r\n") + 4);
	// return requestCharset.encode(data);
	// }
	// return bb;
	// }

	/*
	 * �ж�ByteBuffer�Ƿ������HTTP������������ݡ� HTTP�����ԡ�\r\n\r\n����β��
	 */
	public static boolean isComplete(java.nio.ByteBuffer bb) {
		java.nio.ByteBuffer temp = bb.asReadOnlyBuffer();
		String data = requestCharset.decode(temp).toString();
		if (data.indexOf("\r\n\r\n") != -1) {
			return true;
		}
		return false;
	}

}