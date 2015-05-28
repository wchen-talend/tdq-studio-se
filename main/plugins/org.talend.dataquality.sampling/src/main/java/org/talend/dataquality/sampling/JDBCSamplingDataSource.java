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

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

/**
 * created by zhao Sampling datasource regarding jdbc connection. <br>
 * the parameter ResultSet should be closed by the caller who set it.
 *
 */
public class JDBCSamplingDataSource implements SamplingDataSource<ResultSet> {

    private static Logger log = Logger.getLogger(JDBCSamplingDataSource.class);

    private ResultSet jdbcResultSet = null;

    private int columnSize = 0;

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dq.datascience.SamplingDataSource#setDataSource(java.lang.Object)
     */
    @Override
    public void setDataSource(ResultSet ds) {
        jdbcResultSet = ds;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dq.datascience.SamplingDataSource#getDatasize()
     */
    @Override
    public boolean hasNext() throws Exception {
        try {
            return jdbcResultSet.next();
        } catch (SQLException e) {
            throw new Exception(e);
        }
    }

    /**
     * 
     * DOC zhao Set column size .
     * 
     * @param columnSize the size of the columns in a record.
     */
    public void setColumnSize(int columnSize) {
        this.columnSize = columnSize;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dq.datascience.SamplingDataSource#getRecord()
     */
    @Override
    public Object[] getRecord() throws Exception {
        try {
            Object[] oneRow = new Object[columnSize];
            // --- for each column
            for (int i = 0; i < columnSize; i++) {
                // --- get content of column
                Object object = null;
                try {
                    oneRow[i] = jdbcResultSet.getObject(i + 1);
                } catch (SQLException e) {
                    if (NULLDATE.equals(jdbcResultSet.getString(i + 1))) {
                        oneRow[i] = null;
                    } else {
                        throw new Exception(e);
                    }
                }
            }
            return oneRow;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private static final String NULLDATE = "0000-00-00 00:00:00"; //$NON-NLS-1$

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.sampling.SamplingDataSource#finalizeDataSampling()
     */
    @Override
    public boolean finalizeDataSampling() throws Exception {
        // Noting to do.
        return true;
    }
}
