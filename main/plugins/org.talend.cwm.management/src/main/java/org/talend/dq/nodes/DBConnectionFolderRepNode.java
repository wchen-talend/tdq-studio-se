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
package org.talend.dq.nodes;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.data.container.Container;
import org.talend.commons.utils.data.container.RootContainer;
import org.talend.core.ITDQRepositoryService;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.Folder;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.cwm.management.i18n.Messages;
import org.talend.dq.helper.AnalysisExecutorHelper;
import org.talend.dq.helper.RepositoryNodeHelper;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC klliu class global comment. Detailled comment
 */
public class DBConnectionFolderRepNode extends DQRepositoryNode {

    private static Logger log = Logger.getLogger(DBConnectionFolderRepNode.class);

    private ITDQRepositoryService tdqRepService = null;

    private List<IRepositoryNode> nodesCache = new ArrayList<IRepositoryNode>();

    private Boolean useNodeCache = Boolean.FALSE;

    /**
     * DOC klliu DBConnectionFolderRepNode constructor comment.
     * 
     * @param object
     * @param parent
     * @param type
     */
    public DBConnectionFolderRepNode(IRepositoryViewObject object, RepositoryNode parent, ENodeType type) {
        super(object, parent, type);
        initDQRepositoryService();
    }

    @Override
    public List<IRepositoryNode> getChildren() {
        // MOD qiongli 2011-2-22,bug 17588.override 'getChildren(boolean withDeleted)'. in this case,withDeleted is
        // false.
        return getChildren(false);
    }

    @Override
    public List<IRepositoryNode> getChildren(boolean withDeleted) {
        if (useNodeCache) {
            return nodesCache;
        }
        nodesCache.clear();
        List<IRepositoryNode> children = new ArrayList<IRepositoryNode>();
        try {
            RootContainer<String, IRepositoryViewObject> tdqViewObjects = ProxyRepositoryFactory.getInstance()
                    .getTdqRepositoryViewObjects(getContentType(), RepositoryNodeHelper.getPath(this).toString());
            // sub folders
            // MOD qiongli 2011-1-18.setProperties for every node
            for (Container<String, IRepositoryViewObject> container : tdqViewObjects.getSubContainer()) {
                Folder folder = new Folder((Property) container.getProperty(), ERepositoryObjectType.METADATA_CONNECTIONS);
                if (!withDeleted && folder.isDeleted()) {
                    continue;
                }
                DBConnectionSubFolderRepNode childNodeFolder = new DBConnectionSubFolderRepNode(folder, this,
                        ENodeType.SIMPLE_FOLDER);
                childNodeFolder.setProperties(EProperties.LABEL, ERepositoryObjectType.METADATA_CONNECTIONS);
                childNodeFolder.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_CONNECTIONS);
                folder.setRepositoryNode(childNodeFolder);
                children.add(childNodeFolder);
            }
            // connection files
            for (IRepositoryViewObject viewObject : tdqViewObjects.getMembers()) {
                if (!withDeleted && viewObject.isDeleted()) {
                    continue;
                }
                DBConnectionRepNode repNode = null;
                try {
                    repNode = new DBConnectionRepNode(viewObject, this, ENodeType.REPOSITORY_ELEMENT);
                } catch (Exception e) {
                    log.error(e, e);
                    continue;
                    // MOD zshen there maybe impact file Connection to decide whether had same name connection has been
                    // created
                    // before that.
                }
                repNode.setProperties(EProperties.LABEL, viewObject.getLabel());
                repNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_CONNECTIONS);
                viewObject.setRepositoryNode(repNode);
                children.add(repNode);

                // Update software system
                updateSoftwareSystem(viewObject);
            }
        } catch (PersistenceException e) {
            log.error(e, e);
        }
        List<IRepositoryNode> filteredResults = filterResultsIfAny(children);
        nodesCache.addAll(filteredResults);
        return filteredResults;
    }

    /**
     * DOC zhao Comment method "updateSoftwareSystem".
     * 
     * @param viewObject
     */
    private void updateSoftwareSystem(IRepositoryViewObject viewObject) {
        DatabaseConnection connection = (DatabaseConnection) ((ConnectionItem) viewObject.getProperty().getItem())
                .getConnection();
        if (tdqRepService != null) {
            tdqRepService.publishSoftwareSystemUpdateEvent(connection);
        }

    }

    private void initDQRepositoryService() {
        tdqRepService = AnalysisExecutorHelper.getTDQService();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.RepositoryNode#getLabel()
     */
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
        return Messages.getString("DQRepositoryViewLabelProvider.DBConnectionFolderName"); //$NON-NLS-1$
    }

    public void setUseNodeCache(Boolean isUseCache) {
        this.useNodeCache = isUseCache;
    }

    public boolean isSupportCreateDBMenu() {
        return true;
    }
}
