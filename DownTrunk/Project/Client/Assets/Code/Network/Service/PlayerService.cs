using System.Collections.Generic;

namespace MS
{
	public class PlayerService : IService
	{
		private const int PLAYER_INFO_RES	= 1;
		private const int PLAYER_CUR_SCENE	= 2;
		private const int PLAYER_CUR_HERO	= 3;
		private const int PLAYER_COIN		= 4;
		private const int PLAYER_ALL_HEROS	= 5;

		public override void ProcessMessage(ConnectBase conn, ByteBuffer data)
		{
			int moduleId = data.readByte();
			switch (moduleId)
			{
				case PLAYER_INFO_RES:
					LoginPanel.GetInst().SaveAccount();
					ApplicationConst.bGM = data.readBoolean();
					PlayerData.PlayerId = data.readInt();
					PlayerData.Nickname = data.readUTF();
					PlayerData.CurHP	= 5;   //Temp
					SceneLoader.LoadScene("MainScene");
					break;
				case PLAYER_CUR_SCENE:
					PlayerData.CurScene = data.readByte();
					break;
				case PLAYER_CUR_HERO:
					PlayerData.CurHero = data.readByte();
					break;
				case PLAYER_COIN:
					PlayerData.Coin = data.readInt();
					break;
				case PLAYER_ALL_HEROS:
					PlayerData.AllHeroState = data.readInt();				
					break;
			}
		}  
	}
}
