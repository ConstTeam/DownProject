package net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ByteBuffer implements Cloneable, IByteBuffer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int readPos;
	private int writePos;
	private byte[] data;
	private boolean highEndian = true;
	public static final int MAX_DATA_LENGTH = 2457600;
	/** 读写数据的缓冲区大小 */
	public static final int RW_BUFFER_SIZE = 4 * 1024;

	public ByteBuffer(boolean highEndian) {
		this();
		this.highEndian = highEndian;
	}

	public void position(int i) {
		this.readPos = (this.writePos = i);
	}

	public void readFrom(InputStream inputstream) throws IOException {
		readFrom(inputstream, capacity() - length());
	}

	public void skipBytes(int i) {
		this.readPos += i;
	}

	public void readFrom(InputStream inputstream, int i) throws IOException {
		ensureCapacity(this.writePos + i);
		for (int j = 0; j < i; j += inputstream.read(this.data, this.writePos
				+ j, i - j))
			;
		this.writePos += i;
	}

	public int capacity() {
		return this.data.length;
	}

	private void ensureCapacity(int i) {
		if (i > this.data.length) {
			byte[] abyte0 = new byte[i * 3 / 2];
			System.arraycopy(this.data, 0, abyte0, 0, this.writePos);
			this.data = abyte0;
		}
	}

	public void writeTo(OutputStream outputstream) throws IOException {
		int i = available();
		for (int j = 0; j < i; j++)
			outputstream.write(this.data[(this.readPos++)]);
	}

	public void pack() {
		if (this.readPos == 0)
			return;
		int i = available();
		for (int j = 0; j < i; j++) {
			this.data[j] = this.data[(this.readPos++)];
		}
		this.readPos = 0;
		this.writePos = i;
	}

	public void writeByte(int i) {
		writeNumber(i, 1);
	}

	public int readByte() {
		return this.data[(this.readPos++)];
	}

	public int readUnsignedByte() {
		return this.data[(this.readPos++)] & 0xFF;
	}

	public void read(byte[] abyte0, int i, int j, int k) {
		System.arraycopy(this.data, k, abyte0, i, j);
	}

	public int getReadPos() {
		return this.readPos;
	}

	public void setReadPos(int i) {
		this.readPos = i;
	}

	public void write(byte[] abyte0, int i, int j, int k) {
		ensureCapacity(k + j);
		System.arraycopy(abyte0, i, this.data, k, j);
	}

	public void writeChar(char c) {
		writeNumber(c, 2);
	}

	public char readChar() {
		return (char) (int) (readNumber(2) & 0xFFFF);
	}

	private void writeNumber(long l, int i) {
		if (this.highEndian)
			writeNumberHigh(l, i);
		else
			writeNumberLow(l, i);
	}

	private void writeNumberLow(long l, int i) {
		ensureCapacity(this.writePos + i);
		for (int j = 0; j < i; j++) {
			this.data[(this.writePos++)] = (byte) (int) l;
			l >>= 8;
		}
	}

	private void writeNumberHigh(long l, int i) {
		ensureCapacity(this.writePos + i);
		for (int j = i - 1; j >= 0; j--)
			this.data[(this.writePos++)] = (byte) (int) (l >>> (j << 3));
	}

	private long readNumberHigh(int i) {
		long l = 0L;
		for (int j = i - 1; j >= 0; j--) {
			l |= (this.data[(this.readPos++)] & 0xFF) << (j << 3);
		}
		return l;
	}

	private long readNumberLow(int i) {
		long l = 0L;
		for (int j = 0; j < i; j++) {
			l |= (this.data[(this.readPos++)] & 0xFF) << (j << 3);
		}
		return l;
	}

	private long readNumber(int i) {
		if (this.highEndian) {
			return readNumberHigh(i);
		}
		return readNumberLow(i);
	}

	public byte[] getBytes() {
		byte[] abyte0 = new byte[length()];
		System.arraycopy(this.data, 0, abyte0, 0, abyte0.length);
		return abyte0;
	}

	public Object clone() {
		ByteBuffer bytebuffer = new ByteBuffer(this.writePos);
		System.arraycopy(this.data, 0, bytebuffer.data, 0, this.writePos);
		bytebuffer.writePos = this.writePos;
		bytebuffer.readPos = this.readPos;
		return bytebuffer;
	}

	public void writeAnsiString(String s) {
		if ((s == null) || (s.length() == 0)) {
			writeShort(0);
		} else {
			if (s.length() > 32767)
				throw new IllegalArgumentException("string over flow");
			byte[] abyte0 = s.getBytes();
			writeShort(abyte0.length);
			writeBytes(abyte0);
		}
	}

	public String readAnsiString() {
		int i = readUnsignedShort();
		if (i == 0) {
			return "";
		}
		byte[] abyte0 = readBytes(i);
		return new String(abyte0);
	}

	public int length() {
		return this.writePos;
	}

	public void writeBoolean(boolean flag) {
		writeByte(flag ? 1 : 0);
	}

	public boolean readBoolean() {
		return readByte() != 0;
	}

	public float readFloat() {
		int i = readInt();
		return Float.intBitsToFloat(i);
	}

	public void reset() {
		this.readPos = 0;
	}

	public void writeLong(long l) {
		writeNumber(l, 8);
	}

	public ByteBuffer() {
		this(RW_BUFFER_SIZE);
	}

	public ByteBuffer(int i) {
		if (i > 2457600) {
			throw new IllegalArgumentException("data overflow " + i);
		}
		this.data = new byte[i];
	}

	public ByteBuffer(byte[] abyte0) {
		this(abyte0, 0, abyte0.length);
	}

	public ByteBuffer(byte[] abyte0, int i, int j) {
		this.data = abyte0;
		this.readPos = i;
		this.writePos = (i + j);
	}

	public void writeShortAnsiString(String s) {
		if ((s == null) || (s.length() == 0)) {
			writeByte(0);
		} else {
			byte[] abyte0 = s.getBytes();
			if (abyte0.length > 255)
				throw new IllegalArgumentException("short string over flow");
			writeByte(abyte0.length);
			writeBytes(abyte0);
		}
	}

	public long readLong() {
		return readNumber(8);
	}

	public void writeShort(int i) {
		writeNumber(i, 2);
	}

	public int readShort() {
		return (short) (int) (readNumber(2) & 0xFFFF);
	}

	public void writeByteBuffer(IByteBuffer bytebuffer) {
		writeByteBuffer(bytebuffer, bytebuffer.available());
	}

	public void writeByteBuffer(IByteBuffer bytebuffer, int i) {
		ensureCapacity(length() + i);
		byte[] sourceData = bytebuffer.getRawBytes();
		int sourceReadPos = bytebuffer.getReadPos();

		System.arraycopy(sourceData, sourceReadPos, this.data, this.writePos, i);
		setWritePos(this.writePos + i);
		bytebuffer.setReadPos(sourceReadPos + i);
	}

	public void writeBytes(byte[] abyte0) {
		writeBytes(abyte0, 0, abyte0.length);
	}

	public byte[] readData() {
		int len = readInt();
		if (len < 0)
			return null;
		if (len > 2457600)
			throw new IllegalArgumentException(this
					+ " readData, data overflow:" + len);
		return readBytes(len);
	}

	public void writeData(byte[] data) {
		writeData(data, 0, data != null ? data.length : 0);
	}

	public void writeData(byte[] data, int pos, int len) {
		if (data == null) {
			writeInt(0);
			return;
		}
		writeInt(len);
		writeBytes(data);
	}

	public void writeBytes(byte[] abyte0, int i, int j) {
		ensureCapacity(this.writePos + j);
		for (int k = 0; k < j; k++)
			this.data[(this.writePos++)] = abyte0[(i++)];
	}

	public byte[] readBytes(int i) {
		byte[] abyte0 = new byte[i];
		for (int j = 0; j < i; j++) {
			abyte0[j] = this.data[(this.readPos++)];
		}
		return abyte0;
	}

	public int readUnsignedShort() {
		return (int) (readNumber(2) & 0xFFFF);
	}

	public String readShortAnsiString() {
		int i = readUnsignedByte();
		if (i == 0) {
			return "";
		}
		byte[] abyte0 = readBytes(i);
		return new String(abyte0);
	}

	public int available() {
		return this.writePos - this.readPos;
	}

	public String toString() {
		return new String(this.data, 0, this.writePos);
	}

	public int getWritePos() {
		return this.writePos;
	}

	public void setWritePos(int i) {
		this.writePos = i;
	}

	public byte[] getRawBytes() {
		return this.data;
	}

	public void writeUTF(String s) {
		if (s == null)
			s = "";
		int i = s.length();
		int j = 0;
		for (int k = 0; k < i; k++) {
			char c = s.charAt(k);
			if (c < 0x007F)
				j++;
			else if (c > 0x07FF)
				j += 3;
			else {
				j += 2;
			}
		}
		if (j > 65535)
			throw new IllegalArgumentException("the string is too long:" + i);
		ensureCapacity(this.writePos + j + 2);
		writeShort(j);
		for (int l = 0; l < i; l++) {
			char c1 = s.charAt(l);
			if (c1 < 0x007F) {
				this.data[(this.writePos++)] = (byte) c1;
			} else if (c1 > 0x007F) {
				this.data[(this.writePos++)] = (byte) (0xE0 | c1 >> '\f' & 0xF);
				this.data[(this.writePos++)] = (byte) (0x80 | c1 >> '\006' & 0x3F);
				this.data[(this.writePos++)] = (byte) (0x80 | c1 & 0x3F);
			} else {
				this.data[(this.writePos++)] = (byte) (0xC0 | c1 >> '\006' & 0x1F);
				this.data[(this.writePos++)] = (byte) (0x80 | c1 & 0x3F);
			}
		}
	}

	public String readUTF() {
		int i = readShort();
		if (i == 0)
			return "";
		char[] ac = new char[i];
		int j = 0;
		for (int l = this.readPos + i; this.readPos < l;) {
			int k = this.data[(this.readPos++)] & 0xFF;
			if (k < 127) {
				ac[(j++)] = (char) k;
			} else if (k >> 5 == 7) {
				byte byte0 = this.data[(this.readPos++)];
				byte byte2 = this.data[(this.readPos++)];
				ac[(j++)] = (char) ((k & 0xF) << 12 | (byte0 & 0x3F) << 6 | byte2 & 0x3F);
			} else {
				byte byte1 = this.data[(this.readPos++)];
				ac[(j++)] = (char) ((k & 0x1F) << 6 | byte1 & 0x3F);
			}
		}

		return new String(ac, 0, j);
	}

	public void clear() {
		this.writePos = (this.readPos = 0);
	}

	public void writeInt(int i) {
		writeNumber(i, 4);
	}

	public int readInt() {
		return (int) (readNumber(4) & 0xFFFFFFFF);
	}

	public int position() {
		return this.readPos;
	}

	public boolean isHighEndian() {
		return this.highEndian;
	}

	public void setHighEndian(boolean highEndian) {
		this.highEndian = highEndian;
	}
	
	public void writeDouble(double x) {
        ensureCapacity(this.writePos + 8);

		long dl = Double.doubleToRawLongBits(x);
		data[writePos++] = long7(dl);
		data[writePos++] = long6(dl);
		data[writePos++] = long5(dl);
		data[writePos++] = long4(dl);
		data[writePos++] = long3(dl);
		data[writePos++] = long2(dl);
		data[writePos++] = long1(dl);
		data[writePos++] = long0(dl);
	}
	
	public double readDouble() {
		long makeLong = makeLong(data[readPos++], data[readPos++], 
				data[readPos++], data[readPos++], data[readPos++], 
				data[readPos++], data[readPos++], data[readPos++]);
        return Double.longBitsToDouble(makeLong);
    }
	
	private static long makeLong(byte b7, byte b6, byte b5, byte b4,
            byte b3, byte b2, byte b1, byte b0) {
		return ((((long)b7       ) << 56) |
		(((long)b6 & 0xff) << 48) |
		(((long)b5 & 0xff) << 40) |
		(((long)b4 & 0xff) << 32) |
		(((long)b3 & 0xff) << 24) |
		(((long)b2 & 0xff) << 16) |
		(((long)b1 & 0xff) <<  8) |
		(((long)b0 & 0xff)      ));
	}

	public void setData(byte[] data) {
		this.data = data;
	}
	private static byte long7(long x) { return (byte)(x >> 56); }
    private static byte long6(long x) { return (byte)(x >> 48); }
    private static byte long5(long x) { return (byte)(x >> 40); }
    private static byte long4(long x) { return (byte)(x >> 32); }
    private static byte long3(long x) { return (byte)(x >> 24); }
    private static byte long2(long x) { return (byte)(x >> 16); }
    private static byte long1(long x) { return (byte)(x >>  8); }
    private static byte long0(long x) { return (byte)(x      ); }
}