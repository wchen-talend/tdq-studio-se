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

import org.talend.dataquality.indicators.Indicator;
import org.talend.dq.analysis.explore.DataExplorer;
import org.talend.dq.analysis.explore.PhoneNumbStatisticsExplorer;
import org.talend.dq.indicators.preview.table.ChartDataEntity;

/**
 * created by yyin on 2014-12-2 Detailled comment
 * 
 */
public class PhoneNumbStatisticsStateUtil {

    public static DataExplorer getDataExplorer() {
        return new PhoneNumbStatisticsExplorer();
    }

    /**
     * DOC yyin Comment method "createDataEntity".
     * 
     * @param unit
     * @param value
     * @param label
     * @return
     */
    public static ChartDataEntity createDataEntity(Indicator indicator, double value, String label) {
        ChartDataEntity entity = new ChartDataEntity();
        entity.setIndicator(indicator);
        entity.setLabel(label);
        entity.setValue(String.valueOf(value));
        entity.setPercent(value / indicator.getCount());
        return entity;
    }

}
