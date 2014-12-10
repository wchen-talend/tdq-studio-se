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

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.talend.dataprofiler.common.ui.editor.preview.ICustomerDataset;
import org.talend.dataprofiler.core.ui.editor.preview.TableIndicatorUnit;

/**
 * DOC xqliu class global comment. Detailled comment
 */
public abstract class AbstractChartTypeStatesTable implements IChartTypeStates {

    protected List<TableIndicatorUnit> units = new ArrayList<TableIndicatorUnit>();

    public AbstractChartTypeStatesTable(List<TableIndicatorUnit> units) {
        // remove if executed check, TDQ-8787, for dynamic chart
        if (units != null) {
            this.units.addAll(units);
        }
    }

    public CategoryDataset getDataset() {
        if (getCustomerDataset() != null) {
            return (CategoryDataset) getCustomerDataset();
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

    public List<Object> getChartList() {
        return null;
    }

    public Object getChart() {
        return null;
    }
}
