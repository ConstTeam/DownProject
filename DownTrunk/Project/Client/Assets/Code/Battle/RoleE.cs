using UnityEngine;

namespace MS
{
	public class RoleE : MonoBehaviour
	{
		private static RoleE _inst;
		public static RoleE GetInst()
		{
			return _inst;
		}

		private Transform _transform;

		private void Awake()
		{
			_inst = this;
			_transform = transform;
		}

		private void OnDestroy()
		{
			_inst = null;
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
