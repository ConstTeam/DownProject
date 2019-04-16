using System;
using System.Net.Sockets;

namespace MS
{
	public delegate void LogCallback(ConnectLog connection);
	public class ConnectLog : ConnectShort
	{
		private LogCallback m_CallbackFunc;
	
		public ConnectLog(LogCallback callback)
		{
			m_CallbackFunc = callback;
		}
	
		protected override void ConnectCallback(IAsyncResult ar)
		{
			Socket conn = (Socket)ar.AsyncState;
			conn.EndConnect(ar);
			m_CallbackFunc(this);
		}

		public override void Send(ByteBuffer data)
		{
			ByteBuffer sendData = new ByteBuffer();
			sendData.writeInt(data.data.Length);
			sendData.writeBytes(data.data);
			m_Connect.BeginSend(sendData.data, 0, sendData.data.Length, SocketFlags.None,  new AsyncCallback(SendCallback), m_Connect);
		}

		protected override void SendCallback(IAsyncResult ar)
		{
			try
			{
				m_Connect.EndSend(ar);
				m_Connect.Close();
			}
			catch(SocketException se)
			{
				ByteBuffer data = new ByteBuffer();
				data.writeByte(0);
				data.writeByte(4);
				data.writeInt(se.ErrorCode);
				ServiceManager.PostMessageShort(this, data);
				return;
			}
		}
	}
}
