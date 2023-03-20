package scc.srv;

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

	@SuppressWarnings("deprecation")
	public static String postResource(String key, String serialized, String prefix, String mostRecent,
			String numResource) throws JsonProcessingException {

		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			jedis.set(prefix + ":" + key, serialized);
			jedis.expire(prefix + ":" + key, 15);
			jedis.lpush(mostRecent, serialized);

			jedis.incr(numResource);
			return key;
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String postResourceWoUpdate(String key, String serialized, String prefix, String mostRecent,
			String numResource) throws JsonProcessingException {

		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			jedis.set(prefix + ":" + key, serialized);

			jedis.incr(numResource);

			return key;
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static int getResourceWoUpdate(String id, String prefix, String mostRecent, String numResources)
			throws JsonProcessingException {

		try (Jedis jedis = RedisCache.getCachePool().getResource()) {

			String cacheRes = jedis.get(prefix + ":" + id);

			if (cacheRes == null)
				throw new WebApplicationException(Status.NOT_FOUND);

			return Integer.valueOf(cacheRes);

		}catch(Exception e) {
			e.printStackTrace();
			return -1;
		}

	}

	public static long postCounter(String key) throws JsonProcessingException {
		
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
		return jedis.incr(key);
		}catch(Exception e) {
			e.printStackTrace();
			return 0;
		}
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

			jedis.lpush(mostRecent, mapper.writeValueAsString(bytes));

			jedis.incr(numResources);

			if (bytes == null)
				throw new WebApplicationException(Status.NOT_FOUND);
			return bytes;

		}catch(Exception e) {
			e.printStackTrace();
			return null;
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
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean resourceExists(String id, String prefix) throws JsonProcessingException {

		try (Jedis jedis = RedisCache.getCachePool().getResource()) {

			String cacheRes = jedis.get(prefix + ":" + id);
			return cacheRes != null;

		}catch(Exception e) {
			e.printStackTrace();
			return false;
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
		}catch(Exception e) {
			e.printStackTrace();
		}
		return map;

	}

	public static void deleteResource(String id, String resource) {
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			jedis.del(id);

			jedis.decr(id);
		}catch(Exception e) {
			e.printStackTrace();
		}

	}
}
