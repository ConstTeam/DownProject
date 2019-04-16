using UnityEngine;

namespace MS
{
	public class LoginService : IService
	{
		private const int SERVER_MESSAGEBOX		= 1;
		private const int PVP_REQUEST_RETURN	= 2;
		private const int SERVER_STRING			= 3;

		public override void ProcessMessage(ConnectBase conn, ByteBuffer data)
		{
			int moduleId = data.readByte();
			switch (moduleId)
			{
				case SERVER_MESSAGEBOX:
					int boxType = data.readByte();
					int key = data.readInt();
					int size = data.readByte();
					string value = ConfigData.GetStaticText(key.ToString());
					string[] c = new string[size];
					for (int i = 0; i < size; ++i)
					{
						int a = data.readByte();
						int b = data.readInt();
						c[i] = (0 == a) ? b.ToString() : ConfigData.GetStaticText(b.ToString());
					}
					MsgBoxPanel.ShowMsgBox(string.Empty, string.Format(value, c), boxType);
					break;
				case SERVER_STRING:
					string msg = data.readUTF();
					MsgBoxPanel.ShowMsgBox(string.Empty, msg, 1);
					break;
				case PVP_REQUEST_RETURN:
					string ip = data.readUTF();
					int port = data.readShort();
					SocketHandler.GetInst().LongSetUrl(ip, port.ToString(), 1);
					SocketHandler.GetInst().LongConnect(1);
					break;
			}
		}
	}
}
