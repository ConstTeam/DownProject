using UnityEngine;

namespace MS
{
	public class BattleHeroBase : MonoBehaviour
	{
		public Transform m_Transform;
		protected BattleHeroSp RoleSp;
		protected BattleEnum.RoleAnimType _animType;
		protected SpriteRenderer _spRenderer;
		private Sprite[] _sp;

		public int	PlayerId	{ get; set; }
		public bool IsRobot		{ get; set; }
		public int	HeroId		{ get; set; }

		private void Awake()
		{
			m_Transform = transform;	
			_animType = BattleEnum.RoleAnimType.Idle;
			OnAwake();
		}

		public void Init(int playerId, bool bRobot, int heroId)
		{
			PlayerId = playerId;
			IsRobot = bRobot;
			SetHeroId(heroId);
		}

		private void SetHeroId(int id)
		{
			HeroId = id;
			RoleSp = ResourceLoader.LoadAssetAndInstantiate(string.Format("Prefab/Hero/{0}", id), m_Transform).GetComponent<BattleHeroSp>();
			_spRenderer = RoleSp.SpRenderer;
			_sp = RoleSp.Sp;
		}

		protected virtual void OnAwake()	{ }

		private Vector3 _tempPos = new Vector3();
		public void SetPos(float x, float y)
		{
			_tempPos.x = x;
			_tempPos.y = y;
			m_Transform.localPosition = _tempPos;
		}

		private int _runCurFrame = 1;
		private void Update()
		{
			if(!BattleManager.GetInst().IsBattleRun)
				return;

			OnUpdate();
			switch(_animType)
			{
				case BattleEnum.RoleAnimType.Idle:
					_spRenderer.sprite = _sp[0];
					break;
				case BattleEnum.RoleAnimType.RunLeft:
				case BattleEnum.RoleAnimType.RunRight:
					_spRenderer.sprite = _sp[_runCurFrame];
					if(++_runCurFrame > 4) _runCurFrame = 1;
					break;
			}
		}

		protected virtual void OnUpdate()
		{
			if(IsRobot)
			{

			}
		}
	}
}
