package com.spotonresponse.adapter.repo;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.spotonresponse.adapter.model.MappedRecordJson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;

import java.util.*;

public class DynamoDBRepository {

    public static final String S_MD5HASH = "md5hash";
    public static final String S_Title = "title";
    private static final Logger logger = LogManager.getLogger(DynamoDBRepository.class);

    private static String aws_access_key_id;
    private static String aws_secret_access_key;
    private static String amazon_engpoint;
    private static String amazon_region;
    private static String dynamoDBTableName;
    private static DynamoDB dynamoDBClient = null;
    private static Table table = null;

    public DynamoDBRepository() {}

    public void init(String aws_access_key_id,
                     String aws_secret_access_key,
                     String amazon_endpoint,
                     String amazon_region,
                     String dynamoDBTableName) {

        aws_access_key_id = aws_access_key_id;
        aws_secret_access_key = aws_secret_access_key;
        amazon_endpoint = amazon_endpoint;
        amazon_region = amazon_region;
        dynamoDBTableName = dynamoDBTableName;

        if (aws_access_key_id == null ||
            aws_secret_access_key == null ||
            amazon_endpoint == null ||
            amazon_region == null ||
            dynamoDBTableName == null) {
            return;
        }

        BasicAWSCredentials credentials = new BasicAWSCredentials(aws_access_key_id, aws_secret_access_key);

        try {
            AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
                                                                       .withCredentials(new AWSStaticCredentialsProvider(credentials))
                                                                       .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(amazon_endpoint, amazon_region))
                                                                       .build();
            dynamoDBClient = new DynamoDB(amazonDynamoDB);

            logger.debug("Setting up DynamoDB client");
            table = dynamoDBClient.getTable(dynamoDBTableName);
        } catch (Throwable e) {
            logger.error("Cannot create NOSQL Table: " + e.getMessage());
        }
    }

    public JSONArray query(String title) {

        if (table == null) { return new JSONArray(); }

        QuerySpec querySpec = new QuerySpec().withKeyConditionExpression("title = :v_title")
                                             .withValueMap(new ValueMap().with(":v_title", title));

        ItemCollection<QueryOutcome> items = table.query(querySpec);
        Iterator iterator = items.iterator();
        JSONArray resultArray = new JSONArray();
        while (iterator.hasNext()) {
            Item item = (Item) iterator.next();
            resultArray.put(item.get("item"));
            logger.debug("Item: [{}]", item);
        }

        return resultArray;
    }

    public boolean createAllEntries(List<MappedRecordJson> recordList) {
        recordList.forEach(record -> {
            createEntry(record);
        });
        return true;
    }

    public boolean removeByCreator(String title) {

        return deleteAllEntries(title, queryHashList(title));
    }

    public List<String> queryHashList(String title) {

        List<String> hashList = new ArrayList<String>();

        if (table == null) { return hashList; }

        QuerySpec querySpec = new QuerySpec().withKeyConditionExpression("title = :v_title")
                                             .withValueMap(new ValueMap().with(":v_title", title));

        ItemCollection<QueryOutcome> items = table.query(querySpec);
        Iterator iterator = items.iterator();
        while (iterator.hasNext()) {
            hashList.add((String) ((Item) iterator.next()).get(S_MD5HASH));
        }

        return hashList;
    }

    public void shutown() {

        logger.debug("DynamoDB.shutdown: ... start ...");
        // Don't hold a connection open to the database
        if (dynamoDBClient != null) {
            dynamoDBClient.shutdown();
        }
    }

    public boolean deleteAllEntries(String creator, List<String> hashList) {

        if (hashList.size() == 0) return true;

        try {
            hashList.forEach(hash -> {
                deleteEntry(new AbstractMap.SimpleImmutableEntry(creator, hash));
            });
        } catch (Exception e) {
            logger.error("delete: error: {}", e.getMessage());
        }
        return true;
    }

    public boolean deleteEntry(Map.Entry key) {

        if (table == null) {
            return false;
        }

        try {
            logger.debug("deleteEntry: Title: [{}] & MD5Hash: [{}]", key.getKey(), key);
            DeleteItemSpec deleteItemSpec = new DeleteItemSpec().withPrimaryKey(new PrimaryKey(S_Title, key.getKey(), S_MD5HASH, key
                .getValue()));
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
            table.putItem(new Item().withPrimaryKey(S_MD5HASH, item.getPrimaryKey(), S_Title, item.getCreator())
                                    .withJSON("item", item.toString()));
            logger.debug("createEntry: ... successful ...");

        } catch (Exception e) {
            logger.error("Error: {}", e.getMessage());
            return false;
        }

        return true;
    }
}
