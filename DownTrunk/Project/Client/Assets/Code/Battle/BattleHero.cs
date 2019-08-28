using System.Collections;
using UnityEngine;

namespace MS
{
	public class BattleHero : MonoBehaviour
	{
		public Transform m_Transform;

		private BattleEnum.RoleAnimType _animType;
		private GameObject		_gameObject;
		private BattleHeroSp	_roleSp;
		private SpriteRenderer	_spRenderer;
		private Sprite[]		_sp;
		private BoxCollider2D	_boxCollider;
		private Rigidbody2D		_rigidbody;
		private Vector3			_vecRun;
		private Vector3			_vecSlip;
		
		private bool			_bMine;
		private BattleRobot		_robot;

		public int			PlayerId	{ get; set; }
		public bool			IsRobot		{ get; set; }
		public int			HeroId		{ get; set; }
		public float		RunSpeed	{ get; set; }
		public float		SlipSpeed	{ get; set; }
		public BattlePlat	CurPlat		{ get; set; }

		private void Awake()
		{
			m_Transform		= transform;
			_gameObject		= gameObject;
			_boxCollider	= _gameObject.GetComponent<BoxCollider2D>();
			_rigidbody		= _gameObject.GetComponent<Rigidbody2D>();
			_robot			= _gameObject.AddComponent<BattleRobot>();

			RunSpeed		= 0.04f;
			SlipSpeed		= 0.02f;
			_vecRun			= Vector3.right * RunSpeed;
			_vecSlip		= Vector3.right * SlipSpeed;

			_boxCollider.enabled = false;
			_rigidbody.simulated = false;
			_animType = BattleEnum.RoleAnimType.Idle;
		}

		public void Init(int playerId, bool bRobot, int heroId)
		{
			PlayerId = playerId;
			IsRobot = bRobot;
			SetHeroId(heroId);
			_bMine = PlayerId == PlayerData.PlayerId;
		}

		private void SetHeroId(int id)
		{
			HeroId = id;
			_roleSp = ResourceLoader.LoadAssetAndInstantiate(string.Format("Prefab/Hero/{0}", id), m_Transform).GetComponent<BattleHeroSp>();
			_spRenderer = _roleSp.SpRenderer;
			_sp = _roleSp.Sp;
		}

		public void BattleStart()
		{
			if(_bMine || IsRobot)
			{
				_boxCollider.enabled = true;
				_rigidbody.simulated = true;
			}
		}

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

		private int _frame = -1;
		private int _lastX = 0, _lastY = 0;
		private int _roleX = 0, _roleY = 0;
		private float _t = 0f;
		
		private void OnUpdate()
		{
			_t += Time.deltaTime;
			if(_t > 0.025f)
			{
				if(BattleManager.GetInst().BattleType == 1)
					BattleManager.GetInst().SetFieldPos(++_frame);
				else
				{
					if(_bMine)
					{
						_roleX = (int)(m_Transform.localPosition.x * 1000);
						_roleY = (int)(m_Transform.localPosition.y * 1000);
					}
					else if(IsRobot)
						_robot.RobotControl(this);

					if(_bMine)
					{
						if(_roleX != _lastX || _roleY != _lastY)
						{
							_lastX = _roleX;
							_lastY = _roleY;
							ByteBuffer buff = new ByteBuffer(12);
							buff.writeInt(PlayerData.PlayerId);
							buff.writeInt(_roleX);
							buff.writeInt(_roleY);
							SocketHandler.GetInst().UdpSend(buff);
						}
					}
				}
				_t -= 0.025f;
			}
		}

		public void Disable()
		{
			_rigidbody.simulated = false;
			enabled = false;
		}

		float _runT = 0;
		public void RunLeft()
		{
			_runT += Time.deltaTime;
			if(_runT > 0.025f)
			{
				_spRenderer.flipX = false;
				_animType = BattleEnum.RoleAnimType.RunLeft;
				m_Transform.localPosition -= _vecRun * _runT / 0.025f;
				_runT = 0;
			}	
		}

		public void RunRight()
		{
			_runT += Time.deltaTime;
			if(_runT > 0.025f)
			{
				_spRenderer.flipX = true;
				_animType = BattleEnum.RoleAnimType.RunRight;
				m_Transform.localPosition += _vecRun * _runT / 0.025f;
				_runT = 0;
			}
		}

		public void Idle()
		{
			_spRenderer.flipX = false;
			_animType = BattleEnum.RoleAnimType.Idle;
		}

		float _moveT = 0;
		public void MoveLeft()
		{
			_moveT += Time.deltaTime;
			if(_moveT > 0.025f)
			{
				m_Transform.localPosition -= _vecSlip * _moveT / 0.025f;
				_moveT = 0;
			}
		}

		public void MoveRight()
		{
			_moveT += Time.deltaTime;
			if(_moveT > 0.025f)
			{
				m_Transform.localPosition += _vecSlip * _moveT / 0.025f;
				_moveT = 0;
			}
		}

		private void OnCollisionEnter2D(Collision2D collision)
		{
			CheckEnter(collision.collider);
		}

		private void OnCollisionStay2D(Collision2D collision)
		{
			CheckStay(collision.collider);
		}

		private void CheckEnter(Collider2D collider)
		{
			if(collider.tag == "Plat")
			{
				CurPlat = collider.GetComponent<BattlePlat>();
				switch(CurPlat.Type)
				{
					case 3:
						RemovePlat(collider);
						break;
					case 4:
						_rigidbody.AddForce(Vector2.up * 200, ForceMode2D.Force);
						break;
					case 5:
						ReduceHp(1);
						break;
				}
			}
		}

		private void CheckStay(Collider2D collider)
		{
			if(collider.tag == "Plat")
			{
				BattlePlat plat = collider.GetComponent<BattlePlat>();
				switch(plat.Type)
				{
					case 1:
						MoveLeft();
						break;
					case 2:
						MoveRight();
						break;
				}
			}
		}

		public void ReduceHp(int reduceValue)
		{
			int curHp = BattleManager.GetInst().GetHp(PlayerId);
			int newHp = Mathf.Max(0, curHp - reduceValue);
			if(BattleManager.GetInst().BattleType == 1 || IsRobot)
				BattleManager.GetInst().SyncHp(PlayerId, newHp);
			else	
				CommonCommand.ExecuteLongBattle(Client2ServerList.GetInst().C2S_BATTLE_SYNC_HP, new ArrayList(){ (byte)newHp });
		}

		private void RemovePlat(Collider2D collider)
		{
			StartCoroutine(_RemovePlat(collider));
		}

		WaitForSeconds wait = new WaitForSeconds(1f);
		private IEnumerator _RemovePlat(Collider2D collider)
		{
			yield return wait;
			BattleManager.GetInst().RemovePlat(PlayerId, collider.GetComponent<BattlePlat>());
		}
	}
}
