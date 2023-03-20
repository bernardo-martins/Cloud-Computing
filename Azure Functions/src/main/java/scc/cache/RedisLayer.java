package scc.cache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

public class RedisLayer {
	RedisCache cache;

	private static ObjectMapper mapper = new ObjectMapper();

	public RedisLayer(RedisCache cache) {
		this.cache = cache;
	}

	public static String postResource(String key, String serialized, String prefix, String mostRecent,
			String numResource) throws JsonProcessingException {

		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			jedis.set(prefix + ":" + key, serialized);

			Long cnt = jedis.lpush(mostRecent, serialized);
			if (cnt > 5) {
				jedis.ltrim(mostRecent, 0, 4);

			}
			if (cnt < 5)
				cnt = jedis.incr(numResource);

			return key;
		}
	}

	public static String postResourceWoUpdate(String key, String serialized, String prefix, String mostRecent,
			String numResource) throws JsonProcessingException {

		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			jedis.set(prefix + ":" + key, serialized);

			jedis.incr(numResource);

			return key;
		}
	}

	public static int getResourceWoUpdate(String id, String prefix, String mostRecent, String numResources)
			throws JsonProcessingException {

		try (Jedis jedis = RedisCache.getCachePool().getResource()) {

			String cacheRes = jedis.get(prefix + ":" + id);

			if (cacheRes == null)
				throw new WebApplicationException(Status.NOT_FOUND);

			return Integer.valueOf(cacheRes);

		}

	}

	public static long postCounter(String key) {

		Jedis jedis = RedisCache.getCachePool().getResource();
		return jedis.incr(key);

	}

	public static byte[] getResource(String id, String prefix, String mostRecent, String numResources)
			throws JsonProcessingException {
		byte[] bytes = null;
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {

			String cacheRes = jedis.get(prefix + ":" + id);
			if (cacheRes == null) {

				return null;

			} else {
				bytes = mapper.readValue(cacheRes, byte[].class);
			}

			Long cnt = jedis.lpush(mostRecent, mapper.writeValueAsString(bytes));
			if (cnt > 5) {
				jedis.ltrim(mostRecent, 0, 4);
			}
			if (cnt < 5)
				cnt = jedis.incr(numResources);

			if (bytes == null)
				throw new WebApplicationException(Status.NOT_FOUND);
			return bytes;

		}
	}

	public static void deleteResource(String prefix, int trendingLimit) throws JsonProcessingException {
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {

			Map<String, String> allResources = getAll(prefix, 200000);
			Iterator<Entry<String, String>> it = allResources.entrySet().iterator();
			Entry<String, String> entry = null;
			while (it.hasNext()) {
				entry = it.next();
				if (Integer.valueOf(entry.getValue()) < trendingLimit) {
					jedis.del(entry.getKey());
					jedis.decr(entry.getKey());
				}
			}
		}
	}

	public static boolean resourceExists(String id, String prefix) throws JsonProcessingException {

		try (Jedis jedis = RedisCache.getCachePool().getResource()) {

			String cacheRes = jedis.get(prefix + ":" + id);
			return cacheRes != null;

		}

	}

	public static Map<String, String> getAll(String prefix, int maxSearch) {
		Map<String, String> map = new HashMap<String, String>();

		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			ScanParams scanParam = new ScanParams();
			String cursor = "0";
			scanParam.match(prefix + ":*");
			scanParam.count(maxSearch);
			do {
				ScanResult<Map.Entry<String, String>> hscan = jedis.hscan(prefix + ":*", cursor, scanParam);
				for (Map.Entry<String, String> ent : hscan.getResult())
					map.put(ent.getKey(), ent.getValue());
				cursor = hscan.getCursor();
			} while ("0".equals(cursor));
		}
		return map;

	}

	public static void deleteResource(String id, String resource) {
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			jedis.del(id);

			jedis.decr(id);
		}

	}
}
