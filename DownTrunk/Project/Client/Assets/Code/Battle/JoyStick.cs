using UnityEngine;

namespace MS
{
	public class JoyStick : MonoBehaviour
	{
		private GameObject _gameObject;
		private Vector2 _beginPos;
		private float _fDis;
		private float _fLastDis;

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
			_fLastDis = 0;
		}

		private int _iDirection = 0;
		private void OnMouseDrag()
		{
			_fDis = Input.mousePosition.x - _beginPos.x;	
			if(_fDis * _fLastDis <= 0)
			{
				if(_fDis < -10)
				{
					_iDirection = -1;
					_beginPos = Input.mousePosition;
					_fLastDis = _fDis;
				}
				else if(_fDis > 10)
				{
					_iDirection = 1;
					_beginPos = Input.mousePosition;
					_fLastDis = _fDis;
				}
			}
			else
			{
				_beginPos = Input.mousePosition;
				_fLastDis = _fDis;
			}
			
			if(_iDirection == -1)
				BattleManager.GetInst().RunLeft(PlayerData.PlayerId);
			else if(_iDirection == 1)
				BattleManager.GetInst().RunRight(PlayerData.PlayerId);
		}

		private void OnMouseUp()
		{
			_iDirection = 0;
			_fLastDis = 0;
			BattleManager.GetInst().Idle(PlayerData.PlayerId);
		}
	}
}
