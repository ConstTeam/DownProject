using UnityEngine;
using UnityEngine.UI;

namespace MS
{
	public class ScenePanel : MonoBehaviour
	{
		public Button XBtn;

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
		}

		private void ClosePanel()
		{
			_gameObject.SetActive(false);
		}
	}
}
