package com.spotonresponse.adapter.process;

import au.com.bytecode.opencsv.CSVReader;

import java.io.IOException;
import java.io.Reader;

/*
 * This readNext() will trim the token without leading/trailing spaces
 */
public class XCCSVReader extends CSVReader {

    public XCCSVReader(Reader reader) {
        super(reader);
    }

    @Override
    public String[] readNext() throws IOException {

        final String[] line = super.readNext();
        String[] tokens = null;
        if (line != null && line.length > 0) {
            tokens = new String[line.length];
            for (int i = 0; i < line.length; i++) {
                tokens[i] = line[i].trim();
            }
        }
        return tokens;
    }
}
