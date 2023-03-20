package scc.serverless;

import java.util.*;

import com.microsoft.azure.functions.annotation.*;

import redis.clients.jedis.Jedis;
import scc.cache.RedisCache;
import resources.ChannelDAO;
import resources.MessageDAO;
import resources.User;
import resources.UserDAO;
import srvresources.CosmosDBLayer;

import com.azure.cosmos.util.CosmosPagedIterable;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger. These functions can be accessed at:
 * {Server_URL}/api/{route}
 * Complete URL appear when deploying functions.
 */
public class HttpFunction {
	@FunctionName("http-stats")
	public HttpResponseMessage run(@HttpTrigger(name = "req", 
										methods = {HttpMethod.GET }, 
										authLevel = AuthorizationLevel.ANONYMOUS,
										route = "serverless/stats") 
			HttpRequestMessage<Optional<String>> request,
			final ExecutionContext context) {
		StringBuffer result = new StringBuffer();
		result.append("Serverless stats: v. 0001 : \n");
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			Long vall = jedis.incr("cnt:http");
			result.append("HTTP functions called ");
			result.append(vall);
			result.append(" times.\n");

			String val = jedis.get("cnt:cosmos");
			if( val == null)
				val = "0";
			result.append("Cosmos functions called ");
			result.append(val);
			result.append(" times.\n");

			val = jedis.get("cnt:blob");
			if( val == null)
				val = "0";
			result.append("Blob functions called ");
			result.append(val);
			result.append(" times.\n");

			val = jedis.get("cnt:timer");
			if( val == null)
				val = "0";
			result.append("Timer functions called ");
			result.append(val);
			result.append(" times.\n");
		}
		return request.createResponseBuilder(HttpStatus.OK).body(result.toString()).build();
	}

	@FunctionName("get-redis")
	public HttpResponseMessage getRedis(@HttpTrigger(name = "req", 
											methods = {HttpMethod.GET }, 
											authLevel = AuthorizationLevel.ANONYMOUS, 
											route = "serverless/redis/{key}") 
				HttpRequestMessage<Optional<String>> request,
				@BindingName("key") String key, 
				final ExecutionContext context) {
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			jedis.incr("cnt:http");
			String val = jedis.get(key);
			return request.createResponseBuilder(HttpStatus.OK).body("GET key = " + key + "; val = " + val).build();
		}
	}

	@FunctionName("lrange-redis")
	public HttpResponseMessage lrangeRedis(@HttpTrigger(name = "req", 
											methods = {HttpMethod.GET }, 
											authLevel = AuthorizationLevel.ANONYMOUS, 
											route = "serverless/redis/lrange/{key}") 
				HttpRequestMessage<Optional<String>> request,
				@BindingName("key") String key, 
				final ExecutionContext context) {
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			jedis.incr("cnt:http");
			List<String> val = jedis.lrange(key, 0, -1);
			return request.createResponseBuilder(HttpStatus.OK).body("GET key = " + key + "; val = " + val).build();
		}
	}

	@FunctionName("set-redis")
	public HttpResponseMessage setRedis(@HttpTrigger(name = "req", 
											methods = {HttpMethod.POST }, 
											authLevel = AuthorizationLevel.ANONYMOUS, 
											route = "serverless/redis/{key}") 
				HttpRequestMessage<Optional<String>> request,
				@BindingName("key") String key, 
				final ExecutionContext context) {
		String val = request.getBody().orElse("");
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			jedis.incr("cnt:http");
			jedis.set(key, val);
			return request.createResponseBuilder(HttpStatus.OK).body("SET key = " + key + "; val = " + val).build();
		}
	}

	@FunctionName("echo")
	public HttpResponseMessage echo(@HttpTrigger(name = "req", 
										methods = {HttpMethod.GET }, 
										authLevel = AuthorizationLevel.ANONYMOUS, 
										route = "serverless/echo/{text}") 
				HttpRequestMessage<Optional<String>> request,
				@BindingName("text") String txt, 
				final ExecutionContext context) {
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			jedis.incr("cnt:http");
		}
		return request.createResponseBuilder(HttpStatus.OK).body(txt).build();
	}

	@FunctionName("echo-simple")
	public HttpResponseMessage echoSimple(@HttpTrigger(name = "req", 
											methods = {HttpMethod.GET }, 
											authLevel = AuthorizationLevel.ANONYMOUS, 
											route = "serverless/echosimple/{text}") 
				HttpRequestMessage<Optional<String>> request,
				@BindingName("text") String txt, 
				final ExecutionContext context) {
		return request.createResponseBuilder(HttpStatus.OK).body(txt).build();
	}
	
	
	public void updateMessages(User user) {

		CosmosDBLayer instance = CosmosDBLayer.getInstance();
		CosmosPagedIterable<MessageDAO> res = instance.getMessages(user.getId());

		for (MessageDAO msg : res) {
			msg.setSender("User deleted.");
			instance.putMsg(msg);
		}

	}
	
	public void updateChannels(UserDAO user) {

		for (String idChannel : user.getChannelIds()) {

			CosmosPagedIterable<ChannelDAO> res = CosmosDBLayer.getInstance().getChById(idChannel);

			for (ChannelDAO channel : res) {
				channel.removeUser(user.getId());
				CosmosDBLayer.getInstance().putCh(channel);
			}

		}

	}
	
	
	public void delChannelFromUsers(ChannelDAO ch) {

		for (String idUser : ch.getIdUsers()) {
			
			CosmosPagedIterable<UserDAO> res = CosmosDBLayer.getInstance().getUserById(idUser);

			for (UserDAO user : res) {
				user.removeChannelId(ch.getId());
				CosmosDBLayer.getInstance().putUser(user);
			}

		}

	}
	
	
	public void deleteAllMessagesFromAChannel(ChannelDAO ch) {
		
		CosmosPagedIterable<MessageDAO> res = CosmosDBLayer.getInstance().getMessagesFromChannel(ch.getId(), "", "");
		
		for(MessageDAO msg :res) {
			CosmosDBLayer.getInstance().delMsg(msg);
		}
		
	}
	
	
	
	
	@FunctionName("function")
	public HttpResponseMessage triggerDelUser(@HttpTrigger(name = "req", methods = {
			HttpMethod.DELETE }, authLevel = AuthorizationLevel.ANONYMOUS, route = "users/delete/{id}") HttpRequestMessage<Optional<String>> request,
			@BindingName("id") String id

	) {
		CosmosPagedIterable<UserDAO> res = CosmosDBLayer.getInstance().getUserById(id);
		UserDAO user = null;
		for (UserDAO u : res)
			user = u;

		updateChannels(user);
		updateMessages(user);

		return request.createResponseBuilder(HttpStatus.OK).header("Content-Type", "application/json")
				.body("Channels and Messages Updated!").build();
	}
	
	@FunctionName("function2")
	public HttpResponseMessage triggerDelChannel(@HttpTrigger(name = "req", methods = {
			HttpMethod.DELETE }, authLevel = AuthorizationLevel.ANONYMOUS, route = "channel/delete/{id}") HttpRequestMessage<Optional<String>> request,
			@BindingName("id") String id

	) {
		CosmosPagedIterable<ChannelDAO> res = CosmosDBLayer.getInstance().getChById(id);
		ChannelDAO ch = null;
		for (ChannelDAO c : res)
			ch = c;

		delChannelFromUsers(ch);
		deleteAllMessagesFromAChannel(ch);
		
		return request.createResponseBuilder(HttpStatus.OK).header("Content-Type", "application/json")
				.body("Channels and Messages Updated!").build();
	}
	
	
	
	
	
	
}
