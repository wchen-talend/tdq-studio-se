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

import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.dq.nodes.ConnectionRepNode;
import org.talend.dq.nodes.DBConnectionFolderRepNode;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;

/**
 * created by yyin on 2015年4月22日 Detailled comment
 *
 */
public class HadoopClusterConnectionRepNode extends ConnectionRepNode {

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.RepositoryNode#hasChildren()
     */
    @Override
    public boolean hasChildren() {
        return super.hasChildren();
    }

    /**
     * DOC yyin HadoopClusterConnectionRepNode constructor comment.
     * 
     * @param object
     * @param parent
     * @param type
     */
    public HadoopClusterConnectionRepNode(IRepositoryViewObject object, RepositoryNode parent, ENodeType type) {
        super(object, parent, type);

    }

    /**
     * 1)The current node is under: hadoop/hadoopcluster, need to get the folder to shown as child: hadoop/hdfs,
     * hadoop/hive, 2) only show hdfs or hive folder, when it has children.
     */
    @Override
    public List<IRepositoryNode> getChildren() {
        List<IRepositoryNode> children = new ArrayList<IRepositoryNode>();
        // first : create two sub folders:
        HDFSOfHCFolderRepNode hdfsFolder = new HDFSOfHCFolderRepNode(this.getObject(), this, ENodeType.SIMPLE_FOLDER);
        HiveOfHCFolderRepNode hiveFolder = new HiveOfHCFolderRepNode(this.getObject(), this, ENodeType.SIMPLE_FOLDER);
        addSubFolder(children, hdfsFolder);
        addSubFolder(children, hiveFolder);

        return children;
    }

    // only show it when the related folder has child
    private void addSubFolder(List<IRepositoryNode> children, DBConnectionFolderRepNode childNodeFolder) {
        List<IRepositoryNode> childOfFolder = childNodeFolder.getChildren();
        if (childOfFolder != null && childOfFolder.size() > 0) {

            childNodeFolder.setProperties(EProperties.LABEL, ERepositoryObjectType.METADATA_CONNECTIONS);
            childNodeFolder.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_CONNECTIONS);
            // folder.setRepositoryNode(childNodeFolder);
            children.add(childNodeFolder);
        }
    }
}
