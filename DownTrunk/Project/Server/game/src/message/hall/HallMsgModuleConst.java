package message.hall;

public class HallMsgModuleConst {

	/* ------------------ 接收客户端请求的模块号 ------------------*/
	/** 大厅服-登录服务 */
	public static final int LOGIN_SERVICE = 1;
	/** 大厅服-角色服务 */
	public static final int ROLE_SERVICE = 2;
	/** 大厅服-GM服务 */
	public static final int GM_SERVICE = 4;
	/** 大厅服-任务服务 */
	public static final int QUEST_SERVICE = 6;
	/** 大厅服-指引服务 */
	public static final int GUIDE_SERVICE = 7;
	
	
	/* ------------------ 发送给客户端的模块号 ------------------*/
	/** 大厅服-登录服务 */
	public static final int LOGIN_RESPONSE = 1;
	/** 大厅服-角色服务 */
	public static final int ROLE_RESPONSE = 2;
	/** 大厅服-GM服务 */
	public static final int GM_RESPONSE = 4;
	/** 大厅服-任务服务 */
	public static final int QUEST_RESPONSE = 6;
	/** 大厅服-任务服务 */
	public static final int GUIDE_RESPONSE = 7;
	
}
