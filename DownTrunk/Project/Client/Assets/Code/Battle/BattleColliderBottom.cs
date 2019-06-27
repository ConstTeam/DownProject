using System.Collections;
using UnityEngine;

namespace MS
{
	public class BattleColliderBottom : MonoBehaviour
	{
		private void OnTriggerEnter2D(Collider2D collision)
		{
			if(collision.CompareTag("Role"))
			{
				if(BattleManager.GetInst().BattleType == 2)
				{
					BattleManager.GetInst().m_RoleM.Disable();
					CommonCommand.ExecuteLongBattle(Client2ServerList.GetInst().C2S_BATTLE_HERO_FAILED, new ArrayList() { });
				}
			}	
		}
	}
}
