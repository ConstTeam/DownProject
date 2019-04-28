package module.quest;

public class SignInQuest {

	/** 有可领取签到奖励 */
	private boolean canReceive;
	/** 签到天数 */
	private int signInDay;
	/** 手机号 */
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