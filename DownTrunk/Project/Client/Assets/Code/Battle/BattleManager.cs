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
			field.InitData(PlayerData.PlayerId, 0, PlayerData.Nickname, PlayerData.CurScene, PlayerData.CurHP);
			_lstFields.Add(field);
			_lstFields[field.PlayerIndex].Load();
			_dicPlayerIndex.Add(PlayerData.PlayerId, field.PlayerIndex);
			m_RoleM = ResourceLoader.LoadAssetAndInstantiate("Prefab/BattleHeroM", _lstFields[0].ForegroundTran).GetComponent<BattleHeroM>();
			m_RoleM.Init(PlayerData.PlayerId, PlayerData.CurHero);
			_lstFields[field.PlayerIndex].SetHero(m_RoleM);
			for(int i = 0; i < others.Count; ++i)
			{
				field = ResourceLoader.LoadAssetAndInstantiate("Prefab/BattleFiled", BattleRootTran, PositionMgr.vecFieldPosE).GetComponent<BattleField>();
				field.InitData(others[i].PlayerId, i + 1, others[i].PlayerName, others[i].SceneId, others[i].HP);
				_lstFields.Add(field);
				_lstFields[field.PlayerIndex].Load();
				_dicPlayerIndex.Add(others[i].PlayerId, field.PlayerIndex);
				BattleHeroBase heroE = ResourceLoader.LoadAssetAndInstantiate("Prefab/BattleHeroE", _lstFields[i + 1].ForegroundTran).GetComponent<BattleHeroBase>();
				heroE.Init(others[i].PlayerId, others[i].HeroId);
				_lstFields[field.PlayerIndex].SetHero(heroE);
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
				if(_rand.Next(0, 100) < 10)
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

		public void SetRolePos(int playerId, float x, float y)
		{
			if(PlayerData.PlayerId != playerId)
				_lstFields[_dicPlayerIndex[playerId]].Hero.SetPos(x, y);
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

		public int GetPlayerIdByIndex(int index)
		{
			return _lstFields[index].PlayerId;
		}

		#region --Skill----------------------------------------------------------
		public void ReleaseSkill(int fromId, int toId, int type)
		{
			_lstFields[_dicPlayerIndex[fromId]].ChangePlatType(3, 0);
		}
		#endregion
	}
}
