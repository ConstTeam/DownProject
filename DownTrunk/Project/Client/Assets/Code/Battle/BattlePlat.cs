using UnityEngine;

namespace MS
{
	public class BattlePlat : MonoBehaviour
	{
		public int Index	{ get; set; }
		public int Type		{ get; set; }

		public Transform m_Transform;

		private void Awake()
		{
			m_Transform = transform;
		}
	}
}
