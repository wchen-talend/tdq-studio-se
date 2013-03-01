// ============================================================================
//
// Copyright (C) 2006-2013 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprofiler.core.ui.action.actions;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.database.conn.version.EDatabaseVersion4Drivers;
import org.talend.core.exception.TalendInternalPersistenceException;
import org.talend.core.model.metadata.IMetadataConnection;
import org.talend.core.model.metadata.MetadataFillFactory;
import org.talend.core.model.metadata.MetadataTalendType;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.database.JavaSqlFactory;
import org.talend.core.model.metadata.builder.util.MetadataConnectionUtils;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.cwm.db.connection.ConnectionUtils;
import org.talend.cwm.helper.ColumnSetHelper;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.dataprofiler.core.ImageLib;
import org.talend.dataprofiler.core.i18n.internal.DefaultMessagesImpl;
import org.talend.dataquality.helpers.MetadataHelper;
import org.talend.dq.helper.RepositoryNodeHelper;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.utils.sugars.ReturnCode;
import org.talend.utils.sugars.TypedReturnCode;
import orgomg.cwm.objectmodel.core.Package;
import orgomg.cwm.resource.relational.Schema;

/**
 * DOC bZhou class global comment. Detailled comment
 */
public class ExportConnectionToTOSAction extends Action {

    private static Logger log = Logger.getLogger(ExportConnectionToTOSAction.class);

    private List<Package> packList = new ArrayList<Package>();

    public ExportConnectionToTOSAction(List<Package> packList) {
        super(DefaultMessagesImpl.getString("ExportConnectionToTOSAction.title"));//$NON-NLS-1$
        setImageDescriptor(ImageLib.getImageDescriptor(ImageLib.EXPORT));

        this.packList = packList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        if (packList.isEmpty()) {
            return;
        }

        for (Package pack : packList) {
            IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

            Connection tdDataProvider = ConnectionHelper.getTdDataProvider(pack);

            Property connectionProperty = initConnectionProperty(tdDataProvider, pack);
            connectionProperty.setId(factory.getNextId());

            ConnectionItem connectionItem = initConnectionItem(tdDataProvider, pack);
            connectionItem.setProperty(connectionProperty);

            try {
                factory.create(connectionItem, new Path(""));//$NON-NLS-1$
                MessageDialog
                        .openInformation(
                                null,
                                DefaultMessagesImpl.getString("ExportConnectionToTOSAction.info"), DefaultMessagesImpl.getString("ExportConnectionToTOSAction.meta"));//$NON-NLS-1$ //$NON-NLS-2$
            } catch (TalendInternalPersistenceException e1) {
                //                MessageDialog.openError(null, DefaultMessagesImpl.getString("ExportConnectionToTOSAction.error"), e1.getMessage());//$NON-NLS-1$
            } catch (PersistenceException e) {
                MessageDialog.openError(null, DefaultMessagesImpl.getString("ExportConnectionToTOSAction.error"), e.getMessage());//$NON-NLS-1$
                log.error(e.getMessage(), e);
            }
        }
        // refresh TDQ's matadata tree list
        RepositoryNodeHelper.getDQCommonViewer().refresh(RepositoryNodeHelper.getRootNode(ERepositoryObjectType.METADATA, true));
        // refresh TOS's matadata tree list
        RepositoryManager.refreshCreatedNode(ERepositoryObjectType.METADATA_CONNECTIONS);
    }

    /**
     * DOC bZhou Comment method "initConnectionProperty".
     * 
     * @param tdDataProvider
     * @return
     */
    private Property initConnectionProperty(Connection tdDataProvider, Package pack) {
        Property connectionProperty = PropertiesFactory.eINSTANCE.createProperty();

        String purpose = MetadataHelper.getPurpose(tdDataProvider);
        String description = MetadataHelper.getDescription(tdDataProvider);
        String status = MetadataHelper.getDevStatus(tdDataProvider);

        connectionProperty.setLabel(tdDataProvider.getName() + "_" + pack.getName());//$NON-NLS-1$
        connectionProperty.setAuthor(((RepositoryContext) CoreRuntimePlugin.getInstance().getContext()
                .getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        connectionProperty.setPurpose(purpose);
        connectionProperty.setDescription(description);
        connectionProperty.setStatusCode(status);
        connectionProperty.setVersion(VersionUtils.DEFAULT_VERSION);

        return connectionProperty;
    }

    /**
     * DOC bZhou Comment method "initConnectionItem".
     * 
     * @param tdDataProvider
     * @return
     */
    private ConnectionItem initConnectionItem(Connection tdDataProvider, Package pack) {
        ConnectionItem connectionItem = PropertiesFactory.eINSTANCE.createDatabaseConnectionItem();

        if (tdDataProvider != null) {
            DatabaseConnection connection = ConnectionFactory.eINSTANCE.createDatabaseConnection();

            String host = ConnectionUtils.getServerName(tdDataProvider);
            String port = ConnectionUtils.getPort(tdDataProvider);
            String user = JavaSqlFactory.getUsername(tdDataProvider);
            String pass = JavaSqlFactory.getPassword(tdDataProvider);
            String connName = tdDataProvider.getName();
            String url = JavaSqlFactory.getURL(tdDataProvider);
            String driver = JavaSqlFactory.getDriverClass(tdDataProvider);

            String database = pack.getName();

            if (pack instanceof Schema) {
                Package parent = ColumnSetHelper.getParentCatalogOrSchema(pack);
                if (parent != null) {
                    database = parent.getName();
                }

                database = ConnectionUtils.getSID(tdDataProvider);
            }
            connection.setUiSchema(pack.getName());
            connection.setServerName(host);
            connection.setPort(port);
            connection.setUsername(user);
            ConnectionHelper.setPassword(connection, pass);
            connection.setSID(database);
            connection.setURL(url);
            connection.setDriverClass(driver);

            Boolean isContextMod = tdDataProvider.isContextMode();
            IMetadataConnection metadataConnection = null;
            if (isContextMod) {
                metadataConnection = ConvertionHelper.convert(tdDataProvider, false, tdDataProvider.getContextName());
            } else {
                metadataConnection = ConvertionHelper.convert(tdDataProvider);
            }
            if (metadataConnection != null) {
                String dbType = metadataConnection.getDbType();
                String product = metadataConnection.getProduct();
                String mapping = MetadataTalendType.getDefaultDbmsFromProduct(product).getId();
                String dbVersion = retrieveDBVersion(dbType);
                connection.setName(connName + "_" + pack.getName());//$NON-NLS-1$
                connection.setDatabaseType(dbType);
                connection.setProductId(product);
                connection.setDbmsId(mapping);
                connection.setDbVersionString(dbVersion);
            }
            // MOD gdbu TDQ-4282 2011-12-28 fill catalog and schema.
            fillCatalogSchema(connection);
            // ~TDQ-4282
            connectionItem.setConnection(connection);
        }

        return connectionItem;
    }

    /**
     * 
     * DOC gdbu Comment method "fillCatalogSchema".
     * 
     * @param tdDataProvider
     */
    private void fillCatalogSchema(Connection tdDataProvider) {
        MetadataFillFactory instance = MetadataFillFactory.getDBInstance();

        IMetadataConnection metaConnection = ConvertionHelper.convert(tdDataProvider);
        ReturnCode rc = instance.checkConnection(metaConnection);
        Connection dbConn = null;
        if (rc.isOk()) {
            dbConn = instance.fillUIConnParams(metaConnection, tdDataProvider);
            DatabaseMetaData dbMetadata = null;
            java.sql.Connection sqlConn = null;
            try {
                if (rc instanceof TypedReturnCode) {
                    Object sqlConnObject = ((TypedReturnCode) rc).getObject();
                    if (sqlConnObject instanceof java.sql.Connection) {
                        sqlConn = (java.sql.Connection) sqlConnObject;
                        dbMetadata = org.talend.utils.sql.ConnectionUtils.getConnectionMetadata(sqlConn);
                    }
                }
                List<String> packageFilterCatalog = MetadataConnectionUtils.getPackageFilter(dbConn, dbMetadata, true);
                instance.fillCatalogs(dbConn, dbMetadata, packageFilterCatalog);
                List<String> packageFilterSchema = MetadataConnectionUtils.getPackageFilter(dbConn, dbMetadata, false);
                instance.fillSchemas(dbConn, dbMetadata, packageFilterSchema);

            } catch (SQLException e) {
                log.error(e, e);
            } finally {
                if (sqlConn != null) {
                    ConnectionUtils.closeConnection(sqlConn);
                }
            }
        } else {
            log.error(rc.getMessage());
        }
    }

    /**
     * DOC bZhou Comment method "retrieveDBVersion".
     * 
     * @param product
     * @return
     */
    private String retrieveDBVersion(String product) {
        List<EDatabaseVersion4Drivers> eVersions = EDatabaseVersion4Drivers.indexOfByDbType(product);
        if (eVersions != null && !eVersions.isEmpty()) {
            return eVersions.get(0).getVersionValue();
        }

        return null;
    }

}