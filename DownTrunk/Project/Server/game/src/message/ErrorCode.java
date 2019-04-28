package message;

import config.ConfigData;

public class ErrorCode {
	
	public static final int ERROR = ConfigData.errorCode.get("ERROR");
	/* ------------------ ���ݿ�ģ��------------------*/
	/** ���ӳ����� */
	public static final int CONN_POOL_FULL = ConfigData.errorCode.get("CONN_POOL_FULL");

	/* ------------------ ��¼ģ��------------------*/
	/** ������� */
	public static final int WRONG_PASSWORD = ConfigData.errorCode.get("WRONG_PASSWORD");
	
	/* ------------------ ��ɫģ��------------------*/
	/** ����Ҳ����� */
	public static final int ROLE_NOT_EXIST = ConfigData.errorCode.get("ROLE_NOT_EXIST");
	
}
