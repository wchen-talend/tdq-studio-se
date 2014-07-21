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
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.talend.dataquality.record.linkage.attribute.AttributeMatcherFactory;
import org.talend.dataquality.record.linkage.attribute.IAttributeMatcher;
import org.talend.dataquality.record.linkage.constant.AttributeMatcherType;
import org.talend.dataquality.record.linkage.constant.RecordMatcherType;
import org.talend.dataquality.record.linkage.grouping.swoosh.SurvivorShipAlgorithmParams;
import org.talend.dataquality.record.linkage.record.CombinedRecordMatcher;
import org.talend.dataquality.record.linkage.record.IRecordMatcher;
import org.talend.dataquality.record.linkage.record.RecordMatcherFactory;
import org.talend.dataquality.record.linkage.utils.AnalysisRecordGroupingUtils;
import org.talend.dataquality.record.linkage.utils.CustomAttributeMatcherClassNameConvert;
import org.talend.utils.classloader.TalendURLClassLoader;

/**
 * created by zhao on Jul 19, 2013 <br>
 * Abstract record grouping implementation without hadoop API included.
 * 
 */
public abstract class AbstractRecordGrouping implements IRecordGrouping {

    private List<String[]> masterRecords = new ArrayList<String[]>();

    public static final float DEFAULT_THRESHOLD = 0.95f;

    private float acceptableThreshold = DEFAULT_THRESHOLD;

    // Output distance details or not.
    private boolean isOutputDistDetails = Boolean.FALSE;

    // Allow seperate output give group score and the confidence threshold.
    private boolean isSeperateOutput = Boolean.FALSE;

    private CombinedRecordMatcher combinedRecordMatcher = RecordMatcherFactory.createCombinedRecordMatcher();

    /**
     * @deprecated use {{@link #multiMatchRules}
     */
    @Deprecated
    private List<Map<String, String>> matchingColumns = new ArrayList<Map<String, String>>();

    private List<List<Map<String, String>>> multiMatchRules = new ArrayList<List<Map<String, String>>>();

    private SurvivorShipAlgorithmParams survivorShipAlgorithmParams = null;

    private String columnDelimiter = null;

    private Boolean isLinkToPrevious = Boolean.FALSE;

    // VSR algorithm by default.
    private MatchAlgoithm matchAlgo = MatchAlgoithm.VSR;

    private TSwooshGrouping swooshGrouping = new TSwooshGrouping(this);

    // The exthended column size.
    int extSize;

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.hadoop.group.IRecordGrouping#setColumnDelimiter(java.lang.String)
     */
    @Override
    public void setColumnDelimiter(String columnDelimiter) {
        this.columnDelimiter = columnDelimiter;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.hadoop.group.IRecordGrouping#SetIsLinkToPrevious(java.lang.Boolean)
     */
    @Override
    public void setIsLinkToPrevious(Boolean isLinkToPrevious) {
        this.isLinkToPrevious = isLinkToPrevious;
    }

    /**
     * @deprecated {@link IRecordMatcher#setRecordMatchThreshold(double)}
     */
    @Deprecated
    @Override
    public void setAcceptableThreshold(float acceptableThreshold) {
        this.acceptableThreshold = acceptableThreshold;
    }

    @Override
    public void setIsOutputDistDetails(boolean isOutputDistDetails) {
        this.isOutputDistDetails = isOutputDistDetails;
    }

    /**
     * Sets the isSeperateOutput.
     * 
     * @param isSeperateOutput the isSeperateOutput to set
     */
    @Override
    public void setSeperateOutput(boolean isSeperateOutput) {
        this.isSeperateOutput = isSeperateOutput;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.dataquality.record.linkage.grouping.IRecordGrouping#setMatchingAlgorithm(org.talend.dataquality.record
     * .linkage.grouping.AbstractRecordGrouping.MatchAlgoithm)
     */
    @Override
    public void setRecordLinkAlgorithm(MatchAlgoithm algorithm) {
        matchAlgo = algorithm;
    }

    @Override
    public void doGroup(String[] inputRow) throws IOException, InterruptedException {

        extSize = isOutputDistDetails ? 5 : 4;
        // extSize + 1 when isSeperateOutput is enabled.
        extSize = isSeperateOutput ? extSize + 1 : extSize;
        // In case of current component is linked to previous, and the record is NOT master, just put it to the output
        // and continue;
        if (isLinkToPrevious && !inputRow[inputRow.length - extSize + 2].equalsIgnoreCase("true")) { //$NON-NLS-1$
            outputRow(AnalysisRecordGroupingUtils.join(inputRow, columnDelimiter, AnalysisRecordGroupingUtils.ESCAPE_CHARACTER));
            return;
        }

        if (multiMatchRules.size() == 0) {
            // No rule defined.
            return;
        }
        // temporary array to store attributes to match
        List<Map<String, String>> matchingRule = multiMatchRules.get(0);
        String[] lookupDataArray = new String[matchingRule.size()];

        for (int idx = 0; idx < lookupDataArray.length; idx++) {
            lookupDataArray[idx] = inputRow[Integer.parseInt(matchingRule.get(idx).get(IRecordGrouping.COLUMN_IDX))];
        }
        switch (matchAlgo) {
        case VSR:
            vsrMatch(inputRow, matchingRule, lookupDataArray);
            break;
        case TSWOOSH:
            swooshGrouping.addToList(inputRow, matchingRule);

        }
    }

    /**
     * Record matching with VSR algorithm.
     * 
     * @param inputRow the input row.
     * @param matchingRule mathcing rules.
     * @param lookupDataArray the array data (record) to be matched with.
     * @throws IOException
     * @throws InterruptedException
     */
    private void vsrMatch(String[] inputRow, List<Map<String, String>> matchingRule, String[] lookupDataArray)
            throws IOException, InterruptedException {
        boolean isSimilar = false;
        for (String[] masterRecord : masterRecords) {
            if (isLinkToPrevious) {
                int masterGRPSize = Integer.valueOf(masterRecord[inputRow.length - extSize + 1]);
                int inputGRPSize = Integer.valueOf(inputRow[inputRow.length - extSize + 1]);
                // Don't compare the records whose GRP_SIZE both > 1.
                if (masterGRPSize > 1 && inputGRPSize > 1) {
                    continue;
                }
            }

            String[] masterMatchRecord = new String[lookupDataArray.length];
            // Find the match record from master record.
            for (int idx = 0; idx < lookupDataArray.length; idx++) {
                masterMatchRecord[idx] = masterRecord[Integer.parseInt(matchingRule.get(idx).get(IRecordGrouping.COLUMN_IDX))];
            }
            double matchingProba = combinedRecordMatcher.getMatchingWeight(masterMatchRecord, lookupDataArray);
            // Similar
            if (matchingProba >= combinedRecordMatcher.getRecordMatchThreshold()) {
                String distanceDetails = computeOutputDetails();
                isSimilar = true;
                // Master GRP_SIZE ++
                if (isLinkToPrevious) {
                    int masterGRPSize = Integer.valueOf(masterRecord[masterRecord.length - extSize + 1]);
                    if (masterGRPSize == 1) {
                        inputRow[inputRow.length - extSize + 1] = String.valueOf(Integer.parseInt(inputRow[inputRow.length
                                - extSize + 1]) + 1);
                        updateWithExtendedColumn(masterRecord, inputRow, matchingProba, distanceDetails, columnDelimiter);
                        // Update master record from the temporary master list.
                        masterRecords.remove(masterRecord);
                        masterRecords.add(inputRow);
                        break;
                    }
                }
                masterRecord[masterRecord.length - extSize + 1] = String.valueOf(Integer
                        .parseInt(masterRecord[masterRecord.length - extSize + 1]) + 1);
                // Duplicated record
                updateWithExtendedColumn(inputRow, masterRecord, matchingProba, distanceDetails, columnDelimiter);
                break;
            }

        }
        if (!isSimilar) {
            // For the passes that linked to previous, the extension size (e.g: GID,GRP_SIZE) had already been set
            // before.

            // Master record
            String[] masterRow = new String[inputRow.length + (isLinkToPrevious ? 0 : extSize)];
            for (int idx = 0; idx < inputRow.length; idx++) {
                masterRow[idx] = inputRow[idx];
            }
            if (!isLinkToPrevious) {
                // GID
                masterRow[masterRow.length - extSize] = String.valueOf(UUID.randomUUID());
                // Group size
                masterRow[masterRow.length - extSize + 1] = String.valueOf(1);
                // Master
                masterRow[masterRow.length - extSize + 2] = String.valueOf(true);
                // Score
                masterRow[masterRow.length - extSize + 3] = String.valueOf(1.0);

                int extIdx = 3;
                if (isSeperateOutput) {
                    extIdx++;
                    // Group quality
                    masterRow[masterRow.length - extSize + extIdx] = String.valueOf(1.0);
                }

                if (isOutputDistDetails) {
                    extIdx++;
                    // Match distance details in master record is null
                    masterRow[masterRow.length - extSize + extIdx] = ""; //$NON-NLS-1$
                }
            }

            masterRecords.add(masterRow);
        }
    }

    /**
     * DOC zhao Comment method "computeGroupQuality".
     * 
     * @param matchingProba
     * @return
     */
    private double computeGroupQuality(String[] masterRecord, double matchingProba, int idx) {
        double groupQuality = Double.valueOf(masterRecord[masterRecord.length - extSize + idx]);
        if (matchingProba < groupQuality) {
            // Use the minimal match distance as the group score.
            groupQuality = matchingProba;
        }
        return groupQuality;
    }

    /**
     * DOC zhao Comment method "computeOutputDetails".
     * 
     * @param distanceDetails
     * @return
     */
    private String computeOutputDetails() {
        String distanceDetails = StringUtils.EMPTY;
        if (isOutputDistDetails) {
            combinedRecordMatcher.setDisplayLabels(Boolean.TRUE);
            distanceDetails = combinedRecordMatcher.getLabeledAttributeMatchWeights();
        }
        return distanceDetails;
    }

    @Override
    public void end() throws IOException, InterruptedException {
        switch (matchAlgo) {
        case VSR:
            // output the masters
            for (String[] mst : masterRecords) {
                outputRow(AnalysisRecordGroupingUtils.join(mst, columnDelimiter, AnalysisRecordGroupingUtils.ESCAPE_CHARACTER));
            }
            break;
        case TSWOOSH:
            swooshGrouping.swooshMatch(combinedRecordMatcher, survivorShipAlgorithmParams);

        }
    }

    private void updateWithExtendedColumn(String[] inputRow, String[] masterRecord, double matchingProba, String distanceDetails,
            String delimiter) throws IOException, InterruptedException {
        String[] duplicateRecord = new String[masterRecord.length];
        for (int idx = 0; idx < inputRow.length; idx++) {
            duplicateRecord[idx] = inputRow[idx];
        }
        // GID
        duplicateRecord[duplicateRecord.length - extSize] = masterRecord[masterRecord.length - extSize];
        // Group size
        duplicateRecord[duplicateRecord.length - extSize + 1] = String.valueOf(0);
        // Master
        duplicateRecord[duplicateRecord.length - extSize + 2] = String.valueOf(false);
        // Score
        duplicateRecord[duplicateRecord.length - extSize + 3] = String.valueOf(matchingProba);

        int extIdx = 3;
        // Group quality
        if (isSeperateOutput) {
            extIdx++;
            double groupQuality = computeGroupQuality(masterRecord, matchingProba, extIdx);
            masterRecord[duplicateRecord.length - extSize + extIdx] = String.valueOf(groupQuality);
        }
        if (isOutputDistDetails) {
            extIdx++;
            // Match distance details
            duplicateRecord[duplicateRecord.length - extSize + extIdx] = distanceDetails;
        }
        // output the duplicate record
        outputRow(AnalysisRecordGroupingUtils.join(duplicateRecord, delimiter, AnalysisRecordGroupingUtils.ESCAPE_CHARACTER));
    }

    /**
     * @deprecated use {{@link #addMatchRule}
     */
    @Deprecated
    @Override
    public void add(Map<String, String> matchingColumn) {
        matchingColumns.add(matchingColumn);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.hadoop.group.IRecordGrouping#addMatchRule(java.util.List)
     */
    @Override
    public void addMatchRule(List<Map<String, String>> matchRule) {
        multiMatchRules.add(matchRule);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.dataquality.record.linkage.grouping.IRecordGrouping#setSurvivorShipAlgorithmParams(org.talend.dataquality
     * .record.linkage.grouping.swoosh.SurvivorShipAlgorithmParams)
     */
    @Override
    public void setSurvivorShipAlgorithmParams(SurvivorShipAlgorithmParams survivorShipAlgorithmParams) {
        this.survivorShipAlgorithmParams = survivorShipAlgorithmParams;
    }

    public List<List<Map<String, String>>> getMultiMatchRules() {
        return multiMatchRules;
    }

    /**
     * 
     * Output one row
     * 
     * @param row
     */
    protected abstract void outputRow(String row);

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.hadoop.group.IRecordGrouping#initialize()
     */
    @Override
    public void initialize() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        masterRecords.clear();
        for (List<Map<String, String>> matchRule : multiMatchRules) {
            createSimpleRecordMatcher(matchRule);
        }
    }

    private void createSimpleRecordMatcher(List<Map<String, String>> matchRule) throws InstantiationException,
            IllegalAccessException, ClassNotFoundException {
        final int recordSize = matchRule.size();
        double[] arrAttrWeights = new double[recordSize];
        String[] attributeNames = new String[recordSize];
        String[][] algorithmName = new String[recordSize][2];
        String[] arrMatchHandleNull = new String[recordSize];
        String[] customizedJarPath = new String[recordSize];
        double recordMatchThreshold = acceptableThreshold;// keep compatibility to older version.
        int recordIdx = 0;
        for (Map<String, String> recordMap : matchRule) {
            algorithmName[recordIdx][0] = recordMap.get(IRecordGrouping.MATCHING_TYPE);
            if (AttributeMatcherType.DUMMY.name().equals(algorithmName[recordIdx][0])) {
                recordIdx++;
                continue;
            }
            arrAttrWeights[recordIdx] = Double.parseDouble(recordMap.get(IRecordGrouping.CONFIDENCE_WEIGHT));
            algorithmName[recordIdx][1] = recordMap.get(IRecordGrouping.CUSTOMER_MATCH_CLASS);
            attributeNames[recordIdx] = recordMap.get(IRecordGrouping.ATTRIBUTE_NAME);
            arrMatchHandleNull[recordIdx] = recordMap.get(IRecordGrouping.HANDLE_NULL);
            String rcdMathThresholdEach = recordMap.get(IRecordGrouping.RECORD_MATCH_THRESHOLD);
            customizedJarPath[recordIdx] = recordMap.get(IRecordGrouping.JAR_PATH);
            if (!StringUtils.isEmpty(rcdMathThresholdEach)) {
                recordMatchThreshold = Double.valueOf(rcdMathThresholdEach);

            }
            recordIdx++;
        }
        IAttributeMatcher[] attributeMatcher = new IAttributeMatcher[recordSize];

        for (int indx = 0; indx < recordSize; indx++) {
            AttributeMatcherType attrMatcherType = AttributeMatcherType.get(algorithmName[indx][0]);

            if (attrMatcherType == AttributeMatcherType.CUSTOM && customizedJarPath[indx] != null) {
                // Put the jar into class path so that the class can be loaded.
                TalendURLClassLoader cl = new TalendURLClassLoader(
                        CustomAttributeMatcherClassNameConvert.changeJarPathToURLArray(customizedJarPath[indx]));
                attributeMatcher[indx] = AttributeMatcherFactory.createMatcher(attrMatcherType,
                        CustomAttributeMatcherClassNameConvert.getClassName(algorithmName[indx][1]), cl);
            } else {
                // Use the default class loader to load the class.
                attributeMatcher[indx] = AttributeMatcherFactory.createMatcher(attrMatcherType, algorithmName[indx][1]);
            }
            attributeMatcher[indx].setNullOption(arrMatchHandleNull[indx]);
            attributeMatcher[indx].setAttributeName(attributeNames[indx]);

        }
        IRecordMatcher simpleRecordMatcher = RecordMatcherFactory.createMatcher(RecordMatcherType.simpleVSRMatcher);
        simpleRecordMatcher.setRecordSize(recordSize);
        simpleRecordMatcher.setAttributeWeights(arrAttrWeights);
        simpleRecordMatcher.setAttributeMatchers(attributeMatcher);
        simpleRecordMatcher.setRecordMatchThreshold(recordMatchThreshold);
        combinedRecordMatcher.add(simpleRecordMatcher);
    }

    enum MatchAlgoithm {
        VSR,
        TSWOOSH
    }
}
