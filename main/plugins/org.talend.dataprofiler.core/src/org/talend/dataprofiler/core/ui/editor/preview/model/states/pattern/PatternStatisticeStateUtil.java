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
package org.talend.dataprofiler.core.ui.editor.preview.model.states.pattern;

import org.talend.dataprofiler.core.ui.editor.preview.IndicatorUnit;
import org.talend.dq.analysis.explore.DataExplorer;
import org.talend.dq.analysis.explore.PatternExplorer;
import org.talend.dq.indicators.ext.PatternMatchingExt;
import org.talend.dq.indicators.preview.table.PatternChartDataEntity;

/**
 * created by yyin on 2014-12-3 Detailled comment
 * 
 */
public class PatternStatisticeStateUtil {

    /**
     * DOC yyin Comment method "createDataEntity".
     * 
     * @param unit
     * @param label
     * @param notMathCount
     * @param machCount
     * @return
     */
    public static PatternChartDataEntity createDataEntity(IndicatorUnit unit, String label, double notMathCount, double machCount) {
        PatternChartDataEntity patternEntity = new PatternChartDataEntity();
        patternEntity.setIndicator(unit.getIndicator());
        patternEntity.setLabel(label);
        patternEntity.setNumMatch(String.valueOf(machCount));
        patternEntity.setNumNoMatch(String.valueOf(notMathCount));
        return patternEntity;
    }

    /**
     * DOC yyin Comment method "getMatchCount".
     * 
     * @param patternExt
     * @return
     */
    public static double getMatchCount(PatternMatchingExt patternExt) {
        double machCount = patternExt == null ? Double.NaN : patternExt.getMatchingValueCount();
        return machCount;
    }

    /**
     * DOC yyin Comment method "getNotMatchCount".
     * 
     * @param patternExt
     * @return
     */
    public static double getNotMatchCount(PatternMatchingExt patternExt) {
        double notMathCount = patternExt == null ? Double.NaN : patternExt.getNotMatchingValueCount();
        return notMathCount;
    }

    public static DataExplorer getDataExplorer() {
        return new PatternExplorer();
    }
}
