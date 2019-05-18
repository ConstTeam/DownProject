using UnityEngine;

namespace MS
{
	public class BattleColliderTop : MonoBehaviour
	{
		public delegate void TriggerCallback(BattlePlat plat);
		public TriggerCallback TriggerCbFunc;

		private void OnTriggerEnter2D(Collider2D collision)
		{
			if(collision.CompareTag("Role"))
				BattleManager.GetInst().m_RoleM.ReduceHp(1);
			else
			{
				BattlePlat plat = collision.GetComponent<BattlePlat>();
				if(plat != null)
				{
					TriggerCbFunc(plat);
					return;
				}
					
				BattleItem item = collision.GetComponent<BattleItem>();
				if(item != null)
				{
					ResourceMgr.PushItem(item);
					return;
				}
			}
		}
	}
}
