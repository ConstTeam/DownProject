using System.Collections;
using UnityEngine;
using UnityEngine.UI;

namespace MS
{
	public class BattleResultPanel : MonoBehaviour
	{
		public Button BackBtn;

		private Animator _anim;

		private static BattleResultPanel _inst;

		public static BattleResultPanel GetInst()
		{
			if(_inst == null)
				ResourceLoader.LoadAssetAndInstantiate("PrefabUI/Battle/BattleResultPanel", SceneLoaderMain.GetInst().battleUIRoot);
			return _inst;
		}

		private void Awake()
		{
			_inst = this;
			BackBtn.onClick.AddListener(BackToMainScene);
		}

		public void ShowPanel(bool bWin)
		{
			StartCoroutine(PlayAnim(bWin));
		}

		private IEnumerator PlayAnim(bool bWin)
		{
			yield return new WaitForEndOfFrame();
			gameObject.GetComponent<Animator>().Play(bWin ? "UIBattleResultWin" : "UIBattleResultLose");
		}

		private void BackToMainScene()
		{
			BattleManager.GetInst().IsBattleRun = false;
			SceneLoaderMain.GetInst().ShowMainScene();
			SceneLoaderMain.GetInst().DestroyBattleUI();
			SceneLoader.UnloadBattleScene();
			SearchingPanel.GetInst().ShowPanel(false);
			ResourceMgr.Clear();
		}
	}
}
