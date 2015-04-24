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

import java.util.List;

import org.apache.log4j.Logger;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.data.container.Container;
import org.talend.commons.utils.data.container.RootContainer;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.Folder;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.cwm.management.i18n.Messages;
import org.talend.dq.helper.RepositoryNodeHelper;
import org.talend.dq.nodes.DQRepositoryNode;
import org.talend.repository.hadoopcluster.node.model.HadoopClusterRepositoryNodeType;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;

/**
 * created by yyin on 2015年4月21日 Detailled comment
 *
 */
public class HadoopClusterFolderRepNode extends DQRepositoryNode {

    private static Logger log = Logger.getLogger(HadoopClusterFolderRepNode.class);

    /**
     * DOC yyin HadoopClusterFolderRepNode constructor comment.
     * 
     * @param object
     * @param parent
     * @param type
     */
    public HadoopClusterFolderRepNode(IRepositoryViewObject object, RepositoryNode parent, ENodeType type) {
        super(object, parent, type);
    }

    @Override
    public List<IRepositoryNode> getChildren() {
        return getChildren(false);
    }

    @Override
    public List<IRepositoryNode> getChildren(boolean withDeleted) {
        try {
            super.getChildren().clear();
            RootContainer<String, IRepositoryViewObject> tdqViewObjects = ProxyRepositoryFactory.getInstance()
                    .getTdqRepositoryViewObjects(HadoopClusterRepositoryNodeType.HADOOPCLUSTER,
                            RepositoryNodeHelper.getPath(this).toString());
            // sub folders
            for (Container<String, IRepositoryViewObject> container : tdqViewObjects.getSubContainer()) {
                Folder folder = new Folder((Property) container.getProperty(), HadoopClusterRepositoryNodeType.HADOOPCLUSTER);
                if (!withDeleted && folder.isDeleted()) {
                    continue;
                }
                HadoopClusterSubFolderRepNode childNodeFolder = new HadoopClusterSubFolderRepNode(folder, this,
                        ENodeType.SIMPLE_FOLDER);
                childNodeFolder.setProperties(EProperties.LABEL, HadoopClusterRepositoryNodeType.HADOOPCLUSTER);
                childNodeFolder.setProperties(EProperties.CONTENT_TYPE, HadoopClusterRepositoryNodeType.HADOOPCLUSTER);
                super.getChildren().add(childNodeFolder);
            }
            // connection files
            for (IRepositoryViewObject viewObject : tdqViewObjects.getMembers()) {
                if (!withDeleted && viewObject.isDeleted()) {
                    continue;
                }

                HadoopClusterConnectionRepNode repNode = new HadoopClusterConnectionRepNode(viewObject, this,
                        ENodeType.REPOSITORY_ELEMENT);
                repNode.setProperties(EProperties.LABEL, HadoopClusterRepositoryNodeType.HADOOPCLUSTER);
                repNode.setProperties(EProperties.CONTENT_TYPE, HadoopClusterRepositoryNodeType.HADOOPCLUSTER);
                viewObject.setRepositoryNode(repNode);
                super.getChildren().add(repNode);
            }
        } catch (PersistenceException e) {
            log.error(e, e);
        }
        // MOD gdbu 2011-7-1 bug : 22204
        return filterResultsIfAny(super.getChildren());
    }

    @Override
    public String getLabel() {
        if (getObject() == null) {
            return this.getProperties(EProperties.LABEL).toString();
        }
        return this.getObject().getLabel();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.RepositoryNode#getDisplayText()
     */
    @Override
    public String getDisplayText() {
        return Messages.getString("DQRepositoryViewLabelProvider.HadoopClusterFolderName"); //$NON-NLS-1$
    }

}
