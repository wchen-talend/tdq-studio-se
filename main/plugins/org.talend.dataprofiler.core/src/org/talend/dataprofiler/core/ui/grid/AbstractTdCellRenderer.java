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

import org.eclipse.nebula.widgets.grid.GridCellRenderer;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.nebula.widgets.grid.internal.TextUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 * Abstract class for TdCellRenderer this renderer is fouse on cell of item
 * 
 */
public class AbstractTdCellRenderer extends GridCellRenderer {

    protected int leftMargin = 4;

    protected int rightMargin = 4;

    protected int topMargin = 0;

    protected int bottomMargin = 0;

    protected int insideMargin = 3;

    static final Color Black = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);

    int textTopMargin = 1;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.nebula.widgets.grid.IInternalWidget#notify(int, org.eclipse.swt.graphics.Point,
     * java.lang.Object)
     */
    public boolean notify(int event, Point point, Object value) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.nebula.widgets.grid.IRenderer#paint(org.eclipse.swt.graphics.GC, java.lang.Object)
     */
    public void paint(GC gc, Object value) {
        GridItem item = (GridItem) value;
        gc.setAntialias(SWT.ON);

        int column = getColumn();
        boolean checkable = item.getCheckable(column);
        boolean checked = item.getChecked(column);

        // fill background rectangle
        Color systemBackColor = getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
        gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

        int originX = getBounds().x;
        gc.fillRectangle(originX, getBounds().y, getBounds().width, getBounds().height);

        // draw highlight color as background
        Color highlight = item.getBackground(column);
        if (highlight != null) {
            gc.setBackground(highlight);
            gc.fillRectangle(originX, getBounds().y, getBounds().width, getBounds().height);
        }
        // draw text
        int x = 0;
        int width = getBounds().width;
        String text = TextUtils.getShortString(gc, item.getText(getColumn()), width);

        if (getAlignment() == SWT.RIGHT) {
            int len = gc.stringExtent(text).x;
            if (len < width) {
                x += width - len;
            }
        } else if (getAlignment() == SWT.CENTER) {
            int len = gc.stringExtent(text).x;
            if (len < width) {
                x += (width - len) / 2;
            }
        }
        gc.setForeground(Black);
        gc.drawString(text, getBounds().x + x, getBounds().y + textTopMargin + topMargin, true);

        if (item.getParent().getLinesVisible()) {
            gc.setForeground(item.getParent().getLineColor());
            gc.drawLine(originX, getBounds().y + getBounds().height, originX + getBounds().width - 1, getBounds().y
                    + getBounds().height);
            gc.drawLine(originX + getBounds().width - 1, getBounds().y, originX + getBounds().width - 1, getBounds().y
                    + getBounds().height);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.nebula.widgets.grid.IRenderer#computeSize(org.eclipse.swt.graphics.GC, int, int,
     * java.lang.Object)
     */
    public Point computeSize(GC gc, int wHint, int hHint, Object value) {
        GridItem item = (GridItem) value;

        gc.setFont(item.getFont(getColumn()));

        int x = 0;

        x += leftMargin;

        if (isCheck()) {
            x += getBounds().width + insideMargin;
        }

        int y = 0;

        Image image = item.getImage(getColumn());
        if (image != null) {
            y = topMargin + image.getBounds().height + bottomMargin;

            x += image.getBounds().width + insideMargin;
        }

        return new Point(x, y);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.nebula.widgets.grid.GridCellRenderer#getTextBounds(org.eclipse.nebula.widgets.grid.GridItem,
     * boolean)
     */
    @Override
    public Rectangle getTextBounds(GridItem item, boolean preferred) {
        int x = leftMargin;

        Image image = item.getImage(getColumn());
        if (image != null) {
            x += image.getBounds().width + insideMargin;
        }

        Rectangle bounds = new Rectangle(x, topMargin + textTopMargin, 0, 0);

        GC gc = new GC(item.getParent());
        gc.setFont(item.getFont(getColumn()));
        Point size = gc.stringExtent(item.getText(getColumn()));

        bounds.height = size.y;

        if (preferred) {
            bounds.width = size.x - 1;
        } else {
            bounds.width = getBounds().width - x - rightMargin;
        }

        gc.dispose();

        return bounds;
    }

}
