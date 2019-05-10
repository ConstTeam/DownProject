using System.Collections.Generic;

namespace MS
{
	public class BattleService : IService
	{
		private const int LOAD		= 1;
		private const int START		= 2;
		private const int SYNC_HP	= 3;

		public override void ProcessMessage(ConnectBase conn, ByteBuffer data)
		{
			int moduleId = data.readByte();
			switch (moduleId)
			{
				case LOAD:
				{
					int roomId = data.readInt();
					int seed = data.readInt();
					int frequency = data.readByte();
					int stairs = data.readInt();
					int size = data.readByte();
					List<BattlePlayerData> others = new List<BattlePlayerData>();
					for(int i = 0; i < size; ++i)
					{
						BattlePlayerData player = new BattlePlayerData();
						player.PlayerId = data.readInt();
						player.PlayerName = string.Format("Player-{0}", player.PlayerId);
						player.HeroId = 0;
						player.SceneId = 0;
						player.HP = 5;
						others.Add(player);
					}
					BattleManager.GetInst().Load(roomId, seed, frequency, stairs, others);
					break;
				}
				case START:
				{
					BattleMainPanel.GetInst().ShowPanel();
					SocketHandler.GetInst().UdpStart();
					BattleManager.GetInst().m_RoleM.IsRunning = true;
					break;
				}
				case SYNC_HP:
				{
					int playerId = data.readInt();
					int hp = data.readByte();
					break;
				}
			}
		}
	}
}
