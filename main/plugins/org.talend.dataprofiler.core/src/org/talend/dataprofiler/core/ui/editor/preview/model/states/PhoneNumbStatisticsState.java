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
import org.talend.dataprofiler.common.ui.editor.preview.CustomerDefaultCategoryDataset;
import org.talend.dataprofiler.common.ui.editor.preview.ICustomerDataset;
import org.talend.dataprofiler.common.ui.editor.preview.chart.TopChartFactory;
import org.talend.dataprofiler.core.i18n.internal.DefaultMessagesImpl;
import org.talend.dataprofiler.core.ui.editor.preview.IndicatorUnit;
import org.talend.dataprofiler.core.ui.editor.preview.model.states.utils.CommonStateUtil;
import org.talend.dataprofiler.core.ui.editor.preview.model.states.utils.PhoneNumbStatisticsStateUtil;
import org.talend.dq.analysis.explore.DataExplorer;
import org.talend.dq.indicators.preview.table.ChartDataEntity;

/**
 * DOC qiongli class global comment. Detailled comment
 */
public class PhoneNumbStatisticsState extends AbstractChartTypeStates {

    /**
     * DOC qiongli PhoneNumbStatisticsState constructor comment.
     * 
     * @param units
     */
    public PhoneNumbStatisticsState(List<IndicatorUnit> units) {
        super(units);
        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Jsdoc)
     * 
     * @see org.talend.dataprofiler.core.ui.editor.preview.model.states.IChartTypeStates#getCustomerDataset()
     */
    public ICustomerDataset getCustomerDataset() {

        CustomerDefaultCategoryDataset customerdataset = new CustomerDefaultCategoryDataset();
        for (IndicatorUnit unit : units) {
            double value = CommonStateUtil.getUnitValue(unit.getValue());
            String label = unit.getIndicatorName();

            customerdataset.addValue(value, label, label);

            ChartDataEntity entity = PhoneNumbStatisticsStateUtil.createDataEntity(unit.getIndicator(), value, label);

            customerdataset.addDataEntity(entity);
        }
        return customerdataset;

    }

    /*
     * (non-Jsdoc)
     * 
     * @see org.talend.dataprofiler.core.ui.editor.preview.model.states.IChartTypeStates#getChart()
     */
    public JFreeChart getChart() {
        return TopChartFactory.createBarChart(
                DefaultMessagesImpl.getString("PhoneNumbStatisticsState.PhoneNumbStatistics"), getDataset(), false); //$NON-NLS-1$
    }

    /*
     * (non-Jsdoc)
     * 
     * @see org.talend.dataprofiler.core.ui.editor.preview.model.states.IChartTypeStates#getExampleChart()
     */
    public JFreeChart getExampleChart() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Jsdoc)
     * 
     * @see org.talend.dataprofiler.core.ui.editor.preview.model.states.IChartTypeStates#getDataExplorer()
     */
    public DataExplorer getDataExplorer() {
        return PhoneNumbStatisticsStateUtil.getDataExplorer();
    }

    /*
     * (non-Jsdoc)
     * 
     * @see org.talend.dataprofiler.core.ui.editor.preview.model.states.IChartTypeStates#getReferenceLink()
     */
    public String getReferenceLink() {
        // TODO Auto-generated method stub
        return null;
    }

}
