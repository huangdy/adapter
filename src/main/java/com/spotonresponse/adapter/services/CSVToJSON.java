package com.spotonresponse.adapter.services;

import java.io.File;
import java.util.List;
import java.util.Map;

// import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;

public class CSVToJSON {

    public static JSONObject parse(MultipartFile file, String csvConfigurationName) throws Exception {

        File output = new File("output.json");

        CsvSchema csvSchema = CsvSchema.builder().setUseHeader(true).build();
        CsvMapper csvMapper = new CsvMapper();

        // Read data from CSV file
        List<Object> readAll = csvMapper.readerFor(Map.class).with(csvSchema).readValues(file.getInputStream())
                .readAll();

        JSONObject jsonObject = new JSONObject(readAll);
        return jsonObject;
        // ObjectMapper mapper = new ObjectMapper();

        // Write JSON formated data to output.json file
        // mapper.writerWithDefaultPrettyPrinter().writeValue(output, readAll);

        // Write JSON formated data to stdout
        // System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(readAll));
    }
}