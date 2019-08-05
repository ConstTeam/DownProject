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

		public Dictionary<int, BattleHeroBase>  m_dicHeros = new Dictionary<int, BattleHeroBase>(); 

		private Dictionary<int, int>				_dicPlayerIndex	= new Dictionary<int, int>();
		private Dictionary<int, BattleField>		_dicField		= new Dictionary<int, BattleField>();
		private Dictionary<int, BattlePlayerInfo>	_dicPlayerInfo	= new Dictionary<int, BattlePlayerInfo>();
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
			int playerId = PlayerData.PlayerId, index = 0;
			_dicPlayerIndex.Add(index, playerId);

			BattleField field = ResourceLoader.LoadAssetAndInstantiate("Prefab/BattleFiled", BattleRootTran, pos).GetComponent<BattleField>();
			field.Load(playerId, PlayerData.CurHero, PlayerData.CurScene);
			_dicField.Add(playerId, field);

			m_RoleM = ResourceLoader.LoadAssetAndInstantiate("Prefab/BattleHeroM", _dicField[playerId].ForegroundTran).GetComponent<BattleHeroM>();
			m_RoleM.Init(playerId, PlayerData.CurHero);
			m_dicHeros.Add(playerId, m_RoleM);
		}

		public void LoadOther(Vector3 pos, BattlePlayerData other, int index, float scale)
		{
			int playerId = other.PlayerId;
			_dicPlayerIndex.Add(index, playerId);

			BattleField field = ResourceLoader.LoadAssetAndInstantiate("Prefab/BattleFiled", BattleRootTran, pos).GetComponent<BattleField>();
			field.SetScale(scale);
			field.Load(playerId, other.HeroId, other.SceneId);
			_dicField.Add(playerId, field);

			BattleHeroBase heroE = ResourceLoader.LoadAssetAndInstantiate("Prefab/BattleHeroE", _dicField[playerId].ForegroundTran).GetComponent<BattleHeroBase>();
			heroE.Init(playerId, other.HeroId);
			m_dicHeros.Add(playerId, heroE);
		}

		private void LoadPlayerInfo(int playerId, string playerName, int sceneId, int hp, int playerIndex, Vector3 pos)
		{
			BattlePlayerInfo playerInfo = ResourceLoader.LoadAssetAndInstantiate("Prefab/BattlePlayerInfo", BattleRootTran, pos).GetComponent<BattlePlayerInfo>();
			playerInfo.InitData(playerId, playerName, sceneId, hp, playerIndex);
			_dicPlayerInfo.Add(playerId, playerInfo);
		}

		public void LoadSingle(int roomId, int seed, int frequency, int stairs)
		{
			BattleType = 1;
			SetData(roomId, seed, frequency, stairs, false);
			LoadMy(Vector3.zero);
			LoadPlayerInfo(PlayerData.PlayerId, PlayerData.Nickname, PlayerData.CurScene, PlayerData.CurHP, 0, _dicField[PlayerData.PlayerId].Pos + Vector3.up * 4.3f);
		}

		public void LoadDouble(int roomId, int seed, int frequency, int stairs, List<BattlePlayerData> others)
		{
			BattleType = 2;
			SetData(roomId, seed, frequency, stairs, true);
			LoadMy(PositionMgr.vecFieldPosM);
			LoadOther(PositionMgr.vecFieldPosE, others[0], 1, 0.8f);
			LoadPlayerInfo(PlayerData.PlayerId, PlayerData.Nickname, PlayerData.CurScene, PlayerData.CurHP, 0, _dicField[PlayerData.PlayerId].Pos + Vector3.up * 4.3f);
			LoadPlayerInfo(others[0].PlayerId, others[0].PlayerName, others[0].SceneId, others[0].HP, 1, _dicField[others[0].PlayerId].Pos + Vector3.up * 4.3f);
			CommonCommand.ExecuteLongBattle(Client2ServerList.GetInst().C2S_BATTLE_LOADED, new ArrayList(){});
		}

		public void LoadFive(int roomId, int seed, int frequency, int stairs, List<BattlePlayerData> others)
		{
			BattleType = 2;
			SetData(roomId, seed, frequency, stairs, true);
			LoadMy(PositionMgr.vecFieldPosM);

			for(int i = 0; i < others.Count; ++i)
			{
				LoadOther(PositionMgr.arrVecFieldPosE[i], others[i], i + 1, 0.4f);
				LoadPlayerInfo(others[i].PlayerId, others[i].PlayerName, others[i].SceneId, others[i].HP, i + 1, _dicField[others[i].PlayerId].Pos + Vector3.up * 4.3f);
			}
			CommonCommand.ExecuteLongBattle(Client2ServerList.GetInst().C2S_BATTLE_LOADED, new ArrayList(){});
		}

		private Vector3 _playInfoPos = new Vector3(6f, 3f, 0f);
		public void LoadRacing(int roomId, int seed, int frequency, int stairs, List<BattlePlayerData> others)
		{
			BattleType = 3;
			SetData(roomId, seed, frequency, stairs, false);
			LoadMy(Vector3.zero);
			LoadPlayerInfo(PlayerData.PlayerId, PlayerData.Nickname, PlayerData.CurScene, PlayerData.CurHP, 0, _playInfoPos);
			for(int i = 0; i < others.Count; ++i)
			{
				_dicPlayerIndex.Add(i + 1, others[i].PlayerId);
				BattleHeroBase heroE = ResourceLoader.LoadAssetAndInstantiate("Prefab/BattleHeroE", _dicField[PlayerData.PlayerId].ForegroundTran).GetComponent<BattleHeroBase>();
				heroE.Init(others[i].PlayerId, others[i].HeroId);
				m_dicHeros.Add(others[i].PlayerId, heroE);
				LoadPlayerInfo(others[i].PlayerId, others[i].PlayerName, others[i].SceneId, others[i].HP, i + 1, _playInfoPos + Vector3.down * 1.2f);
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
			if(BattleType == 2)
			{
				for(int i = 0; i < _dicPlayerIndex.Count; ++i)
					_dicField[_dicPlayerIndex[i]].SetPos(frame * Frequency * 0.001f);
			}
			else if(BattleType == 3)
				_dicField[PlayerData.PlayerId].SetPos(frame * Frequency * 0.001f);
		}

		public void SetRolePos(int playerId, float x, float y)
		{
			if(PlayerData.PlayerId != playerId)
				m_dicHeros[playerId].SetPos(x, y);
		}

		public void RemovePlat(int playerId, BattlePlat plat)
		{
			_dicField[playerId].RemovePlat(plat);
		}

		public void SyncHp(int playerId, int hp)
		{
			_dicPlayerInfo[playerId].CurHP = hp;
		}

		public int GetHp(int playerId)
		{
			return _dicPlayerInfo[playerId].CurHP;
		}

		public void EnqueueSkill(int playerId, int skillType)
		{
			_dicField[playerId].EnqueueSkill(skillType);
		}

		public void DequeueSkill(int playerId)
		{
			_dicField[playerId].DequeueSkill();
		}

		public int IndexToPlayer(int index)
		{
			return _dicPlayerIndex[index];
		}

		public void SetFailed(int playerId)
		{
			_dicField[playerId].IsFailed = true;
		}

		#region --Skill----------------------------------------------------------
		public void ReleaseSkill(int fromId, int toId, int type)
		{
			switch(type)
			{
				case 0:
					_dicField[toId].ChangePlatType(3, 0, m_dicHeros[toId]);
					break;
				case 1:
					//_lstFields[toId].ChangeHeroHp(1);
					break;
				case 2:
					//_lstFields[toId].ChangeHeroHp(-1);
					break;
				case 3:
					_dicField[toId].ChangePlatScale(5, m_dicHeros[toId]);
					break;
			}
			
		}
		#endregion
	}
}
