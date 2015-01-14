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
 * created by talend on Dec 26, 2014 Detailled comment
 * 
 */
public interface Observerable<T> {

    boolean addObserver(TDQObserver<T> observer);

    boolean removeObserver(TDQObserver<T> observer);

    void clearObserver();

    void notifyObservers();
}
