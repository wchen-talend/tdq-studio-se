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
package org.talend.dataprofiler.core.ui.utils;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.CategoryDataset;
import org.talend.commons.emf.FactoriesUtil;
import org.talend.dataprofiler.core.model.dynamic.DynamicIndicatorModel;
import org.talend.dataprofiler.core.ui.editor.preview.model.dataset.CustomerDefaultBAWDataset;
import org.talend.dataprofiler.core.ui.editor.preview.model.states.SummaryStatisticsState;
import org.talend.dataprofiler.core.ui.events.BenfordFrequencyDynamicChartEventReceiver;
import org.talend.dataprofiler.core.ui.events.DynamicBAWChartEventReceiver;
import org.talend.dataprofiler.core.ui.events.DynamicChartEventReceiver;
import org.talend.dataprofiler.core.ui.events.EventEnum;
import org.talend.dataprofiler.core.ui.events.EventManager;
import org.talend.dataprofiler.core.ui.events.EventReceiver;
import org.talend.dataprofiler.core.ui.events.FrequencyDynamicChartEventReceiver;
import org.talend.dataquality.indicators.Indicator;
import org.talend.dq.indicators.preview.EIndicatorChartType;
import org.talend.dq.nodes.indicator.type.IndicatorEnum;
import org.talend.resource.EResourceConstant;
import org.talend.resource.ResourceManager;
import org.talend.resource.ResourceService;

/**
 * the Analysis's utility class which associated with UI.
 */
public class AnalysisUtils {

    /**
     * used for table analysis-- select dq rules, add filter for match rule folder TDQ-8001
     * 
     * @return
     */
    public static ViewerFilter createRuleFilter() {
        return new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (element instanceof IFile) {
                    IFile file = (IFile) element;
                    if (FactoriesUtil.DQRULE.equals(file.getFileExtension())) {
                        return true;
                    }
                } else if (element instanceof IFolder) {
                    IFolder folder = (IFolder) element;
                    // filter the match rule folder in table analysis
                    if (EResourceConstant.RULES_MATCHER.getName().equals(folder.getName())) {
                        return false;
                    }// ~
                    return ResourceService.isSubFolder(ResourceManager.getRulesFolder(), folder);
                }
                return false;
            }
        };
    }

    /**
     * create a DynamicChart Event Receiver, according to its type
     * 
     * @param categoryDataset
     * @param index
     * @param oneIndicator
     * @param eIndicatorChartType
     * @return
     */
    public static DynamicChartEventReceiver createDynamicChartEventReceiver(DynamicIndicatorModel indicatorModel, int index,
            Indicator oneIndicator) {
        DynamicChartEventReceiver eReceiver = null;
        if (isBenford(indicatorModel.getChartType())) {
            eReceiver = new BenfordFrequencyDynamicChartEventReceiver();
            ((BenfordFrequencyDynamicChartEventReceiver) eReceiver).setSecondDataset(indicatorModel.getSecondDataset());
        } else if (isFrequency(indicatorModel.getChartType())) {
            eReceiver = new FrequencyDynamicChartEventReceiver();
        } else {
            eReceiver = new DynamicChartEventReceiver();
        }
        eReceiver.setDataset(indicatorModel.getDataset());
        eReceiver.setIndexInDataset(index);
        eReceiver.setIndicatorName(oneIndicator.getName());

        eReceiver.setIndicator(oneIndicator);
        return eReceiver;
    }

    /**
     * DOC yyin Comment method "isBenford".
     * 
     * @param eIndicatorChartType
     * @return
     */
    @SuppressWarnings("incomplete-switch")
    private static boolean isFrequency(EIndicatorChartType eIndicatorChartType) {
        switch (eIndicatorChartType) {
        case BIN_FREQUENCE_STATISTICS:
        case BIN_LOW_FREQUENCE_STATISTICS:
        case DATE_FREQUENCE_STATISTICS:
        case DATE_LOW_FREQUENCE_STATISTICS:
        case FREQUENCE_STATISTICS:
        case LOW_FREQUENCE_STATISTICS:
        case SOUNDEX_FREQUENCY_TABLE:
        case SOUNDEX_LOW_FREQUENCY_TABLE:
        case UDI_FREQUENCY:
        case WEEK_FREQUENCE_STATISTICS:
        case WEEK_LOW_FREQUENCE_STATISTICS:
        case MONTH_FREQUENCE_STATISTICS:
        case MONTH_LOW_FREQUENCE_STATISTICS:
        case QUARTER_FREQUENCE_STATISTICS:
        case QUARTER_LOW_FREQUENCE_STATISTICS:
        case YEAR_FREQUENCE_STATISTICS:
        case YEAR_LOW_FREQUENCE_STATISTICS:
        case MODE_INDICATOR:
        case PATTERN_FREQUENCE_STATISTICS:
        case PATTERN_LOW_FREQUENCE_STATISTICS:
            return true;
        }
        return false;
    }

    /**
     * DOC yyin Comment method "isFrequency".
     * 
     * @param eIndicatorChartType
     * @return
     */
    private static boolean isBenford(EIndicatorChartType eIndicatorChartType) {
        if (EIndicatorChartType.BENFORD_LAW_STATISTICS.equals(eIndicatorChartType)) {
            return true;
        }
        return false;
    }

    /**
     * DOC yyin Comment method "createDynamicBAWChartEventReceiver".
     * 
     * @param oneCategoryIndicatorModel
     * @param categoryDataset
     * @return
     */
    public static DynamicBAWChartEventReceiver createDynamicBAWChartEventReceiver(
            DynamicIndicatorModel oneCategoryIndicatorModel, CategoryDataset categoryDataset,
            Map<Indicator, EventReceiver> eventReceivers) {
        DynamicBAWChartEventReceiver bawReceiver = new DynamicBAWChartEventReceiver();
        if (categoryDataset instanceof CustomerDefaultBAWDataset) {
            // all summary selected
            bawReceiver.setBawDataset((CustomerDefaultBAWDataset) categoryDataset);
        } else {
            // not-all summary selected
            bawReceiver.setDataset(categoryDataset);
        }
        bawReceiver.setBAWparentComposite(oneCategoryIndicatorModel.getBawParentChartComp());
        bawReceiver.setTableViewer(oneCategoryIndicatorModel.getTableViewer());
        int index = 0;
        int length = oneCategoryIndicatorModel.getSummaryIndicators().size();
        for (Indicator oneIndicator : oneCategoryIndicatorModel.getSummaryIndicators()) {
            DynamicChartEventReceiver eReceiver = bawReceiver.createEventReceiver(
                    IndicatorEnum.findIndicatorEnum(oneIndicator.eClass()), oneIndicator);
            eReceiver.setTableViewer(oneCategoryIndicatorModel.getTableViewer());
            eReceiver.setEntityIndex(index++);
            if (SummaryStatisticsState.FULL_FLAG != length) {
                eReceiver.setDataset(categoryDataset);
            }
            eReceiver.clearValue();
            eventReceivers.put(oneIndicator, eReceiver);
            EventManager.getInstance().register(oneIndicator, EventEnum.DQ_DYMANIC_CHART, eReceiver);
        }
        bawReceiver.clearValue();
        return bawReceiver;
    }

    /**
     * create a Dynamic Model for one category of indicators, who related to the same chart.
     * 
     * @param chartType
     * @param indicators
     * @param chart
     * @return
     */
    public static DynamicIndicatorModel createDynamicModel(EIndicatorChartType chartType, List<Indicator> indicators,
            JFreeChart chart) {
        DynamicIndicatorModel dyModel = new DynamicIndicatorModel();

        // one dataset <--> several indicators in same category
        if (chart != null) {
            CategoryPlot plot = chart.getCategoryPlot();
            CategoryDataset dataset = plot.getDataset();
            // Added TDQ-8787 20140612 : store the dataset, and the index of the current indicator
            if (EIndicatorChartType.BENFORD_LAW_STATISTICS.equals(chartType)) {
                dataset = plot.getDataset(1);
                dyModel.setSecondDataset(plot.getDataset(0));
            }
            dyModel.setDataset(dataset);
        }
        dyModel.setIndicatorList(indicators);
        dyModel.setChartType(chartType);
        return dyModel;
    }

}
