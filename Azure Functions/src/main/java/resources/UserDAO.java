package resources;

public class UserDAO extends User {

	private String _rid;
	private String _ts;

	public UserDAO() {

	}

	public UserDAO(String id, String name, String password) {
		super(id, name, password);

	}

	public UserDAO(String id, String name, String password, String idImage, String[] channelIds) {
		super(id, name, password, idImage,channelIds);

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
