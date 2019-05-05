using System.Collections;
using System.Collections.Generic;

namespace MS
{
	public class CommonCommand
	{
		static private ByteBuffer PackData(ArrayList paramList1, ArrayList paramList2, bool bDeviceId = true)
		{
			char[] typeArr = ((string)paramList1[0]).ToCharArray();
			ByteBuffer data = new ByteBuffer();
			data.writeByte((byte)paramList1[1]);
			data.writeByte((byte)paramList1[2]);
			if(bDeviceId)
				data.writeUTF(Client2ServerList.DeviceIdentifier);

			if(null != paramList2)
			{
				for(int i = 0; i < typeArr.Length; ++i)
				{
					switch(typeArr[i])
					{
						case '[':
							_PackDataArr(data, typeArr[i+1], paramList2[i]);
							i += 2;
							break;
						case '<':
							_PackDataList(data, typeArr[i+1], paramList2[i]);
							i += 2;
							break;
						default:
							_PackData(data, typeArr[i], paramList2[i]);
							break;
					}	
				}
			}
		
			return data;
		}

		static private void _PackData(ByteBuffer data, char c, object param)
		{
			switch(c)
			{
				case 'b':
					data.writeBoolean((bool)param);
					break;
				case 'c':
					data.writeByte((byte)param);
					break;
				case 'i':
					data.writeShort((short)param);
					break;
				case 'I':
					data.writeInt((int)param);
					break;
				case 's':
					data.writeUTF((string)param);
					break;
				default:
					break;
			}
		}

		static private void _PackDataArr(ByteBuffer data, char c, object param)
		{
			int len;
			switch(c)
			{
				case 'b':
					bool[] bb = (bool[])param;
					len = bb.Length;
					data.writeInt(len);
					for(int i = 0; i < len; ++i)
						data.writeBoolean(bb[i]);
					break;
				case 'c':
					byte[] cc = (byte[])param;
					len = cc.Length;
					data.writeInt(len);
					for(int i = 0; i < len; ++i)
						data.writeByte(cc[i]);
					break;
				case 'i':
					short[] ii = (short[])param;
					len = ii.Length;
					data.writeInt(len);
					for(int i = 0; i < len; ++i)
						data.writeShort(ii[i]);
					break;
				case 'I':
					int[] II = (int[])param;
					len = II.Length;
					data.writeInt(len);
					for(int i = 0; i < len; ++i)
						data.writeInt(II[i]);
					break;
				case 's':
					string[] ss = (string[])param;
					len = ss.Length;
					data.writeInt(len);
					for(int i = 0; i < len; ++i)
						data.writeUTF(ss[i]);
					break;
				default:
					break;
			}
		}

		static private void _PackDataList(ByteBuffer data, char c, object param)
		{
			int len;
			switch(c)
			{
				case 'b':
					List<bool> bb = (List<bool>)param;
					len = bb.Count;
					data.writeInt(len);
					for(int i = 0; i < len; ++i)
						data.writeBoolean(bb[i]);
					break;
				case 'c':
					List<byte> cc = (List<byte>)param;
					len = cc.Count;
					data.writeInt(len);
					for(int i = 0; i < len; ++i)
						data.writeByte(cc[i]);
					break;
				case 'i':
					List<short> ii = (List<short>)param;
					len = ii.Count;
					data.writeInt(len);
					for(int i = 0; i < len; ++i)
						data.writeShort(ii[i]);
					break;
				case 'I':
					List<int> II = (List<int>)param;
					len = II.Count;
					data.writeInt(len);
					for(int i = 0; i < len; ++i)
						data.writeInt(II[i]);
					break;
				case 's':
					List<string> ss = (List<string>)param;
					len = ss.Count;
					data.writeInt(len);
					for(int i = 0; i < len; ++i)
						data.writeUTF(ss[i]);
					break;
				default:
					break;
			}
		}

		static public void ExecuteShort(ArrayList paramList1, ArrayList paramList2, bool bFlag = false)
		{
			ByteBuffer data = PackData(paramList1, paramList2);
			SocketHandler.GetInst().ShortSend(data, bFlag);
		}
	
		static public void ExecuteLongMain(ArrayList paramList1, ArrayList paramList2)
		{
			ByteBuffer data = PackData(paramList1, paramList2, false);
			SocketHandler.GetInst().LongSend(data, 0);
		}

		static public void ExecuteLongBattle(ArrayList paramList1, ArrayList paramList2)
		{
			ByteBuffer data = PackData(paramList1, paramList2, false);
			SocketHandler.GetInst().LongSend(data, 1);
		}

		static public void ExecuteLongBattleEx(ArrayList paramList1, ArrayList paramList2)
		{
			ByteBuffer data = PackData(paramList1, paramList2, false);
			SocketHandler.GetInst().LongSendEx(data, 1);
		}

		static public void ExecuteLong(ArrayList paramList1, ArrayList paramList2)
		{
			ByteBuffer data = PackData(paramList1, paramList2, false);
			SocketHandler.GetInst().LongSend(data, 1);
		}

		static public void ExecuteUdp(ArrayList paramList1, ArrayList paramList2)
		{
			ByteBuffer data = PackData(paramList1, paramList2, false);
			SocketHandler.GetInst().UdpSend(data);
		}

		static public void ExecuteLog(int iRoleId, string sLog, int iType, int platform, int communication, string bundleVersion, string svnVersion)
		{
			ByteBuffer data = new ByteBuffer();
			data.writeByte(5);
			data.writeInt(iRoleId);
			data.writeUTF(sLog);
			data.writeByte(iType);
			data.writeByte(platform);
			data.writeByte(communication);
			data.writeUTF(bundleVersion);
			data.writeInt(int.Parse(svnVersion));
			//SocketHandler.GetInst().LogSend(data);
		}
	}
}
