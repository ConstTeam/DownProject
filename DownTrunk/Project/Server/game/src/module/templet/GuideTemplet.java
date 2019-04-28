package module.templet;

import module.scene.RoomConst;

public class GuideTemplet extends TempletBase {
	
	public GuideTemplet(int guideId) {
		this.type = RoomConst.ROOM_TYPE_GUIDE;
		this.arg1 = guideId;
	}
}
