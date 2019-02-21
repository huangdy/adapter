package com.spotonresponse.adapter.controller;

import com.spotonresponse.adapter.repo.DynamoDBRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JsonController {

    @RequestMapping(value = "/query", produces = "application/json")
    public String query(@RequestParam(value = "config", defaultValue = "xcore") String config) {

        DynamoDBRepository dynamoDBRepository = new DynamoDBRepository();
        JSONArray resultArray = dynamoDBRepository.query(config);

        int size = resultArray.length();
        JSONObject object = new JSONObject();
        object.put("Configuration", config);
        object.put("Count", size);
        object.put("Data", resultArray);

        return object.toString();
    }

    @RequestMapping(value = "/delete", produces = "application/json")
    public String delete(@RequestParam(value = "config", defaultValue = "xcore") String creator) {

        DynamoDBRepository dynamoDBRepository = new DynamoDBRepository();
        boolean isSuccess = dynamoDBRepository.removeByCreator(creator);

        JSONObject object = new JSONObject();
        object.put("Configuration", creator);
        object.put("Status", (isSuccess ? "true" : false));

        return object.toString();
    }

    @RequestMapping(value = "/test", produces = "application/json")
    public String test(@RequestParam(value = "config", defaultValue = "xcore") String configuration) {

        JSONObject o = new JSONObject();
        o.put("Configuration", configuration);
        o.put("Name", "Daniel Huang");
        return o.toString();
    }
}
