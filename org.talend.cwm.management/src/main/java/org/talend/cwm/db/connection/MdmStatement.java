// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import org.talend.cwm.xml.TdXMLDocument;
import orgomg.cwm.objectmodel.core.ModelElement;

/**
 * DOC xqliu class global comment. Detailled comment
 */
public class MdmStatement {

    private MdmConnection connection;

    private String[] resultSet;

    MdmStatement(MdmConnection connection) {
        this.connection = connection;
    }

    /**
     * DOC xqliu Comment method "execute". ADD xqliu 2010-04-15 bug 12568
     * 
     * @param xmlDocument
     * @param xmlSql
     * @return
     * @throws RemoteException
     * @throws ServiceException
     */
    public boolean execute(TdXMLDocument xmlDocument, String xmlSql) throws RemoteException, ServiceException {
        resultSet = connection.runQuery(xmlDocument, xmlSql);
        return true;
    }

    public boolean execute(String xmlSql) throws RemoteException, ServiceException {
        // MOD xqliu 2010-04-15 bug 12568
        return execute(null, xmlSql);
    }

    public String[] getResultSet() {
        return resultSet;
    }

    /**
     * 
     * DOC zshen Comment method "tidyResultSet".
     * 
     * @param columnTitle The array of column title
     * @param resultSet the value array which according to columnTitle
     * @return result list which one element representative one row on the database
     */
    public List<Map<String, String>> tidyResultSet(ModelElement[] columnTitle, String[] resultSet) {
        List<Map<String, String>> resultSetList = new ArrayList<Map<String, String>>();
        if (columnTitle == null || columnTitle.length == 0 || resultSet == null || resultSet.length == 0) {
            return resultSetList;
        }
        int arraySize = columnTitle.length;
        for (int i = 0; i < resultSet.length; i += arraySize) {
            // String[] strArray = new String[arraySize];
            Map<String, String> rowMap = new HashMap<String, String>();
            for (int j = 0; j < arraySize; j++) {
                rowMap.put(columnTitle[j].getName(), resultSet[i + j]);
                // strArray[j] = resultSet[i + j];
            }
            resultSetList.add(rowMap);
        }

        return resultSetList;
    }
}
