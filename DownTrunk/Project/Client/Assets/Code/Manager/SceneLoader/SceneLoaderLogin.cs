using UnityEngine;

namespace MS
{
	public class SceneLoaderLogin : SceneLoaderBase
	{
		public Transform uiRoot;

		private void Awake()
		{
			Clear();
			LoadPanel();
		}

		private void OnDestroy()
		{
			Clear();
		}

		private void LoadPanel()
		{
			ResourceLoader.LoadAssetAndInstantiate("PrefabUI/Login/LoginPanel",			uiRoot);
			ResourceLoader.LoadAssetAndInstantiate("PrefabUI/Login/ServerListPanel",	uiRoot);
			ResourceLoader.LoadAssetAndInstantiate("PrefabUI/Login/CheckResPanel",		uiRoot);
		}
	}
}
