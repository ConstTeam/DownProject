package message.hall.quest;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.ServerManager;
import config.ConfigData;
import config.model.quest.QuestModel;
import config.model.quest.SignInQuestModel;
import db.PlayerDao;
import db.QuestDao;
import db.SignInDao;
import db.UseCountDao;
import db.module.player.Player;
import message.hall.login.LoginService;
import message.hall.role.RoleMsgSend;
import module.UseCountConst;
import module.quest.Quest;
import module.quest.SignInQuest;
import net.DataAccessException;
import net.IByteBuffer;
import net.ISession;
import net.Servicelet;
import quest.QuestManager;
import sys.HallServerOnlineManager;
import util.ErrorPrint;

/**
 * ����ģ��
 * 
 */
public class QuestService extends Servicelet {

	private static final Logger logger = LoggerFactory.getLogger(QuestService.class);

	@Override
	public void access(ISession session, IByteBuffer data) throws DataAccessException {
		int type = data.readByte();
		String deviceId = null;
		int playerId = 0;

		switch (type) {
		case QuestMsgConst.QUEST_INFO:
			questInfo(session, deviceId, playerId);
			break;
			
		case QuestMsgConst.FLUSH_QUEST:
			int index = data.readByte();
			flushSingleQuest(session, deviceId, playerId, index);
			break;
			
		case QuestMsgConst.SIGNIN_INFO:
			signInQuestInfo(session, deviceId, playerId);
			break;
			
		case QuestMsgConst.RECEIVE_SIGNIN:
			receiveSignInQuest(session, deviceId, playerId);
			break;
			
		case QuestMsgConst.SIGNIN_BIND_PHONE_NUMBER:
			String phoneNumber = data.readUTF();
			setSignInPhoneNumber(session, deviceId, playerId, phoneNumber);
			break;
			
		}

	}
	
	private void questInfo(ISession session, String deviceId, int playerId) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session��player��Ϣ��ʧЧ��");
			return;
		}
		Player player = (Player)session.attachment();
		playerId = player.getPlayerId();
		HallServerOnlineManager.getInstance().getLock().lock(playerId);
		try {
			ArrayList<Quest> questInfos = QuestDao.flushQuest(playerId);
			if (questInfos == null) {
				logger.error("��ң�{}����ȡ������Ϣʧ�ܡ�", playerId);
			} else {
				QuestMsgSend.questInfoSync(session, questInfos);
				logger.info("��ң�{}����ȡ������Ϣ�ɹ���", playerId);
			}
		} finally {
			HallServerOnlineManager.getInstance().getLock().unlock(playerId);
		}	
	}
	
	private void flushSingleQuest(ISession session, String deviceId, int playerId, int index) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session��player��Ϣ��ʧЧ��");
			return;
		}
		Player player = (Player)session.attachment();
		playerId = player.getPlayerId();
		HallServerOnlineManager.getInstance().getLock().lock(playerId);
		try {
			if (index <= 0 || index > 4) {
				logger.error("��ң�{}��ˢ������ʧ�ܡ�Index��{}����", playerId, index);
				return;
			}
			int useCount = UseCountDao.getUseCountByType(playerId, UseCountConst.QUEST_FLUSH_COUNT);
			// TODO �����ñ��ȡ����
			int resCount = 1 - useCount;
			if (useCount >= 0 && resCount <= 0) {
				logger.error("��ң�{}��ˢ������ʧ�ܡ�ÿ�տ�ˢ��{}�Σ�������ˢ��{}�Ρ�", playerId, 1, useCount);
				return;
			}
			HashMap<Integer, Integer> questIds = new HashMap<>();
			HashMap<Integer, Integer> questTypes = new HashMap<>();
			ArrayList<Quest> questInfo = QuestDao.getQuestInfo(playerId, questIds);
			if (questInfo == null) {
				logger.error("��ң�{}��ˢ������ʧ�ܡ���ȡ������Ϣʧ�ܡ�", playerId);
				return;
			}
			for (Quest tempQuest : questInfo) {
				if (tempQuest.getIndex() == index) {
					continue;
				}
				QuestModel questModel = ConfigData.questModels.get(tempQuest.getQuestId());
				questTypes.put(questModel.Type, questModel.Type);
			}
			Quest oldQuest = questInfo.get(index - 1);
			if (oldQuest.getState() != 0) {
				logger.error("��ң�{}��ˢ��Index��{}������Index��{}������Id��{}��ˢ������ʧ�ܡ�����ɵ����񲻿�ˢ�¡�", playerId, index, oldQuest.getIndex(), oldQuest.getQuestId());
				return;
			}
			QuestModel questModel = QuestManager.getRandomQuest(index, questIds, questTypes);
			Quest quest = new Quest();
			quest.setPlayerId(playerId);
			quest.setIndex(index);
			quest.setQuestId(questModel.ID);
			boolean result = QuestDao.flushSingleQuest(playerId, quest);
			if (result) {
				QuestMsgSend.questInfoSingleSync(session, quest);
				RoleMsgSend.syncRoleResCount(session, UseCountConst.QUEST_FLUSH_COUNT, resCount - 1);
				logger.info("��ң�{}��ˢ������ɹ���", playerId);
			} else {
				logger.error("��ң�{}��ˢ������ʧ�ܡ�", playerId);
			}
		} finally {
			HallServerOnlineManager.getInstance().getLock().unlock(playerId);
		}	
	}
	
	private void signInQuestInfo(ISession session, String deviceId, int playerId) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session��player��Ϣ��ʧЧ��");
			return;
		}
		Connection con = ServerManager.gameDBConnect.getDBConnect();
		if (con == null) { // ���ݿ����ӳ�����
			logger.error("quest�������ӳ�����");
			return;
		}
		Player player = (Player)session.attachment();
		playerId = player.getPlayerId();
		HallServerOnlineManager.getInstance().getLock().lock(playerId);
		try {
			SignInQuest signIn = SignInDao.getSignInInfo(con, playerId);
			if (signIn != null) {
				QuestMsgSend.signInQuestSync(session, signIn);
			}
		} catch (Exception e) {
			ErrorPrint.print(e);
		} finally {
			ServerManager.gameDBConnect.closeConnect(con);
			HallServerOnlineManager.getInstance().getLock().unlock(playerId);
		}
	}
	
	private void receiveSignInQuest(ISession session, String deviceId, int playerId) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session��player��Ϣ��ʧЧ��");
			return;
		}
		Connection con = ServerManager.gameDBConnect.getDBConnect();
		if (con == null) { // ���ݿ����ӳ�����
			logger.error("quest�������ӳ�����");
			return;
		}
		Player player = (Player)session.attachment();
		playerId = player.getPlayerId();
		HallServerOnlineManager.getInstance().getLock().lock(playerId);
		try {
			con.setAutoCommit(false);
			SignInQuest signIn = SignInDao.getSignInInfo(con, playerId);
			int gold = 0;
			String cardId = "";
			if (signIn.getCanReceive() == true) {
				int day = signIn.getSignInDay()+1;
				SignInQuestModel model = ConfigData.signInModels.get(day);
				if (model.Gold > 0) {
					PlayerDao.addGold(con, playerId, model.Gold);
					gold = model.Gold;
				}
				RoleMsgSend.syncRoleGold(playerId);
				SignInDao.updateSignInInfo(con, playerId);
				SignInQuest signInEnd = SignInDao.getSignInInfo(con, playerId);
				QuestMsgSend.receiveSignInQuestSync(session, gold, cardId, model.CardPackCount, signInEnd);
			}
		} catch (Exception e) {
			ErrorPrint.print(e);
		} finally {
			try {
				con.setAutoCommit(true);
			} catch (SQLException e) {
				ErrorPrint.print(e);
			}
			ServerManager.gameDBConnect.closeConnect(con);
			HallServerOnlineManager.getInstance().getLock().unlock(playerId);
		}
	}
	
	private void setSignInPhoneNumber(ISession session, String deviceId, int playerId, String phoneNumber) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session��player��Ϣ��ʧЧ��");
			return;
		}
		if (phoneNumber.length() != 11) {
			logger.error("signIn���ֻ��������-�ֻ���Ӧ����11λ");
			return;
		}
		Connection con = ServerManager.gameDBConnect.getDBConnect();
		if (con == null) { // ���ݿ����ӳ�����
			logger.error("quest�������ӳ�����");
			return;
		}
		Player player = (Player)session.attachment();
		playerId = player.getPlayerId();
		HallServerOnlineManager.getInstance().getLock().lock(playerId);
		try {
			SignInDao.updateSignInPhoneNumber(con, playerId, phoneNumber);
		} catch (Exception e) {
			ErrorPrint.print(e);
		} finally {
			ServerManager.gameDBConnect.closeConnect(con);
			HallServerOnlineManager.getInstance().getLock().unlock(playerId);
		}
	}
}