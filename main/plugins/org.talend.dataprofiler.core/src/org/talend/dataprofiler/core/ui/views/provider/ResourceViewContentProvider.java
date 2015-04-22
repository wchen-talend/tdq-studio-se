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
package org.talend.dataprofiler.core.ui.views.provider;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.platform.PluginChecker;
import org.talend.core.model.properties.FolderItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.Folder;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.model.repositoryObject.MetadataColumnRepositoryObject;
import org.talend.dataprofiler.core.i18n.internal.DefaultMessagesImpl;
import org.talend.dataprofiler.core.manager.DQStructureManager;
import org.talend.dataprofiler.core.ui.exchange.ExchangeCategoryRepNode;
import org.talend.dataprofiler.core.ui.exchange.ExchangeComponentRepNode;
import org.talend.dataprofiler.core.ui.exchange.ExchangeFolderRepNode;
import org.talend.dataprofiler.core.ui.utils.ComparatorsFactory;
import org.talend.dataprofiler.ecos.model.IEcosCategory;
import org.talend.dq.helper.RepositoryNodeHelper;
import org.talend.dq.nodes.DBCatalogRepNode;
import org.talend.dq.nodes.DBSchemaRepNode;
import org.talend.dq.nodes.DBTableFolderRepNode;
import org.talend.dq.nodes.DBTableRepNode;
import org.talend.dq.nodes.DBViewFolderRepNode;
import org.talend.dq.nodes.DBViewRepNode;
import org.talend.dq.nodes.DQRepositoryNode;
import org.talend.dq.nodes.RecycleBinRepNode;
import org.talend.dq.nodes.SysIndicatorFolderRepNode;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode;
import org.talend.resource.EResourceConstant;
import org.talend.resource.ResourceManager;
import org.talend.utils.exceptions.MissingDriverException;

/**
 * DOC klliu Reconstructure the ResourceViewContentProvider for using DI's API.
 */
public class ResourceViewContentProvider extends WorkbenchContentProvider {

    private static Logger log = Logger.getLogger(ResourceViewContentProvider.class);

    private TreeViewer treeViewer = null;

    /**
     * DOC rli ResourceViewContentProvider constructor comment.
     */
    public ResourceViewContentProvider() {
        super();
    }

    @Override
    public Object[] getChildren(Object element) {
        if (DQRepositoryNode.isOnDisplayNextOrPreviousNode() && element instanceof IRepositoryNode) {
            List<IRepositoryNode> children = findChildren(element);
            return sortRepositoryNode(children.toArray());
        }

        try {
            if (element instanceof IWorkspaceRoot) {
                return getWorkspaceRootChildren(element);
            } else if (element instanceof RepositoryNode) {
                return getRepositoryNodeChildren(element);
            }
        } catch (CoreException e) {
            log.error(e);
        } catch (PersistenceException e) {
            log.error(e);
        }
        return sortRepositoryNode(super.getChildren(element));
    }

    /**
     * DOC talend Comment method "getRepositoryNodeChildren".
     * 
     * @param element
     * @param instance
     * @return
     * @throws PersistenceException
     */
    private Object[] getRepositoryNodeChildren(Object element) throws PersistenceException {
        RepositoryNodeBuilder instance = RepositoryNodeBuilder.getInstance();
        final RepositoryNode node = (RepositoryNode) element;
        // MOD gdbu 2011-7-20 bug : 23220
        DQRepositoryNode.setIsReturnAllNodesWhenFiltering(false);
        List<IRepositoryNode> children = node.getChildren();
        DQRepositoryNode.setIsReturnAllNodesWhenFiltering(true);

        if ((node instanceof DBTableFolderRepNode || node instanceof DBViewFolderRepNode) && !DQRepositoryNode.isOnFilterring()) {
            if (0 < children.size()) {
                try {

                    StructuredSelection structSel = new StructuredSelection(node);

                    if (null != treeViewer) {
                        ISelection tempSelection = treeViewer.getSelection();
                        treeViewer.setSelection(structSel);
                        TreeItem[] selectionItems = treeViewer.getTree().getSelection();
                        if (0 != selectionItems.length) {
                            selectionItems[0].setText(node.getLabel());
                            treeViewer.setSelection(tempSelection);
                        }
                    } else {
                        ISelection dqTreeSelection = RepositoryNodeHelper.getDQCommonViewer().getSelection();

                        RepositoryNodeHelper.getDQCommonViewer().setSelection(structSel);
                        TreeItem[] selections = RepositoryNodeHelper.getDQCommonViewer().getTree().getSelection();
                        if (0 != selections.length) {
                            selections[0].setText(node.getLabel());
                        }
                        RepositoryNodeHelper.getDQCommonViewer().setSelection(dqTreeSelection);
                    }

                } catch (Exception e) {
                    log.error(e.toString());
                }
            }
        }

        if (children.size() <= 0) {
            // ~23220
            IRepositoryViewObject viewObject = node.getObject();
            String label = viewObject == null ? null : viewObject.getLabel();
            if (EResourceConstant.DATA_PROFILING.getName().equals(label)) {
                List<EResourceConstant> resContants = new ArrayList<EResourceConstant>();
                resContants.add(EResourceConstant.ANALYSIS);
                if (PluginChecker.isTDQLoaded()) {
                    resContants.add(EResourceConstant.REPORTS);
                }
                instance.createRepositoryNodeSystemFolders(node, resContants);
            } else if (EResourceConstant.LIBRARIES.getName().equals(label)) {
                List<EResourceConstant> resContants = new ArrayList<EResourceConstant>();
                resContants.add(EResourceConstant.EXCHANGE);
                resContants.add(EResourceConstant.INDICATORS);
                if (PluginChecker.isTDQLoaded()) {
                    resContants.add(EResourceConstant.JRXML_TEMPLATE);
                }
                resContants.add(EResourceConstant.PATTERNS);
                resContants.add(EResourceConstant.RULES);
                resContants.add(EResourceConstant.SOURCE_FILES);
                instance.createRepositoryNodeSystemFolders(node, resContants);
            } else if (EResourceConstant.METADATA.getName().equals(label)) {
                List<EResourceConstant> resContants = new ArrayList<EResourceConstant>();
                resContants.add(EResourceConstant.DB_CONNECTIONS);
                resContants.add(EResourceConstant.FILEDELIMITED);
                resContants.add(EResourceConstant.HADOOP_CLUSTER);
                instance.createRepositoryNodeSystemFolders(node, resContants);
            }
        }
        return sortRepositoryNode(children.toArray());
    }

    /**
     * DOC talend Comment method "getWorkspaceRootChildren".
     * 
     * @param element
     * @return
     * @throws CoreException
     */
    private Object[] getWorkspaceRootChildren(Object element) throws CoreException {
        Object currentOpenProject = null;
        RepositoryNodeBuilder instance = RepositoryNodeBuilder.getInstance();
        for (Object child : super.getChildren(element)) {
            if (child instanceof IProject) {
                if (((IProject) child).getName().equals(ResourceManager.getRootProjectName())) {
                    currentOpenProject = child;
                    break;
                }
            }
        }
        List<Object> folders = new ArrayList<Object>();

        Object[] rootFolders = new Object[0];
        if (currentOpenProject != null) {
            rootFolders = ((IProject) currentOpenProject).members(false);

            for (Object folder : rootFolders) {
                IRepositoryNode node = null;
                // here we use DQStructureManager.PREFIX_TDQ is because of we expect it is useful when we add some
                // TDQ_XXX node on the DQRepository view
                if (folder instanceof IFolder && ((IFolder) folder).getName().startsWith(DQStructureManager.PREFIX_TDQ)) {
                    IFolder iFolder = (IFolder) folder;
                    if (((IFolder) folder).getName().trim().equals("TDQ_reporting_db")) { //$NON-NLS-1$
                        continue;
                    }
                    IPath relativePath = iFolder.getFullPath().makeRelativeTo(((IProject) currentOpenProject).getFullPath());
                    ERepositoryObjectType respositoryObjectType = instance.retrieveRepObjectTypeByPath(relativePath.toOSString());
                    node = createNewRepNode(respositoryObjectType);
                }
                // MOD mzhao for metadata folder
                if (folder instanceof IFolder && ((IFolder) folder).getName().equals(EResourceConstant.METADATA.getName())) {
                    node = createNewRepNode(ERepositoryObjectType.METADATA);
                }
                if (node != null) {
                    folders.add(node);
                }
            }
        }

        // add RecycleBinRepNode
        RecycleBinRepNode recycleBin = new RecycleBinRepNode(DefaultMessagesImpl.getString("RecycleBin.resBinName")); //$NON-NLS-1$
        folders.add(recycleBin);

        return folders.toArray();
    }

    /**
     * DOC talend Comment method "findChildren".
     * 
     * @param element
     * @return
     */
    private List<IRepositoryNode> findChildren(Object element) {
        List<IRepositoryNode> children = new ArrayList<IRepositoryNode>();
        IRepositoryNode node = (IRepositoryNode) element;
        List<IRepositoryNode> allFilteredNodeList = RepositoryNodeHelper.getAllFilteredNodeList();
        for (IRepositoryNode iRepositoryNode : allFilteredNodeList) {
            if (null != iRepositoryNode.getParent() && iRepositoryNode.getParent().equals(node)) {
                children.add(iRepositoryNode);
            }
        }
        if (children.isEmpty()) {
            for (IRepositoryNode iRepositoryNode : allFilteredNodeList) {
                if (null != iRepositoryNode.getParent() && null != iRepositoryNode.getParent().getParent()
                        && iRepositoryNode.getParent().getParent().equals(node)) {
                    children.add(iRepositoryNode);
                }
            }
        }
        return children;
    }

    /**
     * create new Repositry node by specified type
     * 
     * @param folders
     * @param folder
     * @return new repository node
     */
    private IRepositoryNode createNewRepNode(ERepositoryObjectType type) {
        IRepositoryViewObject viewObject = null;

        FolderItem folderItem = ProxyRepositoryFactory.getInstance().getFolderItem(
                ProjectManager.getInstance().getCurrentProject(), type, Path.EMPTY);
        if (folderItem == null) {
            String folderName = ERepositoryObjectType.getFolderName(type);
            viewObject = new Folder(folderName, folderName);
        } else {
            viewObject = new Folder(folderItem.getProperty(), type);
        }

        IRepositoryNode node = new RepositoryNode(viewObject, null, ENodeType.SYSTEM_FOLDER);
        viewObject.setRepositoryNode(node);
        return node;
    }

    /**
     * sort element on the tree.
     * 
     * @param array
     * @return
     */
    protected Object[] sortRepositoryNode(Object[] array) {
        if (array != null && array.length > 0) {
            List<IRepositoryNode> repositoryNodeList = RepositoryNodeHelper.getRepositoryNodeList(array);
            return ComparatorsFactory.sort(repositoryNodeList.toArray(), ComparatorsFactory.REPOSITORY_NODE_COMPARATOR_ID);
        }
        return new Object[] {};
    }

    @Override
    public boolean hasChildren(Object element) {
        try {
            if (element instanceof IRepositoryNode) {
                IRepositoryNode node = (IRepositoryNode) element;
                IRepositoryViewObject viewObject = node.getObject();
                if (viewObject instanceof MetadataColumnRepositoryObject) {
                    return false;
                } else if (node instanceof ExchangeFolderRepNode) {
                    // ExchangeFolderRepNode always have children
                    return true;
                } else if (node instanceof ExchangeCategoryRepNode) {
                    // ExchangeCategoryRepNode always have children
                    return true;
                } else if (node instanceof ExchangeComponentRepNode) {
                    // ExchangeComponentRepNode always don't have children
                    return false;
                } else if (node instanceof SysIndicatorFolderRepNode) {
                    return true;
                }
            }
            if (element instanceof IEcosCategory) {
                return true;
            }
            // // MOD qiongli feature 9486
            // if (element instanceof IFolder) {
            // // MOD yyi 2010-09-30 15271: svn project can't load exchange nodes
            // if (ResourceManager.isExchangeFolder((IFolder) element)) {
            // return true;
            // }
            // // ~15271
            // List<Object> obsLs = Arrays.asList(super.getChildren(element));
            // if (obsLs.size() == 1) {
            // Object obj = (Object) obsLs.get(0);
            // if (obj instanceof IFolder && ((IFolder) obj).getName().equals(PluginConstant.SVN_SUFFIX))
            // return false;
            // }
            // } else if (element instanceof DQRecycleBinNode) {
            // DQRecycleBinNode rbn = (DQRecycleBinNode) element;
            // Object obj = rbn.getObject();
            // if (obj instanceof IFolder) {
            // try {
            // return ((IFolder) obj).members().length > 0;
            // } catch (CoreException e) {
            // log.error(e);
            // }
            // }
            // return false;
            // }

            // MOD gdbu 2011-9-1 TDQ-3457
            if (element instanceof DBTableFolderRepNode) {
                if (DQRepositoryNode.isOnFilterring()) {
                    return true;
                }
                DBTableFolderRepNode dbTableFolder = (DBTableFolderRepNode) element;
                return dbTableFolder.hasChildren();
            }

            if (element instanceof DBTableRepNode) {
                return true;
            }

            if (element instanceof DBViewRepNode) {
                return true;
            }

            if (element instanceof DBCatalogRepNode) {
                return true;
            }

            if (element instanceof DBSchemaRepNode) {
                return true;
            }

            if (element instanceof DBViewFolderRepNode) {
                if (DQRepositoryNode.isOnFilterring()) {
                    return true;
                }
                DBViewFolderRepNode dbViewFolder = (DBViewFolderRepNode) element;
                return dbViewFolder.hasChildren();
            }
            // ~TDQ-3457

        } catch (MissingDriverException e) {
            if (PluginChecker.isOnlyTopLoaded()) {
                MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                        DefaultMessagesImpl.getString("ResourceViewContentProvider.warining"), //$NON-NLS-1$
                        e.getErrorMessage());
            } else {
                log.error(e, e);
            }
            return false;
        }
        return super.hasChildren(element);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.model.BaseWorkbenchContentProvider#getParent(java.lang.Object)
     */
    @Override
    public Object getParent(Object element) {
        // MOD gdbu 2011-5-16 bug : 21188
        if (element instanceof RepositoryNode) {
            RepositoryNode node = (RepositoryNode) element;
            return node.getParent();
        }
        return super.getParent(element);
        // ~21188
    }

    public void setTreeViewer(TreeViewer fViewer) {
        this.treeViewer = fViewer;
    }

}
