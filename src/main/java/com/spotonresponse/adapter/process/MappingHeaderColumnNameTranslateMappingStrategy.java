package com.spotonresponse.adapter.process;

import au.com.bytecode.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;
import com.spotonresponse.adapter.model.MappedRecord;

public class MappingHeaderColumnNameTranslateMappingStrategy extends HeaderColumnNameTranslateMappingStrategy<MappedRecord> {

    public String[] getHeaders() {

        return header;
    }
}
