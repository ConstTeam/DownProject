using UnityEngine;

namespace MS
{
	public class BattleRoleM : BattleRoleBase
	{
		public bool		IsRunning { get; set; }
		public float	Speed { get; set; }

		protected override void OnAwake()
		{
			IsRunning = false;
			Speed = 0.04f;
		}

		public void MoveLeft()
		{
			m_Transform.localPosition += Vector3.left * Speed;
		}

		public void MoveRight()
		{
			m_Transform.localPosition += Vector3.right * Speed;
		}

		private int _lastX = 0, _lastY = 0;
		private int _roleX = 0, _roleY = 0;
		private float _t = 0f;
		private void Update()
		{
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
	}
}
