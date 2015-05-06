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
package org.talend.dataprofiler.core.ui.action.actions;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.dataprofiler.core.i18n.internal.DefaultMessagesImpl;
import org.talend.dataprofiler.core.ui.wizard.database.CreateHiveTableWizard;
import org.talend.repository.hdfs.action.CreateHDFSSchemaAction;
import org.talend.repository.hdfs.node.model.HDFSRepositoryNodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.hdfs.HDFSConnection;
import org.talend.repository.model.hdfs.HDFSConnectionItem;

/**
 * created by yyin on 2015年4月27日 Detailled comment
 *
 */
public class CreateHiveTableAction extends CreateHDFSSchemaAction {

    private static final String label = DefaultMessagesImpl.getString("CreateHiveTableAction.create"); //$NON-NLS-1$

    public CreateHiveTableAction(RepositoryNode node) {
        super();
        setText(label);
        setToolTipText(label);
        setImageDescriptor(ImageProvider.getImageDesc(ECoreImage.METADATA_TABLE_ICON));
        repositoryNode = node;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.core.repository.ui.actions.metadata.AbstractCreateAction#init(org.talend.repository.model.RepositoryNode
     * )
     */
    @Override
    protected void init(RepositoryNode node) {
        ERepositoryObjectType nodeType = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);
        if (HDFSRepositoryNodeType.HDFS.equals(nodeType)) {
            setText(label);
            collectChildNames(node);
            setEnabled(true);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.actions.AContextualAction#doRun()
     */
    @Override
    protected void doRun() {
        HDFSConnectionItem item = (HDFSConnectionItem) repositoryNode.getObject().getProperty().getItem();
        if (checkHDFSConnection((HDFSConnection) item.getConnection())) {
            openHDFSSchemaWizard(item);
        }

    }

    private void openHDFSSchemaWizard(final HDFSConnectionItem item) {
        WizardDialog wizardDialog = new WizardDialog(Display.getCurrent().getActiveShell(), new CreateHiveTableWizard(
                PlatformUI.getWorkbench(), repositoryNode, getExistingNames()));
        wizardDialog.setPageSize(WIZARD_WIDTH, WIZARD_HEIGHT);
        wizardDialog.create();
        wizardDialog.open();

    }

}
