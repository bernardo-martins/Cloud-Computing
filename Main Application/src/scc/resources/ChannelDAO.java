package scc.resources;


public class ChannelDAO extends Channel {

	private String _id;

	public ChannelDAO() {

	}

	public ChannelDAO(String name, String idOwner, boolean isPriv, String[] idUsers) {
		super(name, idOwner,isPriv,idUsers);
	}
	
	public ChannelDAO(String id, String name, String idOwner, boolean isPriv, String[]  idUsers) {
		super(id, name, idOwner, isPriv, idUsers);
	}


	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

}
