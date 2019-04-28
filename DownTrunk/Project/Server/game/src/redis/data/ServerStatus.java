package redis.data;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import app.ServerStaticInfo;
import sys.GameRoomManager;
import util.ErrorPrint;
import util.TimeFormat;
import util.Tools;

public class ServerStatus {
	/**
	 * 服务器id
	 */
	private String serverId;
	/**
	 * 房间数量
	 */
	private int roomCount;
	/**
	 * 上次心跳时间
	 */
	private int lastHeartbeat;

	public ServerStatus() {
		setServerId(ServerStaticInfo.getServerId());
		setLastHeartbeat(TimeFormat.getTimeInSeconds());
		setRoomCount(GameRoomManager.getInstance().getRoomCount());
	}

	public ServerStatus(String serverId) {
		this.serverId = serverId;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public int getRoomCount() {
		return roomCount;
	}

	public void setRoomCount(int roomCount) {
		this.roomCount = roomCount;
	}

	public int getLastHeartbeat() {
		return lastHeartbeat;
	}

	public void setLastHeartbeat(int lastHeartbeat) {
		this.lastHeartbeat = lastHeartbeat;
	}

	public boolean idIsValid() {
		return !Tools.isEmptyString(serverId);
	}

	public JSONObject toJson() {
		JSONObject json = (JSONObject) JSON.toJSON(this);
		return json;
	}

	public static ServerStatus fromJson(String jsonInfo) {
		try {
			JSONObject json = JSON.parseObject(jsonInfo);
			return JSON.toJavaObject(json, ServerStatus.class);
		} catch (Exception e) {
			ErrorPrint.print(e);
		}
		return null;
	}

	@Override
	public String toString() {
		return toJson().toJSONString();
	}
}
