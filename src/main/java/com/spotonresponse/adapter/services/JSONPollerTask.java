package com.spotonresponse.adapter.services;

import com.spotonresponse.adapter.model.Configuration;
import com.spotonresponse.adapter.model.MappedRecordJson;
import com.spotonresponse.adapter.model.Util;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class JSONPollerTask implements Runnable {

    public static final String S_Features = "features";
    public static final String S_Properties = "properties";
    public static final String S_Geometry = "geometry";
    public static final String S_coordinates = "coordinates";
    public static final String S_TokenSeparator = ":";
    private static final Logger logger = LoggerFactory.getLogger(JSONPollerTask.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    public static String PatternPrefix = "(?i:.*";
    public static String PatternPostfix = ".*)";

    private Configuration configuration;

    public JSONPollerTask(Configuration configuration) {

        this.configuration = configuration;
    }

    @Override
    public void run() {

        logger.info("The time is now {}, JSON URL: {}", dateFormat.format(new Date()), configuration.getJson_ds());
        if (configuration.getJson_ds() == null) {
            // TODO fatal error
            System.exit(-1);
        }
        BufferedReader reader = null;
        HttpURLConnection con = null;
        try {
            URL url = new URL(configuration.getJson_ds());
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            // int status = con.getResponseCode();
            reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = reader.readLine()) != null) {
                content.append(inputLine);
            }
            JSONObject jsonObject = new JSONObject(content.toString());

            // features is array of records
            JSONArray features = (JSONArray) jsonObject.get(S_Features);

            // features: [
            //     { properties -> row of data
            //     geometry -> latitude/longitude
            //     }
            //     .
            //  ]
            for (int i = 0; i < features.length(); i++) {
                JSONObject feature = (JSONObject) features.get(i);

                // convert properties into a row of data
                JSONObject properties = (JSONObject) feature.get(S_Properties);
                Map<String, String> rowData = Util.convertKeyValue(Util.toMap(properties));

                // convert the geometry to latitude and longitude
                JSONObject geo = (JSONObject) feature.get(S_Geometry);
                JSONArray lonLat = (JSONArray) geo.get(S_coordinates);
                rowData.put("Longitude", String.valueOf(lonLat.get(0)));
                rowData.put("Latitude", String.valueOf(lonLat.get(1)));

                // call the toRecord to convert the row of data into the JSON record
                JSONObject record = toRecord(rowData);

                logger.debug("Record: [\n{}\n]", record == null ? "N/A" : record.toString());
            }
            reader.close();
        } catch (Exception e) {
            // TODO
            logger.error("Exception: {}", e.getMessage());
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    //
    // parse the row into the MappedRecordJson
    //
    private JSONObject toRecord(Map<String, String> row) {

        boolean isFullDescription = configuration.isFullDescription();

        MappedRecordJson record = new MappedRecordJson();

        Set<String> keys = configuration.getMap().keySet();
        keys.forEach(key -> {
            StringBuffer sb = new StringBuffer();
            List<String> columns = configuration.getMap().get(key);
            int isFirstColumn = 0;
            for (String column : columns) {
                if (isFirstColumn++ > 0) {
                    sb.append(S_TokenSeparator);
                }
                sb.append(row.get(column) != null ? row.get(column) : "N/A");
            }
            if (key.equalsIgnoreCase(Configuration.FN_FilterName)) {

            }
            record.put(key, sb.toString().trim());
        });

        // check whether filter match the filter text
        String filter = (String) record.get(Configuration.FN_FilterName);
        boolean isMatched = isMatchFilter(filter);
        logger.debug("Filter: [{}] Matched: [{}]", filter, isMatched ? "YES" : "NO");
        isMatched = isWithinBoundingBox(row);
        if (!isMatched) { return null; }

        // fill the content with every columns
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        Collection<String> values = row.values();
        int isFirstColumn = 0;
        for (Object value : values) {
            if (isFirstColumn++ > 0) {
                sb.append(S_TokenSeparator);
            }
            sb.append(value);
        }
        sb.append("]");
        record.put("content", sb.toString());

        if (isFullDescription) {
            sb = new StringBuffer();
            keys = row.keySet();
            for (String key : keys) {
                sb.append("<br/>");
                sb.append("<b>");
                sb.append(key + ": ");
                sb.append("</b>");
                sb.append(row.get(key));
            }
            record.put(Configuration.FN_Description, sb.toString());
        }

        // TO DO to perform the prefix, suffix, distance, filter, ...
        // category.fix, category.prefix, category.suffix
        if (configuration.getCategoryFixed() != null) {
            record.put(Configuration.FN_Category, configuration.getCategoryFixed());
        } else {
            if (configuration.getCategoryPrefix() != null || configuration.getCategorySuffix() != null) {
                String category = (String) record.get(Configuration.FN_FilterName);
                if (configuration.getCategoryPrefix() != null) {
                    category = configuration.getCategoryPrefix() + category;
                }
                if (configuration.getCategorySuffix() != null) {
                    category = category + configuration.getCategorySuffix();
                }
                record.put(Configuration.FN_Category, category);
            }
        }

        // title.prefix, title.suffix
        if (configuration.getTitlePrefix() != null || configuration.getTitleSuffix() != null) {
            String title = (String) record.get(Configuration.FN_Title);
            if (configuration.getTitlePrefix() != null) {
                title = configuration.getTitlePrefix() + title;
            }
            if (configuration.getTitleSuffix() != null) {
                title = title + configuration.getTitleSuffix();
            }
            record.put(Configuration.FN_Title, title);
        }

        record.init((String) record.get(Configuration.FN_Latitude),
                    (String) record.get(Configuration.FN_Longitude),
                    configuration.getId(),
                    MappedRecordJson.ToHash((String) record.get(Configuration.FN_Index)),
                    configuration.getJson_ds());

        mapRecord(record, configuration.getMappingColumns());

        return record;
    }

    private void mapRecord(MappedRecordJson record, Map<String, String> mappingColumns) {

        if (mappingColumns == null) { return; }
        Set<String> columns = mappingColumns.keySet();
        for (String column : columns) {
            Object value;
            try {
                value = record.get(column);
            } catch (JSONException e) {
                // TODO
                continue;
            }
            record.put(mappingColumns.get(column), value);
            record.remove(column);
        }
    }

    private boolean isWithinBoundingBox(Map<String, String> row) {

        return true;
    }

    private boolean isMatchFilter(String filter) {

        boolean negativeExpression = configuration.getFilterText().startsWith("!");
        String filterText = negativeExpression ? configuration.getFilterText()
            .substring(1) : configuration.getFilterText();
        String pattern = PatternPrefix + filterText + PatternPostfix;
        logger.debug("Filter Pattern: " + pattern);

        return filter.matches(pattern);
    }
}
