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
package org.talend.dataprofiler.core.ui.dialog;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.talend.dataprofiler.core.PluginConstant;
import org.talend.dataprofiler.core.i18n.internal.DefaultMessagesImpl;
import org.talend.dataprofiler.core.ui.editor.analysis.AbstractAnalysisMetadataPage;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;

/**
 * created by talend on Jan 7, 2015 Detailled comment
 *
 */
public class ColumnsSelectWithConstraintDialog extends ColumnsSelectionDialog {

    /**
     * DOC talend ColumnsSelectionTakeConstraintDialog constructor comment.
     * 
     * @param metadataFormPage
     * @param parent
     * @param title
     * @param checkedRepoNodes
     * @param rootNode
     * @param message
     */
    public ColumnsSelectWithConstraintDialog(AbstractAnalysisMetadataPage metadataFormPage, Shell parent, String title,
            List<? extends IRepositoryNode> checkedRepoNodes, RepositoryNode rootNode, String message) {
        super(metadataFormPage, parent, title, checkedRepoNodes, rootNode, message);
    }

    public ColumnsSelectWithConstraintDialog(AbstractAnalysisMetadataPage metadataFormPage, Shell parent, String title,
            List<? extends IRepositoryNode> checkedRepoNodes, String message, boolean addConnFilter) {
        super(metadataFormPage, parent, title, checkedRepoNodes, message, addConnFilter);
    }

    @Override
    protected void updateStatusBySelection() {
        Status fCurrStatus;
        // the table node all stored in the map as key, so when the key's number >1, means there are more than one
        // table's column selected. then make the ok status disable
        if (super.modelElementCheckedMap.keySet().size() > 1) {
            fCurrStatus = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.OK, DefaultMessagesImpl.getString(
                    "ColumnMasterDetailsPage.noSameTableWarning", PluginConstant.SPACE_STRING), null); //$NON-NLS-1$ 
        } else {
            fCurrStatus = new Status(IStatus.OK, PlatformUI.PLUGIN_ID, IStatus.OK, PluginConstant.EMPTY_STRING, null);
        }
        updateStatus(fCurrStatus);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.ui.dialog.TwoPartCheckSelectionDialog#updateOKStatus()
     */
    @Override
    protected void updateOKStatus() {
        super.updateOKStatus();
        Button okButton = this.getOkButton();
        if (okButton != null && !okButton.isDisposed() && okButton.isEnabled()) {
            updateStatusBySelection();
        }
    }

}
