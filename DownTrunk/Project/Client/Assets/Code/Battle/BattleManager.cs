using System.Collections;
using System.Collections.Generic;
using UnityEngine;

namespace MS
{
	public class BattleManager : MonoBehaviour
	{
		public Transform	BattleRootTran;
		public Transform	BattlePoorTran;
		public Camera		BattleCam;
		public JoyStick		JoyStick;

		public int RoomId		{ get; set; }
		public int Frequency	{ get; set; }
		public int Stairs		{ get; set; }

		private Dictionary<int, int> _dicPlayerIndex = new Dictionary<int, int>();
		private List<BattleField> _lstFields = new List<BattleField>();
		private Dictionary<int, BattleHeroBase> _dicRoles	= new Dictionary<int, BattleHeroBase>();
		public BattleHeroM m_RoleM;

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

		public void Load(int roomId, int seed, int frequency, int stairs, List<BattlePlayerData> others)
		{
			RoomId = roomId;
			Frequency = frequency;
			Stairs = stairs;
			_rand = new System.Random(seed);
			LoadFieldData();
			BattleField field = ResourceLoader.LoadAssetAndInstantiate("Prefab/BattleFiled", BattleRootTran, PositionMgr.vecFieldPosM).GetComponent<BattleField>();
			field.InitData(0, PlayerData.Nickname, PlayerData.CurHero, PlayerData.CurScene, PlayerData.CurHP);
			_lstFields.Add(field);
			_lstFields[field.PlayerIndex].Load();
			_dicPlayerIndex.Add(PlayerData.PlayerId, field.PlayerIndex);
			m_RoleM = ResourceLoader.LoadAssetAndInstantiate("Prefab/BattleHeroM", _lstFields[0].ForegroundTran).GetComponent<BattleHeroM>();
			m_RoleM.PlayerId = PlayerData.PlayerId;
			m_RoleM.SetHeroId(PlayerData.CurHero);
			for(int i = 0; i < others.Count; ++i)
			{
				field = ResourceLoader.LoadAssetAndInstantiate("Prefab/BattleFiled", BattleRootTran, PositionMgr.vecFieldPosE).GetComponent<BattleField>();
				field.InitData(i + 1, others[i].PlayerName, others[i].HeroId, others[i].SceneId, others[i].HP);
				_lstFields.Add(field);
				_lstFields[field.PlayerIndex].Load();
				_dicPlayerIndex.Add(others[i].PlayerId, field.PlayerIndex);
				_dicRoles.Add(others[i].PlayerId, ResourceLoader.LoadAssetAndInstantiate("Prefab/BattleHeroE", _lstFields[i + 1].ForegroundTran).GetComponent<BattleHeroBase>());
				_dicRoles[others[i].PlayerId].PlayerId = others[i].PlayerId;
				_dicRoles[others[i].PlayerId].SetHeroId(others[i].HeroId);
			}
			CommonCommand.ExecuteLongBattle(Client2ServerList.GetInst().C2S_BATTLE_LOADED, new ArrayList(){ });
		}

		private void LoadFieldData()
		{
			_lstFieldData.Clear();
			float x, y, lastY = 0;
			int type = 0, item = 0;

			BattleFieldData data = new BattleFieldData(0, 0, 0, 0);
			_lstFieldData.Add(data);
			for (int i = 0; i < Stairs; ++i)
			{
				x = _rand.Next(-5, 6) / 2f;
				y = lastY - _rand.Next(2, 7) / 2f;
				type = _rand.Next(0, ApplicationConst.iPlatTypeCount);
				if(_rand.Next(0, 100) < 20)
					item = Random.Range(1, 3);

				lastY = y;
				data = new BattleFieldData(x, y, type, item);
				_lstFieldData.Add(data);
			}
		}

		public BattleFieldData GetFieldData(int index)
		{
			return _lstFieldData[index];
		}

		public void SetFieldPos(int frame)
		{
			for(int i = 0; i < _lstFields.Count; ++i)
				_lstFields[i].SetPos(frame * Frequency * 0.001f);
		}

		public void SetRolePos(int roleId, float x, float y)
		{
			if(PlayerData.PlayerId != roleId)
				_dicRoles[roleId].SetPos(x, y);
		}

		public void RemovePlat(int playerId, BattlePlat plat)
		{
			_lstFields[_dicPlayerIndex[playerId]].RemovePlat(plat);
		}

		public void SyncHp(int playerId, int hp)
		{
			_lstFields[_dicPlayerIndex[playerId]].HP = hp;
		}

		public int GetHp(int playerId)
		{
			return _lstFields[_dicPlayerIndex[playerId]].HP;
		}

		public void EnqueueSkill(int playerId, int skillType)
		{
			_lstFields[_dicPlayerIndex[playerId]].EnqueueSkill(skillType);
		}
	}
}
