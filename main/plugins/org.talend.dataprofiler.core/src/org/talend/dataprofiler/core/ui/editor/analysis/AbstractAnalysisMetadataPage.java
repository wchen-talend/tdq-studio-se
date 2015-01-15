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
package org.talend.dataprofiler.core.ui.editor.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.FileEditorInput;
import org.talend.commons.emf.FactoriesUtil;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.database.PluginConstant;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.cwm.dependencies.DependenciesHandler;
import org.talend.cwm.helper.SwitchHelpers;
import org.talend.cwm.helper.TaggedValueHelper;
import org.talend.cwm.relational.TdColumn;
import org.talend.dataprofiler.core.i18n.internal.DefaultMessagesImpl;
import org.talend.dataprofiler.core.ui.IRuningStatusListener;
import org.talend.dataprofiler.core.ui.editor.AbstractMetadataFormPage;
import org.talend.dataprofiler.core.ui.editor.SupportContextEditor;
import org.talend.dataprofiler.core.ui.editor.composite.AbstractColumnDropTree;
import org.talend.dataprofiler.core.ui.editor.composite.DataFilterComp;
import org.talend.dataprofiler.core.ui.events.EventEnum;
import org.talend.dataprofiler.core.ui.events.EventManager;
import org.talend.dataprofiler.core.ui.events.EventReceiver;
import org.talend.dataquality.analysis.Analysis;
import org.talend.dataquality.analysis.AnalysisParameters;
import org.talend.dataquality.analysis.ExecutionLanguage;
import org.talend.dataquality.exception.DataprofilerCoreException;
import org.talend.dataquality.helpers.AnalysisHelper;
import org.talend.dataquality.indicators.Indicator;
import org.talend.dataquality.indicators.definition.IndicatorDefinition;
import org.talend.dataquality.properties.TDQAnalysisItem;
import org.talend.dataquality.rules.DQRule;
import org.talend.dq.analysis.AnalysisHandler;
import org.talend.dq.analysis.connpool.TdqAnalysisConnectionPool;
import org.talend.dq.helper.ContextHelper;
import org.talend.dq.helper.PropertyHelper;
import org.talend.dq.helper.RepositoryNodeHelper;
import org.talend.dq.helper.resourcehelper.AnaResourceFileHelper;
import org.talend.dq.nodes.AnalysisRepNode;
import org.talend.dq.nodes.DBConnectionRepNode;
import org.talend.dq.nodes.DFConnectionRepNode;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.utils.sugars.ReturnCode;
import orgomg.cwm.foundation.softwaredeployment.DataManager;
import orgomg.cwm.objectmodel.core.Dependency;
import orgomg.cwm.objectmodel.core.ModelElement;

/**
 * DOC rli class global comment. Detailled comment
 */
public abstract class AbstractAnalysisMetadataPage extends AbstractMetadataFormPage implements IRuningStatusListener {

    private static Logger log = Logger.getLogger(AbstractAnalysisMetadataPage.class);

    protected TDQAnalysisItem analysisItem;

    protected AnalysisRepNode analysisRepNode;

    protected Section analysisParamSection;

    protected DataFilterComp dataFilterComp;

    protected Section dataFilterSection = null;

    protected String stringDataFilter;

    // Used for Execute Engine section
    protected CCombo execCombo = null;

    protected String execLang = null;

    protected Button drillDownCheck;

    protected Text maxNumText;

    // ~Execute Engine section

    // MOD yyin 201204 TDQ-4977, change to TableCombo type to show the connection type.
    protected TableCombo connCombo;

    protected Text textConnVersion;

    protected Label labelConnDeleted;

    // Added 20140411 TDQ-8360 yyin
    private EventReceiver refreshDataProvider = null;

    public AbstractAnalysisMetadataPage(FormEditor editor, String id, String title) {
        super(editor, id, title);
        currentEditor = (AnalysisEditor) editor;
    }

    @Override
    protected ModelElement getCurrentModelElement(FormEditor editor) {
        // MOD klliu 2010-12-10
        IEditorInput editorInput = editor.getEditorInput();
        if (editorInput instanceof AnalysisItemEditorInput) {
            AnalysisItemEditorInput fileEditorInput = (AnalysisItemEditorInput) editorInput;
            analysisItem = fileEditorInput.getTDQAnalysisItem();
        } else if (editorInput instanceof FileEditorInput) {
            FileEditorInput input = (FileEditorInput) editorInput;
            Property property = PropertyHelper.getProperty(input.getFile());
            analysisItem = (TDQAnalysisItem) property.getItem();
        }
        Analysis analysis = analysisItem.getAnalysis();
        initAnalysisRepNode(analysis);
        return analysis;
    }

    public AnalysisRepNode getAnalysisRepNode() {
        return this.analysisRepNode;
    }

    private void initAnalysisRepNode(Analysis analysis) {
        RepositoryNode recursiveFind = RepositoryNodeHelper.recursiveFind(analysis);
        if (recursiveFind != null && recursiveFind instanceof AnalysisRepNode) {
            this.analysisRepNode = (AnalysisRepNode) recursiveFind;
        }
    }

    protected IRepositoryNode getCurrentRepNodeOnUI() {
        // MOD klliu 2010-12-10
        IEditorInput editorInput = getEditor().getEditorInput();
        if (editorInput instanceof AnalysisItemEditorInput) {
            AnalysisItemEditorInput fileEditorInput = (AnalysisItemEditorInput) editorInput;
            return fileEditorInput.getConnectionNode();
        } else {
            // ADD TDQ-9613 msjian: when the user do something from the other views for example: from task view
            FileEditorInput fileEditorInput = (FileEditorInput) editorInput;
            Analysis findAnalysis = AnaResourceFileHelper.getInstance().findAnalysis(fileEditorInput.getFile());
            DataManager connection = findAnalysis.getContext().getConnection();
            if (connection != null) {
                return RepositoryNodeHelper.recursiveFind(connection);
            }
            // TDQ-9613~
        }
        return null;
    }

    public TableCombo getConnCombo() {
        return connCombo;
    }

    public Analysis getAnalysis() {
        return analysisItem.getAnalysis();
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        ReturnCode rc = canSave();
        if (!rc.isOk()) {
            // MOD yyi 2012-02-29 TDQ-3605 Pop an error if rc is not ok.
            MessageDialogWithToggle.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                    DefaultMessagesImpl.getString("AbstractAnalysisMetadataPage.SaveAnalysis"), rc.getMessage()); //$NON-NLS-1$
            return;
        } else if (!checkWhithspace()) {
            MessageDialogWithToggle
                    .openError(
                            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                            DefaultMessagesImpl.getString("AbstractAnalysisMetadataPage.SaveAnalysis"), DefaultMessagesImpl.getString("AbstractMetadataFormPage.whitespace")); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            super.doSave(monitor);
            try {
                // SaveContext
                saveContext();

                saveAnalysis();
                this.isDirty = false;
                // MOD qiongli bug 0012766,2010-5-31:After change to another connection
                // which has same columns with before,the editor should not
                // dirty.
                ((AnalysisEditor) this.getEditor()).firePropertyChange(IEditorPart.PROP_DIRTY);
                this.updateAnalysisConnectionVersionInfo();
            } catch (DataprofilerCoreException e) {
                MessageDialogWithToggle.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                        DefaultMessagesImpl.getString("AbstractAnalysisMetadataPage.SaveAnalysis"), e.getMessage()); //$NON-NLS-1$ 
                ExceptionHandler.process(e, Level.ERROR);
            }
        }
    }

    public ScrolledForm getScrolledForm() {
        return null;
    }

    protected abstract ReturnCode canRun();

    public abstract void refresh();

    protected abstract void saveAnalysis() throws DataprofilerCoreException;

    @Override
    public void setDirty(boolean isDirty) {
        if (this.isDirty != isDirty) {
            this.isDirty = isDirty;
            ((AnalysisEditor) this.getEditor()).firePropertyChange(IEditorPart.PROP_DIRTY);
        }
    }

    public void fireRuningItemChanged(boolean status) {
        ((AnalysisEditor) currentEditor).setRunActionButtonState(status);
        ((AnalysisEditor) currentEditor).setRefreshResultPage(status);
        if (status) {
            refresh();
        }
    }

    /**
     * DOC bZhou Comment method "switchToResultPage".
     */
    protected void switchToResultPage() {
        IFormPage resultPage = currentEditor.findPage(AnalysisEditor.RESULT_PAGE);
        if (resultPage != null && !resultPage.isActive()) {
            IFormPage activePageInstance = currentEditor.getActivePageInstance();
            if (activePageInstance.canLeaveThePage()) {
                currentEditor.setActivePage(AnalysisEditor.RESULT_PAGE);
            }
        }
    }

    /**
     * MOD mzhao 2009-06-17 feature 5887.
     * 
     * @param parentComp
     */
    public void createConnBindWidget(Composite parentComp) {
        // ~ MOD mzhao 2009-05-05,Bug 6587.
        Composite labelButtonClient = toolkit.createComposite(parentComp, SWT.NONE);
        GridLayout labelButtonClientLayout = new GridLayout();
        labelButtonClientLayout.numColumns = 4;
        labelButtonClient.setLayout(labelButtonClientLayout);

        toolkit.createLabel(labelButtonClient, DefaultMessagesImpl.getString("AbstractMetadataFormPage.connBind")); //$NON-NLS-1$

        // MOD yyin 201204 TDQ-4977, change to TableCombo type to show the connection type.
        // create TableCombo
        connCombo = new TableCombo(labelButtonClient, SWT.BORDER | SWT.READ_ONLY);
        connCombo.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT));

        // tell the TableCombo that I want 2 blank columns auto sized.
        connCombo.defineColumns(2);

        // set which column will be used for the selected item.
        connCombo.setDisplayColumnIndex(0);
        connCombo.setSize(SWT.DEFAULT, SWT.DEFAULT);

        // add listener
        // connCombo = new TableCombo(labelButtonClient, SWT.BORDER);
        connCombo.setEditable(false);
        connCombo.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                updateAnalysisConnectionVersionInfo();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
        // ADD msjian TDQ-5184 2012-4-8: set the connCombo background color as system set color
        connCombo.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        // TDQ-5184~
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).applyTo(labelButtonClient);

        // register: refresh the dataprovider combobox when the name of the data provider is changed.
        refreshDataProvider = new EventReceiver() {

            @Override
            public boolean handle(Object data) {
                reloadDataproviderAndFillConnCombo();
                // TDQ-9345,avoid to get an old column RepositoryNode when click "selecet columns..."
                updateAnalysisTree();
                return true;
            }
        };
        EventManager.getInstance().register(getAnalysis(), EventEnum.DQ_ANALYSIS_REFRESH_DATAPROVIDER_LIST, refreshDataProvider);

        connCombo.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                EventManager.getInstance().unRegister(getAnalysis(), EventEnum.DQ_ANALYSIS_REFRESH_DATAPROVIDER_LIST,
                        refreshDataProvider);

            }
        });

        reloadDataproviderAndFillConnCombo();
        // ~
        createConnVersionText(labelButtonClient);
        createConnDeletedLabel(labelButtonClient);
    }

    /**
     * create the version Text of the connection.
     * 
     * @param parentComp
     */
    private void createConnVersionText(Composite parentComp) {
        textConnVersion = toolkit.createText(parentComp, PluginConstant.EMPTY_STRING, SWT.FLAT);
        textConnVersion.setEditable(false);
        updateAnalysisConnectionVersionInfo();
    }

    /**
     * update the version info of the analysis.
     */
    public void updateAnalysisConnectionVersionInfo() {
        if (this.textConnVersion != null) {
            String strConnVersion = DefaultMessagesImpl.getString("AbstractMetadataFormPage.connVersion") //$NON-NLS-1$
                    + getConnectionVersion();
            textConnVersion.setText(strConnVersion);
        }
    }

    /**
     * get the database's version of the Analysis.
     * 
     * @return
     */
    public String getConnectionVersion() {
        String version = null;
        if (this.analysisItem.getAnalysis() != null) {
            DataManager dm = this.analysisItem.getAnalysis().getContext().getConnection();
            if (dm != null) {
                if (dm instanceof Connection) {
                    Connection connection = (Connection) dm;
                    version = connection.getVersion();
                    if (version == null) {
                        version = initConnectionVersion(connection);
                    }
                }
            }
        }
        return version == null ? getConnectionVersionDefault() : version;
    }

    /**
     * get the default connection's version of this analysis.
     * 
     * @returnd efault connection's version
     */
    private String getConnectionVersionDefault() {
        String version = "Unknown"; //$NON-NLS-1$
        Object data = connCombo.getData(connCombo.getSelectionIndex() + PluginConstant.EMPTY_STRING);
        if (data != null) {
            if (data instanceof DBConnectionRepNode) {
                DBConnectionRepNode dbConnRepNode = (DBConnectionRepNode) data;
                if (dbConnRepNode.getObject() != null && dbConnRepNode.getObject().getProperty() != null) {
                    version = dbConnRepNode.getObject().getProperty().getVersion();
                }
            } else if (data instanceof DFConnectionRepNode) {
                DFConnectionRepNode dfConnRepNode = (DFConnectionRepNode) data;
                if (dfConnRepNode.getObject() != null && dfConnRepNode.getObject().getProperty() != null) {
                    version = dfConnRepNode.getObject().getProperty().getVersion();
                }
            }
        }
        return version;
    }

    /**
     * init the version of the Connection accroding to the file name.
     * 
     * @param connection
     * @return
     */
    private String initConnectionVersion(Connection connection) {
        String version = "0.1"; //$NON-NLS-1$
        Resource eResource = connection.eResource();
        if (eResource != null) {
            URI uri = eResource.getURI();
            if (uri != null) {
                String fileName = uri.toString().toLowerCase();
                String[] splits = fileName.split("_"); //$NON-NLS-1$
                if (splits.length > 0) {
                    String str = splits[splits.length - 1];
                    int indexOf = str.indexOf("." + FactoriesUtil.ITEM_EXTENSION); //$NON-NLS-1$
                    version = str.substring(0, indexOf);
                }
            }
        }
        return version;
    }

    /**
     * 
     * This method will make connection elem become proxy, look out for use it.
     */
    public void reloadDataproviderAndFillConnCombo() {
        List<IRepositoryNode> connsWithoutDeletion = RepositoryNodeHelper.getConnectionRepositoryNodes(false);

        if (connsWithoutDeletion.size() == 0 && !RepositoryNodeHelper.isOpenDQCommonViewer()) {
            return;
        }

        connCombo.getTable().removeAll();
        fillComb(connsWithoutDeletion);

        DataManager connection = this.analysisItem.getAnalysis().getContext().getConnection();
        if (connection == null) {
            connCombo.select(0);
        } else {
            // Find the conn index first
            int connIdx = findPositionOfCurrentConnection(connsWithoutDeletion, connection);
            if (connIdx == -1) {
                IRepositoryNode currentConnectionNode = getCurrentRepNodeOnUI();
                // The current connection is logical deleted!
                int deleteIndex = connCombo.getItemCount();
                if (currentConnectionNode != null) {
                    addItemToCombo(currentConnectionNode, deleteIndex);
                }
                connCombo.select(deleteIndex);
            } else {
                connCombo.select(connIdx);
            }
        }

    }

    /**
     * findthe Position Of Current Connection.
     * 
     * @param connsWithoutDeletion
     * @param connection
     * @return
     */
    private int findPositionOfCurrentConnection(List<IRepositoryNode> connsWithoutDeletion, DataManager connection) {
        int index = 0;
        for (IRepositoryNode repNode : connsWithoutDeletion) {
            if (StringUtils.equals(repNode.getObject().getLabel(), connection.getName())) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
     * add a connection node into the combobox.
     * 
     * @param repNode
     * @param index
     */
    protected void addItemToCombo(IRepositoryNode repNode, int index) {
        String connectionType = RepositoryNodeHelper.getConnectionType(repNode);

        TableItem ti = new TableItem(connCombo.getTable(), SWT.NONE);
        String displayName = repNode.getObject().getProperty().getDisplayName();
        ti.setText(new String[] { displayName, connectionType });

        connCombo.setData(displayName + connectionType, index);
        connCombo.setData(index + PluginConstant.EMPTY_STRING, repNode);
    }

    /**
     * fill the combobox of connections
     * 
     * @param connsWithoutDeletion
     */
    private void fillComb(List<IRepositoryNode> connsWithoutDeletion) {
        int index = 0;
        for (IRepositoryNode repNode : connsWithoutDeletion) {
            addItemToCombo(repNode, index);
            index++;
        }
    }

    /**
     * check if the connection repNode is supported.
     * 
     * @param repNode
     * @return boolean true:support
     */
    protected boolean isConnectionSupport(IRepositoryNode repNode) {
        return true;
    }

    /**
     * ADD gdbu 2011-6-1 bug : 19833
     * 
     * DOC gdbu Comment method "updateDQRuleDependency".
     * 
     * @param dqRules
     */
    protected void updateDQRuleDependency(List<DQRule> dqRules) {
        for (DQRule dqRule : dqRules) {
            List<Dependency> realSupplierDependency = new ArrayList<Dependency>();
            EList<Dependency> supplierDependency = dqRule.getSupplierDependency();
            for (Dependency dependency : supplierDependency) {
                EList<ModelElement> client = dependency.getClient();
                for (ModelElement modelElement : client) {
                    if (modelElement instanceof Analysis) {
                        List<DQRule> dqRules2 = getDqRules((Analysis) modelElement);
                        if (dqRules2.contains(dqRule)) {
                            realSupplierDependency.add(dependency);
                        }
                    }
                }
            }
            supplierDependency.clear();
            supplierDependency.addAll(realSupplierDependency);
        }
    }

    /**
     * ADD gdbu 2011-6-1 bug : 19833
     * 
     * DOC gdbu Comment method "getDqRules". Get all DQRule from analysis.
     * 
     * @param analysis
     * @return
     */
    public List<DQRule> getDqRules(Analysis analysis) {
        List<DQRule> result = new ArrayList<DQRule>();
        EList<Indicator> indicators = analysis.getResults().getIndicators();
        for (Indicator indicator : indicators) {
            IndicatorDefinition indicatorDefinition = indicator.getIndicatorDefinition();
            if (indicatorDefinition instanceof DQRule) {
                result.add((DQRule) indicatorDefinition);
            }
        }
        return result;
    }

    /**
     * DOC bZhou Comment method "getTreeViewer".
     * 
     * @return
     */
    public AbstractColumnDropTree getTreeViewer() {
        return null;
    }

    /**
     * create a label to indicate this connection is logical deleted.
     * 
     * @param parentComp
     */
    private void createConnDeletedLabel(Composite parentComp) {
        this.labelConnDeleted = toolkit.createLabel(parentComp, PluginConstant.EMPTY_STRING, SWT.NONE);
        labelConnDeleted.setVisible(false);
        labelConnDeleted.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
    }

    public Label getLabelConnDeleted() {
        return this.labelConnDeleted;
    }

    public ExecutionLanguage getUIExecuteEngin() {
        return ExecutionLanguage.SQL;
    }

    /**
     * Log on debug enable.
     * 
     * @param logger
     * @param level
     * @param message
     */
    protected void doLog(Logger logger, Level level, String message) {
        logger.log(level, message);
    }

    /**
     * log when analysis saved
     * 
     * @param saved
     * @throws DataprofilerCoreException
     */
    protected void logSaved(ReturnCode saved) throws DataprofilerCoreException {
        Analysis analysis = analysisItem.getAnalysis();
        String urlString = analysis.eResource() != null ? (analysis.eResource().getURI().isFile() ? analysis.eResource().getURI()
                .toFileString() : analysis.eResource().getURI().toString()) : PluginConstant.EMPTY_STRING;
        if (!saved.isOk()) {
            throw new DataprofilerCoreException(DefaultMessagesImpl.getString(
                    "ColumnMasterDetailsPage.problem", analysis.getName(), urlString, saved.getMessage())); //$NON-NLS-1$

        } else if (log.isDebugEnabled()) {
            // MOD yyi 2012-02-06 TDQ-4581:avoid the instantiation of the strings to optimize the performances.
            doLog(log, Level.INFO, DefaultMessagesImpl.getString("ColumnMasterDetailsPage.success", urlString)); //$NON-NLS-1$
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.ui.editor.AbstractMetadataFormPage#canSave()
     */
    @Override
    public ReturnCode canSave() {
        return canModifyName(ERepositoryObjectType.TDQ_ANALYSIS_ELEMENT);
    }

    /**
     * 
     * 
     * @param TDQAnalysisItem
     * @return whether it has been deleted
     * 
     * delete the dependency between analysis and connection
     */
    protected boolean deleteConnectionDependency(TDQAnalysisItem anaItem) {
        return DependenciesHandler.getInstance().removeConnDependencyAndSave(anaItem);
    }

    /**
     * DOC xqliu Comment method "createAnalysisLimitSection".
     * 
     * @param sForm
     * @param pComp
     * @return
     * @deprecated use createAnalysisLimitComposite(Composite pComp) instead
     */
    @Deprecated
    protected Section createAnalysisLimitSection(final ScrolledForm sForm, Composite pComp) {
        Section section = createSection(sForm, pComp,
                DefaultMessagesImpl.getString("AbstractMetadataFormPage.AnalysisLimit"), null); //$NON-NLS-1$
        Composite parent = this.toolkit.createComposite(section);
        this.createAnalysisLimitComposite(parent);
        section.setClient(parent);
        return section;
    }

    /**
     * DOC xqliu Comment method "createAnalysisLimitComposite".
     * 
     * @param pComp
     * @return
     */
    protected Composite createAnalysisLimitComposite(Composite pComp) {
        Composite comp = pComp;
        comp.setLayout(new GridLayout(2, false));
        this.toolkit.createLabel(comp,
                DefaultMessagesImpl.getString("AnalysisTuningPreferencePage.NumberOfConnectionsPerAnalysis")); //$NON-NLS-1$

        this.numberOfConnectionsPerAnalysisText = this.toolkit.createText(comp, AnalysisHandler.createHandler(getAnalysis())
                .getNumberOfConnectionsPerAnalysisWithContext(), SWT.BORDER);
        GridDataFactory.fillDefaults().grab(false, true).applyTo(this.numberOfConnectionsPerAnalysisText);
        ((GridData) this.numberOfConnectionsPerAnalysisText.getLayoutData()).widthHint = 240;
        installProposals(numberOfConnectionsPerAnalysisText);

        this.numberOfConnectionsPerAnalysisText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                setDirty(true);
            }

        });

        this.numberOfConnectionsPerAnalysisText.addVerifyListener(new VerifyListener() {

            public void verifyText(VerifyEvent e) {
                String inputValue = e.text;
                // if it is context varible, do not check
                if (!ContextHelper.isContextVar(inputValue)) {
                    Pattern pattern = Pattern.compile("^[0-9]"); //$NON-NLS-1$
                    char[] charArray = inputValue.toCharArray();
                    for (char c : charArray) {
                        if (!pattern.matcher(String.valueOf(c)).matches()) {
                            e.doit = false;
                        }
                    }
                }
            }
        });

        return comp;
    }

    /**
     * DOC xqliu Comment method "saveNumberOfConnectionsPerAnalysis".
     * 
     * @throws DataprofilerCoreException
     */
    protected void saveNumberOfConnectionsPerAnalysis() throws DataprofilerCoreException {
        // check whether the field is Blank
        if (StringUtils.isBlank(numberOfConnectionsPerAnalysisText.getText().trim())) {
            throw new DataprofilerCoreException(DefaultMessagesImpl.getString("ColumnMasterDetailsPage.emptyField", //$NON-NLS-1$
                    DefaultMessagesImpl.getString("AnalysisTuningPreferencePage.NumberOfConnectionsPerAnalysis"))); //$NON-NLS-1$

        }
        TaggedValueHelper.setTaggedValue(this.getAnalysis(), TdqAnalysisConnectionPool.NUMBER_OF_CONNECTIONS_PER_ANALYSIS,
                this.numberOfConnectionsPerAnalysisText.getText());
    }

    /**
     * set ScrolledForm.
     * 
     * @param form
     */
    public void setForm(ScrolledForm form) {
        super.form = form;
    }

    /**
     * get ScrolledForm.
     * 
     * @return
     */
    public ScrolledForm getForm() {
        return form;
    }

    /**
     * get ChartComposite.
     * 
     * @return
     */
    public Composite getChartComposite() {
        return null;
    }

    /**
     * create Analysis Param Section.
     * 
     * @param pForm
     * @param pComp
     */
    protected void createAnalysisParamSection(final ScrolledForm pForm, Composite pComp) {
        analysisParamSection = createSection(pForm, pComp,
                DefaultMessagesImpl.getString("ColumnMasterDetailsPage.AnalysisParameter"), null); //$NON-NLS-1$
        Composite sectionClient = toolkit.createComposite(analysisParamSection);
        createAnalysisLimitComposite(sectionClient);
        analysisParamSection.setClient(sectionClient);
    }

    /**
     * Extracted from the column and column set master page, to create the execution language selection section
     * 
     * @param form1
     * @param anasisDataComp
     * @param analyzedColumns
     * @param anaParameters
     * @return
     */
    protected Composite createExecuteEngineSection(final ScrolledForm form1, Composite anasisDataComp,
            EList<ModelElement> analyzedColumns, AnalysisParameters anaParameters) {
        analysisParamSection = createSection(form1, anasisDataComp,
                DefaultMessagesImpl.getString("ColumnMasterDetailsPage.AnalysisParameter"), null); //$NON-NLS-1$
        Composite sectionClient = toolkit.createComposite(analysisParamSection);
        sectionClient.setLayout(new GridLayout(1, false));

        Composite comp1 = new Composite(sectionClient, SWT.NONE);
        this.createAnalysisLimitComposite(comp1);

        Composite comp2 = new Composite(sectionClient, SWT.NONE);
        comp2.setLayout(new GridLayout(2, false));
        GridDataFactory.fillDefaults().grab(true, true).applyTo(comp2);
        toolkit.createLabel(comp2, DefaultMessagesImpl.getString("ColumnMasterDetailsPage.ExecutionEngine")); //$NON-NLS-1$
        // MOD zshen:need to use the component with finish indicator Selection.
        execCombo = new CCombo(comp2, SWT.BORDER);
        // ~
        execCombo.setEditable(false);

        for (ExecutionLanguage language : ExecutionLanguage.VALUES) {
            String temp = language.getLiteral();
            execCombo.add(temp);
        }
        // MOD qiongli 2011-3-17 set DataFilterText disabled except TdColumn.
        if (analyzedColumns != null && !analyzedColumns.isEmpty()) {
            ModelElement mod = analyzedColumns.get(0);
            TdColumn tdColumn = SwitchHelpers.COLUMN_SWITCH.doSwitch(mod);
            dataFilterComp.getDataFilterText().setEnabled((tdColumn != null) ? true : false);
            if (tdColumn == null) {
                dataFilterComp.getDataFilterText().setEnabled(false);
                changeExecuteLanguageToJava(true);
            }
        }

        ExecutionLanguage executionLanguage = analysisItem.getAnalysis().getParameters().getExecutionLanguage();

        execLang = executionLanguage.getLiteral();
        execCombo.setText(execLang);
        // ADD xqliu 2009-08-24 bug 8776
        setLanguageToTreeViewer(ExecutionLanguage.get(execLang));
        // ~

        // MOD msjian TDQ-9467: this part is only for column analysis
        createDrillDownPart(anaParameters, comp2, executionLanguage);

        analysisParamSection.setClient(sectionClient);

        return comp2;
    }

    /**
     * DOC msjian Comment method "createDrillDownPart".
     * 
     * @param anaParameters
     * @param comp2
     * @param executionLanguage
     */
    protected void createDrillDownPart(AnalysisParameters anaParameters, Composite comp2, ExecutionLanguage executionLanguage) {
        // do nothing here, only ColumnMasterDetailsPage need to overwrite this
    }

    /**
     * change ExecutionLanuage to Java.
     */
    public void changeExecuteLanguageToJava(boolean isDisabled) {
        if (this.execCombo == null) {
            return;
        }
        if (!(ExecutionLanguage.JAVA.getLiteral().equals(this.execLang))) {
            int i = 0;
            for (ExecutionLanguage language : ExecutionLanguage.VALUES) {
                if (language.compareTo(ExecutionLanguage.JAVA) == 0) {
                    this.execCombo.select(i);
                } else {
                    i++;
                }
            }
        }
        if (isDisabled) {
            execCombo.setEnabled(false);
        }
    }

    public void enableExecuteLanguage() {
        execCombo.setEnabled(true);
    }

    protected boolean includeDatePatternFreqIndicator() {
        // only needed in column and column set master page
        return false;
    }

    /**
     * set the execute Language To the related TreeViewer in the child class.
     */
    protected void setLanguageToTreeViewer(ExecutionLanguage executionLanguage) {
        // no need to implement there
    }

    // ADD yyin 20131204 TDQ-8413, get the current selected value to judge, no need to use the related parameter in the
    // analysis.
    public String getCurrentExecuteLanguage() {
        return this.execLang;
    }

    public TDQAnalysisItem getAnalysisItem() {
        return this.analysisItem;
    }

    /**
     * create the datafilter section.
     * 
     * @param form1
     * @param anasisDataComp
     * @param needFillBoth: if true, will fill both the section.
     */
    void createDataFilterSection(final ScrolledForm form1, Composite anasisDataComp, boolean needFillBoth) {
        dataFilterSection = createSection(
                form1,
                anasisDataComp,
                DefaultMessagesImpl.getString("ColumnMasterDetailsPage.dataFilter"), DefaultMessagesImpl.getString("ColumnMasterDetailsPage.editDataFilter")); //$NON-NLS-1$ //$NON-NLS-2$

        Composite sectionClient = toolkit.createComposite(dataFilterSection);

        if (needFillBoth) {
            // the text will fill both the section. can see ColumnDependencyMasterDetailsPage
            sectionClient.setLayoutData(new GridData(GridData.FILL_BOTH));
            sectionClient.setLayout(new GridLayout());
        }

        dataFilterComp = new DataFilterComp(sectionClient, stringDataFilter);
        installProposals(dataFilterComp.getDataFilterText());
        // dataFilterComp.addPropertyChangeListener(this);
        addWhitespaceValidate(dataFilterComp.getDataFilterText());
        dataFilterSection.setClient(sectionClient);
    }

    /**
     * create the datafilter section without fill both the section.
     * 
     * @param form1
     * @param anasisDataComp
     */
    void createDataFilterSection(final ScrolledForm form1, Composite anasisDataComp) {
        createDataFilterSection(form1, anasisDataComp, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.ui.editor.AbstractMetadataFormPage#saveContext()
     */
    @Override
    protected void saveContext() {
        // save contexts
        Analysis analysis = getAnalysis();
        analysis.getContextType().clear();
        IContextManager contextManager = currentEditor.getContextManager();
        contextManager.saveToEmf(analysis.getContextType());
        analysis.setDefaultContext(getDefaultContextGroupName((SupportContextEditor) currentEditor));
        AnalysisHelper.setLastRunContext(currentEditor.getLastRunContextGroupName(), analysis);
    }

    /**
     * 
     * when rename the related connection ,it will reload connection combo,also need to update TreeViewer,so that avoid
     * some old column RepositoryNode instance .if it is not dirty before updating,should keep the not dirty satus.
     */
    protected void updateAnalysisTree() {
        AbstractColumnDropTree treeViewer = getTreeViewer();
        if (treeViewer != null) {
            boolean beforeUpdateDirty = treeViewer.isDirty();
            treeViewer.updateModelViewer();
            if (!beforeUpdateDirty) {
                treeViewer.setDirty(false);
            }
        }
    }

}
