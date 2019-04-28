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
 * 登录服务器
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

			// 加载启服类型
			ServerStaticInfo.loadParam();
			// 加载平台类型
			ServerStaticInfo.loadPlatfrom();
			
			// 初始化Dc服务器
			ServerManager.getInstance().init();
			ServerManager.getInstance().dbInit();
			ServerManager.getInstance().dbLogInit();
			
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

			// 启动管理端口监听
			ServerManager.getInstance().startManageServer(ServerStaticInfo.DATA_MANAGE_PORT);

			// 线程执行标记为开启
			ServerStaticInfo.opened = true;
			
			// 充值处理线程
			PayManager.getInstance().init();
			PayManager.start();
			
			ServerManager.getInstance().startSDKServer();
			
			// 关服钩子
			Runtime.getRuntime().addShutdownHook(new Thread(() -> stop()));

			logger.info("CenterServer-{} start - {} - {}:{}", ServerStaticInfo.getServerId(),
					ServerStaticInfo.startServerTime, ServerManager.serverAddr.getHostAddress().toString(),
					ServerStaticInfo.getAddress(ServerStaticInfo.DATA_PORT).getPort());
			if (ServerStaticInfo.IS_DEBUG) {
				logger.info("数据中心服务器-{} 启动成功 - 首次开服时间：{} - {}:{}", ServerStaticInfo.getServerId(),
						ServerStaticInfo.startServerTime, ServerManager.serverAddr.getHostAddress().toString(),
						ServerStaticInfo.getAddress(ServerStaticInfo.DATA_PORT).getPort());
			}
		} catch (Exception e) {
			ErrorPrint.print(e);
			logger.error("启动数据中心服务器失败！");
			System.exit(1);
		}
	}

	public static void stop() {
		ServerManager.closeAll();
		boolean save = RedisProxy.getInstance().save();
		logger.info("关服存盘，Redis数据保存{}。", save ? "成功" : "失败");

		try {
			// 业务线程执行标志为关闭
			ServerStaticInfo.opened = false;
		} catch (Exception e) {
			ErrorPrint.print(e);
		}

		// 关服逻辑全部执行完毕，等待3秒业务线程处理完最后数据后关闭线程。
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			ErrorPrint.print(e);
		}
	}
}
