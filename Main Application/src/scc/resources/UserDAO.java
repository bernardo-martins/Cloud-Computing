package scc.resources;

public class UserDAO extends User {

	private String _id;

	public UserDAO() {

	}

	public UserDAO(String id, String name, String password) {
		super(id, name, password);

	}

	public UserDAO(String id, String name, String password, String idImage, String[] channelIds) {
		super(id, name, password, idImage,channelIds);

	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _rid) {
		this._id = _id;
	}

}
