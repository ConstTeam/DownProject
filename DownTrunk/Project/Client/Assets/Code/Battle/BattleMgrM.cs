using System.Collections;
using UnityEngine;

namespace MS
{
	public class BattleMgrM : MonoBehaviour
	{
		public BattleFieldM m_BattleFiledM;
		public RoleM		m_RoleM;

		private static BattleMgrM _inst;
		public static BattleMgrM GetInst()
		{
			return _inst;
		}

		private void Awake()
		{
			_inst = this;
		}

		private void OnDestroy()
		{
			_inst = null;
		}

		public void LoadBattleFiled()
		{
			m_BattleFiledM.Load();
			m_BattleFiledM.IsRunning = true;
		}

		private float _t = 0f;
		private void Update()
		{
			_t += Time.deltaTime;
			if(_t > 0.03f)
			{
				int roleX	= (int)(m_RoleM.m_Transform.localPosition.x * 1000);
				int roleY	= (int)(m_RoleM.m_Transform.localPosition.y * 1000);
				int filedY	= (int)(m_BattleFiledM.m_Transform.localPosition.y * 1000);
				ByteBuffer buff = new ByteBuffer(4);
				buff.writeInt(PlayerData.PlayerId);
				buff.writeInt(roleX);
				buff.writeInt(roleY);
				buff.writeInt(filedY);
				SocketHandler.GetInst().UdpSend(buff);
				_t = 0f;
			}
		}

		public void MoveLeft()
		{
			m_RoleM.MoveLeft();
		}

		public void MoveRight()
		{
			m_RoleM.MoveRight();
		}
	}
}
