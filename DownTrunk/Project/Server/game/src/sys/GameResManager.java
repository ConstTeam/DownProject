package sys;


import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.ServerStaticInfo;
import module.scene.GameRoom;
import util.ErrorPrint;

public class GameResManager implements Runnable, UncaughtExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GameResManager.class);
	
	private static GameResManager instance;
	
	private static final int INTERVAL = 1 * 60000;

	public static GameResManager getInstance() {
		if (instance == null) {
			instance = new GameResManager();
		}

		return instance;
	}
	
	public static void start() {
		GameResManager manager = GameResManager.getInstance();
		Thread thread = new Thread(manager, "GameResManager");
		thread.setUncaughtExceptionHandler(manager);
		thread.start();
	}

	@Override
	public void run() {
		
			logger.info("服务器开启，金币场自动销毁房间线程开始。");
			while (true) {
				/*
				 * 服务器开启状态判定
				 */
				if (!ServerStaticInfo.opened) {
					logger.info("服务器关闭，金币场自动销毁房间线程结束。");
					break;
				}
				
				try {
					ArrayList<GameRoom> rooms = new ArrayList<>();
					rooms.addAll(GameRoomManager.getInstance().getRooms());
					for (GameRoom room : rooms) {
						int roomId = room.getRoomId();
						GameRoomManager.getInstance().getLock().lock(roomId);
						try {
							GameRoomManager.getInstance().destroyRoom(room.getRoomId());
							logger.info("房间Id：{}，长时间未开始游戏，自动销毁。", room.getRoomId());
						} finally {
							GameRoomManager.getInstance().getLock().unlock(roomId);
						}
					}
					
					Thread.sleep(INTERVAL);
				} catch (Exception e) {
					ErrorPrint.print(e);
				}
				
			}
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		ErrorPrint.print(e);
		System.err.println("	at sys.GameResManager(GameResManager.class:0)");
		start();
	}

}