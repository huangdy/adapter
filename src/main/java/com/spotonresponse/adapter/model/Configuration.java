package com.spotonresponse.adapter.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@Entity
@Table(name = "configuration")
public class Configuration {

    @Id
    private String id;
    private String title;
    private String titlePrefix = null;
    private String titleSuffix = null;
    private String category;
    private String filter;
    private String filterText;
    private String distance = "";
    private String distanceFilterText = "";
    private String latitude;
    private String longitude;
    private String categoryPrefix = null;
    private String categorySuffix = null;
    private String categoryFixed = null;
    private String json_ds;
    private String uri;
    private String username;
    private String password;
    private String mappingColumns;
    @Column(columnDefinition = "VARCHAR(65536)")
    private String description = "title.category";
    @Column(columnDefinition = "VARCHAR(65536)")
    private String index = "title.category.latitude.longitude";
    private boolean autoClose = true;
    private boolean fullDescription = false;
    private String redirectUrl = "http://www.google.com";
}
