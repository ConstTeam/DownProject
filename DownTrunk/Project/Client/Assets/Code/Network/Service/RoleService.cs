namespace MS
{
	public class RoleService : IService
	{
		private const int ROLE_INFO_RES     = 1;
		private const int ROLE_CUR_SCENE	= 2;

		public override void ProcessMessage(ConnectBase conn, ByteBuffer data)
		{
			int moduleId = data.readByte();
			switch (moduleId)
			{
				case ROLE_INFO_RES:
					LoginPanel.GetInst().SaveAccount();
					
					ApplicationConst.bGM	= data.readBoolean();
					RoleData.RoleID			= data.readInt();
					RoleData.Nickname		= data.readUTF();
					RoleData.CurScene		= 0;	//Temp
					SceneLoader.LoadScene("MainScene");
					break;
				case ROLE_CUR_SCENE:
					RoleData.CurScene = data.readByte();
					break;
			}
		}  
	}
}
