package com.crd.msreconciliation.utils;

import com.opencsv.CSVWriter;

import java.io.Writer;

public class CustomCSVWriter extends CSVWriter {


    public CustomCSVWriter(Writer writer) {
        super(writer);
    }
    @Override
    public void writeNext(String [] nextLine){
        this.writeNext(nextLine, false);
    }
}
