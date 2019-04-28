package app;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codec.Desencryption;
import config.ConfigData;
import config.ConfigLoader;
import config.ResourceListener;
import db.ServerDao;
import sys.GameTimer;
import sys.ServerLogTimer;
import util.ErrorPrint;

/**
 * GM������
 * 
 */
public class GMServer {

	private static final Logger logger = LoggerFactory.getLogger(GMServer.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PropertyConfigurator.configure(System.getProperty("etcPath") + "log4jGM.properties");

		try {
			ServerStaticInfo.setServerType("GM");
			
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
			if (!ServerManager.getInstance().redisInit()) {
				logger.error("RedisPool��ʼ��ʧ�ܣ�����������ʧ�ܣ�");
				System.exit(1);
			}

			ServerStaticInfo.loadConfigPath();
			logger.info("׼���������ñ�{}", ConfigData.getConfigPath());

			// ����������ʱ�䣬��ɫid��ʼ��
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

			// ��������˿ڼ���
			ServerManager.getInstance().startManageServer(ServerStaticInfo.GM_MANAGE_PORT);

			// �߳�ִ�б��Ϊ����
			ServerStaticInfo.opened = true;
			ServerLogTimer.setTimer();
			// �ط�����
			Runtime.getRuntime().addShutdownHook(new Thread(() -> stop()));
			
			// ������Ҽ���һ���������������������߼������
			ServerManager.getInstance().startGMServer();
			
			logger.info("GMServer start success. {} - {} - {}:{}", ServerStaticInfo.getServerId(),
					ServerStaticInfo.startServerTime, ServerManager.serverAddr.getHostAddress().toString(),
					ServerStaticInfo.getAddress(ServerStaticInfo.GM_PORT).getPort());
			if (ServerStaticInfo.IS_DEBUG) {
				logger.info("GM������ �����ɹ��� {} - �״ο���ʱ�䣺{} - {}:{}", ServerStaticInfo.getServerId(),
						ServerStaticInfo.startServerTime, ServerManager.serverAddr.getHostAddress().toString(),
						ServerStaticInfo.getAddress(ServerStaticInfo.GM_PORT).getPort());
				System.in.read();
				System.exit(0);
			}
		} catch (Exception e) {
			ErrorPrint.print(e);
			logger.info("GMServer start failed.");
			System.exit(1);
		}
	}
	
	public static void stop() {
		GameTimer.getScheduled().shutdown();
		ServerManager.closeAll();
		// �ط��߼�ȫ��ִ����ϣ��ȴ�3��ҵ���̴߳�����������ݺ�ر��̡߳�
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			ErrorPrint.print(e);
		}
		GameTimer.getScheduled().shutdownNow();
	}
}
