package scc.utils;

public class Login {
	private String password;
	private String id;
	
	
	public Login() {
		
	}
	
	public Login(String id,String password) {
		this.password = password;
		this.id = id;
	}
	
	public void setPassword(String pass) {
		password = pass;
	}

	public String getPass() {
		return password;
	}
	
	public String getId() {
		return id;
	}
	
}
