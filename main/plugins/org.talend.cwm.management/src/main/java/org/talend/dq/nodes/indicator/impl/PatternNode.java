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
package org.talend.dq.nodes.indicator.impl;

import org.talend.dq.nodes.indicator.AbstractNode;
import org.talend.dq.nodes.indicator.IIndicatorNode;

/**
 * created by talend on Dec 30, 2014 Detailled comment
 * 
 */
public class PatternNode extends AbstractNode {

    /**
     * DOC talend PatternNode constructor comment.
     * 
     * @param label
     */
    public PatternNode(String label) {
        super(label);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dq.nodes.indicator.IIndicatorNode#getChildren()
     */
    public IIndicatorNode[] getChildren() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dq.nodes.indicator.IIndicatorNode#isIndicatorEnumNode()
     */
    public boolean isIndicatorEnumNode() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dq.nodes.indicator.AbstractIndicatorNode#hasChildren()
     */
    @Override
    public boolean hasChildren() {
        return false;
    }

}
