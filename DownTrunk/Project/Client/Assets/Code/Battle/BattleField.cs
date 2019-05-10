using TMPro;
using UnityEngine;

namespace MS
{
	public class BattleField : MonoBehaviour
	{
		public Transform		SpriteMaskTran;
		public Transform		ForegroundTran;
		public TextMeshPro		PlayerIndexText;
		public TextMesh			PlayerNameText;
		public GameObject[]		HpGos;
		public BattleColliderTop DisTrigger;

		[HideInInspector]
		public Transform Background;

		public int		SceneId	{ get; set; }

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

		public void InitData(int sceneId, int playerIndex, string playerName, int hp)
		{
			SceneId = sceneId;
			PlayerIndex = playerIndex;
			PlayerName = playerName;
			HP = hp;
		}

		public void SetPos(float y)
		{
			_tempPos.y = y;
			ForegroundTran.localPosition = _tempPos;

			_tempPos.y = y / -30f;
			_matBg.mainTextureOffset = _tempPos;
		}

		public void RemovePlat(PlatBase plat)
		{
			plat.m_Transform.SetParent(BattleManager.GetInst().BattlePoorTran);
			plat.m_Transform.localPosition = PositionMgr.vecHidePos;
			ResourceMgr.PushBox(SceneId, plat.Type, plat);
			int newIndex = plat.Index + 30;
			if(newIndex < BattleManager.GetInst().Stairs)
				AddPlat(plat.Index + 30);
		}

		public void AddPlat(int index)
		{
			BattleFieldData field = BattleManager.GetInst().GetFieldData(index);
			PlatBase plat = ResourceMgr.PopBox(SceneId, field.Type);
			plat.Index = index;
			plat.m_Transform.SetParent(ForegroundTran);
			plat.m_Transform.localPosition = new Vector3(field.X, field.Y, 0f);
		}
	}
}
