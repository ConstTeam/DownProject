using UnityEngine;

namespace MS
{
	public class BattleColliderBottom : MonoBehaviour
	{
		public delegate void TriggerCallback(BattlePlat plat);
		public TriggerCallback TriggerCbFunc;

		private void OnTriggerEnter2D(Collider2D collision)
		{
			if(collision.CompareTag("Role"))
			{

			}
		}
	}
}
