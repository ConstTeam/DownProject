package message;

import config.ConfigData;

public class ErrorCode {
	
	public static final int ERROR = ConfigData.errorCode.get("ERROR");
	/* ------------------ 数据库模块------------------*/
	/** 连接池已满 */
	public static final int CONN_POOL_FULL = ConfigData.errorCode.get("CONN_POOL_FULL");

	/* ------------------ 登录模块------------------*/
	/** 密码错误 */
	public static final int WRONG_PASSWORD = ConfigData.errorCode.get("WRONG_PASSWORD");
	
	/* ------------------ 角色模块------------------*/
	/** 该玩家不存在 */
	public static final int ROLE_NOT_EXIST = ConfigData.errorCode.get("ROLE_NOT_EXIST");
	
}
