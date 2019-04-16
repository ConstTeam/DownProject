using System;
using System.Collections.Generic;
using System.Net.Sockets;
using UnityEngine;

namespace MS
{
	public class StateObject
	{
		public const int bufferSize	= 1024;
		public byte[] buffer		= new byte[bufferSize];
		public List<byte> byteList	= new List<byte>();
	}

	public class ConnectBase
	{
		public static AddressFamily NewAddressFamily = AddressFamily.InterNetwork;
		protected Socket m_Connect;
		protected int m_iEncryptLen;
		
		public ConnectBase()
		{ 
			m_iEncryptLen = int.Parse(ConfigData.GetValue("InitValues_Common", "SOCKET_ENCRYPT_LEN", "Value"));
			m_Connect = new Socket(NewAddressFamily, SocketType.Stream, ProtocolType.Tcp);
		}
	
		public virtual void Close()
		{
			try
			{
				m_Connect.Close();
			}
			catch(System.Exception e)
			{
				Debug.LogError(string.Format("ConnectBaseClose:{0}", e.Message));
			}
			m_Connect = null;
		}

		public virtual void PostError(int secondId, int errCode){}
		public virtual void Send(ByteBuffer data){}
		protected virtual void ConnectCallback(IAsyncResult ar){}
	
		//ipv6-----------------------------------------------------------------------------------------------------------------
		public static string GetIPv6(string host)
		{
	#if UNITY_IOS && !UNITY_EDITOR
			return CommunicateWithOC.GetIPv6(host);
	#else
			return string.Format("{0}&&ipv4", host);
	#endif
		}
	
		private static void GetIPType(String serverIp, out String newServerIp, out AddressFamily mIPType)
		{
			mIPType = AddressFamily.InterNetwork;
			newServerIp = serverIp;
			try
			{
				string mIPv6 = GetIPv6(serverIp);
				if (!string.IsNullOrEmpty(mIPv6))
				{
					string[] m_StrTemp = System.Text.RegularExpressions.Regex.Split(mIPv6, "&&");
					if (m_StrTemp != null && m_StrTemp.Length >= 2)
					{
						string IPType = m_StrTemp[1];
						if (IPType == "ipv6")
						{
							newServerIp = m_StrTemp[0];
							mIPType = AddressFamily.InterNetworkV6;
						}
					}
				}
			}
			catch (Exception e)
			{
				Debug.LogError("GetIPv6 error:" + e);
			}
		}

		public static String SetIpType(String serverIp)
		{
			String newServerIp = "";
			NewAddressFamily = AddressFamily.InterNetwork;
			GetIPType(serverIp, out newServerIp, out NewAddressFamily);
			return (!string.IsNullOrEmpty(newServerIp)) ? newServerIp : serverIp;
		}
		//---------------------------------------------------------------------------------------------------------------------
	}
}
