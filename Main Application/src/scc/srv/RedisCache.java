package scc.srv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import scc.utils.AzureProperties;
import scc.utils.Session;

public class RedisCache {
	private static final String RedisHostname = System.getenv(AzureProperties.REDIS_HOSTNAME);
	//private static final String  = System.getenv(AzureProperties.REDIS_KEY);
	private static JedisPool instance;
	private static ObjectMapper mapper = new ObjectMapper();

	public synchronized static JedisPool getCachePool() {
		if (instance != null)
			return instance;
		final JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(128);
		poolConfig.setMaxIdle(128);
		poolConfig.setMinIdle(16);
		poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setNumTestsPerEvictionRun(3);
		poolConfig.setBlockWhenExhausted(true);
		instance = new JedisPool(new JedisPoolConfig(), RedisHostname, 6379, 1000);
		return instance;
	}

	public JedisPool getInstance() {
		if (instance != null)
			return instance;
		return new JedisPool(new JedisPoolConfig(), RedisHostname, 6379, 1000);
	}

	public static void putSession(Session s) throws JsonProcessingException {
		
		try (Jedis jedis = getCachePool().getResource()) {
		jedis.set("session:" + s.getID(), mapper.writeValueAsString(s));
		jedis.lpush("Sessions", mapper.writeValueAsString(s));
		jedis.expire(mapper.writeValueAsBytes(s), (long) 3600);
		
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static Session getSession(String cookieValue)
			throws CacheException, JsonMappingException, JsonProcessingException {
		try (Jedis jedis = getCachePool().getResource()) {
		String cacheRes = jedis.get("session:" + cookieValue);

		if (cacheRes == null) {
			throw new CacheException();
		}
		Session s = mapper.readValue(cacheRes, Session.class);
		return s;
		}catch(Exception e) {
			throw new CacheException();
			
		}
		
	}


}
