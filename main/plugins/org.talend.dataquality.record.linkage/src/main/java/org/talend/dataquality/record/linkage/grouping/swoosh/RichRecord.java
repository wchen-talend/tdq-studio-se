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

import java.util.Arrays;
import java.util.List;

import org.talend.dataquality.matchmerge.Attribute;
import org.talend.dataquality.matchmerge.Record;

/**
 * A record with original information. (including the columns which is not designated to be match keys) Detailled
 * comment
 * 
 */
public class RichRecord extends Record {

    /**
     * The original row. It is an useful information when the application want to know the original information.
     */
    private String[] originRow = null;

    private boolean isMerged = false;

    private boolean isMaster = false;

    private int grpSize = 0;

    private double score = 0d;

    /**
     * DOC zhao RichRecord constructor .
     * 
     * @param attributes
     * @param id
     * @param timestamp
     * @param source
     */
    public RichRecord(List<Attribute> attributes, String id, long timestamp, String source) {
        super(attributes, id, timestamp, source);
    }

    public RichRecord(String id, long timestamp, String source) {
        super(id, timestamp, source);
    }

    /**
     * Getter for originRow.
     * 
     * @return the originRow
     */
    public String[] getOriginRow() {
        return this.originRow;
    }

    /**
     * Sets the originRow.
     * 
     * @param originRow the originRow to set
     */
    public void setOriginRow(String[] originRow) {
        this.originRow = originRow;
    }

    public void setMaster(boolean isMaster) {
        this.isMaster = isMaster;
    }

    public void setScore(double score) {
        this.score = score;
    }

    /**
     * Getter for isMerged.
     * 
     * @return the isMerged
     */
    public boolean isMerged() {
        return this.isMerged;
    }

    /**
     * Sets the isMerged.
     * 
     * @param isMerged the isMerged to set
     */
    public void setMerged(boolean isMerged) {
        this.isMerged = isMerged;
    }

    /**
     * Getter for grpSize.
     * 
     * @return the grpSize
     */
    public int getGrpSize() {
        return this.grpSize;
    }

    /**
     * Sets the grpSize.
     * 
     * @param grpSize the grpSize to set
     */
    public void setGrpSize(int grpSize) {
        this.grpSize = grpSize;
    }

    /**
     * Getter for isMaster.
     * 
     * @return the isMaster
     */
    public boolean isMaster() {
        return this.isMaster;
    }

    /**
     * Getter for score.
     * 
     * @return the score
     */
    public double getScore() {
        return this.score;
    }

    public String[] getOutputRow() {
        if (originRow == null) {
            return null;
        }
        int extSize = 4;
        if (isMerged()) {
            extSize++;
        }
        String[] row = Arrays.copyOf(originRow, originRow.length + extSize);
        if (isMerged()) {
            // Update the matching key field by the merged attributes.
            List<Attribute> matchKeyAttrs = getAttributes();
            for (Attribute attribute : matchKeyAttrs) {
                row[attribute.getColumnIndex()] = attribute.getValue();
            }
        }
        // GID
        row[originRow.length] = getGroupId();
        // Group size
        row[originRow.length + 1] = String.valueOf(getGrpSize());
        // Master
        row[originRow.length + 2] = String.valueOf(isMaster());
        // Score
        row[originRow.length + 3] = String.valueOf(getScore());

        if (isMerged()) {
            row[originRow.length + 4] = "-MERGED-";
        }
        return row;

    }
}
