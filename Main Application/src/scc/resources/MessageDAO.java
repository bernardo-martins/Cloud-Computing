package scc.resources;

public class MessageDAO extends Message {

	private String _id;

	public MessageDAO() {

	}

	public MessageDAO(String id, String sender, String receiver, String replyTo, String body, String idImage) {
		super(id, sender, receiver, body, idImage);
	}

	public MessageDAO(String sender, String receiver, String replyTo, String body, String idImage) {
		super(sender, receiver, replyTo, body, idImage);
	}

	public MessageDAO(String sender, String receiver, String body, String idImage) {
		super(sender, receiver, body, idImage);
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

}
