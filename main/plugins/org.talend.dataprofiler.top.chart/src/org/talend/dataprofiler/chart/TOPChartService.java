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
import java.util.List;
import java.util.Map;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.experimental.chart.swt.ChartComposite;
import org.talend.dataprofiler.chart.util.ChartDecorator;
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

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.service.ITOPChartService#getDatasetFromChart(java.lang.Object, int)
     */
    @Override
    public Object getDatasetFromChart(Object chart, int datasetIndex) {
        if (datasetIndex > -1) {
            return ((JFreeChart) chart).getCategoryPlot().getDataset(datasetIndex);
        }
        return ((JFreeChart) chart).getCategoryPlot().getDataset();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.service.ITOPChartService#createTalendChartComposite(java.lang.Object, int,
     * java.lang.Object, boolean)
     */
    @Override
    public Object createTalendChartComposite(Object parentComponent, int style, Object chart, boolean useBuffer) {
        ChartComposite cc = new TalendChartComposite((Composite) parentComponent, style, (JFreeChart) chart, useBuffer);

        GridData gd = new GridData();
        gd.widthHint = CHART_STANDARD_WIDHT;
        gd.heightHint = CHART_STANDARD_HEIGHT;
        cc.setLayoutData(gd);
        return cc;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.service.ITOPChartService#getChart()
     */
    @Override
    public Object createBarChart(String title, Object dataset, boolean showLegend) {
        return TopChartFactory.createBarChart(title, (CategoryDataset) dataset, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.service.ITOPChartService#getChart(java.lang.Object)
     */
    @Override
    public Object createBarChart(Object dataset) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.service.ITOPChartService#createBarChart(java.lang.String, java.lang.Object)
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.service.ITOPChartService#decorateChart(java.lang.Object, boolean)
     */
    @Override
    public void decorateChart(Object chart, boolean withPlot) {
        if (withPlot) {
            ChartDecorator.decorate((JFreeChart) chart, PlotOrientation.HORIZONTAL);
        } else {
            ChartDecorator.decorate((JFreeChart) chart, null);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.service.ITOPChartService#createChartComposite(java.lang.Object, int,
     * java.lang.Object, boolean)
     */
    @Override
    public Object createChartComposite(Object composite, int style, Object chart, boolean useBuffer) {
        ChartComposite cc = new ChartComposite((Composite) composite, style, (JFreeChart) chart, useBuffer);

        GridData gd = new GridData();
        gd.widthHint = CHART_STANDARD_WIDHT;
        gd.heightHint = CHART_STANDARD_HEIGHT;
        cc.setLayoutData(gd);
        return cc;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.service.ITOPChartService#setOrientation(boolean)
     */
    @Override
    public void setOrientation(Object chart, boolean isHorizontal) {
        if (isHorizontal) {
            ((JFreeChart) chart).getCategoryPlot().setOrientation(PlotOrientation.HORIZONTAL);
        } else {
            ((JFreeChart) chart).getCategoryPlot().setOrientation(PlotOrientation.VERTICAL);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.service.ITOPChartService#setDisplayDecimalFormatOfChart(java.lang.Object)
     */
    @Override
    public void setDisplayDecimalFormatOfChart(Object chart) {
        ChartDecorator.setDisplayDecimalFormat((JFreeChart) chart);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.service.ITOPChartService#addMouseListenerForChart(java.lang.Object, java.util.Map)
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.service.ITOPChartService#createPieChart(java.lang.String, java.lang.Object, boolean,
     * boolean, boolean)
     */
    @Override
    public Object createPieChart(String title, Object dataset, boolean showLegend, boolean toolTips, boolean urls) {
        return TopChartFactory.createPieChart(title, (PieDataset) dataset, showLegend, toolTips, urls);
    }
}
