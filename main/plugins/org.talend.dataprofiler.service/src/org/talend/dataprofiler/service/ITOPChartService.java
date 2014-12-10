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
package org.talend.dataprofiler.service;

import java.util.List;
import java.util.Map;

/**
 * created by yyin on 2014-11-28 Detailled comment
 * 
 */
public interface ITOPChartService {

    Object getDatasetFromChart(Object chart, int datasetIndex);

    Object createTalendChartComposite(Object composite, int style, Object chart, boolean useBuffer);

    Object createChartComposite(Object composite, int style, Object chart, boolean useBuffer);

    Object createBarChart(String title, Object dataset, boolean showLegend);

    Object createBarChart(Object dataset);

    Object createBarChart(String title, Object dataset);

    Object createPieChart(String title, Object dataset, boolean showLegend, boolean toolTips, boolean urls);

    Object createBenfordChart(String axisXLabel, String categoryAxisLabel, Object dataset, List<String> dotChartLabels,
            double[] formalValues, String title);

    Object createStackedBarChart(String title, Object dataset, boolean showLegend);

    Object createStackedBarChart(String title, Object dataset, boolean isHorizatal, boolean showLegend);

    Object createBoxAndWhiskerChart(String title, Object dataset);

    void decorateChart(Object chart, boolean withPlot);

    void setOrientation(Object chart, boolean isHorizontal);

    void setDisplayDecimalFormatOfChart(Object chart);

    void addMouseListenerForChart(Object chartComposite, Map<String, Object> menuMap);

    void addListenerToChartComp(Object chartComp, final String referenceLink, final String menuText);
}
