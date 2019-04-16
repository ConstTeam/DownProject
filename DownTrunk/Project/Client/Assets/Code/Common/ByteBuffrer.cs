using UnityEngine;
using System;
using System.Text;

namespace MS
{
	public class ByteBuffer
	{
		public int readPos;
		public int writePos;
		public byte[] data;
		private bool highEndian = true;
		public const int MAX_DATA_LENGTH = 2457600;

		public ByteBuffer(bool highEndian)
		{
			this.highEndian = highEndian;
		}
		
		public ByteBuffer():this(1)
		{
		}

		public ByteBuffer(int i)
		{
			if (i > 2457600)
			{
				Debug.LogError(string.Format("data overflow {0}", i));
			}
			this.data = new byte[i];
		}

		public ByteBuffer(byte[] abyte0)
		{
			this.data = abyte0;
			this.readPos = 0;
			this.writePos = abyte0.Length;
		}

		public ByteBuffer(byte[] abyte0, int i, int j)
		{
			this.data = abyte0;
			this.readPos = i;
			this.writePos = (i + j);
		}

		public void position(int i)
		{
			this.readPos = this.writePos = i;
		}

		public int capacity()
		{
			return this.data.Length;
		}

		private void ensureCapacity(int i)
		{
			if (i > this.data.Length)
			{
				byte[] abyte0 = new byte[i * 3 / 2];
				Array.Copy(this.data, 0, abyte0, 0, this.writePos);
				this.data = abyte0;
			}
		}

		public void pack()
		{
			if (this.readPos == 0)
				return;
			int i = available();
			for (int j = 0; j < i; j++)
			{
				this.data[j] = this.data[(this.readPos++)];
			}
			this.readPos = 0;
			this.writePos = i;
		}

		public void writeByte(int i)
		{
			writeNumber(i, 1);
		}

		public int readByte()
		{
			return this.data[(this.readPos++)];
		}

		public int peekByte()
		{
			return this.data [(this.readPos)];
		}

		public int readUnsignedByte()
		{
			return this.data[(this.readPos++)] & 0xFF;
		}

		public void read(byte[] abyte0, int i, int j, int k)
		{
			Array.Copy(this.data, k, abyte0, i, j);
		}

		public int getReadPos()
		{
			return this.readPos;
		}

		public void setReadPos(int i)
		{
			this.readPos = i;
		}

		public void write(byte[] abyte0, int i, int j, int k)
		{
			ensureCapacity(k + j);
			Array.Copy(abyte0, i, this.data, k, j);
		}

		public void writeChar(char c)
		{
			writeNumber(c, 2);
		}

		public char readChar() {
			return (char) (int) (readNumber(2) & 0xFFFF);
		}

		private void writeNumber(long l, int i)
		{
			if (this.highEndian)
				writeNumberHigh(l, i);
			else
				writeNumberLow(l, i);
		}

		private void writeNumberLow(long l, int i)
		{
			ensureCapacity(this.writePos + i);
			for (int j = 0; j < i; j++)
			{
				this.data[(this.writePos++)] = (byte) (int) l;
				l >>= 8;
			}
		}

		private void writeNumberHigh(long l, int i)
		{
			ensureCapacity(this.writePos + i);
			for (int j = i - 1; j >= 0; j--)
			{
				this.data[(this.writePos++)] = (byte) (int) (l >> (j << 3));
			}
		}

		private long readNumberHigh(int i)
		{
			long l = 0L;
			for (int j = i - 1; j >= 0; j--)
			{
				l |= (long)(this.data[(this.readPos++)] & 0xFF) << (j << 3);
			}
			return l;
		}

		private long readNumberLow(int i)
		{
			long l = 0L;
			for (int j = 0; j < i; j++)
			{
				l |= (long)(this.data[(this.readPos++)] & 0xFF) << (j << 3);
			}
			return l;
		}

		private long readNumber(int i)
		{
			if (this.highEndian)
				return readNumberHigh(i);
			
			return readNumberLow(i);
		}

		public byte[] getBytes()
		{
			byte[] abyte0 = new byte[length()];
			Array.Copy(this.data, 0, abyte0, 0, abyte0.Length);
			return abyte0;
		}

		public void writeAnsiString(string s)
		{
			if ((s == null) || (s.Length == 0))
			{
				writeShort(0);
			}
			else
			{
				if (s.Length > 32767)
				{
					Debug.LogError("string over flow");
				}
			
				byte[] abyte0 = Encoding.UTF8.GetBytes(s);
				writeShort(abyte0.Length);
				writeBytes(abyte0);
			}
		}

		public string readAnsiString()
		{
			int i = readUnsignedShort();
			if (i == 0)
				return "";
			
			byte[] abyte0 = readBytes(i);
			return System.Text.Encoding.Default.GetString(abyte0);
		}

		public int length()
		{
			return this.writePos;
		}

		public void writeBoolean(bool flag)
		{
			writeByte(flag ? 1 : 0);
		}

		public bool readBoolean()
		{
			return readByte() != 0;
		}

		public void reset()
		{
			this.readPos = 0;
		}

		public void writeLong(long l)
		{
			writeNumber(l, 8);
		}

		public void writeShortAnsiString(string s)
		{
			if ((s == null) || (s.Length == 0))
			{
				writeByte(0);
			}
			else
			{
				byte[] abyte0 = Encoding.UTF8.GetBytes(s);
				if (abyte0.Length > 255)
				{
					Debug.LogError("short string over flow");
				}
				writeByte(abyte0.Length);
				writeBytes(abyte0);
			}
		}

		public long readLong()
		{
			return readNumber(8);
		}

		public void writeShort(int i)
		{
			writeNumber(i, 2);
		}

		public int readShort()
		{
			return (short) (int) (readNumber(2) & 0xFFFF);
		}

		public void writeBytes(byte[] abyte0)
		{
			writeBytes(abyte0, 0, abyte0.Length);
		}

		public byte[] readData()
		{
			int len = readInt();
			if (len < 0)
				return null;
			if (len > 2457600)
			{
				Debug.LogError(string.Format("readData, data overflow:{0}", len));
			}
			return readBytes(len);
		}

		public void writeData(byte[] data)
		{
			writeData(data, 0, data != null ? data.Length : 0);
		}

		public void writeData(byte[] data, int pos, int len)
		{
			if (data == null)
			{
				writeInt(0);
				return;
			}
			writeInt(len);
			writeBytes(data);
		}

		public void writeBytes(byte[] abyte0, int i, int j)
		{
			ensureCapacity(this.writePos + j);
			for (int k = 0; k < j; k++)
			{
				this.data[(this.writePos++)] = abyte0[(i++)];
			}
		}

		public byte[] readBytes(int i)
		{
			byte[] abyte0 = new byte[i];
			for (int j = 0; j < i; j++)
			{
				abyte0[j] = this.data[(this.readPos++)];
			}
			return abyte0;
		}

		public int readUnsignedShort()
		{
			return (int) (readNumber(2) & 0xFFFF);
		}

		public string readShortAnsiString()
		{
			int i = readUnsignedByte();
			if (i == 0)
				return "";
			
			byte[] abyte0 = readBytes(i);
			return System.Text.Encoding.Default.GetString(abyte0);
		}

		public int available()
		{
			return this.writePos - this.readPos;
		}

		public int getWritePos()
		{
			return this.writePos;
		}

		public void setWritePos(int i)
		{
			this.writePos = i;
		}

		public byte[] getRawBytes()
		{
			return this.data;
		}

		public void writeUTF(string s)
		{
			if (s == null)
				s = "";
			int i = s.Length;
			int j = 0;
			for (int k = 0; k < i; k++)
			{
				char c = s[k];
				if (c < 0x007F)
					j++;
				else if (c > 0x07FF)
					j += 3;
				else 
					j += 2;
			}
			if (j > 65535)
			{
				Debug.LogError(string.Format("the string is too long:{0}", i));
			}
			ensureCapacity(this.writePos + j + 2);
			writeShort(j);
			for (int l = 0; l < i; l++)
			{
				char c1 = s[l];
				if (c1 < 0x007F)
				{
					this.data[(this.writePos++)] = (byte) c1;
				}
				else if (c1 > 0x007F)
				{
					this.data[(this.writePos++)] = (byte) (0xE0 | c1 >> '\f' & 0xF);
					this.data[(this.writePos++)] = (byte) (0x80 | c1 >> 6 & 0x3F);
					this.data[(this.writePos++)] = (byte) (0x80 | c1 & 0x3F);
				}
				else
				{
					this.data[(this.writePos++)] = (byte) (0xC0 | c1 >> 6 & 0x1F);
					this.data[(this.writePos++)] = (byte) (0x80 | c1 & 0x3F);
				}
			}
		}

		public string readUTF()
		{
			int i = readUnsignedShort();
			if (i == 0)
				return "";
			
			char[] ac = new char[i];
			int j = 0;
			
			for (int l = this.readPos + i; this.readPos < l;)
			{
				int k = this.data[(this.readPos++)] & 0xFF;
				if (k < 127)
				{
					ac[(j++)] = (char) k;
				}
				else if (k >> 5 == 7)
				{
					byte byte0 = this.data[(this.readPos++)];
					byte byte2 = this.data[(this.readPos++)];
					ac[(j++)] = (char) ((k & 0xF) << 12 | (byte0 & 0x3F) << 6 | byte2 & 0x3F);
				}
				else
				{
					byte byte1 = this.data[(this.readPos++)];
					ac[(j++)] = (char) ((k & 0x1F) << 6 | byte1 & 0x3F);
				}
			}

			return new string(ac, 0, j);
		}

		public void clear()
		{
			this.writePos = (this.readPos = 0);
		}

		public void writeInt(int i)
		{
			writeNumber(i, 4);
		}

		public int readInt()
		{
			return (int) (readNumber(4) & 0xFFFFFFFF);
		}

		public int position()
		{
			return this.readPos;
		}

		public bool isHighEndian()
		{
			return this.highEndian;
		}

		public void setHighEndian(bool highEndian)
		{
			this.highEndian = highEndian;
		}
	}
}
