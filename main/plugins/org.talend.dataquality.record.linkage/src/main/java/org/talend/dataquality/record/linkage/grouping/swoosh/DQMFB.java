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

import org.talend.dataquality.matchmerge.mfb.MFB;
import org.talend.dataquality.record.linkage.record.IRecordMatcher;
import org.talend.dataquality.record.linkage.record.IRecordMerger;

/**
 * created by zhao on Jul 10, 2014 MFB algorithm adapted to DQ grouping API, which will continue matching two different
 * groups.
 * 
 */
public class DQMFB extends MFB {

    /**
     * DOC zhao DQMFB constructor comment.
     * 
     * @param matcher
     * @param merger
     */
    public DQMFB(IRecordMatcher matcher, IRecordMerger merger) {
        super(matcher, merger);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.matchmerge.mfb.MFB#isMatchDiffGroups()
     */
    @Override
    protected boolean isMatchDiffGroups() {
        return true;
    }

}
