// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprofiler.core.ui.action.provider;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.talend.dataprofiler.core.ui.action.actions.predefined.SuggestAnalysisAction;
import org.talend.dataprofiler.core.ui.utils.RepNodeUtils;
import org.talend.dq.nodes.ColumnSetRepNode;
import org.talend.dq.nodes.DBTableRepNode;

public class SuggestAnalysisActionProvider extends AbstractCommonActionProvider {

    private SuggestAnalysisAction suggestAnalysisAction;

    public SuggestAnalysisActionProvider() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.navigator.CommonActionProvider#init(org.eclipse.ui.navigator.ICommonActionExtensionSite)
     */
    @Override
    public void init(ICommonActionExtensionSite site) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void fillContextMenu(IMenuManager menu) {
        // MOD mzhao user readonly role on svn repository mode.
        if (!isShowMenu()) {
            return;
        }

        TreeSelection currentSelection = ((TreeSelection) this.getContext().getSelection());
        if (!RepNodeUtils.isValidSelectionFromSameTable(currentSelection.toList())) {
            return;
        }
        Object firstElement = currentSelection.getFirstElement();
        if (firstElement instanceof DBTableRepNode) {
            DBTableRepNode node = (DBTableRepNode) firstElement;
            suggestAnalysisAction = new SuggestAnalysisAction(node.getTdTable());
        } else {
            // TODO here should be the second case some columns from same table
            return;
        }

        // suggestAnalysisAction.setColumnSelection(currentSelection);

        // when the selection is valid, only two possible status: only one columnset is select, otherwise is some
        // columns in the same columnset are selected; so only check the fist node in the selection is enough.
        if (currentSelection.toList().get(0) instanceof ColumnSetRepNode) {
            suggestAnalysisAction.setText("Suggest Analysis");
        } else {
            suggestAnalysisAction.setText("SuggestAnalysis");
        }
        menu.add(suggestAnalysisAction);
    }

}
