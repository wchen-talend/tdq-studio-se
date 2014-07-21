package org.talend.dataquality.matchmerge.mfb;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.talend.dataquality.matchmerge.Attribute;
import org.talend.dataquality.matchmerge.Record;
import org.talend.dataquality.record.linkage.attribute.IAttributeMatcher;
import org.talend.dataquality.record.linkage.record.IRecordMatcher;

public class MFBRecordMatcher implements IRecordMatcher {

    private static final Logger LOGGER = Logger.getLogger(MFBRecordMatcher.class);

    private static final double MAX_SCORE = 1;

    private final IRecordMatcher delegate;

    private IAttributeMatcher[] attributeMatchers;

    private final double minConfidenceValue;

    private MFBRecordMatcher(IRecordMatcher delegate, double minConfidenceValue) {
        this.delegate = delegate;
        this.minConfidenceValue = minConfidenceValue;
    }

    public static MFBRecordMatcher wrap(IRecordMatcher matcher, double minConfidenceValue) {
        if (!(matcher instanceof MFBRecordMatcher)) {
            return new MFBRecordMatcher(matcher, minConfidenceValue);
        } else {
            return (MFBRecordMatcher) matcher;
        }
    }

    @Override
    public int getRecordSize() {
        return delegate.getRecordSize();
    }

    @Override
    public void setRecordSize(int numberOfAttributes) {
        delegate.setRecordSize(numberOfAttributes);
    }

    @Override
    public boolean setAttributeWeights(double[] weights) {
        return delegate.setAttributeWeights(weights);
    }

    @Override
    public boolean setAttributeGroups(int[][] groups) {
        return delegate.setAttributeGroups(groups);
    }

    @Override
    public boolean setAttributeMatchers(IAttributeMatcher[] attributeMatchers) {
        this.attributeMatchers = attributeMatchers;
        return delegate.setAttributeMatchers(attributeMatchers);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.record.linkage.record.IRecordMatcher#getAttributeMatchers()
     */
    @Override
    public IAttributeMatcher[] getAttributeMatchers() {
        return attributeMatchers;
    }

    @Override
    public boolean setBlockingAttributeMatchers(int[] attrMatcherIndices) {
        return delegate.setBlockingAttributeMatchers(attrMatcherIndices);
    }

    @Override
    public double getMatchingWeight(String[] record1, String[] record2) {
        return delegate.getMatchingWeight(record1, record2);
    }

    @Override
    public MatchResult getMatchingWeight(Record record1, Record record2) {
        Iterator<Attribute> mergedRecordAttributes = record1.getAttributes().iterator();
        Iterator<Attribute> currentRecordAttributes = record2.getAttributes().iterator();
        double confidence = 0;
        int matchIndex = 0;
        MatchResult result = new MatchResult(record1.getAttributes().size());
        int maxWeight = 0;
        while (mergedRecordAttributes.hasNext()) {
            Attribute left = mergedRecordAttributes.next();
            Attribute right = currentRecordAttributes.next();
            IAttributeMatcher matcher = attributeMatchers[matchIndex];
            // Find the first score to exceed threshold (if any).
            double score = matchScore(left.allValues(), right.allValues(), matcher);
            result.setScore(matchIndex, matcher.getMatchType(), score, left.getValue(), right.getValue());
            result.setThreshold(matchIndex, matcher.getThreshold());
            confidence += score * matcher.getWeight();
            maxWeight += matcher.getWeight();
            matchIndex++;
        }
        double normalizedConfidence = confidence > 0 ? confidence / maxWeight : confidence; // Normalize to 0..1 value
        result.setConfidence(normalizedConfidence);
        if (normalizedConfidence < minConfidenceValue) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Cannot match record: merged record has a too low confidence value (" + normalizedConfidence + " < "
                        + minConfidenceValue + ")");
            }
            return MFB.NonMatchResult.wrap(result);
        }
        record2.setConfidence(normalizedConfidence);
        return result;
    }

    private static double matchScore(Iterator<String> leftValues, Iterator<String> rightValues, IAttributeMatcher matcher) {
        // Find the best score in values
        double maxScore = 0;
        while (leftValues.hasNext()) {
            String leftValue = leftValues.next();
            while (rightValues.hasNext()) {
                String rightValue = rightValues.next();
                double score = matcher.getMatchingWeight(leftValue, rightValue);
                if (score > maxScore) {
                    maxScore = score;
                }
                if (maxScore == MAX_SCORE) {
                    // Can't go higher, no need to perform other checks.
                    return maxScore;
                }
            }
        }
        return maxScore;
    }

    @Override
    public double[] getCurrentAttributeMatchingWeights() {
        return delegate.getCurrentAttributeMatchingWeights();
    }

    @Override
    public String getLabeledAttributeMatchWeights() {
        return delegate.getLabeledAttributeMatchWeights();
    }

    @Override
    public boolean setblockingThreshold(double threshold) {
        return delegate.setblockingThreshold(threshold);
    }

    @Override
    public double getRecordMatchThreshold() {
        return delegate.getRecordMatchThreshold();
    }

    @Override
    public void setRecordMatchThreshold(double recordMatchThreshold) {
        delegate.setRecordMatchThreshold(recordMatchThreshold);
    }

    @Override
    public void setDisplayLabels(boolean displayLabels) {
        delegate.setDisplayLabels(displayLabels);
    }
}
