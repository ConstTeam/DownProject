using System.Collections.Generic;

namespace MS
{
	public class BattleService : IService
	{
		private const int LOAD			= 1;
		private const int START			= 2;
		private const int SYNC_HP		= 3;
		private const int GET_ITEM		= 4;
		private const int RELEASE_SKILL	= 5;
		private const int RESULT		= 6;
		private const int HERO_FAILED	= 7;

		public override void ProcessMessage(ConnectBase conn, ByteBuffer data)
		{
			int moduleId = data.readByte();
			switch(moduleId)
			{
				case LOAD:
				{
					int type		= data.readByte();
					int roomId		= data.readInt();
					int seed		= data.readInt();
					int frequency	= data.readByte();
					int stairs		= data.readInt();
					int size		= data.readByte();
					List<BattlePlayerData> others = new List<BattlePlayerData>();
					for(int i = 0; i < size; ++i)
					{
						BattlePlayerData player = new BattlePlayerData();
						player.PlayerId		= data.readInt();
						player.PlayerName	= data.readUTF();
						player.SceneId		= data.readByte();
						player.HeroId		= data.readByte();
						player.HP			= 5;
						others.Add(player);
					}
					if(type == 1)
						BattleManager.GetInst().LoadDouble(roomId, seed, frequency, stairs, others);
					else if(type == 2)
						BattleManager.GetInst().LoadFive(roomId, seed, frequency, stairs, others);
					else if(type == 3)
						BattleManager.GetInst().LoadRacing(roomId, seed, frequency, stairs, others);

					break;
				}
				case START:
				{
					BattleManager.GetInst().IsBattleRun = true;
					BattleManager.GetInst().JoyStick.Show(true);
					SocketHandler.GetInst().UdpStart();
					break;
				}
				case SYNC_HP:
				{
					int playerId = data.readInt();
					int hp = data.readByte();
					BattleManager.GetInst().SyncHp(playerId, hp);
					break;
				}
				case GET_ITEM:
				{
					int playerId = data.readInt();
					int item = data.readByte();
					BattleManager.GetInst().EnqueueSkill(playerId, item);
					break;
				}
				case RELEASE_SKILL:
				{
					int fromId	= data.readInt();
					int toId	= data.readInt();
					int type	= data.readByte();
					bool bMain	= data.readBoolean();
					BattleManager.GetInst().ReleaseSkill(fromId, toId, type);
					if(!bMain)
						BattleManager.GetInst().DequeueSkill(fromId);
					break;
				}
				case RESULT:
				{
					bool bWin = data.readBoolean();
					BattleResultPanel.GetInst().ShowPanel(bWin);
					break;
				}
				case HERO_FAILED:
				{
					int playerId = data.readInt();
					BattleManager.GetInst().SetFailed(playerId);
					break;
				}
			}
		}
	}
}
