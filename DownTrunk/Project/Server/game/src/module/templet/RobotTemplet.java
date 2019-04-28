package module.templet;

import module.scene.RoomConst;

public class RobotTemplet extends TempletBase {
	
	public RobotTemplet(int robotId) {
		this.type = RoomConst.ROOM_TYPE_ROBOT;
		this.arg1 = robotId;
	}
}
