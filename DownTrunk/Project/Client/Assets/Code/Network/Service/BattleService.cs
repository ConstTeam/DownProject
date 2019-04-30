using System.Collections.Generic;

namespace MS
{
	public class BattleService : IService
	{
		private const int INIT	= 1;
		private const int START = 2;

		public override void ProcessMessage(ConnectBase conn, ByteBuffer data)
		{
			int moduleId = data.readByte();
			switch (moduleId)
			{
				case INIT:
				{
					int roomId = data.readInt();
					int seed = data.readInt();
					int frequency = data.readByte();
					int stairs = data.readInt();
					int size = data.readByte();
					List<int> otherIds = new List<int>();
					for(int i = 0; i < size; ++i)
					{
						otherIds.Add(data.readInt());
					}
					BattleManager.GetInst().Load(roomId, seed, frequency, stairs, otherIds);
					break;
				}
				case START:
				{
					SocketHandler.GetInst().UdpStart();
					BattleMainPanel.GetInst().ShowPanel();
					BattleManager.GetInst().m_RoleM.IsRunning = true;
					break;
				}
			}
		}
	}
}
