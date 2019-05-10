using UnityEngine;

namespace MS
{
	public class BattleColliderTop : MonoBehaviour
	{
		public delegate void TriggerCallback(PlatBase plat);
		public TriggerCallback TriggerCbFunc;

		private void OnTriggerEnter2D(Collider2D collision)
		{
			PlatBase plat = collision.gameObject.GetComponent<PlatBase>();
			if(plat != null)
				TriggerCbFunc(plat);
			else if(collision.CompareTag("Role"))
				BattleManager.GetInst().m_RoleM.ReduceHp(1);
		}
	}
}
