using System.Collections;
using System.Collections.Generic;
using UnityEngine;

namespace MS
{
	public class BattleManager : MonoBehaviour
	{
		public Transform BattleRootTran;
		public Transform BattlePoorTran;
		public BattleRoleM m_RoleM;
		public Dictionary<int, BattleRoleBase>	m_dicRoles	= new Dictionary<int, BattleRoleBase>();
		public List<BattleField>				m_lstFields	= new List<BattleField>();

		public int Stairs		{ get; set; }
		public int RoomId		{ get; set; }
		public int Frequency	{ get; set; }

		private System.Random _rand;
		private List<BattleFieldData> _lstFieldData = new List<BattleFieldData>();

		private static BattleManager _inst;
		public static BattleManager GetInst()
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

		public void Load(int roomId, int seed, int frequency, int stairs, List<int> otherIds)
		{
			RoomId = roomId;
			Frequency = frequency;
			Stairs = stairs;
			_rand = new System.Random(seed);
			LoadFieldData();
			m_lstFields.Add(ResourceLoader.LoadAssetAndInstantiate("Prefab/BattleFiled", BattleRootTran, PositionMgr.vecFieldPosM).GetComponent<BattleField>());
			m_lstFields[0].Load();
			m_RoleM = ResourceLoader.LoadAssetAndInstantiate("Prefab/BattleRoleM", m_lstFields[0].Background).GetComponent<BattleRoleM>();
			for(int i = 0; i < otherIds.Count; ++i)
			{
				m_lstFields.Add(ResourceLoader.LoadAssetAndInstantiate("Prefab/BattleFiled", BattleRootTran, PositionMgr.vecFieldPosE).GetComponent<BattleField>());
				m_lstFields[i + 1].Load();
				m_dicRoles.Add(otherIds[i], ResourceLoader.LoadAssetAndInstantiate("Prefab/BattleRoleE", m_lstFields[i + 1].Background).GetComponent<BattleRoleBase>());
			}
			CommonCommand.ExecuteLongBattle(Client2ServerList.GetInst().C2S_BATTLE_LOADED, new ArrayList(){ });
		}

		private void LoadFieldData()
		{
			_lstFieldData.Clear();
			float x, y, lastY = 0;
			int type;

			BattleFieldData data = new BattleFieldData(0, 0, 0);
			_lstFieldData.Add(data);
			for (int i = 0; i < Stairs; ++i)
			{
				x = _rand.Next(-5, 6) / 2f;
				y = lastY - _rand.Next(2, 7) / 2f;
				type = _rand.Next(0, 3);
				lastY = y;

				data = new BattleFieldData(x, y, type);
				_lstFieldData.Add(data);
			}
		}

		public BattleFieldData GetFieldData(int index)
		{
			return _lstFieldData[index];
		}

		public void SetFieldPos(int frame)
		{
			for(int i = 0; i < m_lstFields.Count; ++i)
				m_lstFields[i].SetPos(frame * Frequency * 0.001f);
		}

		public void SetRolePos(int roleId, float x, float y)
		{
			if(RoleData.RoleID != roleId)
				m_dicRoles[roleId].SetPos(x, y);
		}
	}
}
