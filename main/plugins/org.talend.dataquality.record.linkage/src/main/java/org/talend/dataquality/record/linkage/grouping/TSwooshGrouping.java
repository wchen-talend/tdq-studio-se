// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend./9com
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.talend.dataquality.matchmerge.MatchMergeAlgorithm;
import org.talend.dataquality.matchmerge.Record;
import org.talend.dataquality.matchmerge.mfb.MatchResult;
import org.talend.dataquality.matchmerge.mfb.RecordGenerator;
import org.talend.dataquality.matchmerge.mfb.RecordIterator.ValueGenerator;
import org.talend.dataquality.record.linkage.grouping.swoosh.DQMFB;
import org.talend.dataquality.record.linkage.grouping.swoosh.DQMFBRecordMerger;
import org.talend.dataquality.record.linkage.grouping.swoosh.DQRecordIterator;
import org.talend.dataquality.record.linkage.grouping.swoosh.RichRecord;
import org.talend.dataquality.record.linkage.grouping.swoosh.SurvivorShipAlgorithmParams;
import org.talend.dataquality.record.linkage.grouping.swoosh.SurvivorShipAlgorithmParams.SurvivorshipFunction;
import org.talend.dataquality.record.linkage.record.IRecordMatcher;
import org.talend.dataquality.record.linkage.utils.AnalysisRecordGroupingUtils;
import org.talend.dataquality.record.linkage.utils.SurvivorShipAlgorithmEnum;

/**
 * Record grouping class with t-swoosh algorithm.
 * 
 */
public class TSwooshGrouping {

    List<RecordGenerator> rcdsGenerators = new ArrayList<RecordGenerator>();

    int totalCount = 0;

    AbstractRecordGrouping recordGrouping;

    /**
     * DOC zhao TSwooshGrouping constructor comment.
     */
    public TSwooshGrouping(AbstractRecordGrouping recordGrouping) {
        this.recordGrouping = recordGrouping;
    }

    /**
     * Recording matching with t-swoosh algorithm.
     * 
     * @param inputRow
     * @param matchingRule
     */
    public void addToList(final String[] inputRow, List<Map<String, String>> matchRule) {
        totalCount++;
        String attributeName = null;
        Map<String, ValueGenerator> rcdMap = new HashMap<String, ValueGenerator>();
        for (final Map<String, String> recordMap : matchRule) {
            attributeName = recordMap.get(IRecordGrouping.COLUMN_IDX);
            rcdMap.put(attributeName, new ValueGenerator() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see org.talend.dataquality.matchmerge.mfb.RecordIterator.ValueGenerator#getColumnIndex()
                 */
                @Override
                public int getColumnIndex() {
                    return Integer.valueOf(recordMap.get(IRecordGrouping.COLUMN_IDX));
                }

                @Override
                public String newValue() {
                    return inputRow[Integer.valueOf(recordMap.get(IRecordGrouping.COLUMN_IDX))];
                }
            });
        }
        RecordGenerator rcdGen = new RecordGenerator();
        rcdGen.setMatchKeyMap(rcdMap);
        rcdGen.setOriginalRow(inputRow);
        rcdsGenerators.add(rcdGen);
    }

    public void swooshMatch(IRecordMatcher combinedRecordMatcher, SurvivorShipAlgorithmParams survParams) {
        SurvivorShipAlgorithmEnum[] surviorShipAlgos = new SurvivorShipAlgorithmEnum[survParams.getSurviorShipAlgos().length];
        String[] funcParams = new String[surviorShipAlgos.length];
        int idx = 0;
        for (SurvivorshipFunction func : survParams.getSurviorShipAlgos()) {
            surviorShipAlgos[idx] = func.getSurvivorShipFunction();
            funcParams[idx] = func.getParameter();
            idx++;
        }
        MatchMergeAlgorithm algorithm = new DQMFB(combinedRecordMatcher, new DQMFBRecordMerger("MFB", funcParams,
                surviorShipAlgos));

        Iterator<Record> iterator = new DQRecordIterator(totalCount, rcdsGenerators);
        List<Record> mergedRecords = algorithm.execute(iterator, new GroupingCallBack());
        for (Record rcd : mergedRecords) {
            RichRecord printRcd = (RichRecord) rcd;
            output(printRcd);
        }
        totalCount = 0;
        rcdsGenerators.clear();
    }

    class GroupingCallBack implements MatchMergeAlgorithm.Callback {

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.talend.dataquality.matchmerge.MatchMergeAlgorithm.Callback#onBeginRecord(org.talend.dataquality.matchmerge
         * .Record)
         */
        @Override
        public void onBeginRecord(Record record) {
            // Nothing todo
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.talend.dataquality.matchmerge.MatchMergeAlgorithm.Callback#onMatch(org.talend.dataquality.matchmerge.
         * Record, org.talend.dataquality.matchmerge.Record, org.talend.dataquality.matchmerge.mfb.MatchResult)
         */
        @Override
        public void onMatch(Record record1, Record record2, MatchResult matchResult) {

            // record1 and record2 must be RichRecord from DQ grouping implementation.
            RichRecord richRecord1 = (RichRecord) record1;
            RichRecord richRecord2 = (RichRecord) record2;

            String grpId1 = richRecord1.getGroupId();
            String grpId2 = richRecord2.getGroupId();
            if (grpId1 == null && grpId2 == null) {
                // Both records are original records.
                String gid = UUID.randomUUID().toString(); // Generate a new GID.
                richRecord1.setGroupId(gid);
                richRecord2.setGroupId(gid);
                // group size is 0 for none-master record
                richRecord1.setGrpSize(0);
                richRecord2.setGrpSize(0);

                richRecord1.setMaster(false);
                richRecord2.setMaster(false);
                // TODO set score and matching distance details.

                output(richRecord1);
                output(richRecord2);

            } else if (grpId1 != null && grpId2 != null) {
                // Both records are merged records.
                // Append the new group id to the existing id delimited by ","
                String combinedGRPID = richRecord1.getGroupId() + "," + richRecord2.getGroupId(); //$NON-NLS-1$
                richRecord1.setGroupId(combinedGRPID);
                richRecord2.setGroupId(combinedGRPID);

            } else if (grpId1 == null) {
                // richRecord1 is original record
                // GID is the gid of record 2.
                richRecord1.setGroupId(richRecord2.getGroupId());
                // group size is 0 for none-master record
                richRecord1.setGrpSize(0);
                richRecord1.setMaster(false);
                // TODO set score and matching distance details for record 1.

                output(richRecord1);

            } else {
                // richRecord2 is original record.
                // GID
                richRecord2.setGroupId(richRecord1.getGroupId());
                // group size is 0 for none-master record
                richRecord2.setGrpSize(0);
                richRecord2.setMaster(false);
                // TODO set score and matching distance details for record 2.

                output(richRecord2);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.talend.dataquality.matchmerge.MatchMergeAlgorithm.Callback#onNewMerge(org.talend.dataquality.matchmerge
         * .Record)
         */
        @Override
        public void onNewMerge(Record record) {
            // record must be RichRecord from DQ grouping implementation.
            if (record.getGroupId() != null) {
                RichRecord richRecord = (RichRecord) record;
                richRecord.setMaster(true);
                richRecord.setMerged(true);
                richRecord.setScore(1);
                // TODO check if the size is the size of original records or not.
                richRecord.setGrpSize(richRecord.getRelatedIds().size());
            }

        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.talend.dataquality.matchmerge.MatchMergeAlgorithm.Callback#onRemoveMerge(org.talend.dataquality.matchmerge
         * .Record)
         */
        @Override
        public void onRemoveMerge(Record record) {
            // record must be RichRecord from DQ grouping implementation.
            RichRecord richRecord = (RichRecord) record;
            if (richRecord.isMerged()) {
                richRecord.setOriginRow(null); // set null original row, won't be usefull anymore after another merge.
            }
            richRecord.setMerged(false);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.talend.dataquality.matchmerge.MatchMergeAlgorithm.Callback#onDifferent(org.talend.dataquality.matchmerge
         * .Record, org.talend.dataquality.matchmerge.Record, org.talend.dataquality.matchmerge.mfb.MatchResult)
         */
        @Override
        public void onDifferent(Record record1, Record record2, MatchResult matchResult) {
            RichRecord currentRecord = (RichRecord) record2;
            // group size is 1 for unique record
            currentRecord.setGrpSize(1);
            currentRecord.setMaster(true);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.talend.dataquality.matchmerge.MatchMergeAlgorithm.Callback#onEndRecord(org.talend.dataquality.matchmerge
         * .Record)
         */
        @Override
        public void onEndRecord(Record record) {
            // Nothing todo
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.talend.dataquality.matchmerge.MatchMergeAlgorithm.Callback#isInterrupted()
         */
        @Override
        public boolean isInterrupted() {
            // TODO Auto-generated method stub
            return false;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.talend.dataquality.matchmerge.MatchMergeAlgorithm.Callback#onBeginProcessing()
         */
        @Override
        public void onBeginProcessing() {
            // TODO Auto-generated method stub

        }

        /*
         * (non-Javadoc)
         * 
         * @see org.talend.dataquality.matchmerge.MatchMergeAlgorithm.Callback#onEndProcessing()
         */
        @Override
        public void onEndProcessing() {
            // TODO Auto-generated method stub

        }

    }

    private void output(RichRecord record) {
        String[] outputs = record.getOutputRow();
        recordGrouping.outputRow(AnalysisRecordGroupingUtils.join(outputs, "|", AnalysisRecordGroupingUtils.ESCAPE_CHARACTER));
    }
}
