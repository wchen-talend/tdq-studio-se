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
package org.talend.dataquality.record.linkage.grouping.swoosh;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.talend.dataquality.matchmerge.Attribute;
import org.talend.dataquality.matchmerge.AttributeValues;
import org.talend.dataquality.matchmerge.Record;
import org.talend.dataquality.matchmerge.mfb.MFBRecordMerger;
import org.talend.dataquality.record.linkage.grouping.swoosh.SurvivorShipAlgorithmParams.SurvivorshipFunction;
import org.talend.dataquality.record.linkage.record.CombinedRecordMatcher;
import org.talend.dataquality.record.linkage.record.IRecordMatcher;
import org.talend.dataquality.record.linkage.utils.SurvivorShipAlgorithmEnum;

/**
 * created by zhao on Jul 9, 2014 the merger which adapt to incorporate DQ specific grouping information.
 * 
 */
public class DQMFBRecordMerger extends MFBRecordMerger {

    private SurvivorShipAlgorithmParams matchMergeParam = null;

    public DQMFBRecordMerger(String mergedRecordSource, String[] parameters, SurvivorShipAlgorithmEnum[] typeMergeTable) {
        super(mergedRecordSource, parameters, typeMergeTable);
    }

    public DQMFBRecordMerger(String mergedRecordSource, String[] parameters, SurvivorShipAlgorithmEnum[] typeMergeTable,
            SurvivorShipAlgorithmParams matchMergeParam) {
        super(mergedRecordSource, parameters, typeMergeTable);
        this.matchMergeParam = matchMergeParam;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.matchmerge.mfb.MFBRecordMerger#merge(org.talend.dataquality.matchmerge.Record,
     * org.talend.dataquality.matchmerge.Record)
     */
    @Override
    public Record merge(Record record1, Record record2) {
        // Update the merge function given the current matcher.
        IRecordMatcher recordMatcher = matchMergeParam.getRecordMatcher();
        if (recordMatcher instanceof CombinedRecordMatcher) {
            IRecordMatcher lastMatcher = ((CombinedRecordMatcher) recordMatcher).getLastPositiveMatcher();
            // Update merge functions.
            SurvivorshipFunction[] survFuncs = matchMergeParam.getSurvivorshipAlgosMap().get(lastMatcher);
            int idx = 0;
            for (SurvivorshipFunction func : survFuncs) {
                typeMergeTable[idx] = func.getSurvivorShipAlgoEnum();
                parameters[idx++] = func.getParameter();
            }
        }

        return super.merge(record1, record2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.dataquality.matchmerge.mfb.MFBRecordMerger#createNewRecord(org.talend.dataquality.matchmerge.Record,
     * long)
     */
    @Override
    protected Record createNewRecord(Record record1, Record record2, long mergedRecordTimestamp) {
        RichRecord richRecord1 = (RichRecord) record1;
        RichRecord richRecord2 = (RichRecord) record2;
        RichRecord mergedRecord = new RichRecord(record1.getId(), mergedRecordTimestamp, mergedRecordSource);
        DQAttribute<?>[] mergedRows = richRecord1.getOriginRow().toArray(new DQAttribute<?>[richRecord1.getOriginRow().size()]);

        // Merge columns which are not maching keys using default survior rules.
        List<Attribute> matchingAttributes = record1.getAttributes();
        for (int colIdx = 0; colIdx < richRecord1.getOriginRow().size(); colIdx++) {
            boolean isMatchKeyIndex = false;
            for (Attribute attribute : matchingAttributes) {
                if (attribute.getColumnIndex() == colIdx) {
                    isMatchKeyIndex = true;
                    break;
                }
            }

            if (!isMatchKeyIndex) {
                Map<Integer, SurvivorshipFunction> defaultSurvivorshipFuncs = matchMergeParam.getDefaultSurviorshipRules();
                SurvivorshipFunction survivorshipFunc = defaultSurvivorshipFuncs.get(colIdx);
                if (survivorshipFunc == null || survivorshipFunc.getSurvivorShipAlgoEnum() == null) {
                    // No default survivorship function was set.
                    continue;
                }

                // Get the merged value and update the merged row.
                mergedRows[colIdx] = new DQAttribute(StringUtils.EMPTY, colIdx);// No label for the
                // attributes which are not
                // matching keys;

                // Keep values from original records (if any)
                String leftValue = richRecord1.getOriginRow().get(colIdx).getValue();
                String rightValue = richRecord2.getOriginRow().get(colIdx).getValue();
                AttributeValues<String> leftValues = richRecord1.getOriginRow().get(colIdx).getValues();
                if (leftValues.size() > 0) {
                    mergedRows[colIdx].getValues().merge(leftValues);
                } else {
                    mergedRows[colIdx].getValues().get(leftValue).increment();
                }
                AttributeValues<String> rightValues = richRecord2.getOriginRow().get(colIdx).getValues();
                if (rightValues.size() > 0) {
                    mergedRows[colIdx].getValues().merge(rightValues);
                } else {
                    mergedRows[colIdx].getValues().get(rightValue).increment();
                }
                // Merge values
                if (leftValue == null && rightValue == null) {
                    mergedRows[colIdx].setValue(null);
                } else {
                    SurvivorShipAlgorithmEnum survAlgo = survivorshipFunc.getSurvivorShipAlgoEnum();
                    String parameter = survivorshipFunc.getParameter();
                    String mergedValue = createMergeValue(record1.getSource(), record2.getSource(), parameter,
                            record1.getTimestamp(), record2.getTimestamp(), survAlgo, leftValue, rightValue,
                            mergedRows[colIdx].getValue(), mergedRows[colIdx].getValues());
                    if (mergedValue != null) {
                        mergedRows[colIdx].setValue(mergedValue);
                    }
                }

            }
            /**
             * Else the matching key's value will be udpated when call {@link RichRecord#getOutputRow()}
             */

        }
        List<DQAttribute<?>> originalRowList = new ArrayList<DQAttribute<?>>();
        CollectionUtils.addAll(originalRowList, mergedRows);
        mergedRecord.setOriginRow(originalRowList);
        mergedRecord.setRecordSize(richRecord1.getRecordSize());
        // Set the group quality
        double gQuality1 = richRecord1.getGroupQuality();
        double gQuality2 = richRecord2.getGroupQuality();
        double minQuality = Math.min(gQuality1, gQuality2);
        if (minQuality != 0) {
            mergedRecord.setGroupQuality(minQuality);
        }
        return mergedRecord;
    }
}
