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
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.xy.XYDataset;
import org.talend.dataprofiler.common.ui.editor.preview.ICustomerDataset;
import org.talend.dataprofiler.common.ui.editor.preview.chart.TopChartFactory;
import org.talend.dataprofiler.core.ui.editor.preview.IndicatorUnit;
import org.talend.dq.indicators.preview.table.ChartDataEntity;

/**
 * DOC Zqin class global comment. Detailled comment
 */
public abstract class AbstractChartTypeStates implements IChartTypeStates {

    protected List<IndicatorUnit> units = new ArrayList<IndicatorUnit>();

    public AbstractChartTypeStates() {

    }

    public AbstractChartTypeStates(List<IndicatorUnit> units) {

        if (units != null) {
            this.units.addAll(units);
        }
    }

    public List<JFreeChart> getChartList() {
        return null;
    }

    public CategoryDataset getDataset() {
        ICustomerDataset customerDataset = getCustomerDataset();
        if (customerDataset != null) {
            return (CategoryDataset) customerDataset;
        }

        return null;
    }

    public ChartDataEntity[] getDataEntity() {
        ICustomerDataset customerDataset = getCustomerDataset();
        if (customerDataset != null) {
            return customerDataset.getDataEntities();
        }

        return null;
    }

    public XYDataset getXYDataset() {
        if (getCustomerXYDataset() != null) {
            return (XYDataset) getCustomerXYDataset();
        }

        return null;
    }

    public ICustomerDataset getCustomerXYDataset() {
        return null;
    }

    public PieDataset getPieDataset() {
        return null;
    }

    public JFreeChart getChart(CategoryDataset dataset) {
        return TopChartFactory.createBarChart(StringUtils.EMPTY, dataset, false);
    }

    public List<JFreeChart> getChartList(List<DefaultCategoryDataset> datasets) {
        return null;
    }
}
