using System.Collections;
using TMPro;
using UnityEngine;

namespace MS
{
	public class BattlePlayerInfo : MonoBehaviour
	{
		public GameObject[] HpGos;
		public TextMeshPro PlayerIndexText;
		public TextMesh PlayerNameText;

		public bool	IsRobot		{ get; set; }
		public int	PlayerId	{ get; set; }

		private string _sPlayerName;
		public string PlayerName
		{
			get { return _sPlayerName; }
			set { _sPlayerName = value; PlayerNameText.text = value; }
		}

		private int _sPlayerIndex;
		public int PlayerIndex
		{
			get { return _sPlayerIndex; }
			set { _sPlayerIndex = value; PlayerIndexText.text = (value + 1).ToString(); }
		}

		public int MaxHP { get; set; }

		private int _iCurHp;
		public int CurHP
		{
			get { return _iCurHp; }
			set
			{
				_iCurHp = Mathf.Min(value, MaxHP);
				for(int i = 0; i < HpGos.Length; ++i)
					HpGos[i].SetActive(false);

				for(int i = 0; i < _iCurHp; ++i)
					HpGos[i].SetActive(true);

				if(_iCurHp == 0)
				{
					if(BattleManager.GetInst().BattleType == 2)
						CommonCommand.ExecuteLongBattle(Client2ServerList.GetInst().C2S_BATTLE_HERO_FAILED, new ArrayList() { PlayerId });
				}
			}
		}

		private Transform _transform;

		private void Awake()
		{
			_transform = transform;
		}

		public void InitData(int playerId, string playerName, int sceneId, int hp, int playerIndex)
		{
			PlayerId	= playerId;
			PlayerIndex	= playerIndex;
			PlayerName	= playerName;
			MaxHP		= hp;
			CurHP		= hp;
		}

		public void ChangeHeroHp(int changeValue)
		{
			CurHP += changeValue;
		}
	}
}
