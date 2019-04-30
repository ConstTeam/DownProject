using System;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using UnityEngine;

namespace MS
{
	public class ConnectUdp
	{
		private IPEndPoint	_ipEndClient;
		private IPEndPoint	_ipEnd;
		private EndPoint	_serverEnd;

        private Thread      _receiveThread;
		private Socket		_connect;


		public ConnectUdp(string sIp, int iPort)
		{
			_connect = new Socket(ConnectBase.NewAddressFamily, SocketType.Dgram, ProtocolType.Udp);
			

			_ipEndClient = new IPEndPoint(IPAddress.Any, 0);
			_serverEnd = _ipEndClient;

			_ipEnd = new IPEndPoint(IPAddress.Parse(sIp), iPort);
			_connect.Bind(_ipEndClient);

			_receiveThread = new Thread(new ThreadStart(ReceiveData));
			_receiveThread.Start();
        }


		byte[] sendData = new byte[32 * 4];
		public void Send(ByteBuffer buff)
		{
			_connect.SendTo(buff.data, buff.data.Length, SocketFlags.None, _ipEnd);
		}

		public void Close()
		{
			if(_receiveThread != null)
			{
				_receiveThread.Interrupt();
				_receiveThread.Abort();
			}
			
			if(_connect != null)
				_connect.Close();
		}

		byte[] recvData;
		private int _iReceLen = 0;
        private void ReceiveData()
        {
			while(true)
            {
				recvData = new byte[1024];
				try
                {
					_iReceLen = _connect.ReceiveFrom(recvData, ref _serverEnd);
				}
				catch (Exception err)
				{
					Debug.Log(err);
				}

				if(_iReceLen > 0)
				{
					ByteBuffer data = new ByteBuffer(recvData);
					ServiceManager.PostMessageUdp(data);
				}
            }
        }
    }
}
