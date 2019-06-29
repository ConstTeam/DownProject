using System.Collections;
using UnityEngine;

namespace MS
{
	public class BattleSkillBtn : MonoBehaviour
	{
		public Transform m_Transform;
		private Animation _anim;
		private Vector3 _vecTemp = new Vector3();
		private Vector3 _oriScale = new Vector3(1.5f, 1.5f, 1f);
		private Vector3 _largerScale = new Vector3(1.8f, 1.8f, 1f);
		private Ray _ray;
		private RaycastHit _hit;

		public int Type;

		public bool IsMainSkill = false;

		private void Awake()
		{
			m_Transform = transform;
			_anim = gameObject.GetComponent<Animation>();
		}

		private void OnMouseDown()
		{
			_anim["SkillBtn2"].time = 0;
			_anim["SkillBtn2"].speed = 1;
			_anim.Play("SkillBtn2");
		}

		private void OnMouseUp()
		{
			_ray = BattleManager.GetInst().BattleCam.ScreenPointToRay(Input.mousePosition);
			if(Physics.Raycast(_ray, out _hit, 1000, 1 << LayerMask.NameToLayer("SkillBtn")))
			{
				int playerIndex = int.Parse(_hit.collider.tag);
				int playerId = BattleManager.GetInst().IndexToPlayer(playerIndex - 1);
				CommonCommand.ExecuteLongBattle(Client2ServerList.GetInst().C2S_BATTLE_RELEASE_SKILL, new ArrayList(){ playerId, (byte)Type, IsMainSkill });
			}
			_anim["SkillBtn2"].time = _anim["SkillBtn2"].length;
			_anim["SkillBtn2"].speed = -1;
			_anim.Play("SkillBtn2");
		}

		public void SetToLargerScale()
		{
			m_Transform.localScale = _largerScale;
		}

		public void SetToOriScale()
		{
			m_Transform.localScale = _oriScale;
		}

		public void SetPosImmediately(float y)
		{
			_vecTemp.y = y;
			m_Transform.localPosition = _vecTemp;
		}
	}
}
