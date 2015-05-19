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
package org.talend.dataprofiler.core.helper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.talend.core.model.metadata.IMetadataConnection;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.cwm.helper.TableHelper;
import org.talend.cwm.relational.TdColumn;
import org.talend.dataprofiler.core.model.ColumnIndicator;
import org.talend.dq.dbms.DbmsLanguage;
import org.talend.dq.dbms.DbmsLanguageFactory;
import org.talend.metadata.managment.utils.MetadataConnectionUtils;
import org.talend.utils.sugars.TypedReturnCode;
import orgomg.cwm.objectmodel.core.Expression;

/**
 * DOC talend class global comment. Detailled comment
 */
public class ResultSetHelper {

    public static ResultSet getResultSet(ColumnIndicator columnIndicator, String whereExpression) throws SQLException {
        // connection
        Connection tdDataProvider = ModelElementIndicatorHelper.getTdDataProvider(columnIndicator);
        TdColumn tdColumn = columnIndicator.getTdColumn();
        DbmsLanguage dbmsLanguage = DbmsLanguageFactory.createDbmsLanguage(tdDataProvider);
        Expression columnQueryExpression = dbmsLanguage.getTableQueryExpression(tdColumn, whereExpression);
        IMetadataConnection metadataBean = ConvertionHelper.convert(tdDataProvider);

        Statement createStatement = initStatement(metadataBean);
        return createStatement.executeQuery(columnQueryExpression.getBody());
    }

    public static ResultSet getResultSet(MetadataTable metadataTable, String whereExpression) throws SQLException {
        // connection
        Connection tdDataProvider = TableHelper.getFirstConnection(metadataTable);
        DbmsLanguage dbmsLanguage = DbmsLanguageFactory.createDbmsLanguage(tdDataProvider);
        Expression columnQueryExpression = dbmsLanguage.getTableQueryExpression(metadataTable, whereExpression);
        IMetadataConnection metadataBean = ConvertionHelper.convert(tdDataProvider);

        Statement createStatement = initStatement(metadataBean);
        return createStatement.executeQuery(columnQueryExpression.getBody());
    }

    public static ResultSet getResultSet(ColumnIndicator columnIndicator, String whereExpression, Statement createStatement)
            throws SQLException {
        Connection tdDataProvider = ModelElementIndicatorHelper.getTdDataProvider(columnIndicator);
        TdColumn tdColumn = columnIndicator.getTdColumn();
        DbmsLanguage dbmsLanguage = DbmsLanguageFactory.createDbmsLanguage(tdDataProvider);
        Expression columnQueryExpression = dbmsLanguage.getTableQueryExpression(tdColumn, whereExpression);
        return createStatement.executeQuery(columnQueryExpression.getBody());
    }

    /**
     * DOC talend Comment method "getStatement".
     * 
     * @param metadataBean
     * @return
     * @throws SQLException
     */
    private static Statement initStatement(IMetadataConnection metadataBean) throws SQLException {
        Statement createStatement = null;
        java.sql.Connection sqlConn = null;

        TypedReturnCode<java.sql.Connection> createConnection = MetadataConnectionUtils.createConnection(metadataBean, false);
        if (!createConnection.isOk()) {
            return null;
        }
        sqlConn = createConnection.getObject();

        createStatement = sqlConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        return createStatement;
    }
}
