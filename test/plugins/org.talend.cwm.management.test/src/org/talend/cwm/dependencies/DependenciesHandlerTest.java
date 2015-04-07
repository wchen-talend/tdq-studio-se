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
package org.talend.cwm.dependencies;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.support.membermodification.MemberMatcher.*;
import static org.powermock.api.support.membermodification.MemberModifier.*;

import java.util.List;

import junit.framework.Assert;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Property;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.cwm.helper.ModelElementHelper;
import org.talend.cwm.management.i18n.Messages;
import org.talend.dataquality.analysis.Analysis;
import org.talend.dataquality.analysis.AnalysisContext;
import org.talend.dataquality.analysis.AnalysisFactory;
import org.talend.dataquality.helpers.AnalysisHelper;
import org.talend.dataquality.helpers.IndicatorHelper;
import org.talend.dataquality.properties.TDQAnalysisItem;
import org.talend.dataquality.properties.impl.PropertiesFactoryImpl;
import org.talend.dq.helper.PropertyHelper;
import org.talend.dq.writer.impl.AnalysisWriter;
import org.talend.dq.writer.impl.DataProviderWriter;
import org.talend.dq.writer.impl.ElementWriterFactory;
import org.talend.utils.sugars.ReturnCode;
import org.talend.utils.sugars.TypedReturnCode;
import orgomg.cwm.objectmodel.core.Dependency;
import orgomg.cwm.objectmodel.core.ModelElement;

/**
 * DOC zshen class global comment. Detailled comment
 */

@PrepareForTest({ PropertyHelper.class, ModelElementHelper.class, IndicatorHelper.class, Messages.class,
        ElementWriterFactory.class })
public class DependenciesHandlerTest {

    @Rule
    public PowerMockRule powerMockRule = new PowerMockRule();

    // TODO need to move this test method into ItemRecordTest
    // /**
    // * Test method for {@link
    // org.talend.cwm.dependencies.DependenciesHandler#getClintDependencyForExport(ModelElement)}
    // * .
    // *
    // * case1 for analysis
    // */
    // @Test
    // public void testGetClintDependencyExportModelElementCase1() {
    // // DependenciesHandler is a final class we can not mock it. so test this method need to call really
    // // getAnaDependency method that we must to create so many element
    // // countIndicator
    // Property countIndicatorProperty = org.talend.core.model.properties.PropertiesFactory.eINSTANCE.createProperty();
    // TDQIndicatorDefinitionItem countIndicatorItem = PropertiesFactory.eINSTANCE.createTDQIndicatorDefinitionItem();
    // countIndicatorProperty.setItem(countIndicatorItem);
    // countIndicatorItem.setProperty(countIndicatorProperty);
    // // Analysis
    // Property anaProperty = org.talend.core.model.properties.PropertiesFactory.eINSTANCE.createProperty();
    // TDQAnalysisItem createItem = PropertiesFactory.eINSTANCE.createTDQAnalysisItem();
    //
    // Analysis createAnalysis = AnalysisFactory.eINSTANCE.createAnalysis();
    //
    // // create analysisResult
    // AnalysisResult createAnalysisResult = AnalysisFactory.eINSTANCE.createAnalysisResult();
    // // for getAnaDependency method
    // CountsIndicator createCountsIndicator = IndicatorsFactory.eINSTANCE.createCountsIndicator();
    //
    // BlankCountIndicator createBlankCountIndicator = IndicatorsFactory.eINSTANCE.createBlankCountIndicator();
    // IndicatorDefinition createIndicatorDefinition = DefinitionFactory.eINSTANCE.createIndicatorDefinition();
    // createCountsIndicator.setIndicatorDefinition(createIndicatorDefinition);
    // countIndicatorItem.setIndicatorDefinition(createIndicatorDefinition);
    // createBlankCountIndicator.setIndicatorDefinition(createIndicatorDefinition);
    // createCountsIndicator.getAllChildIndicators().add(createBlankCountIndicator);
    // createAnalysisResult.getIndicators().add(createCountsIndicator);
    //
    // // two different Indicator use same IndicatorDefinition the second one will be filter out.
    // CompositeIndicator createCompositeIndicator = IndicatorsFactory.eINSTANCE.createTextIndicator();
    // createAnalysisResult.getIndicators().add(createCompositeIndicator);
    // createIndicatorDefinition = DefinitionFactory.eINSTANCE.createIndicatorDefinition();
    // createCompositeIndicator.setIndicatorDefinition(createIndicatorDefinition);
    // // ~
    // // ~for getAnaDependency method
    // createAnalysis.setResults(createAnalysisResult);
    // createItem.setAnalysis(createAnalysis);
    // anaProperty.setItem(createItem);
    // createItem.setProperty(anaProperty);
    //
    // // staticMock PropertyHelper
    // PowerMockito.mockStatic(PropertyHelper.class);
    // Mockito.when(PropertyHelper.getModelElement(anaProperty)).thenReturn(createAnalysis);
    // Mockito.when(PropertyHelper.getProperty(createCountsIndicator.getIndicatorDefinition())).thenReturn(
    // countIndicatorProperty);
    // Mockito.when(PropertyHelper.getProperty(createAnalysis)).thenReturn(anaProperty);
    //
    // // staticMock ModelElementHelper
    // PowerMockito.mockStatic(ModelElementHelper.class);
    // ModelElementMatcher modelElementMatcher = new ModelElementMatcher();
    // Mockito.when(ModelElementHelper.compareUUID(Mockito.argThat(modelElementMatcher),
    // Mockito.argThat(modelElementMatcher)))
    // .thenReturn(modelElementMatcher.equals(modelElementMatcher));
    //
    // List<Property> clintDependency = DependenciesHandler.getInstance().getClintDependencyForExport(createAnalysis);
    // Assert.assertEquals(1, clintDependency.size());
    // Assert.assertEquals(countIndicatorProperty, clintDependency.get(0));
    //
    // }

    // TODO need to move this test method into ItemRecordTest
    // /**
    // * Test method for {@link
    // org.talend.cwm.dependencies.DependenciesHandler#getClintDependencyForExport(ModelElement)}
    // * .
    // *
    // * case2 for report
    // */
    // @Test
    // public void testGetClintDependencyForExportModelElementCase2() {
    // // DependenciesHandler is a final class we can not mock it. so test this method need to call really
    // // getAnaDependency method that we must to create so many element
    // // countIndicator
    // Property reportProperty = org.talend.core.model.properties.PropertiesFactory.eINSTANCE.createProperty();
    // TDQReportItem reportItem = PropertiesFactory.eINSTANCE.createTDQReportItem();
    // reportProperty.setItem(reportItem);
    // TdReport tdqReport = ReportsFactory.eINSTANCE.createTdReport();
    // reportItem.setReport(tdqReport);
    //
    // Property countIndicatorProperty = org.talend.core.model.properties.PropertiesFactory.eINSTANCE.createProperty();
    // TDQIndicatorDefinitionItem countIndicatorItem = PropertiesFactory.eINSTANCE.createTDQIndicatorDefinitionItem();
    // countIndicatorProperty.setItem(countIndicatorItem);
    // countIndicatorItem.setProperty(countIndicatorProperty);
    // // Analysis
    // Property anaProperty = org.talend.core.model.properties.PropertiesFactory.eINSTANCE.createProperty();
    // TDQAnalysisItem createItem = PropertiesFactory.eINSTANCE.createTDQAnalysisItem();
    //
    // Analysis createAnalysis = AnalysisFactory.eINSTANCE.createAnalysis();
    // DependenciesHandler.getInstance().setDependencyOn(tdqReport, createAnalysis);
    //
    // // create analysisResult
    // AnalysisResult createAnalysisResult = AnalysisFactory.eINSTANCE.createAnalysisResult();
    // // for getAnaDependency method
    // CountsIndicator createCountsIndicator = IndicatorsFactory.eINSTANCE.createCountsIndicator();
    //
    // BlankCountIndicator createBlankCountIndicator = IndicatorsFactory.eINSTANCE.createBlankCountIndicator();
    // IndicatorDefinition createIndicatorDefinition = DefinitionFactory.eINSTANCE.createIndicatorDefinition();
    // createCountsIndicator.setIndicatorDefinition(createIndicatorDefinition);
    // countIndicatorItem.setIndicatorDefinition(createIndicatorDefinition);
    // createBlankCountIndicator.setIndicatorDefinition(createIndicatorDefinition);
    // createCountsIndicator.getAllChildIndicators().add(createBlankCountIndicator);
    // createAnalysisResult.getIndicators().add(createCountsIndicator);
    //
    // // two different Indicator use same IndicatorDefinition the second one will be filter out.
    // CompositeIndicator createCompositeIndicator = IndicatorsFactory.eINSTANCE.createTextIndicator();
    // createAnalysisResult.getIndicators().add(createCompositeIndicator);
    // createIndicatorDefinition = DefinitionFactory.eINSTANCE.createIndicatorDefinition();
    // createCompositeIndicator.setIndicatorDefinition(createIndicatorDefinition);
    // // ~
    // // ~for getAnaDependency method
    // createAnalysis.setResults(createAnalysisResult);
    // createItem.setAnalysis(createAnalysis);
    // anaProperty.setItem(createItem);
    // createItem.setProperty(anaProperty);
    //
    // // staticMock PropertyHelper
    // PowerMockito.mockStatic(PropertyHelper.class);
    // Mockito.when(PropertyHelper.getModelElement(anaProperty)).thenReturn(createAnalysis);
    // Mockito.when(PropertyHelper.getModelElement(reportProperty)).thenReturn(tdqReport);
    // Mockito.when(PropertyHelper.getProperty(createCountsIndicator.getIndicatorDefinition())).thenReturn(
    // countIndicatorProperty);
    // Mockito.when(PropertyHelper.getProperty(tdqReport)).thenReturn(reportProperty);
    // Mockito.when(PropertyHelper.getProperty(createAnalysis)).thenReturn(anaProperty);
    //
    // // staticMock ModelElementHelper
    // PowerMockito.mockStatic(ModelElementHelper.class);
    // ModelElementMatcher modelElementMatcher = new ModelElementMatcher();
    // Mockito.when(ModelElementHelper.compareUUID(Mockito.argThat(modelElementMatcher),
    // Mockito.argThat(modelElementMatcher)))
    // .thenReturn(modelElementMatcher.equals(modelElementMatcher));
    //
    // List<Property> clintDependency = DependenciesHandler.getInstance().getClintDependencyForExport(tdqReport);
    // Assert.assertEquals(2, clintDependency.size());
    // Assert.assertEquals(anaProperty, clintDependency.get(0));
    // Assert.assertEquals(countIndicatorProperty, clintDependency.get(1));
    //
    // }

    @Test
    /**
     * Add by qiongli 2012-5-16,it is for TDQ-5259.
     */
    public void testGetClintDependencyProperty_2() {
        Property property = mock(Property.class);
        PowerMockito.mockStatic(PropertyHelper.class);
        when(PropertyHelper.getModelElement(property)).thenReturn(null);
        List<Property> clintDependency = DependenciesHandler.getInstance().getClintDependency(property);
        assertTrue(clintDependency.size() == 0);

    }

    class ModelElementMatcher extends ArgumentMatcher<ModelElement> {

        @Override
        public boolean matches(Object list) {
            if (list instanceof ModelElement) {
                return true;
            }
            return false;
        }
    }

    /**
     * Test method for
     * {@link org.talend.cwm.dependencies.DependenciesHandler#removeDependenciesBetweenModel(orgomg.cwm.objectmodel.core.ModelElement, orgomg.cwm.objectmodel.core.ModelElement)}
     * .
     */
    @Test
    public void testRemoveDependenciesBetweenModel() {
        ModelElement ana = AnalysisHelper.createAnalysis("ana1");
        ModelElement conn = ConnectionHelper.createDatabaseConnection("conn1");
        EList<Dependency> clientDependencyFirst = ana.getClientDependency();
        assertEquals(0, clientDependencyFirst.size());

        TypedReturnCode<Dependency> setUsageDependencyOn = DependenciesHandler.getInstance().setUsageDependencyOn(ana, conn);
        assertEquals(1, clientDependencyFirst.size());
        EList<ModelElement> supplier = clientDependencyFirst.get(0).getSupplier();
        assertEquals(1, supplier.size());
        assertEquals(conn, supplier.get(0));
        if (setUsageDependencyOn.isOk()) {
            DependenciesHandler.getInstance().removeDependenciesBetweenModel(ana, conn);
        }
        assertEquals(0, clientDependencyFirst.size());
        EList<Dependency> clientDependencyTwo = conn.getClientDependency();
        assertEquals(0, clientDependencyTwo.size());
        setUsageDependencyOn = DependenciesHandler.getInstance().setUsageDependencyOn(conn, ana);
        assertEquals(1, clientDependencyTwo.size());
        supplier = clientDependencyTwo.get(0).getSupplier();
        assertEquals(1, supplier.size());
        assertEquals(ana, supplier.get(0));
        if (setUsageDependencyOn.isOk()) {
            DependenciesHandler.getInstance().removeDependenciesBetweenModel(ana, conn);
        }
        assertEquals(0, clientDependencyTwo.size());
    }

    @Test
    public void testRemoveConnDependencyAndSave() {
        TDQAnalysisItem analysisItem = PropertiesFactoryImpl.eINSTANCE.createTDQAnalysisItem();
        Analysis analysis = AnalysisFactory.eINSTANCE.createAnalysis();
        analysis.setName("ana"); //$NON-NLS-1$
        analysisItem.setAnalysis(analysis);
        AnalysisContext analysisContext = AnalysisFactory.eINSTANCE.createAnalysisContext();

        ConnectionItem createConnectionItem = org.talend.core.model.properties.PropertiesFactory.eINSTANCE.createConnectionItem();
        Property property = org.talend.core.model.properties.PropertiesFactory.eINSTANCE.createProperty();
        property.setId(EcoreUtil.generateUUID());
        property.setLabel("conn"); //$NON-NLS-1$
        createConnectionItem.setProperty(property);
        Connection connection = ConnectionFactory.eINSTANCE.createConnection();
        connection.setName("conn"); //$NON-NLS-1$
        setEResource(null, connection);
        createConnectionItem.setConnection(connection);
        stub(method(PropertyHelper.class, "getProperty", Connection.class)).toReturn(property); //$NON-NLS-1$
        ReturnCode rc = new ReturnCode(true);
        DataProviderWriter dp = mock(DataProviderWriter.class);
        when(dp.save(createConnectionItem, false)).thenReturn(rc);
        stub(method(ElementWriterFactory.class, "createDataProviderWriter")).toReturn(dp); //$NON-NLS-1$
        AnalysisWriter aw = mock(AnalysisWriter.class);
        when(aw.save(analysisItem, false)).thenReturn(rc);
        stub(method(ElementWriterFactory.class, "createAnalysisWrite")).toReturn(aw); //$NON-NLS-1$

        DependenciesHandler.getInstance().setUsageDependencyOn(analysis, connection);

        analysis.setContext(analysisContext);
        analysisContext.setConnection(connection);
        DependenciesHandler.getInstance().removeConnDependencyAndSave(analysisItem);
        Assert.assertNull(analysis.getContext().getConnection());
        Assert.assertEquals(true, analysis.getClientDependency().isEmpty());

    }

    private void setEResource(EObject parent, EObject eobject) {
        if (eobject.eResource() == null) {
            XMLResource xmlResource = parent != null && parent.eResource() != null ? (XMLResource) parent.eResource()
                    : new XMLResourceImpl();
            xmlResource.getContents().add(eobject);
        }
        Resource res = eobject.eResource();
        if (res instanceof XMLResource) {
            ((XMLResource) res).setID(eobject, EcoreUtil.generateUUID());
        }
    }

}
