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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Display;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.database.JavaSqlFactory;
import org.talend.cwm.helper.SwitchHelpers;
import org.talend.cwm.management.i18n.Messages;
import org.talend.cwm.relational.TdColumn;
import org.talend.dataquality.matchmerge.Record;
import org.talend.dataquality.record.linkage.iterator.ResultSetIterator;
import org.talend.dq.dbms.DbmsLanguage;
import org.talend.dq.dbms.DbmsLanguageFactory;
import org.talend.utils.sql.ResultSetUtils;
import org.talend.utils.sugars.ReturnCode;
import org.talend.utils.sugars.TypedReturnCode;
import orgomg.cwm.foundation.softwaredeployment.DataManager;
import orgomg.cwm.objectmodel.core.ModelElement;

/**
 * SQL executor dedicated for relational database query.
 */
public class DatabaseSQLExecutor extends SQLExecutor {

    private static Logger log = Logger.getLogger(DatabaseSQLExecutor.class);

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
     * DOC yyin Comment method "getSQLConnection".
     * 
     * @param connection
     * @return
     * @throws SQLException
     */
    private TypedReturnCode<java.sql.Connection> getSQLConnection(DataManager connection) throws SQLException {
        TypedReturnCode<java.sql.Connection> sqlconnection = JavaSqlFactory.createConnection((Connection) connection);
        if (!sqlconnection.isOk()) {
            MessageDialogWithToggle.openWarning(Display.getCurrent().getActiveShell(),
                    Messages.getString("DatabaseSQLExecutor.createConnectionError"), //$NON-NLS-1$
                    sqlconnection.getMessage());
            throw new SQLException(sqlconnection.getMessage());
        }
        return sqlconnection;
    }

    /**
     * 
     * createSqlStatement: if has limit, add it, else do not use limit
     * 
     * @param connection
     * @param analysedElements
     * @return
     */
    private String createSqlStatement(DataManager connection, List<ModelElement> analysedElements) {
        return createSqlStatement(connection, analysedElements, null);

    }

    /**
     * 
     * createSqlStatement: if has limit, add it, else do not use limit
     * 
     * @param connection
     * @param analysedElements
     * @return
     */
    private String createSqlStatement(DataManager connection, List<ModelElement> analysedElements, String where) {
        DbmsLanguage dbms = createDbmsLanguage(connection);
        TdColumn col = null;
        StringBuilder sql = new StringBuilder("SELECT ");//$NON-NLS-1$
        final Iterator<ModelElement> iterator = analysedElements.iterator();
        while (iterator.hasNext()) {
            ModelElement modelElement = iterator.next();
            col = SwitchHelpers.COLUMN_SWITCH.doSwitch(modelElement);
            sql.append(dbms.quote(col.getName()));
            // append comma if more columns exist
            if (iterator.hasNext()) {
                sql.append(',');
            }
        }
        sql.append(dbms.from());
        sql.append(dbms.getQueryColumnSetWithPrefix(col));
        if (where != null && where.length() > 0) {
            sql.append(dbms.where());
            sql.append(where);
        }
        if (limit > 0) {
            return dbms.getTopNQuery(sql.toString(), limit);
        } else {
            return sql.toString();
        }

    }

    private DbmsLanguage createDbmsLanguage(DataManager connection) {
        // DataManager connection = analysis.getContext().getConnection();
        return DbmsLanguageFactory.createDbmsLanguage(connection);
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
    public Iterator<Record> getResultSetIterator(DataManager connection, List<ModelElement> analysedElements) throws SQLException {
        String sqlString = createSqlStatement(connection, analysedElements);
        TypedReturnCode<java.sql.Connection> sqlconnection = getSQLConnection(connection);
        List<String> elementsName = new ArrayList<String>();
        for (ModelElement element : analysedElements) {
            elementsName.add(element.getName());
        }

        return new ResultSetIterator(sqlconnection.getObject(), sqlString, elementsName);
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
        int columnListSize = analysedElements.size();

        TypedReturnCode<java.sql.Connection> sqlconnection = getSQLConnection(connection);
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = sqlconnection.getObject().createStatement();
            statement.execute(createSqlStatement(connection, analysedElements, where));
            resultSet = statement.getResultSet();

            while (resultSet.next()) {
                Object[] oneRow = new Object[columnListSize];
                // --- for each column
                for (int i = 0; i < columnListSize; i++) {
                    // --- get content of column
                    oneRow[i] = ResultSetUtils.getObject(resultSet, i + 1);
                }
                handleRow(oneRow);
            }
        } catch (Exception e) {
            log.error(e, e);
            if (SQLException.class.isInstance(e)) {
                throw (SQLException) e;
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
            ReturnCode closed = ConnectionUtils.closeConnection(sqlconnection.getObject());
            if (!closed.isOk()) {
                log.error(closed.getMessage());
            }
        }

        try {
            endQuery();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return dataFromTable;
    }
}
