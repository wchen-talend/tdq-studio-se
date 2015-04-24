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
package org.talend.dq.nodes.hadoopcluster;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.data.container.RootContainer;
import org.talend.core.database.conn.ConnParameterKeys;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.cwm.management.i18n.Messages;
import org.talend.dq.helper.RepositoryNodeHelper;
import org.talend.dq.nodes.DBConnectionFolderRepNode;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.resource.EResourceConstant;

/**
 * created by yyin on 2015年4月24日 Detailled comment
 *
 */
public class HiveOfHCFolderRepNode extends DBConnectionFolderRepNode {

    private static Logger log = Logger.getLogger(HiveOfHCFolderRepNode.class);

    /**
     * DOC yyin HiveOfHCFolderRepNode constructor comment.
     * 
     * @param object
     * @param parent
     * @param type
     */
    public HiveOfHCFolderRepNode(IRepositoryViewObject object, RepositoryNode parent, ENodeType type) {
        super(object, parent, type);
        // TODO Auto-generated constructor stub
    }

    /**
     * for hive folder, its children are under connections folder, not here, so find children there, and show link here.
     */
    @Override
    public List<IRepositoryNode> getChildren(boolean withDeleted) {
        List<IRepositoryNode> children = new ArrayList<IRepositoryNode>();
        IPath path = RepositoryNodeHelper.getPath(this.getParent());// =metadata/hadoop/hadoopcluster
        // change it to: metadata/connections
        path = path.removeLastSegments(2);
        path = path.append(EResourceConstant.DB_CONNECTIONS.getName());

        Item clusterConnectionItem = getParent().getObject().getProperty().getItem();
        String clusterId = clusterConnectionItem.getProperty().getId();

        try {
            RootContainer<String, IRepositoryViewObject> tdqViewObjects = ProxyRepositoryFactory.getInstance()
                    .getTdqRepositoryViewObjects(ERepositoryObjectType.METADATA_CONNECTIONS, path.toString());

            for (IRepositoryViewObject viewObject : tdqViewObjects.getMembers()) {
                if (!withDeleted && viewObject.isDeleted()) {
                    continue;
                }
                HiveOfHCConnectionNode repNode = null;
                // check if ConnParameterKeys.CONN_PARA_KEY_HADOOP_CLUSTER_ID = current hadoop cluster's id
                if (viewObject != null && viewObject.getProperty() != null) {
                    DatabaseConnectionItem dbItem = (DatabaseConnectionItem) viewObject.getProperty().getItem();
                    DatabaseConnection dbConnection = (DatabaseConnection) dbItem.getConnection();
                    String hcId = dbConnection.getParameters().get(ConnParameterKeys.CONN_PARA_KEY_HADOOP_CLUSTER_ID);
                    if (!clusterId.equals(hcId)) {
                        continue;
                    }
                }

                try {
                    repNode = new HiveOfHCConnectionNode(viewObject, this, ENodeType.REPOSITORY_ELEMENT);
                } catch (Exception e) {
                    log.error(e, e);
                    continue;
                }
                repNode.setProperties(EProperties.LABEL, viewObject.getLabel());
                repNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_CONNECTIONS);
                viewObject.setRepositoryNode(repNode);
                children.add(repNode);
            }

        } catch (PersistenceException e) {
            log.error(e, e);
        }
        return children;
    }

    @Override
    public String getDisplayText() {
        return Messages.getString("HiveOfHCFolderRepNode.displayText"); //$NON-NLS-1$
    }

}
