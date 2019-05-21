using UnityEngine;

namespace MS
{
	public class JoyStick : MonoBehaviour
	{
		private GameObject _gameObject;
		private Vector2 _beginPos;
		private float _fDis;

		private void Awake()
		{
			_gameObject = gameObject;
			_gameObject.SetActive(false);
		}

		public void Show(bool bShow)
		{
			_gameObject.SetActive(bShow);
		}

		private void OnMouseDown()
		{
			_beginPos = Input.mousePosition;
		}

		private void OnMouseDrag()
		{
			_fDis = Input.mousePosition.x - _beginPos.x;
			if(_fDis < -10)
				BattleManager.GetInst().m_RoleM.RunLeft();
			else if(_fDis > 10)
				BattleManager.GetInst().m_RoleM.RunRight();
			else
				BattleManager.GetInst().m_RoleM.Idle();
		}

		private void OnMouseUp()
		{
			BattleManager.GetInst().m_RoleM.Idle();
		}
	}
}
