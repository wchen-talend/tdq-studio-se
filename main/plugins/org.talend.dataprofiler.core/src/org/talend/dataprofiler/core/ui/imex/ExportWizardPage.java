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
package org.talend.dataprofiler.core.ui.imex;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.talend.commons.emf.FactoriesUtil;
import org.talend.commons.utils.WorkspaceUtils;
import org.talend.cwm.helper.ResourceHelper;
import org.talend.dataprofiler.core.PluginConstant;
import org.talend.dataprofiler.core.migration.helper.IndicatorDefinitionFileHelper;
import org.talend.dataprofiler.core.ui.imex.model.EImexType;
import org.talend.dataprofiler.core.ui.imex.model.ExportWriterFactory;
import org.talend.dataprofiler.core.ui.imex.model.IExportWriter;
import org.talend.dataprofiler.core.ui.imex.model.ItemRecord;
import org.talend.dataprofiler.core.ui.utils.ImportAndExportUtils;
import org.talend.dataquality.indicators.definition.IndicatorDefinition;
import org.talend.dq.helper.EObjectHelper;
import org.talend.dq.helper.PropertyHelper;
import org.talend.dq.helper.ReportUtils;
import org.talend.dq.helper.resourcehelper.IndicatorResourceFileHelper;
import org.talend.dq.indicators.definitions.DefinitionHandler;
import org.talend.resource.ResourceManager;
import orgomg.cwm.objectmodel.core.ModelElement;

/**
 * This class defines the UI for the export feature in TOP and TQ for the Data profile perspective.
 */
public class ExportWizardPage extends WizardPage {

    private static final String SHOW_ONLY_SELECTED = Messages.getString("ExportWizardPage.9"); //$NON-NLS-1$

    private static final String SELECT_ARCHIVE_FILE = Messages.getString("ExportWizardPage.8"); //$NON-NLS-1$

    private static final String BROWSE = Messages.getString("ExportWizardPage.7"); //$NON-NLS-1$

    private static final String SELECT_ROOT_DIR = Messages.getString("ExportWizardPage.6"); //$NON-NLS-1$

    private static final String INCLUDE_DEPENDANCY = Messages.getString("ExportWizardPage.12"); //$NON-NLS-1$

    private static final String DESELECT_ALL = Messages.getString("ExportWizardPage.11"); //$NON-NLS-1$

    private static final String SELECT_ALL = Messages.getString("ExportWizardPage.10"); //$NON-NLS-1$

    private static Logger log = Logger.getLogger(ExportWizardPage.class);

    private CheckboxTreeViewer repositoryTree;

    protected Button dirBTN, archBTN;

    private Button browseDirBTN, browseArchBTN;

    private Text dirTxt, archTxt;

    protected Button selectAllBTN, deselectAllBTN, addRequireBTN;

    private IPath specifiedPath;

    private List<String> errors;

    protected IExportWriter writer;

    public static final String[] FILE_EXPORT_MASK = { "*.zip;*.tar;*.tar.gz", "*.*" }; //$NON-NLS-1$//$NON-NLS-2$

    private final ViewerFilter treeFilter = new TreeFilter();

    private final String underlineStr = "_";//$NON-NLS-1$

    private final String subrepName = "subreports";//$NON-NLS-1$

    public ExportWizardPage(String specifiedPath) {
        super(Messages.getString("ExportWizardPage.2")); //$NON-NLS-1$
        setMessage(Messages.getString("ExportWizardPage.3")); //$NON-NLS-1$
        this.specifiedPath = specifiedPath == null ? null : new Path(specifiedPath);
        this.writer = ExportWriterFactory.create(EImexType.FILE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        Composite top = new Composite(parent, SWT.NONE);
        top.setLayout(new GridLayout());
        top.setLayoutData(new GridData(GridData.FILL_BOTH));

        createSelectComposite(top);

        createRepositoryTree(top);

        addListeners();

        initControlState();

        setControl(top);
    }

    /**
     * DOC bZhou Comment method "initControlState".
     */
    protected void initControlState() {
        setArchState(false);
        setPageComplete(false);
    }

    /**
     * DOC bZhou Comment method "setDirState".
     * 
     * @param state
     */
    protected void setDirState(boolean state) {
        dirTxt.setEnabled(state);
        browseDirBTN.setEnabled(state);

        if (state) {
            writer = ExportWriterFactory.create(EImexType.FILE);
            updateBasePath();
        }
    }

    /**
     * DOC zshen Comment method "updateBasePath".
     */
    public String updateBasePath() {
        String basePath;
        if (isDirState()) {
            basePath = getTextContent(dirTxt);
        } else {
            basePath = getTextContent(archTxt);
        }
        textModified(basePath);
        return basePath;
    }

    /**
     * DOC zshen Comment method "getTextContent".
     * 
     * @param archTxt2
     * @return
     */
    protected String getTextContent(Text archTxt2) {
        if (archTxt2 == null) {
            return PluginConstant.EMPTY_STRING;
        }
        return archTxt2.getText();
    }

    /**
     * DOC bZhou Comment method "isDirState".
     * 
     * @return
     */
    public boolean isDirState() {
        return dirBTN.getSelection();
    }

    /**
     * DOC bZhou Comment method "setArchState".
     * 
     * @param state
     */
    protected void setArchState(boolean state) {
        archTxt.setEnabled(state);
        browseArchBTN.setEnabled(state);

        if (state) {
            writer = ExportWriterFactory.create(EImexType.ZIP_FILE);
            updateBasePath();
        }
    }

    /**
     * DOC bZhou Comment method "isArchState".
     * 
     * @return
     */
    public boolean isArchState() {
        return archBTN.getSelection();
    }

    /**
     * DOC bZhou Comment method "addListeners".
     */
    private void addListeners() {

        SelectionListener modeSwitchListener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setDirState(isDirState());
                setArchState(!isDirState());
            }
        };

        dirBTN.addSelectionListener(modeSwitchListener);

        archBTN.addSelectionListener(modeSwitchListener);

        ModifyListener populateListener = new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                updateBasePath();
            }
        };

        dirTxt.addModifyListener(populateListener);

        archTxt.addModifyListener(populateListener);

        browseDirBTN.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String result = openDirectoryDialog();
                if (result != null) {
                    dirTxt.setText(result);
                }
            }

            private String openDirectoryDialog() {
                DirectoryDialog dialog = new DirectoryDialog(Display.getDefault().getActiveShell());
                if (dirTxt.getText() != null) {
                    dialog.setFilterPath(dirTxt.getText());
                }

                return dialog.open();
            }
        });

        browseArchBTN.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String result = openFileDialog();
                if (result != null) {
                    IPath path = new Path(result);
                    if (StringUtils.isBlank(path.getFileExtension())) {
                        path = path.addFileExtension("zip");//$NON-NLS-1$ 
                    }

                    archTxt.setText(path.toOSString());
                }
            }

            private String openFileDialog() {
                FileDialog dialog = new FileDialog(Display.getDefault().getActiveShell());
                dialog.setFilterExtensions(FILE_EXPORT_MASK);
                if (archTxt.getText() != null) {
                    dialog.setFileName(archTxt.getText());
                }

                return dialog.open();
            }
        });

        repositoryTree.getTree().addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                TreeItem item = (TreeItem) e.item;
                ItemRecord record = (ItemRecord) item.getData();

                if (record.getFile().isFile()) {
                    // MOD qiongli TDQ-5368 only uncheck the client dependecy when uncheck an item,not supplier
                    // dependency.
                    boolean checked = item.getChecked();
                    if (checked) {
                        // for (File file : record.getDependencyMap().keySet()) {
                        for (File file : record.getDependencySet()) {
                            ItemRecord findRecord = ItemRecord.findRecord(file);
                            if (findRecord != null) {
                                repositoryTree.setChecked(findRecord, checked);
                            } else {
                                log.error("Can't find the file: " + file.getAbsolutePath());//$NON-NLS-1$ 
                            }
                        }
                    } else {
                        ModelElement element = record.getElement();
                        if (element != null) {
                            List<ModelElement> dependencyClients = EObjectHelper.getDependencyClients(element);
                            ImportAndExportUtils.iterateUncheckClientDependency(dependencyClients, repositoryTree);
                        }

                    }

                    repositoryTree.refresh();

                    checkForErrors();
                }
            }
        });

    }

    /**
     * DOC bZhou Comment method "textModified".
     * 
     * @param pathStr
     */
    public void textModified(String pathStr) {

        writer.setBasePath(new Path(pathStr));
        checkForErrors();

    }

    /**
     * this check that the folder entered in the target export location exist otherwhise set an erro message and disable
     * export.
     */
    protected void checkForErrors() {
        errors = new ArrayList<String>();

        errors.addAll(writer.check());

        ItemRecord[] elements = getElements();
        for (ItemRecord record : elements) {
            // Map<File, ModelElement> dependencyMap = record.getDependencyMap();
            for (File depFile : record.getDependencySet()) {
                if (!repositoryTree.getChecked(ItemRecord.findRecord(depFile))) {
                    ModelElement element = ItemRecord.getElement(depFile);
                    // MOD qiongli 2012-12-13 TDQ-5356 use itself file name for jrxml
                    boolean isJrxmlDepFile = depFile.getName().endsWith(FactoriesUtil.JRXML);
                    // MOD msjian TDQ-5909: modify to displayName
                    String fileName = element != null && !isJrxmlDepFile ? PropertyHelper.getProperty(element).getDisplayName()
                            : depFile.getName();
                    // TDQ-5909~
                    // if the dependency Indicator is Technial Indicator, don't add it into errors even if it is not
                    // exist in the export wizard
                    if (element instanceof IndicatorDefinition) {
                        String uuid = ResourceHelper.getUUID(element);
                        if (IndicatorDefinitionFileHelper.isTechnialIndicator(uuid)) {
                            continue;
                        }
                    }
                    errors.add("\"" + record.getName() + "\" miss dependency : " + fileName);//$NON-NLS-1$ //$NON-NLS-2$ 
                }
            }
        }

        if (!errors.isEmpty()) {
            setErrorMessage(errors.get(0));
        } else {
            setErrorMessage(null);
        }

        updatePageStatus();
    }

    /**
     * update the page state that is the finish button enable state according to the error message being present or not
     * and user select at least one item to export.
     */
    protected void updatePageStatus() {
        setPageComplete(getErrorMessage() == null && getElements().length > 0);
    }

    /**
     * DOC bZhou Comment method "createRepositoryTree".
     * 
     * @param top
     */
    protected void createRepositoryTree(Composite top) {
        Composite treeComposite = new Composite(top, SWT.NONE);
        treeComposite.setLayout(new GridLayout(2, false));
        treeComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        repositoryTree = new ContainerCheckedTreeViewer(treeComposite);
        repositoryTree.setContentProvider(new FileTreeContentProvider());
        repositoryTree.setLabelProvider(new FileTreeLabelProvider());
        repositoryTree.setInput(writer.computeInput(specifiedPath));
        repositoryTree.expandAll();
        repositoryTree.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));

        createUtilityButtons(treeComposite);
    }

    /**
     * DOC bZhou Comment method "createUtilityButtons".
     * 
     * @param treeComposite
     */
    protected void createUtilityButtons(Composite treeComposite) {
        Composite utilityComposite = new Composite(treeComposite, SWT.NONE);
        utilityComposite.setLayout(new GridLayout());
        utilityComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));

        Composite btnsComposite = new Composite(utilityComposite, SWT.NONE);
        btnsComposite.setLayout(new GridLayout());

        GridData gd = new GridData();
        gd.verticalAlignment = SWT.BEGINNING;
        gd.horizontalIndent = 0;
        gd.horizontalSpan = 0;
        btnsComposite.setLayoutData(gd);

        selectAllBTN = new Button(btnsComposite, SWT.PUSH);
        selectAllBTN.setText(SELECT_ALL);
        selectAllBTN.setToolTipText(SELECT_ALL);

        deselectAllBTN = new Button(btnsComposite, SWT.PUSH);
        deselectAllBTN.setText(DESELECT_ALL);
        deselectAllBTN.setToolTipText(DESELECT_ALL);

        addRequireBTN = new Button(btnsComposite, SWT.PUSH);
        addRequireBTN.setText(INCLUDE_DEPENDANCY);
        addRequireBTN.setToolTipText(INCLUDE_DEPENDANCY);

        Composite infoComposite = new Composite(utilityComposite, SWT.NONE);
        infoComposite.setLayout(new GridLayout());

        gd = new GridData(GridData.FILL_BOTH);
        gd.verticalAlignment = SWT.BOTTOM;
        gd.horizontalIndent = 0;
        gd.horizontalSpan = 0;
        infoComposite.setLayoutData(gd);

        final Button showSelectBTN = new Button(infoComposite, SWT.CHECK);
        showSelectBTN.setText(SHOW_ONLY_SELECTED);
        showSelectBTN.setToolTipText(SHOW_ONLY_SELECTED);

        Button[] utilityBTNs = new Button[] { selectAllBTN, deselectAllBTN, addRequireBTN };

        for (Button btn : utilityBTNs) {
            gd = new GridData();
            gd.widthHint = 170;
            gd.verticalIndent = 0;
            gd.horizontalIndent = 0;

            btn.setLayoutData(gd);
        }

        selectAllBTN.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                TreeItem[] topItems = repositoryTree.getTree().getItems();
                for (TreeItem treeItem : topItems) {
                    repositoryTree.setSubtreeChecked(treeItem.getData(), true);
                }
                repositoryTree.refresh();
                updatePageStatus();
            }
        });

        deselectAllBTN.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                TreeItem[] topItems = repositoryTree.getTree().getItems();
                for (TreeItem treeItem : topItems) {
                    repositoryTree.setSubtreeChecked(treeItem.getData(), false);
                }
                repositoryTree.refresh();
                updatePageStatus();
            }
        });

        addRequireBTN.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (showSelectBTN.getSelection()) {
                    filterUncheckedItems(false);
                }

                ItemRecord[] records = getElements();
                for (ItemRecord record : records) {
                    // Map<File, ModelElement> dependencyMap = record.getDependencyMap();
                    for (File depFile : record.getDependencySet()) {
                        ItemRecord depRecord = ItemRecord.findRecord(depFile);
                        if (!repositoryTree.getChecked(depRecord)) {
                            repositoryTree.setChecked(depRecord, true);
                        }
                    }
                }

                if (showSelectBTN.getSelection()) {
                    filterUncheckedItems(true);
                }

                checkForErrors();
            }
        });

        showSelectBTN.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                filterUncheckedItems(showSelectBTN.getSelection());
            }
        });

    }

    private void filterUncheckedItems(boolean isFiltered) {
        if (isFiltered) {
            repositoryTree.addFilter(treeFilter);
        } else {
            ItemRecord[] elements = getElements();
            repositoryTree.removeFilter(treeFilter);
            repositoryTree.expandAll();
            repositoryTree.setCheckedElements(elements);
        }
    }

    /**
     * DOC bZhou Comment method "createSelectComposite".
     * 
     * @param top
     */
    protected void createSelectComposite(Composite top) {
        Composite selectComp = new Composite(top, SWT.NONE);
        selectComp.setLayout(new GridLayout(3, false));
        selectComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        dirBTN = new Button(selectComp, SWT.RADIO);
        dirBTN.setText(SELECT_ROOT_DIR);
        dirBTN.setToolTipText(SELECT_ROOT_DIR);
        setButtonLayoutData(dirBTN);

        dirTxt = new Text(selectComp, SWT.BORDER);
        dirTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        browseDirBTN = new Button(selectComp, SWT.PUSH);
        browseDirBTN.setText(BROWSE);
        browseDirBTN.setToolTipText(BROWSE);
        setButtonLayoutData(browseDirBTN);

        archBTN = new Button(selectComp, SWT.RADIO);
        archBTN.setText(SELECT_ARCHIVE_FILE);
        archBTN.setToolTipText(SELECT_ARCHIVE_FILE);
        setButtonLayoutData(archBTN);

        archTxt = new Text(selectComp, SWT.BORDER);
        archTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        browseArchBTN = new Button(selectComp, SWT.PUSH);
        browseArchBTN.setText(BROWSE);
        browseArchBTN.setToolTipText(BROWSE);
        setButtonLayoutData(browseArchBTN);
    }

    /**
     * DOC bZhou Comment method "getElements".
     * 
     * @return
     */
    public ItemRecord[] getElements() {
        List<ItemRecord> itemRecords = new ArrayList<ItemRecord>();

        boolean sumIndicatorAdded = false;
        Object[] checkedElements = repositoryTree.getCheckedElements();
        for (Object obj : checkedElements) {
            if (obj instanceof ItemRecord) {
                ItemRecord record = (ItemRecord) obj;
                // MOD qiongli 2012-4-20,add related subreport jrxml/jasper file to elements.
                File file = record.getFile();
                if (file.isFile()) {
                    itemRecords.add(record);
                    if (file.getName().endsWith(FactoriesUtil.JRXML)) {
                        addSubRepToElements(record, itemRecords);
                    }
                    // if it is Mean Indicator, add it's dependency Sum Indicator
                    if (!sumIndicatorAdded) {
                        String meanIndicatorUuid = "_ccI48RF2Ed2PKb6nEJEvhw"; //$NON-NLS-1$
                        String sumIndicatorLabel = "Sum"; //$NON-NLS-1$
                        ModelElement element = record.getElement();
                        if (element != null && element instanceof IndicatorDefinition
                                && meanIndicatorUuid.equals(ResourceHelper.getUUID(element))) {
                            IndicatorDefinition sumIndicator = DefinitionHandler.getInstance().getIndicatorDefinition(
                                    sumIndicatorLabel);
                            if (sumIndicator != null) {
                                IFile sumIndicatorIFile = IndicatorResourceFileHelper.findCorrespondingFile(sumIndicator);
                                if (sumIndicatorIFile != null) {
                                    File sumIndicatorFile = WorkspaceUtils.ifileToFile(sumIndicatorIFile);
                                    if (sumIndicatorFile != null) {
                                        itemRecords.add(new ItemRecord(sumIndicatorFile));
                                        sumIndicatorAdded = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return itemRecords.toArray(new ItemRecord[itemRecords.size()]);
    }

    /**
     * Getter for writer.
     * 
     * @return the writer
     */
    public IExportWriter getWriter() {
        return this.writer;
    }

    /**
     * DOC bZhou ExportWizardPage class global comment. Detailled comment
     */
    class TreeFilter extends ViewerFilter {

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object,
         * java.lang.Object)
         */
        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            ItemRecord record = (ItemRecord) element;
            return repositoryTree.getChecked(record);
        }
    }

    /**
     * 
     * add related subReport jrxml/jasper file to List.
     * 
     * @param record
     * @param itemRecords
     */
    private void addSubRepToElements(ItemRecord record, List<ItemRecord> itemRecords) {
        File file = record.getFile();
        if (record.getProperty() == null) {
            return;
        }
        String version = record.getProperty().getVersion();
        String nameWithoutVersion = file.getName().replaceAll(underlineStr + version, PluginConstant.EMPTY_STRING)
                .replaceAll(PluginConstant.DOT_STRING + FactoriesUtil.JRXML, PluginConstant.EMPTY_STRING);
        IFile iFile = ResourceManager.getJRXMLFolder().getFile(subrepName);
        File subRepFolder = WorkspaceUtils.ifileToFile(iFile);
        if (subRepFolder == null || !subRepFolder.exists()) {
            return;
        }
        Map<String, List<String>> mainSubRepMap = ReportUtils.getMainSubRepMap();
        String fName;
        for (File f : subRepFolder.listFiles()) {
            fName = f.getName();
            if (fName.equalsIgnoreCase(file.getName())) {
                continue;
            }
            // add the same name jasper file when the sub-report jrxml is checked on UI.
            if (fName.equalsIgnoreCase(nameWithoutVersion + PluginConstant.DOT_STRING + PluginConstant.JASPER_STRING)) {
                ItemRecord itemRecord = new ItemRecord(f);
                itemRecords.add(itemRecord);
                continue;
            }
            // add sub-reports jrxml and jasper when the main-report is checked on UI.
            List<String> subLsFromMap = mainSubRepMap.get(nameWithoutVersion);
            if (subLsFromMap != null) {
                for (String name : subLsFromMap) {
                    if (fName.startsWith(name)) {
                        ItemRecord itemRecord = new ItemRecord(f);
                        itemRecords.add(itemRecord);
                    }
                }
            }
        }
    }
}
