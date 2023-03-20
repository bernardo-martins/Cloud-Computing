package scc.utils;

import scc.resources.User;
import scc.resources.UserDAO;
public class Session {
	String id;
	UserDAO user;
	
	
	public Session(String id,UserDAO userObj) {
		this.id = id;
		this.user = userObj;
	}
	
	public Session() {
		
	}
	
	public UserDAO getUser() {
		return user;
	}
	
	public String getID() {
		return id;
	}
	
}
