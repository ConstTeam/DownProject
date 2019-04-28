package redis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * redis session
 *
 */
public class RedisSession {

	private static Logger logger = LoggerFactory.getLogger(RedisSession.class);
	/**
	 * redis �ͻ������ӳ�
	 */
	private JedisPool jedisPool;
	/**
	 * ��ʼ��redis�ͻ���
	 * 
	 * @param addr
	 * @param port
	 * @param timeout
	 * @param password ����Ϊnull
	 * @param config ����Ϊnull
	 * @return
	 */
	public boolean init(String addr, int port, int timeout, String password, int dbIndex, GenericObjectPoolConfig config) {
		if (jedisPool == null) {
			try {
				if (config == null) {
					config = new GenericObjectPoolConfig();
				}

				if (password == null || password.length() <= 0) {
					jedisPool = new JedisPool(config, addr, port, timeout, null, dbIndex);
				} else {
					jedisPool = new JedisPool(config, addr, port, timeout, password, dbIndex);
				}
				logger.info("jedis pool initialize, addr: {}, port: {}", addr, port);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * ��ȡredis�����ӿͻ��� ��ȡ����ʹ��֮����Ҫ�رն���, ����close�ӿڼ���
	 * 
	 * @return
	 */
	public Jedis getJedis() {
		try {
			if (jedisPool != null) {
				Jedis jedis = jedisPool.getResource();
				if (jedis == null) {
					logger.info("jedis pool resource lack");
				}
				return jedis;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public String getString(String key) {
		Jedis jedis = getJedis();
		if (jedis != null) {
			try {
				return jedis.get(key);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				jedis.close();
			}
		}
		return null;
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public byte[] getBytes(String key) {
		Jedis jedis = getJedis();
		if (jedis != null) {
			try {
				return jedis.get(key.getBytes());
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				jedis.close();
			}
		}
		return null;
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean setString(String key, String value) {
		Jedis jedis = getJedis();
		if (jedis != null) {
			try {
				return jedis.set(key, (String) value) != null;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				jedis.close();
			}
		}
		return false;
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean setBytes(String key, byte[] value) {
		Jedis jedis = getJedis();
		if (jedis != null) {
			try {
				return jedis.set(key.getBytes(), value) != null;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				jedis.close();
			}
		}
		return false;
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean setString(String key, String value, int expireSeconds) {
		Jedis jedis = getJedis();
		if (jedis != null) {
			try {
				if (jedis.set(key, (String) value) == null) {
					return false;
				}

				if (expireSeconds > 0) {
					jedis.expire(key, expireSeconds);
				}
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				jedis.close();
			}
		}
		return false;
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean setBytes(String key, byte[] value, int expireSeconds) {
		Jedis jedis = getJedis();
		if (jedis != null) {
			try {
				if (jedis.set(key.getBytes(), value) == null) {
					return false;
				}

				if (expireSeconds > 0) {
					jedis.expire(key.getBytes(), expireSeconds);
				}
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				jedis.close();
			}
		}
		return false;
	}

	/**
	 * ����key
	 * 
	 * @param key
	 * @return
	 */
	public boolean exists(String key) {
		Jedis jedis = getJedis();
		if (jedis != null) {
			try {
				return jedis.exists(key);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				jedis.close();
			}
		}
		return false;
	}

	/**
	 * ɾ��key
	 * 
	 * @param key
	 * @return
	 */
	public boolean delete(String key) {
		Jedis jedis = getJedis();
		if (jedis != null) {
			try {
				return jedis.del(key) > 0;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				jedis.close();
			}
		}
		return false;
	}
}
