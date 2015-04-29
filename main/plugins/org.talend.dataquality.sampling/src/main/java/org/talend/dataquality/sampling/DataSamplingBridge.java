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
package org.talend.dataquality.sampling;

import java.util.ArrayList;
import java.util.List;

/**
 * created by zhao<br>
 * Bridge from data source extraction to data sample API
 * <p>
 * The call sequencies for following methods must be comply with. <br>
 * 1. {{@link #prepareData()}<br>
 * 2. {{@link #hasNext()}<br>
 * 3. {{@link #getRecord()}<br>
 * 4. {{@link #finalizeDataSampling()}<br>
 * Step 2 and 3 can be exectued several times
 *
 */
public class DataSamplingBridge {

    private SamplingOption samplingOption = SamplingOption.TopN;

    private ReservoirSampler<Object[]> reservoirSampler;

    private List<Object[]> reservoirSamplingData = new ArrayList<Object[]>();

    private int sampleSize = 1000;

    private SamplingDataSource<?> dataSource;

    // The curcor of reservoir sampling records.
    private int recordCursor = 0;

    public DataSamplingBridge(SamplingDataSource<?> ds) {
        this.dataSource = ds;
    }

    public void setSampleSize(int size) {
        sampleSize = size;
    }

    public void setSamplingOption(SamplingOption option) {
        samplingOption = option;
    }

    /**
     * 
     * DOC zhao see if there exist next record or not.
     * 
     * @return true if there is next sampling record, false otherwise.
     * @throws Exception
     */
    public boolean hasNext() throws Exception {
        if (recordCursor >= sampleSize) {
            // Stop getting sample from data soruce.
            return false;
        }
        if (SamplingOption.Reservoir == samplingOption) {
            return recordCursor < reservoirSamplingData.size();
        } else {

            return dataSource.hasNext();
        }
    }

    /**
     * 
     * DOC zhao Do prepare work before getting real data, work such as JDBC or file connection creation.
     * 
     * @return true if success, false otherwise.
     * @throws Exception When unexpected exception occurs
     */
    public boolean prepareData() throws Exception {
        // Reset record cursor
        reservoirSamplingData = new ArrayList<Object[]>();
        switch (samplingOption) {
        case TopN:
            break;
        case Percentage:
            break;
        case Reservoir:
            recordCursor = 0;
            reservoirSampler = new ReservoirSampler<Object[]>(sampleSize, RANDOM_SEED);
            reservoirSampler.clear();
            while (dataSource.hasNext()) {
                reservoirSampler.onNext(dataSource.getRecord());
            }
            reservoirSampler.onCompleted(true);
            reservoirSamplingData = reservoirSampler.sample();
            break;
        default:
            break;
        }
        return false;
    }

    /**
     * 
     * Get one record from iterator of datasoure, note that this function is data-source type dependent such a case is
     * that in JDBC connection, the interation is from ResultSet while in file connection it's from file delimiter API.<br>
     * Before call this method, the method {{@link #hasNext()} should be called to check if there are data available
     * from datasource.
     * 
     * @return true if success, false otherwise
     * @throws Exception occurs when there are unexpected exceptions.
     */
    public Object[] getRecord() throws Exception {
        Object[] records = null;
        switch (samplingOption) {
        case TopN:
            records = dataSource.getRecord();
            break;
        case Percentage:
            break;
        case Reservoir:
            if (reservoirSampler == null) {
                throw new Exception("DataSamplingBridge is not initialized");
            }
            records = reservoirSamplingData.get(recordCursor);
            break;
        default:
            break;
        }
        recordCursor++;
        return records;
    }

    /**
     * 
     * DOC zhao Finallize the data sample , some operation need to be done here such as closing csv file stream.
     * 
     * @return true if success, false otherwise.
     * @throws Exception When unexpected exception occurs
     */
    public boolean finalizeDataSampling() throws Exception {
        reservoirSamplingData = null;
        recordCursor = 0;
        dataSource.finalizeDataSampling();
        return false;
    }

    public static final long RANDOM_SEED = 12345678;

}
