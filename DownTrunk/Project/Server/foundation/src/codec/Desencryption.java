package codec;


public class Desencryption {
	
	public static int ENCODE_LEN = 0;

	public static void encrypt(byte[] data)
	{
		int dataLen = data.length;
		if(dataLen < 2)
			return;
		
		int encodeLen = dataLen > ENCODE_LEN*2 ? ENCODE_LEN : dataLen/2;
		
		int f = 0;
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
	
    public static void decrypt(byte[] data, int beginPos)
	{
		
		int dataLen = data.length;
		if(dataLen < beginPos + 2)
			return;
		
		int encodeLen = dataLen > ENCODE_LEN*2 ? ENCODE_LEN : (dataLen-beginPos)/2;
		
		int f = beginPos;
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
