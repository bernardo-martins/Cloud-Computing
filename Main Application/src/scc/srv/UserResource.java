package scc.srv;

import com.microsoft.azure.functions.HttpMethod;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import scc.resources.Channel;
import scc.resources.ChannelDAO;
import scc.resources.Message;
import scc.resources.MessageDAO;
import scc.resources.User;
import scc.resources.UserDAO;
import scc.utils.AzureProperties;
import scc.utils.Login;
import scc.utils.Session;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.bson.Document;
//mongo
/**
 * Resource for managing media files, such as images.
 */
@Path("/users")
public class UserResource {
	// Get connection string in the storage access keys page
	String storageConnectionString = System.getenv(AzureProperties.COSMOS_DB_CONNECTION_STRING);
	ObjectMapper mapper = new ObjectMapper();



	@GET
	@Path("/{user}/channels")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getMessage(@CookieParam("scc:session") Cookie session, @PathParam("user") String user)
			throws JsonMappingException, NotAuthorizedException, JsonProcessingException, CacheException,
			WebApplicationException {

		if (checkCookieUser(session) == null) {
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		try {

			User usr = getUserPrivate(user);
			return usr.getChannelIds();

		} catch (Exception e) {
			throw e;
		}

	}

	@POST
	@Path("/auth")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response auth(Login user) throws WebApplicationException, NotAuthorizedException, JsonMappingException, JsonProcessingException { // imported
		// core
		// one
		UserDAO userObj = null;
		try {
			userObj = getUserPrivate(user.getId());
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		if (user.getPass().equals(userObj.getPassword()) && user.getId().equals(userObj.getId())) {
			String uid = UUID.randomUUID().toString();
			NewCookie cookie = new NewCookie("scc:session", uid, "/", null, "sessionid", 3600, false, true);
			RedisCache.putSession(new Session(uid, userObj));
			return Response.ok().cookie(cookie).build();
		} else {
			throw new NotAuthorizedException("Incorrect login");
		}

	}

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
		if (!s.getUser().getId().equals(id) && !s.getUser().equals("admin")) // alterar para detetar admin
			throw new NotAuthorizedException("Invalid user : " + s.getUser());
		return s;
	}

	/**
	 * Return the contents of an image. Throw an appropriate error message if id
	 * does not exist.
	 */
	/*
	 * @GET
	 * 
	 * @Path("/{id}")
	 * 
	 * @Produces(MediaType.APPLICATION_OCTET_STREAM) public byte[]
	 * download(@PathParam("id") String id) { try { BlsobContainerClient client =
	 * getBlobContainerClient(); BlobClient blob = client.getBlobClient(id);
	 * BinaryData data = blob.downloadContent(); return data.toBytes(); } catch
	 * (Exception e) { throw new
	 * WebApplicationException(Status.INTERNAL_SERVER_ERROR); } }
	 */

	@POST
	@Path("/create")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public User createUser(User user) throws WebApplicationException, JsonProcessingException {
		try {
			getUserPrivate(user.getId());
			
		}catch(Exception e) {
			MongoDBLayer.getInstance().putUser(user);	
			return user;
		}

		throw new WebApplicationException(Status.FORBIDDEN);		

	
	}
	@POST
	@Path("/{user}/subscribe/{channelId}")
	public void subscribe(@PathParam("user") String userId, @PathParam("channelId") String channelId)
			throws WebApplicationException, JsonProcessingException {
		MongoDBLayer instance = MongoDBLayer.getInstance();

		List<Document> channelRes = instance.getChById(channelId);
		Channel channel = null;
		for (Document c : channelRes) {
			channel = mapper.readValue(c.toJson(), ChannelDAO.class);
			break;
		}
		if (channel != null) {
			List<Document> res = instance.getUserById(userId);
			User user = null;
			for (Document u : res) {
				user = mapper.readValue(u.toJson(), UserDAO.class);
				break;
			}
			user.addChannel(channelId);
			instance.replaceUser(user);
			/*
			 * update cache with n_subscribes de um channel com um timeout
			 */

			if (!RedisLayer.resourceExists(channelId, "activity"))
				RedisLayer.postResourceWoUpdate(channelId, "" + 1, "activity", "MostRecentActivity",
						"NumActivities" + channelId);
			else {
				int counter = RedisLayer.getResourceWoUpdate(channelId, "activity", "MostRecentActivity",
						"NumActivities" + channelId);

				RedisLayer.postResourceWoUpdate(channelId, "" + (Integer.valueOf(String.valueOf(counter)) + 1),
						"activity", "MostRecentActivity", "NumActivities" + channelId);
			}

		} else {
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

	}

	@DELETE
	@Path("/delete/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public User deleteUser(@CookieParam("scc:session") Cookie session, @PathParam("id") String id)
			throws WebApplicationException, JsonMappingException, JsonProcessingException, CacheException {
		if (checkCookieUser(session, id) == null) {
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		FileCreator fc = new FileCreator();
		List<Document> res = MongoDBLayer.getInstance().getUserById(id);
		User user = null;
		for (Document u : res)
			user = mapper.readValue(u.toJson(), UserDAO.class); ;

		if (user.getidImage() != null) {
			try {
				fc.removeFile(user.getidImage());
			}catch(IOException e) {
				
			}
			
		}

		Long resDel = MongoDBLayer.getInstance().delUser(user);
		if (resDel>0)
			return (User) user;
		else
			throw new WebApplicationException(Status.NOT_FOUND);

	}

	@GET
	@Path("/get/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public User get(@CookieParam("scc:session") Cookie session, @PathParam("id") String id)
			throws WebApplicationException, JsonMappingException, JsonProcessingException, CacheException {
		if (checkCookieUser(session) == null) {
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		try {

			return getUserPrivate(id);

		} catch (Exception e) {
			throw e;
		}

	}

	public static UserDAO getUserPrivate(String id)
			throws WebApplicationException, JsonMappingException, JsonProcessingException {

		UserDAO user = null;
		ObjectMapper mapperlocal = new ObjectMapper();
		List<Document> res = MongoDBLayer.getInstance().getUserById(id);
		for (Document u : res) {
			user = mapperlocal.readValue(u.toJson(), UserDAO.class);
		}
			

		if (user == null)
			throw new WebApplicationException(Status.NOT_FOUND);

		return user;

	}

	@PUT
	@Path("/update/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public User updateUser(@CookieParam("scc:session") Cookie session, User user)
			throws JsonMappingException, NotAuthorizedException, JsonProcessingException, CacheException {
		if (checkCookieUser(session) == null) {
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		MongoDBLayer.getInstance().putUser(user);

		return user;

	}

	// isto vai ser trigger/routine
	public void updateChannels(User user) {

		for (String idChannel : user.getChannelIds()) {

			List<Document> res = MongoDBLayer.getInstance().getChById(idChannel);

			for (Document u : res) {
				Channel channel=null;
				try {
					channel = mapper.readValue(u.toJson(), ChannelDAO.class);
				} catch (JsonMappingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				channel.removeUser(user.getId());
				MongoDBLayer.getInstance().putCh(channel);
			}

		}

	}

	public void updateMessages(User user) {

		MongoDBLayer instance = MongoDBLayer.getInstance();
		List<Document> res = instance.getMessages(user.getId());

		for (Document u : res) {
			Message msg= null;
			try {
				msg = mapper.readValue(u.toJson(), MessageDAO.class);
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			msg.setSender("User deleted.");
			instance.putMsg(msg);
		}

	}

	@FunctionName("function")
	public HttpResponseMessage helloFunction(@HttpTrigger(name = "req", methods = {
			HttpMethod.DELETE }, authLevel = AuthorizationLevel.ANONYMOUS, route = "delete/{id}") HttpRequestMessage<Optional<String>> request,
			@BindingName("id") String id

	) {
		List<Document> res = MongoDBLayer.getInstance().getUserById(id);
		User user = null;
		for (Document u : res)
			try {
				user = mapper.readValue(u.toJson(), UserDAO.class);
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		// updateChannels(user);
		updateMessages(user);

		return request.createResponseBuilder(HttpStatus.OK).header("Content-Type", "application/json")
				.body("Channels and Messages Updated!").build();
	}

	/*
	 * @POST
	 * 
	 * @Path("/auth")
	 * 
	 * @Consumes(MediaType.APPLICATION_JSON) public Response auth(Login user) {
	 * boolean pwdOk = false; // Check pwd if (pwdOk) { String uid =
	 * UUID.randomUUID().toString(); NewCookie cookie = new NewCookie("scc:session",
	 * uid, "/", null, "sessionid", 3600, false, true);
	 * RedisLayer.getInstance().putSession(new Session(uid, user.getUser())); return
	 * Response.ok().cookie(cookie).build(); } else throw new
	 * NotAuthorizedException("Incorrect login"); }
	 */

	/**
	 * Lists the ids of images stored.
	 * 
	 * @GET @Path("/")
	 * @Produces(MediaType.APPLICATION_JSON) public List<String> list() { return
	 *                                       null; }
	 */
}
