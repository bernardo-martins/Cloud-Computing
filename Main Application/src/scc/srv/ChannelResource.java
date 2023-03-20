package scc.srv;

import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;

import scc.resources.Channel;
import scc.resources.ChannelDAO;
import scc.resources.Message;
import scc.resources.MessageDAO;
import scc.resources.User;
import scc.resources.UserDAO;
import scc.utils.AzureProperties;
import scc.utils.Session;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.bson.Document;

/**
 * Resource for managing media files, such as images.
 */
@Path("/channels")
public class ChannelResource {
	String storageConnectionString = System.getenv(AzureProperties.COSMOS_DB_CONNECTION_STRING);
	ObjectMapper mapper = new ObjectMapper();



	public Session checkCookieUser(Cookie session)
			throws NotAuthorizedException, CacheException, JsonMappingException, JsonProcessingException {
		if (session == null || session.getValue() == null)
			throw new NotAuthorizedException("No session initialized");
		Session s;
		try {

			s = RedisCache.getSession(session.getValue());
		} catch (CacheException e) {
			throw new NotAuthorizedException("No valid session initialized");
		}
		if (s == null || s.getUser() == null || s.getUser().getId().length() == 0)
			throw new NotAuthorizedException("No valid session initialized");

		return s;
	}

	public Session checkCookieUser(Cookie session, String id)
			throws NotAuthorizedException, CacheException, JsonMappingException, JsonProcessingException {
		if (session == null || session.getValue() == null)
			throw new NotAuthorizedException("No session initialized");
		Session s;
		try {

			s = RedisCache.getSession(session.getValue());
		} catch (CacheException e) {
			throw new NotAuthorizedException("No valid session initialized");
		}
		if (s == null || s.getUser() == null || s.getUser().getId().length() == 0)
			throw new NotAuthorizedException("No valid session initialized");
		if (!s.getUser().getId().equals(id) && !s.getUser().equals("admin"))
			throw new NotAuthorizedException("Invalid user : " + s.getUser());
		return s;
	}

	public void getTrendingChannels() throws JsonProcessingException {

		return;
	}

	@GET
	@Path("/{channelId}/messages")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Message> getMessagesInChannel(@CookieParam("scc:session") Cookie session,
			@PathParam("channelId") String channelId, @QueryParam("st") Integer st, @QueryParam("len") Integer len)
			throws JsonMappingException, NotAuthorizedException, JsonProcessingException, CacheException,
			WebApplicationException {

		if (checkCookieUser(session) == null) {
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		try {

			String offset = "";
			String limit = "";
			if (st != null)
				offset = " OFFSET " + st;
			if (len != null)
				limit = " LIMIT " + len;

			List<Document> iterable =  MongoDBLayer.getInstance().getMessagesFromChannel(channelId,
					offset, limit);

			List<Message> msgs = new ArrayList<Message>();
			for (Document msg : iterable)
				msgs.add(mapper.readValue(msg.toJson(), MessageDAO.class));
			if (msgs.isEmpty())
				throw new WebApplicationException(Status.NO_CONTENT);

			return msgs;

		} catch (Exception e) {
			throw e;
		}

	}

	// podiamos ter os trending channels em cache...
	// trending channels eram updated

	public static Channel getChannelPriv(String id) throws WebApplicationException {
		List<Document> res = MongoDBLayer.getInstance().getChById(id);
		Channel ch = null;
		ObjectMapper mapperlocal = new ObjectMapper();
		for (Document u : res) {
			try {
				ch = mapperlocal.readValue(u.toJson(), ChannelDAO.class);
			} catch (Exception e) {

				e.printStackTrace();
			}
			break;
		}
		if (ch != null)
			return ch;

		throw new WebApplicationException(Status.NOT_FOUND);

	}

	@POST
	@Path("/create/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Channel createChannel(@CookieParam("scc:session") Cookie session, Channel channel)
			throws WebApplicationException, JsonMappingException, JsonProcessingException, CacheException {
		if (checkCookieUser(session) == null) {
			throw new WebApplicationException(Status.FORBIDDEN);
		}

		channel.setId("" + RedisLayer.postCounter("idChannel"));
		MongoDBLayer.getInstance().putCh(channel);
		// CUIDADO COM ESTE CODE
		return channel;
	}

	@DELETE
	@Path("/delete/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Channel deleteChannel(@CookieParam("scc:session") Cookie session, @PathParam("id") String id)
			throws WebApplicationException, JsonMappingException, JsonProcessingException, CacheException {

		if (checkCookieUser(session, id) == null) {
			throw new WebApplicationException(Status.FORBIDDEN);
		}

		long res = MongoDBLayer.getInstance().delChById(id);

		if (res>0) {
			return (Channel) getChannelPriv(id);
		} else {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}

	@PUT
	@Path("/update/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Channel updateChannel(@PathParam("id") String id, Channel channel) throws WebApplicationException {

		// CUIDADO COM ESTE CODE

		MongoDBLayer.getInstance().putCh(channel);
		return channel;
	}

	@GET
	@Path("/get/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Channel getChannel(@PathParam("id") String id) throws WebApplicationException {
		/*
		 * CosmosPagedIterable<ChannelDAO> res =
		 * CosmosDBLayer.getInstance().getChById(id); Channel ch = null; for (ChannelDAO
		 * u : res) { ch = u; break; } if (ch != null) return ch;
		 * 
		 * throw new WebApplicationException(Status.NOT_FOUND);
		 */

		return (Channel) getChannelPriv(id);
	}

	@POST
	@Path("/{channelId}/add/{user}")
	@Produces(MediaType.APPLICATION_JSON)
	public Channel addUserToChannel(@PathParam("channelId") String channelId, @PathParam("user") String userId)
			throws WebApplicationException {
		MongoDBLayer instance = MongoDBLayer.getInstance();

		Channel channel = null;
		try {
			channel = getChannelPriv(channelId);
		} catch (Exception e) {
			throw e;
		}

		List<Document> users = instance.getUserById(userId);
		User user = null;
		for (Document u : users) {
			try {
				user = mapper.readValue(u.toJson(), UserDAO.class);
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}

		if (user == null)
			throw new WebApplicationException(Status.NOT_FOUND);

		channel.addUser(userId);

		instance.replaceChannel(channel);

		return channel;
	}

	// removes every 24 hours
	@FunctionName("periodic-removal")
	public void cosmosFunction(@TimerTrigger(name = "periodicRemoval", schedule = "24 * * * * * *") String timerInfo,
			ExecutionContext context) throws JsonProcessingException {
		RedisLayer.deleteResource("activity", 10);
	}

}
