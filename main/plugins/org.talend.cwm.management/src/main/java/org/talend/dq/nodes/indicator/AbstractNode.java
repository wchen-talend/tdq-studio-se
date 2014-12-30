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
package org.talend.dq.nodes.indicator;

import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.talend.dataquality.PluginConstant;
import org.talend.dataquality.indicators.Indicator;
import org.talend.dq.indicators.definitions.DefinitionHandler;
import org.talend.dq.nodes.indicator.type.IndicatorEnum;

/**
 * @author rli
 * 
 */
public abstract class AbstractNode implements IIndicatorNode {

    private IIndicatorNode parent;

    protected IIndicatorNode[] children;

    protected String label;

    protected IndicatorEnum indicatorEnum;

    protected Indicator indicatorInstance;

    public AbstractNode(IndicatorEnum indicatorEnum) {
        this.indicatorEnum = indicatorEnum;
    }

    public AbstractNode(String label) {
        this.label = label;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(IIndicatorNode parent) {
        this.parent = parent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.model.nodes.indicator.IIndicatorNode#getParent()
     */
    public IIndicatorNode getParent() {
        return parent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.model.nodes.indicator.IIndicatorNode#hasChildren()
     */
    public abstract boolean hasChildren();

    public String getLabel() {
        if (label == null) {
            return PluginConstant.EMPTY_STRING;
        } else {
            return label;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.model.nodes.indicator.IIndicatorNode#getIndicatorEnum()
     */
    public IndicatorEnum getIndicatorEnum() {
        return indicatorEnum;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dq.nodes.indicator.IIndicatorNode#setIndicatorEnum()
     */
    public void setIndicatorEnum(IndicatorEnum indicatorEnum) {
        this.indicatorEnum = indicatorEnum;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.model.nodes.indicator.IIndicatorNode#getIndicatorInstance()
     */
    public Indicator getIndicatorInstance() {
        if (indicatorInstance != null) {
            return indicatorInstance;
        } else if (this.indicatorEnum != null) {
            EFactoryImpl factory = (EFactoryImpl) indicatorEnum.getIndicatorType().getEPackage().getEFactoryInstance();
            indicatorInstance = (Indicator) factory.create(indicatorEnum.getIndicatorType());
            DefinitionHandler.getInstance().setDefaultIndicatorDefinition(indicatorInstance);
            return indicatorInstance;
        } else {
            return null;
        }
    }

}
