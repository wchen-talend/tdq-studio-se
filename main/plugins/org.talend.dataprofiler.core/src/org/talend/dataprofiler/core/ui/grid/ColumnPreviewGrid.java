// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprofiler.core.ui.grid;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.eclipse.nebula.widgets.grid.GridCellRenderer;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.talend.core.model.metadata.IMetadataConnection;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.cwm.helper.ColumnHelper;
import org.talend.cwm.relational.TdColumn;
import org.talend.dataprofiler.core.helper.ModelElementIndicatorHelper;
import org.talend.dataprofiler.core.i18n.internal.DefaultMessagesImpl;
import org.talend.dataprofiler.core.model.ColumnIndicator;
import org.talend.dataprofiler.core.model.ModelElementIndicator;
import org.talend.dataprofiler.core.ui.grid.utils.TDQObserver;
import org.talend.dataquality.PluginConstant;
import org.talend.dq.dbms.DbmsLanguage;
import org.talend.dq.dbms.DbmsLanguageFactory;
import org.talend.metadata.managment.utils.MetadataConnectionUtils;
import org.talend.utils.sugars.TypedReturnCode;
import orgomg.cwm.objectmodel.core.Expression;
import orgomg.cwm.objectmodel.core.ModelElement;

/**
 * created by talend on Dec 25, 2014 Detailled comment
 * 
 */
public class ColumnPreviewGrid extends AbstractIndicatorSelectGrid implements TDQObserver<AbstractIndicatorSelectGrid> {

    private Statement createStatement = null;

    private java.sql.Connection sqlConn = null;

    private final String COLUMN_RESULT_KEY = "columnResult"; //$NON-NLS-1$

    /**
     * DOC talend ColumnPreviewGrid constructor comment.
     * 
     * @param dialog
     * @param parent
     * @param style
     * @param modelElementIndicators
     */
    public ColumnPreviewGrid(IndicatorSelectDialog2 dialog, Composite parent, int style,
            ModelElementIndicator[] modelElementIndicators) {
        super(dialog, parent, style, modelElementIndicators);
    }

    /**
     * DOC talend ColumnPreviewGrid constructor comment.
     * 
     * @param dialog
     * @param parent
     * @param style
     * @param modelElementIndicators
     */
    public ColumnPreviewGrid(IndicatorSelectDialog3 dialog, Composite parent, int style,
            ModelElementIndicator[] modelElementIndicators, int limit) {
        super(dialog, parent, style, modelElementIndicators, limit);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.ui.grid.AbstractIndicatorSelectGrid#createTableContent()
     */
    @Override
    protected void createTableContent() {
        try {
            GridItem PreviewItem = new GridItem(this, SWT.NONE);
            for (int i = 0; i < this.getColumnCount(); i++) {
                PreviewItem.setCheckable(i, false);
            }
            PreviewItem.setText(DefaultMessagesImpl.getString("ColumnPreviewGrid.PreviewItemLabel")); //$NON-NLS-1$
            processNodePrivew(null, PreviewItem);
        } catch (SQLException e) {

        } finally {
            try {
                if (sqlConn != null) {
                    sqlConn.close();
                }
            } catch (SQLException e) {

            }
        }
    }

    /**
     * DOC talend Comment method "processNodePrivew".
     * 
     * @param object
     * @param previewItem
     * @throws SQLException
     */
    private void processNodePrivew(Object object, GridItem parentItem) throws SQLException {
        if (!checkSameTable()) {
            return;
        }
        for (int j = 0; j < limitNumber; j++) {
            GridItem currentItem = new GridItem(parentItem, SWT.NONE);
            currentItem.setCheckable(0, false);
            currentItem.setCheckable(1, false);
            for (int i = 2; i < this.getColumnCount(); i++) {
                GridColumn column = getColumn(i);
                currentItem.setCheckable(i, false);
                String columnValue = getColumnValue(column, currentItem);
                if (columnValue == null) {
                    this.remove(this.indexOf(currentItem));
                    return;
                }
                currentItem.setText(i, columnValue);
            }
        }
    }

    /**
     * DOC talend Comment method "checkSameTable".
     * 
     * @return
     */
    private boolean checkSameTable() {
        List<MetadataColumn> columnsList = ModelElementIndicatorHelper.getColumns(_modelElementIndicators);
        return ColumnHelper.checkSameTable(columnsList.toArray(new ModelElement[columnsList.size()]));
    }

    /**
     * DOC talend Comment method "getColumnValue".
     * 
     * @param column
     * @param currentItem
     * @return
     * @throws SQLException
     */
    private String getColumnValue(GridColumn column, GridItem currentItem) throws SQLException {
        ResultSet rs = (ResultSet) this.getData(COLUMN_RESULT_KEY);
        ModelElementIndicator modelElementIndicator = (ModelElementIndicator) column.getData();
        // connection
        Connection tdDataProvider = ModelElementIndicatorHelper.getTdDataProvider(modelElementIndicator);
        ColumnIndicator columnIndicator = ModelElementIndicatorHelper.switchColumnIndicator(modelElementIndicator);
        TdColumn tdColumn = columnIndicator.getTdColumn();
        // row index
        int indexOfRow = this.indexOf(currentItem);
        if (rs == null) {

            DbmsLanguage dbmsLanguage = DbmsLanguageFactory.createDbmsLanguage(tdDataProvider);
            Expression columnQueryExpression = dbmsLanguage.getTableQueryExpression(tdColumn, _dialog.getWhereExpression());
            IMetadataConnection metadataBean = ConvertionHelper.convert(tdDataProvider);

            createStatement = initStatement(metadataBean);
            rs = createStatement.executeQuery(columnQueryExpression.getBody());
            this.setData(COLUMN_RESULT_KEY, rs);
        }
        while (rs.absolute(indexOfRow)) {
            Object columnValue = rs.getObject(tdColumn.getName());
            return columnValue == null ? PluginConstant.NULL_STRING : columnValue.toString();
        }
        return null;
    }

    /**
     * DOC talend Comment method "getColumnValue".
     * 
     * @param column
     * @param currentItem
     * @return
     * @throws SQLException
     */
    private String getColumnValue(GridColumn column, int rowNumber) throws SQLException {
        ResultSet rs = (ResultSet) this.getData(COLUMN_RESULT_KEY);
        ModelElementIndicator modelElementIndicator = (ModelElementIndicator) column.getData();
        // connection
        Connection tdDataProvider = ModelElementIndicatorHelper.getTdDataProvider(modelElementIndicator);
        ColumnIndicator columnIndicator = ModelElementIndicatorHelper.switchColumnIndicator(modelElementIndicator);
        TdColumn tdColumn = columnIndicator.getTdColumn();
        // row index
        int indexOfRow = rowNumber;
        if (rs == null) {

            DbmsLanguage dbmsLanguage = DbmsLanguageFactory.createDbmsLanguage(tdDataProvider);
            Expression columnQueryExpression = dbmsLanguage.getTableQueryExpression(tdColumn, _dialog.getWhereExpression());
            IMetadataConnection metadataBean = ConvertionHelper.convert(tdDataProvider);

            createStatement = initStatement(metadataBean);
            rs = createStatement.executeQuery(columnQueryExpression.getBody());
            this.setData(COLUMN_RESULT_KEY, rs);
        }
        while (rs.absolute(indexOfRow + 1)) {
            Object columnValue = rs.getObject(tdColumn.getName());
            return columnValue == null ? PluginConstant.NULL_STRING : columnValue.toString();
        }
        return null;
    }

    /**
     * DOC talend Comment method "getColumnValue".
     * 
     * @return
     * @throws SQLException
     */
    protected String getColumnValue(int rowIndex, String sqlQuery, String columnName, IMetadataConnection metadataBean)
            throws SQLException {

        createStatement = initStatement(metadataBean);
        ResultSet rs = createStatement.executeQuery(sqlQuery);
        while (rs.absolute(rowIndex + 1)) {
            return rs.getObject(columnName).toString();
        }
        return null;
    }

    /**
     * DOC talend Comment method "getStatement".
     * 
     * @param metadataBean
     * @return
     * @throws SQLException
     */
    protected Statement initStatement(IMetadataConnection metadataBean) throws SQLException {
        if (createStatement != null && !createStatement.isClosed()) {
            return createStatement;
        }
        if (sqlConn == null || sqlConn.isClosed()) {
            TypedReturnCode<java.sql.Connection> createConnection = MetadataConnectionUtils.createConnection(metadataBean, false);
            if (!createConnection.isOk()) {
                return null;
            }
            sqlConn = createConnection.getObject();
        }

        createStatement = sqlConn.createStatement();
        return createStatement;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.ui.grid.utils.TalendObserver#Update(org.talend.dataprofiler.core.ui.grid.utils.
     * Observerable)
     */
    public void update(AbstractIndicatorSelectGrid observer) {
        this.setItemHeaderWidth(observer.getItemHeaderWidth());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.ui.grid.AbstractIndicatorSelectGrid#getCellRenderer()
     */
    @Override
    protected GridCellRenderer getCellRenderer() {
        return new AbstractTdCellRenderer();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.ui.grid.AbstractIndicatorSelectGrid#redrawTable()
     */
    @Override
    protected void redrawTable(IIndicatorSelectDialog dialog) {
        GridData previewGridData = (GridData) this.getLayoutData();
        if (previewGridData.minimumHeight == this.getItemHeight()) {
            previewGridData.minimumHeight = this.getItemHeight() * 10;
        } else {
            previewGridData.minimumHeight = this.getItemHeight();
        }
        dialog.getDialogComposite().layout();
    }

}
