package scc.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Channel {

	private String id;
	private String idOwner;
	private String name;
	private boolean isPriv;
	private List<String> idUsers;

	public Channel() {

	}

	public Channel(String id, String name, String idOwner, boolean isPriv, String[] idUsers) {
		this.id = id;
		this.name = name;
		this.idOwner = idOwner;
		this.isPriv = isPriv;
		this.idUsers = new ArrayList<String>(Arrays.asList(idUsers));

	}
	
	public Channel(String name, String idOwner, boolean isPriv, String[] idUsers) {
		this.name = name;
		this.idOwner = idOwner;
		this.isPriv = isPriv;
		this.idUsers = new ArrayList<String>(Arrays.asList(idUsers));
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void removeUser(String idUser) {
		idUsers.remove(idUser);
		return;
	}

	public String getName() {
		return name;
	}

	public String getIdOwner() {
		return idOwner;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean getIsPriv() {
		return isPriv;
	}

	public void setIsPriv(boolean isPriv) {
		this.isPriv = isPriv;
	}

	public List<String> getIdUsers() {
		return idUsers;
	}

	public void setIdUsers(List<String> users) {
		this.idUsers = users;
	}

	public void addUser(String userId) {
		if (!idUsers.contains(userId))
			idUsers.add(userId);
	}

}
