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
package org.talend;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.talend.cwm.db.connection.ConnectionUtilsTest;
import org.talend.cwm.db.connection.MdmStatementTest;
import org.talend.cwm.db.connection.StoreOnDiskHandlerTest;
import org.talend.cwm.dependencies.DependenciesHandlerTest;
import org.talend.cwm.management.api.SoftwareSystemManagerTest;
import org.talend.cwm.management.i18n.MessagesTest;
import org.talend.dq.CWMPluginTest;
import org.talend.dq.analysis.AnalysisExecutorTest;
import org.talend.dq.analysis.AnalysisRealExecutorForDB2Test;
import org.talend.dq.analysis.AnalysisRecordGroupingUtilsTest;
import org.talend.dq.analysis.ColumnAnalysisExecutorTest;
import org.talend.dq.analysis.ColumnAnalysisSqlExecutorTest;
import org.talend.dq.analysis.ConnectionAnalysisCreationTest;
import org.talend.dq.analysis.ExecuteMatchRuleHandlerTest;
import org.talend.dq.analysis.MatchAnalysisExecutorTest;
import org.talend.dq.analysis.MultiColumnAnalysisExecutorTest;
import org.talend.dq.analysis.MultiNominalColAnalysisTest;
import org.talend.dq.analysis.category.CategoryHandlerTest;
import org.talend.dq.analysis.connpool.TdqAnalysisConnectionPoolTest;
import org.talend.dq.analysis.explore.BenfordLawFrequencyExplorerTest;
import org.talend.dq.analysis.explore.DataPatternMatchingTest;
import org.talend.dq.analysis.explore.FrequencyStatisticsExplorerTest;
import org.talend.dq.analysis.explore.FunctionFrequencyStatExplorerTest;
import org.talend.dq.analysis.explore.PatternExplorerTest;
import org.talend.dq.analysis.explore.RowMatchExplorerTest;
import org.talend.dq.analysis.explore.SQLPatternExplorerTest;
import org.talend.dq.analysis.explore.SimpleStatisticsExplorerTest;
import org.talend.dq.analysis.explore.SummaryStastictisExplorerRealTest;
import org.talend.dq.analysis.explore.SummaryStastictisExplorerTest;
import org.talend.dq.analysis.explore.TextStatisticsExplorerTest;
import org.talend.dq.dbms.DbmsLanguageFactoryTest;
import org.talend.dq.dbms.DbmsLanguageTest;
import org.talend.dq.dbms.InfomixDbmsLanguageTest;
import org.talend.dq.dbms.IngresDbmsLanguageTest;
import org.talend.dq.dbms.MSSqlDbmsLanguageTest;
import org.talend.dq.dbms.MySQLDbmsLanguageTest;
import org.talend.dq.dbms.NetezzaDbmsLanguageTest;
import org.talend.dq.dbms.TeradataDbmsLanguageTest;
import org.talend.dq.helper.AnalysisExecutorHelperTest;
import org.talend.dq.helper.ContextHelperTest;
import org.talend.dq.helper.DQDeleteHelperTest;
import org.talend.dq.helper.EObjectHelperTest;
import org.talend.dq.helper.ParameterUtilTest;
import org.talend.dq.helper.PropertyHelperTest;
import org.talend.dq.helper.ProxyRepositoryManagerTest;
import org.talend.dq.helper.ReportUtilsRealTest;
import org.talend.dq.helper.RepositoryNodeHelperRealTest;
import org.talend.dq.helper.RepositoryNodeHelperTest;
import org.talend.dq.helper.UDIHelperTest;
import org.talend.dq.indicators.AbstractSchemaEvaluatorTest;
import org.talend.dq.indicators.ColumnSetIndicatorEvaluatorTest;
import org.talend.dq.indicators.DelimitedFileIndicatorEvaluatorTest;
import org.talend.dq.indicators.SqlIndicatorHandlerTest;
import org.talend.dq.indicators.definitions.LowerUpperQuantileForDB2Test;
import org.talend.dq.nodes.AnalysisFolderRepNodeTest;
import org.talend.dq.nodes.ReportSubFolderRepNodeTest;
import org.talend.dq.writer.AElementPersistanceRealTest;
import org.talend.dq.writer.AElementPersistanceTest;

/**
 * DOC yyin class global comment. Detailled comment
 */
@RunWith(Suite.class)
@SuiteClasses({ BenfordLawFrequencyExplorerTest.class, DataPatternMatchingTest.class, FrequencyStatisticsExplorerTest.class,
        PatternExplorerTest.class, RowMatchExplorerTest.class, SimpleStatisticsExplorerTest.class,
        TextStatisticsExplorerTest.class, SummaryStastictisExplorerRealTest.class, SummaryStastictisExplorerTest.class,
        AnalysisExecutorTest.class, ColumnAnalysisExecutorTest.class, ConnectionAnalysisCreationTest.class,
        ExecuteMatchRuleHandlerTest.class, MatchAnalysisExecutorTest.class, MultiColumnAnalysisExecutorTest.class,
        MultiNominalColAnalysisTest.class, CategoryHandlerTest.class, TdqAnalysisConnectionPoolTest.class,
        DbmsLanguageTest.class, InfomixDbmsLanguageTest.class, IngresDbmsLanguageTest.class, MSSqlDbmsLanguageTest.class,
        MySQLDbmsLanguageTest.class, AnalysisExecutorHelperTest.class, ContextHelperTest.class, DQDeleteHelperTest.class,
        ParameterUtilTest.class, PropertyHelperTest.class, ProxyRepositoryManagerTest.class, ReportUtilsRealTest.class,
        RepositoryNodeHelperRealTest.class, DbmsLanguageFactoryTest.class, RepositoryNodeHelperTest.class, UDIHelperTest.class,
        AbstractSchemaEvaluatorTest.class, NetezzaDbmsLanguageTest.class, ColumnSetIndicatorEvaluatorTest.class,
        DelimitedFileIndicatorEvaluatorTest.class, SqlIndicatorHandlerTest.class, ReportSubFolderRepNodeTest.class,
        AElementPersistanceRealTest.class, AElementPersistanceTest.class, CWMPluginTest.class, MessagesTest.class,
        SoftwareSystemManagerTest.class, DependenciesHandlerTest.class, StoreOnDiskHandlerTest.class, MdmStatementTest.class,
        ConnectionUtilsTest.class, AnalysisFolderRepNodeTest.class, ColumnAnalysisSqlExecutorTest.class, EObjectHelperTest.class,
        AnalysisRealExecutorForDB2Test.class, LowerUpperQuantileForDB2Test.class, SQLPatternExplorerTest.class,
        TeradataDbmsLanguageTest.class, AnalysisRecordGroupingUtilsTest.class, FunctionFrequencyStatExplorerTest.class })
public class AllCwmManagementTests {

}
