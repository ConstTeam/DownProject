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
 * 登录服务器
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

			// 加载启服类型
			ServerStaticInfo.loadParam();
			// 加载平台类型
			ServerStaticInfo.loadPlatfrom();
			ServerStaticInfo.loadVersion();

			// 初始化Dc服务器
			ServerManager.getInstance().init();
			ServerManager.getInstance().dbInit();
			ServerManager.getInstance().dbLogInit();

			// 本地服个性化配置
			ServerStaticInfo.loadLocalServer();
			
			// 加载Redis
			ServerManager.getInstance().redisInit();

			ServerStaticInfo.loadConfigPath();
			logger.info("准备载入配置表：{}", ConfigData.getConfigPath());

			// 服务器开服时间
			ServerStaticInfo.startServerTime = ServerDao.init();
			if (ServerStaticInfo.startServerTime == null) {
				logger.error("开服时间有错误！启动服务器失败！");
				System.exit(1);
			}

			// 加载配置文件
			ConfigLoader.setServerInfo(ServerStaticInfo.startServerTime, ServerStaticInfo.getPlatform());
			boolean load = ConfigLoader.load();
			if (!load) {
				logger.error("######### 配置表检测不通过！服务器无法启动！##########");
				System.exit(1);
			} else {
				logger.info("*****************  配置表检测成功！   ***************");
			}
			ResourceListener.addListener(ConfigData.getConfigPath());

			if (ConfigData.arguments.get("SOCKET_ENCRYPT_LEN") != null) {
				Desencryption.ENCODE_LEN = ConfigData.arguments.get("SOCKET_ENCRYPT_LEN");
			}

			/*
			 * 游戏服启服注册，10秒心跳
			 */
			if (!RedisProxy.getInstance().serverInfoRegister()) {
				logger.error("游戏服启服注册失败！服务器无法启动！");
				System.exit(1);
			}
			GameTimer.getScheduled().scheduleAtFixedRate(() -> RedisProxy.getInstance().updateServerStatus(), 0, 1, TimeUnit.SECONDS);
			RedisProxy.getInstance().destroyAllRoom();
			
			// 启动管理端口监听
			ServerManager.getInstance().startManageServer(ServerStaticInfo.GAME_MANAGE_PORT);

			// 线程执行标记为开启
			ServerStaticInfo.opened = true;
			ServerLogTimer.setTimer();
			UDPMsgManager.start();
			
			// 游戏房间订阅消息
			SingleThreadManager.getInstance().execute(() -> RedisProxy.getInstance().roomNoticeRegister());
			
			// 关服钩子
			Runtime.getRuntime().addShutdownHook(new Thread(() -> stop()));

			// 启动玩家监听一定放在所有启动服务器逻辑的最后！
			ServerManager.getInstance().startGameClientServer();
			
			logger.info("GameServer start success. {} - {} - {}:{}", ServerStaticInfo.getServerId(),
					ServerStaticInfo.startServerTime, ServerManager.serverAddr.getHostAddress().toString(),
					ServerStaticInfo.getAddress(ServerStaticInfo.GAME_INTERNET).getPort());
			if (ServerStaticInfo.IS_DEBUG) {
				logger.info("游戏服务器 启动成功。 {} - 首次开服时间：{} - {}:{}", ServerStaticInfo.getServerId(),
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
		logger.info("关服存盘，Redis数据保存{}。", save ? "成功" : "失败");

		try {
			// 业务线程执行标志为关闭
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

		// 关服逻辑全部执行完毕，等待3秒业务线程处理完最后数据后关闭线程。
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			ErrorPrint.print(e);
		}
		GameTimer.getScheduled().shutdownNow();
	}
}
