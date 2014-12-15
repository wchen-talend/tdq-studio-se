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
package org.talend.dataquality.record.linkage.ui.composite.chart;

import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.talend.dataprofiler.service.ITOPChartService;
import org.talend.dq.CWMPlugin;

/**
 * created by yyin on 2014-12-11 Detailled comment
 * 
 */
public class TOPChartUtil {

    private static TOPChartUtil instance;

    private ITOPChartService chartService;

    public static TOPChartUtil getInstance() {
        if (instance == null) {
            instance = new TOPChartUtil();
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

    public Object createChartComposite(Object composite, int style, Object chart, boolean useBuffer) {
        if (chartService != null) {
            return chartService.createChartComposite(composite, style, chart, useBuffer);
        }
        return null;
    }

    public Object createMatchRuleBarChart(String categoryAxisLabel, String valueAxisLabel, Object dataset) {
        if (chartService != null) {
            return chartService.createMatchRuleBarChart(categoryAxisLabel, valueAxisLabel, dataset);
        }
        return null;
    }

    public void refrechChart(Object chartComp, Object chart) {
        if (chartService != null) {
            chartService.refrechChart(chartComp, chart);
        }
    }

    public Object createDatasetForMatchRule(Map<Object, Long> groupSize2GroupFrequency, List<String> groups, int times,
            String items) {
        if (chartService != null) {
            return chartService.createDatasetForMatchRule(groupSize2GroupFrequency, groups, times, items);
        }
        return null;
    }

    public Object createBlockingBarChart(String title, Object dataset) {
        if (chartService != null) {
            return chartService.createBlockingBarChart(title, dataset);
        }
        return null;
    }

    public Object createHistogramDataset(double[] valueArray, double maxValue, int bins) {
        if (chartService != null) {
            return chartService.createHistogramDataset(valueArray, maxValue, bins);
        }
        return null;
    }

    public Object createDuplicateRecordPieChart(String title, Object dataset) {
        if (chartService != null) {
            return chartService.createDuplicateRecordPieChart(title, dataset);
        }
        return null;
    }

    public Object createDatasetForDuplicateRecord(Map<String, Long> dupStats) {
        if (chartService != null) {
            return chartService.createDatasetForDuplicateRecord(dupStats);
        }
        return null;
    }

}
