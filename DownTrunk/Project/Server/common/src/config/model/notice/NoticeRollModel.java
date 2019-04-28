package config.model.notice;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import util.ErrorPrint;

/**
 * 滚动公告
 * 
 */
public class NoticeRollModel {
	/**
	 * 公告id
	 */
	public int id;
	/**
	 * 优先级,数字越大优先级越高
	 */
	public int order;
	/**
	 * 文本内容
	 */
	public String content;
	/**
	 * 次数
	 */
	public int count;
	/**
	 * 间隔时间(秒)
	 */
	public int interval;
	/**
	 * 开始时间:yyyy-MM-dd HH:mm:ss
	 */
	public String startTime;
	/**
	 * 结束时间:yyyy-MM-dd HH:mm:ss
	 */
	public String endTime;
	/**
	 * 开始时间
	 */
	public transient long startTimeInMillis;
	/**
	 * 结束时间
	 */
	public transient long endTimeInMillis;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public long getStartTimeInMillis() {
		return startTimeInMillis;
	}

	public void setStartTimeInMillis(long startTimeInMillis) {
		this.startTimeInMillis = startTimeInMillis;
	}

	public long getEndTimeInMillis() {
		return endTimeInMillis;
	}

	public void setEndTimeInMillis(long endTimeInMillis) {
		this.endTimeInMillis = endTimeInMillis;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public JSONObject toJson() {
		JSONObject json = (JSONObject) JSON.toJSON(this);
		return json;
	}

	public static NoticeRollModel fromJson(String jsonInfo) {
		try {
			JSONObject json = JSON.parseObject(jsonInfo);
			return JSON.toJavaObject(json, NoticeRollModel.class);
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
