package scc.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AzureProperties {
	public static final String BLOB_KEY = "BlobStoreConnection";// "DefaultEndpointsProtocol=https;AccountName=sccstorewesteurope52676;AccountKey=nKWQybU38oHvKhogpebE+bs3WtvuzRO4scYyQkKanTno3OwpSq4cOWYIFiYf8lMx6yxNr3uIbrujCxVtvhzBZw==;EndpointSuffix=core.windows.net";
	public static final String COSMOSDB_KEY = "COSMOSDB_KEY";// "xhgrJkSbEXPVZD1TQ8OAm1qmQVN8F4492stIhigcKwuFmxap5ZQuFs89YuOy13OrbGdy4sRP79AL4dlbQDSUaQ==";
	public static final String COSMOSDB_URL = "COSMOSDB_URL";// "https://scc212252676.documents.azure.com:443/";
	public static final String COSMOSDB_DATABASE = "COSMOSDB_DATABASE";// "scc2122db52676";
	public static final String REDIS_KEY = "REDIS_KEY";// "iwA7TWKc0QTjgqAxJttUqPbMvbB8rbdNYAzCaKcdsXc=";
	public static final String REDIS_URL = "REDIS_URL";// "rediswesteurope52676.redis.cache.windows.net";
	public static final String REDIS_HOSTNAME = "REDIS_HOSTNAME";
	public static final String COSMOS_DB_CONNECTION_STRING = "BlobStoreConnection";// "DefaultEndpointsProtocol=https;AccountName=sccstorewesteurope53647;AccountKey=NsB0PfMpia1SmBiBYY1UeX+BvLf4JPYMuMxArMBvUOzg4+PxYEPWg7+z5OvgwuIlG+dVo1c5iE7XAdBQS3c92w==;EndpointSuffix=core.windows.net";
	public static final String MONGODB_URL = "MONGO_URL";
	
	
	public static final String PROPS_FILE = "azurekeys-westeurope.props";
	private static Properties props;

	public static synchronized Properties getProperties() {
		
		if (props == null) {
			props = new Properties();
			try {
				props.load(new FileInputStream(PROPS_FILE));
			} catch (IOException e) {
				// do nothing
			}
		}
		return props;
	}

}
