using UnityEngine;

namespace MS
{
	public class BattleRoleBase : MonoBehaviour
	{
		public Transform m_Transform;

		private void Awake()
		{
			m_Transform = transform;
			OnAwake();
		}

		protected virtual void OnAwake() { }

		private Vector3 _tempPos = new Vector3();
		public void SetPos(float x, float y)
		{
			_tempPos.x = x;
			_tempPos.y = y;
			m_Transform.localPosition = _tempPos;
		}
	}
}
