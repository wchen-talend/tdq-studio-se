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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EClass;
import org.osgi.framework.ServiceException;
import org.talend.cwm.db.connection.MdmStatement;
import org.talend.cwm.db.connection.MdmWebserviceConnection;
import org.talend.cwm.exception.AnalysisExecutionException;
import org.talend.cwm.helper.ResourceHelper;
import org.talend.cwm.helper.SwitchHelpers;
import org.talend.cwm.helper.XmlElementHelper;
import org.talend.cwm.management.i18n.Messages;
import org.talend.cwm.relational.TdExpression;
import org.talend.cwm.xml.TdXmlElementType;
import org.talend.cwm.xml.TdXmlSchema;
import org.talend.dataquality.PluginConstant;
import org.talend.dataquality.analysis.Analysis;
import org.talend.dataquality.analysis.AnalysisResult;
import org.talend.dataquality.helpers.BooleanExpressionHelper;
import org.talend.dataquality.helpers.IndicatorHelper;
import org.talend.dataquality.indicators.CompositeIndicator;
import org.talend.dataquality.indicators.Indicator;
import org.talend.dataquality.indicators.IndicatorsPackage;
import org.talend.dataquality.indicators.NullCountIndicator;
import org.talend.dataquality.indicators.RowCountIndicator;
import org.talend.dataquality.indicators.definition.IndicatorDefinition;
import org.talend.dq.indicators.definitions.DefinitionHandler;
import org.talend.dq.nodes.indicator.type.IndicatorEnum;
import org.talend.utils.collections.MultiMapHelper;
import org.talend.utils.sugars.TypedReturnCode;
import orgomg.cwm.objectmodel.core.Expression;
import orgomg.cwm.objectmodel.core.ModelElement;

/**
 * DOC xqliu class global comment.
 */
public class MdmAnalysisSqlExecutor extends MdmAnalysisExecutor {

    private static Logger log = Logger.getLogger(MdmAnalysisSqlExecutor.class);

    @Override
    protected String createSqlStatement(Analysis analysis) {
        this.cachedAnalysis = analysis;
        AnalysisResult results = analysis.getResults();
        assert results != null;

        try {
            // --- get data filter
            ModelElementAnalysisHandler handler = new ModelElementAnalysisHandler();
            handler.setAnalysis(analysis);
            String stringDataFilter = handler.getStringDataFilter();

            // --- get all the leaf indicators used for the sql computation
            Collection<Indicator> leafIndicators = IndicatorHelper.getIndicatorLeaves(results);
            // --- create one sql statement for each leaf indicator
            for (Indicator indicator : leafIndicators) {
                if (!createSqlQuery(stringDataFilter, indicator)) {
                    log.error(Messages.getString("ColumnAnalysisSqlExecutor.CREATEQUERYERROR") + indicator.getName());//$NON-NLS-1$
                    // return null;
                }
            }
        } catch (AnalysisExecutionException e) {
            log.error(e, e);
            return null;
        }

        return PluginConstant.EMPTY_STRING;
    }

    /**
     * DOC xqliu Comment method "createSqlQuery".
     * 
     * @param dataFilterAsString
     * @param indicator
     * @return
     * @throws AnalysisExecutionException
     */
    private boolean createSqlQuery(String dataFilterAsString, Indicator indicator) throws AnalysisExecutionException {
        ModelElement analyzedElement = indicator.getAnalyzedElement();
        if (analyzedElement == null) {
            traceError(Messages.getString("ColumnAnalysisSqlExecutor.ANALYSISELEMENTISNULL", indicator.getName()));//$NON-NLS-1$
            return Boolean.FALSE;
        }
        TdXmlElementType xmlElement = SwitchHelpers.XMLELEMENTTYPE_SWITCH.doSwitch(indicator.getAnalyzedElement());
        if (xmlElement == null) {
            traceError(Messages.getString("MdmAnalysisSqlExecutor.   ANALYZEDELEMENTISNOTAXMLELEMENT", indicator.getName()));//$NON-NLS-1$
            return Boolean.FALSE;
        }
        // String elementName = XmlElementHelper.getFullName(xmlElement);
        String elementName = xmlElement.getName();
        // get correct language for current database
        String language = dbms().getDbmsName();
        Expression sqlGenericExpression = null;
        // --- create select statement
        // get indicator's sql columnS (generate the real SQL statement from its definition)
        IndicatorDefinition indicatorDefinition;
        String label = indicator.getIndicatorDefinition().getLabel();
        if (label == null || PluginConstant.EMPTY_STRING.equals(label)) {
            indicatorDefinition = indicator.getIndicatorDefinition();
        } else {
            indicatorDefinition = DefinitionHandler.getInstance().getIndicatorDefinition(label);
        }
        if (indicatorDefinition == null) {
            traceError(Messages.getString("ColumnAnalysisSqlExecutor.INTERNALERROR", indicator.getName()));//$NON-NLS-1$
            return Boolean.FALSE;
        }
        sqlGenericExpression = dbms().getSqlExpression(indicatorDefinition);
        final EClass indicatorEclass = indicator.eClass();
        if (sqlGenericExpression == null || sqlGenericExpression.getBody() == null) {
            // when the indicator is a pattern indicator, a possible cause is that the DB does not support regular
            // expressions.
            if (IndicatorsPackage.eINSTANCE.getRegexpMatchingIndicator().equals(indicatorEclass)) {
                traceError(Messages.getString("ColumnAnalysisSqlExecutor.PLEASEREMOVEALLPATTEN", language));//$NON-NLS-1$
                return Boolean.FALSE;
            }
            traceError(Messages
                    .getString(
                            "ColumnAnalysisSqlExecutor.UNSUPPORTEDINDICATOR", (indicator.getName() != null ? indicator.getName() : indicatorEclass.getName()), ResourceHelper.getUUID(indicatorDefinition)));//$NON-NLS-1$
            return Boolean.FALSE;
        }
        // --- get indicator parameters and convert them into sql expression
        List<String> whereExpression = new ArrayList<String>();
        if (StringUtils.isNotBlank(dataFilterAsString)) {
            whereExpression.add(dataFilterAsString);
        }
        String parentElementName = XmlElementHelper.getParentElementName(xmlElement);
        String body = sqlGenericExpression.getBody();
        // MOD qiongli 2012-6-19 TDQ-5139.should use like "(collection("Product/Product")//Product/Stores/Store)" or
        // "(collection("Product/Product")//Product/Stores)/Store"
        if (parentElementName != null) {
            if (body != null) {
                body = sqlGenericExpression.getBody().replaceAll("//", PluginConstant.EMPTY_STRING);//$NON-NLS-1$
                String path = null;
                if (!body.contains("/<%=__COLUMN_NAMES__%")) {//$NON-NLS-1$
                    path = XmlElementHelper.getFullPath(xmlElement, xmlElement.getName());
                } else {
                    path = XmlElementHelper.getFullPath(xmlElement, PluginConstant.EMPTY_STRING);
                }
                parentElementName = "(collection('" + super.getTopLevelsName(xmlElement) + "')//" + path + ")";//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
            }
        }

        // ### evaluate SQL Statement depending on indicators ###
        String completedSqlString = null;
        // --- default case
        completedSqlString = dbms().fillGenericQueryWithColumnsAndTable(body, elementName, parentElementName);
        completedSqlString = addWhereToSqlStringStatement(whereExpression, completedSqlString);
        // completedSqlString is the final query
        String finalQuery = completedSqlString;
        if (finalQuery != null) {
            TdExpression instantiateSqlExpression = BooleanExpressionHelper.createTdExpression(language, finalQuery);
            indicator.setInstantiatedExpression(instantiateSqlExpression);
            return true;
        }
        return false;
    }

    @Override
    protected boolean runAnalysis(Analysis analysis, String sqlStatement) {
        boolean ok = true;
        TypedReturnCode<MdmWebserviceConnection> trc = this.getMdmConnection(analysis);
        if (!trc.isOk()) {
            traceError(Messages.getString(
                    "FunctionalDependencyExecutor.CANNOTEXECUTEANALYSIS", analysis.getName(), trc.getMessage()));//$NON-NLS-1$
            return Boolean.FALSE;
        }

        MdmWebserviceConnection connection = trc.getObject();
        try {
            // store map of element to each indicator used for computation (leaf indicator)
            Map<ModelElement, List<Indicator>> elementToIndicator = new HashMap<ModelElement, List<Indicator>>();

            // execute the sql statement for each indicator
            Collection<Indicator> indicators = IndicatorHelper.getIndicatorLeaves(analysis.getResults());
            ok = runAnalysisIndicators(connection, elementToIndicator, indicators);
            // connection.close();

            // --- finalize indicators by setting the row count and null when they exist.
            setRowCountAndNullCount(elementToIndicator);

        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            setError(e.getMessage());
            // FIXME remove the following information once profiling of SQL mode MDM is supported
            String unableDirectQueryStr = "Unable to perform a direct query"; //$NON-NLS-1$
            if (getErrorMessage().contains(unableDirectQueryStr)) {
                String sqlModeUnsupportedStr = Messages.getString("MdmAnalysisSqlExecutor.SQLMODEUNSUPPORTED"); //$NON-NLS-1$
                String refactoredErrorMessage = StringUtils.replace(getErrorMessage(), unableDirectQueryStr,
                        sqlModeUnsupportedStr, -1);
                setError(refactoredErrorMessage);
            }
            ok = false;
        } catch (ServiceException e) {
            log.error(e.getMessage(), e);
            setError(e.getMessage());
            ok = false;
        } finally {
            // ConnectionUtils.closeConnection(connection);
        }
        return ok;
    }

    /**
     * DOC xqliu Comment method "runAnalysisIndicators".
     * 
     * @param connection
     * @param elementToIndicator
     * @param indicators
     * @return
     * @throws ServiceException
     * @throws RemoteException
     */
    private boolean runAnalysisIndicators(MdmWebserviceConnection connection,
            Map<ModelElement, List<Indicator>> elementToIndicator, Collection<Indicator> indicators) throws RemoteException,
            ServiceException {
        boolean isSuccess = Boolean.TRUE;
        for (Indicator indicator : indicators) {
            // skip composite indicators that do not require a sql execution
            if (indicator instanceof CompositeIndicator) {
                // options of composite indicators are handled elsewhere
                continue;
            }
            // add mapping of analyzed elements to their indicators
            MultiMapHelper.addUniqueObjectToListMap(indicator.getAnalyzedElement(), indicator, elementToIndicator);

            Expression query = dbms().getInstantiatedExpression(indicator);
            if (query == null) {
                // TODO externalize the strings.
                traceError("Query not executed for indicator: \"" + indicator.getName() + "\" "//$NON-NLS-1$//$NON-NLS-2$
                        + "query is null");//$NON-NLS-1$
                isSuccess = Boolean.FALSE;
                continue;
            }
            Boolean isExeSuccess = executeQuery(indicator, connection, query.getBody());
            if (!isExeSuccess) {
                traceError("Query not executed for indicator: \"" + indicator.getName() + "\" "//$NON-NLS-1$//$NON-NLS-2$
                        + "SQL query: " + query.getBody());//$NON-NLS-1$
                isSuccess = Boolean.FALSE;
                continue;
            }
            // set computation done
            indicator.setComputed(true);

        }
        return isSuccess;
    }

    /**
     * DOC xqliu Comment method "setRowCountAndNullCount".
     * 
     * @param elementToIndicator
     */
    protected void setRowCountAndNullCount(Map<ModelElement, List<Indicator>> elementToIndicator) {
        Set<ModelElement> analyzedElements = elementToIndicator.keySet();
        for (ModelElement modelElement : analyzedElements) {
            // get row count indicator
            RowCountIndicator rowCount = IndicatorHelper.getRowCountIndicator(modelElement, elementToIndicator);
            // get null count indicator
            NullCountIndicator nullCount = IndicatorHelper.getNullCountIndicator(modelElement, elementToIndicator);

            List<Indicator> list = elementToIndicator.get(modelElement);
            for (Indicator ind : list) {
                // set row count value to each indicator
                if (rowCount != null && needPercentage(ind)) {
                    ind.setCount(rowCount.getCount());
                }
                // set null count value to each indicator
                if (nullCount != null) {
                    ind.setNullCount(nullCount.getNullCount());
                }
            }
        }
    }

    /**
     * DOC bZhou Comment method "needPercentage".
     * 
     * @param ind
     * @return
     */
    private boolean needPercentage(Indicator ind) {
        IndicatorEnum indType = IndicatorEnum.findIndicatorEnum(ind.eClass());

        return indType != IndicatorEnum.ModeIndicatorEnum && !indType.isAChildOf(IndicatorEnum.TextIndicatorEnum)
                && !indType.isAChildOf(IndicatorEnum.BoxIIndicatorEnum);
    }

    /**
     * DOC xqliu Comment method "executeQuery".
     * 
     * @param indicator
     * @param connection
     * @param queryStmt
     * @return
     * @throws ServiceException
     * @throws RemoteException
     */
    protected boolean executeQuery(Indicator indicator, MdmWebserviceConnection connection, String queryStmt)
            throws RemoteException, ServiceException {
        TdXmlElementType analyzedElement = (TdXmlElementType) indicator.getAnalyzedElement();
        TdXmlSchema xmlDocument = analyzedElement.getOwnedDocument();
        if (log.isInfoEnabled()) {
            log.info(Messages.getString("ColumnAnalysisSqlExecutor.COMPUTINGINDICATOR", indicator.getName())//$NON-NLS-1$
                    + "\t" + Messages.getString("ColumnAnalysisSqlExecutor.EXECUTINGQUERY", queryStmt));//$NON-NLS-1$ //$NON-NLS-2$
        }
        // give result to indicator so that it handles the results
        boolean ret = false;
        try {

            List<Object[]> myResultSet = executeQuery(xmlDocument, connection, queryStmt);

            ret = indicator.storeSqlResults(myResultSet);
        } catch (Exception e) {
            throw new RemoteException(e.toString());
        }
        return ret;
    }

    /**
     * DOC xqliu Comment method "executeQuery".
     * 
     * @param xmlDocument
     * @param connection
     * @param queryStmt
     * @return
     * @throws ServiceException
     * @throws RemoteException
     * @throws javax.xml.rpc.ServiceException
     */
    protected List<Object[]> executeQuery(TdXmlSchema xmlDocument, MdmWebserviceConnection connection, String queryStmt)
            throws RemoteException, ServiceException, javax.xml.rpc.ServiceException {
        // create query statement
        MdmStatement statement = connection.createStatement();
        // statement.setFetchSize(fetchSize);

        if (continueRun()) {
            // MOD xqliu 2010-04-15 bug 12568
            statement.execute(xmlDocument, queryStmt);
        }

        // TODO should support several columns result
        // get the results
        String[] resultSet = statement.getResultSet();
        if (resultSet == null) {
            String mess = Messages.getString("ColumnAnalysisSqlExecutor.NORESULTSETFORTHISSTATEMENT") + queryStmt;//$NON-NLS-1$
            log.warn(mess);
            return null;
        }

        List<Object[]> myResultSet = new ArrayList<Object[]>();
        for (String rs : resultSet) {
            Object[] result = new Object[1];
            result[0] = rs;
            myResultSet.add(result);
        }

        return myResultSet;
    }

    /**
     * DOC xqliu Comment method "addWhereToSqlStringStatement".
     * 
     * @param whereExpressions
     * @param completedSqlString
     * @return
     */
    private String addWhereToSqlStringStatement(List<String> whereExpressions, String completedSqlString) {
        return dbms().addWhereToSqlStringStatement(completedSqlString, whereExpressions);
    }
}
