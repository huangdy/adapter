package com.spotonresponse.adapter.controller;

import com.spotonresponse.adapter.model.QueryResult;
import com.spotonresponse.adapter.repo.DynamoDBRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JsonController {

    @Autowired
    private DynamoDBRepository repo;

    @RequestMapping(value = "/query", produces = "application/json")
    public QueryResult query(@RequestParam(value = "config", defaultValue = "xcore") String creator) {

        JSONArray resultArray = repo.query(creator);

        return new QueryResult(creator, resultArray.length(), resultArray);
    }

    @RequestMapping(value = "/delete", produces = "application/json")
    public QueryResult delete(@RequestParam(value = "config", defaultValue = "xcore") String creator) {

        int count = repo.removeByCreator(creator);
        return new QueryResult(creator, count, null);
    }

    @RequestMapping(value = "/test", produces = "application/json")
    public String test(@RequestParam(value = "config", defaultValue = "xcore") String configuration) {

        JSONObject o = new JSONObject();
        o.put("Configuration", configuration);
        o.put("Name", "Daniel Huang");
        return o.toString();
    }
}
