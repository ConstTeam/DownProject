package module.quest;

public class SignInQuest {

	/** �п���ȡǩ������ */
	private boolean canReceive;
	/** ǩ������ */
	private int signInDay;
	/** �ֻ��� */
	private String phoneNumber;
	
	public boolean getCanReceive() {
		return canReceive;
	}

	public void setCanReceive(boolean canReceive) {
		this.canReceive = canReceive;
	}

	public int getSignInDay() {
		return signInDay;
	}

	public void setSignInDay(int signInDay) {
		this.signInDay = signInDay;
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

}