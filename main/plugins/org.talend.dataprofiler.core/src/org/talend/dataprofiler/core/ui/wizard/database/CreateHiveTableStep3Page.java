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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.talend.dataprofiler.core.i18n.internal.DefaultMessagesImpl;
import org.talend.dataprofiler.core.ui.wizard.AbstractWizardPage;
import org.talend.dq.nodes.hadoopcluster.HiveOfHCFolderRepNode;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;

/**
 * The third page of Creating hive table wizard, to let the user select an existed hive, or create a new one, on which
 * to create the hive table.
 *
 */
public class CreateHiveTableStep3Page extends AbstractWizardPage {

    private RepositoryNode parent;

    private List<IRepositoryNode> hiveNodes;

    private CCombo hiveListCombo;

    private Button selectOne;

    private Button createOne;

    private Text tableNameText;

    /**
     * DOC yyin CreateHiveTableStep3Page constructor comment.
     * 
     * @param currentNode
     */
    public CreateHiveTableStep3Page(RepositoryNode currentNode) {
        parent = currentNode.getParent().getParent();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent2) {
        Composite container = new Composite(parent2, SWT.NONE);
        GridLayout gdLayout = new GridLayout(2, false);
        container.setLayout(gdLayout);

        GridData gd = new GridData();
        gd.widthHint = 280;
        gd.heightHint = 22;

        // the field of the table name
        Label label1 = new Label(container, SWT.NONE);
        label1.setText(DefaultMessagesImpl.getString("CreateHiveTableStep3Page.newTableName")); //$NON-NLS-1$

        tableNameText = new Text(container, SWT.BORDER);
        tableNameText.setText(StringUtils.EMPTY);
        tableNameText.setLayoutData(gd);
        tableNameText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                if (StringUtils.isBlank(tableNameText.getText())) {
                    setErrorMessage(DefaultMessagesImpl.getString("AbstractMetadataFormPage.nameCannotBeEmpty")); //$NON-NLS-1$
                    setPageComplete(false);
                } else if (StringUtils.contains(tableNameText.getText(), '-')) {
                    setErrorMessage(DefaultMessagesImpl.getString("CreateHiveTableStep3Page.nameNotQualified")); //$NON-NLS-1$
                    setPageComplete(false);
                } else {
                    setErrorMessage(null);
                    setPageComplete(true);
                }
            }

        });

        selectOne = new Button(container, SWT.RADIO);
        selectOne.setText(DefaultMessagesImpl.getString("CreateHiveTableStep3Page.selectHive")); //$NON-NLS-1$
        selectOne.setSelection(true);

        hiveListCombo = new CCombo(container, SWT.BORDER);
        hiveListCombo.setEditable(false);
        hiveListCombo.setLayoutData(gd);

        createOne = new Button(container, SWT.RADIO);
        createOne.setText(DefaultMessagesImpl.getString("CreateHiveTableStep3Page.createHive")); //$NON-NLS-1$

        String[] allHiveConnection = getAllHiveConnection();
        hiveListCombo.setItems(allHiveConnection);
        if (allHiveConnection.length > 0) {
            hiveListCombo.select(0);// default to select the first
        }
        setControl(container);
    }

    /*
     * when the current page is actived, get the selected file name from the wizard
     */
    @Override
    public void setVisible(boolean visible) {
        String defaultTableName = ((CreateHiveTableWizard) this.getWizard()).getDefaultTableName();
        tableNameText.setText(defaultTableName);
        super.setVisible(visible);

        // make the previous pages is pageComplete
        for (int i = 0; i < getWizard().getPageCount(); i++) {
            IWizardPage[] pages = getWizard().getPages();
            ((WizardPage) pages[i]).setPageComplete(true);
        }

        getContainer().updateButtons();// need to update
    }

    /**
     * find all existed hive connections of the current parent: hadoop cluster
     * 
     * @return
     */
    private String[] getAllHiveConnection() {
        List<IRepositoryNode> children = parent.getChildren();
        List<String> allHives = new ArrayList<String>();
        for (IRepositoryNode child : children) {
            if (child instanceof HiveOfHCFolderRepNode) {
                hiveNodes = child.getChildren();
                for (IRepositoryNode hive : hiveNodes) {
                    allHives.add(hive.getLabel());
                }
            }
        }
        if (allHives.size() == 0) {// if no hives, make the choice of creating a new hive as default
            createOne.setSelection(true);
            selectOne.setSelection(false);
            selectOne.setEnabled(false);
        }

        return allHives.toArray(new String[allHives.size()]);
    }

    /**
     * find the selected hive node, or null(when the user select to create a new hive)
     * 
     * @return the selected Hive node(if selected a existed one), or null( if check "create a new hive")
     */
    public IRepositoryNode getSelectedHive() {
        if (createOne.getSelection()) {
            return null;
        }
        for (IRepositoryNode hive : hiveNodes) {
            if (StringUtils.equals(hiveListCombo.getText(), hive.getLabel())) {
                return hive;
            }
        }
        return null;
    }

    public String getTableName() {
        return this.tableNameText.getText();
    }
}
