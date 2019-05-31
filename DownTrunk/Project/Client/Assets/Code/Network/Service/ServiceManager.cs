using System.Collections.Generic;
using UnityEngine;

namespace MS
{
	public struct ServerData
	{
		public ConnectBase conn;
		public ByteBuffer buff;
		public bool bShort;

		public ServerData(ConnectBase c, ByteBuffer b, bool bs)
		{
			conn	= c;
			buff	= b;
			bShort	= bs;
		}
	}

	public class ServiceManager : MonoBehaviour
	{
		public const int MODULE_MAIN_THREAD_SERVICE		= 0;
		public const int MODULE_LOGIN_SERVICE			= 1;
		public const int MODULE_ROLE_SERVICE			= 2;

		public const int MODULE_BATTLE_LOGIN_SERVICE	= 101;
		public const int MODULE_BATTLE_SERVICE			= 102;
	
		public static Queue<ByteBuffer> m_queUdpData	= new Queue<ByteBuffer>();
		public static Queue<ServerData> m_lstData		= new Queue<ServerData>();
		private static Object m_LockerUdp;
		private static Object m_Locker;
		private static Dictionary<int, IService> m_ServiceDic = new Dictionary<int, IService>();

		private static ServiceManager m_Inst;

		public static ServiceManager GetInst()
		{
			return m_Inst;
		}

		void OnDestroy()
		{
			m_Inst = null;
		}

		void Awake()
		{
			m_Locker = new Object();
			m_LockerUdp = new Object();
			m_Inst = (ServiceManager)(MonoBehaviour)this;

			m_ServiceDic.Add(MODULE_MAIN_THREAD_SERVICE,	new MainThreadService());
			m_ServiceDic.Add(MODULE_LOGIN_SERVICE,			new LoginService());
			m_ServiceDic.Add(MODULE_ROLE_SERVICE,			new PlayerService());

			m_ServiceDic.Add(MODULE_BATTLE_LOGIN_SERVICE,	new BattleLoginService());
			m_ServiceDic.Add(MODULE_BATTLE_SERVICE,			new BattleService());
		}

		public static IService GetService(int id)
		{	
			return m_ServiceDic[id];
		}

		public static void MessageArrived(ConnectBase conn, ByteBuffer data)
		{
			int bigId = data.readByte();
			IService service = GetService(bigId);
			service.ProcessMessage(conn, data);
		}

		void Update()
		{
			lock(m_Locker)
			{
				if(m_lstData.Count > 0)
				{
					ServerData temp = m_lstData.Dequeue();
					MessageArrived(temp.conn, temp.buff);
					if(temp.bShort && null != SocketHandler.ShortSendBackFun)
						SocketHandler.ShortSendBackFun();
				}
			}

			lock(m_LockerUdp)
			{
				if(m_queUdpData.Count > 0)
				{
					ByteBuffer data = m_queUdpData.Dequeue();
					int frame = data.readInt();
					int size = data.readByte();
					for(int i = 0; i < size; ++i)
					{
						int playerId = data.readInt();
						float roleX = data.readInt() / 1000f;
						float roleY = data.readInt() / 1000f;
						BattleManager.GetInst().SetRolePos(playerId, roleX, roleY);
					}
					BattleManager.GetInst().SetFieldPos(frame);
				}
			}
		}

		public static void PostMessageShort(ConnectBase conn, ByteBuffer buff)
		{
			lock(m_Locker)
			{
				SocketHandler.GetInst().ShortClose((ConnectShort)conn);
				ServerData data = new ServerData(conn, buff, true);
				m_lstData.Enqueue(data);
			}
		}

		public static void PostMessageLong(ConnectBase conn, ByteBuffer buff)
		{
			lock(m_Locker)
			{
				ServerData data = new ServerData(conn, buff, false);
				m_lstData.Enqueue(data);
			}
		}

		public static void PostMessageUdp(ByteBuffer buff)
		{
			lock(m_LockerUdp)
			{
				m_queUdpData.Enqueue(buff);
			}
		}
	}
}
