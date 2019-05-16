using UnityEngine;

namespace MS
{
	public class BattleItem : MonoBehaviour
	{
		public Transform m_Transform;

		private void Awake()
		{
			m_Transform = transform;
		}
	}
}
