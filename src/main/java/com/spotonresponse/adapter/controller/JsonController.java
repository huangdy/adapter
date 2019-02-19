package com.spotonresponse.adapter.controller;

import com.spotonresponse.adapter.model.MappedRecordJson;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JsonController {

    @RequestMapping("/jsoncontroller")
    public String jsoncontroller(@RequestParam(value = "config", defaultValue = "xcore") String config) {
        MappedRecordJson object = new MappedRecordJson();
        object.init("1.1", "1,0", "Title", "xxxx", "localhost");
        return object.toString();
    }
}
