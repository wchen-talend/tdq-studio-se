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
package org.talend.dataquality.record.linkage.grouping.swoosh;

import org.talend.dataquality.matchmerge.Record;
import org.talend.dataquality.matchmerge.mfb.MFBRecordMerger;
import org.talend.dataquality.record.linkage.utils.SurvivorShipAlgorithmEnum;

/**
 * created by zhao on Jul 9, 2014 the merger which adapt to incorporate DQ specific grouing information.
 * 
 */
public class DQMFBRecordMerger extends MFBRecordMerger {

    /**
     * DOC zhao DQMFBRecordMerger constructor comment.
     * 
     * @param mergedRecordSource
     * @param parameters
     * @param typeMergeTable
     */
    public DQMFBRecordMerger(String mergedRecordSource, String[] parameters, SurvivorShipAlgorithmEnum[] typeMergeTable) {
        super(mergedRecordSource, parameters, typeMergeTable);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.dataquality.matchmerge.mfb.MFBRecordMerger#createNewRecord(org.talend.dataquality.matchmerge.Record,
     * long)
     */
    @Override
    protected Record createNewRecord(Record record1, long mergedRecordTimestamp) {
        RichRecord richRecord1 = (RichRecord) record1;
        RichRecord richRecord = new RichRecord(record1.getId(), mergedRecordTimestamp, mergedRecordSource);
        richRecord.setOriginRow(richRecord1.getOriginRow());
        return richRecord;
    }
}
