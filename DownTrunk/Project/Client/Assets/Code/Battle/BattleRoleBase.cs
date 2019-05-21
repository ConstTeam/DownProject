using UnityEngine;

namespace MS
{
	public class BattleRoleBase : MonoBehaviour
	{
		public Transform m_Transform;
		public BattleRoleSp RoleSp;
		protected BattleEnum.RoleAnimType _animType;
		protected SpriteRenderer _spRenderer;
		private Sprite[] _sp;


		public int PlayerId { get; set; }

		private void Awake()
		{
			m_Transform = transform;
			_spRenderer = RoleSp.SpRenderer;
			_sp = RoleSp.Sp;
			_animType = BattleEnum.RoleAnimType.Idle;
			OnAwake();
		}

		protected virtual void OnAwake()	{ }
		protected virtual void OnUpdate()	{ }

		private Vector3 _tempPos = new Vector3();
		public void SetPos(float x, float y)
		{
			_tempPos.x = x;
			_tempPos.y = y;
			m_Transform.localPosition = _tempPos;
		}

		private int _runCurFrame = 0;
		private int[] _runFrames = new int[4]{ 1, 2, 3, 2 };
		private void Update()
		{
			OnUpdate();
			switch(_animType)
			{
				case BattleEnum.RoleAnimType.Idle:
					_spRenderer.sprite = _sp[0];
					break;
				case BattleEnum.RoleAnimType.RunLeft:
				case BattleEnum.RoleAnimType.RunRight:
					_spRenderer.sprite = _sp[_runFrames[_runCurFrame]];
					if(++_runCurFrame > 3) _runCurFrame = 0;
					break;
			}
		}
	}
}
