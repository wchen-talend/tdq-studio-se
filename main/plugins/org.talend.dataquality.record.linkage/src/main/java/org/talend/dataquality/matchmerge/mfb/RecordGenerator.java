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
package org.talend.dataquality.matchmerge.mfb;

import java.util.HashMap;
import java.util.Map;

import org.talend.dataquality.matchmerge.mfb.RecordIterator.ValueGenerator;

/**
 * Record generator with original row.
 * 
 */
public class RecordGenerator {

    private Map<String, ValueGenerator> matchKeyMap = new HashMap<String, ValueGenerator>();

    private String[] originalRow = null;

    /**
     * Getter for matchKeyMap.
     * 
     * @return the matchKeyMap
     */
    public Map<String, ValueGenerator> getMatchKeyMap() {
        return this.matchKeyMap;
    }

    /**
     * Sets the matchKeyMap.
     * 
     * @param matchKeyMap the matchKeyMap to set
     */
    public void setMatchKeyMap(Map<String, ValueGenerator> matchKeyMap) {
        this.matchKeyMap = matchKeyMap;
    }

    /**
     * Getter for originalRow.
     * 
     * @return the originalRow
     */
    public String[] getOriginalRow() {
        return this.originalRow;
    }

    /**
     * Sets the originalRow.
     * 
     * @param originalRow the originalRow to set
     */
    public void setOriginalRow(String[] originalRow) {
        this.originalRow = originalRow;
    }

}
