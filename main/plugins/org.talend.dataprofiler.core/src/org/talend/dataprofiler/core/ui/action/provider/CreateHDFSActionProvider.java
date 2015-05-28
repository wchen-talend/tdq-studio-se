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
package org.talend.dataprofiler.core.ui.action.provider;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.talend.dataprofiler.core.service.AbstractSvnRepositoryService;
import org.talend.dataprofiler.core.service.GlobalServiceRegister;
import org.talend.dataprofiler.core.ui.action.actions.CreateHDFSAction;
import org.talend.dq.nodes.hadoopcluster.HDFSOfHCFolderRepNode;
import org.talend.dq.nodes.hadoopcluster.HadoopClusterConnectionRepNode;
import org.talend.repository.model.RepositoryNode;

/**
 * created by yyin on 2015年4月23日 Detailled comment
 *
 */
public class CreateHDFSActionProvider extends AbstractCommonActionProvider {

    @Override
    public void fillContextMenu(IMenuManager menu) {
        // MOD mzhao user readonly role on svn repository mode.
        if (!isShowMenu()) {
            return;
        }
        // MOD mzhao user readonly role on svn repository mode.
        AbstractSvnRepositoryService svnReposService = GlobalServiceRegister.getDefault().getSvnRepositoryService(
                AbstractSvnRepositoryService.class);
        if (svnReposService != null && svnReposService.isReadonly()) {
            return;
        }

        // MOD gdbu 2011-4-1 bug 20051
        RepositoryNode node = (RepositoryNode) getFistContextNode();

        if (node != null) {
            // ~20051
            IAction action = null;
            if (node instanceof HadoopClusterConnectionRepNode || node instanceof HDFSOfHCFolderRepNode) {
                action = new CreateHDFSAction(node);
                menu.add(action);
            }

        }
    }
}
