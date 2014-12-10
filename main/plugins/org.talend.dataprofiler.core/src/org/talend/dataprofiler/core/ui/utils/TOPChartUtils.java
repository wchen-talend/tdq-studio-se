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
package org.talend.dataprofiler.core.ui.utils;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.talend.dataprofiler.service.ITOPChartService;
import org.talend.dq.CWMPlugin;

/**
 * created by yyin on 2014-12-8 Detailled comment
 * 
 */
public class TOPChartUtils {

    private static Logger log = Logger.getLogger(TOPChartUtils.class);

    private static TOPChartUtils instance;

    private ITOPChartService chartService;

    public static TOPChartUtils getInstance() {
        if (instance == null) {
            instance = new TOPChartUtils();
        }
        return instance;
    }

    public boolean isTOPChartInstalled() {
        initTOPChartService(false);
        return this.chartService != null;
    }

    /**
     * DOC yyin Comment method "initTOPChartService".
     * 
     * @param b
     */
    private void initTOPChartService(boolean b) {
        if (this.chartService == null) {
            BundleContext context = CWMPlugin.getDefault().getBundleContext();
            if (context == null) {
                return;
            }

            ServiceReference serviceReference = context.getServiceReference(ITOPChartService.class.getName());
            if (serviceReference != null) {
                Object obj = context.getService(serviceReference);
                if (obj != null) {
                    this.chartService = (ITOPChartService) obj;
                }
            }
        }
    }

    public Object createBarChart(String title, Object dataset, boolean showLegend) {
        if (this.chartService != null) {
            return chartService.createBarChart(title, dataset, showLegend);
        }
        return null;
    }

    public Object createBarChart(String title, Object dataset) {
        if (this.chartService != null) {
            return chartService.createBarChart(title, dataset);
        }
        return null;
    }

    public Object createBenfordChart(String axisXLabel, String categoryAxisLabel, Object dataset, List<String> dotChartLabels,
            double[] formalValues, String title) {
        if (chartService != null) {
            return chartService.createBenfordChart(axisXLabel, categoryAxisLabel, dataset, dotChartLabels, formalValues, title);
        }
        return null;
    }

    public Object createTalendChartComposite(Object parentComponent, int style, Object chart, boolean useBuffer) {
        if (chartService != null) {
            return chartService.createTalendChartComposite(parentComponent, style, chart, useBuffer);
        }
        return null;
    }

    public Object createChartComposite(Object composite, int style, Object chart, boolean useBuffer) {
        if (chartService != null) {
            return chartService.createChartComposite(composite, style, chart, useBuffer);
        }
        return null;
    }

    public Object getDatasetFromChart(Object chart, int datasetIndex) {
        if (chartService != null) {
            return chartService.getDatasetFromChart(chart, datasetIndex);
        }
        return null;
    }

    public void decorateChart(Object chart, boolean withPlot) {
        if (chartService != null) {
            chartService.decorateChart(chart, withPlot);
        }
    }

    public void setOrientation(Object chart, boolean isHorizontal) {
        if (chartService != null) {
            chartService.setOrientation(chart, isHorizontal);
        }
    }

    public void setDisplayDecimalFormatOfChart(Object chart) {
        if (chartService != null) {
            chartService.setDisplayDecimalFormatOfChart(chart);
        }
    }

    public void addMouseListenerForChart(Object chartComposite, final Map<String, Object> menuMap) {
        if (chartService != null) {
            chartService.addMouseListenerForChart(chartComposite, menuMap);
        }
    }

    public Object createPieChart(String title, Object dataset, boolean showLegend, boolean toolTips, boolean urls) {
        if (chartService != null) {
            return chartService.createPieChart(title, dataset, showLegend, toolTips, urls);
        }
        return null;
    }

}
