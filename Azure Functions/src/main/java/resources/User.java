package resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class User {

	private String id;
	private String name;
	private String password;
	private String idImage;
	private List<String> channelIds;

	public User() {

	}

	public User(String id, String name, String password) {
		this.id = id;
		this.name = name;
		this.password = password;
	}

	public User(String id, String name, String password, String idImage,  String[] channelIds) {
		this.id = id;
		this.name = name;
		this.password = password;
		this.idImage = idImage;
		this.channelIds = new ArrayList<String>(Arrays.asList(channelIds));
		System.out.println("channelIds constructor:  " + this.channelIds);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPass(String password) {
		this.password = password;
	}

	public String getidImage() {
		return idImage;
	}

	public void setidImage(String idImage) {
		this.idImage = idImage;
	}

	public List<String> getChannelIds() {
		return channelIds;
	}

	public void setChannelIds(List<String> channelIds) {
		this.channelIds = channelIds;
	}
	
	public void removeChannelId(String channelId) {
		this.channelIds.remove(channelId);
	}

	public void addChannel(String channelId) {
		System.out.println("ChanneIds" + channelIds);
		if (!channelIds.contains(channelId))
			channelIds.add(channelId);
	}

}
