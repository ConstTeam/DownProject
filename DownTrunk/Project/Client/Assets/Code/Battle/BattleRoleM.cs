using System.Collections;
using UnityEngine;

namespace MS
{
	public class BattleRoleM : BattleRoleBase
	{
		public bool		IsRunning	{ get; set; }
		public float	RunSpeed	{ get; set; }
		public float	MoveSpeed	{ get; set; }

		protected override void OnAwake()
		{
			IsRunning	= false;
			RunSpeed	= 0.04f;
			MoveSpeed	= 0.02f;
		}

		public void RunLeft()
		{
			m_Transform.localPosition += Vector3.left * RunSpeed;
		}

		public void RunRight()
		{
			m_Transform.localPosition += Vector3.right * RunSpeed;
		}

		public void MoveLeft()
		{
			m_Transform.localPosition += Vector3.left * MoveSpeed;
		}

		public void MoveRight()
		{
			m_Transform.localPosition += Vector3.right * MoveSpeed;
		}

		private int _lastX = 0, _lastY = 0;
		private int _roleX = 0, _roleY = 0;
		private float _t = 0f;
		private RaycastHit2D _hit;
		private Vector3 _vecOffset = Vector3.left * 0.1f;
		private void Update()
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
						buff.writeInt(RoleData.RoleID);
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
					RemovePlat(collider);
					return true;
				}	
				else if(collider.CompareTag("Plat2"))
				{
					MoveLeft();
					return true;
				}
				else if(collider.CompareTag("Plat3"))
				{
					MoveRight();
					return true;
				}
			}
			return false;
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
