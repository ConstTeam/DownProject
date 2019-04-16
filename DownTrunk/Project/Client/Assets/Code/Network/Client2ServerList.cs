using System.Collections;
using UnityEngine;

namespace MS
{
	public class Client2ServerList
	{
		//--大厅服---------------------------------------------------------------------------------------
		/**登录消息---*/
		public ArrayList C2S_LOGIN_LONG;
		public ArrayList C2S_LOGIN_PVP_REQUEST;
        public ArrayList C2S_CANCEL_ASSIGN;
		public ArrayList C2S_ASSIGN_GUIDE_INSTANCE_SERVER;

		public ArrayList C2S_GM;


		//--战斗服---------------------------------------------------------------------------------------
		public ArrayList C2S_BATTLE_LOGIN;
		public ArrayList C2S_BATTLE_SYNC_POS;

        //*******************************************************************************************

        public static string DeviceIdentifier;
		public static Client2ServerList _Inst = null;

		public static Client2ServerList GetInst()
		{
			if (null == _Inst)
				_Inst = new Client2ServerList();
			return _Inst;
		}

		public Client2ServerList()
		{
			//短连接全加上设备ID用以保证，唯一程序唯一设备中运行; 当使用长连接时，可以考虑去掉。
			DeviceIdentifier = SystemInfo.deviceUniqueIdentifier;

			//--大厅服---------------------------------------------------------------------------------------
			//登录
			C2S_LOGIN_LONG			= new ArrayList() { "sssssss",	ModuleDataFirst.MODULE_LOGIN, (byte)1 };
			C2S_LOGIN_PVP_REQUEST	= new ArrayList() { "I",		ModuleDataFirst.MODULE_LOGIN, (byte)2 };

			//--战斗服---------------------------------------------------------------------------------------
			C2S_BATTLE_LOGIN		= new ArrayList() { "I",		ModuleDataFirst.MODULE_BATTLE_LOGIN, (byte)1 };

			C2S_BATTLE_SYNC_POS		= new ArrayList() { "II",		ModuleDataFirst.MODULE_BATTLE, (byte)250 };
		}
	}
}
