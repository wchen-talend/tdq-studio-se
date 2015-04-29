// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataquality.sampling;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

import com.talend.csv.CSVReader;

/**
 * File delimited data.
 *
 */
public class FileSamplingDataSource implements SamplingDataSource<File> {

    private static Logger log = Logger.getLogger(FileSamplingDataSource.class);

    private CSVReader csvReader = null;

    private File sourceFile = null;

    private String fieldSeperator = ","; //$NON-NLS-1$

    private String encoding = "UTF-8"; //$NON-NLS-1$

    private String rowSep = "\\n"; //$NON-NLS-1$

    private String textEnclosure = "\""; //$NON-NLS-1$

    private String escapeChar = "\\"; //$NON-NLS-1$

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dq.datascience.SamplingDataSource#setDataSource(java.lang.Object)
     */
    @Override
    public void setDataSource(File ds) {
        sourceFile = ds;
    }

    public void setFieldSeperator(String sep) {
        this.fieldSeperator = sep;
    }

    /**
     * Sets the rowSep.
     * 
     * @param rowSep the rowSep to set
     */
    public void setRowSep(String rowSep) {
        this.rowSep = rowSep;
    }

    /**
     * Sets the textEnclosure.
     * 
     * @param textEnclosure the textEnclosure to set
     */
    public void setTextEnclosure(String textEnclosure) {
        this.textEnclosure = textEnclosure;
    }

    /**
     * Sets the escapeChar.
     * 
     * @param escapeChar the escapeChar to set
     */
    public void setEscapeChar(String escapeChar) {
        this.escapeChar = escapeChar;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dq.datascience.SamplingDataSource#getDatasize()
     */
    @Override
    public boolean hasNext() throws Exception {
        boolean hasNext = false;
        try {
            if (csvReader == null) {
                if (sourceFile != null) {
                    csvReader = createCsvReader(sourceFile);
                } else {
                    return false;
                }
            }
            hasNext = csvReader.readNext();
            if (!hasNext) {
                csvReader.close();
                csvReader = null;
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new Exception(e);
        }
        return hasNext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dq.datascience.SamplingDataSource#getRecord()
     */
    @Override
    public Object[] getRecord() {
        return csvReader.getValues();
    }

    /**
     * DOC zhao create csv reader given file, field seperator , encoding and row seperator .
     * 
     * @param file
     * @param fieldSeperator field seperator
     * @param encoding file encoding
     * @param rowSep row seperator
     * @param textEnclosure text encolosure
     * @param escapeChar escapse charactor.
     * @return CSVReader instance.
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    private CSVReader createCsvReader(File file) throws UnsupportedEncodingException, FileNotFoundException {

        CSVReader csvReaderNew = new CSVReader(new BufferedReader(new InputStreamReader(new java.io.FileInputStream(file),
                encoding)), fieldSeperator.charAt(0));
        csvReaderNew.setSeparator(rowSep.charAt(0));

        csvReaderNew.setSkipEmptyRecords(true);
        if (textEnclosure != null && textEnclosure.length() > 0) {
            csvReaderNew.setQuoteChar(textEnclosure.charAt(0));
        }
        if (escapeChar != null) {
            csvReaderNew.setEscapeChar(escapeChar.charAt(0));
        }
        return csvReaderNew;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.sampling.SamplingDataSource#finalizeDataSampling()
     */
    @Override
    public boolean finalizeDataSampling() throws Exception {
        if (csvReader != null) {
            try {
                csvReader.close();
            } catch (IOException e) {
                throw new Exception(e);
            }
        }
        return true;
    }

}