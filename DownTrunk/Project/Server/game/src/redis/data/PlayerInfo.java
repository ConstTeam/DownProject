package redis.data;

import java.util.Calendar;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import util.ErrorPrint;
import util.Tools;

/**
 * Redis角色数据模型
 *
 */
public class PlayerInfo {

	/** 玩家id */
	private int playerId;
	/** 最后登录设备Id */
	private String deviceId;
	/** 最后登录服务器Id */
	private String serverId;
	/** 房间Id */
	private int roomId;
	/** 最后登录时间 */
	private String lastLoginTime;
	/** 最后下线时间 */
	private String lastLogoutTime;
	/** 玩家在线状态 */
	private boolean online = false;
	/** 昵称 */
	private String nickname;
	/** 用户性别，1为男性，2为女性 */
	private int sex = 1;
	/** 头像 */
	private String icon;
	/** 场景 */
	private int sceneId = 0;
	/** 签名 */
	private String sign = "";
	/** 钻石 */
	private transient int diamond;
	/** 最终分配时间 */
	private transient Calendar assignTime;
	/** 金币 */
	private transient int gold;
	/** 卡组Id */
	private String deckId;

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void randomPlayerId() {
		this.playerId = Tools.random(100000, 999999);
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(String lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	public int getRoomId() {
		return roomId;
	}

	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}

	public JSONObject toJson() {
		JSONObject json = (JSONObject) JSON.toJSON(this);
		return json;
	}

	public static PlayerInfo fromJson(String jsonInfo) {
		try {
			JSONObject json = JSON.parseObject(jsonInfo);
			return JSON.toJavaObject(json, PlayerInfo.class);
		} catch (Exception e) {
			ErrorPrint.print(e);
		}
		return null;
	}

	@Override
	public String toString() {
		return toJson().toJSONString();
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public int getDiamond() {
		return diamond;
	}

	public void setDiamond(int diamond) {
		this.diamond = diamond;
	}

	public int getGold() {
		return gold;
	}

	public void setGold(int gold) {
		this.gold = gold;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getLastLogoutTime() {
		return lastLogoutTime;
	}

	public void setLastLogoutTime(String lastLogoutTime) {
		this.lastLogoutTime = lastLogoutTime;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public String getDeckId() {
		return deckId;
	}

	public void setDeckId(String deckId) {
		this.deckId = deckId;
	}

	public Calendar getAssignTime() {
		return assignTime;
	}

	public void setAssignTime(Calendar assignTime) {
		this.assignTime = assignTime;
	}

	public int getSceneId() {
		return sceneId;
	}

	public void setSceneId(int sceneId) {
		this.sceneId = sceneId;
	}
}
