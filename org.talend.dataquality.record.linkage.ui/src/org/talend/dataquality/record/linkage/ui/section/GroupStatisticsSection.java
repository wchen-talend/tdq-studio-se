// ============================================================================
//
// Copyright (C) 2006-2013 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataquality.record.linkage.ui.section;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.talend.dataquality.PluginConstant;
import org.talend.dataquality.analysis.Analysis;
import org.talend.dataquality.indicators.columnset.RecordMatchingIndicator;
import org.talend.dataquality.record.linkage.ui.composite.chart.MatchRuleDataChart;
import org.talend.dataquality.record.linkage.ui.composite.tableviewer.GroupStatisticsTableViewer;
import org.talend.dataquality.record.linkage.ui.composite.tableviewer.provider.GroupStatisticsRow;
import org.talend.dataquality.record.linkage.ui.composite.utils.MatchRuleAnlaysisUtils;
import org.talend.dataquality.record.linkage.ui.i18n.internal.DefaultMessagesImpl;

/**
 * created by zhao on Aug 19, 2013 Group statistics section
 * 
 */
public class GroupStatisticsSection extends AbstractMatchAnaysisTableSection {

    private GroupStatisticsTableViewer groupStatisticsTableViewer = null;

    private MatchRuleDataChart matchRuleChartComp = null;

    /**
     * DOC zhao GroupStatisticsSection constructor comment.
     * 
     * @param form
     * @param parent
     * @param style
     * @param toolkit
     * @param analysis
     */
    public GroupStatisticsSection(ScrolledForm form, Composite parent, int style, FormToolkit toolkit, Analysis analysis) {
        super(form, parent, style, toolkit, analysis);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.dataquality.record.linkage.ui.section.AbstractMatchAnaysisTableSection#createSubChart(org.eclipse.
     * swt.widgets.Composite)
     */
    @Override
    protected void createSubChart(Composite sectionClient) {
        RecordMatchingIndicator recordMatchingIndicator = MatchRuleAnlaysisUtils.getRecordMatchIndicatorFromAna(analysis);
        matchRuleChartComp = new MatchRuleDataChart(sectionClient, recordMatchingIndicator.getGroupSize2groupFrequency());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.dataquality.record.linkage.ui.section.AbstractMatchAnaysisTableSection#createSubContent(org.eclipse
     * .swt.widgets.Composite)
     */
    @Override
    protected Composite createSubContent(Composite sectionClient) {
        groupStatisticsTableViewer = new GroupStatisticsTableViewer(sectionClient, SWT.NONE);
        setGroupStatisticsTableInput();
        return sectionClient;
    }

    /**
     * DOC zhao Comment method "setGroupStatisticsTableInput".
     */
    private void setGroupStatisticsTableInput() {
        RecordMatchingIndicator recordMatchingIndicator = MatchRuleAnlaysisUtils.getRecordMatchIndicatorFromAna(analysis);
        // Row count
        Long rowCount = recordMatchingIndicator.getCount();

        Map<Object, Long> groupSize2GroupFreq = recordMatchingIndicator.getGroupSize2groupFrequency();
        Iterator<Object> groupSizeIterator = groupSize2GroupFreq.keySet().iterator();

        List<GroupStatisticsRow> groups = new ArrayList<GroupStatisticsRow>();
        while (groupSizeIterator.hasNext()) {
            Object groupSize = groupSizeIterator.next();
            Long groupFreq = groupSize2GroupFreq.get(groupSize);
            GroupStatisticsRow groupStatsRow = new GroupStatisticsRow();
            groupStatsRow.setGroupSize(Long.valueOf(groupSize.toString()));
            groupStatsRow.setGroupCount(groupFreq);
            groupStatsRow.setRecordCount(groupStatsRow.getGroupSize() * groupStatsRow.getGroupCount());
            setPercentage(groupStatsRow.getRecordCount(), rowCount, groupStatsRow);
            groups.add(groupStatsRow);
        }
        groupStatisticsTableViewer.setInput(groups);
    }

    /**
     * DOC zhao Comment method "setPercentage".
     * 
     * @param count
     * @param rowCount
     * @param rowCountRow
     */
    private void setPercentage(Long count, Long rowCount, GroupStatisticsRow groupStatsRow) {
        if (rowCount != 0) {
            BigDecimal bd = new BigDecimal(count * 100 / rowCount);
            bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
            groupStatsRow.setRecordPercentage(bd.doubleValue() + PluginConstant.PERCENTAGE_STR);
        } else {
            groupStatsRow.setRecordPercentage(PluginConstant.NA_STRING);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.record.linkage.ui.section.AbstractMatchAnaysisTableSection#getSectionName()
     */
    @Override
    protected String getSectionName() {
        return DefaultMessagesImpl.getString("MatchAnalysisResultPage.GROUP_STATS_NAME"); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.record.linkage.ui.section.AbstractMatchAnaysisTableSection#refreshChart()
     */
    @Override
    public void refreshChart() {
        setGroupStatisticsTableInput();

        RecordMatchingIndicator recordMatchingIndicator = MatchRuleAnlaysisUtils.getRecordMatchIndicatorFromAna(analysis);
        matchRuleChartComp.refresh(recordMatchingIndicator.getGroupSize2groupFrequency());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.dataquality.record.linkage.ui.section.AbstractMatchAnaysisTableSection#isKeyDefinitionAdded(java.lang
     * .String)
     */
    @Override
    public Boolean isKeyDefinitionAdded(String columnName) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.dataquality.record.linkage.ui.section.AbstractMatchAnaysisTableSection#createButtons(org.eclipse.swt
     * .widgets.Composite)
     */
    @Override
    protected void createButtons(Composite sectionClient) {
        // No implementation
    }
}