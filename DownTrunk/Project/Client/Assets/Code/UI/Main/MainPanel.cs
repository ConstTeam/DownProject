using System.Collections;
using UnityEngine;
using UnityEngine.UI;

namespace MS
{
	public class MainPanel : MonoBehaviour
	{
		public Button SingleBtn;
		public Button PVPBtn;

		public Button HeroBtn;
		public Button SceneBtn;
		public Button ShopBtn;
		public Button NoticeBtn;
		public Button SettingBtn;

		private void Awake()
		{
			SingleBtn.onClick.AddListener(OnClickSingle);
			PVPBtn.onClick.AddListener(OnClickPVP);
			HeroBtn.onClick.AddListener(OnClickHeroBtnBtn);
			SceneBtn.onClick.AddListener(OnClickSceneBtn);
			//ShopBtn.onClick.AddListener(OnClickPVP);
			//NoticeBtn.onClick.AddListener(OnClickPVP);
			//SettingBtn.onClick.AddListener(OnClickPVP);
		}

		private void OnClickSingle()
		{
			BattleManager.GetInst().LoadSingle(0, 123, 1, 100);
			BattleManager.GetInst().IsBattleRun = true;
			BattleManager.GetInst().JoyStick.Show(true);
		}

		private void OnClickPVP()
		{
			CommonCommand.ExecuteLongMain(Client2ServerList.GetInst().C2S_LOGIN_PVP_REQUEST, new ArrayList() { });
		}

		public void OnClickSceneBtn()
		{
			ScenePanel.GetInst().OpenPanel();
		}

		public void OnClickHeroBtnBtn()
		{
			HeroPanel.GetInst().OpenPanel();
		}
	}
}
