package scc.srv;

import javax.ws.rs.Consumes;
import java.io.File;
import java.io.IOException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import com.azure.storage.blob.BlobContainerClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import scc.utils.AzureProperties;
import scc.utils.Hash;

@Path("/media")
public class MediaResource {

	String storageConnectionString = System.getenv(AzureProperties.COSMOS_DB_CONNECTION_STRING);
	ObjectMapper mapper = new ObjectMapper();

	
	

	@POST
	@Path("/upload")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	public String upload(byte[] bytes) {
		String key = "";
		try {
			FileCreator fc = new FileCreator();
		

		   	key = Hash.of(bytes);
			fc.newFile(key,new String(bytes));

			return RedisLayer.postResource(key, mapper.writeValueAsString(bytes), "media", "MostRecentMedia",
					"NumMedia");
		}catch (Exception e) {
			e.printStackTrace();
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Return the contents of an image. Throw an appropriate error message if id
	 * does not exist.
	 * 
	 * @throws JsonProcessingException
	 * @throws JsonMappingException
	 */
	@GET
	@Path("/get/{id}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public byte[] getMedia(@PathParam("id") String id) throws JsonMappingException, JsonProcessingException {

		byte[] bytes = null;
		BlobContainerClient client = null;

		bytes = RedisLayer.getResource(id, "media", "MostRecentMedia", "NumMedia");

		if (bytes == null) {
			FileCreator fc = new FileCreator();

			
			try {
				String data = fc.getFileContent(id);
				bytes = data.getBytes();
			}catch(IOException e) {
				
			}
			

			
		}

		if (bytes == null)
			throw new WebApplicationException(Status.NOT_FOUND);
		
		RedisLayer.postResource(id, mapper.writeValueAsString(bytes), "media", "MostRecentMedia", "NumMedia");

		return bytes;

	}

	@DELETE
	@Path("/delete/{id}")
	public void deleteMessage(@PathParam("id") String id) throws WebApplicationException {
		FileCreator fc = new FileCreator();

		try {
			fc.removeFile(id);
		}catch(IOException e) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
			

		RedisLayer.deleteResource(id, "Media");

		return;
	}

}
