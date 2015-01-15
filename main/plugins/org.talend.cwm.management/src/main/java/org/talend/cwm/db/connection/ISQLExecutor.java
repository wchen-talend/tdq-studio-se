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

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.dataquality.analysis.Analysis;
import org.talend.dataquality.indicators.columnset.RecordMatchingIndicator;
import org.talend.dataquality.matchmerge.Record;
import orgomg.cwm.foundation.softwaredeployment.DataManager;
import orgomg.cwm.objectmodel.core.ModelElement;

/**
 * DOC yyin class global comment. Detailled comment
 */
public interface ISQLExecutor {

    public List<Object[]> executeQuery(DataManager connection, List<ModelElement> analysedElements) throws SQLException;

    public List<Object[]> executeQuery(DataManager connection, List<ModelElement> analysedElements, String where)
            throws SQLException;

    void setLimit(int limit);

    public void setStoreOnDisk(Boolean storeOnDisk);

    public void initStoreOnDiskHandler(Analysis analysis, RecordMatchingIndicator recordMatchingIndicator,
            Map<MetadataColumn, String> columnMap);

    public Boolean getStoreOnDisk();

    public StoreOnDiskHandler getStoreOnDiskHandler();

    public Iterator<Record> getResultSetIterator(DataManager connection, List<ModelElement> analysedElements) throws SQLException;
}
