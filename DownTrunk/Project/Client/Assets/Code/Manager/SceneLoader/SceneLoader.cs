using System.Collections;
using UnityEngine;
using UnityEngine.SceneManagement;
using UnityEngine.UI;

namespace MS
{
	public class SceneLoader : MonoBehaviour
	{
		public GameObject BG;
		public Text ProgressTxt;

		public static bool IsSingle = true;

		private static SceneLoader _inst;

		private void OnDestroy()
		{
			_inst = null;
		}

		private void Awake()
		{
			_inst = this;
			BG.SetActive(false);
			SceneManager.sceneLoaded += OnSceneLoaded;
		}

		private void Update()
		{
			if(m_fProgress < m_fProgressTar)
			{
				m_fProgress = Mathf.Min(m_fProgressTar, m_fProgress + m_fSpeed);
				ProgressTxt.text = Mathf.Ceil(m_fProgress * 100).ToString()+"%";

			}
			else if (m_fProgressTar >= 1 && _bAsync)
			{
				BG.SetActive(false);
				ProgressTxt.text = "0%";
				enabled = false;
			}
		}

		public static float m_fSpeed = 0.01f;
		public static float m_fProgress = 0f;
		public static float m_fProgressTar = 1f;
		private static string _sCurSceneName;
		private static bool _bAsync;
		private AsyncOperation _async;

		public static void Reinit()
		{
			m_fSpeed = 0.01f;
			m_fProgress = 0f;
			m_fProgressTar = 0f;
			_bAsync = false;
		}

		public static string sTargetSceneName = "";

		//卸载当前场景 显示进度条
		public static void LoadScene(string sceneName)
		{
			if(_sCurSceneName == sceneName) return;
			_sCurSceneName = sceneName;

			_inst.LoadTargetScene(sceneName, true, true);
		}

		//加载登录场景
		public static void ToLoginScene(bool bDirect)
		{
			if(_sCurSceneName == "LoginScene") return;
			_sCurSceneName = "LoginScene";

			_inst.LoadTargetScene("LoginScene", bDirect, false);
			int count = SceneManager.sceneCount;
			for(int i = 0; i < count; ++i)
			{
				Scene scene = SceneManager.GetSceneAt(i);
				if(scene.name == "BattleScene")
				{
					SceneManager.UnloadSceneAsync("BattleScene");
					break;
				}
			}
		}

		public static void LoadBattleScene()
		{
			if(_sCurSceneName == "BattleScene") return;
			_sCurSceneName = "BattleScene";

			SceneManager.LoadScene("BattleScene", LoadSceneMode.Additive);
		}

		public static void UnloadBattleScene()
		{
			_sCurSceneName = "MainScene";
			SceneManager.UnloadSceneAsync("BattleScene");
		}

		private void ShowBG()
		{
			BG.SetActive(true);
			enabled = true;
		}

		private void LoadTargetScene(string target, bool bUnload, bool bProgress)
		{
			if(bProgress)
				ShowBG();

			Reinit();
			StartCoroutine(_LoadTargetScene(target, bUnload, bProgress));
		}

		IEnumerator _LoadTargetScene(string target, bool bUnload, bool bProgress)
		{
			if(bProgress)
				yield return SetProgress(m_fProgress, 0.1f);

			Scene curScene = SceneManager.GetActiveScene();
			if(bUnload)
				SceneManager.UnloadSceneAsync(curScene);

			Resources.UnloadUnusedAssets();

			_async = SceneManager.LoadSceneAsync(target, LoadSceneMode.Additive);
			yield return _async;
			_bAsync = true;
			SceneManager.SetActiveScene(SceneManager.GetSceneByName(target));
		}

		public static IEnumerator SetProgress(float src, float tar)
		{
			m_fProgress = src;
			m_fProgressTar = tar;
			while(m_fProgress < m_fProgressTar)
				yield return new WaitForEndOfFrame();
		}


		private void OnSceneLoaded(Scene scene, LoadSceneMode mod)
		{
			if(scene.name == "BattleScene" && IsSingle)
			{
				BattleManager.GetInst().LoadSingle(0, 123, 30, 100);
				BattleManager.GetInst().IsBattleRun = true;
				BattleManager.GetInst().JoyStick.Show(true);
			}
		}
	}
}
