package sys;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.CenterServer;
import app.ServerStaticInfo;
import db.RechargeDao;
import module.RechargeInfo;
import platform.PayModel;
import platform.alipay.AliPayHttpHandler;
import redis.RedisProxy;
import redis.data.PlayerInfo;
import redis.subscribe.SubPubConst;
import util.ErrorPrint;

public class PayManager implements Runnable, UncaughtExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(CenterServer.class);
	private static PayManager instance;

	public static PayModel payInfos;
	public static PayManager getInstance() {
		if (instance == null) {
			instance = new PayManager();
		}

		return instance;
	}
	
	public static void start() {
		PayManager pay = PayManager.getInstance();
		Thread payThread = new Thread(pay, "PayThread");
		payThread.setUncaughtExceptionHandler(pay);
		payThread.start();
	}

	@Override
	public void run() {
		
		logger.info("服务器开启，游戏充值线程开始。");
		
		while (true) {
			/*
			 * 服务器开启状态判定
			 */
			if (!ServerStaticInfo.opened) {
				logger.info("服务器关闭，游戏充值线程结束。");
				break;
			}

			/*
			 * 处理充值的所有消息
			 */
			RechargeInfo rechargeInfo = null;
			
			while ((rechargeInfo = payInfos.poll()) != null) {
				
				int playerId = rechargeInfo.getPlayerId();
				try {
					PlayerInfo playerInfo = RedisProxy.getInstance().getPlayerInfo(playerId);
					if (playerInfo == null) {
						logger.error("玩家：{}，该玩家不存在，不能进行充值操作。", playerId);
						continue;
					}
					if (RechargeDao.recharge(rechargeInfo)) {
						logger.info("充值操作结束！playerId：" + playerId + "，OrderId：" + rechargeInfo.getOrderId());
						RedisProxy.getInstance().playerNotice(playerInfo.getServerId(), playerInfo.getPlayerId(), SubPubConst.PLAYER_RECHARGE);
					} else {
						payInfos.redealRechargeInfo(rechargeInfo);
					}
				} catch (Exception e) {
					ErrorPrint.print(e);
				}
			}
		}
	}
	
	/**
	 * 初始化
	 */
	public void init() {
		payInfos = PayModel.getInstance();
		new AliPayHttpHandler();
	}
	
	public void addRechargeInfo(ArrayList<RechargeInfo> rechargeInfos) {
		if (rechargeInfos != null && rechargeInfos.size() > 0) {
			for (RechargeInfo rechargeInfo: rechargeInfos) {
				addRechargeInfo(rechargeInfo);
			}
		}
	}
	
	public void addRechargeInfo(RechargeInfo rechargeInfo) {
		payInfos.dealRechargeInfo(rechargeInfo);
	}
	
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		ErrorPrint.print(e);
		System.err.println("	at sys.manager.PayThreadExceptionHandler(PayThreadExceptionHandler.class:0)");
		start();
	}
	
}