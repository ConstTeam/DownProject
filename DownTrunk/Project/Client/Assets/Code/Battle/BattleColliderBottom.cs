using System.Collections;
using UnityEngine;

namespace MS
{
	public class BattleColliderBottom : MonoBehaviour
	{
		private void OnTriggerEnter2D(Collider2D collider)
		{
			if(collider.CompareTag("Role"))
			{
				if(BattleManager.GetInst().BattleType == 2)
				{
					BattleHero hero = collider.GetComponent<BattleHero>();
					CommonCommand.ExecuteLongBattle(Client2ServerList.GetInst().C2S_BATTLE_HERO_FAILED, new ArrayList() { hero.PlayerId });
				}
			}
		}
	}
}
