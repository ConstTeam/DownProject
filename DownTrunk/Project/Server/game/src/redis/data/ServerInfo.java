package redis.data;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import app.ServerStaticInfo;
import util.ErrorPrint;

public class ServerInfo {

	/**
	 * ������id
	 */
	private String serverId;
	/**
	 * ��Ӧ������ַ
	 */
	private String ip;
	/**
	 * ����˿�
	 */
	private int port;
	/**
	 * �������Ƿ񱻹���
	 */
	private int suspend;

	public ServerInfo() {
	}

	public ServerInfo(int type) {
		setServerId(ServerStaticInfo.getServerId());
		setIp(ServerStaticInfo.getPublicIp());
		setPort(ServerStaticInfo.getAddress(ServerStaticInfo.GAME_INTERNET).getPort());
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getSuspend() {
		return suspend;
	}

	public void setSuspend(int suspend) {
		this.suspend = suspend;
	}

	public JSONObject toJson() {
		JSONObject json = (JSONObject) JSON.toJSON(this);
		return json;
	}

	public static ServerInfo fromJson(String jsonInfo) {
		try {
			JSONObject json = JSON.parseObject(jsonInfo);
			return JSON.toJavaObject(json, ServerInfo.class);
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
