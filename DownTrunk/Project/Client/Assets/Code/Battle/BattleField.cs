using System.Collections.Generic;
using TMPro;
using UnityEngine;

namespace MS
{
	public class BattleField : MonoBehaviour
	{
		public Transform			SpriteMaskTran;
		public Transform			ForegroundTran;
		public Transform			SkillBarTran;
		public TextMeshPro			PlayerIndexText;
		public TextMesh				PlayerNameText;
		public GameObject[]			HpGos;
		public BattleColliderTop	DisTrigger;

		[HideInInspector]
		public Transform Background;

		public int				PlayerId	{ get; set; }
		public int				SceneId		{ get; set; }
		public BattleHeroBase	Hero		{ get; set; }

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
			}
		}

		private Transform _transform;
		private Material _matBg;
		private Vector3 _tempPos = new Vector3();
		private int _iCurMinIndex = 0;
		private Dictionary<int, BattlePlat> _dicPlat = new Dictionary<int, BattlePlat>();

		private void Awake()
		{
			_transform = transform;
			DisTrigger.TriggerCbFunc = RemovePlat;
		}

		public void Load()
		{
			ResourceLoader.LoadAssetAndInstantiate(string.Format("Prefab/Scene/{0}/Nails", SceneId), SpriteMaskTran);
			Background = ResourceLoader.LoadAssetAndInstantiate(string.Format("Prefab/Scene/{0}/Background", SceneId), SpriteMaskTran).GetComponent<Transform>();
			_matBg = Background.GetComponent<SpriteRenderer>().material;
			for (int i = 0; i < 30; ++i)
			{
				AddPlat(i);
			}
		}

		public void InitData(int playerId, int playerIndex, string playerName, int sceneId, int hp)
		{
			PlayerId	= playerId;
			PlayerIndex	= playerIndex;
			PlayerName	= playerName;
			SceneId		= sceneId;
			MaxHP		= hp;
			CurHP		= hp;
		}

		public void SetHero(BattleHeroBase hero)
		{
			Hero = hero;
			SetMainSkill(Hero.HeroId);
		}

		public void SetPos(float y)
		{
			_tempPos.y = y;
			ForegroundTran.localPosition = _tempPos;

			_tempPos.y = y / -30f;
			_matBg.mainTextureOffset = _tempPos;
		}

		public void PlatOutField(BattlePlat plat)
		{
			RemovePlat(plat);
			_iCurMinIndex = plat.Index + 1;
		}

		public void RemovePlat(BattlePlat plat)
		{
			_dicPlat.Remove(plat.Index);
			ResourceMgr.PushBox(SceneId, plat.Type, plat);
			int newIndex = plat.Index + 30;
			if(newIndex < BattleManager.GetInst().Stairs)
				AddPlat(newIndex);
		}

		public void AddPlat(int index)
		{
			BattleFieldData field = BattleManager.GetInst().GetFieldData(index);
			BattlePlat plat = ResourceMgr.PopBox(SceneId, field.Type);
			plat.Index = index;
			plat.Y = field.Y;
			plat.m_Transform.SetParent(ForegroundTran);
			plat.m_Transform.localPosition = new Vector3(field.X, field.Y, 0f);
			if(field.Item > 0)
			{
				string itemType = ConfigData.GetValue("Scene_Common", SceneId.ToString(), field.Item.ToString());
				if(itemType != "-1")
				{
					BattleItem item = ResourceMgr.PopItem(int.Parse(itemType));
					item.m_Transform.SetParent(plat.m_Transform, false);
				}
			}
			_dicPlat.Add(index, plat);
		}

		#region --Skill------------------------------------------------------------------
		public void SetMainSkill(int skillId)
		{
			BattleSkillBtn skill = ResourceMgr.PopSkill(skillId);
			skill.IsMainSkill = true;
			skill.m_Transform.SetParent(SkillBarTran);
			skill.SetPosImmediately(-4.7f);
		}

		private Queue<BattleSkillBtn> _queSkill = new Queue<BattleSkillBtn>();
		public void EnqueueSkill(int skillType)
		{
			if(_queSkill.Count > 4)
				DequeueSkill();

			BattleSkillBtn skill = ResourceMgr.PopSkill(skillType);
			skill.m_Transform.SetParent(SkillBarTran);
			skill.SetToOriScale();
			_queSkill.Enqueue(skill);
			ResetSkillBar();
		}

		public void DequeueSkill()
		{
			BattleSkillBtn skill = _queSkill.Dequeue();
			ResourceMgr.PushSkill(skill.Type, skill);
			ResetSkillBar();
		}

		private void ResetSkillBar()
		{
			int i = 0;
			foreach(BattleSkillBtn skill in _queSkill)
			{
				skill.SetPosImmediately(-2.8f + 1.6f * i++);
			}
		}

		//------
		public void ChangePlatType(int count, int type)
		{
			float curHeroY = Hero.m_Transform.localPosition.y;
			for(int i = _iCurMinIndex; i < count; ++i)
			{
				if(_dicPlat.ContainsKey(i))
				{
					if(_dicPlat[i].Y < curHeroY)
						RemovePlat(_dicPlat[i]);
				}
			}
		}

		private Vector3 _vecScale = new Vector3(0.5f, 1f, 1f);
		public void ChangePlatScale(int count)
		{
			float curHeroY = Hero.m_Transform.localPosition.y;
			for(int i = _iCurMinIndex; i < count; ++i)
			{
				if(_dicPlat.ContainsKey(i))
				{
					if(_dicPlat[i].Y < curHeroY)
						_dicPlat[i].m_Transform.localScale = _vecScale;
				}
			}
		}

		public void ChangeHeroHp(int changeValue)
		{
			CurHP += changeValue;
		}
		#endregion
	}
}
