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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.talend.dataquality.matchmerge.Attribute;
import org.talend.dataquality.matchmerge.Record;
import org.talend.dataquality.matchmerge.mfb.RecordGenerator;
import org.talend.dataquality.matchmerge.mfb.RecordIterator;

/**
 * created by zhao on Jul 9, 2014 Detailled comment
 * 
 */
public class DQRecordIterator extends RecordIterator {

    /**
     * DOC zhao DQValueIterator constructor comment.
     * 
     * @param size
     * @param generators
     */
    public DQRecordIterator(int size, List<RecordGenerator> generators) {
        super(size, generators);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.matchmerge.mfb.ValuesIterator#createRecord(java.util.Vector)
     */
    @Override
    protected Record createRecord(Vector<Attribute> attriVector, List<DQAttribute<?>> originalRow) {
        // Sort the attributes in the vector by column index.
        Collections.sort(attriVector, new Comparator<Attribute>() {

            @Override
            public int compare(Attribute o1, Attribute o2) {
                return o1.getColumnIndex() - o2.getColumnIndex();
            }

        });
        RichRecord record = new RichRecord(attriVector, String.valueOf(currentIndex - 1), timestamp++, "MFB"); //$NON-NLS-1$
        record.setOriginRow(originalRow);
        record.setRecordSize(originalRow.size());
        return record;
    }

}
