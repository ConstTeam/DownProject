namespace MS
{
	public class BattleService : IService
	{
		private const int BEGIN	= 1;
		private const int SYNC_POS = 250;

		public override void ProcessMessage(ConnectBase conn, ByteBuffer data)
		{
			int moduleId = data.readByte();
			switch (moduleId)
			{
				case BEGIN:
				{
					BattleMainPanel.GetInst().ShowPanel();
					break;
				}
				case SYNC_POS:
				{
					float x = data.readInt() / 1000f;
					float y = data.readInt() / 1000f;
					RoleE.GetInst().SetPos(x, y);
					break;
				}
			}
		}
	}
}
