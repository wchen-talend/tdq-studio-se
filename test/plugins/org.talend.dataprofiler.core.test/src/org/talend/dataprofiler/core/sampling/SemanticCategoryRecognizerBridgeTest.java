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
package org.talend.dataprofiler.core.sampling;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.talend.dataprofiler.service.semantic.SemanticCategory;
import org.talend.dataquality.sampling.DataSamplingBridge;
import org.talend.dataquality.sampling.FileSamplingDataSource;
import org.talend.dataquality.sampling.SamplingDataSource;
import org.talend.dataquality.sampling.SamplingOption;

/**
 * created by zhao on 2015年4月22日 Detailled comment
 *
 */
public class SemanticCategoryRecognizerBridgeTest {

    @Test
    public void testInferSemanticCategory() throws Exception {
        // 1. Create a datasource instance.
        SamplingDataSource<File> fileDatasource = new FileSamplingDataSource();
        fileDatasource
                .setDataSource(new File(
                        "D:/code/g5/tdq-studio-se/test/plugins/org.talend.dataprofiler.core.test/src/org/talend/dataprofiler/core/sampling/employee.csv")); //$NON-NLS-1$
        // 2. create a sampling bridge instance.
        DataSamplingBridge samplingBridge = new DataSamplingBridge(fileDatasource);
        // 2.1 set sampling option , by default top n if not set.
        samplingBridge.setSamplingOption(SamplingOption.Reservoir);
        // 2.2 set sampling size , by default 1000 if not set.
        samplingBridge.setSampleSize(100);

        // 3. create a semantic category recognizer instance.
        SemanticCategoryRecognizerBridge categoryRecognizer = new SemanticCategoryRecognizerBridge(samplingBridge);
        Map<Integer, List<SemanticCategory>> columnIdx2SemanticCat = categoryRecognizer.inferSemanticCategory();
        Iterator<Integer> columnIdxIter = columnIdx2SemanticCat.keySet().iterator();
        while (columnIdxIter.hasNext()) {
            Integer columnIdx = columnIdxIter.next();
            List<SemanticCategory> categories = columnIdx2SemanticCat.get(columnIdx);
            System.out.println("Column index " + columnIdx);
            if (categories.size() > 0) {
                System.out.println("Semantic Category: " + categories.get(0).getSemanticCategory());
                System.out.println("count: " + categories.get(0).getCount());
                System.out.println("freqency: " + categories.get(0).getFrequencies());
            }
        }
    }
}
