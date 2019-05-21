namespace MS
{
	public class RoleService : IService
	{
		private const int ROLE_INFO_RES     = 1;

		public override void ProcessMessage(ConnectBase conn, ByteBuffer data)
		{
			int moduleId = data.readByte();
			switch (moduleId)
			{
				case ROLE_INFO_RES:
					LoginPanel.GetInst().SaveAccount();
					
					ApplicationConst.bGM	= data.readBoolean();
					PlayerData.PlayerId		= data.readInt();
					PlayerData.Nickname		= data.readUTF();
					SceneLoader.LoadScene("MainScene");
					break;
			}
		}  
	}
}
