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

    Object createMatchRuleBarChart(String categoryAxisLabel, String valueAxisLabel, Object dataset);

    Object createBlockingBarChart(String title, Object dataset);

    Object createDuplicateRecordPieChart(String title, Object dataset);

    void decorateChart(Object chart, boolean withPlot);

    void decorateColumnDependency(Object chart);

    void setOrientation(Object chart, boolean isHorizontal);

    void setDisplayDecimalFormatOfChart(Object chart);

    void addMouseListenerForChart(Object chartComposite, Map<String, Object> menuMap);

    void addListenerToChartComp(Object chartComp, final String referenceLink, final String menuText);

    void refrechChart(Object chartComp, Object chart);

    Object createDatasetForMatchRule(Map<Object, Long> groupSize2GroupFrequency, List<String> groups, int times, String items);

    Object createDatasetForDuplicateRecord(Map<String, Long> dupStats);

    Object createHistogramDataset(double[] valueArray, double maxValue, int bins);

    Object createDefaultCategoryDataset();

    Object createPieDataset(Map<String, Double> valueMap);

    Object createDefaultBoxAndWhiskerCategoryDataset(Double mean, Double median, Double q1, Double q3, Double minRegularValue,
            Double maxRegularValue);

    Object createXYDataset(Map<Integer, Double> valueMap);

    void addValueToCategoryDataset(Object dataset, double value, String labelX, String labelY);

    int getRowCount(Object dataset);

    int getColumnCount(Object dataset);

    Number getValue(Object dataset, int row, int column);

    Comparable getRowKey(Object dataset, int row);

    int getRowIndex(Object dataset, Comparable key);

    List getRowKeys(Object dataset);

    Comparable getColumnKey(Object dataset, int column);

    int getColumnIndex(Object dataset, Comparable key);

    List getColumnKeys(Object dataset);

    Number getValue(Object dataset, Comparable rowKey, Comparable columnKey);

    void setValue(Object dataset, Number value, Comparable rowKey, Comparable columnKey);

    void clearDataset(Object dataset);

    void clearDefaultBoxAndWhiskerCategoryDataset(Object dataset);
}
