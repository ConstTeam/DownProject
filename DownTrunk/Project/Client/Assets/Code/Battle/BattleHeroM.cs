using System.Collections;
using UnityEngine;

namespace MS
{
	public class BattleHeroM : BattleHeroBase
	{
		public float	RunSpeed		{ get; set; }
		public float	SlipSpeed		{ get; set; }

		private BoxCollider _boxCollider;
		private Rigidbody2D	_rigidbody;
		private Vector3		_vecRun;
		private Vector3		_vecSlip;

		protected override void OnAwake()
		{
			_boxCollider	= gameObject.GetComponent<BoxCollider>();
			_rigidbody		= gameObject.GetComponent<Rigidbody2D>();
			RunSpeed		= 0.04f;
			SlipSpeed		= 0.02f;
			_vecRun			= Vector3.right * RunSpeed;
			_vecSlip		= Vector3.right * SlipSpeed;
		}

		public void Disable()
		{
			_rigidbody.simulated = false;
			enabled = false;
		}

		public void RunLeft()
		{
			_spRenderer.flipX = false;
			_animType = BattleEnum.RoleAnimType.RunLeft;
			m_Transform.localPosition -= _vecRun;
		}

		public void RunRight()
		{
			_spRenderer.flipX = true;
			_animType = BattleEnum.RoleAnimType.RunRight;
			m_Transform.localPosition += _vecRun;
		}

		public void Idle()
		{
			_spRenderer.flipX = false;
			_animType = BattleEnum.RoleAnimType.Idle;
		}

		public void MoveLeft()
		{
			m_Transform.localPosition -= _vecSlip;
		}

		public void MoveRight()
		{
			m_Transform.localPosition += _vecSlip;
		}

		private int _frame = -1;
		private int _lastX = 0, _lastY = 0;
		private int _roleX = 0, _roleY = 0;
		private float _t = 0f;
		protected override void OnUpdate()
		{
			_t += Time.deltaTime;
			if(_t > 0.025f)
			{
				if(BattleManager.GetInst().BattleType == 1)
					BattleManager.GetInst().SetFieldPos(++_frame);
				else
				{
					_roleX = (int)(m_Transform.localPosition.x * 1000);
					_roleY = (int)(m_Transform.localPosition.y * 1000);
					if (_roleX != _lastX || _roleY != _lastY)
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
				_t = 0f;
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
			switch(collider.tag)
			{
				case "Plat3":
					RemovePlat(collider);
					break;
				case "Plat4":
					_rigidbody.AddForce(Vector2.up * 200, ForceMode2D.Force);
					break;
				case "Plat5":
					ReduceHp(1);
					break;
			}
		}

		private void CheckStay(Collider2D collider)
		{
			switch(collider.tag)
			{
				case "Plat1":
					MoveLeft();
					break;
				case "Plat2":
					MoveRight();
					break;
			}
		}

		public void ReduceHp(int reduceValue)
		{
			int curHp = BattleManager.GetInst().GetHp(PlayerData.PlayerId);
			int newHp = Mathf.Max(0, curHp - reduceValue);
			BattleManager.GetInst().SyncHp(PlayerData.PlayerId, newHp);
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
