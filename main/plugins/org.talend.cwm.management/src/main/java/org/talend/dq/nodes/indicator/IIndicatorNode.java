// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dq.nodes.indicator;

import org.talend.dataquality.indicators.Indicator;
import org.talend.dq.nodes.indicator.type.IndicatorEnum;

/**
 * @author rli
 * 
 */
public interface IIndicatorNode {

    public IIndicatorNode[] getChildren();

    public boolean hasChildren();

    public IIndicatorNode getParent();

    public void setParent(IIndicatorNode indicatorNode);

    /**
     * @return the indicatorFieldEnum
     */
    public IndicatorEnum getIndicatorEnum();

    public void setIndicatorEnum(IndicatorEnum indicatorEnum);

    public Indicator getIndicatorInstance();

    public String getLabel();

    public boolean isIndicatorEnumNode();

    public String getImageName();

}
