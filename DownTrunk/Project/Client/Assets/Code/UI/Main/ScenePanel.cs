using System.Collections;
using System.Collections.Generic;
using TMPro;
using UnityEngine;
using UnityEngine.UI;

namespace MS
{
	public class ScenePanel : MonoBehaviour
	{
		public Button XBtn;
		public TextMeshProUGUI CoinText;
		public Toggle[] Toggles;
		public Transform Content;
		public ToggleGroup ToggleG;
		public ScenePanelItem ItemRes;

		private GameObject _gameObject;
		private List<ScenePanelItem> _lstSceneItems = new List<ScenePanelItem>();


		public static ScenePanel _inst;
		public static ScenePanel GetInst()
		{
			if(_inst == null)
				ResourceLoader.LoadAssetAndInstantiate("PrefabUI/Main/ScenePanel", SceneLoaderMain.GetInst().mainUIRoot);
			return _inst;
		}

		private void OnDestroy()
		{
			_inst = null;
		}

		private void Awake()
		{
			_inst = this;
			_gameObject = gameObject;
			_gameObject.SetActive(false);
			XBtn.onClick.AddListener(ClosePanel);
		}

		private void Init()
		{
			ConfigTable tbl = ConfigData.GetValue("Scene_Common");
			int count = tbl.m_Data.Count;
			ScenePanelItem item;
			ConfigRow row;
			for(int i = 0; i < count; ++i)
			{
				row = tbl.GetRow(i.ToString());
				item = Instantiate(ItemRes, Content, true);
				item.SetInfo(i, row.GetValue("Name"), row.GetValue("Coin"), row.GetValue("Money"), row.GetValue("Item1"), row.GetValue("Item2"), ToggleG);
				_lstSceneItems.Add(item);
			}

			CoinText.text = PlayerData.Coin.ToString();
			for(int i = 0; i < _lstSceneItems.Count; ++i)
			{
				_lstSceneItems[i].Toggle.isOn = i == PlayerData.CurScene;
				_lstSceneItems[i].SetState(((PlayerData.AllSceneState & 1 << i) >> i) == 1);
			}
		}

		public void SetCoin()
		{
			CoinText.text = PlayerData.Coin.ToString();
		}

		public void SetCurScene()
		{
			for(int i = 0; i < _lstSceneItems.Count; ++i)
				_lstSceneItems[i].Toggle.isOn = i == PlayerData.CurHero;
		}

		public void SetState()
		{
			for(int i = 0; i < _lstSceneItems.Count; ++i)
				_lstSceneItems[i].SetState(((PlayerData.AllSceneState & 1 << i) >> i) == 1);
		}

		public void OpenPanel()
		{
			_gameObject.SetActive(true);
		}

		private void ClosePanel()
		{
			_gameObject.SetActive(false);
			for(int i = 0; i < Toggles.Length; ++i)
			{
				if(Toggles[i].isOn && i != PlayerData.CurScene)
					CommonCommand.ExecuteLongMain(Client2ServerList.GetInst().C2S_PLAYER_SET_SCENE, new ArrayList() { (byte)i });
			}
		}
	}
}
