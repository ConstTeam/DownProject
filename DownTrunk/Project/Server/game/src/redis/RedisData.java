package redis;

import java.util.Map;

import redis.data.ServerInfo;

public class RedisData {

	/**
	 * 当前缓存的整个列表
	 */
	public static Map<String, ServerInfo> cachedServerInfo;
}
