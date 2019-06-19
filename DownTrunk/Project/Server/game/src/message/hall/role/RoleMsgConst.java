package message.hall.role;

public class RoleMsgConst {
	
	/* ------------------ 接收 ------------------*/
	/** 修改头像 */
	public static final int CHANGE_SCENE = 1;
	/** 修改角色 */
	public static final int CHANGE_ROLE = 2;
	
	/* ------------------ 发送 ------------------*/
	/** 同步角色信息 */
	public static final int ROLE_INFO_RES = 1;
	/** 修改场景结果 */
	public static final int CHANGE_SCENE_RES = 2;
	/** 修改角色结果 */
	public static final int CHANGE_ROLE_RES = 3;
	/** 同步角色金币 */
	public static final int ROLE_GOLD_SYNC = 4;
	/** 同步角色列表 */
	public static final int ROLE_LIST_SYNC = 5;
}
