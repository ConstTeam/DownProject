package net;

public interface ISendData {
	/** 从nio中发出消息 */
	public int sendDataImpl(byte abyte0[], int i, int j);

	/** 是否忙 */
	public boolean isIsbusy();

	/** 设置是否繁忙 */
	public void setIsbusy(boolean isbusy);

	public void close();
}
