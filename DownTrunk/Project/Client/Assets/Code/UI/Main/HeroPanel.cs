using System.Collections;
using System.Collections.Generic;
using TMPro;
using UnityEngine;
using UnityEngine.UI;

namespace MS
{
	public class HeroPanel : MonoBehaviour
	{
		public GameObject m_GO;
		public Button XBtn;
		public TextMeshProUGUI CoinText;
		public Toggle[] Toggles;
		public HeroPanelItem ItemRes;
		public Transform Content;
		public ToggleGroup ToggleG;

		private GameObject _gameObject;
		private List<HeroPanelItem> _lstHeroItems = new List<HeroPanelItem>();

		public static HeroPanel _inst;
		public static HeroPanel GetInst()
		{
			if(_inst == null)
				ResourceLoader.LoadAssetAndInstantiate("PrefabUI/Main/HeroPanel", SceneLoaderMain.GetInst().mainUIRoot);
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

			Init();
		}

		private void Init()
		{
			ConfigTable tbl = ConfigData.GetValue("Hero_Common");
			int count = tbl.m_Data.Count;
			HeroPanelItem item;
			ConfigRow row;
			for(int i = 0; i < count; ++i)
			{
				row = tbl.GetRow(i.ToString());
				item = Instantiate(ItemRes, Content, true);
				item.SetInfo(i, row.GetValue("Name"), row.GetValue("Coin"), row.GetValue("Money"), ToggleG);
				_lstHeroItems.Add(item);
			}
		}

		public bool IsActive()
		{
			return m_GO.activeSelf;
		}

		public void OpenPanel()
		{
			CoinText.text = PlayerData.Coin.ToString();
			for(int i = 0; i < _lstHeroItems.Count; ++i)
			{
				_lstHeroItems[i].Toggle.isOn = i == PlayerData.CurHero;
				_lstHeroItems[i].SetState((PlayerData.AllHeroState & 1 << i) == 1);
			}
			_gameObject.SetActive(true);
		}

		private void ClosePanel()
		{
			_gameObject.SetActive(false);
			for(int i = 0; i < _lstHeroItems.Count; ++i)
			{
				if(_lstHeroItems[i].Toggle.isOn && i != PlayerData.CurHero)
					CommonCommand.ExecuteLongMain(Client2ServerList.GetInst().C2S_PLAYER_SET_HERO, new ArrayList() { (byte)i });
			}
		}
	}
}