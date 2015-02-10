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

import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.nebula.widgets.grid.TalendGridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.talend.dataprofiler.core.model.ModelElementIndicator;
import org.talend.dataprofiler.core.ui.grid.utils.Observerable;
import org.talend.dataprofiler.core.ui.grid.utils.TDQObserver;
import org.talend.dataprofiler.core.ui.grid.utils.events.ObserverEvent;
import org.talend.dataprofiler.core.ui.grid.utils.events.ObserverEventEnum;

/**
 * this customized nebula grid is used for indicator selection.
 */
public class IndicatorSelectGrid extends AbstractIndicatorSelectGrid implements Observerable<ObserverEvent>,
        TDQObserver<ObserverEvent> {

    private List<TDQObserver<ObserverEvent>> observers = null;

    /**
     * DOC talend IndicatorSelectGrid constructor comment.
     * 
     * @param dialog
     * @param parent
     * @param style
     * @param modelElementIndicators
     */
    public IndicatorSelectGrid(IndicatorSelectDialog dialog, Composite parent, int style,
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
    public boolean addObserver(TDQObserver<ObserverEvent> observer) {
        initObserverable();
        return observers.add(observer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.dataprofiler.core.ui.grid.utils.Observerable#removeObserver(org.talend.dataprofiler.core.ui.grid.utils
     * .TalendObserver)
     */
    public boolean removeObserver(TDQObserver<ObserverEvent> observer) {
        if (observers == null) {
            return false;
        }
        return observers.remove(observer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.ui.grid.utils.Observerable#clearObserver()
     */
    public void clearObserver() {
        if (observers == null) {
            return;
        }
        observers.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.ui.grid.utils.Observerable#notifyObservers()
     */
    public void notifyObservers() {
        if (observers == null) {
            return;
        }
        for (TDQObserver<ObserverEvent> observer : observers) {
            ObserverEvent observerEvent = new ObserverEvent(ObserverEventEnum.ItemHeaderWidth);
            observerEvent.putData(ObserverEvent.ITEM_HEADER_WIDTH, this.getItemHeaderWidth());
            observer.update(observerEvent);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.ui.grid.utils.Observerable#initObserverable()
     */
    public void initObserverable() {
        if (observers == null) {
            observers = new ArrayList<TDQObserver<ObserverEvent>>();
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

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.ui.grid.utils.TDQObserver#update(java.lang.Object)
     */
    public void update(ObserverEvent observerEvent) {
        switch (observerEvent.getEventType()) {
        case ColumnResize:
            Object data = observerEvent.getData(ObserverEvent.COLUMN_HEADER_RESIZE);
            if (data == null) {
                return;
            }
            GridColumn sourceGridColumn = ((GridColumn) data);
            for (GridColumn currColumn : this.getColumns()) {
                Object oldmodelElementIndicator = sourceGridColumn.getData();
                if (oldmodelElementIndicator == currColumn.getData()) {
                    currColumn.setWidth(sourceGridColumn.getWidth());
                    break;
                }
            }
            break;

        case MoveColumn:
            data = observerEvent.getData(ObserverEvent.COLUMN_HEADER_MOVE);
            if (data == null) {
                return;
            }
            this.setColumnOrder((int[]) data);
            if (result == null) {
                result = new ModelElementIndicator[getColumnCount() - 2];
            }
            int[] order = getColumnOrder();
            int j = 0;
            for (int columnIndex : order) {
                if (columnIndex > 1) {
                    result[j] = _modelElementIndicators[columnIndex - 2]; // indicator selection starts from the 3rd
                                                                          // column
                    j++;
                }
            }
            redraw();
            break;
        case HSrcollMove:
            data = observerEvent.getData(ObserverEvent.HORIZONTAL_SCROLLBAR_MOVE);
            if (data == null || getHorizontalBar() == null) {
                return;
            }
            getHorizontalBar().setSelection(Integer.parseInt(data.toString()));
            redraw(getClientArea().x, getClientArea().y, getClientArea().width, getClientArea().height, false);
            break;
        case VSrcollVisible:
            data = observerEvent.getData(ObserverEvent.VERTICAL_SRCOLL_VISABLE);

            if (data == null || getVerticalBar() == null) {
                return;
            }
            if (!getVerticalBar().isVisible() && Boolean.parseBoolean(data.toString())) {
                // make current table bounds change to small
                GridData previewGridData = (GridData) this.getLayoutData();
                previewGridData.widthHint = this.getBounds().width - 70;
                previewGridData.minimumWidth = this.getBounds().width - 70;
                previewGridData.horizontalAlignment = SWT.BEGINNING;
                this.getParent().layout();
            }
            if (!getVerticalBar().isVisible() && !Boolean.parseBoolean(data.toString())) {
                // make current table bounds change to big
                GridData previewGridData = (GridData) this.getLayoutData();
                if (previewGridData.horizontalAlignment == SWT.FILL) {
                    return;
                }
                previewGridData.minimumWidth = 650;
                previewGridData.horizontalAlignment = SWT.FILL;
                notifyVerticalBarShown(false);
            }
            break;
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.ui.grid.AbstractIndicatorSelectGrid#notifyHscrollSelectionChange()
     */
    @Override
    protected void notifyHscrollSelectionChange() {
        if (observers == null) {
            return;
        }
        for (TDQObserver<ObserverEvent> observer : observers) {
            ObserverEvent observerEvent = new ObserverEvent(ObserverEventEnum.HSrcollMove);
            observerEvent.putData(ObserverEvent.HORIZONTAL_SCROLLBAR_MOVE, this.getHorizontalBar().getSelection());
            observer.update(observerEvent);
        }
    }

    /**
     * DOC talend Comment method "notifyVerticalBarVisible".
     * 
     * @param observer
     */
    @Override
    protected void notifyVerticalBarShown(boolean show) {
        if (observers == null) {
            return;
        }
        for (TDQObserver<ObserverEvent> observer : observers) {
            ObserverEvent observerEvent = new ObserverEvent(ObserverEventEnum.VSrcollVisible);
            observerEvent.putData(ObserverEvent.VERTICAL_SRCOLL_VISABLE, show);
            observer.update(observerEvent);
        }

    }

    /**
     * DOC talend Comment method "hideInvalidItem".
     * 
     * @param selection
     */
    public void hideInvalidItem(boolean selection) {
        for (GridItem item : this.getItems()) {
            if (TalendGridItem.class.isInstance(item)) {
                TalendGridItem talendItem = (TalendGridItem) item;
                if (!talendItem.getCheckable()) {
                    talendItem.hideItem(selection);

                }
            }
        }
        redraw();

    }

}
