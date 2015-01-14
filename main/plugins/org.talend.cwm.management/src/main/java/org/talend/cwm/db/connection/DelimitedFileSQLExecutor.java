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
package org.talend.cwm.db.connection;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.EList;
import org.talend.core.model.metadata.builder.connection.DelimitedFileConnection;
import org.talend.core.model.metadata.builder.connection.Escape;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.database.JavaSqlFactory;
import org.talend.cwm.helper.ColumnHelper;
import org.talend.dataquality.matchmerge.Record;
import org.talend.dq.helper.AnalysisExecutorHelper;
import org.talend.dq.helper.FileUtils;
import org.talend.fileprocess.FileInputDelimited;
import orgomg.cwm.foundation.softwaredeployment.DataManager;
import orgomg.cwm.objectmodel.core.ModelElement;

import com.talend.csv.CSVReader;

/**
 * DOC yyin class global comment. Detailled comment
 */
public class DelimitedFileSQLExecutor extends SQLExecutor {

    private static Logger log = Logger.getLogger(DelimitedFileSQLExecutor.class);

    // if limit = 0. do not use the limit, only when it is set >0, use the limit
    private int limit = 0;

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.cwm.db.connection.ISQLExecutor#executeQuery(org.talend.dataquality.analysis.Analysis)
     */
    public List<Object[]> executeQuery(DataManager connection, List<ModelElement> analysedElements) throws SQLException {
        return executeQuery(connection, analysedElements, null);
    }

    /**
     * DOC yyin Comment method "useFileInputDelimited".
     * 
     * @param analysedElements
     * @param delimitedFileconnection
     * @throws IOException
     * @throws Exception
     */
    private void useFileInputDelimited(List<ModelElement> analysedElements, DelimitedFileConnection delimitedFileconnection)
            throws IOException, Exception {
        int[] analysedColumnIndex = getAnalysedColumnPositionInFileTable(analysedElements);
        FileInputDelimited fileInputDelimited = AnalysisExecutorHelper.createFileInputDelimited(delimitedFileconnection);
        int index = 0;
        while (fileInputDelimited.nextRecord()) {
            index++;
            int columsCount = analysedElements.size();
            String[] rowValues = new String[columsCount];
            for (int i = 0; i < columsCount; i++) {
                rowValues[i] = fileInputDelimited.get(analysedColumnIndex[i]);
            }
            handleRow(rowValues);
            if (limit > 0 && index >= limit) {
                break;
            }

        }
        fileInputDelimited.close();
    }

    private int[] getAnalysedColumnPositionInFileTable(List<ModelElement> analysedElements) {
        // find the position of the analysed elements in the file table's column
        int analysedColumnIndex[] = new int[analysedElements.size()];
        MetadataColumn mColumn = (MetadataColumn) analysedElements.get(0);
        MetadataTable metadataTable = ColumnHelper.getColumnOwnerAsMetadataTable(mColumn);
        EList<MetadataColumn> columns = metadataTable.getColumns();
        int colIndex = 0;
        for (ModelElement analysedColumn : analysedElements) {
            for (int i = 0; i < columns.size(); i++) {
                if (columns.get(i).getLabel().equals(analysedColumn.getName())) {
                    analysedColumnIndex[colIndex++] = i;
                    break;
                }
            }
        }
        return analysedColumnIndex;
    }

    private void useCsvReader(File file, DelimitedFileConnection delimitedFileconnection, List<ModelElement> analysisElementList) {
        int limitValue = JavaSqlFactory.getLimitValue(delimitedFileconnection);
        int headValue = JavaSqlFactory.getHeadValue(delimitedFileconnection);
        CSVReader csvReader = null;
        try {
            csvReader = FileUtils.createCsvReader(file, delimitedFileconnection);
            FileUtils.initializeCsvReader(delimitedFileconnection, csvReader);

            int analysedColumnIndex[] = new int[analysisElementList.size()];
            // need to find the analysed element position , and only get these analysed column's values.
            List<String> columnLabels = new ArrayList<String>();
            for (int i = 0; i < headValue && csvReader.readNext(); i++) {
                Collections.addAll(columnLabels, csvReader.getValues());
            }
            for (int j = 0; j < analysisElementList.size(); j++) {
                analysedColumnIndex[j] = columnLabels.indexOf(analysisElementList.get(j).getName());
            }// ~

            long currentRecord = 0;
            while (csvReader.readNext()) {
                currentRecord++;
                if (limitValue != -1 && currentRecord > limitValue) {
                    break;
                }
                // only get the analysed columns' values
                String[] values = csvReader.getValues();
                String[] analysedValues = new String[analysisElementList.size()];
                for (int i = 0; i < analysedColumnIndex.length; i++) {
                    analysedValues[i] = values[analysedColumnIndex[i]];
                }
                handleRow(analysedValues);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (csvReader != null) {
                try {
                    csvReader.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.cwm.db.connection.ISQLExecutor#setLimit(int)
     */
    public void setLimit(int limit) {
        this.limit = limit;

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.cwm.db.connection.ISQLExecutor#getResultSetIterator(orgomg.cwm.foundation.softwaredeployment.DataManager
     * , java.util.List)
     */
    public Iterator<Record> getResultSetIterator(DataManager connection, List<ModelElement> analysedElements) {

        return new DelimitedFileIterator((DelimitedFileConnection) connection, analysedElements);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.cwm.db.connection.ISQLExecutor#executeQuery(orgomg.cwm.foundation.softwaredeployment.DataManager,
     * java.util.List, java.lang.String)
     */
    public List<Object[]> executeQuery(DataManager connection, List<ModelElement> analysedElements, String where)
            throws SQLException {
        dataFromTable.clear();
        try {
            beginQuery();
        } catch (Exception e1) {
            log.error(e1.getMessage(), e1);
            return dataFromTable;
        }
        DelimitedFileConnection delimitedFileconnection = (DelimitedFileConnection) connection;
        String path = JavaSqlFactory.getURL(delimitedFileconnection);
        IPath iPath = new Path(path);

        try {
            File file = iPath.toFile();
            if (!file.exists()) {
                return null;
            }

            if (Escape.CSV.equals(delimitedFileconnection.getEscapeType())) {
                useCsvReader(file, delimitedFileconnection, analysedElements);
            } else {
                useFileInputDelimited(analysedElements, delimitedFileconnection);
            }
            endQuery();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return dataFromTable;
    }

}
