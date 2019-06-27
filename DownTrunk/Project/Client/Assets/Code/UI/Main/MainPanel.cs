using System.Collections;
using UnityEngine;
using UnityEngine.UI;

namespace MS
{
	public class MainPanel : MonoBehaviour
	{
		public Button SingleBtn;
		public Button PVPBtn;
		public Button MultiBtn;
		public Button RacingBtn;

		public Button HeroBtn;
		public Button SceneBtn;
		public Button RankBtn;
		public Button NoticeBtn;
		public Button SettingBtn;

		private void Awake()
		{
			SingleBtn.onClick.AddListener(OnClickSingle);
			PVPBtn.onClick.AddListener(OnClickPVP);
			MultiBtn.onClick.AddListener(OnClickMulti);
			RacingBtn.onClick.AddListener(OnClickRacing);
			HeroBtn.onClick.AddListener(OnClickHeroBtnBtn);
			SceneBtn.onClick.AddListener(OnClickSceneBtn);
			//ShopBtn.onClick.AddListener(OnClickPVP);
			//NoticeBtn.onClick.AddListener(OnClickPVP);
			//SettingBtn.onClick.AddListener(OnClickPVP);
		}

		private void OnClickSingle()
		{
			SceneLoader.IsSingle = true;
			SceneLoaderMain.GetInst().LoadBattleScene();
		}

		private void OnClickPVP()
		{
			SceneLoader.IsSingle = false;
			CommonCommand.ExecuteLongMain(Client2ServerList.GetInst().C2S_LOGIN_PVP_REQUEST, new ArrayList(){ (byte)1 });
		}

		private void OnClickMulti()
		{
			SceneLoader.IsSingle = false;
			CommonCommand.ExecuteLongMain(Client2ServerList.GetInst().C2S_LOGIN_PVP_REQUEST, new ArrayList(){ (byte)2 });
		}

		private void OnClickRacing()
		{
			SceneLoader.IsSingle = false;
			CommonCommand.ExecuteLongMain(Client2ServerList.GetInst().C2S_LOGIN_PVP_REQUEST, new ArrayList() { (byte)3 });
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
