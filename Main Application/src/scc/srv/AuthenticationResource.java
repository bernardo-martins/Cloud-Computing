package scc.srv;

import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;

import scc.utils.Login;
import scc.utils.Session;

public class AuthenticationResource {

	@POST
	@Path("/auth")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response auth(Login user) throws NotAuthorizedException, JsonProcessingException { // imported core Response
																								// and not azure one
		Session s = null;
		try {
			s = RedisCache.getSession(user.getId());
		} catch (CacheException e) {
			throw new NotAuthorizedException("No valid session initialized");
		}
		if (s == null || s.getUser() == null || s.getUser().getId().length() == 0)
			throw new NotAuthorizedException("No valid session initialized");
		if (!s.getID().equals(user.getId()) && !s.getUser().equals("admin")) {
			throw new NotAuthorizedException("Invalid user : " + s.getUser());
		}

		if (user.getPass().equals(s.getUser().getPassword()) && user.getId().equals(s.getUser().getId())) {
			String uid = UUID.randomUUID().toString();
			NewCookie cookie = new NewCookie("scc:session", uid, "/", null, "sessionid", 3600, false, true);
			RedisCache.putSession(new Session(uid, s.getUser()));
			return Response.ok().cookie(cookie).build();
		} else
			throw new NotAuthorizedException("Incorrect login");

	}

}
