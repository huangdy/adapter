package com.spotonresponse.adapter.controller;

import com.spotonresponse.adapter.services.CSVToJSON;
import com.spotonresponse.adapter.services.FileStorageService;
import com.spotonresponse.adapter.services.JsonScheduler;
import com.spotonresponse.adapter.model.Configuration;
import com.spotonresponse.adapter.model.MappedRecordJson;
import com.spotonresponse.adapter.process.CSVParser;
import com.spotonresponse.adapter.process.ConfigFileParser;
import com.spotonresponse.adapter.repo.ConfigurationRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ConfigurationRepository configurationRepository;

    @PostMapping(path = "/uploadConfig", produces = "application/json")
    @CrossOrigin(origins = "http://localhost:3000")
    public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file) {

        String fileName = fileStorageService.storeFile(file);
        ConfigFileParser parser;
        try {
            parser = new ConfigFileParser(fileName, file.getInputStream());
            List<Configuration> configurationList = parser.getConfigurationList();
            for (Configuration configuration : configurationList) {
                configurationRepository.save(configuration);
                JsonScheduler.getInstance().setSchedule(configuration);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/downloadFile/")
                .path(fileName).toUriString();

        return new UploadFileResponse(fileName, fileDownloadUri, file.getContentType(), file.getSize());
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/uploadMultiConfig")
    public List<UploadFileResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {

        return Arrays.asList(files).stream().map(file -> uploadFile(file)).collect(Collectors.toList());
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/uploadCSVFile")
    public UploadFileResponse uploadCSVFile(@RequestParam("file") MultipartFile file, String csvConfiugrationName) {

        try {
            // convert MultipartFile into Map
            // retrieve the configuration
            Optional<Configuration> configuration = configurationRepository.findById(csvConfiugrationName);
            CSVParser csvParser = new CSVParser(configuration.get(), CSVToJSON.parse(file));
            List<MappedRecordJson> records = csvParser.getRecordList();
        } catch (Exception e) {

        }

        // parse the map with configuration
        return new UploadFileResponse(csvConfiugrationName, "xyz", "csv", 0);
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/uploadMultiCSVFile")
    public List<UploadFileResponse> uploadMultipleCSVFiles(
            @RequestParam("configuration_name") String csvConfiugrationName,
            @RequestParam("files") MultipartFile[] files) {

        return Arrays.asList(files).stream().map(file -> uploadCSVFile(file, csvConfiugrationName))
                .collect(Collectors.toList());
    }

    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
