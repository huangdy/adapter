package com.spotonresponse.adapter.services;

import com.spotonresponse.adapter.model.Configuration;
import com.spotonresponse.adapter.process.ConfigFileParser;
import com.spotonresponse.adapter.repo.ConfigurationRepository;
import com.spotonresponse.adapter.repo.DynamoDBRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@Component
public class ConfigurationSetupService implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationSetupService.class);

    @Value("${jsonpoller.cron.schedule}")
    private String cronSchedule;

    @Value("${config.path}")
    private String configPath;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Autowired
    private DynamoDBRepository repo;

    /**
     * This event is executed as late as conceivably possible to indicate that
     * the application is ready to service requests.
     * - initialize Directory Monitor
     * - initialize the Configuration files
     * - initilize JSON poller if the json_ds existed
     */
    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {

        // startConfigurationDirectory();
        startConfigurationDirectoryWatcherThread();

        return;
    }

    private void startConfigurationDirectory() {

        try {

            logger.info("startConfigurationDirectory: Path: {}", configPath);

            // to use configPath as the resource to find the configuration path
            File[] files = resourceLoader.getResource(configPath).getFile().listFiles();

            // the map for the configuration and its task
            Map<String, ScheduledFuture> scheduleMap = new HashMap<String, ScheduledFuture>();

            for (File file : files) {

                logger.debug("Configuration File: {}", file.getPath());
                ConfigFileParser configFileParser = new ConfigFileParser(file.getPath(), new FileInputStream(file));

                List<Configuration> configurationList = configFileParser.getConfigurationList();
                configurationList.forEach(configuration -> {

                    // configRepo.add(configuration);
                    configurationRepository.save(configuration);
                    if (configuration.getJson_ds() != null) {
                        logger.info("Start JSON poller Thread: ID: [{}], URL: [{}], schedule: [{}]",
                                    configuration.getId(),
                                    configuration.getJson_ds(),
                                    cronSchedule);
                        scheduleMap.put(configuration.getId(),
                                        taskScheduler.schedule(new JSONPollerTask(configuration),
                                                               new CronTrigger(cronSchedule)));
                    }
                });
            }
        } catch (Exception e) {
            // TODO
            e.printStackTrace();
        }
    }

    public void startConfigurationDirectoryWatcherThread() {

        logger.info("Start Configuration Directory Watcher: {}", configPath);
        try {
            new ConfigurationDirectoryWatcher(Paths.get(resourceLoader.getResource(configPath).getFile().getPath()),
                                              taskScheduler,
                                              cronSchedule).process();
        } catch (Exception e) {
            // TODO
            e.printStackTrace();
        }
        logger.info("Done: Configuration Directory Watcher: {}", configPath);
    }
}
