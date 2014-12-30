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
package org.talend.dataprofiler.core.ui.grid;

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridCellRenderer;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.nebula.widgets.grid.IInternalWidget;
import org.eclipse.nebula.widgets.grid.internal.BranchRenderer;
import org.eclipse.nebula.widgets.grid.internal.TextUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;

/**
 * The renderer for a cell in Grid.
 */
public class TdRowHeaderRenderer extends GridCellRenderer {

    int leftMargin = 4;

    int rightMargin = 4;

    int topMargin = 0;

    int bottomMargin = 0;

    int textTopMargin = 4;

    int textBottomMargin = 2;

    private int insideMargin = 3;

    int treeIndent = 15;

    private TdToggleRenderer toggleRenderer;

    private BranchRenderer branchRenderer;

    private TextLayout textLayout;

    /**
     * {@inheritDoc}
     */
    public void paint(GC gc, Object value) {
        GridItem item = (GridItem) value;

        gc.setFont(item.getFont(getColumn()));

        if (item.getParent().isEnabled()) {
            Color back = item.getBackground(getColumn());

            if (back != null) {
                gc.setBackground(back);
            }
        } else {
            gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        }
        gc.setForeground(item.getForeground(getColumn()));

        gc.fillRectangle(getBounds());

        int x = leftMargin;

        if (isTree()) {
            boolean renderBranches = item.getParent().getTreeLinesVisible();
            if (renderBranches) {
                branchRenderer.setBranches(getBranches(item));
                branchRenderer.setIndent(treeIndent);
                branchRenderer.setBounds(getBounds().x + x, getBounds().y, getToggleIndent(item), getBounds().height + 1); // Take
                                                                                                                           // into
                                                                                                                           // account
                                                                                                                           // border
            }

            x += getToggleIndent(item);

            toggleRenderer.setExpanded(item.isExpanded());

            toggleRenderer.setHover(getHoverDetail().equals("toggle"));

            toggleRenderer.setLocation(getBounds().x + x, (getBounds().height - toggleRenderer.getBounds().height) / 2
                    + getBounds().y);
            if (item.hasChildren()) {
                toggleRenderer.paint(gc, null);
            }

            if (renderBranches) {
                branchRenderer.setToggleBounds(toggleRenderer.getBounds());
                branchRenderer.paint(gc, null);
            }

            x += toggleRenderer.getBounds().width + insideMargin;

        }

        Image image = item.getImage(getColumn());
        if (image != null) {
            int y = getBounds().y;

            y += (getBounds().height - image.getBounds().height) / 2;

            gc.drawImage(image, getBounds().x + x, y);

            x += image.getBounds().width + insideMargin;
        }

        int width = getBounds().width - x - rightMargin;

        gc.setForeground(item.getForeground(getColumn()));

        if (!isWordWrap()) {
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

            gc.drawString(text, getBounds().x + x, getBounds().y + textTopMargin + topMargin, true);
        } else {
            if (textLayout == null) {
                textLayout = new TextLayout(gc.getDevice());
                item.getParent().addDisposeListener(new DisposeListener() {

                    public void widgetDisposed(DisposeEvent e) {
                        textLayout.dispose();
                    }
                });
            }
            textLayout.setFont(gc.getFont());
            textLayout.setText(item.getText(getColumn()));
            textLayout.setAlignment(getAlignment());
            textLayout.setWidth(width < 1 ? 1 : width);
            if (item.getParent().isAutoHeight()) {
                // Look through all columns (except this one) to get the max height needed for this item
                int columnCount = item.getParent().getColumnCount();
                int maxHeight = textLayout.getBounds().height + textTopMargin + textBottomMargin;
                for (int i = 0; i < columnCount; i++) {
                    GridColumn column = item.getParent().getColumn(i);
                    if (i != getColumn() && column.getWordWrap()) {
                        int height = column.getCellRenderer().computeSize(gc, column.getWidth(), SWT.DEFAULT, item).y;
                        maxHeight = Math.max(maxHeight, height);
                    }
                }

                // Also look at the row header if necessary
                if (item.getParent().isWordWrapHeader()) {
                    int height = item.getParent().getRowHeaderRenderer().computeSize(gc, SWT.DEFAULT, SWT.DEFAULT, item).y;
                    maxHeight = Math.max(maxHeight, height);
                }

                if (maxHeight != item.getHeight()) {
                    item.setHeight(maxHeight);
                }
            }
            textLayout.draw(gc, getBounds().x + x, getBounds().y + textTopMargin + topMargin);
        }

        boolean checkable = item.getCheckable(1);
        boolean checked = item.getChecked(1);

        // fill background rectangle
        Color systemBackColor = getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
        if (checkable) {
            gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        } else {
            gc.setBackground(IndicatorSelectGrid.gray);
        }

        int originX = getBounds().x + getBounds().width - 50;
        gc.fillRectangle(originX, getBounds().y, 50, getBounds().height);

        // show row select cells
        if (checkable) {

            // draw highlight color as background
            Color highlight = item.getBackground(1);
            if (highlight != null) {
                gc.setBackground(highlight);
                gc.fillRectangle(originX, getBounds().y, 50, getBounds().height);
            }

            if (checked) {
                // draw background oval
                int offset = 50 - getBounds().height;
                gc.setBackground(IndicatorSelectGrid.blue);
                gc.fillOval(originX + offset / 2 + 1, getBounds().y + 1, 50 - offset - 2, getBounds().height - 2);

                // draw a white oval for partially selected cells
                if (item.getGrayed(1)) {
                    gc.setBackground(systemBackColor);
                    gc.setAlpha(160);
                    gc.fillOval(originX + offset / 2 + 1, getBounds().y + 1, 50 - offset - 2, getBounds().height - 2);
                    gc.setAlpha(-1);
                }

                // draw tick image
                if (highlight != null) {
                    gc.setForeground(highlight);
                } else {
                    gc.setForeground(systemBackColor);
                }
                gc.setLineWidth(3);
                gc.drawLine(originX + 18, getBounds().y + 11, originX + 23, getBounds().y + 16);
                gc.drawLine(originX + 21, getBounds().y + 16, originX + 31, getBounds().y + 6);
                gc.setLineWidth(1);
            }

        }

        if (item.getParent().getLinesVisible()) {
            gc.setForeground(item.getParent().getLineColor());
            gc.drawLine(getBounds().x, getBounds().y + getBounds().height, getBounds().x + getBounds().width - 1, getBounds().y
                    + getBounds().height);
            gc.drawLine(getBounds().x + getBounds().width - 51, getBounds().y, getBounds().x + getBounds().width - 51,
                    getBounds().y + getBounds().height);
            gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
            gc.drawLine(getBounds().x + getBounds().width - 1, getBounds().y, getBounds().x + getBounds().width - 1,
                    getBounds().y + getBounds().height);
        }
    }

    /**
     * Calculates the sequence of branch lines which should be rendered for the provided item
     * 
     * @param item
     * @return an array of integers composed using the constants in {@link BranchRenderer}
     */
    private int[] getBranches(GridItem item) {
        int[] branches = new int[item.getLevel() + 1];
        GridItem[] roots = item.getParent().getRootItems();

        // Is this a node or a leaf?
        if (item.getParentItem() == null) {
            // Add descender if not last item
            if (!item.isExpanded() && roots[roots.length - 1].equals(item)) {
                if (item.hasChildren()) {
                    branches[item.getLevel()] = BranchRenderer.LAST_ROOT;
                } else {
                    branches[item.getLevel()] = BranchRenderer.SMALL_L;
                }
            } else {
                if (item.hasChildren()) {
                    branches[item.getLevel()] = BranchRenderer.ROOT;
                } else {
                    branches[item.getLevel()] = BranchRenderer.SMALL_T;
                }
            }

        } else if (item.hasChildren()) {
            if (item.isExpanded()) {
                branches[item.getLevel()] = BranchRenderer.NODE;
            } else {
                branches[item.getLevel()] = BranchRenderer.NONE;
            }
        } else {
            branches[item.getLevel()] = BranchRenderer.LEAF;
        }

        // Branch for current item
        GridItem parent = item.getParentItem();
        if (parent == null) {
            return branches;
        }

        // Are there siblings below this item?
        if (parent.indexOf(item) < parent.getItemCount() - 1) {
            branches[item.getLevel() - 1] = BranchRenderer.T;
        } else if (parent.getParentItem() == null && !parent.equals(roots[roots.length - 1])) {
            branches[item.getLevel() - 1] = BranchRenderer.T;
        } else {
            branches[item.getLevel() - 1] = BranchRenderer.L;
        }

        Grid grid = item.getParent();
        item = parent;
        parent = item.getParentItem();

        // Branches for parent items
        while (item.getLevel() > 0) {
            if (parent.indexOf(item) == parent.getItemCount() - 1) {
                if (parent.getParentItem() == null && !grid.getRootItem(grid.getRootItemCount() - 1).equals(parent)) {
                    branches[item.getLevel() - 1] = BranchRenderer.I;
                } else {
                    branches[item.getLevel() - 1] = BranchRenderer.NONE;
                }
            } else {
                branches[item.getLevel() - 1] = BranchRenderer.I;
            }
            item = parent;
            parent = item.getParentItem();
        }
        // item should be null at this point
        return branches;
    }

    /**
     * {@inheritDoc}
     */
    public Point computeSize(GC gc, int wHint, int hHint, Object value) {
        GridItem item = (GridItem) value;

        gc.setFont(item.getFont(getColumn()));

        int x = 0;

        x += leftMargin;

        if (isTree()) {
            x += getToggleIndent(item);

            x += toggleRenderer.getBounds().width + insideMargin;

        }

        int y = 0;

        Image image = item.getImage(getColumn());
        if (image != null) {
            y = topMargin + image.getBounds().height + bottomMargin;

            x += image.getBounds().width + insideMargin;
        }

        // MOPR-DND
        // MOPR: replaced this code (to get correct preferred height for cells in word-wrap columns)
        //
        // x += gc.stringExtent(item.getText(column)).x + rightMargin;
        //
        // y = Math.max(y,topMargin + gc.getFontMetrics().getHeight() + bottomMargin);
        //
        // with this code:

        int textHeight = 0;
        if (!isWordWrap()) {
            x += gc.textExtent(item.getText(getColumn())).x + rightMargin;

            textHeight = topMargin + textTopMargin + gc.getFontMetrics().getHeight() + textBottomMargin + bottomMargin;
        } else {
            int plainTextWidth;
            if (wHint == SWT.DEFAULT) {
                plainTextWidth = gc.textExtent(item.getText(getColumn())).x;
            } else {
                plainTextWidth = wHint - x - rightMargin;
            }

            TextLayout currTextLayout = new TextLayout(gc.getDevice());
            currTextLayout.setFont(gc.getFont());
            currTextLayout.setText(item.getText(getColumn()));
            currTextLayout.setAlignment(getAlignment());
            currTextLayout.setWidth(plainTextWidth < 1 ? 1 : plainTextWidth);

            x += plainTextWidth + rightMargin;

            textHeight += topMargin + textTopMargin;
            for (int cnt = 0; cnt < currTextLayout.getLineCount(); cnt++) {
                textHeight += currTextLayout.getLineBounds(cnt).height;
            }
            textHeight += textBottomMargin + bottomMargin;

            currTextLayout.dispose();
        }

        y = Math.max(y, textHeight);

        return new Point(x, y);
    }

    /**
     * {@inheritDoc}
     */
    public boolean notify(int event, Point point, Object value) {

        GridItem item = (GridItem) value;

        if (isTree() && item.hasChildren()) {
            if (event == IInternalWidget.MouseMove) {
                if (overToggle(item, point)) {
                    setHoverDetail("toggle");
                    return true;
                }
            }

            if (event == IInternalWidget.LeftMouseButtonDown) {
                if (overToggle(item, point)) {
                    item.setExpanded(!item.isExpanded());
                    item.getParent().redraw();

                    if (item.isExpanded()) {
                        item.fireEvent(SWT.Expand);
                    } else {
                        item.fireEvent(SWT.Collapse);
                    }

                    return true;
                }
            }
        }

        return false;
    }

    private int getToggleIndent(GridItem item) {
        return item.getLevel() * treeIndent;
    }

    private boolean overToggle(GridItem item, Point point) {

        point = new Point(point.x, point.y);

        point.x -= getBounds().x - 1;
        point.y -= getBounds().y - 1;

        int x = leftMargin;
        x += getToggleIndent(item);

        if (point.x >= x && point.x < (x + toggleRenderer.getSize().x)) {
            // return true;
            int yStart = ((getBounds().height - toggleRenderer.getBounds().height) / 2);
            if (point.y >= yStart && point.y < yStart + toggleRenderer.getSize().y) {
                return true;
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTree(boolean tree) {
        super.setTree(tree);

        if (tree) {
            toggleRenderer = new TdToggleRenderer();
            toggleRenderer.setDisplay(getDisplay());

            branchRenderer = new BranchRenderer();
            branchRenderer.setDisplay(getDisplay());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Rectangle getTextBounds(GridItem item, boolean preferred) {
        int x = leftMargin;

        if (isTree()) {
            x += getToggleIndent(item);

            x += toggleRenderer.getBounds().width + insideMargin;

        }

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
