package db;

import java.sql.Connection;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class MultiConnectPool {
	
	private static final Logger logger = LoggerFactory.getLogger(MultiConnectPool.class);
	
	private static final String GAME_START_STR = "db_";
	private static final String LOG_START_STR = "dblog_";
	private static final String END_STR = ".cfg";
	
	private static HashMap<String, ConnectionPool> gameConnectPools = new HashMap<>();
	
	private static HashMap<String, ConnectionPool> logConnectPools = new HashMap<>();
	
	public static Connection getGameDBConnect(String serverId) {
		
		if (gameConnectPools.get(serverId) == null) {
			String dbConfigPath = System.getProperty("etcPath") + GAME_START_STR + serverId + END_STR;
			ConnectionPool connectPool = new ConnectionPool();
			try {
				connectPool.initialize(dbConfigPath);
				gameConnectPools.put(serverId, connectPool);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("数据库配置文件【"+dbConfigPath+"】！填写有误！");
				return null;
			}
		}
		
		return gameConnectPools.get(serverId).getConnection();
	}
	
	public static Connection getLogDBConnect(String serverId) {
		
		if (logConnectPools.get(serverId) == null) {
			String dbConfigPath = System.getProperty("etcPath") + LOG_START_STR + serverId + END_STR;
			ConnectionPool connectPool = new ConnectionPool();
			try {
				connectPool.initialize(dbConfigPath);
				logConnectPools.put(serverId, connectPool);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("数据库配置文件【"+dbConfigPath+"】！填写有误！");
				return null;
			}
		}
		
		return logConnectPools.get(serverId).getConnection();
	}
	
	public static void closeLogConnect(String serverId, Connection connect) {
		if (logConnectPools.get(serverId) == null || connect == null) {
			return;
		}
		logConnectPools.get(serverId).close(connect);

	}
	
	public static void closeGameConnect(String serverId, Connection connect) {
		if (gameConnectPools.get(serverId) == null || connect == null) {
			return;
		}
		gameConnectPools.get(serverId).close(connect);
	}
}
