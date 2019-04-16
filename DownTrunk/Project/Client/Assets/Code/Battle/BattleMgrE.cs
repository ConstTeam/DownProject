using UnityEngine;

namespace MS
{
	public class BattleMgrE : MonoBehaviour
	{
		public BattleFieldE m_BattleFiledE;
		public RoleE		m_RoleE;

		private static BattleMgrE _inst;
		public static BattleMgrE GetInst()
		{
			return _inst;
		}

		private void Awake()
		{
			_inst = this;
		}

		private void OnDestroy()
		{
			_inst = null;
		}

		public void LoadBattleFiled()
		{
			m_BattleFiledE.Load();
		}

		public void SetPos(float roleX, float roleY, float fieldX, float fieldY)
		{
			m_RoleE.SetPos(roleX, roleY);
			m_BattleFiledE.SetPos(fieldX, fieldY);
		}
	}
}
