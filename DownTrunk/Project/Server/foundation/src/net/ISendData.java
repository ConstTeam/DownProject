package net;

public interface ISendData {
	/** ��nio�з�����Ϣ */
	public int sendDataImpl(byte abyte0[], int i, int j);

	/** �Ƿ�æ */
	public boolean isIsbusy();

	/** �����Ƿ�æ */
	public void setIsbusy(boolean isbusy);

	public void close();
}
