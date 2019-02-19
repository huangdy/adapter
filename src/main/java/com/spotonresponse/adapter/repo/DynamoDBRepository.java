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

    private static final String S_MD5HASH = "md5hash";
    private static final String S_CREATOR = "title";
    private static DynamoDB dynamoDB = null;
    private static Table table = null;

    public DynamoDBRepository() {

    }

    public void init(String aws_access_key_id,
                     String aws_secret_access_key,
                     String amazon_endpoint,
                     String amazon_region,
                     String dynamoDBTableName) {

        if (aws_access_key_id == null ||
            aws_secret_access_key == null ||
            amazon_endpoint == null ||
            amazon_region == null ||
            dynamoDBTableName == null) {
            return;
        }

        BasicAWSCredentials credentials = new BasicAWSCredentials(aws_access_key_id, aws_secret_access_key);

        try {
            AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(amazon_endpoint, amazon_region))
                .build();
            dynamoDB = new DynamoDB(client);

            logger.debug("Setting up DynamoDB client");
            table = dynamoDB.getTable(dynamoDBTableName);
        } catch (Throwable e) {
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
            logger.debug("deleteEntry: Title: [{}] & MD5Hash: [{}]", key.getKey(), key);
            DeleteItemSpec deleteItemSpec = new DeleteItemSpec().withPrimaryKey(new PrimaryKey(S_CREATOR,
                                                                                               key.getKey(),
                                                                                               S_MD5HASH,
                                                                                               key.getValue()));
            table.deleteItem(deleteItemSpec);
            logger.debug("eleteEntry: ... successful ...");
        } catch (Exception e) {
            logger.error("deleteEntry: Title: [{}] & MD5Hash: [{}]: Error: [{}]", key.getKey(), key, e.getMessage());
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

        logger.debug("createEntry: Creator: [{}] MD5HASH: [{}]", item.getCreator(), item.getPrimaryKey());
        try {
            table.putItem(new Item().withPrimaryKey(S_MD5HASH, item.getPrimaryKey(), S_CREATOR, item.getCreator())
                              .withJSON("item", item.toString()));
            logger.debug("createEntry: ... successful ...");

        } catch (Exception e) {
            logger.error("Error: {}", e.getMessage());
            return false;
        }

        return true;
    }
}
