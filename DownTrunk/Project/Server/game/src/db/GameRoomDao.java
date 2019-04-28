package db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.ServerManager;
import module.fight.BattleRole;
import util.ErrorPrint;

/**
 * 
 */
public class GameRoomDao {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(GameRoomDao.class);

	public static ArrayList<Object> settlement(boolean isWin, BattleRole role, BattleRole enemy) {
		Connection connect = ServerManager.gameDBConnect.getDBConnect();
		if (connect == null) { // 数据库连接池已满
			return null;
		}
		
		try {
			ArrayList<Object> result = new ArrayList<>();
			
			connect.setAutoCommit(false);
			
			int playerGold = PlayerDao.getPlayerGold(connect, role.getPlayerId());
			result.add(playerGold);
			
			return result;
		} catch (Exception e) {
			try {
				connect.rollback();
			} catch (SQLException e2) {
				ErrorPrint.print(e2);
			}
			ErrorPrint.print(e);
		} finally {
			try {
				connect.setAutoCommit(true);
			} catch (SQLException e2) {
				ErrorPrint.print(e2);
			}
			ServerManager.gameDBConnect.closeConnect(connect);
		}
		return null;
	}

}