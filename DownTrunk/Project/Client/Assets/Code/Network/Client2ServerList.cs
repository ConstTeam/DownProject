using System.Collections;
using UnityEngine;

namespace MS
{
	public class Client2ServerList
	{
		public ArrayList C2S_GM;

		//--大厅服---------------------------------------------------------------------------------------
		public ArrayList C2S_LOGIN_LONG;
		public ArrayList C2S_LOGIN_PVP_REQUEST;

		public ArrayList C2S_PLAYER_SET_SCENE;
		public ArrayList C2S_PLAYER_SET_HERO;

		public ArrayList C2S_STORE_BUY_HERO;




		//--战斗服---------------------------------------------------------------------------------------
		public ArrayList C2S_BATTLE_LOGIN;
		public ArrayList C2S_BATTLE_LOADED;
		public ArrayList C2S_BATTLE_SYNC_HP;
		public ArrayList C2S_BATTLE_GET_ITEM;
		public ArrayList C2S_BATTLE_RELEASE_SKILL;

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
			C2S_LOGIN_LONG				= new ArrayList() { "sssssss",	ModuleDataFirst.MODULE_LOGIN,			(byte)1 };
			C2S_LOGIN_PVP_REQUEST		= new ArrayList() { "",			ModuleDataFirst.MODULE_LOGIN,			(byte)2 };

			C2S_PLAYER_SET_SCENE		= new ArrayList() { "c",		ModuleDataFirst.MODULE_PLAYER,			(byte)1 };
			C2S_PLAYER_SET_HERO			= new ArrayList() { "c",		ModuleDataFirst.MODULE_PLAYER,			(byte)2 };

			C2S_STORE_BUY_HERO			= new ArrayList() { "cb",		ModuleDataFirst.MODULE_STORE,			(byte)1 };

			//--战斗服---------------------------------------------------------------------------------------
			C2S_BATTLE_LOGIN			= new ArrayList() { "I",		ModuleDataFirst.MODULE_BATTLE_LOGIN,	(byte)1 };

			C2S_BATTLE_LOADED			= new ArrayList() { "",			ModuleDataFirst.MODULE_BATTLE,			(byte)1 };
			C2S_BATTLE_SYNC_HP			= new ArrayList() { "c",		ModuleDataFirst.MODULE_BATTLE,			(byte)2 };
			C2S_BATTLE_GET_ITEM			= new ArrayList() { "c",		ModuleDataFirst.MODULE_BATTLE,			(byte)3 };
			C2S_BATTLE_RELEASE_SKILL	= new ArrayList() { "Icb",		ModuleDataFirst.MODULE_BATTLE,			(byte)4 };
		}
	}
}
