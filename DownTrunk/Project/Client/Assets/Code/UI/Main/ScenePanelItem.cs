using System.Collections;
using TMPro;
using UnityEngine;
using UnityEngine.UI;

namespace MS
{
	public class ScenePanelItem : MonoBehaviour
	{
		public TextMeshProUGUI Name;
		public Toggle Toggle;
		public Button BuyBtn;
		public Button CoinBtn;
		public Button MoneyBtn;
		public TextMeshProUGUI CoinBtnText;
		public TextMeshProUGUI MoneyBtnText;

		public int SceneId { get; set; }

		private string _sCoin;
		private string Coin
		{
			get { return _sCoin; }
			set { _sCoin = value; CoinBtnText.text = value; }
		}

		private string _sMoney;
		private string Money
		{
			get { return _sMoney; }
			set { _sMoney = value; MoneyBtnText.text = value; }
		}

		private void Awake()
		{
			BuyBtn.onClick.AddListener(OnClickBuy);
			CoinBtn.onClick.AddListener(OnClickCoin);
			MoneyBtn.onClick.AddListener(OnClickMoney);
		}

		public void SetInfo(int sceneId, string name, string coin, string money, string item1, string item2, ToggleGroup tg)
		{
			SceneId = sceneId;
			Name.text = name;
			Coin = coin;
			Money = money;
			ResourceLoader.LoadAssetAndInstantiate(string.Format("PrefabUI/Main/Scene/{0}", SceneId), transform);
			if(item1 != "-1")
				ResourceLoader.LoadAssetAndInstantiate(string.Format("PrefabUI/Main/Item/{0}", item1), transform, new Vector3(-50f, -310f));
			if(item2 != "-1")
				ResourceLoader.LoadAssetAndInstantiate(string.Format("PrefabUI/Main/Item/{0}", item2), transform, new Vector3(50f, -310f));
			Toggle.group = tg;

			BuyBtn.gameObject.SetActive(Coin != "0" && Money != "0");
			CoinBtn.gameObject.SetActive(Coin != "0" && Money == "0");
			MoneyBtn.gameObject.SetActive(Coin == "0" && Money != "0");

			if(Coin != "0" && Money != "0")
			{
				CoinBtn.transform.localPosition = new Vector2(176f, -410f);
				MoneyBtn.transform.localPosition = new Vector2(176f, -410f + 71f);
				CoinBtn.transform.localScale = new Vector3(0.45f, 0.5f, 1f);
				MoneyBtn.transform.localScale = new Vector3(0.45f, 0.5f, 1f);
			}
		}

		public void SetState(bool bGot)
		{
			if(bGot)
			{
				BuyBtn.gameObject.SetActive(false);
				CoinBtn.gameObject.SetActive(false);
				MoneyBtn.gameObject.SetActive(false);
			}
			Toggle.gameObject.SetActive(bGot);
		}

		public void OnClickBuy()
		{
			CoinBtn.gameObject.SetActive(!CoinBtn.gameObject.activeSelf);
			MoneyBtn.gameObject.SetActive(!MoneyBtn.gameObject.activeSelf);
		}

		public void OnClickCoin()
		{
			if(PlayerData.Coin < int.Parse(Coin))
			{
				MsgBoxPanel.ShowMsgBox("Warning", string.Format("Not enough Coins !"), 1);
				return;
			}
			MsgBoxPanel.MsgCallback OnOk = () => { CommonCommand.ExecuteLongMain(Client2ServerList.GetInst().C2S_STORE_BUY_SCENE, new ArrayList() { (byte)SceneId, true }); };
			MsgBoxPanel.ShowMsgBox("Buy Hero", string.Format("Spend {0} Coins to buy {1} ?", Coin, Name.text), 2, OnOk);
		}

		public void OnClickMoney()
		{
			CommonCommand.ExecuteLongMain(Client2ServerList.GetInst().C2S_STORE_BUY_SCENE, new ArrayList() { (byte)SceneId, false });
		}
	}
}
