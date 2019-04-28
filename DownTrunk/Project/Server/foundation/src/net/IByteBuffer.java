package net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

public interface IByteBuffer extends Serializable {

	public abstract void position(int i);

	public abstract void readFrom(InputStream inputstream) throws IOException;

	public abstract void skipBytes(int i);

	public abstract void readFrom(InputStream inputstream, int i)
			throws IOException;

	public abstract int capacity();

	public abstract void writeTo(OutputStream outputstream) throws IOException;

	public abstract void pack();

	public abstract void writeByte(int i);

	public abstract int readByte();

	public abstract int readUnsignedByte();

	public abstract void read(byte abyte0[], int i, int j, int k);

	public abstract int getReadPos();

	public abstract void setReadPos(int i);

	public abstract void write(byte abyte0[], int i, int j, int k);

	public abstract void writeChar(char c);

	public abstract char readChar();

	public abstract byte[] getBytes();

	public abstract Object clone();

	public abstract void writeAnsiString(String s);

	public abstract String readAnsiString();

	public abstract int length();

	public abstract void writeBoolean(boolean flag);

	public abstract boolean readBoolean();

	public abstract float readFloat();

	public abstract void reset();

	public abstract void writeLong(long l);
	
	public abstract void writeDouble(double d);
	
	public abstract double readDouble();

	public abstract void writeShortAnsiString(String s);

	public abstract long readLong();

	public abstract void writeShort(int i);

	public abstract int readShort();

	public abstract void writeByteBuffer(IByteBuffer bytebuffer);

	public abstract void writeByteBuffer(IByteBuffer bytebuffer, int i);

	public abstract void writeBytes(byte abyte0[]);

	public abstract void writeBytes(byte abyte0[], int i, int j);

	public abstract byte[] readBytes(int i);

	public abstract int readUnsignedShort();

	public abstract String readShortAnsiString();

	public abstract int available();

	public abstract String toString();

	public abstract int getWritePos();

	public abstract void setWritePos(int i);

	public abstract byte[] getRawBytes();

	public abstract void writeUTF(String s);

	public abstract String readUTF();

	public abstract void clear();

	public abstract void writeInt(int i);

	public abstract int readInt();

	public abstract int position();
	
	public abstract void setData(byte[] data);

	/** 读出一个指定长度的字节数组 */
	public byte[] readData();

	/** 写入一个字节数组，可以为null */
	public void writeData(byte[] data);
}