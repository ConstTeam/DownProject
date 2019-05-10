using System.Collections.Generic;

namespace MS
{
	public class BattleService : IService
	{
		private const int LOAD	= 1;
		private const int START = 2;

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
					List<BattlePlayer> others = new List<BattlePlayer>();
					for(int i = 0; i < size; ++i)
					{
						BattlePlayer player = new BattlePlayer();
						player.PlayerId = data.readInt();
						player.PlayerName = string.Format("Player-{0}", player.PlayerId);
						player.HeroId = 0;
						player.SceneId = 0;
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
			}
		}
	}
}
