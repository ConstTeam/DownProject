package db;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.ConfigData;
import config.model.quest.AwardModel;

/**
 * 
 */
public class AwardDao {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(AwardDao.class);

	public static int addAward(Connection con, int playerId, String awardId) throws SQLException {
		AwardModel awardModel = ConfigData.awardModels.get(awardId);
		if (awardModel == null) {
			return -1;
		}
		if (awardModel.GoldCount > 0) {
			PlayerDao.addGold(con, playerId, awardModel.GoldCount);
			return awardModel.GoldCount;
		}
		return -1;
	}
}