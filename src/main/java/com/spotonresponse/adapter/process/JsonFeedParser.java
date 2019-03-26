package com.spotonresponse.adapter.process;

import com.spotonresponse.adapter.model.ConfigHelper;
import com.spotonresponse.adapter.model.Configuration;
import com.spotonresponse.adapter.model.MappedRecord;
import com.spotonresponse.adapter.model.MappedRecordJson;
import com.spotonresponse.adapter.model.Util;
import com.spotonresponse.adapter.services.JSONPollerTask;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonFeedParser {

    public static final String S_Features = "features";
    public static final String S_Properties = "properties";
    public static final String S_Geometry = "geometry";
    public static final String S_coordinates = "coordinates";
    public static final String S_TokenSeparator = ":";
    public static final String PatternPrefix = "(?i:.*";
    public static final String PatternPostfix = ".*)";
    public static final double Pi = 3.14159;
    public static final double Radius = 6378137.0;

    private static Logger logger = LoggerFactory.getLogger(JSONPollerTask.class);

    private final List<MappedRecordJson> recordJsonList = new ArrayList<>();

    private Configuration configuration;

    public JsonFeedParser(Configuration configuration, String contentText) {

        this.configuration = configuration;
        JSONObject jsonObject = new JSONObject(contentText);

        List<MappedRecord> recordList = new ArrayList<MappedRecord>();

        // features is array of records
        JSONArray features = (JSONArray) jsonObject.get(S_Features);

        // features: [
        // {
        // properties -> row of data
        // geometry -> latitude/longitude
        // }
        // ]
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
            MappedRecord record = toRecord(rowData);
            if (record != null) {
                recordList.add(record);
            }
        }

        List<MappedRecord> mismatched = new ArrayList<MappedRecord>();
        // to apply the distance filter/distance
        // - calculate the bounding box
        // - find out whether the object is within the bounding box
        if (configuration.getDistance() != null && (configuration.getDistanceFilterText() == null
                || configuration.getDistanceFilterText().equalsIgnoreCase(configuration.getFilterText()))) {
            Double[][] boundingBox = calculateBoundingBox(recordList, configuration.getDistance());
            for (MappedRecord record : recordList) {
                if (Util.IsInsideBoundingBox(boundingBox, record.getLatitude(), record.getLongitude()) == false) {
                    logger.trace("{} is outside bounding box", record);
                    mismatched.add(record);
                }
            }
            if (mismatched.size() > 0) {
                recordList.removeAll(mismatched);
            }
        }

        Map<String, MappedRecordJson> map = new HashMap<String, MappedRecordJson>();
        logger.info("before hash: count: {}", recordList.size());
        for (MappedRecord record : recordList) {
            MappedRecordJson mappedRecordJson = new MappedRecordJson(record);
            Util.nullToNA(mappedRecordJson);
            map.put(mappedRecordJson.getPrimaryKey(), mappedRecordJson);
        }
        logger.debug("after hash: count: {}", map.size());

        for (MappedRecordJson record : map.values()) {
            recordJsonList.add(record);
        }
    }

    //
    // parse the row into the MappedRecordJson
    //
    private MappedRecord toRecord(Map<String, String> row) {

        boolean isFullDescription = configuration.isFullDescription();

        MappedRecord record = new MappedRecord();
        record.setCreator(configuration.getId());
        record.setCoreUri(configuration.getJson_ds());
        record.setLastUpdated(new Date());

        Set<String> columnNames = ConfigHelper.getMap(configuration).keySet();
        for (String columnName : columnNames) {
            StringBuffer sb = new StringBuffer();
            List<String> columns = ConfigHelper.getMap(configuration).get(columnName);
            if (columnName.equalsIgnoreCase(ConfigHelper.FN_Description) && !isFullDescription) {
                for (String column : columns) {
                    String newKeyName = getMappedName(column);
                    sb.append("<br/>");
                    sb.append("<b>");
                    sb.append(newKeyName + ": ");
                    sb.append("</b>");
                    sb.append(row.get(column));
                }
            } else {
                int isFirstColumn = 0;
                for (String column : columns) {
                    if (isFirstColumn++ > 0) {
                        sb.append(S_TokenSeparator);
                    }
                    sb.append(row.get(column));
                }
            }
            record.put(columnName, sb.toString().trim());
        }

        // check whether filter match the filter text
        String filter = record.getFilter();
        boolean isMatched = isMatchFilter(filter);
        logger.trace("Filter: [{}] Matched: [{}]", filter, isMatched ? "YES" : "NO");

        // if the filter mis-match then we don't need to continue
        if (!isMatched) {
            return null;
        }

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
        record.setContent(sb.toString());

        if (isFullDescription) {
            sb = new StringBuffer();
            columnNames = row.keySet();
            for (String key : columnNames) {
                String theKey = getMappedName(key);
                sb.append("<br/>");
                sb.append("<b>");
                sb.append(theKey + ": ");
                sb.append("</b>");
                sb.append(row.get(key));
            }
            record.setDescription(sb.toString());
        }

        // TO DO to perform the prefix, suffix, distance, filter, ...
        // category.fix, category.prefix, category.suffix
        if (configuration.getCategoryFixed() != null) {
            record.setCategory(configuration.getCategoryFixed());
        } else {
            if (configuration.getCategoryPrefix() != null || configuration.getCategorySuffix() != null) {
                String category = record.get(ConfigHelper.FN_FilterName);
                if (configuration.getCategoryPrefix() != null) {
                    category = configuration.getCategoryPrefix() + category;
                }
                if (configuration.getCategorySuffix() != null) {
                    category = category + configuration.getCategorySuffix();
                }
                record.setCategory(category);
            }
        }

        // title.prefix, title.suffix
        if (configuration.getTitlePrefix() != null || configuration.getTitleSuffix() != null) {
            String title = record.getTitle();
            if (configuration.getTitlePrefix() != null) {
                title = configuration.getTitlePrefix() + title;
            }
            if (configuration.getTitleSuffix() != null) {
                title = title + configuration.getTitleSuffix();
            }
            record.setTitle(title);
        }

        return record;
    }

    private boolean isMatchFilter(String filter) {

        if (filter == null || filter.length() == 0) {
            return false;
        }

        boolean negativeExpression = configuration.getFilterText().startsWith("!");
        String filterText = negativeExpression ? configuration.getFilterText().substring(1)
                : configuration.getFilterText();
        String pattern = PatternPrefix + filterText + PatternPostfix;
        logger.trace("Filter Pattern: " + pattern);
        boolean isMatched = filter.matches(pattern);
        return isMatched && negativeExpression == false || isMatched == false && negativeExpression == true;
    }

    private String getMappedName(String name) {

        Map<String, String> mappingColumns = ConfigHelper.getMappingColumns(name);
        if (mappingColumns == null) {
            return name;
        }

        String mappedName = mappingColumns.get(name);
        return mappedName != null ? mappedName : name;
    }

    private Double[][] calculateBoundingBox(Collection<MappedRecord> records, String distanceText) {

        double distance = new Double(distanceText).doubleValue();

        double south = 0.0;
        double north = 0.0;
        double west = 0.0;
        double east = 0.0;
        for (MappedRecord record : records) {
            double lat = Double.parseDouble(record.getLatitude());
            north = lat > 0 ? lat > north ? lat : north : lat < north ? lat : north;
            south = lat > 0 ? lat < south ? lat : south : lat > south ? lat : south;
            double lon = Double.parseDouble(record.getLongitude());
            west = lon > 0 ? lon < west ? lon : west : lon > west ? west : lon;
            east = lon > 0 ? lon > east ? lon : east : lon < east ? east : lon;
            if (south == 0) {
                south = north;
            }
            if (north == 0) {
                north = south;
            }
            if (east == 0) {
                east = west;
            }
            if (west == 0) {
                west = east;
            }
        }

        /*
         * Earth’s radius, sphere R=6378137 offsets in meters dn = 100 de = 100
         * Coordinate offsets in radians dLat = dn/R dLon =de/(R*Cos(Pi*lat/180))
         * OffsetPosition, decimal degrees latO = lat + dLat * 180/Pi lonO = lon + dLon
         * * 180/Pi
         */
        double d = distance * 1000.0;
        double deltaLat = d / Radius * 180 / Pi;
        north += deltaLat * (north > 0 ? 1 : -1);
        south -= deltaLat * (south > 0 ? 1 : -1);
        double northDelta = d / (Radius * Math.cos(Pi * north / 180.0)) * 180.0 / Pi;
        double northWestLon = west - northDelta;
        double northEastLon = east + northDelta;
        double southDelta = d / (Radius * Math.cos(Pi * south / 180.0)) * 180.0 / Pi;
        double southWestLon = west - southDelta;
        double southEastLon = east + southDelta;
        Double[][] boundingBox = new Double[5][2];
        boundingBox[0][0] = northWestLon;
        boundingBox[0][1] = north;
        boundingBox[1][0] = northEastLon;
        boundingBox[1][1] = north;
        boundingBox[2][0] = southEastLon;
        boundingBox[2][1] = south;
        boundingBox[3][0] = southWestLon;
        boundingBox[3][1] = south;
        boundingBox[4][0] = northWestLon;
        boundingBox[4][1] = north;

        return boundingBox;
    }

    public List<MappedRecordJson> getRecordList() {

        return this.recordJsonList;
    }
}
