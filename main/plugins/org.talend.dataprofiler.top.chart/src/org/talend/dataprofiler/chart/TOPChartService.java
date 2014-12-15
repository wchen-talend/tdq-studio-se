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
package org.talend.dataprofiler.chart;

import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.experimental.chart.swt.ChartComposite;
import org.talend.dataprofiler.chart.util.ChartDecorator;
import org.talend.dataprofiler.chart.util.ChartUtils;
import org.talend.dataprofiler.chart.util.TalendChartComposite;
import org.talend.dataprofiler.chart.util.TopChartFactory;
import org.talend.dataprofiler.service.ITOPChartService;

/**
 * created by yyin on 2014-11-28 Detailled comment
 * 
 */
public class TOPChartService implements ITOPChartService {

    public static final int CHART_STANDARD_WIDHT = 600;

    public static final int CHART_STANDARD_HEIGHT = 275;

    @Override
    public Object getDatasetFromChart(Object chart, int datasetIndex) {
        if (datasetIndex > -1) {
            return ((JFreeChart) chart).getCategoryPlot().getDataset(datasetIndex);
        }
        return ((JFreeChart) chart).getCategoryPlot().getDataset();
    }

    @Override
    public Object createTalendChartComposite(Object parentComponent, int style, Object chart, boolean useBuffer) {
        ChartComposite cc = new TalendChartComposite((Composite) parentComponent, style, (JFreeChart) chart, useBuffer);

        GridData gd = new GridData();
        gd.widthHint = CHART_STANDARD_WIDHT;
        gd.heightHint = CHART_STANDARD_HEIGHT;
        cc.setLayoutData(gd);
        return cc;
    }

    @Override
    public Object createBarChart(String title, Object dataset, boolean showLegend) {
        return TopChartFactory.createBarChart(title, (CategoryDataset) dataset, false);
    }

    @Override
    public Object createBarChart(Object dataset) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object createBarChart(String title, Object dataset) {
        return TopChartFactory.createBarChart(title, (CategoryDataset) dataset);
    }

    @Override
    public Object createBenfordChart(String axisXLabel, String categoryAxisLabel, Object dataset, List<String> dotChartLabels,
            double[] formalValues, String title) {
        ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());
        Object barChart = ChartFactory.createBarChart(null, axisXLabel, categoryAxisLabel, (CategoryDataset) dataset,
                PlotOrientation.VERTICAL, false, true, false);

        Object lineChart = ChartDecorator.decorateBenfordLawChart((CategoryDataset) dataset, (JFreeChart) barChart, title,
                categoryAxisLabel, dotChartLabels, formalValues);
        return lineChart;

    }

    @Override
    public void decorateChart(Object chart, boolean withPlot) {
        if (withPlot) {
            ChartDecorator.decorate((JFreeChart) chart, PlotOrientation.HORIZONTAL);
        } else {
            ChartDecorator.decorate((JFreeChart) chart, null);
        }

    }

    @Override
    public void decorateColumnDependency(Object chart) {
        ChartDecorator.decorateColumnDependency((JFreeChart) chart);
    }

    @Override
    public Object createChartComposite(Object composite, int style, Object chart, boolean useBuffer) {
        ChartComposite cc = new ChartComposite((Composite) composite, style, (JFreeChart) chart, useBuffer);

        GridData gd = new GridData();
        gd.widthHint = CHART_STANDARD_WIDHT;
        gd.heightHint = CHART_STANDARD_HEIGHT;
        cc.setLayoutData(gd);
        return cc;
    }

    @Override
    public void setOrientation(Object chart, boolean isHorizontal) {
        if (isHorizontal) {
            ((JFreeChart) chart).getCategoryPlot().setOrientation(PlotOrientation.HORIZONTAL);
        } else {
            ((JFreeChart) chart).getCategoryPlot().setOrientation(PlotOrientation.VERTICAL);
        }
    }

    @Override
    public void setDisplayDecimalFormatOfChart(Object chart) {
        ChartDecorator.setDisplayDecimalFormat((JFreeChart) chart);
    }

    @Override
    public void addMouseListenerForChart(Object chartComposite, final Map<String, Object> menuMap) {
        final ChartComposite chartComp = (ChartComposite) chartComposite;
        chartComp.addChartMouseListener(new ChartMouseListener() {

            @Override
            public void chartMouseClicked(ChartMouseEvent event) {
                boolean flag = event.getTrigger().getButton() != MouseEvent.BUTTON3;
                chartComp.setDomainZoomable(flag);
                chartComp.setRangeZoomable(flag);
                if (flag) {
                    return;
                }

                ChartEntity chartEntity = event.getEntity();
                if (chartEntity != null && chartEntity instanceof CategoryItemEntity) {
                    CategoryItemEntity cateEntity = (CategoryItemEntity) chartEntity;

                    Menu menu = getCurrentMenu(cateEntity, menuMap);

                    chartComp.setMenu(menu);

                    menu.setVisible(true);
                }
            }

            private Menu getCurrentMenu(CategoryItemEntity cateEntity, Map<String, Object> menuMap1) {
                Object menu = menuMap1.get(cateEntity.getRowKey());
                if (menu != null) {
                    return (Menu) menu;
                }
                menu = menuMap1.get(cateEntity.getColumnKey());
                if (menu != null) {
                    return (Menu) menu;
                }
                return null;
            }

            @Override
            public void chartMouseMoved(ChartMouseEvent event) {
                // no action here

            }

        });
        chartComp.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                chartComp.dispose();

            }

        });
    }

    @Override
    public Object createPieChart(String title, Object dataset, boolean showLegend, boolean toolTips, boolean urls) {
        return TopChartFactory.createPieChart(title, (PieDataset) dataset, showLegend, toolTips, urls);
    }

    @Override
    public Object createBoxAndWhiskerChart(String title, Object dataset) {
        return TopChartFactory.createBoxAndWhiskerChart(title, (BoxAndWhiskerCategoryDataset) dataset);
    }

    @Override
    public Object createStackedBarChart(String title, Object dataset, boolean showLegend) {
        JFreeChart stackedBarChart = TopChartFactory.createStackedBarChart(title, (CategoryDataset) dataset, showLegend);
        ChartDecorator.decorate(stackedBarChart, null);
        return stackedBarChart;
    }

    @Override
    public Object createStackedBarChart(String title, Object dataset, boolean isHorizatal, boolean showLegend) {
        if (isHorizatal) {
            return TopChartFactory
                    .createStackedBarChart(title, (CategoryDataset) dataset, PlotOrientation.HORIZONTAL, showLegend);
        } else {
            return TopChartFactory.createStackedBarChart(title, (CategoryDataset) dataset, PlotOrientation.VERTICAL, showLegend);
        }
    }

    @Override
    public void addListenerToChartComp(Object chartComposite, final String referenceLink, final String menuText) {
        final ChartComposite chartComp = (ChartComposite) chartComposite;
        chartComp.addChartMouseListener(new ChartMouseListener() {

            @Override
            public void chartMouseClicked(ChartMouseEvent event) {
                if (event.getTrigger().getButton() == 1 && referenceLink != null) {
                    Menu menu = new Menu(chartComp.getShell(), SWT.POP_UP);
                    chartComp.setMenu(menu);

                    MenuItem item = new MenuItem(menu, SWT.PUSH);
                    item.setText(menuText);
                    item.addSelectionListener(new SelectionAdapter() {

                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            ChartUtils.openReferenceLink(referenceLink);
                        }
                    });

                    menu.setVisible(true);
                }
            }

            @Override
            public void chartMouseMoved(ChartMouseEvent event) {
                // no need to implement
            }

        });
        chartComp.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                chartComp.dispose();

            }
        });
    }

    @Override
    public Object createMatchRuleBarChart(String categoryAxisLabel, String valueAxisLabel, Object dataset) {
        return TopChartFactory.createMatchRuleBarChart(categoryAxisLabel, valueAxisLabel, (CategoryDataset) dataset);
    }

    @Override
    public void refrechChart(Object chartComp, Object chart) {
        ((ChartComposite) chartComp).setChart((JFreeChart) chart);
        ((ChartComposite) chartComp).forceRedraw();
    }

    @Override
    public Object createDatasetForMatchRule(Map<Object, Long> groupSize2GroupFrequency, List<String> groups, int times,
            String items) {
        DefaultCategoryDataset defaultcategorydataset = new DefaultCategoryDataset();
        if (groups == null) {
            return defaultcategorydataset;
        }
        for (String count : groups) {
            if (Integer.parseInt(count) > times - 1) {
                defaultcategorydataset.addValue(groupSize2GroupFrequency.get(count), items, count);
            }
        }
        return defaultcategorydataset;
    }

    @Override
    public Object createBlockingBarChart(String title, Object dataset) {
        Object chart = TopChartFactory.createBlockingBarChart(title, (HistogramDataset) dataset);
        return chart;
    }

    @Override
    public Object createHistogramDataset(double[] valueArray, double maxValue, int bins) {
        HistogramDataset defaultcategorydataset = new HistogramDataset();
        if (valueArray == null) {
            return defaultcategorydataset;
        }
        defaultcategorydataset.addSeries("Key distribution", valueArray, bins, 0, maxValue); //$NON-NLS-1$
        return defaultcategorydataset;
    }

    @Override
    public Object createDatasetForDuplicateRecord(Map<String, Long> dupStats) {
        if (dupStats != null) {
            DefaultPieDataset dataset = new DefaultPieDataset();
            Iterator<String> iterator = dupStats.keySet().iterator();
            while (iterator.hasNext()) {
                String label = iterator.next();
                dataset.setValue(label, dupStats.get(label));
            }
            return dataset;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.service.ITOPChartService#createDuplicateRecordPieChart(java.lang.String,
     * java.lang.Object)
     */
    @Override
    public Object createDuplicateRecordPieChart(String title, Object dataset) {
        return TopChartFactory.createDuplicateRecordPieChart(title, (PieDataset) dataset, true, true, false);
    }

}
