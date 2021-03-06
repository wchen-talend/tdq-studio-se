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
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.database.DqRepositoryViewService;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.ISubRepositoryObject;
import org.talend.core.repository.model.repositoryObject.MetadataCatalogRepositoryObject;
import org.talend.core.repository.model.repositoryObject.MetadataSchemaRepositoryObject;
import org.talend.core.repository.model.repositoryObject.TdTableRepositoryObject;
import org.talend.cwm.db.connection.ConnectionUtils;
import org.talend.cwm.helper.PackageHelper;
import org.talend.cwm.relational.TdTable;
import org.talend.dq.helper.RepositoryNodeHelper;
import org.talend.dq.nodes.factory.DQRepNodeCreateFactory;
import org.talend.dq.nodes.foldernode.IConnectionElementSubFolder;
import org.talend.dq.writer.impl.ElementWriterFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.utils.exceptions.MissingDriverException;
import orgomg.cwm.objectmodel.core.Package;
import orgomg.cwm.resource.relational.Catalog;
import orgomg.cwm.resource.relational.Schema;

/**
 * DOC klliu Folder node node displayed on repository view (UI), knowing exact folder type by folder
 * object:TDQFolderObject.
 */
public class DBTableFolderRepNode extends DQDBFolderRepositoryNode implements IConnectionElementSubFolder {

    private static Logger log = Logger.getLogger(DBTableFolderRepNode.class);

    private IRepositoryViewObject viewObject;

    private Catalog catalog;

    private ConnectionItem item;

    private Connection connection;

    private Schema schema;

    public Catalog getCatalog() {
        return this.catalog;
    }

    public ConnectionItem getItem() {
        return this.item;
    }

    public Connection getConnection() {
        if (this.connection == null) {
            getConnectionFromViewObject();
        }
        return this.connection;
    }

    public Schema getSchema() {
        return this.schema;
    }

    /**
     * DOC klliu FolderRepNode constructor comment.
     * 
     * @param object
     * @param parent if parent is null will try to create new one to insert of old parent.
     * @param type
     */
    public DBTableFolderRepNode(IRepositoryViewObject object, RepositoryNode parent, ENodeType type,
            org.talend.core.model.general.Project inWhichProject) {
        super(object, parent, type, inWhichProject);
        if (parent == null) {
            RepositoryNode createParentNode = createParentNode(object);
            this.setParent(createParentNode);
        }
    }

    /**
     * DOC talend Comment method "setConnection".
     * 
     * @param object
     */
    private void getConnectionFromViewObject() {
        IRepositoryViewObject object = this.getObject() == null ? this.getParent().getObject() : this.getObject();
        if (object != null && object instanceof ISubRepositoryObject) {
            Property property = ((ISubRepositoryObject) object).getProperty();
            if (property == null) {
                return;
            }
            Item theItem = property.getItem();
            if (theItem != null && theItem instanceof ConnectionItem) {
                connection = ((ConnectionItem) theItem).getConnection();
            }
        }
    }

    /**
     * create the node of parent.
     * 
     * @param object
     * @return
     */
    private RepositoryNode createParentNode(IRepositoryViewObject object) {
        RepositoryNode dbParentRepNode = null;
        if (object instanceof MetadataCatalogRepositoryObject) {
            dbParentRepNode = DQRepNodeCreateFactory.createDBCatalogRepNode(object, null, ENodeType.TDQ_REPOSITORY_ELEMENT,
                    getProject());
        } else if (object instanceof MetadataSchemaRepositoryObject) {
            dbParentRepNode = new DBSchemaRepNode(object, null, ENodeType.TDQ_REPOSITORY_ELEMENT, getProject());
        }
        object.setRepositoryNode(dbParentRepNode);
        return dbParentRepNode;
    }

    @Override
    public List<IRepositoryNode> getChildren() {
        // MOD gdbu 2011-7-1 bug : 22204
        List<IRepositoryNode> children = new ArrayList<IRepositoryNode>();
        IRepositoryViewObject object = this.getParent().getObject();
        createRepositoryNodeTableFolderNode(children, object);
        // ADD msjian 2011-7-22 22206: fix the note 93101
        if (DQRepositoryNode.isUntilSchema()) {
            return children;
        }
        // ~22206
        return filterResultsIfAny(children);
        // ~22204
    }

    /**
     * Create TableFolderNodeRepositoryNode.
     * 
     * @param nodes parent RepositoryNode
     * @param metadataObject parent CatalogViewObject or SchemaViewObject
     */
    private void createRepositoryNodeTableFolderNode(List<IRepositoryNode> nodes, IRepositoryViewObject metadataObject) {
        List<TdTable> tables = new ArrayList<TdTable>();
        String filterCharacter = null;
        try {
            if (metadataObject instanceof MetadataCatalogRepositoryObject) {
                viewObject = ((MetadataCatalogRepositoryObject) metadataObject).getViewObject();
                item = (ConnectionItem) viewObject.getProperty().getItem();
                connection = item.getConnection();
                if (((MetadataCatalogRepositoryObject) metadataObject).getCatalog().eIsProxy()) {
                    // reload the connection to make sure the connection(and all it's owned elements) is not proxy
                    reloadConnectionViewObject();
                }
                catalog = ((MetadataCatalogRepositoryObject) metadataObject).getCatalog();

                tables = PackageHelper.getTables(catalog);
                filterCharacter = RepositoryNodeHelper.getTableFilter(catalog, schema);

                // MOD TDQ-8718 20140505 yyin --the repository view cares about if use the filter or not, the column
                // select dialog cares about if connect to DB or not.
                if (tables.isEmpty()) {
                    if (isCallingFromColumnDialog()) {
                        tables = DqRepositoryViewService.getTables(connection, catalog, null, isLoadDBFromDialog());
                    } else if (!isOnFilterring()) {
                        // MOD mzhao 0022204 : when the tree is rendering with a filter, do not loading from db.
                        tables = DqRepositoryViewService.getTables(connection, catalog, null, true);
                    }
                    if (tables.size() > 0) {
                        ElementWriterFactory.getInstance().createDataProviderWriter().save(item, false);
                    }
                }

            } else {
                viewObject = ((MetadataSchemaRepositoryObject) metadataObject).getViewObject();
                item = (ConnectionItem) viewObject.getProperty().getItem();
                connection = item.getConnection();
                if (((MetadataSchemaRepositoryObject) metadataObject).getSchema().eIsProxy()) {
                    // reload the connection to make sure the connection(and all it's owned elements) is not proxy
                    reloadConnectionViewObject();
                }
                schema = ((MetadataSchemaRepositoryObject) metadataObject).getSchema();
                tables = PackageHelper.getTables(schema);
                filterCharacter = RepositoryNodeHelper.getTableFilter(catalog, schema);
                RepositoryNode parent = metadataObject.getRepositoryNode().getParent();
                IRepositoryViewObject object = parent.getObject();
                if (object instanceof MetadataCatalogRepositoryObject && filterCharacter.equals("")) { //$NON-NLS-1$
                    filterCharacter = RepositoryNodeHelper.getTableFilter(
                            ((MetadataCatalogRepositoryObject) object).getCatalog(), null);
                }

                if (tables.isEmpty()) {
                    if (isCallingFromColumnDialog()) {
                        tables = DqRepositoryViewService.getTables(connection, schema, null, isLoadDBFromDialog());
                    } else if (!isOnFilterring()) {
                        // MOD mzhao 0022204 : when the tree is rendering with a filter, do not loading from db.
                        tables = DqRepositoryViewService.getTables(connection, schema, null, true);
                    }
                    if (tables.size() > 0) {
                        ElementWriterFactory.getInstance().createDataProviderWriter().save(item, false);
                    }
                }
            }

            ConnectionUtils.retrieveColumn(tables);

            if (filterCharacter != null && !filterCharacter.equals("")) { //$NON-NLS-1$
                tables = RepositoryNodeHelper.filterTables(tables, filterCharacter);
            }

        } catch (Exception e) {
            log.error(e, e);
        }
        createTableRepositoryNode(tables, nodes);
    }

    /**
     * DOC klliu Comment method "createTableRepositoryNode".
     * 
     * @param tables
     */
    private void createTableRepositoryNode(List<TdTable> tables, List<IRepositoryNode> node) {
        for (TdTable table : tables) {
            table.setTableType("TABLE"); //$NON-NLS-1$
            TdTableRepositoryObject metadataTable = new TdTableRepositoryObject(viewObject, table);
            metadataTable.setTableName(table.getName());
            metadataTable.setLabel(table.getName());
            metadataTable.setId(table.getName());

            DBTableRepNode tableNode = new DBTableRepNode(metadataTable, this, ENodeType.TDQ_REPOSITORY_ELEMENT, getProject());
            tableNode.setProperties(EProperties.LABEL, ERepositoryObjectType.METADATA_CON_TABLE);
            tableNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_CON_TABLE);

            metadataTable.setRepositoryNode(tableNode);
            node.add(tableNode);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.RepositoryNode#getLabel()
     */
    @Override
    public String getLabel() {
        return "Tables (" + this.getChildrenCount() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    private int getChildrenCount() {
        List<TdTable> tables = new ArrayList<TdTable>();
        IRepositoryViewObject object = this.getParent().getObject();
        if (object instanceof MetadataCatalogRepositoryObject) {
            catalog = ((MetadataCatalogRepositoryObject) object).getCatalog();
            tables = PackageHelper.getTables(catalog);
        } else {
            schema = ((MetadataSchemaRepositoryObject) object).getSchema();
            tables = PackageHelper.getTables(schema);
        }
        return tables.size();
    }

    @Override
    public boolean hasChildren() {
        // MOD gdbu 2011-9-1 TDQ-3457
        if (!hasChildrenInFile()) {
            if (isOnFilterring()) {
                return false;
            } else {
                if (!hasChildrenInDataBase()) {
                    return false;
                }
            }
        }
        return true;
        // ~ TDQ-3457
    }

    private boolean hasChildrenInFile() {
        IRepositoryViewObject object = this.getParent().getObject();
        if (object instanceof MetadataCatalogRepositoryObject) {
            Catalog catalogInFile = ((MetadataCatalogRepositoryObject) object).getCatalog();
            List<TdTable> tables = PackageHelper.getTables(catalogInFile);
            if (tables.isEmpty()) {
                return false;
            } else {
                return true;
            }
        } else if (object instanceof MetadataSchemaRepositoryObject) {
            Schema schemaInFile = ((MetadataSchemaRepositoryObject) object).getSchema();
            List<TdTable> tables = PackageHelper.getTables(schemaInFile);
            if (tables.isEmpty()) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    private boolean hasChildrenInDataBase() {
        boolean hasChildrenInDB = false;
        IRepositoryViewObject object = this.getParent().getObject();
        if (object instanceof MetadataCatalogRepositoryObject) {
            viewObject = ((MetadataCatalogRepositoryObject) object).getViewObject();
            item = (ConnectionItem) viewObject.getProperty().getItem();
            connection = item.getConnection();
            catalog = ((MetadataCatalogRepositoryObject) object).getCatalog();
            try {
                hasChildrenInDB = DqRepositoryViewService.isCatalogHasChildren(connection, catalog, null,
                        DqRepositoryViewService.TABLE_TYPES);
            } catch (Exception e) {
                log.error(e.toString());
            }
        } else {
            viewObject = ((MetadataSchemaRepositoryObject) object).getViewObject();
            item = (ConnectionItem) viewObject.getProperty().getItem();
            connection = item.getConnection();
            schema = ((MetadataSchemaRepositoryObject) object).getSchema();
            try {
                hasChildrenInDB = DqRepositoryViewService.isSchemaHasChildren(connection, schema, null,
                        DqRepositoryViewService.TABLE_TYPES);
            } catch (MissingDriverException e) {
                throw e;
            } catch (Exception e) {
                log.error(e.toString());
            }
        }
        return hasChildrenInDB;
    }

    /**
     * return the Catalog or Schema, or null.
     * 
     * @return
     */
    public Package getPackage() {
        Package result = null;

        if (this.getCatalog() != null) {
            result = this.getCatalog();
        } else if (this.getSchema() != null) {
            result = this.getSchema();
        } else {
            RepositoryNode parent = this.getParent();

            if (parent instanceof DBSchemaRepNode) {
                this.schema = ((DBSchemaRepNode) parent).getSchema();
                result = this.schema;
            } else if (parent instanceof DBCatalogRepNode) {
                this.catalog = ((DBCatalogRepNode) parent).getCatalog();
                result = this.catalog;
            }
        }

        return result;
    }
}
