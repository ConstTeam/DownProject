namespace MS
{
	public class BattleService : IService
	{
		private const int BEGIN	= 1;

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
					SocketHandler.GetInst().UdpStart();
					break;
				}
			}
		}
	}
}
