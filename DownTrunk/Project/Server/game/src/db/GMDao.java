package db;

import java.sql.Connection;

import app.ServerManager;
import util.ErrorPrint;

public class GMDao {
	
	public static int addGold(int playerId, int gold) {
		Connection connect = ServerManager.gameDBConnect.getDBConnect();
		if (connect == null) { // 数据库连接池已满
			return -1;
		}
		try {
			return PlayerDao.addGold(connect, playerId, gold);
		} catch (Exception e) {
			ErrorPrint.print(e);
		} finally {
			ServerManager.gameDBConnect.closeConnect(connect);
		}
		return -1;
	}
}
