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
package org.talend.dataprofiler.core.ui.editor.preview.model.states;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerItem;
import org.talend.dataprofiler.common.ui.editor.preview.CustomerDefaultCategoryDataset;
import org.talend.dataprofiler.common.ui.editor.preview.ICustomerDataset;
import org.talend.dataprofiler.common.ui.editor.preview.chart.ChartDatasetUtils;
import org.talend.dataprofiler.common.ui.editor.preview.chart.ChartDecorator;
import org.talend.dataprofiler.common.ui.editor.preview.chart.TopChartFactory;
import org.talend.dataprofiler.core.i18n.internal.DefaultMessagesImpl;
import org.talend.dataprofiler.core.ui.editor.preview.IndicatorUnit;
import org.talend.dataprofiler.core.ui.editor.preview.model.dataset.CustomerDefaultBAWDataset;
import org.talend.dataprofiler.core.ui.editor.preview.model.states.utils.SummaryStatisticsStateUtil;
import org.talend.dq.analysis.explore.DataExplorer;
import org.talend.dq.indicators.preview.table.ChartDataEntity;
import org.talend.dq.nodes.indicator.type.IndicatorEnum;
import org.talend.utils.sql.Java2SqlType;

/**
 * DOC Zqin class global comment. Detailled comment
 */
public class SummaryStatisticsState extends AbstractChartTypeStates {

    private static Logger log = Logger.getLogger(SummaryStatisticsState.class);

    // when all the summary indicators is selected, include the catalog, the number should be 8
    public static final int FULL_FLAG = 8;

    public static final int FULL_CHART = 6;

    private int sqltype;

    /**
     * Sets the sqltype.
     * 
     * @param sqltype the sqltype to set
     */
    public void setSqltype(int sqltype) {
        this.sqltype = sqltype;
    }

    public SummaryStatisticsState(List<IndicatorUnit> units) {
        if (units != null) {
            this.units.addAll(SummaryStatisticsStateUtil.check(units));
        }

        sqltype = SummaryStatisticsStateUtil.findSqlType(units);
    }

    public JFreeChart getChart() {
        if (Java2SqlType.isDateInSQL(sqltype)) {
            return null;
        } else {
            if (isIntact()) {
                BoxAndWhiskerCategoryDataset dataset = (BoxAndWhiskerCategoryDataset) getDataset();
                return TopChartFactory.createBoxAndWhiskerChart(
                        DefaultMessagesImpl.getString("SummaryStatisticsState.SummaryStatistics"), dataset); //$NON-NLS-1$
            } else {
                JFreeChart barChart = TopChartFactory.createBarChart(
                        DefaultMessagesImpl.getString("SummaryStatisticsState.Summary_Statistics"), getDataset(), false); //$NON-NLS-1$
                ChartDecorator.setDisplayDecimalFormat(barChart);
                return barChart;
            }
        }
    }

    @Override
    public JFreeChart getChart(CategoryDataset dataset) {
        if (Java2SqlType.isDateInSQL(sqltype)) {
            return null;
        } else {
            if (isIntact()) {
                BoxAndWhiskerCategoryDataset dataset2 = (BoxAndWhiskerCategoryDataset) getDataset();
                return TopChartFactory.createBoxAndWhiskerChart(
                        DefaultMessagesImpl.getString("SummaryStatisticsState.SummaryStatistics"), dataset2); //$NON-NLS-1$
            } else {
                JFreeChart barChart = TopChartFactory.createBarChart(
                        DefaultMessagesImpl.getString("SummaryStatisticsState.Summary_Statistics"), dataset, false); //$NON-NLS-1$
                ChartDecorator.setDisplayDecimalFormat(barChart);
                return barChart;
            }
        }
    }

    public ICustomerDataset getCustomerDataset() {
        Map<IndicatorEnum, Double> map = new HashMap<IndicatorEnum, Double>();
        CustomerDefaultCategoryDataset customerdataset = new CustomerDefaultCategoryDataset();
        for (IndicatorUnit unit : units) {
            // MOD xqliu 2009-06-29 bug 7068
            String value = SummaryStatisticsStateUtil.getUnitValue(unit);
            if (Java2SqlType.isNumbericInSQL(sqltype)) {
                try {
                    map.put(unit.getType(), Double.parseDouble(value));
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

            ChartDataEntity entity = SummaryStatisticsStateUtil.createDataEntity(unit, value);

            customerdataset.addDataEntity(entity);
        }

        if (isIntact()) {
            CustomerDefaultBAWDataset dataset = new CustomerDefaultBAWDataset();
            BoxAndWhiskerItem item = ChartDatasetUtils.createBoxAndWhiskerItem(map.get(IndicatorEnum.MeanIndicatorEnum),
                    map.get(IndicatorEnum.MedianIndicatorEnum), map.get(IndicatorEnum.LowerQuartileIndicatorEnum),
                    map.get(IndicatorEnum.UpperQuartileIndicatorEnum), map.get(IndicatorEnum.MinValueIndicatorEnum),
                    map.get(IndicatorEnum.MaxValueIndicatorEnum), null);

            dataset.add(item, "0", ""); //$NON-NLS-1$ //$NON-NLS-2$

            @SuppressWarnings("rawtypes")
            List zerolist = new ArrayList();
            dataset.add(zerolist, "1", ""); //$NON-NLS-1$ //$NON-NLS-2$
            dataset.add(zerolist, "2", ""); //$NON-NLS-1$ //$NON-NLS-2$
            dataset.add(zerolist, "3", ""); //$NON-NLS-1$ //$NON-NLS-2$
            dataset.add(zerolist, "4", ""); //$NON-NLS-1$ //$NON-NLS-2$
            dataset.add(zerolist, "5", ""); //$NON-NLS-1$ //$NON-NLS-2$
            dataset.add(zerolist, "6", ""); //$NON-NLS-1$ //$NON-NLS-2$

            dataset.addDataEntity(customerdataset.getDataEntities());
            return dataset;
        } else {
            // MOD hcheng,Range indicator value should not appear in bar chart
            map.remove(IndicatorEnum.RangeIndicatorEnum);
            map.remove(IndicatorEnum.IQRIndicatorEnum);

            for (IndicatorEnum indicatorEnum : map.keySet()) {
                customerdataset.addValue(map.get(indicatorEnum), indicatorEnum.getLabel(), indicatorEnum.getLabel());
            }
            return customerdataset;
        }
    }

    public DataExplorer getDataExplorer() {
        return SummaryStatisticsStateUtil.getDataExplorer(sqltype);
    }

    public JFreeChart getExampleChart() {
        // TODO Auto-generated method stub
        return null;
    }

    private boolean isIntact() {
        return units.size() == FULL_FLAG && SummaryStatisticsStateUtil.isMeaning();
    }

    public String getReferenceLink() {
        String url = null;

        if (getDataset() instanceof BoxAndWhiskerCategoryDataset) {
            url = "http://en.wikipedia.org/wiki/Box_plot"; //$NON-NLS-1$
        } else {
            url = "http://en.wikipedia.org/wiki/Histogram"; //$NON-NLS-1$
        }
        return url;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.dataprofiler.core.ui.editor.preview.model.states.IChartTypeStates#getChart(org.talend.dataprofiler
     * .common.ui.editor.preview.ICustomerDataset)
     */
    public JFreeChart getChart(ICustomerDataset dataset) {
        return null;
    }
}
