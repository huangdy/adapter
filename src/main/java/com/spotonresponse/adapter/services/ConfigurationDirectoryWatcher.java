package com.spotonresponse.adapter.services;

import com.spotonresponse.adapter.model.Configuration;
import com.spotonresponse.adapter.process.ConfigFileParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Example to watch a directory (or tree) for changes to files.
 */
public class ConfigurationDirectoryWatcher {

    private static Logger logger = LoggerFactory.getLogger(ConfigurationDirectoryWatcher.class);
    private final Map<String, Long> lastAccessTimestamp = new HashMap<String, Long>();
    private final Map<String, ScheduledFuture> scheduleMap = new HashMap<String, ScheduledFuture>();
    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private ThreadPoolTaskScheduler scheduler;

    /**
     * Creates a WatchService and registers the given directory
     */
    public ConfigurationDirectoryWatcher(Path dir, ThreadPoolTaskScheduler scheduler) throws IOException {

        logger.info("ConfigurationDirectoryWatcher: Directory: {}", dir.getFileName());
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey, Path>();
        this.scheduler = scheduler;

        logger.debug("Register: ... {} ...\n", dir);
        register(dir);
        logger.debug("Register: ... done ...");
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {

        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        Path prev = keys.get(key);
        if (prev == null) {
            logger.info("register: Directory: {}", dir);
        } else {
            if (!dir.equals(prev)) {
                logger.info("update: {}: {} -> {}", key, prev, dir);
            }
        }
        keys.put(key, dir);
    }

    /**
     * Process all events for keys queued to the watcher
     */
    void processEvents() {

        while (true) {

            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                logger.error("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                // TBD - provide example of how OVERFLOW event is handled
                if (kind == OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);

                // logger.info("Filename: {}, Event: {}, @{}", child, kind, new Date());
                if (lastAccessTimestamp.get(child.toString()) != null &&
                    new Date().getTime() - lastAccessTimestamp.get(child.toString()) < 3000) {
                    continue;
                }
                // print out event
                // logger.info("Record last access time for Event: {}, file: {}, name: {}", kind, child, name);
                lastAccessTimestamp.put(child.toString(), new Date().getTime());

                logger.info("{} -> Filename: {}", kind, child);
                if (kind == ENTRY_CREATE) {
                    createSchedule(child.toString());
                } else if (kind == ENTRY_MODIFY) {
                    updateSchedule(child.toString());
                } else if (kind == ENTRY_DELETE) {
                    cancelSchedule(child.toString());
                } else {
                    logger.warn("Unkonw Event: {}", kind);
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }

    private void createSchedule(String filename) {

        logger.info("createSchedule: File: {}", filename);

        File file = new File(filename);
        try {
            ConfigFileParser configFileParser = new ConfigFileParser(file.getPath(), new FileInputStream(file));
            List<Configuration> configurationList = configFileParser.getConfigurationList();
            for (Configuration configuration : configurationList) {
                // configRepo.add(configuration);
                // configurationRepository.save(configuration);
                if (configuration.getJson_ds() != null) {
                    Date currentTimestamp = new Date();
                    long oneSecondLater = currentTimestamp.getTime() + 1000;
                    logger.info("current: {}, scheduled time: {}", currentTimestamp, new Date(oneSecondLater));
                    logger.info("Start JSON poller Thread for [{}], URL: [{}], schedule: [{}]",
                                configuration.getId(),
                                configuration.getJson_ds(),
                                new Date(oneSecondLater));
                    scheduleMap.put(filename,
                                    scheduler.schedule(new JSONPollerTask(configuration), new Date(oneSecondLater)));
                }
            }
        } catch (Exception e) {
            //TODO
            logger.error("createSchedule: open fis: {}", e.getMessage());
        }
    }

    private void updateSchedule(String filename) {

        logger.info("updateSchedule: FILE: {}", filename);
        cancelSchedule(filename);
        createSchedule(filename);
    }

    private void cancelSchedule(String filename) {

        logger.info("cancelSchedule: FILE: {}", filename);
        ScheduledFuture taskSchedule = scheduleMap.get(filename);
        if (taskSchedule != null) {
            taskSchedule.cancel(true);
        }
    }

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {

        return (WatchEvent<T>) event;
    }
}
