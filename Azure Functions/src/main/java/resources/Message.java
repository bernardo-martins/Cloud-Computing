package resources;

public class Message {
	private String id;
	private String sender, receiver, replyTo;
	private String idImage;
	private String body;

	public Message() {

	}

	public Message(String id, String sender, String receiver, String replyTo, String body, String idImage) {
		this.id = id;
		this.sender = sender;
		this.receiver = receiver;
		this.replyTo = replyTo;
		this.body = body;
		this.idImage = idImage;
	}

	public Message(String sender, String receiver, String replyTo, String body, String idImage) {
		this.sender = sender;
		this.receiver = receiver;
		this.replyTo = replyTo;
		this.body = body;
		this.idImage = idImage;
	}

	public Message(String sender, String receiver, String body, String idImage) {
		this.sender = sender;
		this.receiver = receiver;
		this.replyTo = null;
		this.body = body;
		this.idImage = idImage;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getIdImage() {
		return idImage;
	}

	public void setIdImage(String idImage) {
		this.idImage = idImage;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getReplyTo() {
		return replyTo;
	}

	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}

}
