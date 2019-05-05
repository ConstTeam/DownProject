using UnityEngine;

namespace MS
{
	public class BattleMainPanel : MonoBehaviour
	{
		private static BattleMainPanel _inst;
		public static BattleMainPanel GetInst()
		{
			if(_inst == null)
				ResourceLoader.LoadAssetAndInstantiate("PrefabUI/Battle/BattleMainPanel", SceneLoaderMain.GetInst().battleUIRoot);

			return _inst;
		}

		private GameObject _gameObject;
		private Vector2 _beginPos;
		private float _fDis;

		private void Awake()
		{
			_inst = this;
			_gameObject = gameObject;
			_gameObject.SetActive(false);
		}

		private void OnDestroy()
		{
			_inst = null;
		}

		public void ShowPanel()
		{
			_gameObject.SetActive(true);
		}

		private void OnMouseDown()
		{
			_beginPos = Input.mousePosition;
		}

		private void OnMouseDrag()
		{
			_fDis = Input.mousePosition.x - _beginPos.x;
			if(_fDis < -10)
<<<<<<< HEAD
				BattleManager.GetInst().m_RoleM.RunLeft();
			else if(_fDis > 10)
				BattleManager.GetInst().m_RoleM.RunRight();
=======
				BattleMgrM.GetInst().MoveLeft();
			else if(_fDis > 10)
				BattleMgrM.GetInst().MoveRight();
>>>>>>> e5a4dd6ca2269b0f23caf04583f63c3f51491d09
		}

		private void OnMouseUp()
		{

		}
	}
}
