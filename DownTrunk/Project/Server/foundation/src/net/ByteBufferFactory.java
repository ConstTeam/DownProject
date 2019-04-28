package net;

public class ByteBufferFactory {
	public static IByteBuffer getNewByteBuffer() {
		return new ByteBuffer();
	}

	public static IByteBuffer getNewByteBuffer(int size) {
		return new ByteBuffer(size);
	}

	public static IByteBuffer getNewByteBuffer(byte[] data) {
		return new ByteBuffer(data);
	}

	public static IByteBuffer getBigEndianByteBuffer() {
		return new ByteBuffer(true);
	}
}