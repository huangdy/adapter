package com.spotonresponse.adapter.process;

import com.spotonresponse.adapter.model.Configuration;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class ConfigFileParser {

    private static final Logger logger = LoggerFactory.getLogger(ConfigFileParser.class);

    private List<Configuration> configurationList = new ArrayList<Configuration>();

    public ConfigFileParser() {

        super();
    }

    public ConfigFileParser(String configFilename, InputStream configInputStream) throws Exception {

        super();

        final String creator = FilenameUtils.getBaseName(configFilename);
        logger.info("start parsing {} and creator: {}", configFilename, creator);

        int startCount = 0;
        int endCount = 0;
        String line = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(configInputStream));
            reader.mark(20480);
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#") || line.length() == 0) {
                    continue;
                }
                if (line.equalsIgnoreCase(Configuration.N_Configuration_Start)) {
                    startCount++;
                    continue;
                } else if (line.equalsIgnoreCase(Configuration.N_Configuration_End)) {
                    endCount++;
                    continue;
                }
            }
        } catch (final Exception e) {
            throw new Exception(e.getMessage());
        }

        if (startCount != endCount) {
            throw new Exception("Configuration File: " + configFilename + ": incomplete configuration block");
        }

        try {

            reader.reset();

            Configuration configuration = null;

            if (startCount == 0) {
                configuration = new Configuration();
                configuration.setId(creator);
            }

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#") || line.length() == 0) {
                    continue;
                }
                if (line.equalsIgnoreCase(Configuration.N_Configuration_Start)) {
                    logger.debug("Configuration: .. start ...");
                    configuration = new Configuration();
                    configuration.setId(creator);
                    continue;
                } else if (line.equalsIgnoreCase(Configuration.N_Configuration_End)) {
                    logger.debug("Configuration: .. end ...");
                    if (configuration.isValid()) {
                        configurationList.add(configuration);
                        configuration = null;
                        continue;
                    } else {
                        throw new Exception(
                            "Configuration File: " + configFilename + ": " + configuration.getMissingAttributes());
                    }
                }
                if (configuration == null) {
                    throw new Exception("Configuration File: " + configFilename + ": Invalid format ...");
                }
                final String[] tokens = line.split(",", -1);
                if (tokens.length != 2) {
                    logger.error("Configuration File: " + configFilename + "Invalid formated Line: [" + line + "]");
                    continue;
                }
                tokens[0] = tokens[0].trim().toLowerCase();
                tokens[1] = tokens[1].trim();
                configuration.setKeyValue(tokens);
            }
            if (configuration != null) {
                if (configuration.isValid()) {
                    configurationList.add(configuration);
                } else {
                    throw new Exception(
                        "Configuration File: " + configFilename + ": " + configuration.getMissingAttributes());
                }
            }
            reader.close();
            logger.info("done parsing [{}]", configFilename);
        } catch (final Exception e) {
            throw new Exception("Configuration File: " + configFilename + ", Error: " + e.getMessage());
        }
    }

    public List<Configuration> getConfigurationList() {

        return configurationList;
    }

    public void setConfigurationList(List<Configuration> configurationList) {

        this.configurationList = configurationList;
    }
}
