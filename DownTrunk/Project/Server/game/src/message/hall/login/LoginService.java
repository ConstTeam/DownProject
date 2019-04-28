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
 * 登录模块
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
			logger.info("设备登录：" + deviceId);
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
			logger.error("玩家已经登录！ uuid: " + uuid);
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
				logger.info("未开启Debug模式，不能进行其他平台登录。当前登录平台：{}，登录帐号：{}，设备号：{}", platform, platformId, deviceId);
				return;
			}
		}
		
		login(platformId, deviceId, session, platform, channel, uuid, logStr, nickname, icon, sex, unionId);
	}
	
	private void assignServer(ISession session, int deckId) {
		
		if (session.attachment() == null) {
			logger.error("session中player信息已失效。");
			return;
		}
		Player player = (Player)session.attachment();
		PlayerInfo playerInfo = RedisProxy.getInstance().getPlayerInfo(player.getPlayerId());
		if (playerInfo == null) {
			logger.error("玩家：{}，进入匹配队列失败，获取玩家信息失败。", player.getPlayerId());
			return;
		}
		int playerId = playerInfo.getPlayerId();
		HallServerOnlineManager.getInstance().getLock().lock(playerId);
		try {
//			CardDeckModel cardDeck = CardGroupDao.getCardDeck(player.getPlayerId(), String.valueOf(deckId));
//			if (cardDeck == null) {
//				logger.info("玩家：{}，卡组Id:{}。设置卡组失败，获取卡组信息失败。", player.getPlayerId(), deckId);
//				return;
//			}
			if (!GameRoomAssign.getInstance().addPlayerInfo(playerInfo)) {
				logger.error("玩家：{}，进入匹配队列失败。", player.getPlayerId());
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
			logger.error("session中player信息已失效。");
			return;
		}
		Player player = (Player)session.attachment();
		PlayerInfo playerInfo = RedisProxy.getInstance().getPlayerInfo(player.getPlayerId());
		if (playerInfo == null) {
			logger.error("玩家：{}，进入匹配队列失败，获取玩家信息失败。", player.getPlayerId());
			return;
		}
		int playerId = playerInfo.getPlayerId();
		HallServerOnlineManager.getInstance().getLock().lock(playerId);
		try {
			ServerInfo serverInfo = GameRoomAssign.getInstance().distributeServer();
			if (serverInfo == null) {
				logger.error("创建房间失败。无可分配的游戏服。");
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
			logger.error("session中player信息已失效。");
			return;
		}
		Player player = (Player)session.attachment();
		PlayerInfo playerInfo = RedisProxy.getInstance().getPlayerInfo(player.getPlayerId());
		if (playerInfo == null) {
			logger.error("玩家：{}，进入匹配队列失败，获取玩家信息失败。", player.getPlayerId());
			return;
		}
		int playerId = playerInfo.getPlayerId();
		HallServerOnlineManager.getInstance().getLock().lock(playerId);
		try {
			if (!GameRoomAssign.getInstance().removePlayerInfo(playerId)) {
				logger.error("玩家：{}，取消匹配失败。", player.getPlayerId());
				FightMsgSend.messageBox(session, "");
				return;
			}
			LoginMessageSend.cancelAssign(session);
			logger.error("玩家：{}，取消匹配。", player.getPlayerId());
		} finally {
			HallServerOnlineManager.getInstance().getLock().unlock(playerId);
		}
	}
	
	/**
	 * 登录 如果无帐号信息，自动创建帐号并向客户端发送需要创建角色消息
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
		// 防止同一帐号，同时登录
		loginLocks.putIfAbsent(platformId, new AtomicBoolean(false));
		if (!loginLocks.get(platformId).compareAndSet(false, true)) {
			return null;
		}
		Connection connect = ServerManager.gameDBConnect.getDBConnect();
		if (connect == null) { // 数据库连接池已满
			loginLocks.get(platformId).set(false);
			return null;
		}
		try {
			Calendar calendar = uuidMap.get(uuid);
			if (calendar != null) {
				logger.error("账号：{}，uuid重复，玩家已经登录！ uuid：{}", platformId, uuid);
				return null;
			}
			Account account = AccountDao.getAccount(connect, platformId, platform, channel);
			if (account == null) {
				logger.error("获取帐号信息失败！帐号：" + platformId);
				return null;
			}
			boolean createPlayer = false;
			PlayerInfo playerInfo = null;
			if (account.playerId == 0) { // 如果没有账号
				if (!RedisProxy.getInstance().isCanCreatePlayer()) {
					return null;
				}
				playerInfo = RedisProxy.getInstance().addPlayerInfo();
				if (playerInfo == null) {
					logger.error("创建帐号失败！生成玩家Id失败！");
					return null;
				}
				if (!PlayerDao.createPlayer(connect, playerInfo.getPlayerId(), nickname, icon, platformId, platform, channel)) { // 自动创建帐号和角色
					logger.error("创建帐号失败！帐号：" + platformId);
					RedisProxy.getInstance().delPlayerInfo(playerInfo.getPlayerId());
					return null;
				}
				createPlayer = true;
				logger.info("创建帐号成功！帐号：{}，玩家：{}", platformId, playerInfo.getPlayerId());
				account = AccountDao.getAccount(connect, platformId, platform, channel);
				if (account == null || account.playerId == 0) {
					logger.error("获取帐号信息失败！帐号：{}", platformId);
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
					logger.error("获取玩家Redis信息失败！玩家：{}", account.playerId);
					return null;
				}
				if (playerInfo.isOnline()) {
					if (!deviceId.equals(playerInfo.getDeviceId())) {
						logger.info("玩家上次登录设备号：{}，本次登录设备号：{}", playerInfo.getDeviceId(), deviceId);
					}
					if (playerInfo.getServerId().equals(ServerStaticInfo.getServerId())) {
						ISession oldSession = SessionMemory.getInstance().getSession(playerInfo.getPlayerId());
						if (oldSession != null && !oldSession.isClosed()) {
							S2CMessageSend.messageBox(oldSession, "帐号重复登录，已被顶号下线");
							HallServerOnlineManager.getInstance().playerLogout(oldSession);
						}
					} else {
						RedisProxy.getInstance().playerNotice(playerInfo.getServerId(), playerInfo.getPlayerId(), SubPubConst.PLAYER_LOGOUT);
					}
				}
				if (playerInfo.getRoomId() != 0) {
					// 断线前在房间内，恢复到房间
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
			 * 玩家是否已被冻结
			 */
			String forbidden = RedisProxy.getInstance().getPlayerForbidden(playerInfo.getPlayerId());
			if (forbidden != null) {
				try {
					if ("forever".equalsIgnoreCase(forbidden)) {
						logger.info("玩家：{}，已被永久封停，不能进行登录。", playerInfo.getPlayerId());
						return null;
					}
					Calendar forbiddenTime = TimeFormat.getTimeByStr(forbidden);
					Calendar now = Calendar.getInstance();
					if (now.before(forbiddenTime)) {
						logger.info("玩家：{}，封停至{}，不能进行登录。", playerInfo.getPlayerId(), forbidden);
						return null;
					}
				} catch (ParseException e) {
					ErrorPrint.print(e);
				}
			}
			
			Player player = PlayerDao.getPlayerInfo(connect, account.playerId);
			if (player == null) {
				logger.error("获取角色信息失败！帐号：" + platformId);
				return null;
			}
			ClearDao.dailyClearByPlayerId(connect, account.playerId);
			HashMap<Integer,Integer> useCount = UseCountDao.getUseCount(connect, account.playerId);
			if (useCount == null) {
				logger.error("获取角色剩余次数信息失败！玩家Id：" + account.playerId);
				return null;
			}
			
			player.setAccountId(account.platformId);

			if (nickname.equals("")) {
				if (playerInfo.getNickname() != null && !"".equals(playerInfo.getNickname())) {
					nickname = playerInfo.getNickname();
				} else if (player.getNickname() != null && !"".equals(player.getNickname())) {
					nickname = player.getNickname();
				} else {
					nickname = "玩家"; // 临时修改
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
			
			// DB也需要更新玩家信息，但是更新失败不能影响登录流程
			PlayerDao.updatePlayerInfo(connect, playerInfo);
			
			RedisProxy.getInstance().removeRechargeNotice(playerInfo.getPlayerId());
			
			 /*
			  *  登录信息发送
			  */
			login(session, player, serverInfo, platform);
			int guideId = RedisProxy.getInstance().getPlayerGuideID(playerInfo.getPlayerId());
			RoleMsgSend.syncRoleGuideId(session, guideId);
			RoleMsgSend.syncRoleResCount(session, useCount);
			
			uuidMap.put(uuid, Calendar.getInstance());
			logger.info("玩家登录LoginServer，账号：" + platformId + "，UUID：" + uuid);

			// 打点日志
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
	 * 登录数据同步
	 */
	private void login(ISession session, Player player, ServerInfo serverInfo, String platform) {

		HallServerOnlineManager.getInstance().playerLogin(session, player);
		/*
		 *  登录发送玩家信息
		 */
		S2CMessageSend.loginResult(session, player, platform);
		logger.info("登录发送玩家信息，account：{}，playerId：{}", player.getAccountId(), player.getPlayerId());
	}
	
	private void reLogin(ISession session, int playerId, String deviceId) {
		if (!LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		/*
		 * 开服时间
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
				logger.info("玩家{}，大厅服断线重连Session清理。", playerId);
			}
			Player player = PlayerDao.getPlayerInfo(playerId);
			if (player == null) {
				logger.error("玩家：{}，大厅服断线重连失败。获取玩家信息失败。", playerId);
				return;
			}
			/*
			 * 玩家冻结
			 */
			String forbidden = RedisProxy.getInstance().getPlayerForbidden(playerId);
			if (forbidden != null) {
				try {
					if ("forever".equalsIgnoreCase(forbidden)) {
						logger.info("玩家：{}，已被永久封停，不能进行登录。", playerId);
						return;
					}
					Calendar forbiddenTime = TimeFormat.getTimeByStr(forbidden);
					Calendar now = Calendar.getInstance();
					if (now.before(forbiddenTime)) {
						logger.info("玩家：{}，封停至{}，不能进行登录。", playerId, forbidden);
						return;
					}
				} catch (ParseException e) {
					ErrorPrint.print(e);
				}
			}
			if (RedisProxy.getInstance().isHaveRechargeNotice(playerId)) {
			}
			HallServerOnlineManager.getInstance().playerLogin(session, player);
			logger.info("玩家：{}，大厅服断线重连成功。", playerId);
		} finally {
			HallServerOnlineManager.getInstance().getLock().unlock(playerId);
		}
	}
	
	public static boolean checkDeviceId(int playerId, String deviceId, ISession session) {
		
		PlayerInfo playerInfo = RedisProxy.getInstance().getPlayerInfo(playerId);
		if (playerInfo == null) {
			logger.info("玩家不存在，玩家：" + playerId);
			return false;
		}
		if (!deviceId.equals(playerInfo.getDeviceId())) {
			if (playerInfo.getDeviceId().equals(DBModuleConst.FORCE_LOGOUT)) {
				logger.info("玩家已被封号，玩家：" + playerId);
				session.close();
				return false;
			} else {
				session.close();
				logger.info("玩家更换设备，登录信息失效，玩家：" + playerId);
				return false;
			}
		}
		return true;
	}
}
