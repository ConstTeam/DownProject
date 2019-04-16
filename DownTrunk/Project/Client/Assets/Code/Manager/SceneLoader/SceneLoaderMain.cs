using System.Collections;
using UnityEngine;

namespace MS
{
	public class SceneLoaderMain : SceneLoaderBase
	{
		private static SceneLoaderMain _inst;

		public static SceneLoaderMain GetInst()
		{
			return _inst;
		}

		public GameObject	mainUICanvas;
		public Camera		mainUICamera;
		public Transform	mainUIRoot;

		public GameObject	battleUICanvas;
		public Camera		battleUICamera;
		public Transform	battleUIRoot;

		private void Awake()
		{
			_inst = this;
			StartCoroutine(LoadPanel());
		}

		private void OnDestroy()
		{
			_inst = null;
		}

		private IEnumerator LoadPanel()
		{
			SceneLoader.m_fSpeed = 0.1f;

            //主界面
			yield return LoadPanelProgress("PrefabUI/Main/MainPanel");


            yield return SceneLoader.SetProgress(0.01f, 0.99f);
            PanelLoaded();
		}

		private IEnumerator LoadPanelProgress(string path, int order = 0)
		{
			ResourceLoader.LoadPanel(path, mainUIRoot, order);
			yield return SceneLoader.SetProgress(0.01f, 0.99f);
		}

		public void PanelLoaded()
		{
			Clear();
			StartCoroutine(SceneLoader.SetProgress(SceneLoader.m_fProgress, 1f));
			//UIMgr.GetInstance().Open("MainPanel");
		}

		public void LoadBattleScene()
		{
			mainUICanvas.SetActive(false);
			battleUICanvas.SetActive(true);
			SceneLoader.LoadBattleScene();
		}

		public void DestroyBattleUI()
		{
			battleUICanvas.SetActive(false);
			GameObject go;
			int count = battleUIRoot.childCount;
			for(int i = count - 1; i >=0; --i)
			{
				go = battleUIRoot.GetChild(i).gameObject;
				Destroy(go);
			}
		}

		public void ShowMainScene()
		{
			mainUICanvas.SetActive(true);
		}
	}
}
