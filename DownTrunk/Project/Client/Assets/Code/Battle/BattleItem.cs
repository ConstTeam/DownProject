using System.Collections;
using UnityEngine;

namespace MS
{
	public class BattleItem : MonoBehaviour
	{
		public Transform m_Transform;
		public int Type;

		private void Awake()
		{
			m_Transform = transform;
		}

		private void OnTriggerEnter2D(Collider2D collision)
		{
			if(collision.CompareTag("Role"))
			{
				ResourceMgr.PushItem(this);
				BattleHero hero = collision.GetComponent<BattleHero>();
				BattleManager.GetInst().EnqueueSkill(hero.PlayerId, Type);
				CommonCommand.ExecuteLongBattle(Client2ServerList.GetInst().C2S_BATTLE_GET_ITEM, new ArrayList(){ (byte)Type });
			}
		}
	}
}
