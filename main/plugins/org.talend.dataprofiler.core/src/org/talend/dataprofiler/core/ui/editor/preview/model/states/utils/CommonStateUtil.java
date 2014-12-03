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
package org.talend.dataprofiler.core.ui.editor.preview.model.states.utils;

import java.util.ArrayList;
import java.util.List;

import org.talend.dataprofiler.core.ui.editor.preview.IndicatorUnit;
import org.talend.dq.indicators.preview.table.ChartDataEntity;

/**
 * created by yyin on 2014-12-2 Detailled comment
 * 
 */
public class CommonStateUtil {

    public static double getUnitValue(Object unitValue) {
        return unitValue != null ? Double.parseDouble(unitValue.toString()) : Double.NaN;
    }

    /**
     * create the data entity
     * 
     * @param unit
     * @param value
     * @param label
     * @return
     */
    public static ChartDataEntity createDataEntity(IndicatorUnit unit, double value, String label) {
        ChartDataEntity entity = new ChartDataEntity();
        entity.setIndicator(unit.getIndicator());
        entity.setLabel(label);
        entity.setValue(String.valueOf(value));
        entity.setPercent(value / unit.getIndicator().getCount());
        return entity;
    }

    public static ChartDataEntity[] getDataEntity(List<IndicatorUnit> units) {
        List<ChartDataEntity> dataEnities = new ArrayList<ChartDataEntity>();

        for (IndicatorUnit unit : units) {
            double value = CommonStateUtil.getUnitValue(unit.getValue());
            ChartDataEntity entity = CommonStateUtil.createDataEntity(unit, value, unit.getIndicatorName());
            dataEnities.add(entity);
        }

        return dataEnities.toArray(new ChartDataEntity[dataEnities.size()]);
    }
}
