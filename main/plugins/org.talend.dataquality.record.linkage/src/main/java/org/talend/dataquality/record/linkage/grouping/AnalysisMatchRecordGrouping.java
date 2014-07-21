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
package org.talend.dataquality.record.linkage.grouping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.talend.dataquality.record.linkage.utils.AnalysisRecordGroupingUtils;

/**
 * created by zshen on Aug 7, 2013 Detailled comment
 * 
 */
public class AnalysisMatchRecordGrouping extends AbstractRecordGrouping {

    private static Logger log = Logger.getLogger(AnalysisMatchRecordGrouping.class);

    private List<String[]> resultStrList = new ArrayList<String[]>();

    List<Object[]> inputList = new ArrayList<Object[]>();

    private static final String columnDelimiter = "|"; //$NON-NLS-1$

    private MatchGroupResultConsumer matchResultConsumer = null;

    public AnalysisMatchRecordGrouping(MatchGroupResultConsumer matchResultConsumer) {
        this.matchResultConsumer = matchResultConsumer;
        setColumnDelimiter(columnDelimiter);
        setIsOutputDistDetails(true);
        setSeperateOutput(Boolean.TRUE);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.dataquality.record.linkage.grouping.AbstractRecordGrouping#outputRow(java.lang.String)
     */
    @Override
    protected void outputRow(String row) {
        matchResultConsumer.handle(AnalysisRecordGroupingUtils.split(row, columnDelimiter,
                AnalysisRecordGroupingUtils.ESCAPE_CHARACTER));
    }

    public void addRuleMatcher(List<Map<String, String>> ruleMatcherConvertResult) {
        addMatchRule(ruleMatcherConvertResult);

    }

    /**
     * Sets the inputList.
     * 
     * @param inputRows the inputList to set
     */
    public void setMatchRows(List<Object[]> inputRows) {
        this.inputList = inputRows;
    }

    public void run() throws InstantiationException, IllegalAccessException, ClassNotFoundException {

        initialize();
        try {
            for (Object[] inputRow : inputList) {
                String[] inputStrRow = new String[inputRow.length];
                int index = 0;
                for (Object obj : inputRow) {
                    inputStrRow[index++] = obj == null ? null : obj.toString();
                }
                doGroup(inputStrRow);
            }
            end();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Getter for resultStrList.
     * 
     * @return the resultStrList
     */
    public List<String[]> getResultStrList() {
        return this.resultStrList;
    }

}
