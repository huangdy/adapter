package com.spotonresponse.adapter.repo;

import com.spotonresponse.adapter.model.Configuration;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationRepo {

    public static final Map<String, Configuration> configurationRepo = new HashMap<String, Configuration>();

    public void ConfigurationRepo() {}

    public Configuration getConfiguration(String id) {
        return configurationRepo.get(id);
    }

    public void add(Configuration configuration) {

        configurationRepo.put(configuration.getId(), configuration);
    }
}
