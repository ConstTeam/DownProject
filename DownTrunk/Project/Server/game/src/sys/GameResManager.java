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
		
			logger.info("��������������ҳ��Զ����ٷ����߳̿�ʼ��");
			while (true) {
				/*
				 * ����������״̬�ж�
				 */
				if (!ServerStaticInfo.opened) {
					logger.info("�������رգ���ҳ��Զ����ٷ����߳̽�����");
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
							logger.info("����Id��{}����ʱ��δ��ʼ��Ϸ���Զ����١�", room.getRoomId());
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