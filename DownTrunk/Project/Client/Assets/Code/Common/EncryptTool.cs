using UnityEngine;
using System.Collections;

namespace MS
{
	public class EncryptTool
	{
		public static void Encrypt(ref byte[] data, int encryptLen, int beginPos = 0)
		{
			int dataLen = data.Length;
			if(dataLen < beginPos + 2)
				return;
			
			int encodeLen = dataLen > encryptLen*2 ? encryptLen : (dataLen-beginPos)/2;
			
			int f = beginPos;
			int b = dataLen;
			for(int i = 0; i < 2; i++)
			{
				for(int j = 0; j < encodeLen; j++)
				{
					data[f++] ^= data[--b];
				}
				f -= encodeLen;
				
				for(int j = 0; j < encodeLen; j++)
				{
					data[b++] ^= data[f++];
				}
				f -= encodeLen;
			}
		}
		
		public static void Decrypt(ref byte[] data, int encryptLen, int beginPos = 0)
		{
			int dataLen = data.Length;
			if(dataLen < 2)
				return;
			
			int encodeLen = dataLen > encryptLen*2 ? encryptLen : dataLen/2;
			
			int f = 0;
			int b = dataLen - encodeLen;
			
			for(int i = 0; i < 2; i++)
			{
				for(int j = 0; j < encodeLen; j++)
				{
					data[b++] ^= data[f++];
				}
				f -= encodeLen;
				
				for(int j = 0; j < encodeLen; j++)
				{
					data[f++] ^= data[--b];
				}
				f -= encodeLen;
			}
		}
	}
}
