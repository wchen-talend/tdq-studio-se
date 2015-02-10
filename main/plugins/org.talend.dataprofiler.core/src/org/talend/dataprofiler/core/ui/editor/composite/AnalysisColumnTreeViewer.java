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
package org.talend.dataprofiler.core.ui.editor.composite;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.help.HelpSystem;
import org.eclipse.help.IContext;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.repositoryObject.MetadataColumnRepositoryObject;
import org.talend.core.repository.model.repositoryObject.MetadataTableRepositoryObject;
import org.talend.core.repository.model.repositoryObject.MetadataXmlElementTypeRepositoryObject;
import org.talend.cwm.db.connection.ConnectionUtils;
import org.talend.cwm.helper.ColumnSetHelper;
import org.talend.cwm.helper.ModelElementHelper;
import org.talend.cwm.helper.ResourceHelper;
import org.talend.cwm.relational.TdTable;
import org.talend.dataprofiler.core.ImageLib;
import org.talend.dataprofiler.core.helper.ModelElementIndicatorHelper;
import org.talend.dataprofiler.core.i18n.internal.DefaultMessagesImpl;
import org.talend.dataprofiler.core.manager.DQPreferenceManager;
import org.talend.dataprofiler.core.model.DelimitedFileIndicator;
import org.talend.dataprofiler.core.model.ModelElementIndicator;
import org.talend.dataprofiler.core.ui.dialog.composite.TooltipTree;
import org.talend.dataprofiler.core.ui.editor.AbstractAnalysisActionHandler;
import org.talend.dataprofiler.core.ui.editor.AbstractMetadataFormPage;
import org.talend.dataprofiler.core.ui.editor.analysis.AbstractAnalysisMetadataPage;
import org.talend.dataprofiler.core.ui.editor.analysis.ColumnMasterDetailsPage;
import org.talend.dataprofiler.core.ui.editor.preview.IndicatorUnit;
import org.talend.dataprofiler.core.ui.grid.IndicatorSelectDialog;
import org.talend.dataprofiler.core.ui.grid.utils.Observerable;
import org.talend.dataprofiler.core.ui.grid.utils.TDQObserver;
import org.talend.dataprofiler.core.ui.utils.UDIUtils;
import org.talend.dataprofiler.core.ui.views.ColumnViewerDND;
import org.talend.dataprofiler.help.HelpPlugin;
import org.talend.dataquality.analysis.Analysis;
import org.talend.dataquality.analysis.ExecutionLanguage;
import org.talend.dataquality.helpers.AnalysisHelper;
import org.talend.dataquality.helpers.MetadataHelper;
import org.talend.dataquality.indicators.CompositeIndicator;
import org.talend.dataquality.indicators.DataminingType;
import org.talend.dataquality.indicators.Indicator;
import org.talend.dataquality.indicators.definition.IndicatorDefinition;
import org.talend.dataquality.indicators.sql.UserDefIndicator;
import org.talend.dq.dbms.DbmsLanguage;
import org.talend.dq.dbms.DbmsLanguageFactory;
import org.talend.dq.helper.RepositoryNodeHelper;
import org.talend.dq.helper.resourcehelper.IndicatorResourceFileHelper;
import org.talend.dq.nodes.AnalysisRepNode;
import org.talend.repository.model.IRepositoryNode;
import org.talend.resource.ResourceManager;
import org.talend.utils.sugars.TypedReturnCode;
import orgomg.cwm.foundation.softwaredeployment.DataManager;
import orgomg.cwm.foundation.softwaredeployment.DataProvider;
import orgomg.cwm.objectmodel.core.Expression;
import orgomg.cwm.objectmodel.core.ModelElement;

/**
 * @author rli
 * 
 */
public class AnalysisColumnTreeViewer extends AbstractColumnDropTree implements Observerable<ModelElement[]>,
        TDQObserver<Map<String, Integer>> {

    protected static Logger log = Logger.getLogger(AnalysisColumnTreeViewer.class);

    public static final String VIEWER_KEY = "org.talend.dataprofiler.core.ui.editor.composite.AnasisColumnTreeViewer"; //$NON-NLS-1$

    private Composite parentComp;

    private ColumnMasterDetailsPage masterPage;

    // ADD xqliu 2009-08-24 bug 8776
    private ExecutionLanguage language;

    private Composite buttonsComp;

    private List<TDQObserver<ModelElement[]>> Observers = null;

    public ExecutionLanguage getLanguage() {
        return language;
    }

    public void setLanguage(ExecutionLanguage language) {
        this.language = language;
    }

    public AnalysisColumnTreeViewer(Composite parent, ColumnMasterDetailsPage masterPage) {
        this.viewKey = VIEWER_KEY;
        absMasterPage = masterPage;
        parentComp = parent;
        this.tree = createTree(parent);
        this.masterPage = masterPage;
        // this.setElements(masterPage.getCurrentModelElementIndicators());
        this.createUpDownButtons(parent);
        this.setDirty(false);
    }

    /**
     * @param parent
     */
    private Tree createTree(Composite parent) {
        final Tree newTree = new TooltipTree(parent, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION) {

            @Override
            protected boolean isValidItem(TreeItem item) {
                if (item == null) {
                    return false;
                }
                Object itemData = item.getData(INDICATOR_UNIT_KEY);
                Object columnItemData = item.getData(MODELELEMENT_INDICATOR_KEY);
                if (itemData != null) {
                    IndicatorUnit indicatorUnit = (IndicatorUnit) item.getData(INDICATOR_UNIT_KEY);
                    if (indicatorUnit != null && !(indicatorUnit.getIndicator() instanceof CompositeIndicator)) {
                        return true;
                    }
                    return false;
                } else {
                    if (columnItemData != null) {
                        ModelElementIndicator modelElementIndicator = (ModelElementIndicator) columnItemData;
                        IRepositoryNode modelElementRepositoryNode = modelElementIndicator.getModelElementRepositoryNode();
                        return modelElementRepositoryNode == null ? false : true;
                    }
                    return false;
                }
            }

            @Override
            protected String getItemTooltipText(TreeItem item) {
                String expCont = isExpressionNull(item);
                if (expCont == null) {
                    return DefaultMessagesImpl.getString("AnalysisColumnTreeViewer.queryNotGen"); //$NON-NLS-1$
                }
                return expCont;
            }
        };
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(newTree);

        newTree.setHeaderVisible(true);

        createTreeItem(newTree, 190, "AnalysisColumnTreeViewer.analyzedColumns"); //$NON-NLS-1$
        createTreeDataminingItem(newTree);
        createTreeItem(newTree, 80, "AnalysisColumnTreeViewer.pattern"); //$NON-NLS-1$
        createTreeItem(newTree, 80, "AnalysisColumnTreeViewer.udi"); //$NON-NLS-1$
        createTreeItem(newTree, 80, "AnalysisColumnTreeViewer.operation"); //$NON-NLS-1$

        parent.layout();

        AbstractAnalysisActionHandler actionHandler = new AbstractAnalysisActionHandler(parent) {

            @Override
            protected void handleRemove() {
                removeSelectedElements(newTree);
            }

        };

        parent.setData(AbstractMetadataFormPage.ACTION_HANDLER, actionHandler);
        ColumnViewerDND.installDND(newTree);
        this.addTreeListener(newTree);
        newTree.setData(this);
        return newTree;
    }

    /**
     * DOC yyi create Up Down and Delete Buttons.
     * 
     * @param parent
     */
    private void createUpDownButtons(Composite parent) {
        if (null != buttonsComp) {
            buttonsComp.dispose();
        }
        buttonsComp = masterPage.getEditor().getToolkit().createComposite(parent, SWT.NONE);
        buttonsComp.setLayout(new GridLayout(3, true));
        buttonsComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridData buttonGridData = new GridData(SWT.CENTER, SWT.CENTER, true, false);

        Button delButton = new Button(buttonsComp, SWT.NONE);
        delButton.setImage(ImageLib.getImage(ImageLib.DELETE_ACTION));
        delButton.setLayoutData(buttonGridData);
        delButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                removeSelectedElements(tree);
                notifyObservers();
            }
        });

        Button moveUpButton = new Button(buttonsComp, SWT.NONE);
        moveUpButton.setText(DefaultMessagesImpl.getString("ColumnsComparisonMasterDetailsPage.moveUp")); //$NON-NLS-1$
        moveUpButton.setLayoutData(buttonGridData);
        moveUpButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                moveSelectedElements(tree, -1);
                notifyObservers();
            }
        });

        Button moveDownButton = new Button(buttonsComp, SWT.NONE);
        moveDownButton.setText(DefaultMessagesImpl.getString("ColumnsComparisonMasterDetailsPage.moveDown")); //$NON-NLS-1$
        moveDownButton.setLayoutData(buttonGridData);
        moveDownButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                moveSelectedElements(tree, 1);
                notifyObservers();
            }
        });

        parent.layout();
    }

    /**
     * DOC yyi to set the Up Down and Delete Buttons is visible.
     * 
     * @param isShow
     */
    public void setControlButtonsVisible(boolean isShow) {
        buttonsComp.setVisible(isShow);
    }

    /**
     * DOC yyi 7466 2010-03-22 change the order of appearence of indicators.
     * 
     * @param newTree
     * @param step
     */
    protected void moveSelectedElements(Tree newTree, int step) {
        TreeItem[] selection = newTree.getSelection();
        if (selection.length > 0) {
            TreeItem item = selection[0];
            IndicatorUnit indicatorUnit = (IndicatorUnit) item.getData(INDICATOR_UNIT_KEY);
            ModelElementIndicator data = (ModelElementIndicator) item.getData(MODELELEMENT_INDICATOR_KEY);
            if (indicatorUnit != null) {
                data = (ModelElementIndicator) item.getData(MODELELEMENT_INDICATOR_KEY);
                TypedReturnCode<IndicatorUnit[]> code = sortIndicatorUnits(data.getIndicatorUnits(), indicatorUnit, step);
                if (code.isOk()) {
                    if (null != code.getObject()) {
                        Indicator[] inds = new Indicator[code.getObject().length];
                        for (int i = 0; i < code.getObject().length; i++) {
                            inds[i] = code.getObject()[i].getIndicator();
                        }
                        data.setIndicators(inds);
                    }
                    setElements(modelElementIndicators, true, false);
                    selectElement(tree.getItems(), indicatorUnit);
                }
            } else {
                data = (ModelElementIndicator) item.getData(MODELELEMENT_INDICATOR_KEY);
                int index = -1;
                for (int i = 0; i < modelElementIndicators.length; i++) {
                    if (data == modelElementIndicators[i]) {
                        index = i;
                        break;
                    }
                }
                if (index + step > -1 && index + step < modelElementIndicators.length) {
                    ModelElementIndicator tmpElement = modelElementIndicators[index + step];
                    modelElementIndicators[index + step] = modelElementIndicators[index];
                    modelElementIndicators[index] = tmpElement;
                    setElements(modelElementIndicators, true, false);
                    selectElement(tree.getItems(), data);
                }
            }
            masterPage.synNagivatorStat();
        }
    }

    /**
     * DOC yyi select element after moved.
     * 
     * @param items
     * @param data
     */
    private void selectElement(TreeItem[] items, Object data) {

        if (data instanceof ModelElementIndicator) {
            for (TreeItem item : items) {
                ModelElementIndicator model = (ModelElementIndicator) item.getData(MODELELEMENT_INDICATOR_KEY);
                if (model == data) {
                    tree.setSelection(item);
                } else {
                    selectElement(item.getItems(), data);
                }
            }
        } else if (data instanceof IndicatorUnit) {
            for (TreeItem item : items) {
                IndicatorUnit unit = (IndicatorUnit) item.getData(INDICATOR_UNIT_KEY);
                if (unit != null && ((IndicatorUnit) data).getIndicator() == unit.getIndicator()) {
                    tree.setSelection(item);
                } else {
                    selectElement(item.getItems(), data);
                }
            }
        }
    }

    /**
     * DOC yyi Sort indicators.
     * 
     * @param units
     * @param targetUnit
     * @param step
     * @return
     */
    private TypedReturnCode<IndicatorUnit[]> sortIndicatorUnits(IndicatorUnit[] units, IndicatorUnit targetUnit, int step) {

        TypedReturnCode<IndicatorUnit[]> code = new TypedReturnCode<IndicatorUnit[]>();
        int index = -1;
        for (int i = 0; i < units.length; i++) {
            if (targetUnit.getIndicator() == units[i].getIndicator()) {
                index = i;
                break;
            } else if (null != units[i].getChildren()) {
                TypedReturnCode<IndicatorUnit[]> code2 = sortIndicatorUnits(units[i].getChildren(), targetUnit, step);
                if (null != code2.getObject()) {
                    units[i].setChildren(code2.getObject());
                    code.setOk(true);
                    break;
                }
            }
        }
        if (-1 != index && index + step > -1 && index + step < units.length) {
            IndicatorUnit tmpUnit = units[index + step];
            units[index + step] = units[index];
            units[index] = tmpUnit;
            code.setReturnCode("", true, units);//$NON-NLS-1$
        }
        return code;
    }

    @Override
    public void setElements(ModelElementIndicator[] elements) {
        setElements(elements, true);
    }

    public void setElements(ModelElementIndicator[] elements, boolean isDirty) {
        setElements(elements, isDirty, false);
    }

    public void setElements(ModelElementIndicator[] elements, boolean isDirty, boolean isExpandAll) {
        removeAllItemElements();
        tree.setData(VIEWER_KEY, this);
        this.modelElementIndicators = elements;

        addItemElements(elements, isExpandAll);

        if (isDirty) {
            this.setDirty(true);
        }
        initializedConnection(elements);
        // MOD mzhao 2009-05-5, bug 6587.
        updateBindConnection(masterPage, modelElementIndicators, tree);

        createUpDownButtons(this.parentComp);
    }

    /**
     * DOC talend Comment method "removeAllItemElements".
     */
    private void removeAllItemElements() {
        for (Control control : tree.getChildren()) {
            if (control != null) {
                control.dispose();
            }
        }
        for (TreeItem treeItem : tree.getItems()) {
            if (treeItem != null) {
                treeItem.dispose();
            }
        }
    }

    /**
     * MOD mzhao 2009-06-16 feature 5887.
     */
    @Override
    public void updateModelViewer() {
        masterPage.recomputeIndicators();
        modelElementIndicators = masterPage.getCurrentModelElementIndicators();
        masterPage.refreshTheTree(modelElementIndicators);
        // setElements(modelElementIndicators);
    }

    @Override
    public void addElements(final ModelElementIndicator[] elements) {
        ModelElementIndicator[] newsArray = new ModelElementIndicator[getAllTheElementIndicator().length + elements.length];
        System.arraycopy(getAllTheElementIndicator(), 0, newsArray, 0, getAllTheElementIndicator().length);
        for (int i = 0; i < elements.length; i++) {
            newsArray[getAllTheElementIndicator().length + i] = elements[i];
        }
        this.addItemElements(elements, false);
        this.setDirty(true);
        initializedConnection(elements);
        // MOD mzhao 2009-05-5, bug 6587.
        updateBindConnection(masterPage, modelElementIndicators, tree);
        masterPage.refreshTheTree(newsArray);
        masterPage.refreshPreviewTable(newsArray);
        masterPage.goLastPage();
        if (elements != null && elements.length > 0) {
            selectElement(tree.getItems(), elements[0]);
        }
    }

    /**
     * DOC Administrator Comment method "initializedConnection". for 6560
     * 
     * @param indicators
     * 
     */
    private void initializedConnection(ModelElementIndicator[] indicators) {
        Analysis analysis = masterPage.getAnalysisHandler().getAnalysis();
        DataManager connection = analysis.getContext().getConnection();
        // Connection tdDataProvider = null;

        boolean enableWhereClauseFlag = true;
        boolean enableExecuteLanguageFlag = false;
        // ~
        if (indicators != null && indicators.length > 0) {
            if (connection == null) {
                connection = ModelElementIndicatorHelper.getTdDataProvider(indicators[0]);
                analysis.getContext().setConnection(connection);
            }
            if (connection != null && masterPage.getExecCombo() != null) {
                if (ConnectionUtils.isDelimitedFileConnection((DataProvider) connection)) {
                    masterPage.setWhereClauseDisabled();
                    // when the selected column is not DB type,will disable the execute engine combobox.
                    masterPage.changeExecuteLanguageToJava(true);

                    enableWhereClauseFlag = false;
                    enableExecuteLanguageFlag = false;
                } else {// when the selected column is back to DB type, should enable the execute engine combobox again.
                    masterPage.enableExecuteLanguage();
                }
            }
        }
        // MOD klliu if default ExecutionLanguage is java,it is not changed to SQL.2011-11-21
        String execLang = analysis.getParameters().getExecutionLanguage().getLiteral();
        if (execLang != null && ExecutionLanguage.JAVA.getLiteral().equals(execLang)
                && (ConnectionUtils.isDelimitedFileConnection((DataProvider) connection))) {
            enableExecuteLanguageFlag = false;
        }

        if (enableWhereClauseFlag) {
            masterPage.setWhereClauseEnable();
        }

        if (enableExecuteLanguageFlag) {
            masterPage.changeExecuteLanguageToSql(true);
        }
    }

    private void addItemElements(final ModelElementIndicator[] elements, boolean isExpandAll) {
        for (ModelElementIndicator element : elements) {
            final TreeItem treeItem = new TreeItem(tree, SWT.NONE);
            treeItem.setImage(getColumnElementImage());

            final ModelElementIndicator meIndicator = element;
            treeItem.setText(0, ModelElementIndicatorHelper.getModelElementDisplayName(meIndicator));
            treeItem.setData(MODELELEMENT_INDICATOR_KEY, meIndicator);

            TreeEditor comboEditor = new TreeEditor(tree);
            final CCombo combo = new CCombo(tree, SWT.BORDER);
            for (DataminingType type : DataminingType.values()) {
                combo.add(type.getLiteral()); // MODSCA 2008-04-10 use literal
                // for presentation
            }

            final ModelElement modelElement = RepositoryNodeHelper
                    .getSubModelElement(meIndicator.getModelElementRepositoryNode());
            DataminingType dataminingType = MetadataHelper.getDataminingType(modelElement);
            // MOD qiongli 2010-11-15 feature 16796
            if (meIndicator instanceof DelimitedFileIndicator) {
                dataminingType = MetadataHelper.getDefaultDataminingType(meIndicator.getJavaType());
            }

            if (dataminingType == null) {
                combo.select(0);
            } else {
                combo.setText(dataminingType.getLiteral());
            }
            combo.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    MetadataHelper.setDataminingType(DataminingType.get(combo.getText()), modelElement);
                    setDirty(true);
                }

            });
            combo.setEditable(false);

            comboEditor.minimumWidth = WIDTH1_CELL;
            comboEditor.setEditor(combo, treeItem, 1);

            TreeEditor addPatternEditor = new TreeEditor(tree);
            Label addPatternLabl = createTreeItemLabel(tree, ImageLib.ADD_PATTERN, "AnalysisColumnTreeViewer.addPattern"); //$NON-NLS-1$ 

            // MOD mzhao feature:13040, 2010-05-21
            addPatternLabl.addMouseListener(new PatternMouseAdapter(this, masterPage, meIndicator, treeItem));
            addPatternEditor.minimumWidth = addPatternLabl.getImage().getBounds().width;
            addPatternEditor.setEditor(addPatternLabl, treeItem, 2);

            // ADD xqliu 2010-02-23 feature 11617
            TreeEditor addUdiEditor = addColumnUdi(treeItem, meIndicator, 3);
            // ~

            TreeEditor delLabelEditor = new TreeEditor(tree);
            Label delLabel = createTreeItemLabel(tree, ImageLib.DELETE_ACTION, "AnalysisColumnTreeViewer.delete"); //$NON-NLS-1$
            delLabel.setData(treeItem);
            delLabel.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseDown(MouseEvent e) {
                    // // here is the delLable which is behind of column, so need to remove current column and it's all
                    // of
                    TreeItem currentTreeItem = (TreeItem) e.widget.getData();
                    removeSelectedElements(new TreeItem[] { currentTreeItem });
                    notifyObservers();
                }

            });

            delLabelEditor.minimumWidth = delLabel.getImage().getBounds().width;
            delLabelEditor.horizontalAlignment = SWT.CENTER;
            delLabelEditor.setEditor(delLabel, treeItem, 4);
            treeItem.setData(ITEM_EDITOR_KEY, new TreeEditor[] { comboEditor, delLabelEditor, addPatternEditor, addUdiEditor });
            if (meIndicator.hasIndicators()) {
                createIndicatorItems(treeItem, meIndicator.getIndicatorUnits());
            }
            treeItem.setExpanded(isExpandAll);
        }

    }

    /**
     * DOC yyi 2010-04-29 12572 for MDM elements.
     * 
     * @param element
     * @return
     */
    private Image getColumnElementImage() {
        return ImageLib.getImage(ImageLib.TD_COLUMN);
    }

    /**
     * DOC xqliu Comment method "addColumnUdi". ADD xqliu 2010-02-23 feature 11617
     * 
     * @param treeItem
     * @param meIndicator
     * @param columnIndex
     * @return
     */
    private TreeEditor addColumnUdi(final TreeItem treeItem, final ModelElementIndicator meIndicator, int columnIndex) {
        TreeEditor addUdiEditor = new TreeEditor(tree);
        Label addUdiLabl = new Label(tree, SWT.NONE);
        addUdiLabl.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
        addUdiLabl.setImage(ImageLib.getImage(ImageLib.ADD_IND_DEFINITION));
        addUdiLabl.setToolTipText(DefaultMessagesImpl.getString("AnalysisColumnTreeViewer.addUdi")); //$NON-NLS-1$
        addUdiLabl.pack();

        addUdiLabl.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseDown(MouseEvent e) {
                DataManager dm = getAnalysis().getContext().getConnection();
                if (dm == null) {
                    masterPage.doSave(null);
                }

                IFolder udiProject = ResourceManager.getUDIFolder();

                CheckedTreeSelectionDialog dialog = UDIUtils.createUdiCheckedTreeSelectionDialog(udiProject, meIndicator);

                if (dialog.open() == Window.OK) {
                    // MOD qiongli 2012-10-24 TDQ-6308,just remove some deselected indicatorUnit then set dirty to
                    // true,just create new indicatorUnit for some new added UDI then set dirty to true.
                    List<IFile> allSelectedFiles = new ArrayList<IFile>();
                    Set<String> allSelectedIndNames = new HashSet<String>();
                    for (Object obj : dialog.getResult()) {
                        if (obj instanceof IFile) {
                            allSelectedFiles.add((IFile) obj);
                            IndicatorDefinition udid = IndicatorResourceFileHelper.getInstance().findIndDefinition((IFile) obj);
                            if (udid != null) {
                                allSelectedIndNames.add(udid.getName());
                            }
                        }
                    }

                    Set<String> oldSelectedIndNames = new HashSet<String>();
                    for (IndicatorUnit indicatorUnit : meIndicator.getIndicatorUnits()) {
                        Indicator indicator = indicatorUnit.getIndicator();
                        if (indicator instanceof UserDefIndicator) {
                            if (allSelectedIndNames.contains(indicator.getName())) {
                                oldSelectedIndNames.add(indicator.getName());
                            } else {
                                // remove the UDI from UI need to insert this UDI into removeList
                                deleteIndicatorItems(meIndicator, indicatorUnit);
                                if (!isDirty()) {
                                    setDirty(true);
                                }
                            }

                        }
                    }
                    treeItem.removeAll();

                    for (IFile file : allSelectedFiles) {
                        IndicatorDefinition udid = IndicatorResourceFileHelper.getInstance().findIndDefinition(file);
                        if (udid != null && oldSelectedIndNames.contains(udid.getName())) {
                            continue;
                        }
                        IndicatorUnit[] addIndicatorUnits = null;
                        try {
                            addIndicatorUnits = UDIUtils.createIndicatorUnit(file, meIndicator, getAnalysis());
                        } catch (Throwable e1) {
                            log.warn(e1, e1);
                        }
                        if (addIndicatorUnits != null && addIndicatorUnits.length > 0) {
                            for (IndicatorUnit unit : addIndicatorUnits) {
                                createOneUnit(treeItem, unit);
                            }
                            if (!isDirty()) {
                                setDirty(true);
                            }
                        }
                    }

                    treeItem.setExpanded(true);
                    masterPage.refreshTheTree(masterPage.getCurrentModelElementIndicators());
                }
            }
        });
        addUdiEditor.minimumWidth = addUdiLabl.getImage().getBounds().width;
        addUdiEditor.setEditor(addUdiLabl, treeItem, columnIndex);
        return addUdiEditor;
    }

    /**
     * DOC rli Comment method "deleteTreeElements".
     * 
     * @param deleteModelElementIndiciator
     */
    private void deleteModelElementItems(ModelElementIndicator deleteModelElementIndiciator) {
        ModelElementIndicator[] remainIndicators = new ModelElementIndicator[modelElementIndicators.length - 1];
        int i = 0;
        for (ModelElementIndicator indicator : modelElementIndicators) {
            if (deleteModelElementIndiciator.equals(indicator)) {
                continue;
            } else {
                remainIndicators[i] = indicator;
                i++;
            }
        }
        this.modelElementIndicators = remainIndicators;

        initializedConnection(this.modelElementIndicators);
    }

    public ModelElementIndicator[] openIndicatorSelectDialog(Shell shell) {
        String whereExpression = AnalysisHelper.getStringDataFilter(this.getAnalysis());
        final IndicatorSelectDialog dialog = new IndicatorSelectDialog(
                shell,
                DefaultMessagesImpl.getString("AnalysisColumnTreeViewer.indicatorSelection"), masterPage.getCurrentModelElementIndicators(), whereExpression); //$NON-NLS-1$
        dialog.setLimitNumber(this.masterPage.getPreviewLimit());
        dialog.create();
        if (!DQPreferenceManager.isBlockWeb()) {
            dialog.getShell().addShellListener(new ShellAdapter() {

                @Override
                public void shellActivated(ShellEvent e) {
                    dialog.getShell().setFocus();
                    IContext context = HelpSystem.getContext(HelpPlugin.getDefault().getIndicatorSelectorHelpContextID());
                    PlatformUI.getWorkbench().getHelpSystem().displayHelp(context);
                }
            });

            dialog.getShell().addHelpListener(new HelpListener() {

                public void helpRequested(HelpEvent e) {
                    IContext context = HelpSystem.getContext(HelpPlugin.getDefault().getIndicatorSelectorHelpContextID());
                    PlatformUI.getWorkbench().getHelpSystem().displayHelp(context);
                }
            });
        }
        if (dialog.checkWhereClause() && dialog.open() == Window.OK) {
            ModelElementIndicator[] result = dialog.getResult();
            for (ModelElementIndicator modelElementIndicator : result) {
                modelElementIndicator.storeTempIndicator();
            }

            // this.setElements(result);
            return result;
        } else {
            ModelElementIndicator[] result = dialog.getResult();
            for (ModelElementIndicator modelElementIndicator : result) {
                modelElementIndicator.getTempIndicator().clear();
            }
            return new ModelElementIndicator[0];
        }

    }

    /**
     * Remove the selected elements(eg:TdColumn or Indicator) from tree.
     * 
     * @param newTree
     */
    private void removeSelectedElements(Tree newTree) {
        TreeItem[] selection = newTree.getSelection();
        removeSelectedElements(selection);
    }

    private void removeSelectedElements(TreeItem[] items) {
        boolean branchIndicatorExist = false;
        for (TreeItem item : items) {
            IndicatorUnit indicatorUnit = (IndicatorUnit) item.getData(INDICATOR_UNIT_KEY);
            if (indicatorUnit != null) {
                deleteIndicatorItems((ModelElementIndicator) item.getData(MODELELEMENT_INDICATOR_KEY), indicatorUnit);
            } else {
                deleteModelElementItems((ModelElementIndicator) item.getData(MODELELEMENT_INDICATOR_KEY));
            }
            // if the item's parent item is a indicator item, when current
            // indicator item removed, it's parent item
            // should be removed and recreate the tree;else,just need remove
            // current item and it's branch.
            if (item.getParentItem() != null && item.getParentItem().getData(INDICATOR_UNIT_KEY) != null) {
                branchIndicatorExist = true;
                continue;
            } else {
                removeItemBranch(item);
            }
        }
        if (branchIndicatorExist) {
            setElements(modelElementIndicators);
        } else {
            masterPage.synNagivatorStat();
            if (modelElementIndicators.length == 0) {
                masterPage.refreshTheTree(getAllTheElementIndicator());
            }
        }
        // MOD mzhao 2009-05-5, bug 6587.
        // MOD mzhao 2009-06-8, bug 5887.
        // updateBindConnection(masterPage, getColumnIndicator(), tree);
    }

    private String isExpressionNull(TreeItem item) {
        String expressContent = null;
        ModelElement me = null;
        IndicatorUnit indicatorUnit = (IndicatorUnit) item.getData(INDICATOR_UNIT_KEY);
        ModelElementIndicator meIndicator = (ModelElementIndicator) item.getData(MODELELEMENT_INDICATOR_KEY);
        if (indicatorUnit != null) {
            IRepositoryViewObject reposViewObj = meIndicator.getModelElementRepositoryNode().getObject();
            // MOD klliu 2010-01-30 Distinguish MetadataColumnRepositoryObject and
            // MetadataXmlElementTypeRepositoryObject
            if (reposViewObj instanceof MetadataColumnRepositoryObject) {
                me = ((MetadataColumnRepositoryObject) reposViewObj).getTdColumn();
            }
            Connection dataprovider = ModelElementHelper.getTdDataProvider(me);
            DbmsLanguage dbmsLang = DbmsLanguageFactory.createDbmsLanguage(dataprovider);
            Expression expression = dbmsLang.getInstantiatedExpression(indicatorUnit.getIndicator());
            if (expression != null) {
                expressContent = expression.getBody();
            }
        } else {
            IRepositoryNode parentNodeForColumnNode = RepositoryNodeHelper.getParentNodeForColumnNode(meIndicator
                    .getModelElementRepositoryNode());
            expressContent = DefaultMessagesImpl.getString(
                    "AnalysisColumnTreeViewer.columnParent", parentNodeForColumnNode.getObject().getLabel()); //$NON-NLS-1$ //;
        }

        return expressContent;
    }

    private void addTreeListener(final Tree tree) {
        tree.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                TreeItem item = (TreeItem) e.item;
                if (item == null || DATA_PARAM.equals(item.getData(DATA_PARAM))) {
                    Menu m = tree.getMenu();
                    if (m != null && !m.isDisposed()) {
                        m.dispose();
                    }
                    tree.setMenu(null);
                } else {
                    new AnalysisColumnColumnMenuProvider(tree).createTreeMenu(Boolean.FALSE);
                }
            }

        });

        tree.addTreeListener(treeAdapter);

        tree.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseDoubleClick(MouseEvent e) {
                TreeItem[] treeSelection = tree.getSelection();

                if (treeSelection.length > 0) {
                    TreeItem item = treeSelection[0];
                    Object indicatorobj = item.getData(INDICATOR_UNIT_KEY);
                    Object meobj = item.getData(MODELELEMENT_INDICATOR_KEY);
                    if (meobj != null && indicatorobj == null) {
                        // open indicator selector
                        ModelElementIndicator[] modelElementIndicator = openIndicatorSelectDialog(masterPage.getEditor()
                                .getEditorSite().getShell());
                        masterPage.refreshCurrentTreeViewer(modelElementIndicator);
                    } else if (meobj != null && indicatorobj != null) {
                        // open indicator option wizard
                        openIndicatorOptionDialog(Display.getCurrent().getActiveShell(), item);
                    }
                }
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.dataprofiler.core.ui.editor.composite.AbstractColumnDropTree#getTheSuitedComposite(org.eclipse.swt
     * .events.SelectionEvent)
     */
    @Override
    public ExpandableComposite getTheSuitedComposite(SelectionEvent e) {
        Composite[] previewChartCompsites = masterPage.getPreviewChartCompsites();
        if (previewChartCompsites == null) {
            return null;
        }

        Object obj = e.item.getData(MODELELEMENT_INDICATOR_KEY);
        if (obj instanceof ModelElementIndicator) {
            ModelElementIndicator meIndicator = (ModelElementIndicator) obj;
            for (Composite comp : previewChartCompsites) {
                if (comp.getData() == meIndicator) {
                    return (ExpandableComposite) comp;
                }
            }
        }

        return null;
    }

    /**
     * Getter for analysis.
     * 
     * @return the analysis
     */
    public Analysis getAnalysis() {
        return this.masterPage.getAnalysisHandler().getAnalysis();
    }

    /**
     * Getter for analysis.
     * 
     * @return the analysis
     */
    public AnalysisRepNode getAnalysisNode() {
        return this.masterPage.getAnalysisRepNode();
    }

    public Tree getTree() {
        return tree;
    }

    @Override
    public boolean canDrop(IRepositoryNode metadataRepositoryNode) {
        IRepositoryViewObject metadataRepObject = metadataRepositoryNode.getObject();
        ConnectionItem connItem = (ConnectionItem) metadataRepObject.getProperty().getItem();
        // MOD qiongli 2010-8-19,bug 14436:could not come from diffrent connection
        Connection tdProvider = connItem.getConnection();
        // FIXME text is never used.
        boolean text = metadataRepObject instanceof MetadataXmlElementTypeRepositoryObject;
        if (tdProvider == null) {
            return false;
        } else if (this.getAnalysis().getContext().getConnection() != null
                && !ResourceHelper.areSame(tdProvider, this.getAnalysis().getContext().getConnection())) {
            return false;
        }
        // MOD mzhao, 2010-07-23 bug 14014: If the editor is dirty, save it firstly before drag and drop an elements.
        if (masterPage.isDirty()) {
            masterPage.doSave(new NullProgressMonitor());
            return false;
        }
        List<ModelElement> existModelElements = new ArrayList<ModelElement>();
        for (ModelElementIndicator modelElementIndicator : this.getAllTheElementIndicator()) {
            ModelElement me = RepositoryNodeHelper.getModelElementFromRepositoryNode(modelElementIndicator
                    .getModelElementRepositoryNode());
            existModelElements.add(me);
        }
        // MOD mzhao 9848 2010-01-14, allowing to drag and drop table.
        if (metadataRepObject instanceof MetadataTableRepositoryObject) {
            MetadataTableRepositoryObject tableRepObject = (MetadataTableRepositoryObject) metadataRepObject;
            return !existModelElements.containsAll(ColumnSetHelper.getColumns((TdTable) tableRepObject.getTable()));
        }
        if (metadataRepObject instanceof MetadataColumnRepositoryObject) {
            if (existModelElements.contains(((MetadataColumnRepositoryObject) metadataRepObject).getTdColumn())) {
                return false;
            }
        }

        if (metadataRepObject instanceof MetadataXmlElementTypeRepositoryObject) {
            return !metadataRepositoryNode.hasChildren();
        }
        return true;
    }

    @Override
    public void setInput(Object[] objs) {
        if (objs == null || objs.length == 0) {
            this.removeSelectedElements(tree.getItems());
            return;
        }
        super.setInput(objs);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.ui.editor.composite.AbstractColumnDropTree#getMasterPage()
     */
    @Override
    public AbstractAnalysisMetadataPage getMasterPage() {
        return masterPage;
    }

    /**
     * DOC yyi 2010-06-10 Refactor to ModelElementTreeMenuProvider
     */
    class AnalysisColumnColumnMenuProvider extends ModelElementTreeMenuProvider {

        /**
         * DOC yyi ColumnModelElementTreeMenuProvider constructor comment. true *
         * 
         * @param tree
         */
        public AnalysisColumnColumnMenuProvider(Tree tree) {
            super(tree);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.talend.dataprofiler.core.ui.editor.composite.ModelElementTreeMenuProvider#getAnalysis2()
         */
        @Override
        protected AnalysisRepNode getAnalysis2() {
            return getAnalysisNode();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.talend.dataprofiler.core.ui.editor.composite.ModelElementTreeMenuProvider#removeSelectedElements2(org
         * .eclipse.swt.widgets.Tree)
         */
        @Override
        protected void removeSelectedElements2(Tree tree) {
            removeSelectedElements(tree);
        }

    }

    @Override
    protected ModelElementIndicator[] getAllTheElementIndicator() {
        return masterPage.getCurrentModelElementIndicators();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.dataprofiler.core.ui.grid.utils.Observerable#addObserver(org.talend.dataprofiler.core.ui.grid.utils
     * .TalendObserver)
     */
    public boolean addObserver(TDQObserver<ModelElement[]> observer) {
        initObserverable();
        return Observers.add(observer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.dataprofiler.core.ui.grid.utils.Observerable#removeObserver(org.talend.dataprofiler.core.ui.grid.utils
     * .TalendObserver)
     */
    public boolean removeObserver(TDQObserver<ModelElement[]> observer) {
        if (Observers == null) {
            return false;
        }
        return Observers.remove(observer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.ui.grid.utils.Observerable#clearObserver()
     */
    public void clearObserver() {
        if (Observers == null) {
            return;
        }
        Observers.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.ui.grid.utils.Observerable#notifyObservers()
     */
    public void notifyObservers() {
        if (Observers == null) {
            return;
        }
        for (TDQObserver<ModelElement[]> observer : Observers) {
            observer.update(ModelElementIndicatorHelper.getModelElementFromMEIndicator(getAllTheElementIndicator()));
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.ui.grid.utils.Observerable#initObserverable()
     */
    private void initObserverable() {
        if (Observers == null) {
            Observers = new ArrayList<TDQObserver<ModelElement[]>>();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.ui.grid.utils.TDQObserver#update(java.lang.Object)
     */
    public void update(Map<String, Integer> columnIndexMap) {

        ModelElementIndicator[] reorderModelElement = reorderModelElement(masterPage.getCurrentModelElementIndicators(),
                columnIndexMap);
        masterPage.refreshTheTree(reorderModelElement);
        masterPage.restorePage();
    }

    /**
     * DOC talend Comment method "reorderModelElement".
     * 
     * @param currentModelElementIndicators
     */
    private ModelElementIndicator[] reorderModelElement(ModelElementIndicator[] currentModelElementIndicators,
            Map<String, Integer> columnIndexMap) {
        ModelElementIndicator[] sortedModelElements = new ModelElementIndicator[currentModelElementIndicators.length];
        for (ModelElementIndicator currModelElement : currentModelElementIndicators) {
            String columnName = currModelElement.getElementName();
            sortedModelElements[columnIndexMap.get(columnName)] = currModelElement;
        }
        return sortedModelElements;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.ui.grid.utils.TDQObserver#update(int)
     */
    public void update(int EventType) {
        // TODO Auto-generated method stub

    }
}
