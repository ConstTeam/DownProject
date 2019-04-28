package message.game;

import java.util.HashMap;

import message.Pinglet;
import message.game.fight.FightService;
import message.game.gm.GMGameService;
import message.game.room.RoomService;


public class GameServiceConfig {
	
	private HashMap<Integer, Class<?>> clientServices = new HashMap<>();
	
	private static GameServiceConfig instance;
	
	private GameServiceConfig() {
		clientServices.put(0, Pinglet.class);
		clientServices.put(GameMsgModuleConst.GM_SERVICE, GMGameService.class);
		clientServices.put(GameMsgModuleConst.ROOM_SERVICE, RoomService.class);
		clientServices.put(GameMsgModuleConst.FIGHT_SERVICE, FightService.class);
	}

	public static GameServiceConfig getInstance() {
		if (instance == null) {
			instance = new GameServiceConfig();
		}
		
		return instance;
	}

	public HashMap<Integer, Class<?>> getServices() {
		return clientServices;
	}
	
}
