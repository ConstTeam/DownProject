using System;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using UnityEngine;

namespace MS
{
	public class ConnectLong : ConnectBase
	{
		public delegate void VoidDelegate();
		public VoidDelegate ReconnectFunc;
		public VoidDelegate ConnectedFunc;

		private int			_iType;
		private bool		_bQuitPingThread		= false;
		private bool		_bQuitReceiveMsgThread	= false;
		private StateObject	_state					= new StateObject();
		private IPEndPoint	_iep;
		private Thread		_thRece;
		private Thread		_thPing;


		public void Connect(string sIp, int iPort, int type)
		{
			_iType = type;
			_iep = new IPEndPoint(IPAddress.Parse(sIp), iPort);
			m_Connect = new Socket(NewAddressFamily, SocketType.Stream, ProtocolType.Tcp);
			m_Connect.BeginConnect(_iep, new AsyncCallback(ConnectCallback), m_Connect);
		}
	
		public void Reconnect()
		{
			Close();
			m_Connect = new Socket(NewAddressFamily, SocketType.Stream, ProtocolType.Tcp);
			m_Connect.BeginConnect(_iep, new AsyncCallback(ConnectCallback), m_Connect);
		}
	
		public override void Close()
		{
			_bQuitPingThread		= true;
			_bQuitReceiveMsgThread	= true;

			if(null == m_Connect)
				return;

			try
			{
				m_Connect.Close();
			}
			catch(Exception e)
			{
				Debug.LogError(string.Format("ConnectLongClose:{0}", e.Message));
			}
		}

		public void QuitThread()
		{
			_bQuitPingThread		= true;
			_bQuitReceiveMsgThread	= true;
		}
	
		protected override void ConnectCallback(IAsyncResult ar)
		{
			ByteBuffer data;
			try
			{
				m_Connect.EndConnect(ar);
			}
			catch(SocketException e)
			{
				PostError(3, e.ErrorCode);
				return;
			}

			while((null != _thPing && _thPing.IsAlive) || (null != _thRece && _thRece.IsAlive))
			{
				Thread.Sleep(500);
			}

			_bQuitPingThread		= false;
			_bQuitReceiveMsgThread	= false;

			_thPing = new Thread(SendPingThread);
			_thPing.IsBackground = true;
			_thPing.Start();
		
			_thRece = new Thread(ReceiveMsgThread);
			_thRece.IsBackground = true; 
			_thRece.Start();
		
			data = new ByteBuffer(2);
			data.writeByte(0);
			data.writeByte(1);
			data.writeByte(_iType);

			ServiceManager.PostMessageLong(this, data);
		}

		public override void Send(ByteBuffer data)
		{
			//EncryptTool.Encrypt(ref data.data, m_iEncryptLen, 1);
			ByteBuffer sendData = new ByteBuffer(4 + data.data.Length);
			sendData.writeInt(data.data.Length);
			sendData.writeBytes(data.data);

			if(m_Connect.Connected)
				m_Connect.BeginSend(sendData.data, 0, sendData.data.Length, SocketFlags.None, null, null);
		}
	
		public void SyncSend(ByteBuffer data)
		{
			ByteBuffer sendData = new ByteBuffer(4 + data.data.Length);
			sendData.writeInt(data.data.Length);
			sendData.writeBytes(data.data);

			try
			{
				m_Connect.Send(sendData.data);
			}
			catch(SocketException e)
			{
				PostError(3, e.ErrorCode);
			}
		}
	
		private void SendPing()
		{
			TimeSpan ts = DateTime.Now - DateTime.Parse("1970-01-01");
			long milli = (long)ts.TotalMilliseconds * 10;
			int h = (int)(milli >> 32);
			int l = (int)((milli << 32) >> 32);
			ByteBuffer data = new ByteBuffer(10);
			data.writeByte(0);
			data.writeByte(_iType);
			data.writeInt(h);
			data.writeInt(l);
			SyncSend(data);
		}
	
		private void SendPingThread()
		{
			while(true)
			{
				Thread.Sleep(5000);

				if(_bQuitPingThread)
					return;

				if(ApplicationConst.bAppRunning)
					SendPing();
			}
		}

		byte[] buffer = new byte[1024];
		private void ReceiveMsgThread()
		{
			Socket conn = m_Connect;

			while(true)
			{	
				if(_bQuitReceiveMsgThread)
					return;

				try
				{
					int bytesRead = conn.Receive(buffer);
				
					if (bytesRead > 0)
					{
						ByteBuffer ret = null;
						byte[] tempReceive = new byte[bytesRead];
						Array.Copy(buffer, tempReceive, bytesRead);	
						_state.byteList.AddRange(tempReceive);
						ByteBuffer data = new ByteBuffer(_state.byteList.ToArray());

						int len = -1;
						while(data.available() > 0)
						{
							if(-1 == len)
							{
								if(data.available() < 4)
									break;
							
								len = data.readInt();
							}
							if(data.available() < len)
								break;
							else if(data.available() == len)
							{
								ret = new ByteBuffer(data.readBytes(len));
								_state.byteList.RemoveRange(0, len + 4);
								len = -1;
								ServiceManager.PostMessageLong(this, ret);
								break;
							}
							else if(data.available() > len)
							{
								ret = new ByteBuffer(data.readBytes(len));
								_state.byteList.RemoveRange(0, len + 4);
								len = -1;
								ServiceManager.PostMessageLong(this, ret);
							}
							else
								break;
						}
					}
					else
					{
						PostError(3, 0);
						return;
					}
				}
				catch(SocketException e)
				{
					PostError(3, e.ErrorCode);
					return;
				}
			}
		}

		public override void PostError(int secondId, int errCode)
		{
			ByteBuffer data = new ByteBuffer(6);
			data.writeByte(0);
			data.writeByte(secondId);
			data.writeByte(_iType);
			data.writeInt(errCode);
			ServiceManager.PostMessageLong(this, data);
		}
	}
}
