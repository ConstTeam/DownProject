package app;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codec.Desencryption;
import config.ConfigData;
import config.ConfigLoader;
import config.ResourceListener;
import db.ServerDao;
import memory.SessionMemory;
import net.ISession;
import redis.RedisProxy;
import redis.subscribe.SingleThreadManager;
import sys.GameRoomManager;
import sys.GameServerOnlineManager;
import sys.GameTimer;
import sys.ServerLogTimer;
import sys.UDPMsgManager;
import util.ErrorPrint;

/**
 * ��¼������
 * 
 */
public class GameServer {

	private static final Logger logger = LoggerFactory.getLogger(GameServer.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PropertyConfigurator.configure(System.getProperty("etcPath") + "log4jGame.properties");

		try {
			ServerStaticInfo.setServerType("Game");
			
			ErrorPrint.sysLogInitialized(ServerStaticInfo.getServerType());

			// ������������
			ServerStaticInfo.loadParam();
			// ����ƽ̨����
			ServerStaticInfo.loadPlatfrom();
			ServerStaticInfo.loadVersion();

			// ��ʼ��Dc������
			ServerManager.getInstance().init();
			ServerManager.getInstance().dbInit();
			ServerManager.getInstance().dbLogInit();

			// ���ط����Ի�����
			ServerStaticInfo.loadLocalServer();
			
			// ����Redis
			ServerManager.getInstance().redisInit();

			ServerStaticInfo.loadConfigPath();
			logger.info("׼���������ñ�{}", ConfigData.getConfigPath());

			// ����������ʱ��
			ServerStaticInfo.startServerTime = ServerDao.init();
			if (ServerStaticInfo.startServerTime == null) {
				logger.error("����ʱ���д�������������ʧ�ܣ�");
				System.exit(1);
			}

			// ���������ļ�
			ConfigLoader.setServerInfo(ServerStaticInfo.startServerTime, ServerStaticInfo.getPlatform());
			boolean load = ConfigLoader.load();
			if (!load) {
				logger.error("######### ���ñ��ⲻͨ�����������޷�������##########");
				System.exit(1);
			} else {
				logger.info("*****************  ���ñ���ɹ���   ***************");
			}
			ResourceListener.addListener(ConfigData.getConfigPath());

			if (ConfigData.arguments.get("SOCKET_ENCRYPT_LEN") != null) {
				Desencryption.ENCODE_LEN = ConfigData.arguments.get("SOCKET_ENCRYPT_LEN");
			}

			/*
			 * ��Ϸ������ע�ᣬ10������
			 */
			if (!RedisProxy.getInstance().serverInfoRegister()) {
				logger.error("��Ϸ������ע��ʧ�ܣ��������޷�������");
				System.exit(1);
			}
			GameTimer.getScheduled().scheduleAtFixedRate(() -> RedisProxy.getInstance().updateServerStatus(), 0, 1, TimeUnit.SECONDS);
			RedisProxy.getInstance().destroyAllRoom();
			
			// ��������˿ڼ���
			ServerManager.getInstance().startManageServer(ServerStaticInfo.GAME_MANAGE_PORT);

			// �߳�ִ�б��Ϊ����
			ServerStaticInfo.opened = true;
			ServerLogTimer.setTimer();
			UDPMsgManager.start();
			
			// ��Ϸ���䶩����Ϣ
			SingleThreadManager.getInstance().execute(() -> RedisProxy.getInstance().roomNoticeRegister());
			
			// �ط�����
			Runtime.getRuntime().addShutdownHook(new Thread(() -> stop()));

			// ������Ҽ���һ���������������������߼������
			ServerManager.getInstance().startGameClientServer();
			
			logger.info("GameServer start success. {} - {} - {}:{}", ServerStaticInfo.getServerId(),
					ServerStaticInfo.startServerTime, ServerManager.serverAddr.getHostAddress().toString(),
					ServerStaticInfo.getAddress(ServerStaticInfo.GAME_INTERNET).getPort());
			if (ServerStaticInfo.IS_DEBUG) {
				logger.info("��Ϸ������ �����ɹ��� {} - �״ο���ʱ�䣺{} - {}:{}", ServerStaticInfo.getServerId(),
						ServerStaticInfo.startServerTime, ServerManager.serverAddr.getHostAddress().toString(),
						ServerStaticInfo.getAddress(ServerStaticInfo.GAME_INTERNET).getPort());
				System.in.read();
				System.exit(0);
			}
		} catch (Exception e) {
			ErrorPrint.print(e);
			logger.info("GameServer start failed.");
			System.exit(1);
		}
	}

	public static void stop() {
		GameTimer.getScheduled().shutdown();
		ServerManager.closeAll();
		RedisProxy.getInstance().serverInfoSuspend();
		boolean save = RedisProxy.getInstance().save();
		logger.info("�ط����̣�Redis���ݱ���{}��", save ? "�ɹ�" : "ʧ��");

		try {
			// ҵ���߳�ִ�б�־Ϊ�ر�
			ServerStaticInfo.opened = false;

			GameRoomManager.getInstance().destroyAllRoom();
			
			Collection<ISession> sessions = SessionMemory.getInstance().getSessions();
			for (ISession session : sessions) {
				if (!session.isClosed()) {
					GameServerOnlineManager.getInstance().playerLogout(session);
				}
			}
		} catch (Exception e) {
			ErrorPrint.print(e);
		}

		// �ط��߼�ȫ��ִ����ϣ��ȴ�3��ҵ���̴߳�����������ݺ�ر��̡߳�
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			ErrorPrint.print(e);
		}
		GameTimer.getScheduled().shutdownNow();
	}
}
