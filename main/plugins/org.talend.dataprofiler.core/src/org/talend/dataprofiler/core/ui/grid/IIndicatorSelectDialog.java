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
package org.talend.dataprofiler.core.ui.grid;

import org.eclipse.nebula.widgets.grid.GridItem;
import org.talend.dataprofiler.core.model.ModelElementIndicator;
import org.talend.dq.nodes.indicator.IIndicatorNode;

/**
 * created by talend on Dec 25, 2014 Detailled comment
 * 
 */
public interface IIndicatorSelectDialog {

    public void updateIndicatorInfo(GridItem item);

    boolean isMatchCurrentIndicator(ModelElementIndicator currentIndicator, IIndicatorNode indicatorNode);
}
