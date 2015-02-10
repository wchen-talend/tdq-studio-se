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
package org.talend.dataprofiler.core.ui.grid.utils;

/**
 * The interface for TDQObserver
 * 
 */
public interface TDQObserver<T> {

    /**
     * 
     * Execute update action
     * 
     * @param observerable
     */
    public void update(T observerable);
}
