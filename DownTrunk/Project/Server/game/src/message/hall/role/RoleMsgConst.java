package message.hall.role;

public class RoleMsgConst {
	
	/* ------------------ 接收 ------------------*/
	/** 修改角色 */
	public static final int CHANGE_ROLE = 1;
	/** 修改头像 */
	public static final int CHANGE_SCENE = 2;
	
	/* ------------------ 发送 ------------------*/
	/** 同步角色信息 */
	public static final int ROLE_INFO_RES = 1;
	/** 同步角色金币 */
	public static final int ROLE_GOLD_SYNC = 2;
	/** 修改角色结果 */
	public static final int CHANGE_ROLE_RES = 3;
	/** 修改场景结果 */
	public static final int CHANGE_SCENE_RES = 4;
	/** 同步角色列表 */
	public static final int ROLE_LIST_SYNC = 5;
	/** 同步场景列表 */
	public static final int SCENE_LIST_SYNC = 6;
}
