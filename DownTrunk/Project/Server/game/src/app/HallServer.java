package app;

import java.text.ParseException;
import java.util.Calendar;
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
import redis.data.IFunctionSwitchConst;
import redis.data.ServerOpen;
import redis.subscribe.SingleThreadManager;
import sys.GameRoomAssign;
import sys.GameTimer;
import sys.HallServerOnlineManager;
import sys.ServerLogTimer;
import util.ErrorPrint;
import util.TimeFormat;
import util.Tools;

/**
 * ��¼������
 * 
 */
public class HallServer {

	private static final Logger logger = LoggerFactory.getLogger(HallServer.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PropertyConfigurator.configure(System.getProperty("etcPath") + "log4j.properties");

		try {
			ServerStaticInfo.setServerType("Hall");
			
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

			/*
			 * ������ÿ1���ӻ�ȡһ����Ϸ����Ϣ
			 */
			GameTimer.getScheduled().scheduleWithFixedDelay(() -> RedisProxy.getInstance().updateServerListCache(), 0, 1, TimeUnit.MINUTES);
			
			// ��������˿ڼ���
			ServerManager.getInstance().startManageServer(ServerStaticInfo.HALL_MANAGE_PORT);

			// �߳�ִ�б��Ϊ����
			ServerStaticInfo.opened = true;
			ServerLogTimer.setTimer();
			GameRoomAssign.start();
			
			// ˢ�¶�����Ϣ
			SingleThreadManager.getInstance().execute(() -> RedisProxy.getInstance().hallNoticeRegister());

			// �ط�����
			Runtime.getRuntime().addShutdownHook(new Thread(() -> stop()));
			
			// ������Ҽ���һ���������������������߼������
			ServerManager.getInstance().startClientServer();
			
			RedisProxy.getInstance().setFunctionSwitch(IFunctionSwitchConst.GM, true);
			
			logger.info("HallServer start success. {} - {} - {}:{}", ServerStaticInfo.getServerId(),
					ServerStaticInfo.startServerTime, ServerManager.serverAddr.getHostAddress().toString(),
					ServerStaticInfo.getAddress(ServerStaticInfo.HALL_INTERNET).getPort());
			if (ServerStaticInfo.IS_DEBUG) {
				logger.info("���������� �����ɹ��� {} - �״ο���ʱ�䣺{} - {}:{}", ServerStaticInfo.getServerId(),
						ServerStaticInfo.startServerTime, ServerManager.serverAddr.getHostAddress().toString(),
						ServerStaticInfo.getAddress(ServerStaticInfo.HALL_INTERNET).getPort());
				System.in.read();
				System.exit(0);
			}
		} catch (Exception e) {
			ErrorPrint.print(e);
			logger.info("HallServer start failed.");
			System.exit(1);
		}
	}
	
	public static void stop() {
		GameTimer.getScheduled().shutdown();
		ServerManager.closeAll();
		boolean save = RedisProxy.getInstance().save();
		logger.info("�������ط����̣�Redis���ݱ���{}��", save ? "�ɹ�" : "ʧ��");
		
		
		try {
			// ҵ���߳�ִ�б�־Ϊ�ر�
			ServerStaticInfo.opened = false;
			
			Collection<ISession> sessions = SessionMemory.getInstance().getSessions();
			
			ServerOpen open = RedisProxy.getInstance().getServerOpenMessage();
			if (open != null && !Tools.isEmptyString(open.getOpenTime()) && !Tools.isEmptyString(open.getMessage())) {
				try {
					Calendar calendar = TimeFormat.getTimeByStr(open.getOpenTime());
					if (calendar.after(Calendar.getInstance())) {
						if (Tools.isEmptyString(open.getExitMessage())) {
							// TODO ֪ͨ��ҹط�
						} else {
							// TODO ֪ͨ��ҹط�
						}
					}
				} catch (ParseException e) {
					ErrorPrint.print(e);
				}
			}
			
			for (ISession session : sessions) {
				if (!session.isClosed()) {
					HallServerOnlineManager.getInstance().playerLogout(session);
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
