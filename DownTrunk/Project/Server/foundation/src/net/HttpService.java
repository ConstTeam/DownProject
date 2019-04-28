package net;

import java.nio.CharBuffer;
import java.nio.charset.Charset;

public abstract class HttpService implements IService {
	private static String CRLF;
	private static Charset responseCharset;

	static {
		CRLF = "\r\n";

		responseCharset = Charset.forName("UTF8");
	}

	public void doMessage(ISession session, IByteBuffer data) {
		try {
			try {
				String result = access(session, data);
				if (result == null)
					throw new DataAccessException(500, "null data");
				CharBuffer sendChar = CharBuffer.allocate(result.length() * 4);
				sendChar.put(result);
				sendChar.flip();
				java.nio.ByteBuffer senddata = responseCharset.encode(sendChar);

				CharBuffer cb = CharBuffer.allocate(1024 + senddata.remaining());
				cb.put("HTTP/1.1 ").put(Code.OK.toString()).put(CRLF);
				cb.put("Server: linekong/1.1").put(CRLF);
				cb.put("Content-type: text/html").put(CRLF);
				cb.put("Content-length: ")
						.put(Long.toString(senddata.remaining()))
						.put(CRLF);
				cb.put(CRLF);
				
				IByteBuffer bb = ByteBufferFactory.getNewByteBuffer();
				bb.writeUTF("HTTP/2.0 ");
				bb.writeUTF(Code.OK.toString());
				bb.writeUTF(CRLF);
				bb.writeUTF("Content-type: text/html");
				bb.writeUTF(CRLF);
				bb.writeUTF("Content-length: ");
				bb.writeUTF(String.valueOf(senddata.remaining()));
				bb.writeUTF(CRLF);
				bb.writeUTF(CRLF);
				session.send(bb);
			} catch (DataAccessException e) {
				e.printStackTrace();

				CharBuffer sendChar = CharBuffer.allocate(1024);
				sendChar.put(String.valueOf(e.getType()));
				sendChar.flip();
				java.nio.ByteBuffer senddata = responseCharset.encode(sendChar);

				CharBuffer cb = CharBuffer.allocate(1024 + senddata.remaining());
				cb.put("HTTP/1.1 ").put(Code.OK.toString()).put(CRLF);
				cb.put("Server: linekong/1.1").put(CRLF);
				cb.put("Content-type: text/html").put(CRLF);
				cb.put("Content-length: ")
						.put(Long.toString(senddata.remaining()))
						.put(CRLF);
				cb.put(CRLF);
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out
					.println(getBackletInfo() + " service !" + e.getMessage());
			CharBuffer sendChar = CharBuffer.allocate(1024);
			sendChar.put("500");
			sendChar.flip();
			java.nio.ByteBuffer senddata = responseCharset.encode(sendChar);

			CharBuffer cb = CharBuffer.allocate(senddata.remaining());
			cb.put("HTTP/1.1 ").put(Code.OK.toString()).put(CRLF);
			cb.put("Server: linekong/1.1").put(CRLF);
			cb.put("Content-type: text/html").put(CRLF);
			cb.put("Content-length: ")
					.put(Long.toString(senddata.remaining())).put(CRLF);
			cb.put(CRLF);
		}
	}

	public String getBackletInfo() {
		return getClass().getName();
	}

	public void destroy() {
	}

	public abstract String access(ISession paramISession,
			IByteBuffer paramIByteBuffer) throws DataAccessException;

	static class Code {
		private int number;
		private String reason;
		static Code OK = new Code(200, "OK");

		static Code BAD_REQUEST = new Code(400, "Bad Request");

		static Code NOT_FOUND = new Code(404, "Not Found");

		static Code METHOD_NOT_ALLOWED = new Code(405, "Method Not Allowed");

		private Code(int i, String r) {
			this.number = i;
			this.reason = r;
		}

		public String toString() {
			return this.number + " " + this.reason;
		}
	}
}