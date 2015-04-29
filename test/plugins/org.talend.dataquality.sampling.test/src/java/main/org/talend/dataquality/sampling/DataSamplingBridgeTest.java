package org.talend.dataquality.sampling;

import java.io.File;

import org.junit.Test;

public class DataSamplingBridgeTest {

    @Test
    public void testGetRecord() throws Exception {
        // 1. Create a data source from csv file.
        SamplingDataSource<File> fileDatasource = new FileSamplingDataSource();
        fileDatasource.setDataSource(new File(getClass().getClassLoader()
                .getResource("org/talend/dataquality/sampling/simple_data.csv").getFile())); //$NON-NLS-1$

        // 2. create a sampling bridge instance.
        DataSamplingBridge samplingBridge = new DataSamplingBridge(fileDatasource);
        // 2.1 set sampling option , by default top n if not set.
        samplingBridge.setSamplingOption(SamplingOption.Reservoir);
        // 2.2 set sampling size , by default 1000 if not set.
        samplingBridge.setSampleSize(100);
        // 2.3 prepare the data sampling data source.
        samplingBridge.prepareData();
        // 2.4 get data
        while (samplingBridge.hasNext()) {
            System.out.println(getString(samplingBridge.getRecord()));
        }

        // 3. Finallize the sampling.
        samplingBridge.finalizeDataSampling();
    }

    public String getString(Object[] data) {
        StringBuffer sb = new StringBuffer();
        for (Object o : data) {
            sb.append(o == null ? "" : o.toString()).append(",");
        }
        return sb.toString();
    }
}
