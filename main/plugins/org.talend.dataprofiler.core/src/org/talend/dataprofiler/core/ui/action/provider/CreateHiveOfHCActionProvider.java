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
import org.talend.core.hadoop.version.EHadoopDistributions;
import org.talend.core.hadoop.version.EHadoopVersion4Drivers;
import org.talend.dataprofiler.core.service.AbstractSvnRepositoryService;
import org.talend.dataprofiler.core.service.GlobalServiceRegister;
import org.talend.dataprofiler.core.ui.action.actions.CreateHiveOfHCAction;
import org.talend.dq.nodes.hadoopcluster.HadoopClusterConnectionRepNode;
import org.talend.dq.nodes.hadoopcluster.HiveOfHCFolderRepNode;
import org.talend.repository.hadoopcluster.util.HCRepositoryUtil;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.hadoopcluster.HadoopClusterConnection;
import org.talend.repository.model.hadoopcluster.HadoopClusterConnectionItem;

/**
 * created by yyin on 2015年4月23日 Detailled comment
 *
 */
public class CreateHiveOfHCActionProvider extends AbstractCommonActionProvider {

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
            if (hideAction(node)) {
                return;
            }

            // ~20051
            IAction action = null;
            if (node instanceof HadoopClusterConnectionRepNode || node instanceof HiveOfHCFolderRepNode) {
                action = new CreateHiveOfHCAction(node);
                menu.add(action);
            }

        }
    }

    private boolean hideAction(RepositoryNode node) {
        HadoopClusterConnectionItem hcConnectionItem = HCRepositoryUtil.getHCConnectionItemFromRepositoryNode(node);
        if (hcConnectionItem != null) {
            HadoopClusterConnection hcConnection = (HadoopClusterConnection) hcConnectionItem.getConnection();
            EHadoopDistributions distribution = EHadoopDistributions.getDistributionByName(hcConnection.getDistribution(), false);
            EHadoopVersion4Drivers version4Drivers = EHadoopVersion4Drivers.indexOfByVersion(hcConnection.getDfVersion());
            if (EHadoopVersion4Drivers.APACHE_0_20_204.equals(version4Drivers)
                    || EHadoopVersion4Drivers.APACHE_0_20_2.equals(version4Drivers)) {
                return true;
            }
            if (distribution == EHadoopDistributions.MICROSOFT_HD_INSIGHT) {
                return true;
            }
        }

        return false;
    }

}
