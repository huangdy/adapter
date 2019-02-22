package com.spotonresponse.adapter.controller;

import com.spotonresponse.adapter.repo.DynamoDBRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JsonController {

    private DynamoDBRepository dynamoDBRepository;

    public JsonController() { this.dynamoDBRepository = new DynamoDBRepository(); }

    @RequestMapping(value = "/query", produces = "application/json")
    public String query(@RequestParam(value = "config", defaultValue = "xcore") String config) {

        JSONArray resultArray = dynamoDBRepository.query(config);

        int size = resultArray.length();
        JSONObject object = new JSONObject().put("Configuration", config)
                                            .put("Count", size)
                                            .put("Content", resultArray);
        return object.toString();
    }

    @RequestMapping(value = "/delete", produces = "application/json")
    public String delete(@RequestParam(value = "config", defaultValue = "xcore") String creator) {

        int count = dynamoDBRepository.removeByCreator(creator);

        JSONObject object = new JSONObject();
        object.put("Configuration", creator).put("Count", count);
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
