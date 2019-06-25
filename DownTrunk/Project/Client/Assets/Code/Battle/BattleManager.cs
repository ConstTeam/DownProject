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

		public bool	IsBattleRun { get; set; }
		public int	BattleType	{ get; set; }
		public int	RoomId		{ get; set; }
		public int	Frequency	{ get; set; }
		public int	Stairs		{ get; set; }

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
			BattleCam.orthographicSize /= ApplicationConst.screenRatio;
		}

		private void OnDestroy()
		{
			_inst = null;
		}

		private void SetData(int roomId, int seed, int frequency, int stairs, bool loadItem)
		{
			RoomId = roomId;
			Frequency = frequency;
			Stairs = stairs;
			_rand = new System.Random(seed);
			LoadFieldData(loadItem);
		}

		public void LoadMy(Vector3 pos)
		{
			BattleField field = ResourceLoader.LoadAssetAndInstantiate("Prefab/BattleFiled", BattleRootTran, pos).GetComponent<BattleField>();
			field.InitData(PlayerData.PlayerId, 0, PlayerData.Nickname, PlayerData.CurScene, PlayerData.CurHP);
			_lstFields.Add(field);
			_lstFields[field.PlayerIndex].Load();
			_dicPlayerIndex.Add(PlayerData.PlayerId, field.PlayerIndex);
			m_RoleM = ResourceLoader.LoadAssetAndInstantiate("Prefab/BattleHeroM", _lstFields[0].ForegroundTran).GetComponent<BattleHeroM>();
			m_RoleM.Init(PlayerData.PlayerId, PlayerData.CurHero);
			_lstFields[field.PlayerIndex].SetHero(m_RoleM);
		}

		public void LoadOther(Vector3 pos, BattlePlayerData other, int index)
		{
			BattleField field = ResourceLoader.LoadAssetAndInstantiate("Prefab/BattleFiled", BattleRootTran, PositionMgr.vecFieldPosE).GetComponent<BattleField>();
			field.InitData(other.PlayerId, index, other.PlayerName, other.SceneId, other.HP);
			_lstFields.Add(field);
			_lstFields[field.PlayerIndex].Load();
			_dicPlayerIndex.Add(other.PlayerId, field.PlayerIndex);
			BattleHeroBase heroE = ResourceLoader.LoadAssetAndInstantiate("Prefab/BattleHeroE", _lstFields[index].ForegroundTran).GetComponent<BattleHeroBase>();
			heroE.Init(other.PlayerId, other.HeroId);
			_lstFields[field.PlayerIndex].SetHero(heroE);
		}

		public void LoadSingle(int roomId, int seed, int frequency, int stairs)
		{
			SetData(roomId, seed, frequency, stairs, false);
			LoadMy(Vector3.zero);
		}

		public void LoadDouble(int roomId, int seed, int frequency, int stairs, List<BattlePlayerData> others)
		{
			SetData(roomId, seed, frequency, stairs, true);
			LoadMy(PositionMgr.vecFieldPosM);
			LoadOther(PositionMgr.vecFieldPosE, others[0], 1);
			CommonCommand.ExecuteLongBattle(Client2ServerList.GetInst().C2S_BATTLE_LOADED, new ArrayList(){});
		}

		public void LoadSix(int roomId, int seed, int frequency, int stairs, List<BattlePlayerData> others)
		{
			SetData(roomId, seed, frequency, stairs, true);
			LoadMy(PositionMgr.vecFieldPosM);

			for(int i = 0; i < others.Count; ++i)
			{
				LoadOther(PositionMgr.vecFieldPosE, others[i], i + 1);
			}
			CommonCommand.ExecuteLongBattle(Client2ServerList.GetInst().C2S_BATTLE_LOADED, new ArrayList(){});
		}

		private void LoadFieldData(bool loadItem)
		{
			_lstFieldData.Clear();
			float x, y, lastY = 0;
			int type = 0, item = 0;

			BattleFieldData data = new BattleFieldData(0, 0, 0, 0);
			_lstFieldData.Add(data);
			for (int i = 0; i < Stairs; ++i)
			{
				x = _rand.Next(-5, 6) / 2f;
				y = lastY - _rand.Next(4, 8) / 2f;
				type = _rand.Next(0, ApplicationConst.iPlatTypeCount);
				item = loadItem && _rand.Next(0, 100) < 10 ? _rand.Next(1, 2) : 0;

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
			_lstFields[_dicPlayerIndex[playerId]].CurHP = hp;
		}

		public int GetHp(int playerId)
		{
			return _lstFields[_dicPlayerIndex[playerId]].CurHP;
		}

		public void EnqueueSkill(int playerId, int skillType)
		{
			_lstFields[_dicPlayerIndex[playerId]].EnqueueSkill(skillType);
		}

		public void DequeueSkill(int playerId)
		{
			_lstFields[_dicPlayerIndex[playerId]].DequeueSkill();
		}

		public int GetPlayerIdByIndex(int index)
		{
			return _lstFields[index].PlayerId;
		}

		#region --Skill----------------------------------------------------------
		public void ReleaseSkill(int fromId, int toId, int type)
		{
			switch(type)
			{
				case 0:
					_lstFields[_dicPlayerIndex[toId]].ChangePlatType(3, 0);
					break;
				case 1:
					_lstFields[_dicPlayerIndex[toId]].ChangeHeroHp(1);
					break;
				case 2:
					_lstFields[_dicPlayerIndex[toId]].ChangeHeroHp(-1);
					break;
				case 3:
					_lstFields[_dicPlayerIndex[toId]].ChangePlatScale(5);
					break;
			}
			
		}
		#endregion
	}
}
