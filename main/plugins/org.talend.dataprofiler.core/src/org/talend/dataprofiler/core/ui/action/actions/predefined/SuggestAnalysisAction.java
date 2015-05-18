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
package org.talend.dataprofiler.core.ui.action.actions.predefined;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.dataprofiler.core.CorePlugin;
import org.talend.dataprofiler.core.ImageLib;
import org.talend.dataprofiler.core.i18n.internal.DefaultMessagesImpl;
import org.talend.dataprofiler.service.ISemanticStudioService;
import org.talend.dq.helper.RepositoryNodeHelper;
import org.talend.repository.model.IRepositoryNode;
import org.talend.resource.EResourceConstant;

/**
 * created by scorreia on Oct 1, 2013 Detailled comment
 * 
 */
public class SuggestAnalysisAction extends Action {

    private static Logger log = Logger.getLogger(SuggestAnalysisAction.class);

    private ISemanticStudioService service;

    private MetadataTable metadataTable;

    public SuggestAnalysisAction(MetadataTable set) {
        this();
        this.metadataTable = set;
    }

    public SuggestAnalysisAction() {
        super(DefaultMessagesImpl.getString("SuggestAnalysisActionProvider.suggestAnalysis")); // TODO externalize //$NON-NLS-1$
        setImageDescriptor(ImageLib.getImageDescriptor(ImageLib.ACTION_NEW_ANALYSIS));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        ISemanticStudioService service = CorePlugin.getDefault().getSemanticStudioService();

        if (service != null) {
            int wizardReturnCode = service.openSemanticDiscoveryWizard(metadataTable);
            boolean success =false;
            if(SWT.OK==wizardReturnCode){
                success = service.suggestAnalysis(this.metadataTable);
            }
            if (success) {
                IRepositoryNode node = RepositoryNodeHelper.getDataProfilingFolderNode(EResourceConstant.ANALYSIS);
                CorePlugin.getDefault().refreshDQView(node);
            } else {
                MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error",
                        "Failed to recommend analysis. See the log for more details.");
            }
        }

    }

}
