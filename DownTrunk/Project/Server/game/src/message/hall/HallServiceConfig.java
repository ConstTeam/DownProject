package message.hall;

import java.util.HashMap;

import message.Pinglet;
import message.hall.guide.GuideService;
import message.hall.login.LoginService;
import message.hall.quest.QuestService;
import message.hall.role.RoleService;

public class HallServiceConfig {

	private HashMap<Integer, Class<?>> clientServices = new HashMap<>();

	private static HallServiceConfig instance;

	private HallServiceConfig() {
		clientServices.put(0, Pinglet.class);
		clientServices.put(HallMsgModuleConst.LOGIN_SERVICE, LoginService.class);
		clientServices.put(HallMsgModuleConst.ROLE_SERVICE, RoleService.class);
		clientServices.put(HallMsgModuleConst.QUEST_SERVICE, QuestService.class);
		clientServices.put(HallMsgModuleConst.GM_SERVICE, GMHallService.class);
		clientServices.put(HallMsgModuleConst.GUIDE_SERVICE, GuideService.class);
	}

	public static HallServiceConfig getInstance() {
		if (instance == null) {
			instance = new HallServiceConfig();
		}

		return instance;
	}

	public HashMap<Integer, Class<?>> getServices() {
		return clientServices;
	}

}
