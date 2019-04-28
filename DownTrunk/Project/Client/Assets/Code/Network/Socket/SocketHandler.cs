using System.Collections;
using System.Collections.Generic;
using System.Net;
using UnityEngine;

namespace MS
{
	public delegate void ShortSendExcute();
	public delegate void ShortSendBack();
	public delegate void LongSendExcute();
	public class SocketHandler : MonoBehaviour
	{
		public static string			socketResponse		= "";
		public static float				shortTimeout		= 10;
		public static ShortSendExcute	ShortSendExcuteFun;
		public static ShortSendBack		ShortSendBackFun;
		public static LongSendExcute	LongSendExcuteFun;
	
		private List<ConnectShort>		_lstShortConn;
		private string					_sShortIp;
		private int						_sShortPort;

		private ConnectLong[]			_arrLongConn;
		private string[][]				_arrIpInfo;
		private ConnectUdp				_Udp;

		private static int m_iMultiCode = 0;
		public static int GetMultiCode()
		{
			return ++m_iMultiCode;
		}

		private static SocketHandler m_Inst = null;
		public static SocketHandler GetInst()
		{
			return m_Inst;
		}

		void OnDestroy()
		{
			for(int i = 0; i < _arrLongConn.Length; ++i)
			{
				if(null != _arrLongConn[i])
					_arrLongConn[i].Close();
			}

			UdpEnd();

			m_Inst = null;
		}
	
		void Awake()
		{
			m_Inst = this;
		}

		public void Init(int count)
		{
			_lstShortConn	= new List<ConnectShort>();
			_arrLongConn	= new ConnectLong[count];
			_arrIpInfo		= new string[count][];
		}

		public void ShortSetUrl(string sIp, int iPort)
		{
			IPAddress[] ips	= Dns.GetHostAddresses(sIp);
			_sShortIp		= ConnectBase.SetIpType(ips[0].ToString());
			_sShortPort		= iPort;
		}

		public void ShortSend(ByteBuffer data, bool flag = false, bool bEncrypt = true)
		{
			if(flag && null != ShortSendExcuteFun)
				ShortSendExcuteFun();

			ConnectShort conn = new ConnectShort(data, flag, bEncrypt);
			_lstShortConn.Add(conn);
			conn.ConnectToServer(_sShortIp, _sShortPort);

			StartCoroutine(ShortTimeout(conn));
		}

		private IEnumerator ShortTimeout(ConnectShort conn)
		{
			yield return new WaitForSeconds(shortTimeout);
			int index = _lstShortConn.IndexOf(conn);
			if(index >= 0)
			{
				conn.PostError(4, 0);
			}
		}
	
		public void ShortClose(ConnectShort conn)
		{
			int index = _lstShortConn.IndexOf(conn);
			if(index >= 0)
			{
				_lstShortConn[index].Close();
				_lstShortConn.RemoveAt(index);
			}
		}

		public int GetShortConnListCount()
		{
			return _lstShortConn.Count;
		}

		public void LongSetUrl(string sIp, string sPort, int type)
		{
			IPAddress[] ips		= Dns.GetHostAddresses(sIp);
			_arrIpInfo[type]	= new string[]{ConnectBase.SetIpType(ips[0].ToString()), sPort};
		}

		public void LongConnect(int type)
		{
			try
			{
				if(null != _arrLongConn[type])
					_arrLongConn[type].Close();

				_arrLongConn[type] = new ConnectLong();
				_arrLongConn[type].Connect(_arrIpInfo[type][0], int.Parse(_arrIpInfo[type][1]), type);
			}
			catch(System.Exception e)
			{
				Debug.LogError(e.Message);
			}
		}

		public void LongSend(ByteBuffer data, int type) 
		{
			if(null != LongSendExcuteFun)
				LongSendExcuteFun();
			if(_arrLongConn[type] != null)
				_arrLongConn[type].Send(data);
		}

		public void LongSendEx(ByteBuffer data, int type) 
		{
			if(_arrLongConn[type] != null)
				_arrLongConn[type].Send(data);
		}

		public void LongClose(int type)
		{
			if(_arrLongConn[type] == null)
				return;

			_arrLongConn[type].Close();
			_arrLongConn[type] = null;
		}

		public void LongQuitThread(int type)
		{
			_arrLongConn[type].QuitThread();
		}

		public void LongReconnect(int type)
		{
			_arrLongConn[type].Reconnect();
		}

		public void UdpStart()
		{
			_Udp = new ConnectUdp("192.168.1.49", 8801);
		}

		public void UdpEnd()
		{
			if(_Udp != null)
			{
				_Udp.Close();
				_Udp = null;
			}
		}

		public void UdpSend(ByteBuffer data)
		{
			_Udp.Send(data);
		}
	}
}
