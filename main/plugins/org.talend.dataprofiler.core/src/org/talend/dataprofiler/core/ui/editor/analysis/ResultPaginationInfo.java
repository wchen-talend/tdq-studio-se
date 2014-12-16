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
package org.talend.dataprofiler.core.ui.editor.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.talend.dataprofiler.common.ui.editor.preview.ICustomerDataset;
import org.talend.dataprofiler.core.i18n.internal.DefaultMessagesImpl;
import org.talend.dataprofiler.core.model.ModelElementIndicator;
import org.talend.dataprofiler.core.model.dynamic.DynamicIndicatorModel;
import org.talend.dataprofiler.core.ui.editor.composite.AnalysisColumnTreeViewer;
import org.talend.dataprofiler.core.ui.editor.preview.CompositeIndicator;
import org.talend.dataprofiler.core.ui.editor.preview.IndicatorUnit;
import org.talend.dataprofiler.core.ui.editor.preview.model.ChartTableFactory;
import org.talend.dataprofiler.core.ui.editor.preview.model.ChartTypeStatesFactory;
import org.talend.dataprofiler.core.ui.editor.preview.model.TableTypeStatesFactory;
import org.talend.dataprofiler.core.ui.editor.preview.model.TableWithData;
import org.talend.dataprofiler.core.ui.editor.preview.model.states.IChartTypeStates;
import org.talend.dataprofiler.core.ui.editor.preview.model.states.table.ITableTypeStates;
import org.talend.dataprofiler.core.ui.events.DynamicChartEventReceiver;
import org.talend.dataprofiler.core.ui.events.EventEnum;
import org.talend.dataprofiler.core.ui.events.EventManager;
import org.talend.dataprofiler.core.ui.events.IEventReceiver;
import org.talend.dataprofiler.core.ui.pref.EditorPreferencePage;
import org.talend.dataprofiler.core.ui.utils.TOPChartUtils;
import org.talend.dataprofiler.core.ui.utils.pagination.UIPagination;
import org.talend.dataquality.analysis.Analysis;
import org.talend.dataquality.analysis.ExecutionLanguage;
import org.talend.dataquality.indicators.Indicator;
import org.talend.dq.analysis.explore.DataExplorer;
import org.talend.dq.helper.RepositoryNodeHelper;
import org.talend.dq.indicators.preview.EIndicatorChartType;
import org.talend.dq.indicators.preview.table.ChartDataEntity;
import org.talend.repository.model.IRepositoryNode;

/**
 * 
 * DOC mzhao UIPagination.MasterPaginationInfo class global comment. Detailled comment
 */
public class ResultPaginationInfo extends IndicatorPaginationInfo {

    private ColumnMasterDetailsPage masterPage;

    // Added TDQ-8787 20140617 yyin : store the temp indicator and its related table between one running
    private Map<List<Indicator>, TableViewer> indicatorTableMap = new HashMap<List<Indicator>, TableViewer>();

    // Added TDQ-9272 20140806, only use the Dynamic model for SQL mode.
    private boolean isSQLMode = true;

    public ResultPaginationInfo(ScrolledForm form, List<? extends ModelElementIndicator> modelElementIndicators,
            ColumnMasterDetailsPage masterPage, UIPagination uiPagination) {
        super(form, modelElementIndicators, uiPagination);
        this.masterPage = masterPage;
        if (masterPage.getTreeViewer() != null) {
            ExecutionLanguage language = ((AnalysisColumnTreeViewer) masterPage.getTreeViewer()).getLanguage();
            if (ExecutionLanguage.JAVA.equals(language)) {
                isSQLMode = false;
            }
        }
    }

    @Override
    protected void render() {
        clearDynamicList();
        indicatorTableMap.clear();
        for (final ModelElementIndicator modelElementIndicator : modelElementIndicators) {

            ExpandableComposite exComp = uiPagination.getToolkit().createExpandableComposite(uiPagination.getChartComposite(),
                    ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT | ExpandableComposite.EXPANDED);
            needDispostWidgets.add(exComp);
            // MOD klliu add more information about the column belongs to which table/view.
            IRepositoryNode modelElementRepositoryNode = modelElementIndicator.getModelElementRepositoryNode();
            IRepositoryNode parentNodeForColumnNode = RepositoryNodeHelper.getParentNodeForColumnNode(modelElementRepositoryNode);
            String label = parentNodeForColumnNode.getObject().getLabel();
            if (label != null && !label.equals("")) { //$NON-NLS-1$
                label = label.concat(".").concat(modelElementIndicator.getElementName());//$NON-NLS-1$
            } else {
                label = modelElementIndicator.getElementName();
            }
            // ~
            exComp.setText(DefaultMessagesImpl.getString("ColumnAnalysisResultPage.Column", label)); //$NON-NLS-1$
            exComp.setLayout(new GridLayout());
            exComp.setLayoutData(new GridData(GridData.FILL_BOTH));

            // MOD xqliu 2009-06-23 bug 7481
            exComp.setExpanded(EditorPreferencePage.isUnfoldingAnalyzedEelements());
            // ~

            final Composite comp = uiPagination.getToolkit().createComposite(exComp);
            comp.setLayout(new GridLayout());
            comp.setLayoutData(new GridData(GridData.FILL_BOTH));
            exComp.setClient(comp);

            createResultDataComposite(comp, modelElementIndicator);

            exComp.addExpansionListener(new ExpansionAdapter() {

                @Override
                public void expansionStateChanged(ExpansionEvent e) {
                    uiPagination.getChartComposite().layout();
                    form.reflow(true);
                }

            });
            uiPagination.getChartComposite().layout();
            masterPage.registerSection(exComp);
        }
    }

    private void createResultDataComposite(final Composite comp, final ModelElementIndicator modelElementIndicator) {

        if (modelElementIndicator.getIndicators().length != 0) {
            Map<EIndicatorChartType, List<IndicatorUnit>> indicatorComposite = CompositeIndicator.getInstance()
                    .getIndicatorComposite(modelElementIndicator);
            for (EIndicatorChartType chartType : indicatorComposite.keySet()) {
                List<IndicatorUnit> units = indicatorComposite.get(chartType);
                if (!units.isEmpty()) {
                    if (chartType == EIndicatorChartType.UDI_FREQUENCY) {
                        for (IndicatorUnit unit : units) {
                            List<IndicatorUnit> specialUnit = new ArrayList<IndicatorUnit>();
                            specialUnit.add(unit);
                            createChart(comp, chartType, specialUnit);
                        }
                    } else {
                        createChart(comp, chartType, units);
                    }
                }
            }
        }
    }

    /**
     * DOC bZhou Comment method "createChart".
     * 
     * @param comp
     * @param chartType
     * @param units
     */
    private void createChart(final Composite comp, EIndicatorChartType chartType, List<IndicatorUnit> units) {
        DynamicIndicatorModel dyModel = new DynamicIndicatorModel();

        // MOD TDQ-8787 20140618 yyin: to let the chart and table use the same dataset
        Object chart = null;
        Object dataset = null;
        // Added TDQ-8787 20140722 yyin:(when first switch from master to result) if there is some dynamic event for the
        // current indicator, use its dataset directly (TDQ-9241)
        IEventReceiver event = EventManager.getInstance().findRegisteredEvent(units.get(0).getIndicator(),
                EventEnum.DQ_DYMANIC_CHART, 0);
        // get the dataset from the event
        if (event != null) {
            dataset = ((DynamicChartEventReceiver) event).getDataset();
        }// ~

        // Added TDQ-8787 2014-06-18 yyin: add the current units and dataset into the list
        List<Indicator> indicators = null;
        dyModel.setChartType(chartType);
        this.dynamicList.add(dyModel);

        if (EIndicatorChartType.SUMMARY_STATISTICS.equals(chartType)) {
            // for the summary indicators, the table show 2 more than the bar chart
            dyModel.setSummaryIndicators(getIndicatorsForTable(units, true));
        }

        // create UI
        ExpandableComposite subComp = uiPagination.getToolkit().createExpandableComposite(comp,
                ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT | ExpandableComposite.EXPANDED);
        subComp.setText(chartType.getLiteral());
        subComp.setLayoutData(new GridData(GridData.FILL_BOTH));

        // MOD xqliu 2009-06-23 bug 7481
        subComp.setExpanded(EditorPreferencePage.isUnfoldingIndicators());
        // ~

        final Composite composite = uiPagination.getToolkit().createComposite(subComp, SWT.NULL);
        composite.setLayout(new GridLayout(2, false));
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        Analysis analysis = masterPage.getAnalysisHandler().getAnalysis();

        // create table viewer firstly
        ITableTypeStates tableTypeState = TableTypeStatesFactory.getInstance().getTableState(chartType, units);

        TableWithData chartData = new TableWithData(chartType, tableTypeState.getDataEntity());
        TableViewer tableviewer = tableTypeState.getTableForm(composite);
        tableviewer.setInput(chartData);
        dyModel.setTableViewer(tableviewer);

        DataExplorer dataExplorer = tableTypeState.getDataExplorer();
        ChartTableFactory.addMenuAndTip(tableviewer, dataExplorer, analysis);
        // ~

        ChartDataEntity[] dataEntities = tableTypeState.getDataEntity();
        if (EIndicatorChartType.TEXT_STATISTICS.equals(chartType) && dataEntities != null && dataEntities.length > 0) {
            // only text indicator need
            indicators = getIndicators(dataEntities);
        } else {
            indicators = getIndicators(units);
        }
        dyModel.setIndicatorList(indicators);

        // create chart
        if (!EditorPreferencePage.isHideGraphics()) {
            IChartTypeStates chartTypeState = ChartTypeStatesFactory.getChartState(chartType, units);
            if (event == null) {
                chart = chartTypeState.getChart();
                if (chart != null && isSQLMode) {// chart is null for MODE. Get the dataset by this way for SQL mode
                    if (EIndicatorChartType.BENFORD_LAW_STATISTICS.equals(chartType)) {
                        dataset = TOPChartUtils.getInstance().getDatasetFromChart(chart, 1);
                        dyModel.setSecondDataset(TOPChartUtils.getInstance().getDatasetFromChart(chart, 0));
                    } else {
                        dataset = TOPChartUtils.getInstance().getDatasetFromChart(chart, -1);
                    }
                }
            } else {
                chart = chartTypeState.getChart(((DynamicChartEventReceiver) event).getDataset());
            }

            TOPChartUtils.getInstance().decorateChart(chart, false);

            // if (dataset == null) {
            // dataset = chartTypeState.getDataset();
            // }
            dyModel.setDataset(dataset);

            Object chartComposite = TOPChartUtils.getInstance().createTalendChartComposite(comp, SWT.NONE, chart, true);
            if (EIndicatorChartType.SUMMARY_STATISTICS.equals(chartType)) {
                // for summary indicators: need to record the chart composite, which is used for create BAW chart
                dyModel.setBawParentChartComp(chartComposite);
            }

            Map<String, Object> menuMap = createMenuForAllDataEntity(((Composite) chartComposite).getShell(), dataExplorer,
                    analysis, ((ICustomerDataset) chartTypeState.getDataset()).getDataEntities());
            // call chart service to create related mouse listener
            TOPChartUtils.getInstance().addMouseListenerForChart(chartComposite, menuMap);
        }

        subComp.setClient(composite);
        subComp.addExpansionListener(new ExpansionAdapter() {

            @Override
            public void expansionStateChanged(ExpansionEvent e) {
                form.reflow(true);
            }

        });

        masterPage.registerSection(subComp);
    }

    /**
     * get the indicators from the data entities, which maybe sorted, and the order is changed.
     * 
     * @param dataEntities
     * @return
     */
    private List<Indicator> getIndicators(ChartDataEntity[] dataEntities) {
        List<Indicator> indicators = new ArrayList<Indicator>();

        for (ChartDataEntity entity : dataEntities) {
            indicators.add(entity.getIndicator());
        }
        return indicators;
    }

    public Map<List<Indicator>, TableViewer> getIndicatorTableMap() {
        return this.indicatorTableMap;
    }

}
