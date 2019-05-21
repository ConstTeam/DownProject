using System.Collections.Generic;
using TMPro;
using UnityEngine;

namespace MS
{
	public class BattleField : MonoBehaviour
	{
		public Transform		SpriteMaskTran;
		public Transform		ForegroundTran;
		public Transform		SkillBarTran;
		public TextMeshPro		PlayerIndexText;
		public TextMesh			PlayerNameText;
		public GameObject[]		HpGos;
		public BattleColliderTop DisTrigger;

		[HideInInspector]
		public Transform Background;

		public int HeroId	{ get; set; }
		public int SceneId	{ get; set; }

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

		private int _iHp;
		public int HP
		{
			get { return _iHp; }
			set
			{
				_iHp = value;
				for(int i = 0; i < HpGos.Length; ++i)
					HpGos[i].SetActive(false);

				for(int i = 0; i < value; ++i)
					HpGos[i].SetActive(true);
			}
		}

		private Transform _transform;
		private Material _matBg;
		private Vector3 _tempPos = new Vector3();
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

		public void InitData(int playerIndex, string playerName, int heroId, int sceneId, int hp)
		{
			PlayerIndex = playerIndex;
			PlayerName = playerName;
			HeroId = heroId;
			SceneId = sceneId;
			HP = hp;
			SetMainSkill(heroId);
		}

		public void SetPos(float y)
		{
			_tempPos.y = y;
			ForegroundTran.localPosition = _tempPos;

			_tempPos.y = y / -30f;
			_matBg.mainTextureOffset = _tempPos;
		}

		public void RemovePlat(BattlePlat plat)
		{
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
			plat.m_Transform.SetParent(ForegroundTran);
			plat.m_Transform.localPosition = new Vector3(field.X, field.Y, 0f);
			if(field.Item > 0)
			{
				string itemType = ConfigData.GetValue("Scene_Common", SceneId.ToString(), field.Item.ToString());
				if(itemType != "0")
				{
					BattleItem item = ResourceMgr.PopItem(int.Parse(itemType));
					item.m_Transform.SetParent(plat.m_Transform, false);
				}
			}
			_dicPlat.Add(index, plat);
		}

		public void SetMainSkill(int skillId)
		{
			BattleSkillBtn skill = ResourceMgr.PopSkill(skillId);
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
	}
}
