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

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

/**
 * created by zhao on 2015年4月27日 Detailed comment
 *
 */
public class FileSamplingDataSourceTest {

    private SamplingDataSource<File> fileDataSource = null;

    @Test
    public void testGetRecord() throws Exception {
        fileDataSource = new FileSamplingDataSource();
        fileDataSource.setDataSource(new File(getClass().getClassLoader()
                .getResource("org/talend/dataquality/sampling/simple_data.csv").getFile())); //$NON-NLS-1$
        int idx = 0;
        while (fileDataSource.hasNext()) {
            String value = getString(fileDataSource.getRecord());
            System.out.println(value);
            if (0 == idx) {
                assertEquals("-24,male,4000,2010-10-23,", value); //$NON-NLS-1$
            } else if (1 == idx) {
                assertEquals("-50.0,male,2000,2011-02-02 12:10:00,", value); //$NON-NLS-1$
            } else if (5 == idx) {
                assertEquals(",female,4000,02/01/2008,", value); //$NON-NLS-1$
            } else if (9 == idx) {
                assertEquals("a str,male,30000,2004-12-20 00:00:00,", value); //$NON-NLS-1$
            }
            idx++;
        }
    }

    public String getString(Object[] data) {
        StringBuffer sb = new StringBuffer();
        for (Object o : data) {
            sb.append(o == null ? "" : o.toString()).append(","); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return sb.toString();
    }
}
