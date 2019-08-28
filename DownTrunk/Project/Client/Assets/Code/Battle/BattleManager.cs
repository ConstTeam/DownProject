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

		public Dictionary<int, BattleHero>  m_dicHeros = new Dictionary<int, BattleHero>(); 

		private Dictionary<int, int>				_dicPlayerIndex	= new Dictionary<int, int>();
		private Dictionary<int, BattleField>		_dicField		= new Dictionary<int, BattleField>();
		private Dictionary<int, BattlePlayerInfo>	_dicPlayerInfo	= new Dictionary<int, BattlePlayerInfo>();
		public BattleHero m_RoleM;

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
			IsBattleRun = false;
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

			m_RoleM = ResourceLoader.LoadAssetAndInstantiate("Prefab/BattleHero", _dicField[playerId].ForegroundTran).GetComponent<BattleHero>();
			m_RoleM.Init(playerId, false, PlayerData.CurHero);
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

			BattleHero heroE = ResourceLoader.LoadAssetAndInstantiate("Prefab/BattleHero", _dicField[playerId].ForegroundTran).GetComponent<BattleHero>();
			heroE.Init(playerId, other.IsRobot, other.HeroId);
			m_dicHeros.Add(playerId, heroE);
		}

		private void LoadPlayerInfo(int playerId, string playerName, int sceneId, int hp, int playerIndex)
		{
			Vector3 pos = _dicField[playerId].Pos + Vector3.up * 4.3f;
			BattlePlayerInfo playerInfo = ResourceLoader.LoadAssetAndInstantiate("Prefab/BattlePlayerInfo", BattleRootTran, pos).GetComponent<BattlePlayerInfo>();
			playerInfo.InitData(playerId, playerName, sceneId, hp, playerIndex);
			_dicPlayerInfo.Add(playerId, playerInfo);
		}

		private void LoadPlayerInfo(BattlePlayerData data, int playerIndex)
		{
			LoadPlayerInfo(data.PlayerId, data.PlayerName, data.SceneId, data.HP, playerIndex);
		}

		public void LoadSingle(int roomId, int seed, int frequency, int stairs)
		{
			BattleType = 1;
			SetData(roomId, seed, frequency, stairs, false);
			LoadMy(Vector3.zero);
			LoadPlayerInfo(PlayerData.PlayerId, PlayerData.Nickname, PlayerData.CurScene, PlayerData.CurHP, 0);
		}

		public void LoadDouble(int roomId, int seed, int frequency, int stairs, List<BattlePlayerData> others)
		{
			BattleType = 2;
			SetData(roomId, seed, frequency, stairs, true);
			LoadMy(PositionMgr.vecFieldPosM);
			LoadOther(PositionMgr.vecFieldPosE, others[0], 1, 0.8f);
			LoadPlayerInfo(PlayerData.PlayerId, PlayerData.Nickname, PlayerData.CurScene, PlayerData.CurHP, 0);
			LoadPlayerInfo(others[0], 1);
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
				LoadPlayerInfo(others[i], i + 1);
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

		public void BattleStart()
		{
			for(int i = 0; i < _dicPlayerIndex.Count; ++i)
				m_dicHeros[_dicPlayerIndex[i]].BattleStart();

			IsBattleRun = true;
			JoyStick.Show(true);
		}

		public void SetFieldPos(int frame)
		{
			for(int i = 0; i < _dicPlayerIndex.Count; ++i)
				_dicField[_dicPlayerIndex[i]].SetPos(frame * Frequency * 0.001f);
		}

		public float GetFieldPos(int playerId)
		{
			return _dicField[playerId].GetPos();
		}

		public BattlePlat GetPlat(int playerId, int index)
		{
			return _dicField[playerId].GetPlat(index);
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
