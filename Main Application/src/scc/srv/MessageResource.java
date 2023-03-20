package scc.srv;

import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import scc.resources.Channel;
import scc.resources.ChannelDAO;
import scc.resources.Message;
import scc.resources.MessageDAO;
import scc.resources.User;
import scc.resources.UserDAO;
import scc.utils.AzureProperties;
import scc.utils.Session;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.bson.Document;

/**
 * Resource for managing media files, such as images.
 */
@Path("/messages")
public class MessageResource {
	String storageConnectionString = System.getenv(AzureProperties.COSMOS_DB_CONNECTION_STRING);
	ObjectMapper mapper = new ObjectMapper();



	public Session checkCookieMsg(Cookie session)
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
	
	public Session checkCookieMsg(Cookie session, String idSender,String receiver)
			throws NotAuthorizedException, CacheException, JsonMappingException, JsonProcessingException {
		if (session == null || session.getValue() == null)
			throw new NotAuthorizedException("No session initialized");
		Session s;
		try {

			s = RedisCache.getSession(session.getValue());
		} catch (CacheException e) {
			throw new NotAuthorizedException("No valid session initialized");
		}
		if(s.getUser().getId().equals(idSender)) {
			Channel channel = ChannelResource.getChannelPriv(receiver);
			if(channel == null)
				throw new NotAuthorizedException("Invalid Channel");		
			if(channel!=null && channel.getIsPriv() && !channel.getIdUsers().contains(idSender)) {
				throw new NotAuthorizedException("User has no premission to access this message");
			}else {
				User user = UserResource.getUserPrivate(idSender);
				if(user == null) {
					throw new NotAuthorizedException("User does not exist");
				}
					
				if(user.getChannelIds() == null || !user.getChannelIds().contains(receiver)) {
					throw new NotAuthorizedException("User is not subscribed to this channel");
				}
			}
		}
			
		
		
		if (s == null || s.getUser() == null || s.getUser().getId().length() == 0)
			throw new NotAuthorizedException("No valid session initialized");
		if (!s.getUser().getId().equals(idSender) && !s.getUser().equals("admin")) // alterar para detetar admin
			throw new NotAuthorizedException("Invalid user : " + s.getUser());
		return s;
	}


	/**
	 * Post a new message :)
	 * 
	 * @throws CacheException
	 * @throws JsonProcessingException
	 * @throws NotAuthorizedException
	 * @throws JsonMappingException
	 */
	@POST
	@Path("/upload")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Message upload(@CookieParam("scc:session") Cookie session, Message msg)
			throws JsonMappingException, NotAuthorizedException, JsonProcessingException, CacheException {

		if (checkCookieMsg(session,msg.getSender(),msg.getReceiver()) == null) {
			throw new WebApplicationException(Status.FORBIDDEN);
		}

		try {

			msg.setId("" + RedisLayer.postCounter("idMsg"));
			MongoDBLayer.getInstance().putMsg(msg);
			RedisLayer.postResource(msg.getId(), mapper.writeValueAsString(msg), "message", "MostRecentMessages", "NumMessages");
			if (!RedisLayer.resourceExists(msg.getReceiver(), "activity"))
				RedisLayer.postResourceWoUpdate(msg.getReceiver(), "" + 1, "activity", "MostRecentActivity",
						"NumActivities" + msg.getReceiver());
			else {
				int counter = RedisLayer.getResourceWoUpdate(msg.getReceiver(), "activity", "MostRecentActivity",
						"NumActivities" + msg.getReceiver());

				RedisLayer.postResourceWoUpdate(msg.getReceiver(), "" + (Integer.valueOf(String.valueOf(counter)) + 1),
						"activity", "MostRecentActivity", "NumActivities" + msg.getReceiver());
			}
			
			return msg;

		} catch (Exception e) {
			e.printStackTrace();
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * Return the contents of an image. Throw an appropriate error message if id
	 * does not exist.
	 * 
	 * @throws IOException
	 * @throws CacheException
	 * @throws NotAuthorizedException
	 */
	@GET
	@Path("/get/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Message getMessage(@CookieParam("scc:session") Cookie session, @PathParam("id") String id)
			throws IOException, NotAuthorizedException, CacheException {

		if (checkCookieMsg(session) == null) {
			throw new WebApplicationException(Status.FORBIDDEN);
		}

		Message msg = null;

		byte[] message = RedisLayer.getResource(id, "message", "MostRecentMessages", "NumMessages");

		if (message == null) {
			List<Document> res = MongoDBLayer.getInstance().getMsgById(id);

			for (Document u : res)
				msg = mapper.readValue(u.toJson(), MessageDAO.class);
		} else {
			msg = mapper.readValue(message, MessageDAO.class);
		}

		if (msg == null)
			throw new WebApplicationException(Status.NOT_FOUND);

		return msg;

	}

	@DELETE
	@Path("/delete/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Message deleteMessage(@CookieParam("scc:session") Cookie session, @PathParam("id") String id)
			throws WebApplicationException, CacheException, IOException {

		if (checkCookieMsg(session) == null) {
			throw new WebApplicationException(Status.FORBIDDEN);
		}

		Long res = MongoDBLayer.getInstance().delMsgById(id);
		
		FileCreator fc = new FileCreator();
		Message message = (Message) getMessage(session,id);

		if (message.getIdImage() != null) {
			try {
				fc.removeFile(message.getIdImage());
			}catch(IOException e) {
			}
			
		}

		RedisLayer.deleteResource(id, "Message");

		if (res>0) {
			return message;
		} else {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}

}
