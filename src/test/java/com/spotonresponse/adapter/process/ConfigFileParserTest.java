package com.spotonresponse.adapter.process;

import com.spotonresponse.adapter.model.Configuration;
import com.spotonresponse.adapter.repo.ConfigurationRepository;
import com.spotonresponse.adapter.repo.CoreConfigurationRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ConfigFileParserTest {

    private static Logger logger = LoggerFactory.getLogger(ConfigFileParserTest.class);

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private CoreConfigurationRepository coreConfigurationRepository;

    @Test
    public void testConfigFileParser() {

        try {
            File[] files = new ClassPathResource("config").getFile().listFiles();
            for (File file : files) {
                logger.debug("Filename: {}", file.getAbsolutePath());
                ConfigFileParser configFileParser = new ConfigFileParser(file.getPath(),
                                                                         new FileInputStream(file.getAbsolutePath()));
                List<Configuration> configurationList = configFileParser.getConfigurationList();
                for (Configuration configuration: configurationList) {
                    configurationRepository.save(configuration);
                }
            }
        }
        catch (Exception e) {
            // TODO
            logger.error("Exception: {}", e.getMessage());
        }
    }
}
