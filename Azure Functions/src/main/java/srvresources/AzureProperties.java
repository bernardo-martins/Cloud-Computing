package srvresources;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AzureProperties {
	public static final String BLOB_KEY = "BlobStoreConnection";// "DefaultEndpointsProtocol=https;AccountName=sccstorewesteurope53647;AccountKey=NsB0PfMpia1SmBiBYY1UeX+BvLf4JPYMuMxArMBvUOzg4+PxYEPWg7+z5OvgwuIlG+dVo1c5iE7XAdBQS3c92w==;EndpointSuffix=core.windows.net";
	public static final String COSMOSDB_KEY = "COSMOSDB_KEY";// "22WXrHqX3kBStEGGxryLcSzFHZbxBi729ou43jYrqnZqvI6DX3MNuiC8cXPuOHnWGjVE3FyaWw1qt9dGr0wbCg==";
	public static final String COSMOSDB_URL = "COSMOSDB_URL";// "https://scc212253647.documents.azure.com:443/";
	public static final String COSMOSDB_DATABASE = "COSMOSDB_DATABASE";// "scc2122db53647";
	public static final String REDIS_KEY = "REDIS_KEY";// "7Z8ebYuTQSJ3CIZlcknchkULqihf4tzafAzCaGDCPFo=";
	public static final String REDIS_HOSTNAME = "REDIS_URL";// "rediswesteurope53647.redis.cache.windows.net";
	public static final String COSMOS_DB_CONNECTION_STRING = "BlobStoreConnection";// "DefaultEndpointsProtocol=https;AccountName=sccstorewesteurope53647;AccountKey=NsB0PfMpia1SmBiBYY1UeX+BvLf4JPYMuMxArMBvUOzg4+PxYEPWg7+z5OvgwuIlG+dVo1c5iE7XAdBQS3c92w==;EndpointSuffix=core.windows.net";

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
