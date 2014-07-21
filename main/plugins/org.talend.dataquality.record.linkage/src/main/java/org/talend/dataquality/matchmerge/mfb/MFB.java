/*
 * Copyright (C) 2006-2014 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.dataquality.matchmerge.mfb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import org.apache.log4j.Logger;
import org.talend.dataquality.matchmerge.MatchMergeAlgorithm;
import org.talend.dataquality.matchmerge.Record;
import org.talend.dataquality.matchmerge.SubString;
import org.talend.dataquality.record.linkage.attribute.AttributeMatcherFactory;
import org.talend.dataquality.record.linkage.attribute.IAttributeMatcher;
import org.talend.dataquality.record.linkage.constant.AttributeMatcherType;
import org.talend.dataquality.record.linkage.record.IRecordMatcher;
import org.talend.dataquality.record.linkage.record.IRecordMerger;
import org.talend.dataquality.record.linkage.record.SimpleVSRRecordMatcher;
import org.talend.dataquality.record.linkage.utils.SurvivorShipAlgorithmEnum;

public class MFB implements MatchMergeAlgorithm {

    private static final Logger LOGGER = Logger.getLogger(MFB.class);

    private final IRecordMatcher matcher;

    private final IRecordMerger merger;

    /**
     * Builds a Swoosh implementation based on a {@link org.talend.dataquality.record.linkage.record.IRecordMatcher
     * matcher} and a {@link org.talend.dataquality.record.linkage.record.IRecordMerger merger}.
     * 
     * @param matcher A matcher to be used to compare records together.
     * @param merger A merger to be used to create a merged (a.k.a "golden") record.
     * @see #execute(java.util.Iterator)
     */
    public MFB(IRecordMatcher matcher, IRecordMerger merger) {
        if (matcher == null) {
            throw new IllegalArgumentException("Matcher cannot be null.");
        }
        if (merger == null) {
            throw new IllegalArgumentException("Merger cannot be null.");
        }
        this.matcher = matcher;
        this.merger = merger;
    }

    /**
     * Builds a Swoosh implementation based on provided parameters. This builder is
     * 
     * @param algorithms Types of algorithm to use for match ordered by position of field.
     * @param algorithmParameters Parameter for nth match algorithm (or null if N/A).
     * @param thresholds Threshold for the nth match algorithm (consider the nth column as a match if match is greater
     * than or equals the threshold).
     * @param minConfidenceValue The minimum confidence in the final (merged) record.
     * @param merges The algorithms to use for merging records.
     * @param mergesParameters Parameter for nth merge algorithm (or null if N/A).
     * @param weights Indicates weight for the nth match algorithm.
     * @param nullOptions Indicates how Swoosh should handle <code>null</code> values for nth field.
     * @param subStrings Indicates if Swoosh should perform any substring operation before comparison.
     * @param mergedRecordSource Indicate what should be the
     * {@link org.talend.dataquality.matchmerge.Record#getSource() source} of merged records.
     * @return A {@link org.talend.dataquality.matchmerge.MatchMergeAlgorithm} implementation ready for usage.
     * @see org.talend.dataquality.matchmerge.MatchMergeAlgorithm#execute(java.util.Iterator)
     */
    public static MFB build(AttributeMatcherType[] algorithms, String[] algorithmParameters, float[] thresholds,
            double minConfidenceValue, SurvivorShipAlgorithmEnum[] merges, String[] mergesParameters, double[] weights,
            IAttributeMatcher.NullOption[] nullOptions, SubString[] subStrings, String mergedRecordSource) {
        IRecordMatcher newMatcher = MFBRecordMatcher.wrap(new SimpleVSRRecordMatcher(), minConfidenceValue);
        newMatcher.setRecordSize(algorithms.length);
        // Create attribute match
        IAttributeMatcher[] attributeMatchers = new IAttributeMatcher[algorithms.length];
        int i = 0;
        for (AttributeMatcherType algorithm : algorithms) {
            IAttributeMatcher attributeMatcher;
            if (algorithm != AttributeMatcherType.CUSTOM) {
                attributeMatcher = AttributeMatcherFactory.createMatcher(algorithm);
            } else {
                try {
                    attributeMatcher = AttributeMatcherFactory.createMatcher(algorithm, algorithmParameters[i]);
                } catch (Exception e) {
                    throw new RuntimeException("Could not instantiate match class '" + algorithmParameters[i] + "'.", e);
                }
            }
            attributeMatcher.setNullOption(nullOptions[i]); // Null handling
            attributeMatchers[i] = MFBAttributeMatcher.wrap(attributeMatcher, weights[i], thresholds[i], subStrings[i]);
            i++;
        }
        newMatcher.setAttributeMatchers(attributeMatchers);
        // Set minimum confidence
        newMatcher.setRecordMatchThreshold(minConfidenceValue);
        // Attribute weights
        newMatcher.setAttributeWeights(weights);
        // Create MFB instance
        return new MFB(newMatcher, new MFBRecordMerger(mergedRecordSource, mergesParameters, merges));
    }

    @Override
    public List<Record> execute(Iterator<Record> sourceRecords) {
        return execute(sourceRecords, DefaultCallback.INSTANCE);
    }

    @Override
    public List<Record> execute(Iterator<Record> sourceRecords, Callback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null.");
        }
        List<Record> mergedRecords = new ArrayList<Record>();
        int index = 0;
        // Read source record per record
        Queue<Record> queue = new ProcessQueue<Record>(sourceRecords);
        callback.onBeginProcessing();
        while (!queue.isEmpty() && !callback.isInterrupted()) {
            if (LOGGER.isDebugEnabled() && index % 10000 == 0) {
                LOGGER.debug("Current index: " + index);
            }
            Record currentRecord = queue.poll();
            callback.onBeginRecord(currentRecord);
            // Sanity checks
            if (currentRecord == null) {
                throw new IllegalArgumentException("Record cannot be null.");
            }
            // MFB algorithm
            boolean hasCreatedNewMerge = false;
            for (Record mergedRecord : mergedRecords) {
                MatchResult matchResult = doMatch(mergedRecord, currentRecord);
                if (matchResult.isMatch()) {
                    callback.onMatch(mergedRecord, currentRecord, matchResult);
                    Record newMergedRecord = merger.merge(currentRecord, mergedRecord);
                    queue.offer(newMergedRecord);
                    callback.onNewMerge(newMergedRecord);
                    mergedRecords.remove(mergedRecord);
                    callback.onRemoveMerge(mergedRecord);
                    hasCreatedNewMerge = true;
                    break;
                } else {
                    callback.onDifferent(mergedRecord, currentRecord, matchResult);
                }
            }
            if (!hasCreatedNewMerge) {
                currentRecord.getRelatedIds().add(currentRecord.getId());
                mergedRecords.add(currentRecord);
                callback.onNewMerge(currentRecord);
            }
            callback.onEndRecord(currentRecord);
            index++;
        }
        // Post merge processing (most common values...)
        callback.onEndProcessing();
        return mergedRecords;
    }

    private MatchResult doMatch(Record leftRecord, Record rightRecord) {
        if (leftRecord.getAttributes().size() != rightRecord.getAttributes().size()) {
            throw new IllegalArgumentException("Records do not share same attribute count.");
        }
        if (leftRecord.getGroupId() != null && rightRecord.getGroupId() != null) {
            if (!leftRecord.getGroupId().equals(rightRecord.getGroupId())) {
                boolean isMatchDiffGroup = isMatchDiffGroups();
                if (!isMatchDiffGroup) {
                    return NonMatchResult.INSTANCE;
                }
            } else { // Two records of same group
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Merging already merged records (same group id).");
                }
            }
        }
        // Build match result
        return matcher.getMatchingWeight(leftRecord, rightRecord);
    }

    /**
     * whether match records in different groups.
     */
    protected boolean isMatchDiffGroups() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Cannot match record: already different groups.");
        }
        return false;
    }

    @Override
    public IRecordMatcher getMatcher() {
        return matcher;
    }

    public static class NonMatchResult extends MatchResult {

        public static final MatchResult INSTANCE = wrap(new MatchResult(0));

        private final MatchResult result;

        private NonMatchResult(MatchResult result) {
            super(result.getScores().size());
            this.result = result;
        }

        public static MatchResult wrap(MatchResult result) {
            return new NonMatchResult(result);
        }

        @Override
        public List<Score> getScores() {
            return result.getScores();
        }

        @Override
        public List<Float> getThresholds() {
            return result.getThresholds();
        }

        @Override
        public void setScore(int index, AttributeMatcherType algorithm, double score, String value1, String value2) {
            result.setScore(index, algorithm, score, value1, value2);
        }

        @Override
        public void setThreshold(int index, float threshold) {
            result.setThreshold(index, threshold);
        }

        @Override
        public boolean isMatch() {
            return false;
        }
    }
}
