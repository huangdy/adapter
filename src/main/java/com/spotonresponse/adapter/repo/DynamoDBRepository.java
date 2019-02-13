package com.spotonresponse.adapter.repo;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.spotonresponse.adapter.model.MappedRecordJson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class DynamoDBRepository {

    private static final Logger logger = LogManager.getLogger(DynamoDBRepository.class);

    private static DynamoDB dynamoDB = null;
    private static Table table = null;

    public DynamoDBRepository() {

    }

    public void init(String aws_access_key_id, String aws_secret_access_key, String amazon_endpoint, String amazon_region, String dynamoDBTableName) {

        if (aws_access_key_id == null || aws_secret_access_key == null || amazon_endpoint == null || amazon_region == null || dynamoDBTableName == null) {
            return;
        }

        BasicAWSCredentials credentials = new BasicAWSCredentials(aws_access_key_id, aws_secret_access_key);

        try {
            AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials)).withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(amazon_endpoint, amazon_region)).build();
            dynamoDB = new DynamoDB(client);

            logger.debug("Setting up DynamoDB client");
            table = dynamoDB.getTable(dynamoDBTableName);
        }
        catch (Throwable e) {
            logger.error("Cannot create NOSQL Table: " + e.getMessage());
        }
    }

    public void shutown() {

        logger.debug("DynamoDB.shutdown: ... start ...");
        // Don't hold a connection open to the database
        if (dynamoDB != null) {
            dynamoDB.shutdown();
        }
    }

    public boolean deleteEntry(Map.Entry key) {

        if (table == null) {
            return false;
        }

        try {
            logger.debug("deleteEntry: Title: [" + key.getKey() + "] & MD5hash: [" + key.getValue() + "]");
            DeleteItemSpec deleteItemSpec = new DeleteItemSpec().withPrimaryKey(new PrimaryKey("title", key.getKey(), "md5hash", key.getValue()));
            table.deleteItem(deleteItemSpec);
            logger.debug("eleteEntry: ... successful ...");
        }
        catch (Exception e) {
            logger.error("deleteEntry: Title: [" + key.getKey() + "] & MD5hash: [" + key.getValue() + "]: Error: " + e.getMessage());
            return false;
        }

        return true;

    }

    public boolean updateEntry(MappedRecordJson item) {

        logger.debug("updateEntry: ... start ...");
        boolean isSuccess = this.deleteEntry(item.getMapEntry()) && this.createEntry(item);
        logger.debug("updateEntry: ... end ... [" + (isSuccess ? "Successful" : " Failure") + "]");
        return isSuccess;
    }

    public boolean createEntry(MappedRecordJson item) {

        if (table == null) {
            return false;
        }

        // Get the values for the indexes (keys)
        String md5hash = item.getPrimaryKey();
        String title = item.getTitle();

        try {
            logger.debug("createEntry: Title: [" + title + "] & MD5hash: [" + md5hash + "]");
            table.putItem(new Item().withPrimaryKey("md5hash", md5hash, "title", title).withJSON("item", item.toString()));
            logger.debug("createEntry: ... successful ...");

        }
        catch (Exception e) {
            logger.debug("createEntry: Title: [" + title + "] & MD5hash: [" + md5hash + "]: Error: " + e.getMessage());
            return false;
        }

        return true;
    }
}
