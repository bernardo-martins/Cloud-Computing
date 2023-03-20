package srvresources;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedIterable;


import resources.UserDAO;
import resources.MessageDAO;
import resources.ChannelDAO;
import srvresources.AzureProperties;

public class CosmosDBLayer {
	private static final String CONNECTION_URL = System.getenv(AzureProperties.COSMOSDB_URL);
	private static final String DB_KEY = System.getenv(AzureProperties.COSMOSDB_KEY);
	private static final String DB_NAME = System.getenv(AzureProperties.COSMOSDB_DATABASE);

	private static CosmosDBLayer instance;

	public static synchronized CosmosDBLayer getInstance() {
		if (instance != null)
			return instance;

		CosmosClient client = new CosmosClientBuilder().endpoint(CONNECTION_URL).key(DB_KEY).gatewayMode() // replace by
																											// .directMode()
																											// for
																											// better
																											// performance
				.consistencyLevel(ConsistencyLevel.SESSION).connectionSharingAcrossClientsEnabled(true)
				.contentResponseOnWriteEnabled(true).buildClient();
		instance = new CosmosDBLayer(client);
		return instance;

	}

	private CosmosClient client;
	private CosmosDatabase db;
	private CosmosContainer users;
	private CosmosContainer msgs;
	private CosmosContainer channels;

	public CosmosDBLayer(CosmosClient client) {
		this.client = client;
		init();
	}

	private synchronized void init() {
		if (db != null)
			return;
		db = client.getDatabase(DB_NAME);
		users = db.getContainer("Users");
		msgs = db.getContainer("Messages");
		channels = db.getContainer("Channels");

	}

	public CosmosItemResponse<Object> delUserById(String id) {
		init();
		PartitionKey key = new PartitionKey(id);
		return users.deleteItem(id, key, new CosmosItemRequestOptions());
	}

	public CosmosItemResponse<Object> delMsgById(String id) {
		init();
		PartitionKey key = new PartitionKey(id);
		return msgs.deleteItem(id, key, new CosmosItemRequestOptions());
	}

	public CosmosItemResponse<Object> delChById(String id) {
		init();
		PartitionKey key = new PartitionKey(id);
		return channels.deleteItem(id, key, new CosmosItemRequestOptions());
	}

	public CosmosItemResponse<Object> delUser(UserDAO user) {
		init();
		return users.deleteItem(user, new CosmosItemRequestOptions());
	}

	public CosmosItemResponse<Object> delMsg(MessageDAO msg) {
		init();
		return msgs.deleteItem(msg, new CosmosItemRequestOptions());
	}

	public CosmosItemResponse<Object> delCh(ChannelDAO ch) {
		init();
		return channels.deleteItem(ch, new CosmosItemRequestOptions());
	}

	public CosmosItemResponse<UserDAO> putUser(UserDAO user) {
		init();

		return users.createItem(user);
	}

	public void replaceUser(UserDAO user) {
		init();

		users.replaceItem(user, user.getId(), new PartitionKey(user.getId()), new CosmosItemRequestOptions());
	}

	public CosmosItemResponse<MessageDAO> putMsg(MessageDAO msg) {
		init();
		return msgs.createItem(msg);
	}

	public CosmosItemResponse<ChannelDAO> putCh(ChannelDAO ch) {
		init();

		return channels.createItem(ch);
	}

	public void replaceChannel(ChannelDAO ch) {
		init();
		channels.replaceItem(ch, ch.getId(), new PartitionKey(ch.getId()), new CosmosItemRequestOptions());
	}

	public CosmosPagedIterable<UserDAO> getUserById(String id) {
		init();
		return users.queryItems("SELECT * FROM Users WHERE Users.id=\"" + id + "\"", new CosmosQueryRequestOptions(),
				UserDAO.class);
	}

	public CosmosPagedIterable<MessageDAO> getMsgById(String id) {
		init();
		return msgs.queryItems("SELECT * FROM Messages WHERE Messages.id=\"" + id + "\"",
				new CosmosQueryRequestOptions(), MessageDAO.class);
	}

	public CosmosPagedIterable<ChannelDAO> getChById(String id) {
		init();
		return channels.queryItems("SELECT * FROM Channels WHERE Channels.id=\"" + id + "\"",
				new CosmosQueryRequestOptions(), ChannelDAO.class);
	}

	public CosmosPagedIterable<MessageDAO> getMessages(String name) {
		return msgs.queryItems("SELECT * FROM Messages ORDER BY NAME WHERE Messages.sender=\""+name+"\"",
				new CosmosQueryRequestOptions(), MessageDAO.class);
	}

	public CosmosPagedIterable<MessageDAO> getMessagesFromChannel(String channelId, String offset, String limit) {
		init();
		return msgs.queryItems( "SELECT * FROM Messages WHERE Messages.receiver=\"" + channelId +"\""+  offset + limit,
				new CosmosQueryRequestOptions(), MessageDAO.class);

	}

	public CosmosPagedIterable<UserDAO> getUsers() {
		init();
		return users.queryItems("SELECT * FROM Users ", new CosmosQueryRequestOptions(), UserDAO.class);
	}

	public CosmosPagedIterable<MessageDAO> getMessages() {
		init();
		return msgs.queryItems("SELECT * FROM Messages ", new CosmosQueryRequestOptions(), MessageDAO.class);
	}

	public CosmosPagedIterable<ChannelDAO> getChannels() {
		init();
		return channels.queryItems("SELECT * FROM Channels ", new CosmosQueryRequestOptions(), ChannelDAO.class);
	}

	public void close() {
		client.close();
	}

}
