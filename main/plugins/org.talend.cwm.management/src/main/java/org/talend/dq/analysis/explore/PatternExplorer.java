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
package org.talend.dq.analysis.explore;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.talend.cwm.relational.TdExpression;
import org.talend.dataquality.PluginConstant;
import org.talend.dataquality.analysis.ExecutionLanguage;
import org.talend.dataquality.indicators.columnset.ColumnsetPackage;
import org.talend.dataquality.indicators.definition.IndicatorDefinition;
import org.talend.dataquality.indicators.definition.userdefine.UDIndicatorDefinition;
import org.talend.dq.dbms.DbmsLanguage;
import org.talend.dq.dbms.GenericSQLHandler;
import orgomg.cwm.objectmodel.core.Expression;
import orgomg.cwm.objectmodel.core.ModelElement;

/**
 * @author scorreia
 * 
 * This class explores the data that matched or did not match a pattern indicator.
 */
public class PatternExplorer extends DataExplorer {

    /**
     * DOC scorreia PatternExplorer constructor comment.
     * 
     */
    public PatternExplorer() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dq.analysis.explore.IDataExplorer#getQueryMap()
     */
    public Map<String, String> getQueryMap() {
        Map<String, String> map = new HashMap<String, String>();// MOD zshen feature 12919 adapt to pop-menu for Jave
        // MOD qiongli 2011-3-30,feature 19192,filter this menu and query for ColumnSet AllMatchIndicator
        if (ColumnsetPackage.eINSTANCE.getAllMatchIndicator().isSuperTypeOf(indicator.eClass())) {
            return map;
        }
        // engin on result page
        boolean isSqlEngine = ExecutionLanguage.SQL.equals(analysis.getParameters().getExecutionLanguage());
        // MOD gdbu 2011-12-5 TDQ-4087 get function name from sql sentence when use MSSQL
        EList<Expression> instantiatedExpressions = indicator.getInstantiatedExpressions();
        if (instantiatedExpressions.size() > 0) {
            Expression expression = instantiatedExpressions.get(0);
            String regexp = dbmsLanguage.getRegexPatternString(indicator);
            dbmsLanguage.setRegularExpressionFunction(dbmsLanguage.extractRegularExpressionFunction(expression, regexp));
            dbmsLanguage.setFunctionReturnValue(dbmsLanguage.extractRegularExpressionFunctionReturnValue(expression, regexp));
        }
        // ~TDQ-4087

        // MOD zshen 10448 Add menus "view invalid values" and "view valid values" on pattern matching indicator
        map.put(MENU_VIEW_INVALID_VALUES, isSqlEngine ? getComment(MENU_VIEW_INVALID_VALUES) + getInvalidValuesStatement() : null);
        map.put(MENU_VIEW_VALID_VALUES, isSqlEngine ? getComment(MENU_VIEW_VALID_VALUES) + getValidValuesStatement() : null);
        // ~10448
        map.put(MENU_VIEW_INVALID_ROWS, isSqlEngine ? getComment(MENU_VIEW_INVALID_ROWS) + getInvalidRowsStatement() : null);
        map.put(MENU_VIEW_VALID_ROWS, isSqlEngine ? getComment(MENU_VIEW_VALID_ROWS) + getValidRowsStatement() : null);

        return map;
    }

    /**
     * 
     * get the Valid Values Statement.
     * 
     * @return SELECT statement for the invalid Value of select column
     */
    public String getInvalidValuesStatement() {
        // when the indicator is the use define match
        IndicatorDefinition indicatorDefinition = this.indicator.getIndicatorDefinition();
        if (indicatorDefinition instanceof UDIndicatorDefinition) {
            EList<TdExpression> list = ((UDIndicatorDefinition) indicatorDefinition).getViewInvalidValuesExpression();
            return getQueryAfterReplaced(indicatorDefinition, list);
        }

        String regexPatternString = dbmsLanguage.getRegexPatternString(this.indicator);
        String regexCmp = getRegexNotLike(regexPatternString) + dbmsLanguage.getFunctionReturnValue();
        // add null as invalid rows
        String nullClause = dbmsLanguage.or() + columnName + dbmsLanguage.isNull();
        // mzhao TDQ-4967 add "(" and ")" for regex and null clause.
        String pattCondStr = "(" + regexCmp + nullClause + ")";//$NON-NLS-1$//$NON-NLS-2$
        return getValuesStatement(columnName, pattCondStr);
    }

    protected String getRegexNotLike(String regexPatternString) {
        return dbmsLanguage.regexNotLike(columnName, regexPatternString);
    }

    /**
     * 
     * get the Valid Values Statement.
     * 
     * @return SELECT statement for the valid Value of select column
     */
    public String getValidValuesStatement() {
        // when the indicator is the use define match
        IndicatorDefinition indicatorDefinition = this.indicator.getIndicatorDefinition();
        if (indicatorDefinition instanceof UDIndicatorDefinition) {
            EList<TdExpression> list = ((UDIndicatorDefinition) indicatorDefinition).getViewValidValuesExpression();
            return getQueryAfterReplaced(indicatorDefinition, list);
        }

        String regexPatternString = dbmsLanguage.getRegexPatternString(this.indicator);
        String regexCmp = getRegexLike(regexPatternString) + dbmsLanguage.getFunctionReturnValue();
        return getValuesStatement(columnName, regexCmp);
    }

    protected String getRegexLike(String regexPatternString) {
        return dbmsLanguage.regexLike(columnName, regexPatternString);
    }

    /**
     * get the Invalid Rows Statement.
     * 
     * @return
     */
    public String getInvalidRowsStatement() {
        // when the indicator is the use define match
        IndicatorDefinition indicatorDefinition = this.indicator.getIndicatorDefinition();
        if (indicatorDefinition instanceof UDIndicatorDefinition) {
            EList<TdExpression> list = ((UDIndicatorDefinition) indicatorDefinition).getViewInvalidRowsExpression();
            return getQueryAfterReplaced(indicatorDefinition, list);
        }

        String regexPatternString = dbmsLanguage.getRegexPatternString(this.indicator);
        String regexCmp = getRegexNotLike(regexPatternString) + dbmsLanguage.getFunctionReturnValue();
        // add null as invalid rows
        String nullClause = dbmsLanguage.or() + columnName + dbmsLanguage.isNull();
        // mzhao TDQ-4967 add "(" and ")" for regex and null clause.
        String pattCondStr = "(" + regexCmp + nullClause + ")";//$NON-NLS-1$//$NON-NLS-2$
        return getRowsStatement(pattCondStr);
    }

    /**
     * get the Valid Rows Statement.
     * 
     * @return
     */
    public String getValidRowsStatement() {
        // when the indicator is the use define match
        IndicatorDefinition indicatorDefinition = this.indicator.getIndicatorDefinition();
        if (indicatorDefinition instanceof UDIndicatorDefinition) {
            EList<TdExpression> list = ((UDIndicatorDefinition) indicatorDefinition).getViewValidRowsExpression();
            return getQueryAfterReplaced(indicatorDefinition, list);
        }

        String regexPatternString = dbmsLanguage.getRegexPatternString(this.indicator);
        String regexCmp = getRegexLike(regexPatternString) + dbmsLanguage.getFunctionReturnValue();
        return getRowsStatement(regexCmp);
    }

    /**
     * get the Query After Replaced.
     * 
     * @param indicatorDefinition
     * @param list
     * @return String
     */
    public String getQueryAfterReplaced(IndicatorDefinition indicatorDefinition, EList<TdExpression> list) {
        String sql = PluginConstant.EMPTY_STRING;
        TdExpression tdExp = DbmsLanguage.getSqlExpression(indicatorDefinition, dbmsLanguage.getDbmsName(), list,
                dbmsLanguage.getDbVersion());
        sql = tdExp.getBody();
        String dataFilterClause = getDataFilterClause();
        if (!dataFilterClause.equals(PluginConstant.EMPTY_STRING)) {
            sql = sql.replace(GenericSQLHandler.WHERE_CLAUSE, dbmsLanguage.where() + "(" + dataFilterClause + ")"); //$NON-NLS-1$ //$NON-NLS-2$
            sql = sql.replace(GenericSQLHandler.AND_WHERE_CLAUSE, dbmsLanguage.and() + "(" + dataFilterClause + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            sql = sql.replace(GenericSQLHandler.WHERE_CLAUSE, PluginConstant.EMPTY_STRING);
            sql = sql.replace(GenericSQLHandler.AND_WHERE_CLAUSE, PluginConstant.EMPTY_STRING);
        }
        ModelElement analyzedElement = indicator.getAnalyzedElement();
        String tableName = getFullyQualifiedTableName(analyzedElement);
        sql = dbmsLanguage.fillGenericQueryWithColumnsAndTable(sql, analyzedElement.getName(), tableName);
        if (sql.indexOf(GenericSQLHandler.GROUP_BY_ALIAS) != -1) {
            sql = sql.replace(GenericSQLHandler.GROUP_BY_ALIAS, "'" + analyzedElement.getName() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return sql;
    }

    public boolean isImplementRegexFunction(String menuLabel) {
        if (menuLabel == null) {
            return false;
        }
        String regexPatternString = dbmsLanguage.getRegexPatternString(this.indicator);
        if (menuLabel.equals(MENU_VIEW_VALID_ROWS) || menuLabel.equals(MENU_VIEW_VALID_VALUES)) {
            if (getRegexLike(regexPatternString) != null) {
                return true;
            }
        } else if (menuLabel.equals(DataExplorer.MENU_VIEW_INVALID_ROWS) || menuLabel.equals(MENU_VIEW_INVALID_VALUES)) {
            if (getRegexNotLike(regexPatternString) != null) {
                return true;
            }
        }
        return false;

    }

}
