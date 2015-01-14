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
package org.talend.dq.analysis;

import org.eclipse.emf.common.util.EList;
import orgomg.cwm.objectmodel.core.ModelElement;

/**
 * created by talend on Jan 4, 2015 Detailled comment
 *
 */
public class ColumnAnalysisHandler extends ModelElementAnalysisHandler {

    public ModelElement[] getSelectedColumns() {
        EList<ModelElement> analyzedColumns = getAnalyzedColumns();
        ModelElement[] selectedColumns = new ModelElement[analyzedColumns.size()];
        int i = 0;
        for (ModelElement element : analyzedColumns) {
            selectedColumns[i++] = element;
        }
        return selectedColumns;
    }

}
