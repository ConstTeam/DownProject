using System.Collections;
using UnityEngine;
using UnityEngine.UI;

namespace MS
{
	public class ScenePanel : MonoBehaviour
	{
		public Button XBtn;
		public Toggle[] Toggles;

		private GameObject _gameObject;

		private static ScenePanel _inst;
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

		public void OpenPanel()
		{
			_gameObject.SetActive(true);
			for(int i = 0; i < Toggles.Length; ++i)
			{
				Toggles[i].isOn = i == PlayerData.CurScene;
			}
		}

		private void ClosePanel()
		{
			_gameObject.SetActive(false);
			for(int i = 0; i < Toggles.Length; ++i)
			{
				if(Toggles[i].isOn && i != PlayerData.CurScene)
					CommonCommand.ExecuteLongBattle(Client2ServerList.GetInst().C2S_PLAYER_SET_SCENE, new ArrayList() { (byte)i });
			}
		}
	}
}
