namespace MS
{
	public class BattleLoginService : IService
	{
		private const int INTO_BATTLE = 1;

		public override void ProcessMessage(ConnectBase conn, ByteBuffer data)
		{
			int moduleId = data.readByte();
			switch (moduleId)
			{
				case INTO_BATTLE:
					SceneLoaderMain.GetInst().LoadBattleScene();
					break;
				default:
					break;
			}
		}  
	}
}
