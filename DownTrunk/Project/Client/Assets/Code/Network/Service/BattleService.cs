namespace MS
{
	public class BattleService : IService
	{
		private const int BEGIN	= 1;
		private const int SYNC_POS = 2;

		public override void ProcessMessage(ConnectBase conn, ByteBuffer data)
		{
			int moduleId = data.readByte();
			switch (moduleId)
			{
				case BEGIN:
				{
					BattleMainPanel.GetInst().ShowPanel();
					ResourceLoader.LoadAssetAndInstantiate("Prefab/RootM", BattleManager.GetInst().BattleRootTran);
					ResourceLoader.LoadAssetAndInstantiate("Prefab/RootE", BattleManager.GetInst().BattleRootTran);
					BattleMgrM.GetInst().LoadBattleFiled();
					BattleMgrE.GetInst().LoadBattleFiled();
					break;
				}
				case SYNC_POS:
				{
					float roleX = data.readInt() / 1000f;
					float roleY = data.readInt() / 1000f;
					float fieldX = data.readInt() / 1000f;
					float fieldY = data.readInt() / 1000f;
					BattleMgrE.GetInst().SetPos(roleX, roleY, fieldX, fieldY);
					break;
				}
			}
		}
	}
}
