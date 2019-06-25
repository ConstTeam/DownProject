using System.Collections;
using UnityEngine;
using UnityEngine.UI;

namespace MS
{
	public class SearchingPanel : MonoBehaviour
	{
		public Button XBtn;
		private GameObject _gameObject;

		public static SearchingPanel _inst;
		public static SearchingPanel GetInst()
		{
			if(_inst == null)
				ResourceLoader.LoadAssetAndInstantiate("PrefabUI/Main/SearchingPanel", SceneLoaderMain.GetInst().mainUIRoot);
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
			XBtn.onClick.AddListener(CancelSearch);
		}

		private void CancelSearch()
		{
			CommonCommand.ExecuteLongMain(Client2ServerList.GetInst().C2S_LOGIN_PVP_CANCEL, new ArrayList(){});
		}

		public void ShowPanel(bool bShow)
		{
			_gameObject.SetActive(bShow);
		}
	}
}
