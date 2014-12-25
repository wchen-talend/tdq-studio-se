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
package org.talend.dataprofiler.common.ui.editor.preview;

import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.talend.dataprofiler.common.ui.CommonUIPlogin;
import org.talend.dataprofiler.service.ITOPChartService;

/**
 * created by yyin on 2014-12-15 Detailled comment
 * 
 */
public class TOPChartUtil {

    private static TOPChartUtil instance;

    private ITOPChartService chartService;

    public static TOPChartUtil getInstance() {
        if (instance == null) {
            instance = new TOPChartUtil();
        }
        return instance;
    }

    public boolean isTOPChartInstalled() {
        initTOPChartService(false);
        return this.chartService != null;
    }

    /**
     * DOC yyin Comment method "initTOPChartService".
     * 
     * @param b
     */
    private void initTOPChartService(boolean b) {
        if (this.chartService == null) {
            BundleContext context = CommonUIPlogin.getDefault().getBundleContext();
            if (context == null) {
                return;
            }

            ServiceReference serviceReference = context.getServiceReference(ITOPChartService.class.getName());
            if (serviceReference != null) {
                Object obj = context.getService(serviceReference);
                if (obj != null) {
                    this.chartService = (ITOPChartService) obj;
                }
            }
        }
    }

    public Object createDefaultCategoryDataset() {
        if (isTOPChartInstalled()) {
            return chartService.createDefaultCategoryDataset();
        }
        return null;
    }

    public void addValueToCategoryDataset(Object dataset, double value, String labelX, String labelY) {
        if (isTOPChartInstalled()) {
            chartService.addValueToCategoryDataset(dataset, value, labelX, labelY);
        }
    }

    public int getRowCount(Object dataset) {
        if (isTOPChartInstalled()) {
            return chartService.getRowCount(dataset);
        }
        return Integer.MIN_VALUE;
    }

    public int getColumnCount(Object dataset) {
        if (isTOPChartInstalled()) {
            return chartService.getColumnCount(dataset);
        }
        return Integer.MIN_VALUE;
    }

    public Number getValue(Object dataset, int row, int column) {
        if (isTOPChartInstalled()) {
            return chartService.getValue(dataset, row, column);
        }
        return Integer.MIN_VALUE;
    }

    public Comparable getRowKey(Object dataset, int row) {
        if (isTOPChartInstalled()) {
            return chartService.getRowKey(dataset, row);
        }
        return Integer.MIN_VALUE;
    }

    public int getRowIndex(Object dataset, Comparable key) {
        if (isTOPChartInstalled()) {
            return chartService.getRowIndex(dataset, key);
        }
        return Integer.MIN_VALUE;
    }

    public List getRowKeys(Object dataset) {
        if (isTOPChartInstalled()) {
            return chartService.getRowKeys(dataset);
        }
        return null;
    }

    public Comparable getColumnKey(Object dataset, int column) {
        if (isTOPChartInstalled()) {
            return chartService.getColumnKey(dataset, column);
        }
        return Integer.MIN_VALUE;
    }

    public int getColumnIndex(Object dataset, Comparable key) {
        if (isTOPChartInstalled()) {
            return chartService.getColumnIndex(dataset, key);
        }
        return Integer.MIN_VALUE;
    }

    public List getColumnKeys(Object dataset) {
        if (isTOPChartInstalled()) {
            return chartService.getColumnKeys(dataset);
        }
        return null;
    }

    public Number getValue(Object dataset, Comparable rowKey, Comparable columnKey) {
        if (chartService != null) {
            return chartService.getValue(dataset, rowKey, columnKey);
        }
        return Integer.MIN_VALUE;
    }

    public void clearDataset(Object dataset) {
        if (chartService != null) {
            chartService.clearDataset(dataset);
        }
    }

}
