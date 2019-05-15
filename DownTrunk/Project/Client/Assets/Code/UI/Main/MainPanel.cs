using System.Collections;
using UnityEngine;
using UnityEngine.UI;

namespace MS
{
	public class MainPanel : MonoBehaviour
	{
		public Button PVPBtn;

		public Button RoleBtn;
		public Button SceneBtn;
		public Button ShopBtn;
		public Button NoticeBtn;
		public Button SettingBtn;

		private void Awake()
		{
			PVPBtn.onClick.AddListener(OnClickPVP);
			//RoleBtn.onClick.AddListener(OnClickPVP);
			SceneBtn.onClick.AddListener(OnClickSceneBtn);
			//ShopBtn.onClick.AddListener(OnClickPVP);
			//NoticeBtn.onClick.AddListener(OnClickPVP);
			//SettingBtn.onClick.AddListener(OnClickPVP);
		}

		private void OnClickPVP()
		{
			CommonCommand.ExecuteLongMain(Client2ServerList.GetInst().C2S_LOGIN_PVP_REQUEST, new ArrayList() { });
		}

		public void OnClickSceneBtn()
		{
			ScenePanel.GetInst().OpenPanel();
		}
	}
}
