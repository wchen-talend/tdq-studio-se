package org.talend.dataprofiler.core.ui.action.actions;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.talend.commons.emf.FactoriesUtil;
import org.talend.dataprofiler.core.ui.wizard.analysis.table.DQRuleLabelProvider;
import org.talend.dq.helper.PropertyHelper;
import org.talend.dq.helper.resourcehelper.DQRuleResourceFileHelper;

public class ImportMatchRuleLabelProvider extends DQRuleLabelProvider {

    @Override
    public String getText(Object element) {
        if (element instanceof IFile && FactoriesUtil.DQRULE.equals(((IFile) element).getFileExtension())) {
            IFile file = (IFile) element;
            String name = DQRuleResourceFileHelper.getInstance().getModelElement(file).getName();
            String purpose = PropertyHelper.getProperty(file).getPurpose();
            if (purpose != null && !StringUtils.EMPTY.equals(purpose.trim())) {
                name += " (" + purpose + ")";
            }
            return name;
        }

        if (element instanceof IFolder) {
            return ((IFolder) element).getName();
        }

        return ""; //$NON-NLS-1$
    }
}
