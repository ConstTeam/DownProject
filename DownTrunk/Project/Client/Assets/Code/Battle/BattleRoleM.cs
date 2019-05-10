using System.Collections;
using UnityEngine;

namespace MS
{
	public class BattleRoleM : BattleRoleBase
	{
		public bool		IsRunning	{ get; set; }
		public float	RunSpeed	{ get; set; }
		public float	SlipSpeed	{ get; set; }

		private Rigidbody2D	_rigidbody;
		private Vector3		_vecRunLeft;
		private Vector3		_vecRunRight;
		private Vector3		_vecSlipLeft;
		private Vector3		_vecSlipRight;

		protected override void OnAwake()
		{
			_rigidbody		= gameObject.GetComponent<Rigidbody2D>();
			IsRunning		= false;
			RunSpeed		= 0.04f;
			SlipSpeed		= 0.02f;
			_vecRunLeft		= Vector3.left * RunSpeed;
			_vecRunRight	= Vector3.right * RunSpeed;
			_vecSlipLeft	= Vector3.left * SlipSpeed;
			_vecSlipRight	= Vector3.right * SlipSpeed;
		}

		public void RunLeft()
		{
			_spRenderer.flipX = false;
			_animType = BattleEnum.RoleAnimType.RunLeft;
			m_Transform.localPosition += _vecRunLeft;
		}

		public void RunRight()
		{
			_spRenderer.flipX = true;
			_animType = BattleEnum.RoleAnimType.RunRight;
			m_Transform.localPosition += _vecRunRight;
		}

		public void Idle()
		{
			_spRenderer.flipX = false;
			_animType = BattleEnum.RoleAnimType.Idle;
		}

		public void MoveLeft()
		{
			m_Transform.localPosition += _vecSlipLeft;
		}

		public void MoveRight()
		{
			m_Transform.localPosition += _vecSlipRight;
		}

		private int _lastX = 0, _lastY = 0;
		private int _roleX = 0, _roleY = 0;
		private float _t = 0f;
		private RaycastHit2D _hit;
		private Vector3 _vecOffset = Vector3.left * 0.16f;
		protected override void OnUpdate()
		{
			//Debug.DrawLine(m_Transform.position + _vecOffset, m_Transform.position + _vecOffset + Vector3.down * 0.2f, Color.red);
			_hit = Physics2D.Raycast(m_Transform.position + _vecOffset, Vector3.down, 0.2f, 1 << LayerMask.NameToLayer("Plat"));
			if(!CheckHit())
			{
				//Debug.DrawLine(m_Transform.position - _vecOffset, m_Transform.position - _vecOffset + Vector3.down * 0.2f, Color.red);
				_hit = Physics2D.Raycast(m_Transform.position - _vecOffset, Vector3.down, 0.2f, 1 << LayerMask.NameToLayer("Plat"));
				CheckHit();
			}

			if(IsRunning)
			{
				_t += Time.deltaTime;
				if (_t > 0.025f)
				{
					_roleX = (int)(m_Transform.localPosition.x * 1000);
					_roleY = (int)(m_Transform.localPosition.y * 1000);
					if (_roleX != _lastX || _roleY != _lastY)
					{
						_lastX = _roleX;
						_lastY = _roleY;
						ByteBuffer buff = new ByteBuffer(4);
						buff.writeInt(BattleManager.GetInst().RoomId);
						buff.writeInt(PlayerData.PlayerId);
						buff.writeInt(_roleX);
						buff.writeInt(_roleY);
						SocketHandler.GetInst().UdpSend(buff);
					}
					_t = 0f;
				}
			}
		}

		private bool CheckHit()
		{
			if(_hit)
			{
				Collider2D collider = _hit.collider;	
				if(collider.CompareTag("Plat1"))
				{
					MoveLeft();
					return true;
				}
				else if(collider.CompareTag("Plat2"))
				{
					MoveRight();
					return true;
				}
				else if(collider.CompareTag("Plat3"))
				{
					RemovePlat(collider);
					return true;
				}
				else if(collider.CompareTag("Plat4"))
				{
					_rigidbody.AddForce(Vector2.up * 100, ForceMode2D.Force);
					return true;
				}
				else if(collider.CompareTag("Plat5"))
				{
					collider.tag = "Untagged";
					ReduceHp(1);
					return true;
				}
			}
			return false;
		}

		public void ReduceHp(int reduceValue)
		{
			int curHp = BattleManager.GetInst().GetHp(PlayerData.PlayerId);
			int newHp = Mathf.Max(0, curHp - reduceValue);
			BattleManager.GetInst().SyncHp(PlayerData.PlayerId, newHp);
			CommonCommand.ExecuteLongBattle(Client2ServerList.GetInst().C2S_BATTLE_SYNC_HP, new ArrayList() { (byte)newHp });
		}

		private void RemovePlat(Collider2D collider)
		{
			StartCoroutine(_RemovePlat(collider));
		}

		WaitForSeconds wait = new WaitForSeconds(1f);
		private IEnumerator _RemovePlat(Collider2D collider)
		{
			yield return wait;
			BattleManager.GetInst().RemovePlat(collider.GetComponent<PlatBase>());
		}
	}
}
