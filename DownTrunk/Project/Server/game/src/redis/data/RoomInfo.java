package redis.data;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import util.ErrorPrint;
import util.TimeFormat;
import util.Tools;

public class RoomInfo {

	/** ����id */
	private int roomId;
	/** ������id */
	private String serverId;
	/** ����ʱ�� */
	private String createTime;
	/** ���� */
	private String rule;
	/** �������� */
	private int type;
	/** ���Ӳ��� */
	private int arg1;
	/** ����״̬ */
	private int state;
	
	public RoomInfo() {
	}

	public RoomInfo(String serverId) {
		this.serverId = serverId;
		createTime = TimeFormat.getTime();
	}

	public int getRoomId() {
		return roomId;
	}

	public String getServerId() {
		return serverId;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public void randomRoomId() {
		this.roomId = Tools.random(100000, 300000);
	}

	public JSONObject toJson() {
		JSONObject json = (JSONObject) JSON.toJSON(this);
		return json;
	}

	public static RoomInfo fromJson(String jsonInfo) {
		try {
			JSONObject json = JSON.parseObject(jsonInfo);
			return JSON.toJavaObject(json, RoomInfo.class);
		} catch (Exception e) {
			ErrorPrint.print(e);
		}
		return null;
	}

	@Override
	public String toString() {
		return toJson().toJSONString();
	}

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	public int getArg1() {
		return arg1;
	}

	public void setArg1(int arg1) {
		this.arg1 = arg1;
	}


	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
