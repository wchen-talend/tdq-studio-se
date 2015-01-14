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

import org.eclipse.jface.window.IShellProvider;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.talend.dataprofiler.core.model.ModelElementIndicator;
import org.talend.dq.nodes.indicator.IIndicatorNode;

/**
 * created by talend on Dec 25, 2014 Detailled comment
 * 
 */
public interface IIndicatorSelectDialog extends IShellProvider {

    public void updateIndicatorInfo(GridItem item);

    public Control getDialogControl();

    public Composite getDialogComposite();

    public String getWhereExpression();

    public boolean isMatchCurrentIndicator(ModelElementIndicator currentIndicator, IIndicatorNode indicatorNode);
}
