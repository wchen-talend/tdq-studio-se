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
package org.talend.dq.analysis.parameters;

/**
 * DOC xqliu class global comment. Detailled comment
 */
public class UDIndicatorParameter extends ConnectionParameter {

    private String expression;

    private String language;

    public UDIndicatorParameter() {
        super(EParameterType.UDINDICATOR);
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getExpression() {
        return expression;
    }

    public String getLanguage() {
        return language;
    }
}
