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
package org.talend.dataquality.record.linkage.ui.composite.table;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.cwm.db.connection.DatabaseSQLExecutor;
import org.talend.cwm.db.connection.DelimitedFileSQLExecutor;
import org.talend.cwm.db.connection.ISQLExecutor;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.cwm.relational.TdColumn;
import org.talend.dataquality.record.linkage.ui.i18n.internal.DefaultMessagesImpl;
import orgomg.cwm.foundation.softwaredeployment.DataManager;
import orgomg.cwm.objectmodel.core.ModelElement;

/**
 * The data sample table on the column analysis
 *
 */
public class ColumnAnalysisDataSamTable extends DataSampleTable {

    private Logger log = Logger.getLogger(ColumnAnalysisDataSamTable.class);

    private String dataFilter = StringUtils.EMPTY;

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.record.linkage.ui.composite.table.DataSampleTable#createFixedColumns()
     */
    @Override
    protected Collection<? extends String> createFixedColumns(int columnSize) {
        return new ArrayList<String>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.record.linkage.ui.composite.table.DataSampleTable#getFixedColumnCount()
     */
    @Override
    protected int getFixedColumnCount() {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.record.linkage.ui.composite.table.DataSampleTable#isContainGID(java.util.List)
     */
    @Override
    protected boolean isContainGID(List<Object[]> results) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.dataquality.record.linkage.ui.composite.table.DataSampleTable#initTablePanelLayoutPanel(org.eclipse
     * .ui.forms.widgets.Section, org.eclipse.swt.layout.GridData,
     * org.talend.dataquality.record.linkage.ui.composite.table.DataSampleTable.TControl)
     */
    @Override
    protected void initTablePanelLayoutPanel(Composite dataTableComp, GridData layoutDataFillBoth, TControl tControl) {
        // when open the editor, the dataSampleSection's width is 0, just set the layout fill both.
        tablePanel.setLayoutData(layoutDataFillBoth);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.record.linkage.ui.composite.table.DataSampleTable#getPreviewData()
     */
    @Override
    protected List<Object[]> createPreviewData(ModelElement[] columns) {
        List<Object[]> previewData = new ArrayList<Object[]>();
        DataManager connection = null;
        boolean isDelimitedFile = false;
        // no columns be selected so that no data can be read
        if (columns == null || columns.length == 0) {
            return previewData;
        }
        // use ModelElement instead of node to get the data source type directly.
        // get connection from column[0]
        ModelElement modelElement = columns[0];
        if (modelElement instanceof MetadataColumn && !(modelElement instanceof TdColumn)) {
            isDelimitedFile = true;
            connection = ConnectionHelper.getTdDataProvider((MetadataColumn) modelElement);
        } else if (modelElement instanceof TdColumn) {
            connection = ConnectionHelper.getTdDataProvider((TdColumn) modelElement);
        } else {// other case it is not support by now
            log.warn(DefaultMessagesImpl.getString("ColumnAnalysisDataSamTable.UnSupportType")); //$NON-NLS-1$
            return previewData;
        }

        ISQLExecutor sqlExecutor = null;
        if (isDelimitedFile) {
            sqlExecutor = new DelimitedFileSQLExecutor();
        } else {// is database
            sqlExecutor = new DatabaseSQLExecutor();
        }
        try {
            // set limit
            sqlExecutor.setLimit(getLimitNumber());
            return sqlExecutor.executeQuery(connection, Arrays.asList(columns), dataFilter);
        } catch (SQLException e) {
            MessageDialog.openWarning(null, DefaultMessagesImpl.getString("ColumnAnalysisDataSamTable.InValidWhereClause"), //$NON-NLS-1$
                    e.getMessage());
            return previewData;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.dataquality.record.linkage.ui.composite.table.DataSampleTable#handleEmptyRow(orgomg.cwm.objectmodel
     * .core.ModelElement[], java.util.List)
     */
    @Override
    protected List<Object[]> handleEmptyRow(ModelElement[] columns, List<Object[]> listOfData) {
        return listOfData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.dataquality.record.linkage.ui.composite.table.DataSampleTable#handleGID(org.eclipse.swt.widgets.Composite
     * , java.util.List)
     */
    @Override
    protected TControl handleGID(Composite parentContainer, List<Object[]> results) {
        return null;
    }

    /**
     * DOC talend Comment method "setDataFilter".
     * 
     * @param dataFilterString
     */
    public void setDataFilter(String dataFilterString) {
        dataFilter = dataFilterString;

    }

}
