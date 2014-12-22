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
package org.talend.dataprofiler.core.ui.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.wizard.Wizard;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.dataprofiler.core.CorePlugin;
import org.talend.dataprofiler.core.ui.utils.MessageUI;
import org.talend.dataprofiler.core.ui.utils.UIMessages;
import org.talend.dataprofiler.core.ui.wizard.database.DatabaseWizardPage;
import org.talend.dataquality.analysis.impl.AnalysisImpl;
import org.talend.dataquality.helpers.MetadataHelper;
import org.talend.dataquality.reports.impl.TdReportImpl;
import org.talend.dq.analysis.parameters.ConnectionParameter;
import org.talend.dq.helper.RepositoryNodeHelper;
import org.talend.dq.helper.resourcehelper.AnaResourceFileHelper;
import org.talend.dq.helper.resourcehelper.DQRuleResourceFileHelper;
import org.talend.dq.helper.resourcehelper.IndicatorResourceFileHelper;
import org.talend.dq.helper.resourcehelper.PatternResourceFileHelper;
import org.talend.dq.helper.resourcehelper.RepResourceFileHelper;
import org.talend.dq.helper.resourcehelper.ResourceFileMap;
import org.talend.dq.nodes.DQRepositoryNode;
import org.talend.repository.model.IRepositoryNode;
import org.talend.utils.sugars.ReturnCode;
import org.talend.utils.sugars.TypedReturnCode;
import orgomg.cwm.objectmodel.core.ModelElement;

/**
 * DOC zqin class global comment. Detailled comment
 */
public abstract class AbstractWizard extends Wizard implements ICWMResouceAdapter {

    private static Logger log = Logger.getLogger(DatabaseWizardPage.class);

    protected ModelElement modelElement = null;

    @Override
    public boolean performFinish() {

        // MOD mzhao feature 15750 Use repository object represent ModelElement.
        modelElement = initCWMResourceBuilder();
        if (modelElement != null) {
            fillMetadataToCWMResource(modelElement);
            // Save the repository objects.
            TypedReturnCode<Object> csResult = createAndSaveCWMFile(modelElement);
            if (csResult.isOk()) {
                Object savedObj = csResult.getObject();
                if (savedObj instanceof Item) {
                    openEditor((Item) savedObj);
                }

                if (modelElement instanceof AnalysisImpl || modelElement instanceof TdReportImpl) {
                    // MOD by zshen refresh the folder which contain the modelElement but not select it
                    CorePlugin.getDefault().refreshDQView(
                            RepositoryNodeHelper.findNearestSystemFolderNode(RepositoryNodeHelper.recursiveFind(modelElement)));
                } else {
                    IRepositoryNode currentSelectionNode = CorePlugin.getDefault().getCurrentSelectionNode();
                    // if DqRepositoryView is not opened currentSelectionNode will be null and refreshDQView method will
                    // get one error log.
                    CorePlugin.getDefault().refreshDQView(currentSelectionNode);
                }
                CorePlugin.getDefault().refreshWorkSpace();
                // MOD gdbu 2011-11-18 TDQ-3969 : after create items re-filter the tree , to create a new list .
                if (DQRepositoryNode.isOnFilterring()) {
                    RepositoryNodeHelper.fillTreeList(null);
                    RepositoryNodeHelper.setFilteredNode(RepositoryNodeHelper.getRootNode(
                            ERepositoryObjectType.TDQ_DATA_PROFILING, true));
                }

                return true;
            } else {
                MessageUI.openError(csResult.getMessage());
            }
        }

        return false;
    }

    public abstract void openEditor(Item item);

    public ReturnCode checkMetadata() {
        String elementName = getParameter().getName();
        IFolder folderResource = getParameter().getFolderProvider().getFolderResource();
        if (elementName == null || folderResource == null) {
            return new ReturnCode("", false); //$NON-NLS-1$
        } else {
            Collection<ModelElement> modelElements = new ArrayList<ModelElement>();

            switch (getParameter().getParamType()) {
            case ANALYSIS:
                modelElements.addAll(AnaResourceFileHelper.getInstance().getAllElement(folderResource));
                break;
            case REPORT:
                modelElements.addAll(RepResourceFileHelper.getInstance().getAllElement(folderResource));
                break;
            case PATTERN:
                modelElements.addAll(PatternResourceFileHelper.getInstance().getAllElement(folderResource));
                break;
            case CONNECTION:
                List<Connection> conns = new ArrayList<Connection>();
                try {
                    for (ConnectionItem connItem : ProxyRepositoryFactory.getInstance().getMetadataConnectionsItem()) {
                        conns.add(connItem.getConnection());
                    }
                } catch (PersistenceException e) {
                    log.error(e, e);
                }
                modelElements.addAll(conns);
                break;
            case DQRULE:
                modelElements.addAll(DQRuleResourceFileHelper.getInstance().getAllElement(folderResource));
                break;
            case UDINDICATOR:
                modelElements.addAll(IndicatorResourceFileHelper.getInstance().getAllElement(folderResource));
                break;
            default:
                break;
            }

            if (!modelElements.isEmpty()) {
                for (ModelElement element : modelElements) {
                    String theElementName = element.getName();
                    if (theElementName == null) {
                        if (element instanceof Connection) {
                            // MOD klliu 2011-04-18 get repository Object label
                            // theElementName =RepositoryNodeHelper.recursiveFind(element).getObject().getLabel();
                        }
                    }

                    if (elementName.equals(theElementName)) {
                        // MOD xqliu 2010-09-21 bug 15762
                        return new ReturnCode(UIMessages.MSG_ANALYSIS_SAME_NAME, false);
                        // if (!MessageUI.openConfirm(UIMessages.MSG_ANALYSIS_SAME_NAME)) {
                        //     getParameter().setName(elementName + DateFormatUtils.format(new Date(), "yyyyMMddHHmmss")); //$NON-NLS-1$
                        // }
                        // break;
                        // ~ 15762
                    }
                }
            }
        }
        return new ReturnCode(true);
    }

    public void fillMetadataToCWMResource(ModelElement cwmElement) {
        MetadataHelper.setDevStatus(cwmElement, getParameter().getStatus());
        MetadataHelper.setAuthor(cwmElement, getParameter().getAuthor());
        MetadataHelper.setPurpose(getParameter().getPurpose(), cwmElement);
        MetadataHelper.setDescription(getParameter().getDescription(), cwmElement);
        MetadataHelper.setVersion(getParameter().getVersion(), cwmElement);
    }

    protected abstract ResourceFileMap getResourceFileMap();

    protected abstract ConnectionParameter getParameter();

    protected abstract String getEditorName();

}
