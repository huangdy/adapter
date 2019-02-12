package com.spotonresponse.adapter.services;

import com.spotonresponse.adapter.model.Configuration;
import com.spotonresponse.adapter.process.ConfigFileParser;
import com.spotonresponse.adapter.repo.ConfigurationRepo;
import com.spotonresponse.adapter.repo.ConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

@Component
public class ConfigurationSetupService implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationSetupService.class);

    @Value("${jsonpoller.cron.schedule}")
    private String cronSchedule;

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    /**
     * This event is executed as late as conceivably possible to indicate that
     * the application is ready to service requests.
     * - initialize Directory Monitor
     * - initialize the Configuration files
     * - initilize JSON poller if the json_ds existed
     */
    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {

        try {
            ConfigurationRepo configRepo = new ConfigurationRepo();
            File[] files = new ClassPathResource("config").getFile().listFiles();
            for (File file : files) {
                ConfigFileParser configFileParser = new ConfigFileParser(file.getAbsolutePath(),
                                                                         new FileInputStream(file));

                logger.debug("Configuration File: {}", file.getAbsolutePath());

                List<Configuration> configurationList = configFileParser.getConfigurationList();
                configurationList.forEach(configuration -> {
                    configRepo.add(configuration);
                    configurationRepository.save(configuration);
                    if (configuration.getJson_ds() != null) {
                        logger.debug("Start JSON poller: [{}], with cron: [{}]", configuration.getJson_ds(), cronSchedule);
                        threadPoolTaskScheduler.schedule(new JSONPollerTask(configuration),
                                                         new CronTrigger(cronSchedule));
                    }
                });
            }
        }
        catch (Exception e) {
            // TODO
            e.printStackTrace();
        }

        return;
    }
}
