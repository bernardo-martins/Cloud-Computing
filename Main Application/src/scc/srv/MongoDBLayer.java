package scc.srv;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.bson.Document;

import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.MongoClientURI;

import scc.resources.Channel;
import scc.resources.ChannelDAO;
import scc.resources.Message;
import scc.resources.MessageDAO;
import scc.resources.User;
import scc.resources.UserDAO;
import scc.utils.AzureProperties;

public class MongoDBLayer {
	private static final String MONGODB_URL = System.getenv(AzureProperties.MONGODB_URL);

	private static MongoDBLayer instance;

	public static synchronized MongoDBLayer getInstance() {
		if (instance != null)
			return instance;

		MongoClient client = new MongoClient(new MongoClientURI(MONGODB_URL));
		instance = new MongoDBLayer(client);
		return instance;

	}

	private MongoClient client;
	private MongoDatabase db;
	private MongoCollection<Document> users;
	private MongoCollection<Document> msgs;
	private MongoCollection<Document> channels;
	ObjectMapper mapper = new ObjectMapper();

	public MongoDBLayer(MongoClient client) {
		this.client = client;
		init();
	}

	private synchronized void init() {
		if (db != null)
			return;
		db = client.getDatabase("DiscordMongoDB");
		users = db.getCollection("Users");
		msgs = db.getCollection("Messages");
		channels = db.getCollection("Channels");

	}

	public long delUserById(String id) {
		init();
		long count = users.deleteOne(new Document("_id", id)).getDeletedCount();
		return count;
	}

	public long delMsgById(String id) {
		init();
		long count = msgs.deleteOne(new Document("_id", id)).getDeletedCount();
		return count;
	}

	public long delChById(String id) {
		init();
		long count = channels.deleteOne(new Document("_id", id)).getDeletedCount();
		return count;
	}

	public long delUser(User user) {
		init();
		long count = users.deleteOne(new Document("_id", user.getId())).getDeletedCount();
		return count;
	}

	public long delMsg(Message msg) {
		init();
		long count = msgs.deleteOne(new Document("_id", msg.getId())).getDeletedCount();
		return count;
	}

	public long delCh(Channel ch) {
		init();
		long count = channels.deleteOne(new Document("_id", ch.getId())).getDeletedCount();
		return count;
	}

	public void putUser(User user) {
		init();
		users.insertOne(new Document("_id", user.getId()).append("id", user.getId()).append("name", user.getName())
				.append("password", user.getPassword())
				.append("idImage", user.getidImage())
				.append("channelIds", user.getChannelIds()));
	}

	public void replaceUser(User user) {
		init();
		users.replaceOne(new Document("_id", user.getId()),
				new Document("id", user.getId()).append("name", user.getName())
				.append("password", user.getPassword())
				.append("idImage", user.getidImage())
				.append("channelIds", user.getChannelIds()));
	}

	public void putMsg(Message msg) {
		init();
		msgs.insertOne(new Document("_id", msg.getId()).append("id", msg.getId()).append("sender", msg.getSender())
				.append("receiver", msg.getReceiver()).append("replyTo", msg.getReplyTo()).append("body", msg.getBody())
				.append("idImage", msg.getIdImage()));
	}

	public void putCh(Channel ch) {
		init();
		channels.insertOne(new Document("_id", ch.getId()).append("id", ch.getId()).append("idOwner", ch.getIdOwner())
				.append("name", ch.getName())
				.append("isPriv", ch.getIsPriv())
				.append("idUsers", ch.getIdUsers()));
	}

	public void replaceChannel(Channel ch) {
		init();
		channels.replaceOne(new Document("_id", ch.getId()),
				new Document("id", ch.getId()).append("idOwner", ch.getIdOwner())
				.append("name", ch.getName())
				.append("isPriv", ch.getIsPriv())
				.append("idUsers", ch.getIdUsers()));
	}

	public List<Document> getUserById(String id) {
		List<Document> docs = new ArrayList<Document>();
		MongoCursor<Document> cursor = users.find(new Document("_id", id)).iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			docs.add(doc);
		}
		return docs;
	}

	public List<Document> getMsgById(String id) {
		List<Document> docs = new ArrayList<Document>();
		MongoCursor<Document> cursor = msgs.find(new Document("_id", id)).iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			docs.add(doc);
		}
		return docs;
	}

	public List<Document> getChById(String id) {
		List<Document> docs = new ArrayList<Document>();
		MongoCursor<Document> cursor = channels.find(new Document("_id", id)).iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			docs.add(doc);
		}
		return docs;
	}

	public List<Document> getMessages(String name) {
		init();
		List<Document> docs = new ArrayList<Document>();
		MongoCursor<Document> cursor = msgs.find(new Document("sender", name)).iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			docs.add(doc);
		}

		return docs;
	}

	public List<Document> getMessagesFromChannel(String channelId, String offset, String limit) {
		List<Document> docs = new ArrayList<Document>();
		MongoCursor<Document> cursor = msgs.find(new Document("receiver", channelId)).iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			docs.add(doc);
		}
		return docs;

	}

	public List<Document> getUsers() {
		init();
		List<Document> docs = new ArrayList<Document>();
		MongoCursor<Document> cursor = users.find().iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			docs.add(doc);

		}
		return docs;
	}

	public List<Document> getMessages() {
		init();
		List<Document> docs = new ArrayList<Document>();
		MongoCursor<Document> cursor = msgs.find().iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			docs.add(doc);

		}
		return docs;
	}

	public List<Document> getChannels() {
		init();
		List<Document> docs = new ArrayList<Document>();
		MongoCursor<Document> cursor = channels.find().iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			docs.add(doc);

		}
		return docs;
	}

	public void close() {
		client.close();
	}

}
