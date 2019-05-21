using UnityEngine;

namespace MS
{
	public class RoleM : MonoBehaviour
	{

		public float Speed { get; set; }

		public Transform m_Transform;

		private void Awake()
		{
			m_Transform = transform;
			Speed = 0.04f;
		}

		public void MoveLeft()
		{
			m_Transform.localPosition += Vector3.left * Speed;
		}

		public void MoveRight()
		{
			m_Transform.localPosition += Vector3.right * Speed;
		}
	}
}
