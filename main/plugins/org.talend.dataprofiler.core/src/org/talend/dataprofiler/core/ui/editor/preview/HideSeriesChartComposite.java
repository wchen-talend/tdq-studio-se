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
package org.talend.dataprofiler.core.ui.editor.preview;

import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.cwm.helper.ColumnHelper;
import org.talend.cwm.helper.SwitchHelpers;
import org.talend.dataprofiler.core.i18n.internal.DefaultMessagesImpl;
import org.talend.dataprofiler.core.ui.editor.preview.DatasetUtils.DateValueAggregate;
import org.talend.dataprofiler.core.ui.editor.preview.DatasetUtils.ValueAggregator;
import org.talend.dataprofiler.core.ui.utils.TOPChartUtils;
import org.talend.dataquality.analysis.Analysis;
import org.talend.dataquality.indicators.columnset.ColumnSetMultiValueIndicator;
import org.talend.dataquality.indicators.columnset.ColumnsetPackage;
import org.talend.dq.helper.SqlExplorerUtils;
import orgomg.cwm.objectmodel.core.ModelElement;

/**
 * DOC bzhou class global comment. Detailled comment
 */
public class HideSeriesChartComposite {

    private static Logger log = Logger.getLogger(HideSeriesChartComposite.class);

    private ColumnSetMultiValueIndicator indicator;

    private ModelElement column;

    private Object chart;

    private Object chartComposite;

    private boolean isNeedUtility;

    private static final String SERIES_KEY_ID = "SERIES_KEY"; //$NON-NLS-1$

    // used for the bubble chart to add mouse listeners
    private Map<String, String> bubbleQueryMap;

    private Map<String, String> ganttQueryMap;

    private Analysis analysis = null;

    boolean isCoungAvg = false;

    boolean isMinMax = false;

    public HideSeriesChartComposite(Composite comp, Analysis ana, ColumnSetMultiValueIndicator indicator, ModelElement column,
            boolean isNeedUtility) {

        this.analysis = ana;
        this.indicator = indicator;
        this.column = column;
        this.isNeedUtility = isNeedUtility;

        this.chart = createChart();

        chartComposite = ChartHelper.createChartComposite(comp, chart, indicator.getAnalyzedColumns().size() * 30 < 230 ? 230
                : indicator.getAnalyzedColumns().size() * 30);

        if (chart != null && isNeedUtility) {
            createUtilityControl(comp);
        }

        isCoungAvg = ColumnsetPackage.eINSTANCE.getCountAvgNullIndicator().equals(indicator.eClass());
        isMinMax = ColumnsetPackage.eINSTANCE.getMinMaxDateIndicator().equals(indicator.eClass());

        addSpecifiedListeners(isCoungAvg, isMinMax);
    }

    class ChartHelper {

        static Object createChartComposite(Composite parent, Object chart, int height) {
            ChartComposite chartComposite = new ChartComposite(comp, SWT.NONE);
            chartComposite.setCursor(new Cursor(Display.getDefault(), SWT.CURSOR_HAND));
            chartComposite.setToolTipText("sdfsdf"); //$NON-NLS-1$

            // the analysis.
            GridData gd = new GridData();
            gd.heightHint = height;
            gd.widthHint = 460;
            chartComposite.setLayoutData(gd);

            if (chart != null) {
                chartComposite.setChart(chart);
            }

            // ~14173
            return chartComposite;
        }

    }

    private Menu createMenu(final boolean isAvg, final boolean isDate) {
        // create menu
        Menu menu = new Menu(((Composite) chartComposite).getShell(), SWT.POP_UP);
        MenuItem itemShowInFullScreen = new MenuItem(menu, SWT.PUSH);
        itemShowInFullScreen.setText(DefaultMessagesImpl.getString("HideSeriesChartComposite.ShowInFullScreen")); //$NON-NLS-1$
        itemShowInFullScreen.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Display.getDefault().asyncExec(new Runnable() {

                    public void run() {
                        TOPChartUtils.getInstance().showChartInFillScreen(createChart(), isAvg, isDate);
                    }
                });
            }
        });
        return menu;
    }

    private void addSpecifiedListeners(final boolean isAvg, final boolean isDate) {
        final Menu menu = createMenu(isAvg, isDate);

        Map<String, String> queryMap;
        if (isAvg) {
            TOPChartUtils.getInstance().addSpecifiedListenersForCorrelationChart(queryMap, isAvg, isDate, menu, queryMap,
                    createSelectAdapter(sql));
        } else if (isDate) {
            TOPChartUtils.getInstance().addSpecifiedListenersForCorrelationChart(this.ganttQueryMap, isAvg, isDate, menu,
                    queryMap, createSelectAdapter(sql));
        }

    }

    /**
     * DOC yyin Comment method "createSelectAdapter".
     * 
     * @param sql
     * @return
     */
    private SelectionAdapter createSelectAdapter(final String sql) {
        return new SelectionAdapter() {

            private String sql;

            @Override
            public void widgetSelected(SelectionEvent e) {
                Display.getDefault().asyncExec(new Runnable() {

                    public void run() {
                        Connection tdDataProvider = SwitchHelpers.CONNECTION_SWITCH.doSwitch(analysis.getContext()
                                .getConnection());
                        String query = sql;
                        String editorName = ColumnHelper.getColumnSetOwner(column).getName();
                        SqlExplorerUtils.getDefault().runInDQViewer(tdDataProvider, query, editorName);
                    }

                });
            }

        };
    }

    /**
     * DOC bzhou Comment method "createChart".
     * 
     * @return
     */
    private Object createChart() {
        Object jchart = null;

        if (isCoungAvg) {
            jchart = createBubbleChart();
            TOPChartUtils.getInstance().decorateChart(jchart, false);
        } else {
            if (isMinMax) {
                final int nbNominalColumns = indicator.getNominalColumns().size();
                final int nbDateFunctions = indicator.getDateFunctions().size();
                final int indexOfDateCol = indicator.getDateColumns().indexOf(column);
                assert indexOfDateCol != -1;

                jchart = createGanttChart();

                TOPChartUtils.getInstance().createAnnotOnGantt(jchart, indicator.getListRows(),
                        nbNominalColumns + nbDateFunctions * indexOfDateCol + 3, nbNominalColumns);

                TOPChartUtils.getInstance().decorateChart(jchart, false);
            }
        }

        return jchart;
    }

    /**
     * DOC yyin Comment method "createGanttChart".
     * 
     * @return
     */
    private Object createGanttChart() {
        final Map<String, DateValueAggregate> createGannttDatasets = DatasetUtils.createGanttDatasets(indicator, column);

        Object ganttDataset = TOPChartUtils.getInstance().createTaskSeriesCollection();
        final Iterator<String> iterator = createGannttDatasets.keySet().iterator();
        while (iterator.hasNext()) {
            final String next = iterator.next();
            createGannttDatasets.get(next).addSeriesToGanttDataset(ganttDataset, next);
        }

        ganttQueryMap = DatasetUtils.getGanttQueryMap(createGannttDatasets, indicator, column, analysis);

        String chartAxies = DefaultMessagesImpl.getString("TopChartFactory.chartAxies", column.getName()); //$NON-NLS-1$

        return TOPChartUtils.getInstance().createGanttChart(chartAxies, ganttDataset);
    }

    /**
     * DOC yyin Comment method "createBubbleChart".
     * 
     * @return
     */
    private Object createBubbleChart() {
        final Map<String, ValueAggregator> createXYZDatasets = DatasetUtils.createXYZDatasets(indicator, column);

        Object dataset = TOPChartUtils.getInstance().createDefaultXYZDataset();
        final Iterator<String> iterator = createXYZDatasets.keySet().iterator();
        while (iterator.hasNext()) {
            final String next = iterator.next();
            createXYZDatasets.get(next).addSeriesToXYZDataset(dataset, next);
        }

        bubbleQueryMap = DatasetUtils.getQueryMap(createXYZDatasets, indicator, column, analysis);

        String chartName = DefaultMessagesImpl.getString("TopChartFactory.ChartName", column.getName()); //$NON-NLS-1$

        return TOPChartUtils.getInstance().createBubbleChart(chartName, dataset);
    }

    private void createUtilityControl(Composite parent) {
        Composite comp = new Composite(parent, SWT.BORDER);
        comp.setLayout(new GridLayout());
        comp.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END));
        comp.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));

        SelectionAdapter listener = (SelectionAdapter) TOPChartUtils.getInstance().createSelectionAdapterForButton(chart,
                isCoungAvg, isMinMax);

        if (this.isCoungAvg) {
            int count = TOPChartUtils.getInstance().getSeriesCount(chart);

            for (int i = 0; i < count; i++) {

                Button checkBtn = new Button(comp, SWT.CHECK);
                checkBtn.setText(dataset.getSeriesKey(i).toString());
                checkBtn.setSelection(true);
                checkBtn.addSelectionListener(listener);
                checkBtn.setData(SERIES_KEY_ID, i);
            }
        }

        if (isMinMax) {
            int count = TOPChartUtils.getInstance().getSeriesRowCount(chart);

            for (int i = 0; i < count; i++) {

                Button checkBtn = new Button(comp, SWT.CHECK);
                checkBtn.setText(dataset.getRowKey(i).toString());
                checkBtn.setSelection(true);
                checkBtn.addSelectionListener(listener);
                checkBtn.setData(SERIES_KEY_ID, i);
            }
        }

    }

    // CategoryToolTipGenerator toolTipGenerator = new CategoryToolTipGenerator() {
    //
    // public String generateToolTip(CategoryDataset dataset, int row, int column) {
    // TaskSeriesCollection taskSeriesColl = (TaskSeriesCollection) dataset;
    // List<Task> taskList = new ArrayList<Task>();
    // for (int i = 0; i < taskSeriesColl.getSeriesCount(); i++) {
    // for (int j = 0; j < taskSeriesColl.getSeries(i).getItemCount(); j++) {
    // taskList.add(taskSeriesColl.getSeries(i).get(j));
    // }
    // }
    // Task task = taskList.get(column);
    // // Task task = taskSeriesColl.getSeries(row).get(column);
    // String taskDescription = task.getDescription();
    //
    // Date startDate = task.getDuration().getStart();
    // Date endDate = task.getDuration().getEnd();
    //            return taskDescription + ",     " + startDate + "---->" + endDate; //$NON-NLS-1$ //$NON-NLS-2$
    // // return "this is a tooltip";
    // }
    // };
    //

}
