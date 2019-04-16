using System.Collections;
using UnityEngine;

namespace MS
{
	public class MainThreadService : IService
	{
		private const int LONG_CONNECTED		= 1;	//长连接连接成功
		private const int PING_BACK				= 2;	//ping消息返回
		private const int LCONNECT_EXCEPTION	= 3;	//长连接错误(逻辑)
		private const int SCONNECT_EXCEPTION	= 4;	//短连接错误(登陆)
		private const int LONG_MESSAGE_END		= 5;	//长链接消息结束

		public override void ProcessMessage(ConnectBase conn, ByteBuffer data)
		{
			int type, errCode;
			int moduleId = data.readByte();
			switch (moduleId)
			{
				case LONG_CONNECTED:
					type = data.readByte();
					if(type == 0)
						LoginPanel.GetInst().OnPlatformLogin();
					else
						CommonCommand.ExecuteLongBattle(Client2ServerList.GetInst().C2S_BATTLE_LOGIN, new ArrayList() { RoleData.RoleID });

					//ConnectLong.GetInst().ConnectedFunc();
					break;
				case LCONNECT_EXCEPTION:
					type = data.readByte();
					errCode = data.readInt();
					if(10049 == errCode		//Cannot assign requested address
						|| 10051 == errCode	//Network is unreachable
						|| 10054 == errCode	//Connection reset by peer
						|| 10057 == errCode	//Socket is not connected
						|| 10058 == errCode	//Cannot send after socket shutdown
						|| 10060 == errCode	//Connection timed out
						|| 10061 == errCode	//Connection refused
						|| 0 == errCode)	//Directly send error
					{
						if(type == 0)
							ApplicationEntry.ToLoginScene();
					}
					else if(10053 != errCode)
						Debug.LogError(string.Format("LCONNECT_EXCEPTION-Code:{0}", errCode));
					break;
				case SCONNECT_EXCEPTION:
					errCode = data.readInt();
					MsgBoxPanel.MsgCallback Reconnect = () =>
					{
						ConnectShort co = (ConnectShort)conn;
						SocketHandler.GetInst().ShortSend(co.m_Data, co.m_bFlag, co.m_bNeedEncrypt);
						SocketHandler.ShortSendBackFun();
					};
					MsgBoxPanel.ShowMsgBox(string.Empty, (string)ApplicationConst.dictStaticText["22"], 1, Reconnect);
					break;
				case LONG_MESSAGE_END:
					Connecting.GetInst().ForceHide();
					break;
				default:
					break;
			}
		}
	}
}
