package resources;

public class MessageDAO extends Message {

	private String _rid;
	private String _ts;

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

	public String get_ts() {
		return _ts;
	}

	public void set_ts(String _ts) {
		this._ts = _ts;
	}

	public String get_rid() {
		return _rid;
	}

	public void set_rid(String _rid) {
		this._rid = _rid;
	}

}
