package com.spotonresponse.adapter.model;

import com.google.gson.GsonBuilder;
import org.json.JSONObject;

import javax.xml.bind.DatatypeConverter;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.AbstractMap;
import java.util.Map;

public class MappedRecordJson extends JSONObject {

    /*
    Source -> {The name of your program}
    SourceHost -> {Where your program is running}
    SourceURL -> {URL to view/manage source (if applicable)}
    SourceContact -> {Name of person or group to contact about source data if needed (if applicable)}
    SourceEmail -> {Email of SourceContact (if applicable)}
     */
    private static final String S_Source = "Source";
    private static final String S_SourceHost = "SourceHost";
    private static final String S_SourceURL = "SourceURL";
    private static final String S_SourceContact = "SourceConteact";
    private static final String S_SourceEmail = "SourceEmail";
    private static final String S_CSVAdapter = "CSV Adapter";
    private static final String S_NA = "N/A";
    private static final String S_Title = "title";
    private static final String S_MD5HASH = "md5hash";
    private static final String[] removeEntries = {
        "coreUri",
        "longitude",
        "latitude",
        // "workProductID",
    };

    private String host;
    private String path;

    public MappedRecordJson(MappedRecord record) {

        super(new GsonBuilder().setPrettyPrinting().create().toJson(record));

        setWhere(record.getLatitude(), record.getLongitude());
        parseUrl(record.getCoreUri());

        this.put(S_Source, S_CSVAdapter);
        this.put(S_SourceHost, this.host);
        this.put(S_SourceURL, this.path);
        // this.put(S_SourceContact, S_NA);
        // this.put(S_SourceEmail, S_NA);
        this.put(S_Title, record.getTitle());
        this.put(S_MD5HASH, getHash(record.getIndex().getBytes()));
        clearUp();
    }

    private void parseUrl(String url) {

        try {
            URL aURL = new URL(url);
            this.host = aURL.getHost();
            this.path = aURL.getPath();
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void clearUp() {

        for (String key : removeEntries) {
            this.remove(key);
        }
    }

    private String getHash(byte[] byes) {

        MessageDigest md5hash = null;
        try {
            md5hash = MessageDigest.getInstance("MD5");
        }
        catch (Exception e) {
            return null;
        }
        md5hash.update(byes);
        byte[] digest = md5hash.digest();
        return DatatypeConverter.printHexBinary(digest).toUpperCase();
    }

    private void setWhere(String lat, String lon) {

        JSONObject where = new JSONObject();
        JSONObject point = new JSONObject();
        point.put("pos", lat + " " + lon);
        where.put("Point", point);
        this.put("where", where);
    }

    public String getTitle() {

        return (String) this.get(S_Title);
    }

    public String getPrimaryKey() {

        return (String) this.get(S_MD5HASH);
    }

    public Map.Entry getMapEntry() {

        return new AbstractMap.SimpleImmutableEntry(getTitle(), getPrimaryKey());
    }
}

