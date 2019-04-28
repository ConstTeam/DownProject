package message.hall.login;

import java.sql.Connection;
import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.ServerManager;
import app.ServerStaticInfo;
import db.AccountDao;
import db.ClearDao;
import db.DBModuleConst;
import db.PlayerDao;
import db.UseCountDao;
import db.log.LogDao;
import db.module.player.Player;
import memory.SessionMemory;
import message.game.fight.FightMsgSend;
import message.hall.S2CMessageSend;
import message.hall.role.RoleMsgSend;
import module.Account;
import net.DataAccessException;
import net.IByteBuffer;
import net.ISession;
import net.Servicelet;
import platform.fb.FBHttpHandler;
import platform.fb.FBUserInfoEntity;
import platform.wx.WXUserInfoEntity;
import platform.wx.WeiXinHttpHandler;
import redis.RedisProxy;
import redis.data.PlayerInfo;
import redis.data.RoomInfo;
import redis.data.ServerInfo;
import redis.data.ServerOpen;
import redis.subscribe.SubPubConst;
import sys.GameRoomAssign;
import sys.HallServerOnlineManager;
import util.ErrorPrint;
import util.TimeFormat;
import util.Tools;

/**
 * ��¼ģ��
 * 
 */
public class LoginService extends Servicelet {

	private static final Logger logger = LoggerFactory.getLogger(LoginService.class);
	
	private ConcurrentHashMap<String, AtomicBoolean> loginLocks = new ConcurrentHashMap<>();
	public static HashMap<String, Calendar> uuidMap = new HashMap<>();

	@Override
	public void access(ISession session, IByteBuffer data) throws DataAccessException {
		int type = data.readByte();

		switch (type) {
		case LoginMessageConst.LOGIN:
			String platform = data.readUTF();
			String channel = data.readUTF();
			String deviceInfo = data.readUTF();
			String uuid = data.readUTF();
			String deviceId = data.readUTF();
			String platformId = data.readUTF();
			String sessionId = data.readUTF();
			platformLogin(session, deviceId, platform, channel, uuid, platformId, sessionId, deviceInfo);
			logger.info("�豸��¼��" + deviceId);
			break;
			
		case LoginMessageConst.ASSIGN_INSTANCE_SERVER:
//			int deckId = data.readInt();
			assignServer(session, 0);
			break;
			
		case LoginMessageConst.CANCEL_ASSIGN:
			cancelAssign(session);
			break;
			
		case LoginMessageConst.RE_LOGIN:
			int playerId = data.readInt();
			deviceId = data.readUTF();
			reLogin(session, playerId, deviceId);
			break;
		
		case LoginMessageConst.ASSIGN_GUIDE_INSTANCE_SERVER:
			guideServerAssign(session);
			break;
		}
	}

	private void platformLogin(ISession session, String deviceId, String platform, String channel, String uuid,
			String platformId, String sessionId, String logStr) {
		ServerOpen open = RedisProxy.getInstance().getServerOpenMessage();
		if (open != null && !Tools.isEmptyString(open.getOpenTime()) && !Tools.isEmptyString(open.getMessage())) {
			try {
				Calendar calendar = TimeFormat.getTimeByStr(open.getOpenTime());
				if (calendar.after(Calendar.getInstance())) {
					if (!RedisProxy.getInstance().checkTestDeviceId(deviceId)) {
						return;
					}
				}
			} catch (ParseException e) {
				ErrorPrint.print(e);
			}
		}
		Calendar calendar = uuidMap.get(uuid);
		if (calendar != null) {
			logger.error("����Ѿ���¼�� uuid: " + uuid);
			return;
		}
		String nickname = "";
		String icon = "1";
		String unionId = "";
		int sex = 1;

		switch (platform) {
		case "WX":
			WXUserInfoEntity userInfo = WeiXinHttpHandler.login(sessionId);
			if (userInfo == null) {
				return;
			}
			platformId = userInfo.getOpenid();
			nickname = userInfo.getNickname();
//			icon = userInfo.getHeadimgurl();
			sex = userInfo.getSex() == 2 ? 2 : 1;
			unionId = userInfo.getUnionid();
			break;
		case "FB":
			FBUserInfoEntity info = FBHttpHandler.login(sessionId);
			platformId = info.getId();
			break;
		default:
			if (!ServerStaticInfo.getPlatform().equals("ALL")) {
				logger.info("δ����Debugģʽ�����ܽ�������ƽ̨��¼����ǰ��¼ƽ̨��{}����¼�ʺţ�{}���豸�ţ�{}", platform, platformId, deviceId);
				return;
			}
		}
		
		login(platformId, deviceId, session, platform, channel, uuid, logStr, nickname, icon, sex, unionId);
	}
	
	private void assignServer(ISession session, int deckId) {
		
		if (session.attachment() == null) {
			logger.error("session��player��Ϣ��ʧЧ��");
			return;
		}
		Player player = (Player)session.attachment();
		PlayerInfo playerInfo = RedisProxy.getInstance().getPlayerInfo(player.getPlayerId());
		if (playerInfo == null) {
			logger.error("��ң�{}������ƥ�����ʧ�ܣ���ȡ�����Ϣʧ�ܡ�", player.getPlayerId());
			return;
		}
		int playerId = playerInfo.getPlayerId();
		HallServerOnlineManager.getInstance().getLock().lock(playerId);
		try {
//			CardDeckModel cardDeck = CardGroupDao.getCardDeck(player.getPlayerId(), String.valueOf(deckId));
//			if (cardDeck == null) {
//				logger.info("��ң�{}������Id:{}�����ÿ���ʧ�ܣ���ȡ������Ϣʧ�ܡ�", player.getPlayerId(), deckId);
//				return;
//			}
			if (!GameRoomAssign.getInstance().addPlayerInfo(playerInfo)) {
				logger.error("��ң�{}������ƥ�����ʧ�ܡ�", player.getPlayerId());
				return;
			}
			Calendar assignTime = Calendar.getInstance();
			assignTime.add(Calendar.SECOND, GameRoomAssign.LAST_ASSIGN_TIME);
			playerInfo.setAssignTime(assignTime);
			playerInfo.setDeckId(String.valueOf(deckId));
			RedisProxy.getInstance().updatePlayerInfo(playerInfo, "deckId");
		} finally {
			HallServerOnlineManager.getInstance().getLock().unlock(playerId);
		}
	}
	
	private void guideServerAssign(ISession session) {
		if (session.attachment() == null) {
			logger.error("session��player��Ϣ��ʧЧ��");
			return;
		}
		Player player = (Player)session.attachment();
		PlayerInfo playerInfo = RedisProxy.getInstance().getPlayerInfo(player.getPlayerId());
		if (playerInfo == null) {
			logger.error("��ң�{}������ƥ�����ʧ�ܣ���ȡ�����Ϣʧ�ܡ�", player.getPlayerId());
			return;
		}
		int playerId = playerInfo.getPlayerId();
		HallServerOnlineManager.getInstance().getLock().lock(playerId);
		try {
			ServerInfo serverInfo = GameRoomAssign.getInstance().distributeServer();
			if (serverInfo == null) {
				logger.error("��������ʧ�ܡ��޿ɷ������Ϸ����");
				LoginMessageSend.cancelAssign(session);
				return;
			}
			LoginMessageSend.assignSuccess(session);
			LoginMessageSend.connGameServer(session, serverInfo);
		} finally {
			HallServerOnlineManager.getInstance().getLock().unlock(playerId);
		}
	}
	
	private void cancelAssign(ISession session) {
		
		if (session.attachment() == null) {
			logger.error("session��player��Ϣ��ʧЧ��");
			return;
		}
		Player player = (Player)session.attachment();
		PlayerInfo playerInfo = RedisProxy.getInstance().getPlayerInfo(player.getPlayerId());
		if (playerInfo == null) {
			logger.error("��ң�{}������ƥ�����ʧ�ܣ���ȡ�����Ϣʧ�ܡ�", player.getPlayerId());
			return;
		}
		int playerId = playerInfo.getPlayerId();
		HallServerOnlineManager.getInstance().getLock().lock(playerId);
		try {
			if (!GameRoomAssign.getInstance().removePlayerInfo(playerId)) {
				logger.error("��ң�{}��ȡ��ƥ��ʧ�ܡ�", player.getPlayerId());
				FightMsgSend.messageBox(session, "");
				return;
			}
			LoginMessageSend.cancelAssign(session);
			logger.error("��ң�{}��ȡ��ƥ�䡣", player.getPlayerId());
		} finally {
			HallServerOnlineManager.getInstance().getLock().unlock(playerId);
		}
	}
	
	/**
	 * ��¼ ������ʺ���Ϣ���Զ������ʺŲ���ͻ��˷�����Ҫ������ɫ��Ϣ
	 * 
	 * @param platformId
	 * @param password
	 * @param deviceId
	 * @param session
	 * @param platform
	 * @param channel
	 * @param uuid
	 * @return
	 */
	private Player login(String platformId, String deviceId, ISession session, String platform, String channel, String uuid, String logStr, String nickname, String icon, int sex, String unionId) {
		// ��ֹͬһ�ʺţ�ͬʱ��¼
		loginLocks.putIfAbsent(platformId, new AtomicBoolean(false));
		if (!loginLocks.get(platformId).compareAndSet(false, true)) {
			return null;
		}
		Connection connect = ServerManager.gameDBConnect.getDBConnect();
		if (connect == null) { // ���ݿ����ӳ�����
			loginLocks.get(platformId).set(false);
			return null;
		}
		try {
			Calendar calendar = uuidMap.get(uuid);
			if (calendar != null) {
				logger.error("�˺ţ�{}��uuid�ظ�������Ѿ���¼�� uuid��{}", platformId, uuid);
				return null;
			}
			Account account = AccountDao.getAccount(connect, platformId, platform, channel);
			if (account == null) {
				logger.error("��ȡ�ʺ���Ϣʧ�ܣ��ʺţ�" + platformId);
				return null;
			}
			boolean createPlayer = false;
			PlayerInfo playerInfo = null;
			if (account.playerId == 0) { // ���û���˺�
				if (!RedisProxy.getInstance().isCanCreatePlayer()) {
					return null;
				}
				playerInfo = RedisProxy.getInstance().addPlayerInfo();
				if (playerInfo == null) {
					logger.error("�����ʺ�ʧ�ܣ��������Idʧ�ܣ�");
					return null;
				}
				if (!PlayerDao.createPlayer(connect, playerInfo.getPlayerId(), nickname, icon, platformId, platform, channel)) { // �Զ������ʺźͽ�ɫ
					logger.error("�����ʺ�ʧ�ܣ��ʺţ�" + platformId);
					RedisProxy.getInstance().delPlayerInfo(playerInfo.getPlayerId());
					return null;
				}
				createPlayer = true;
				logger.info("�����ʺųɹ����ʺţ�{}����ң�{}", platformId, playerInfo.getPlayerId());
				account = AccountDao.getAccount(connect, platformId, platform, channel);
				if (account == null || account.playerId == 0) {
					logger.error("��ȡ�ʺ���Ϣʧ�ܣ��ʺţ�{}", platformId);
					return null;
				}
			}

			ServerInfo serverInfo = null;
			if (playerInfo == null) {
				playerInfo = RedisProxy.getInstance().getPlayerInfo(account.playerId);
				if (playerInfo == null) {
					playerInfo = RedisProxy.getInstance().addPlayerInfo(account.playerId);
				}
				if (playerInfo == null) {
					logger.error("��ȡ���Redis��Ϣʧ�ܣ���ң�{}", account.playerId);
					return null;
				}
				if (playerInfo.isOnline()) {
					if (!deviceId.equals(playerInfo.getDeviceId())) {
						logger.info("����ϴε�¼�豸�ţ�{}�����ε�¼�豸�ţ�{}", playerInfo.getDeviceId(), deviceId);
					}
					if (playerInfo.getServerId().equals(ServerStaticInfo.getServerId())) {
						ISession oldSession = SessionMemory.getInstance().getSession(playerInfo.getPlayerId());
						if (oldSession != null && !oldSession.isClosed()) {
							S2CMessageSend.messageBox(oldSession, "�ʺ��ظ���¼���ѱ���������");
							HallServerOnlineManager.getInstance().playerLogout(oldSession);
						}
					} else {
						RedisProxy.getInstance().playerNotice(playerInfo.getServerId(), playerInfo.getPlayerId(), SubPubConst.PLAYER_LOGOUT);
					}
				}
				if (playerInfo.getRoomId() != 0) {
					// ����ǰ�ڷ����ڣ��ָ�������
					RoomInfo roomInfo = RedisProxy.getInstance().getRoomInfo(playerInfo.getRoomId());
					if (roomInfo != null) {
						serverInfo = RedisProxy.getInstance().getServerInfo(roomInfo.getServerId());
					} else {
						playerInfo.setRoomId(0);
						RedisProxy.getInstance().updatePlayerInfo(playerInfo, "roomId");
					}
				}
			}
			/*
			 * ����Ƿ��ѱ�����
			 */
			String forbidden = RedisProxy.getInstance().getPlayerForbidden(playerInfo.getPlayerId());
			if (forbidden != null) {
				try {
					if ("forever".equalsIgnoreCase(forbidden)) {
						logger.info("��ң�{}���ѱ����÷�ͣ�����ܽ��е�¼��", playerInfo.getPlayerId());
						return null;
					}
					Calendar forbiddenTime = TimeFormat.getTimeByStr(forbidden);
					Calendar now = Calendar.getInstance();
					if (now.before(forbiddenTime)) {
						logger.info("��ң�{}����ͣ��{}�����ܽ��е�¼��", playerInfo.getPlayerId(), forbidden);
						return null;
					}
				} catch (ParseException e) {
					ErrorPrint.print(e);
				}
			}
			
			Player player = PlayerDao.getPlayerInfo(connect, account.playerId);
			if (player == null) {
				logger.error("��ȡ��ɫ��Ϣʧ�ܣ��ʺţ�" + platformId);
				return null;
			}
			ClearDao.dailyClearByPlayerId(connect, account.playerId);
			HashMap<Integer,Integer> useCount = UseCountDao.getUseCount(connect, account.playerId);
			if (useCount == null) {
				logger.error("��ȡ��ɫʣ�������Ϣʧ�ܣ����Id��" + account.playerId);
				return null;
			}
			
			player.setAccountId(account.platformId);

			if (nickname.equals("")) {
				if (playerInfo.getNickname() != null && !"".equals(playerInfo.getNickname())) {
					nickname = playerInfo.getNickname();
				} else if (player.getNickname() != null && !"".equals(player.getNickname())) {
					nickname = player.getNickname();
				} else {
					nickname = "���"; // ��ʱ�޸�
				}
			}

			if (Tools.isEmptyString(playerInfo.getIcon())) {
				playerInfo.setIcon(icon);
			}
			playerInfo.setNickname(nickname);
			playerInfo.setSex(sex);
			playerInfo.setDeviceId(deviceId);
			playerInfo.setServerId(ServerStaticInfo.getServerId());
			playerInfo.setLastLoginTime(TimeFormat.getTime());
			playerInfo.setOnline(true);
			player.setNickname(nickname);
			player.setIcon(playerInfo.getIcon());
			player.setSex(sex);
			player.setDeviceId(deviceId);
			
			RedisProxy.getInstance().updatePlayerInfo(playerInfo);
			RedisProxy.getInstance().addPlayerGoldRanking(player.getPlayerId(), player.getGold());
			
			// DBҲ��Ҫ���������Ϣ�����Ǹ���ʧ�ܲ���Ӱ���¼����
			PlayerDao.updatePlayerInfo(connect, playerInfo);
			
			RedisProxy.getInstance().removeRechargeNotice(playerInfo.getPlayerId());
			
			 /*
			  *  ��¼��Ϣ����
			  */
			login(session, player, serverInfo, platform);
			int guideId = RedisProxy.getInstance().getPlayerGuideID(playerInfo.getPlayerId());
			RoleMsgSend.syncRoleGuideId(session, guideId);
			RoleMsgSend.syncRoleResCount(session, useCount);
			
			uuidMap.put(uuid, Calendar.getInstance());
			logger.info("��ҵ�¼LoginServer���˺ţ�" + platformId + "��UUID��" + uuid);

			// �����־
			try {
				if (createPlayer) {
					LogDao.user(player, deviceId, platform, channel, session.getAddress().getHostAddress().toString(), unionId);
				}
				if (!Tools.isEmptyString(logStr)) {
					LogDao.device(player, deviceId, session.getAddress().getHostAddress().toString(), logStr);
				}
				LogDao.login(player, deviceId, platform, channel, session.getAddress().getHostAddress().toString(), "");
			} catch (Exception e) {
				ErrorPrint.print(e);
			}

			return player;
		} finally {
			ServerManager.gameDBConnect.closeConnect(connect);
			loginLocks.get(platformId).set(false);
		}
	}

	/**
	 * ��¼����ͬ��
	 */
	private void login(ISession session, Player player, ServerInfo serverInfo, String platform) {

		HallServerOnlineManager.getInstance().playerLogin(session, player);
		/*
		 *  ��¼���������Ϣ
		 */
		S2CMessageSend.loginResult(session, player, platform);
		logger.info("��¼���������Ϣ��account��{}��playerId��{}", player.getAccountId(), player.getPlayerId());
	}
	
	private void reLogin(ISession session, int playerId, String deviceId) {
		if (!LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		/*
		 * ����ʱ��
		 */
		ServerOpen open = RedisProxy.getInstance().getServerOpenMessage();
		if (open != null && !Tools.isEmptyString(open.getOpenTime()) && !Tools.isEmptyString(open.getMessage())) {
			try {
				Calendar calendar = TimeFormat.getTimeByStr(open.getOpenTime());
				if (calendar.after(Calendar.getInstance())) {
					return;
				}
			} catch (ParseException e) {
				ErrorPrint.print(e);
			}
		}
		HallServerOnlineManager.getInstance().getLock().lock(playerId);
		try {
			ISession oldSession = SessionMemory.getInstance().getSession(playerId);
			if (oldSession != null && !session.equals(oldSession)) {
				HallServerOnlineManager.getInstance().playerLogout(oldSession);
				oldSession.close();
				logger.info("���{}����������������Session����", playerId);
			}
			Player player = PlayerDao.getPlayerInfo(playerId);
			if (player == null) {
				logger.error("��ң�{}����������������ʧ�ܡ���ȡ�����Ϣʧ�ܡ�", playerId);
				return;
			}
			/*
			 * ��Ҷ���
			 */
			String forbidden = RedisProxy.getInstance().getPlayerForbidden(playerId);
			if (forbidden != null) {
				try {
					if ("forever".equalsIgnoreCase(forbidden)) {
						logger.info("��ң�{}���ѱ����÷�ͣ�����ܽ��е�¼��", playerId);
						return;
					}
					Calendar forbiddenTime = TimeFormat.getTimeByStr(forbidden);
					Calendar now = Calendar.getInstance();
					if (now.before(forbiddenTime)) {
						logger.info("��ң�{}����ͣ��{}�����ܽ��е�¼��", playerId, forbidden);
						return;
					}
				} catch (ParseException e) {
					ErrorPrint.print(e);
				}
			}
			if (RedisProxy.getInstance().isHaveRechargeNotice(playerId)) {
			}
			HallServerOnlineManager.getInstance().playerLogin(session, player);
			logger.info("��ң�{}�����������������ɹ���", playerId);
		} finally {
			HallServerOnlineManager.getInstance().getLock().unlock(playerId);
		}
	}
	
	public static boolean checkDeviceId(int playerId, String deviceId, ISession session) {
		
		PlayerInfo playerInfo = RedisProxy.getInstance().getPlayerInfo(playerId);
		if (playerInfo == null) {
			logger.info("��Ҳ����ڣ���ң�" + playerId);
			return false;
		}
		if (!deviceId.equals(playerInfo.getDeviceId())) {
			if (playerInfo.getDeviceId().equals(DBModuleConst.FORCE_LOGOUT)) {
				logger.info("����ѱ���ţ���ң�" + playerId);
				session.close();
				return false;
			} else {
				session.close();
				logger.info("��Ҹ����豸����¼��ϢʧЧ����ң�" + playerId);
				return false;
			}
		}
		return true;
	}
}
