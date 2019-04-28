package app;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codec.Desencryption;
import config.ConfigData;
import config.ConfigLoader;
import config.ResourceListener;
import db.ServerDao;
import redis.RedisProxy;
import sys.PayManager;
import util.ErrorPrint;

/**
 * ��¼������
 * 
 */
public class CenterServer {

	private static final Logger logger = LoggerFactory.getLogger(CenterServer.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PropertyConfigurator.configure(System.getProperty("etcPath") + "log4jCenter.properties");

		try {
			ServerStaticInfo.setServerType("Center");
			
			ErrorPrint.sysLogInitialized(ServerStaticInfo.getServerType());

			// ������������
			ServerStaticInfo.loadParam();
			// ����ƽ̨����
			ServerStaticInfo.loadPlatfrom();
			
			// ��ʼ��Dc������
			ServerManager.getInstance().init();
			ServerManager.getInstance().dbInit();
			ServerManager.getInstance().dbLogInit();
			
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

			// ��������˿ڼ���
			ServerManager.getInstance().startManageServer(ServerStaticInfo.DATA_MANAGE_PORT);

			// �߳�ִ�б��Ϊ����
			ServerStaticInfo.opened = true;
			
			// ��ֵ�����߳�
			PayManager.getInstance().init();
			PayManager.start();
			
			ServerManager.getInstance().startSDKServer();
			
			// �ط�����
			Runtime.getRuntime().addShutdownHook(new Thread(() -> stop()));

			logger.info("CenterServer-{} start - {} - {}:{}", ServerStaticInfo.getServerId(),
					ServerStaticInfo.startServerTime, ServerManager.serverAddr.getHostAddress().toString(),
					ServerStaticInfo.getAddress(ServerStaticInfo.DATA_PORT).getPort());
			if (ServerStaticInfo.IS_DEBUG) {
				logger.info("�������ķ�����-{} �����ɹ� - �״ο���ʱ�䣺{} - {}:{}", ServerStaticInfo.getServerId(),
						ServerStaticInfo.startServerTime, ServerManager.serverAddr.getHostAddress().toString(),
						ServerStaticInfo.getAddress(ServerStaticInfo.DATA_PORT).getPort());
			}
		} catch (Exception e) {
			ErrorPrint.print(e);
			logger.error("�����������ķ�����ʧ�ܣ�");
			System.exit(1);
		}
	}

	public static void stop() {
		ServerManager.closeAll();
		boolean save = RedisProxy.getInstance().save();
		logger.info("�ط����̣�Redis���ݱ���{}��", save ? "�ɹ�" : "ʧ��");

		try {
			// ҵ���߳�ִ�б�־Ϊ�ر�
			ServerStaticInfo.opened = false;
		} catch (Exception e) {
			ErrorPrint.print(e);
		}

		// �ط��߼�ȫ��ִ����ϣ��ȴ�3��ҵ���̴߳�����������ݺ�ر��̡߳�
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			ErrorPrint.print(e);
		}
	}
}
