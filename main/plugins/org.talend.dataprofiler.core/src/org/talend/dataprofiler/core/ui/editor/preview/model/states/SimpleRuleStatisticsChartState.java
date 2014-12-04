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

import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.talend.dataprofiler.common.ui.editor.preview.CustomerDefaultCategoryDataset;
import org.talend.dataprofiler.common.ui.editor.preview.ICustomerDataset;
import org.talend.dataprofiler.common.ui.editor.preview.chart.ChartDecorator;
import org.talend.dataprofiler.common.ui.editor.preview.chart.TopChartFactory;
import org.talend.dataprofiler.core.i18n.internal.DefaultMessagesImpl;
import org.talend.dataprofiler.core.ui.editor.preview.TableIndicatorUnit;
import org.talend.dataprofiler.core.ui.editor.preview.model.states.utils.CommonStateUtil;
import org.talend.dataprofiler.core.ui.editor.preview.model.states.utils.SimpleStatisticsStateUtil;
import org.talend.dq.analysis.explore.DataExplorer;
import org.talend.dq.indicators.preview.table.ChartDataEntity;

/**
 * DOC xqliu class global comment. Detailled comment
 */
public class SimpleRuleStatisticsChartState extends AbstractChartTypeStatesTable {

    public SimpleRuleStatisticsChartState(List<TableIndicatorUnit> units) {
        super(units);
    }

    @Override
    public JFreeChart getChart() {
        JFreeChart chart = TopChartFactory.createBarChart(
                DefaultMessagesImpl.getString("SimpleStatisticsStateTable.SimpleStatistics"), getDataset(), true); //$NON-NLS-1$
        ChartDecorator.decorate(chart, null);
        return chart;
    }

    public ICustomerDataset getCustomerDataset() {
        CustomerDefaultCategoryDataset customerdataset = new CustomerDefaultCategoryDataset();
        for (TableIndicatorUnit unit : units) {
            double value = Double.parseDouble(unit.getValue().toString());
            String label = unit.getIndicatorName();

            customerdataset.addValue(value, label, ""); //$NON-NLS-1$

            ChartDataEntity entity = CommonStateUtil.createDataEntity(unit, value, label);

            customerdataset.addDataEntity(entity);
        }
        return customerdataset;
    }

    public DataExplorer getDataExplorer() {
        return SimpleStatisticsStateUtil.getDataExplorer();
    }

    public JFreeChart getExampleChart() {
        return null;
    }

    public String getReferenceLink() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.dataprofiler.core.ui.editor.preview.model.states.IChartTypeStates#getChart(org.jfree.data.category
     * .CategoryDataset)
     */
    public JFreeChart getChart(CategoryDataset dataset) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.ui.editor.preview.model.states.IChartTypeStates#getChartList(java.util.List)
     */
    public List<JFreeChart> getChartList(List<DefaultCategoryDataset> datasets) {
        // TODO Auto-generated method stub
        return null;
    }
}
