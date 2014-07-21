package org.talend.dataquality.record.linkage.grouping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.dataquality.record.linkage.grouping.AbstractRecordGrouping.MatchAlgoithm;
import org.talend.dataquality.record.linkage.grouping.swoosh.SurvivorShipAlgorithmParams;
import org.talend.dataquality.record.linkage.grouping.swoosh.SurvivorShipAlgorithmParams.SurvivorshipFunction;
import org.talend.dataquality.record.linkage.utils.SurvivorShipAlgorithmEnum;

public class SwooshRecordGroupingTest {

    private static Logger log = Logger.getLogger(SwooshRecordGroupingTest.class);

    /**
     * The input data.
     */
    private List<String[]> inputList = null;

    private IRecordGrouping recordGroup = null;

    private static final String columnDelimiter = "|"; //$NON-NLS-1$

    private List<String[]> groupingRecords = new ArrayList<String[]>();

    @Before
    public void setUp() throws Exception {

        groupingRecords.clear();

    }

    @Test
    public void testDoGroup() throws IOException {
        // account_num , last name, first name, middle initial, address, city, state, zipcode, blocking key
        // Read input data file.
        InputStream in = this.getClass().getResourceAsStream("incoming_customers_swoosh1.txt"); //$NON-NLS-1$
        BufferedReader bfr = new BufferedReader(new InputStreamReader(in));
        List<String> listOfLines = IOUtils.readLines(bfr);
        inputList = new ArrayList<String[]>();
        for (String line : listOfLines) {
            String[] fields = StringUtils.splitPreserveAllTokens(line, columnDelimiter);
            inputList.add(fields);
        }

        // set the matching parameters
        // matching parameters for lname
        recordGroup = new AbstractRecordGrouping() {

            /*
             * (non-Javadoc)
             * 
             * @see org.talend.dataquality.record.linkage.grouping.AbstractRecordGrouping#outputRow(java.lang.String)
             */
            @Override
            protected void outputRow(String row) {
                System.out.println(row);
                groupingRecords.add(StringUtils.splitByWholeSeparatorPreserveAllTokens(row, columnDelimiter));
            }
        };
        recordGroup.setRecordLinkAlgorithm(MatchAlgoithm.TSWOOSH);
        SurvivorShipAlgorithmParams survivorShipAlgorithmParams = new SurvivorShipAlgorithmParams();
        SurvivorshipFunction func = survivorShipAlgorithmParams.new SurvivorshipFunction();
        func.setParameter(""); //$NON-NLS-1$
        func.setSurvivorShipFunction(SurvivorShipAlgorithmEnum.MOST_COMMON);
        survivorShipAlgorithmParams.setSurviorShipAlgos(new SurvivorshipFunction[] { func });
        recordGroup.setSurvivorShipAlgorithmParams(survivorShipAlgorithmParams);

        recordGroup.setColumnDelimiter(columnDelimiter);
        recordGroup.setIsLinkToPrevious(Boolean.FALSE);
        List<Map<String, String>> matchingRule = new ArrayList<Map<String, String>>();

        Map<String, String> lnameRecords = new HashMap<String, String>();
        lnameRecords.put(IRecordGrouping.COLUMN_IDX, String.valueOf(0));
        lnameRecords.put(IRecordGrouping.ATTRIBUTE_NAME, "ID");
        lnameRecords.put(IRecordGrouping.MATCHING_TYPE, "JARO_WINKLER"); //$NON-NLS-1$
        lnameRecords.put(IRecordGrouping.CONFIDENCE_WEIGHT, String.valueOf(1));

        matchingRule.add(lnameRecords);

        recordGroup.addMatchRule(matchingRule);
        try {
            recordGroup.initialize();
        } catch (InstantiationException e) {
            log.error(e.getMessage(), e);
            Assert.fail();
        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e);
            Assert.fail();
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage(), e);
            Assert.fail();
        }

        recordGroup.setIsOutputDistDetails(true);
        recordGroup.setAcceptableThreshold(0.95f);
        // loop on all input rows.
        try {
            for (String[] inputRow : inputList) {
                recordGroup.doGroup(inputRow);
            }
            recordGroup.end();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }

        // Assertions

        for (String[] rds : groupingRecords) {
            if (rds[0].equals("2") && rds[11].equals("true")) { //$NON-NLS-1$ //$NON-NLS-2$
                // The group size should be 2 for master record which id is 2
                Assert.assertEquals(2, Integer.valueOf(rds[rds.length - 4]).intValue());
            } else if (rds[0].equals("7") && rds[11].equals("true")) { //$NON-NLS-1$ //$NON-NLS-2$
                // The group size should be 2 for master record which id is 7
                Assert.assertEquals(2, Integer.valueOf(rds[rds.length - 4]).intValue());
            } else if (rds[0].equals("1") && rds[11].equals("true")) { //$NON-NLS-1$ //$NON-NLS-2$
                // The group size should be 3 for master record which id is 1
                Assert.assertEquals(3, Integer.valueOf(rds[rds.length - 4]).intValue());
            }
        }

    }

    @Test
    public void testDoGroupMergeValues() throws IOException {
        InputStream in = this.getClass().getResourceAsStream("incoming_customers_swoosh2.txt"); //$NON-NLS-1$
        BufferedReader bfr = new BufferedReader(new InputStreamReader(in));
        List<String> listOfLines = IOUtils.readLines(bfr);
        inputList = new ArrayList<String[]>();
        for (String line : listOfLines) {
            String[] fields = StringUtils.splitPreserveAllTokens(line, columnDelimiter);
            inputList.add(fields);
        }
        recordGroup = new AbstractRecordGrouping() {

            /*
             * (non-Javadoc)
             * 
             * @see org.talend.dataquality.record.linkage.grouping.AbstractRecordGrouping#outputRow(java.lang.String)
             */
            @Override
            protected void outputRow(String row) {
                System.out.println(row);
                groupingRecords.add(StringUtils.splitByWholeSeparatorPreserveAllTokens(row, columnDelimiter));
            }
        };
        recordGroup.setRecordLinkAlgorithm(MatchAlgoithm.TSWOOSH);
        SurvivorShipAlgorithmParams survivorShipAlgorithmParams = new SurvivorShipAlgorithmParams();
        SurvivorshipFunction func = survivorShipAlgorithmParams.new SurvivorshipFunction();
        func.setParameter(""); //$NON-NLS-1$
        func.setSurvivorShipFunction(SurvivorShipAlgorithmEnum.MOST_COMMON);
        survivorShipAlgorithmParams.setSurviorShipAlgos(new SurvivorshipFunction[] { func });
        recordGroup.setSurvivorShipAlgorithmParams(survivorShipAlgorithmParams);

        recordGroup.setColumnDelimiter(columnDelimiter);
        recordGroup.setIsLinkToPrevious(Boolean.FALSE);
        List<Map<String, String>> matchingRule = new ArrayList<Map<String, String>>();

        Map<String, String> lnameRecords = new HashMap<String, String>();
        lnameRecords.put(IRecordGrouping.COLUMN_IDX, String.valueOf(1));
        lnameRecords.put(IRecordGrouping.ATTRIBUTE_NAME, "NAME");
        lnameRecords.put(IRecordGrouping.MATCHING_TYPE, "JARO_WINKLER"); //$NON-NLS-1$
        lnameRecords.put(IRecordGrouping.CONFIDENCE_WEIGHT, String.valueOf(1));

        matchingRule.add(lnameRecords);

        recordGroup.addMatchRule(matchingRule);
        try {
            recordGroup.initialize();
        } catch (InstantiationException e) {
            log.error(e.getMessage(), e);
            Assert.fail();
        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e);
            Assert.fail();
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage(), e);
            Assert.fail();
        }

        recordGroup.setIsOutputDistDetails(true);
        recordGroup.setAcceptableThreshold(0.95f);
        // loop on all input rows.
        try {
            for (String[] inputRow : inputList) {
                recordGroup.doGroup(inputRow);
            }
            recordGroup.end();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }

        // Assertions

        for (String[] rds : groupingRecords) {
            if (rds[11].equals("true")) { //$NON-NLS-1$
                // Master record's group size is 4
                Assert.assertEquals(4, Integer.valueOf(rds[rds.length - 4]).intValue());
                // Assert the merged value is the "most common" value.
                Assert.assertEquals("Amburgay", rds[1]);
            }
        }

    }
}
