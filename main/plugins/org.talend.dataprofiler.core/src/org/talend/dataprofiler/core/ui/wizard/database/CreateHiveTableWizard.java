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
package org.talend.dataprofiler.core.ui.wizard.database;

import java.util.List;

import org.eclipse.ui.IWorkbench;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.properties.Item;
import org.talend.dataprofiler.core.i18n.internal.DefaultMessagesImpl;
import org.talend.designer.hdfsbrowse.model.IHDFSNode;
import org.talend.repository.hdfs.ui.HDFSFileSelectorWizardPage;
import org.talend.repository.hdfs.ui.HDFSSchemaWizard;
import org.talend.repository.hdfs.ui.metadata.ExtractHDFSSchemaManager;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;

/**
 * created by yyin on 2015年4月28日 Detailled comment
 *
 */
public class CreateHiveTableWizard extends HDFSSchemaWizard {

    private HDFSFileSelectorWizardPage step1Page;

    private CreateHiveTableStep2page step2page;

    private CreateHiveTableStep3Page step3Page;

    private RepositoryNode currentNode;

    public CreateHiveTableWizard(IWorkbench workbench, RepositoryNode repositoryNode, String[] existingNames) {
        super(workbench, true, repositoryNode.getObject(), null, existingNames, false);
        currentNode = repositoryNode;
    }

    @Override
    public void addPages() {
        step1Page = new HDFSFileSelectorWizardPage(connectionItem, isRepositoryObjectEditable(), this.getTempHDFSConnection());
        step1Page.setTitle(DefaultMessagesImpl.getString(
                "HDFSSchemaWizardPage.titleCreate", connectionItem.getProperty().getLabel())); //$NON-NLS-1$
        step1Page.setDescription(DefaultMessagesImpl.getString("CreateHiveTableStep2page.descriptionCreate")); //$NON-NLS-1$
        step1Page.setPageComplete(true);
        addPage(step1Page);

        step2page = new CreateHiveTableStep2page(connectionItem, getTempHDFSConnection());
        step2page.setTitle(DefaultMessagesImpl.getString("CreateHiveTableStep2page.titleCreate", connectionItem.getProperty() //$NON-NLS-1$
                .getLabel()));
        step2page.setDescription(DefaultMessagesImpl.getString("CreateHiveTableStep2page.descriptionCreate")); //$NON-NLS-1$
        step2page.setPageComplete(true);
        addPage(step2page);

        // add step 3 page:
        step3Page = new CreateHiveTableStep3Page(currentNode);
        step3Page.setTitle("Where to create the table:"); //$NON-NLS-1$
        step3Page.setDescription("create a table on the selected hive connection"); //$NON-NLS-1$
        step3Page.setPageComplete(true);
        addPage(step3Page);
    }

    @Override
    public boolean performFinish() {
        IRepositoryNode selectedHive = step3Page.getSelectedHive();
        if (selectedHive == null) {
            // to open the wizard: create hive

            // selectedHive = new one
        }
        // use the current hive to create the DDL:

        String createTableSql = getCreateTableSql();

        // execute the DDL
        Item item = selectedHive.getObject().getProperty().getItem();

        // java.sql.Statement stmt = ((DatabaseConnectionItem)item).getConnection().createStatement();
        // try {
        // stmt.execute(createTableSql);
        // } catch (java.sql.SQLException e) {
        //
        // } finally {
        //
        // stmt.close();
        // }

        return true;
    }

    // private MetadataTable getSelectedMetadata() {
    // List<MetadataTable> tables = ConnectionHelper.getTablesWithOrders(getTempHDFSConnection());
    // if (tables != null && tables.size() > 0) {
    // MetadataTable metadataTable2 = tables.get(0);
    // return metadataTable2;
    // }
    // return null;
    // }

    /**
     * "Create external table <filename>(<schema>)  row format delimited fields terminated by '\n' stored as textfile location '<path to hdfs file>'"
     * 
     * @param metadataTable
     * 
     * @return
     */
    private String getCreateTableSql() {
        StringBuilder createTableSQL = new StringBuilder();

        createTableSQL.append("CREATE EXTERNAL TABLE ");
        // createTableSQL.append(createIfNotExist?"IF NOT EXISTS":"");
        String tableName = "";
        createTableSQL.append(tableName);

        IHDFSNode selectedFile = this.step1Page.getSelectedFile();
        try {
            List<MetadataColumn> metadataColumns = ExtractHDFSSchemaManager.getInstance().extractColumns(getTempHDFSConnection(),
                    selectedFile);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // add columns
        // if ((metadatas != null) && (metadatas.size() > 0)) {
        // IMetadataTable metadata = metadatas.get(1);
        // if (metadata != null) {
        // List<IMetadataColumn> columnList = metadata.getListColumns();
        // if (columnList != null && columnList.size() > 0) {
        // createTableSQL.append(" PARTITIONED BY (");
        // util.generateColumnsSQL(columnList, createTableSQL);
        // createTableSQL.append(")");
        // }
        // }
        // }

        createTableSQL.append("row format delimited fields terminated by '\\n' stored as textfile");

        String location = "";
        createTableSQL.append(" LOCATION '");
        createTableSQL.append(location);
        createTableSQL.append(" + \"'");

        return createTableSQL.toString();
    }
}
