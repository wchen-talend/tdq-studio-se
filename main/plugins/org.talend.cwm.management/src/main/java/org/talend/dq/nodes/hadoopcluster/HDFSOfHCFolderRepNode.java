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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.data.container.RootContainer;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.cwm.management.i18n.Messages;
import org.talend.dq.helper.RepositoryNodeHelper;
import org.talend.dq.nodes.DBConnectionFolderRepNode;
import org.talend.repository.hdfs.node.model.HDFSRepositoryNodeType;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.hdfs.HDFSConnection;
import org.talend.repository.model.hdfs.HDFSConnectionItem;

/**
 * created by yyin on 2015年4月24日 Detailled comment
 *
 */
public class HDFSOfHCFolderRepNode extends DBConnectionFolderRepNode {

    private static Logger log = Logger.getLogger(HDFSOfHCFolderRepNode.class);

    public HDFSOfHCFolderRepNode(IRepositoryViewObject object, RepositoryNode parent, ENodeType type) {
        super(object, parent, type);

    }

    @Override
    public String getDisplayText() {
        return Messages.getString("HDFSOfHCFolderRepNode.displayText"); //$NON-NLS-1$
    }

    /**
     * this folder path is: hadoop/hdfs, its parent's path is: hadoop/hadoopcluster
     */
    @Override
    public List<IRepositoryNode> getChildren(boolean withDeleted) {
        List<IRepositoryNode> children = new ArrayList<IRepositoryNode>();
        IPath path = getPathOfHDFSFolder();
        Item clusterConnectionItem = getParent().getObject().getProperty().getItem();
        String clusterId = clusterConnectionItem.getProperty().getId();
        try {
            RootContainer<String, IRepositoryViewObject> tdqViewObjects = ProxyRepositoryFactory.getInstance()
                    .getTdqRepositoryViewObjects(HDFSRepositoryNodeType.HDFS, path.toString());
            for (IRepositoryViewObject viewObject : tdqViewObjects.getMembers()) {
                if (!withDeleted && viewObject.isDeleted()) {
                    continue;
                }
                HDFSOfHCConnectionNode repNode = null;
                // check if this hdfs's relativeHadoopClusterId = this hadoop cluster's id
                if (viewObject != null && viewObject.getProperty() != null) {
                    HDFSConnectionItem dbItem = (HDFSConnectionItem) viewObject.getProperty().getItem();
                    HDFSConnection dbConnection = (HDFSConnection) dbItem.getConnection();
                    String hcId = dbConnection.getRelativeHadoopClusterId();
                    if (!StringUtils.equals(clusterId, hcId)) {
                        continue;
                    }
                }
                try {
                    repNode = new HDFSOfHCConnectionNode(viewObject, this, ENodeType.REPOSITORY_ELEMENT);
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

    private IPath getPathOfHDFSFolder() {
        IPath path = RepositoryNodeHelper.getPath(this.getParent());// =metadata/hadoop/hadoopcluster
        // change it to: metadata/hadoop/hdfs
        path = path.removeLastSegments(1);
        path = path.append("hdfs"); //$NON-NLS-1$
        return path;
    }

    @Override
    public boolean isSupportCreateDBMenu() {
        return false;
    }
}
