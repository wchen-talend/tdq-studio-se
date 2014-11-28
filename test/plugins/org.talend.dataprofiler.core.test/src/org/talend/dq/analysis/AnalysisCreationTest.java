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

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.AllDataProfilerCoreTests;
import org.talend.core.model.metadata.IMetadataConnection;
import org.talend.core.model.metadata.MetadataFillFactory;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.database.DqRepositoryViewService;
import org.talend.cwm.db.connection.ConnectionUtils;
import org.talend.cwm.helper.CatalogHelper;
import org.talend.cwm.management.api.FolderProvider;
import org.talend.cwm.relational.RelationalFactory;
import org.talend.cwm.relational.TdColumn;
import org.talend.cwm.relational.TdExpression;
import org.talend.cwm.relational.TdTable;
import org.talend.dataprofiler.core.manager.DQStructureManager;
import org.talend.dataquality.analysis.Analysis;
import org.talend.dataquality.analysis.AnalysisType;
import org.talend.dataquality.domain.Domain;
import org.talend.dataquality.domain.DomainFactory;
import org.talend.dataquality.domain.RangeRestriction;
import org.talend.dataquality.domain.pattern.Pattern;
import org.talend.dataquality.domain.pattern.PatternFactory;
import org.talend.dataquality.domain.pattern.RegularExpression;
import org.talend.dataquality.factories.PatternIndicatorFactory;
import org.talend.dataquality.indicators.Indicator;
import org.talend.dataquality.indicators.IndicatorParameters;
import org.talend.dataquality.indicators.IndicatorsFactory;
import org.talend.dataquality.indicators.PatternMatchingIndicator;
import org.talend.dataquality.indicators.definition.DefinitionFactory;
import org.talend.dataquality.indicators.definition.IndicatorCategory;
import org.talend.dataquality.indicators.definition.IndicatorDefinition;
import org.talend.dq.analysis.parameters.DBConnectionParameter;
import org.talend.dq.helper.ParameterUtil;
import org.talend.dq.writer.impl.ElementWriterFactory;
import org.talend.repository.model.ProjectNodeHelper;
import org.talend.resource.ResourceManager;
import org.talend.utils.exceptions.TalendException;
import org.talend.utils.properties.PropertiesLoader;
import org.talend.utils.properties.TypedProperties;
import org.talend.utils.sugars.ReturnCode;
import orgomg.cwm.objectmodel.core.ModelElement;
import orgomg.cwm.resource.relational.Catalog;

/**
 * DOC scorreia class global comment. Detailled comment
 */
public class AnalysisCreationTest {

    private static final String ANALYSIS_NAME = "My_test_analysis" + System.currentTimeMillis(); //$NON-NLS-1$\

    private static final String DATA_PROVIDER_NAME = "My_data_provider" + System.currentTimeMillis(); //$NON-NLS-1$\

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    /**
     * test column name
     */
    private static final String COLUMN_ANALYZED = "lname"; //$NON-NLS-1$

    /**
     * test table name
     */
    private static final String TABLE_ANALYZED = "customer"; //$NON-NLS-1$

    /**
     * test database name
     */
    private static final String DB_TO_ANALYZE = "tbi"; //$NON-NLS-1$

    /**
     * test regexp pattern
     */
    private static final String REGEXP = "'su.*'"; //$NON-NLS-1$

    /**
     * default DomainFactory
     */
    private static final DomainFactory DOMAIN = DomainFactory.eINSTANCE;

    private static final String INDICATOR_NAME_RegexpMatchingIndicator = "RegexpMatchingIndicator"; //$NON-NLS-1$

    private static Logger log = Logger.getLogger(AnalysisCreationTest.class);

    private AnalysisBuilder analysisBuilder;

    @Before
    public void setUp() throws Exception {
        if (DQStructureManager.getInstance().isNeedCreateStructure()) {
            DQStructureManager.getInstance().createDQStructure();
        }
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * DOC scorreia Comment method "run".
     * 
     * @throws TalendException
     */
    @Test
    public void testRun() throws TalendException {
        analysisBuilder = new AnalysisBuilder();

        boolean analysisInitialized = analysisBuilder.initializeAnalysis(ANALYSIS_NAME, AnalysisType.COLUMN);
        Assert.assertTrue(ANALYSIS_NAME + " failed to initialize!", analysisInitialized); //$NON-NLS-1$

        // get the connection
        Connection dataManager = getDataManager();
        Assert.assertNotNull("No datamanager found!", dataManager); //$NON-NLS-1$
        analysisBuilder.setAnalysisConnection(dataManager);

        // get a column to analyze
        ModelElement column = null;
        try {
            column = getColumn(dataManager);
        } catch (Exception e) {
            log.error(e, e);
        }
        Indicator[] indicators = getIndicators(column);
        analysisBuilder.addElementToAnalyze(column, indicators);

        // get the domain constraint
        Domain dataFilter = getDataFilter(dataManager, (TdColumn) column); // CAST here for test
        analysisBuilder.addFilterOnData(dataFilter);

        // TODO scorreia save domain with analysisbuilder?
        FolderProvider folderProvider = new FolderProvider();
        folderProvider.setFolderResource(ResourceManager.getAnalysisFolder());

        // run analysis
        Analysis analysis = analysisBuilder.getAnalysis();
        final boolean useSql = true;
        IAnalysisExecutor exec = useSql ? new ColumnAnalysisSqlExecutor() : new ColumnAnalysisExecutor();
        ReturnCode executed = exec.execute(analysis);
        Assert.assertTrue("Problem executing analysis: " + ANALYSIS_NAME + ": " + executed.getMessage(), executed.isOk()); //$NON-NLS-1$ //$NON-NLS-2$

        // assert before create: the folder is empty
        File[] listFiles = folderProvider.getFolder().listFiles();
        // on the server there are many cases which may already create some thing, so the folder may not empy
        // Assert.assertTrue(listFiles == null || listFiles.length == 0);

        // save data provider
        ElementWriterFactory.getInstance().createDataProviderWriter().create(dataManager, folderProvider.getFolderResource());
        // save analysis
        ElementWriterFactory.getInstance().createAnalysisWrite().create(analysis, folderProvider.getFolderResource());

        // assert after create
        boolean dataProviderOK = false;
        boolean analysisOK = false;
        listFiles = folderProvider.getFolder().listFiles();
        if (listFiles != null) {
            for (File file : listFiles) {
                String name = file.getName();
                if (name.startsWith(DATA_PROVIDER_NAME)) {
                    dataProviderOK = true;
                } else if (name.startsWith(ANALYSIS_NAME)) {
                    analysisOK = true;
                }
            }
        }
        Assert.assertTrue(dataProviderOK);
        Assert.assertTrue(analysisOK);
    }

    public Analysis createAndRunAnalysis() throws TalendException {
        this.analysisBuilder = new AnalysisBuilder();

        boolean analysisInitialized = analysisBuilder.initializeAnalysis(ANALYSIS_NAME, AnalysisType.COLUMN);
        Assert.assertTrue(ANALYSIS_NAME + " failed to initialize!", analysisInitialized); //$NON-NLS-1$

        // get the connection
        Connection dataManager = getDataManager();
        Assert.assertNotNull("No datamanager found!", dataManager); //$NON-NLS-1$
        analysisBuilder.setAnalysisConnection(dataManager);

        // get a column to analyze
        ModelElement column = null;
        try {
            column = getColumn(dataManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Indicator[] indicators = getIndicators(column);
        analysisBuilder.addElementToAnalyze(column, indicators);

        // get the domain constraint
        Domain dataFilter = getDataFilter(dataManager, (TdColumn) column); // CAST here for test
        analysisBuilder.addFilterOnData(dataFilter);

        // run analysis
        Analysis analysis = analysisBuilder.getAnalysis();
        final boolean useSql = true;
        IAnalysisExecutor exec = useSql ? new ColumnAnalysisSqlExecutor() : new ColumnAnalysisExecutor();
        ReturnCode executed = exec.execute(analysis);
        Assert.assertTrue("Problem executing analysis: " + ANALYSIS_NAME + ": " + executed.getMessage(), executed.isOk()); //$NON-NLS-1$ //$NON-NLS-2$
        return analysis;
    }

    /**
     * DOC scorreia Comment method "getDataFilter".
     * 
     * @param dataManager
     * @param column
     * @return
     */
    private Domain getDataFilter(Connection dataManager, TdColumn column) {
        Domain domain = DOMAIN.createDomain();
        RangeRestriction rangeRestriction = DOMAIN.createRangeRestriction();
        domain.getRanges().add(rangeRestriction);
        // BooleanExpressionNode expr = getExpression(column);
        // rangeRestriction.setExpressions(expr);
        return domain;
    }

    /**
     * DOC scorreia Comment method "getIndicators".
     * 
     * @param column
     * @return
     */
    private Indicator[] getIndicators(ModelElement column) {
        List<Indicator> allIndicators = new ArrayList<Indicator>();
        allIndicators.add(createPatternMatchingIndicator());

        for (Indicator indicator : allIndicators) {
            indicator.setAnalyzedElement(column);
            boolean definitionSet = setDefaultIndicatorDefinition(indicator);
            if (log.isDebugEnabled()) {
                log.debug("Definition set for " + indicator.getName() + ": " + definitionSet); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        return allIndicators.toArray(new Indicator[allIndicators.size()]);
    }

    /**
     * DOC xqliu Comment method "setDefaultIndicatorDefinition".
     * 
     * @param indicator
     * @return
     */
    private boolean setDefaultIndicatorDefinition(Indicator indicator) {
        boolean result = false;
        if (indicator != null) {
            IndicatorDefinition indDef = createIndicatorDefinitionByName(indicator.getName());
            if (indDef != null) {
                indicator.setIndicatorDefinition(indDef);
                result = true;
            }
        }
        return result;
    }

    /**
     * DOC xqliu Comment method "createIndicatorDefinitionByName".
     * 
     * @param name
     * @return
     */
    private IndicatorDefinition createIndicatorDefinitionByName(String name) {
        if (INDICATOR_NAME_RegexpMatchingIndicator.equals(name)) {
            return createIndicatorDefinitionRegexpMatchingIndicator();
        }
        return null;
    }

    /**
     * DOC xqliu Comment method "createIndicatorDefinitionRegexpMatchingIndicator".
     * 
     * @return
     */
    private IndicatorDefinition createIndicatorDefinitionRegexpMatchingIndicator() {
        IndicatorDefinition indDef = DefinitionFactory.eINSTANCE.createIndicatorDefinition();

        String defName = "Regular Expression Matching"; //$NON-NLS-1$
        indDef.setName(defName);
        indDef.setLabel(EMPTY_STRING);

        indDef.getCategories().add(createIndicatorCategoryPatternMatching());

        TdExpression regularExpressionMatchingMysql = RelationalFactory.eINSTANCE.createTdExpression();
        regularExpressionMatchingMysql
                .setBody("SELECT COUNT(CASE WHEN <%=__COLUMN_NAMES__%> REGEXP BINARY <%=__PATTERN_EXPR__%> THEN 1 END), COUNT(*) FROM <%=__TABLE_NAME__%> <%=__WHERE_CLAUSE__%>"); //$NON-NLS-1$
        regularExpressionMatchingMysql.setLanguage("MySQL"); //$NON-NLS-1$
        indDef.getSqlGenericExpression().add(regularExpressionMatchingMysql);

        return indDef;
    }

    /**
     * DOC xqliu Comment method "createIndicatorCategoryPatternMatching".
     * 
     * @return
     */
    protected IndicatorCategory createIndicatorCategoryPatternMatching() {
        IndicatorCategory indCat = DefinitionFactory.eINSTANCE.createIndicatorCategory();
        String catName = "Pattern Matching"; //$NON-NLS-1$
        indCat.setName(catName);
        indCat.setLabel(catName);
        return indCat;
    }

    /**
     * DOC scorreia Comment method "createPatternMatchingIndicator".
     * 
     * @return
     */
    private PatternMatchingIndicator createPatternMatchingIndicator() {
        Pattern pattern = PatternFactory.eINSTANCE.createPattern();
        pattern.setName("My Pattern"); //$NON-NLS-1$
        RegularExpression regularExpr = PatternFactory.eINSTANCE.createRegularExpression();
        TdExpression expression = RelationalFactory.eINSTANCE.createTdExpression();
        expression.setBody(REGEXP);
        expression.setLanguage("SQL"); //$NON-NLS-1$
        regularExpr.setExpression(expression);
        pattern.getComponents().add(regularExpr);

        PatternIndicatorFactory.createRegexpMatchingIndicator(pattern);
        PatternMatchingIndicator patternMatchingIndicator = IndicatorsFactory.eINSTANCE.createRegexpMatchingIndicator();
        IndicatorParameters indicParams = IndicatorsFactory.eINSTANCE.createIndicatorParameters();
        Domain validData = DomainFactory.eINSTANCE.createDomain();
        validData.getPatterns().add(pattern);
        indicParams.setDataValidDomain(validData);
        patternMatchingIndicator.setParameters(indicParams);

        return patternMatchingIndicator;
    }

    /**
     * DOC scorreia Comment method "getColumn".
     * 
     * @param dataManager
     * @return
     * @throws Exception
     */
    private ModelElement getColumn(Connection dataManager) throws Exception {
        List<Catalog> tdCatalogs = CatalogHelper.getCatalogs(dataManager.getDataPackage());
        System.out.println("Catalogs: " + tdCatalogs); //$NON-NLS-1$
        Assert.assertFalse(tdCatalogs.isEmpty());
        Catalog catalog = tdCatalogs.get(0);
        for (Catalog tdCatalog : tdCatalogs) {
            if (DB_TO_ANALYZE.equals(tdCatalog.getName())) {
                catalog = tdCatalog;
                break;
            }
        }
        Assert.assertNotNull(catalog);
        System.out.println("analysed Catalog: " + catalog.getName()); //$NON-NLS-1$
        List<TdTable> tables = DqRepositoryViewService.getTables(dataManager, catalog, EMPTY_STRING, true);

        // store tables in catalog
        CatalogHelper.addTables(tables, catalog);

        Assert.assertFalse(tables.isEmpty());
        TdTable tdTable = tables.get(0);
        for (TdTable table : tables) {
            if (TABLE_ANALYZED.equals(table.getName())) {
                tdTable = table;
                break;
            }
        }
        System.out.println("analyzed Table: " + tdTable.getName()); //$NON-NLS-1$
        List<TdColumn> columns;
        columns = DqRepositoryViewService.getColumns(dataManager, tdTable, null, true);
        // MOD scorreia 2009-01-29 columns are stored in the table
        // TableHelper.addColumns(tdTable, columns);

        Assert.assertFalse(columns.isEmpty());
        TdColumn col = columns.get(0);
        for (TdColumn tdColumn : columns) {
            if (COLUMN_ANALYZED.equals(tdColumn.getName())) {
                col = tdColumn;
                break;
            }
        }
        System.out.println("analyzed Column: " + col.getName()); //$NON-NLS-1$
        return col;
    }

    /**
     * DOC scorreia Comment method "getDataManager".
     * 
     * @return
     */
    public Connection getDataManager() {
        TypedProperties connectionParams = PropertiesLoader.getProperties(AllDataProfilerCoreTests.class, "db.properties"); //$NON-NLS-1$
        String driverClassName = connectionParams.getProperty("driver"); //$NON-NLS-1$
        String dbUrl = connectionParams.getProperty("url"); //$NON-NLS-1$
        String sqlTypeName = connectionParams.getProperty("sqlTypeName"); //$NON-NLS-1$

        DBConnectionParameter params = new DBConnectionParameter();
        params.setName("My connection"); //$NON-NLS-1$
        params.setDriverClassName(driverClassName);
        params.setJdbcUrl(dbUrl);
        params.setSqlTypeName(sqlTypeName);
        params.setParameters(connectionParams);

        // create connection
        ConnectionUtils.setTimeout(false);

        MetadataFillFactory instance = MetadataFillFactory.getDBInstance();
        IMetadataConnection metaConnection = instance.fillUIParams(ParameterUtil.toMap(params));

        ReturnCode rc = null;
        try {
            rc = instance.checkConnection(metaConnection);
        } catch (java.lang.RuntimeException e) {
            Assert.fail("connect to " + dbUrl + "failed," + e.getMessage());
        }
        Connection dataProvider = null;
        if (rc.isOk()) {
            dataProvider = instance.fillUIConnParams(metaConnection, null);
            dataProvider.setName(DATA_PROVIDER_NAME);

            // because the DI side code is changed, modify the following code.
            metaConnection.setCurrentConnection(dataProvider);
            try {
                ProjectNodeHelper.fillCatalogAndSchemas(metaConnection, (DatabaseConnection) dataProvider);
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        Assert.assertNotNull(dataProvider);
        return dataProvider;
    }
}
