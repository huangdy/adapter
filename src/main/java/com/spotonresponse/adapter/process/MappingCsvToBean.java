package com.spotonresponse.adapter.process;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.bean.CsvToBean;
import au.com.bytecode.opencsv.bean.MappingStrategy;
import com.spotonresponse.adapter.model.Configuration;
import com.spotonresponse.adapter.model.MappedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.List;

public class MappingCsvToBean extends CsvToBean<MappedRecord> {

    private static final Logger logger = LoggerFactory.getLogger(MappingCsvToBean.class);

    private static final String TokenSeparator = ":";
    private final String[][] columnNames;
    private final Integer[][] columnIndexes;
    private Configuration configuration = null;
    private boolean autoClose = false;

    public MappingCsvToBean(Configuration configMap) {

        configuration = configMap;
        setAutoClose(configMap.isAutoClose());
        int columns = Configuration.DefinedColumnNames.length;
        columnNames = new String[columns][];
        columnIndexes = new Integer[columns][];
        for (int i = 0; i < columns; i++) {
            // if the attribute is not defined the skip
            if (configMap.getFieldValue(Configuration.DefinedColumnNames[i]) == null) {
                continue;
            }
            columnNames[i] = configMap.getFieldValue(Configuration.DefinedColumnNames[i]).split("[.]", -1);
            columnIndexes[i] = new Integer[columnNames[i].length];
        }
    }

    // figure out the index key in column order
    private void figureOutMultiColumnField(String[] headers) {

        for (int i = 0; i < columnNames.length; i++) {
            if (columnNames[i] == null || columnNames[i].length == 1) {
                continue;
            }
            for (int j = 0; j < columnNames[i].length; j++) {
                for (int k = 0; k < headers.length; k++) {
                    if (columnNames[i][j].equalsIgnoreCase(headers[k])) {
                        columnIndexes[i][j] = k;
                        break;
                    }
                }
            }
        }
    }

    public boolean isAutoClose() {

        return autoClose;
    }

    private void setAutoClose(boolean autoClose) {

        this.autoClose = autoClose;
    }

    @Override
    public List<MappedRecord> parse(MappingStrategy<MappedRecord> mapper, CSVReader csvReader) {

        try {
            mapper.captureHeader(csvReader);

            figureOutMultiColumnField(((MappingHeaderColumnNameTranslateMappingStrategy) mapper).getHeaders());

            String[] columns;
            List<MappedRecord> list = new ArrayList<MappedRecord>();
            while (null != (columns = csvReader.readNext())) {
                MappedRecord bean = processLine(mapper, columns);
                postProcessing(bean, columns);
                list.add(bean);
            }
            return list;
        }
        catch (Exception e) {
            throw new RuntimeException("Error parsing CSV!" + e.getMessage());
        }
    }

    @Override
    protected Object convertValue(String value, PropertyDescriptor prop) throws InstantiationException, IllegalAccessException {

        PropertyEditor editor = getPropertyEditor(prop);
        Object obj = value;
        if (null != editor) {
            try {
                editor.setAsText(value);
            }
            catch (NumberFormatException e) {
                logger.warn("Value: [" + value + "]: " + e.getMessage());
                editor.setAsText("0");
            }
            obj = editor.getValue();
        }
        return obj;
    }

    private String getRecordValue(MappedRecord record, Configuration configuration, String attributeName, String[] columnValues, Integer columnIndex) {

        if (attributeName.equals(configuration.getDescription())) {
            return record.getDescription();
        } else if (attributeName.equals(configuration.getCategory())) {
            return record.getCategory();
        } else if (attributeName.equals(configuration.getTitle())) {
            return record.getTitle();
        } else if (attributeName.equals(configuration.getFilter())) {
            return record.getFilter();
        } else {
            return columnValues[columnIndex];
        }
    }

    // figure out the multi-column fields: index, description
    private void postProcessing(MappedRecord record, String[] columns) {

        // fill the empty fields
        if (record.getCategory().equals("N/A")) {
            record.setCategory(
                getAttributeValue(record, configuration.getDuplicateAttributeValue(Configuration.FN_Category)));
        }
        if (record.getFilter().equals("N/A")) {
            record.setFilter(
                getAttributeValue(record, configuration.getDuplicateAttributeValue(Configuration.FN_FilterName)));
        }
        if (record.getIndex().equals("N/A")) {
            record.setIndex(
                getAttributeValue(record, configuration.getDuplicateAttributeValue(Configuration.FN_Index)));
        }
        if (record.getDescription().equals("N/A")) {
            record.setDescription(
                getAttributeValue(record, configuration.getDuplicateAttributeValue(Configuration.FN_Description)));
        }

        // figure out the content which is the whole row of data
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        for (String column : columns) {
            sb.append(column + TokenSeparator);
        }
        String value = sb.toString();
        value = value.substring(0, value.lastIndexOf(TokenSeparator));
        record.setContent(value + "]");

        // figure out the value for the multi-column field
        for (int i = 0; i < columnNames.length; i++) {
            if (columnNames[i] == null || columnNames[i].length == 1) {
                continue;
            }
            final boolean isDescription = Configuration.DefinedColumnNames[i].equalsIgnoreCase(
                Configuration.FN_Description);
            sb = new StringBuffer();
            for (int j = 0; j < columnNames[i].length; j++) {
                if (isDescription) {
                    sb.append("<br/>");
                    sb.append("<b>");
                    sb.append(Configuration.getMappingColumn(columnNames[i][j]) + ": ");
                    sb.append("</b>");
                    sb.append(getRecordValue(record, configuration, columnNames[i][j], columns, columnIndexes[i][j]));
                } else {
                    if (columnNames[i][j] == null) {
                        sb.append("N/A");
                    } else {
                        sb.append(
                            getRecordValue(record, configuration, columnNames[i][j], columns, columnIndexes[i][j]));
                    }
                    sb.append(TokenSeparator);
                }
            }
            value = sb.toString();
            if (isDescription == false) {
                value = value.substring(0, value.lastIndexOf(TokenSeparator));
            }

            if (Configuration.DefinedColumnNames[i].equalsIgnoreCase(Configuration.FN_Category)) {
                record.setCategory(value);
            } else if (Configuration.DefinedColumnNames[i].equalsIgnoreCase(Configuration.FN_Title)) {
                record.setTitle(value);
            } else if (Configuration.DefinedColumnNames[i].equalsIgnoreCase(Configuration.FN_FilterName)) {
                record.setFilter(value);
            } else if (Configuration.DefinedColumnNames[i].equalsIgnoreCase(Configuration.FN_Description)) {
                record.setDescription(value);
            } else if (Configuration.DefinedColumnNames[i].equalsIgnoreCase(Configuration.FN_Index)) {
                record.setIndex(value);
            }
        }
        // logger.debug("+++++++++++++++++++\npostProcessing:\n" + record + "\n+++++++++++++++++++++++++++++");
    }

    private String getAttributeValue(MappedRecord record, String attributeName) {

        if (attributeName == null) {
            return "N/A";
        } else if (attributeName.equals(Configuration.FN_Title)) {
            return record.getTitle();
        } else if (attributeName.equals(Configuration.FN_Index)) {
            return record.getIndex();
        } else if (attributeName.equals(Configuration.FN_Category)) {
            return record.getCategory();
        } else if (attributeName.equals(Configuration.FN_Description)) {
            return record.getDescription();
        } else {
            return "N/A";
        }
    }
}
