using System;
using System.Net;
using System.Net.Sockets;
using UnityEngine;

namespace MS
{
	public class ConnectShort:ConnectBase
	{
		public ByteBuffer m_Data;
		public bool m_bFlag = false;
		public bool m_bNeedEncrypt = true;

		public ConnectShort(){}
	
		public ConnectShort(ByteBuffer data, bool flag, bool bEncrypt = true)
		{
			m_Data = data;
			m_bFlag = flag;
			m_bNeedEncrypt = bEncrypt;
		}
	
		public void ConnectToServer(string ip, int port)
		{
			IPEndPoint iep = new IPEndPoint(IPAddress.Parse(ip), port);
			m_Connect.BeginConnect(iep, new AsyncCallback(ConnectCallback), m_Connect);
		}
	
		protected override void ConnectCallback(IAsyncResult ar)
		{
			Socket conn = (Socket)ar.AsyncState;
			try
			{
				conn.EndConnect(ar);
			}
			catch(SocketException se)
			{
				PostError(4, se.ErrorCode);
				return;
			}

			Send(m_Data);
		}
	
		public override void Send(ByteBuffer data)
		{
			if(m_bNeedEncrypt)
			{
				EncryptTool.Encrypt(ref data.data, m_iEncryptLen, 1);
				m_bNeedEncrypt = false;
			}

			ByteBuffer sendData = new ByteBuffer(4 + data.data.Length);
			sendData.writeInt(data.data.Length);
			sendData.writeBytes(data.data);
			m_Connect.BeginSend(sendData.data, 0, sendData.data.Length, SocketFlags.None, new AsyncCallback(SendCallback), m_Connect);
		}
	
		virtual protected void SendCallback(IAsyncResult ar)
		{
			try
			{
				m_Connect.EndSend(ar);
			}
			catch(SocketException se)
			{
				PostError(4, se.ErrorCode);
				return;
			}
		
			StateObject state = new StateObject();
			m_Connect.BeginReceive(state.buffer, 0, StateObject.bufferSize, 0, new AsyncCallback(ReceiveCallback), state);
		}
	
		private void ReceiveCallback(IAsyncResult ar)
		{
			StateObject state = null;
			int bytesRead = 0;
			try
			{
				state = (StateObject)ar.AsyncState;
				bytesRead = m_Connect.EndReceive(ar);
			}
			catch(SocketException se)
			{
				PostError(4, se.ErrorCode);
				return;
			}
		
			if (bytesRead > 0)
			{
				ByteBuffer ret = null;
			
				byte[] tempReceive = new byte[bytesRead];
				Array.Copy(state.buffer, tempReceive, bytesRead);	
				state.byteList.AddRange(tempReceive);
				ByteBuffer data = new ByteBuffer(state.byteList.ToArray());
			
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
						state.byteList.RemoveRange(0, len + 4);
						len = -1;
						ServiceManager.PostMessageShort(this, ret);
						return;
					}
					else
					{
						Debug.LogError("Short connection too long");
						return;
					}
				}

				Array.Clear(state.buffer, 0, state.buffer.Length);
				m_Connect.BeginReceive(state.buffer, 0, StateObject.bufferSize, 0, new AsyncCallback(ReceiveCallback), state);
			}
			else
			{
				//SocketHandler.socketResponse += "\nBase:bytesRead <= 0";
			}
		}

		public override void PostError(int secondId, int errCode)
		{
			ByteBuffer data = new ByteBuffer(6);
			data.writeByte(0);
			data.writeByte(secondId);
			data.writeInt(errCode);
			ServiceManager.PostMessageShort(this, data);
		}
	}
}
