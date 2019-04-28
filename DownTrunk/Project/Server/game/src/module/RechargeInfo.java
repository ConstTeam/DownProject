package module;

import java.math.BigInteger;
import java.util.Calendar;

import net.ISession;

/**
 * ��ֵ
 *
 */
public class RechargeInfo {

	/** ��ˮ�� */
	private BigInteger lid;
	/** ��ɫId */
	private int playerId;
	/** �ʺ� */
	private String accountId;
	/** ƽ̨�ʺ� */
	private String platformId;
	/** OrderId ������� */
	private String orderId;
	/** ��ֵ���� */
	private String payCode;

	/** ��ֵ��� */
	private String price;

	private boolean deal;
	/** ��ֵ��ʯ�� */
	private int diamond;

	private ISession session;

	private Calendar time;

	private int retimes;

	/** ����ǳ� */
	private String nickname;
	/** �Ƿ�ɹ� */
	private int isSucc;

	/** ƽ̨ */
	private String platform;
	/** ���� */
	private String channel;

	/**
	 * ��ֵ������ѯ��
	 * 
	 * @param playerId
	 * @param accountId
	 * @param orderId
	 * @param payCode
	 * @param price
	 * @param isSucc
	 * @param nickname
	 * @param time
	 */
	public RechargeInfo(int playerId, String accountId, String orderId, String payCode, String price, int isSucc,
			String nickname, Calendar time) {
		this.playerId = playerId;
		this.accountId = accountId;
		this.payCode = payCode;
		this.orderId = orderId;
		this.price = price;
		this.time = time;
		this.nickname = nickname;
		this.isSucc = isSucc;
	}

	/**
	 * ��ֵ�ص����ɶ�����
	 * 
	 * @param playerId
	 * @param orderId
	 * @param payCode
	 * @param price
	 * @param diamond
	 * @param platform
	 * @param channel
	 * @param serverId
	 */
	public RechargeInfo(int playerId, String orderId, String payCode, String price, int diamond, String platform,
			String channel) {
		this.playerId = playerId;
		this.setAccountId("");
		this.payCode = payCode;
		this.orderId = orderId;
		this.price = price;
		this.diamond = diamond;
		this.deal = false;
		this.time = Calendar.getInstance();
		this.session = null;
		this.retimes = 0;
		this.platform = platform;
		this.channel = channel;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public String getPayCode() {
		return payCode;
	}

	public void setPayCode(String payCode) {
		this.payCode = payCode;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public boolean isDeal() {
		return deal;
	}

	public void setDeal(boolean deal) {
		this.deal = deal;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public ISession getSession() {
		if (session.isClosed()) {
			return null;
		}
		return session;
	}

	public void setSession(ISession session) {
		this.session = session;
	}

	public Calendar getTime() {
		return time;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public int getDiamond() {
		return diamond;
	}

	public void setDiamond(int diamond) {
		this.diamond = diamond;
	}

	public int getRetimes() {
		return retimes;
	}

	public void addRetimes() {
		this.retimes += 1;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getPlatformId() {
		return platformId;
	}

	public void setPlatformId(String platformId) {
		this.platformId = platformId;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public int getIsSucc() {
		return isSucc;
	}

	public void setIsSucc(int isSucc) {
		this.isSucc = isSucc;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public BigInteger getLid() {
		return lid;
	}

	public void setLid(BigInteger lid) {
		this.lid = lid;
	}
}
