namespace MS
{
	public class PlayerService : IService
	{
		private const int PLAYER_INFO_RES		= 1;
		private const int PLAYER_COIN			= 2;
		private const int PLAYER_CUR_HERO		= 3;
		private const int PLAYER_CUR_SCENE		= 4;
		private const int PLAYER_STATE_HERO		= 5;
		private const int PLAYER_STATE_SCENE	= 6;
		private const int PLAYER_SEARCHING		= 7;

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
				case PLAYER_COIN:
					PlayerData.Coin = data.readInt();
					break;
				case PLAYER_CUR_HERO:
					PlayerData.CurHero = data.readByte();
					break;
				case PLAYER_CUR_SCENE:
					PlayerData.CurScene = data.readByte();
					break;
				case PLAYER_STATE_HERO:
					PlayerData.StateHero = data.readInt();				
					break;
				case PLAYER_STATE_SCENE:
					PlayerData.StateScene = data.readInt();
					break;
				case PLAYER_SEARCHING:
					SearchingPanel.GetInst().ShowPanel(data.readBoolean());
					break;
			}
		}  
	}
}
