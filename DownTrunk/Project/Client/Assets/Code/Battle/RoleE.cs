using UnityEngine;

namespace MS
{
	public class RoleE : MonoBehaviour
	{
		private Transform _transform;

		private void Awake()
		{
			_transform = transform;
		}

		private Vector3 _tempPos = new Vector3();
		public void SetPos(float x, float y)
		{
			_tempPos.x = x;
			_tempPos.y = y;
			_transform.localPosition = _tempPos;
		}
	}
}
