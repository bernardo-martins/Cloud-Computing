package resources;


public class ChannelDAO extends Channel {

	private String _rid;
	private String _ts;

	public ChannelDAO() {

	}

	public ChannelDAO(String name, String idOwner, boolean isPriv, String[] idUsers) {
		super(name, idOwner,isPriv,idUsers);
	}
	
	public ChannelDAO(String id, String name, String idOwner, boolean isPriv, String[]  idUsers) {
		super(id, name, idOwner, isPriv, idUsers);
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
