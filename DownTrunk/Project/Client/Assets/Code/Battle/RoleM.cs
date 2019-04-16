using System.Collections;
using UnityEngine;

namespace MS
{
	public class RoleM : MonoBehaviour
	{

		private static RoleM _inst;
		public static RoleM GetInst()
		{
			return _inst;
		}

		public float Speed { get; set; }

		private Transform _transform;

		private void Awake()
		{
			_inst = this;
			_transform = transform;
			Speed = 0.04f;
		}

		private void OnDestroy()
		{
			_inst = null;
		}

		public void MoveLeft()
		{
			_transform.localPosition += Vector3.left * Speed;
		}

		public void MoveRight()
		{
			_transform.localPosition += Vector3.right * Speed;
		}

		float t = 0f;
		private void Update()
		{
			t += Time.deltaTime;
			if(t > 0.1f)
			{
				float x = _transform.localPosition.x;
				float y = _transform.localPosition.y;
				CommonCommand.ExecuteLongBattle(Client2ServerList.GetInst().C2S_BATTLE_LOGIN, new ArrayList() { x, y });
				t = 0f;
			}
		}
	}
}
