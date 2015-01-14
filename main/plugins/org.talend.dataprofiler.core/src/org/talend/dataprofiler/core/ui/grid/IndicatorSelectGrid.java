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
package org.talend.dataprofiler.core.ui.grid;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.talend.dataprofiler.core.model.ModelElementIndicator;
import org.talend.dataprofiler.core.ui.grid.utils.Observerable;
import org.talend.dataprofiler.core.ui.grid.utils.TDQObserver;

/**
 * this customized nebula grid is used for indicator selection.
 */
public class IndicatorSelectGrid extends AbstractIndicatorSelectGrid implements Observerable<AbstractIndicatorSelectGrid> {

    private List<TDQObserver<AbstractIndicatorSelectGrid>> Observers = null;

    /**
     * DOC talend IndicatorSelectGrid constructor comment.
     * 
     * @param dialog
     * @param parent
     * @param style
     * @param modelElementIndicators
     */
    public IndicatorSelectGrid(IndicatorSelectDialog2 dialog, Composite parent, int style,
            ModelElementIndicator[] modelElementIndicators) {
        super(dialog, parent, style, modelElementIndicators);
    }

    /**
     * DOC talend IndicatorSelectGrid constructor comment.
     * 
     * @param dialog
     * @param parent
     * @param style
     * @param modelElementIndicators
     */
    public IndicatorSelectGrid(IndicatorSelectDialog3 dialog, Composite parent, int style,
            ModelElementIndicator[] modelElementIndicators) {
        super(dialog, parent, style, modelElementIndicators);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.widgets.Control#redraw()
     */
    @Override
    public void redraw() {
        super.redraw();
        notifyObservers();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.dataprofiler.core.ui.grid.utils.Observerable#addObserver(org.talend.dataprofiler.core.ui.grid.utils
     * .TalendObserver)
     */
    public boolean addObserver(TDQObserver<AbstractIndicatorSelectGrid> observer) {
        initObserverable();
        return Observers.add(observer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.dataprofiler.core.ui.grid.utils.Observerable#removeObserver(org.talend.dataprofiler.core.ui.grid.utils
     * .TalendObserver)
     */
    public boolean removeObserver(TDQObserver<AbstractIndicatorSelectGrid> observer) {
        if (Observers == null) {
            return false;
        }
        return Observers.remove(observer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.ui.grid.utils.Observerable#clearObserver()
     */
    public void clearObserver() {
        if (Observers == null) {
            return;
        }
        Observers.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.ui.grid.utils.Observerable#notifyObservers()
     */
    public void notifyObservers() {
        if (Observers == null) {
            return;
        }
        for (TDQObserver<AbstractIndicatorSelectGrid> observer : Observers) {
            observer.update(this);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.ui.grid.utils.Observerable#initObserverable()
     */
    public void initObserverable() {
        if (Observers == null) {
            Observers = new ArrayList<TDQObserver<AbstractIndicatorSelectGrid>>();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.ui.grid.AbstractIndicatorSelectGrid#getColumnHeaderRenderer()
     */
    @Override
    protected AbstractColumnHerderRenderer getColumnHeaderRenderer() {
        return new TdColumnEmptyHeaderRenderer();
    }

}
