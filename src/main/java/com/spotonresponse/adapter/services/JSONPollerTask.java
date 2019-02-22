package com.spotonresponse.adapter.services;

import com.spotonresponse.adapter.model.Configuration;
import com.spotonresponse.adapter.model.MappedRecordJson;
import com.spotonresponse.adapter.process.JsonFeedParser;
import com.spotonresponse.adapter.repo.DynamoDBRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;

public class JSONPollerTask implements Runnable {

    static Logger logger = LoggerFactory.getLogger(JSONPollerTask.class);

    private DynamoDBRepository dynamoDBRepository;
    private Configuration configuration;

    public JSONPollerTask(Configuration configuration) {
        this.configuration = configuration;
        this.dynamoDBRepository = new DynamoDBRepository();
    }

    @Override
    public void run() {

        MDC.put("logFileName", this.configuration.getId());

        LocalDateTime localDateTime = LocalDateTime.now();
        logger.info("Current DateTime is {}, JSON URL: {}", localDateTime, configuration.getJson_ds());
        if (configuration.getJson_ds() == null) {
            // TODO fatal error
            System.exit(-1);
        }
        BufferedReader reader;
        HttpURLConnection con = null;
        try {
            URL url = new URL(configuration.getJson_ds());
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            // int status = con.getResponseCode();
            reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = reader.readLine()) != null) {
                content.append(inputLine);
            }

            // use the input stream to generate records
            List<MappedRecordJson> recordList = new JsonFeedParser(
                this.configuration,
                content.toString()).getRecordList();

            dynamoDBRepository.removeByCreator(configuration.getId());
            dynamoDBRepository.createAllEntries(recordList);

            // close the reader
            reader.close();
        } catch (Exception e) {
            // TODO
            logger.error("Exception: {}", e.getMessage());
            e.printStackTrace();
        } finally {
            MDC.remove("logFileName");

            // close the url connection
            if (con != null) {
                con.disconnect();
            }
        }
    }
}
