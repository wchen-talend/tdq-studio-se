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
package org.talend.dataquality.record.linkage.ui.composite.table;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.event.ColumnHeaderSelectionEvent;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.eclipse.nebula.widgets.nattable.sort.config.DefaultSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.talend.dataprofiler.common.ui.editor.preview.MatchRuleColorRegistry;
import org.talend.dataquality.PluginConstant;
import org.talend.dataquality.record.linkage.ui.composite.ListObjectDataProvider;
import org.talend.dataquality.record.linkage.ui.composite.utils.ImageLib;
import org.talend.dataquality.record.linkage.ui.composite.utils.MatchRuleAnlaysisUtils;
import org.talend.dataquality.record.linkage.utils.MatchAnalysisConstant;
import orgomg.cwm.objectmodel.core.ModelElement;

/**
 * when the property(displayed column) name of the table is changed, need to call initTableProperty first. if only the
 * data of the table changed, need only to call createTableControl. TODO: refresh the table with new data(column not
 * changed), and no need to create the table for every refresh; or give all the data to the table at one time?
 */
public class DataSampleTable {

    private BodyLayerStack bodyLayer;

    @SuppressWarnings("rawtypes")
    private ListObjectDataProvider bodyDataProvider;

    private String[] propertyNames;

    private Map<String, String> propertyToLabels;

    private NatTable natTable;

    public static final String MATCH_EKY = "MATCH"; //$NON-NLS-1$

    public static final String BLOCK_EKY = "BLOCK"; //$NON-NLS-1$

    public static final Color COLOR_BLACK = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);

    public static final Color COLOR_RED = Display.getDefault().getSystemColor(SWT.COLOR_RED);

    public static final Color COLOR_GREEN = Display.getDefault().getSystemColor(SWT.COLOR_GREEN);

    private static final Color SYSTEM_COLOR = Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND);

    protected PropertyChangeSupport listeners = new PropertyChangeSupport(this);

    private String currentSelectedColumn = null;

    // record the columns which is used as block keys
    private List<String> markedAsBlockKey = null;

    // record the columns which is used as match keys
    private List<String> markedAsMatchKey = null;

    // TDQ-9297 msjian: set the default value the same as lessSpin default value.
    private int minGrpSize = PluginConstant.HIDDEN_GROUP_LESS_THAN_DEFAULT;

    private SortState sortState = new SortState(-1);

    // <groupid, related color>
    private Map<String, Integer> rowOfGIDWithColor = new HashMap<String, Integer>();

    private int masterColumn;

    private boolean isContainGID = false;

    private final static Color[] COLOR_LIST = MatchRuleColorRegistry.getColorsForSwt();

    private ColumnPosition additionalColumnPosition;

    public DataSampleTable() {
    }

    /**
     * create the nattable every time when the user select some columns
     * 
     * @param columns
     * @param listOfData
     */
    public TControl createTable(Composite parentContainer, ModelElement[] columns, List<Object[]> listOfData) {
        reset();

        initTableProperty(columns);

        // initial the data if it is empty
        List<Object[]> results = listOfData == null ? new ArrayList<Object[]>() : listOfData;
        if (results.size() < 1) {
            results.add(getEmptyRow(columns.length));
        }
        // Two kinds of result for listOfData : 1) is coming from match: the result contains GID
        // 2) is coming from the query without match/or from block:the result did not contain GID
        isContainGID = results.get(0).length > additionalColumnPosition.GIDindex;

        if (isContainGID) {
            initGIDMap(results);
            if (minGrpSize > 1) {
                return hideGroup(parentContainer, results);
            }
        }
        return createTableControl(parentContainer, results);

    }

    /**
     * before create the table control, need to init the property name and name to label map
     * 
     * @param proNames
     */
    private void initTableProperty(ModelElement[] columns) {
        propertyNames = createColumnLabel(columns);

        Map<String, String> columnToLabelMap = new HashMap<String, String>();
        for (String label : propertyNames) {
            columnToLabelMap.put(label, label);
        }
        this.propertyToLabels = columnToLabelMap;

    }

    private String[] createColumnLabel(ModelElement[] columns) {
        int columnCount = MatchAnalysisConstant.DEFAULT_COLUMN_COUNT;
        if (columns != null) {
            columnCount = columns.length + columnCount;
        }
        String[] columnsName = new String[columnCount];

        int i = 0;
        if (columns != null) {
            for (ModelElement column : columns) {
                columnsName[i++] = column.getName();
            }
        }
        columnsName[i++] = MatchAnalysisConstant.BLOCK_KEY;
        // remember the index of the GID;
        additionalColumnPosition = new ColumnPosition(i);
        columnsName[i++] = MatchAnalysisConstant.GID;
        // record the index of the GRP_SIZE
        sortState = new SortState(i);
        columnsName[i++] = MatchAnalysisConstant.GRP_SIZE;
        this.masterColumn = i;

        columnsName[i++] = MatchAnalysisConstant.MASTER;
        columnsName[i++] = MatchAnalysisConstant.SCORE;
        columnsName[i++] = MatchAnalysisConstant.GRP_QUALITY;
        columnsName[i++] = MatchAnalysisConstant.ATTRIBUTE_SCORES;
        return columnsName;
    }

    /**
     * store the GID's group size.
     * 
     * @param listOfData
     */
    private void initGIDMap(List<Object[]> listOfData) {
        for (Object[] row : listOfData) {
            int grpSize = getGroupSize(row);
            if (grpSize == 0) {
                continue;
            }
            String groupId = (String) row[additionalColumnPosition.GIDindex];
            // String[] gids = StringUtils.splitByWholeSeparatorPreserveAllTokens(groupId, PluginConstant.COMMA_STRING);
            // for (String gid : gids) {
            // this.rowOfGIDWithColor.put(gid, grpSize);
            // }
            this.rowOfGIDWithColor.put(groupId, grpSize);
        }

    }

    private int getGroupSize(Object[] row) {
        try {
            return Integer.valueOf((String) row[sortState.getGrpSizeIndex()]);
        } catch (java.lang.NumberFormatException nfe) {
            // no need to handle--when no column given
            return 0;
        }

    }

    /**
     * DOC yyin Comment method "reset".
     */
    private void reset() {
        // reset some properties: GID-Grpsize map,
        this.rowOfGIDWithColor.clear();
    }

    private TControl hideGroup(Composite parentContainer, List<Object[]> listOfData) {
        List<Object[]> filteredList = new ArrayList<Object[]>();
        boolean flag = false;
        for (Object[] row : listOfData) {
            int grpSize = getGroupSize(row);
            if (grpSize == 0) {
                if (flag) {
                    filteredList.add(row);
                }
            } else if (grpSize >= minGrpSize) {
                flag = true;
                filteredList.add(row);
            } else {
                flag = false;
            }
        }
        return createTableControl(parentContainer, filteredList);
    }

    private void addCustomSelectionBehaviour() {
        natTable.addLayerListener(new ILayerListener() {

            @Override
            public void handleLayerEvent(ILayerEvent event) {
                if ((event instanceof ColumnHeaderSelectionEvent)) {
                    ColumnHeaderSelectionEvent columnEvent = (ColumnHeaderSelectionEvent) event;
                    Collection<Range> ranges = columnEvent.getColumnPositionRanges();
                    if (ranges.size() > 0) {
                        Range range = ranges.iterator().next();
                        handleColumnSelectionChange(range.start);
                    }
                }
            }
        });
    }

    private void handleColumnSelectionChange(int index) {
        sortState.setSelectedColumnIndex(index - 1);
        // need also to remember the selected column name, for fix the arrow on the column when scrolling
        sortState.setSelectedColumnName(this.propertyNames[index - 1]);
        currentSelectedColumn = this.getUserColumnNameByPosition(index);
        listeners.firePropertyChange(MatchAnalysisConstant.DATA_SAMPLE_TABLE_COLUMN_SELECTION, true, false);

    }

    public String getCurrentSelectedColumn() {
        return this.currentSelectedColumn;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.addPropertyChangeListener(listener);
    }

    // sort by the current selected column
    public void sortByColumn(List<ModelElement> columns) {
        if (columns == null || columns.size() < 1) {
            return;
        }
        // if the next sort direction is back to original
        SortDirectionEnum nextSortDirection = sortState.getNextSortDirection();

        List<Object[]> sortedData = bodyDataProvider.getList();
        if (SortDirectionEnum.NONE.equals(nextSortDirection) && isContainGID) {
            // if the data has GID, back to order by GID
            sortedData = MatchRuleAnlaysisUtils.sortResultByGID(propertyNames, sortedData);
        } else {
            sortedData = MatchRuleAnlaysisUtils.sortDataByColumn(sortState, sortedData, columns);
        }
        // refresh the table by the sorted data
        Composite parent = this.natTable.getParent();
        createTableControl(parent, sortedData);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
        // add sort data function
        natTable.addConfiguration(new DefaultSortConfiguration());
        natTable.configure();

        this.natTable.redraw();
        parent.getParent().layout();
        parent.layout();
    }

    /**
     * when the data is empty, the column can not response the click event, so we need to add an empty row to it.
     * 
     * @param length
     * 
     * @return
     */
    private Object[] getEmptyRow(int length) {
        Object[] emptyRow = new Object[length];

        for (int i = 0; i < length; i++) {
            emptyRow[i] = StringUtils.EMPTY;
        }
        return emptyRow;
    }

    public void changeColumnHeaderLabelColor(String columnName, Color color, String keyName) {
        updateMarkedKeys(columnName, color, keyName);

        Style cellStyle = new Style();
        cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, color);

        natTable.getConfigRegistry().registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL,
                columnName);

        natTable.configure();

    }

    /**
     * update Marked Keys
     * 
     * @param columnName
     * @param color
     * @param keyName
     */
    private void updateMarkedKeys(String columnName, Color color, String keyName) {
        if (this.markedAsBlockKey == null) {
            markedAsBlockKey = new ArrayList<String>();
        }
        if (this.markedAsMatchKey == null) {
            markedAsMatchKey = new ArrayList<String>();
        }
        if (GUIHelper.COLOR_BLACK.equals(color)) {
            removeMarkedKey(columnName, keyName);
        } else if (GUIHelper.COLOR_GREEN.equals(color)) {
            markedAsBlockKey.add(columnName);
        } else if (GUIHelper.COLOR_RED.equals(color)) {
            this.markedAsMatchKey.add(columnName);
        }

    }

    /**
     * remove Marked Key".
     * 
     * @param columnName
     * @param keyName
     */
    private void removeMarkedKey(String columnName, String keyName) {
        if (DataSampleTable.MATCH_EKY.equals(keyName)) {
            this.markedAsMatchKey.remove(columnName);
        } else if (DataSampleTable.BLOCK_EKY.endsWith(keyName)) {
            this.markedAsBlockKey.remove(keyName);
        }

    }

    /**
     * When the column is the user selected one, return its name; when the column is the default additional one, return
     * null
     * 
     * @param position
     * @return
     */
    public String getUserColumnNameByPosition(int position) {
        if (position > propertyNames.length - MatchAnalysisConstant.DEFAULT_COLUMN_COUNT) {
            return null;
        }
        return propertyNames[position - 1];
    }

    public void refresh() {
        if (natTable == null) {
            return;
        }
        natTable.refresh();
    }

    /**
     * create the NatTable according to the property, and list of data to display
     * 
     * @param parent
     * @param data
     * @return
     */
    public TControl createTableControl(Composite parent, List<Object[]> data) {
        bodyDataProvider = setupBodyDataProvider(data);
        bodyLayer = new BodyLayerStack(bodyDataProvider);

        DefaultColumnHeaderDataProvider colHeaderDataProvider = new DefaultColumnHeaderDataProvider(propertyNames,
                propertyToLabels);
        ColumnHeaderLayerStack columnHeaderLayer = new ColumnHeaderLayerStack(colHeaderDataProvider);

        DefaultRowHeaderDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(bodyDataProvider);
        RowHeaderLayerStack rowHeaderLayer = new RowHeaderLayerStack(rowHeaderDataProvider);

        DefaultCornerDataProvider cornerDataProvider = new DefaultCornerDataProvider(colHeaderDataProvider, rowHeaderDataProvider);
        CornerLayer cornerLayer = new CornerLayer(new DataLayer(cornerDataProvider), rowHeaderLayer, columnHeaderLayer);

        GridLayer gridLayer = new GridLayer(bodyLayer, columnHeaderLayer, rowHeaderLayer, cornerLayer);

        if (natTable != null) {
            clearTable();
        }
        natTable = new NatTable(parent, gridLayer, false);
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration() {

            {// use the own painter to paint the group color
                cellPainter = new RowBackgroundGroupPainter(new ForegroundTextPainter(false, false, false));
            }
        });

        natTable.configure();

        natTable.getConfigRegistry().registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE,
                IEditableRule.NEVER_EDITABLE, DisplayMode.EDIT, "ODD_BODY"); //$NON-NLS-1$
        natTable.getConfigRegistry().registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE,
                IEditableRule.NEVER_EDITABLE, DisplayMode.EDIT, "EVEN_BODY"); //$NON-NLS-1$

        natTable.setBackground(SYSTEM_COLOR);

        // add the listener for the column header selection
        addCustomSelectionBehaviour();

        TControl retObj = new TControl();
        retObj.setControl(natTable);
        // the width = (columnCount * DEFAULT_COLUMN_WIDTH) + 60, 60 is the width of first sequence number column
        retObj.setWidth(colHeaderDataProvider.getColumnCount() * DataLayer.DEFAULT_COLUMN_WIDTH + 60);
        return retObj;
    }

    private void clearTable() {
        natTable.dispose();
        if (this.markedAsBlockKey != null) {
            this.markedAsBlockKey.clear();
        }
        if (this.markedAsMatchKey != null) {
            this.markedAsMatchKey.clear();
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private ListObjectDataProvider setupBodyDataProvider(List<Object[]> data) {
        IColumnPropertyAccessor columnPropertyAccessor = new ReflectiveColumnPropertyAccessor(propertyNames);
        return new ListObjectDataProvider(data, columnPropertyAccessor);
    }

    private class ForegroundTextPainter extends TextPainter {

        private boolean changeForegroundColor = false;

        private boolean drawImage = false;

        private Image masterImage = ImageLib.getImage(ImageLib.MASTER_IMAGE);

        public ForegroundTextPainter(boolean wrapText, boolean paintBg, boolean calculate) {
            super(wrapText, paintBg, calculate);
        }

        @Override
        public void paintCell(ILayerCell cell, GC gc, Rectangle rectangle, IConfigRegistry configRegistry) {
            super.paintCell(cell, gc, rectangle, configRegistry);
            if (drawImage && !changeForegroundColor & cell.getColumnIndex() == 0) {
                gc.drawImage(masterImage, rectangle.x, rectangle.y);
            }
        }

        @Override
        public void setupGCFromConfig(GC gc, IStyle cellStyle) {
            super.setupGCFromConfig(gc, cellStyle);
            if (changeForegroundColor) {
                gc.setForeground(ImageLib.COLOR_GREY);
            }
        }

        public void setChangeForegroundColor(boolean isChange) {
            changeForegroundColor = isChange;
        }

        protected void setDrawImage(boolean isDraw) {
            drawImage = isDraw;
        }
    }

    // for different data group, use different background color
    private class RowBackgroundGroupPainter extends BackgroundPainter {// GradientBackgroundPainter {

        private String previousGID = null;

        public RowBackgroundGroupPainter(ICellPainter painter) {
            super(painter);
        }

        @Override
        public void paintCell(ILayerCell cell, GC gc, Rectangle bounds, IConfigRegistry configRegistry) {
            String currentGID = getGID(cell);
            if (currentGID == null) {// when only show the data without match, use the grey color for the text
                ((ForegroundTextPainter) getWrappedPainter()).setChangeForegroundColor(true);
            }

            // if the row is not a master row, make the text color grey.
            if (currentGID != null && !StringUtils.EMPTY.equals(currentGID)) {
                ((ForegroundTextPainter) getWrappedPainter()).setDrawImage(true);
                Object[] rowObject = (Object[]) bodyDataProvider.getRowObject(cell.getRowIndex());
                // use master to judge instead of "0"
                Boolean isMaster = Boolean.parseBoolean(rowObject[masterColumn].toString());
                if (isMaster) {
                    ((ForegroundTextPainter) getWrappedPainter()).setChangeForegroundColor(false);
                } else {
                    ((ForegroundTextPainter) getWrappedPainter()).setChangeForegroundColor(true);
                }
            } else {
                ((ForegroundTextPainter) getWrappedPainter()).setDrawImage(false);
            }

            super.paintCell(cell, gc, bounds, configRegistry);
            // when the GID changed, draw a line
            if (currentGID != null) {
                // only draw a line when the group with same size neighbour with each other and the record in current
                // line is master.
                Object[] rowObject = (Object[]) bodyDataProvider.getRowObject(cell.getRowIndex());
                Boolean isMaster = Boolean.parseBoolean(rowObject[masterColumn].toString());
                if (isMaster && !StringUtils.equals(previousGID, currentGID) && isEqualGroupSize(previousGID, currentGID)) {
                    gc.setLineWidth(gc.getLineWidth() * 2);
                    gc.setLineStyle(SWT.LINE_DOT);
                    gc.setForeground(GUIHelper.COLOR_BLUE);
                    gc.drawLine(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y);
                }

                previousGID = currentGID;
            }

        }

        @Override
        protected Color getBackgroundColour(ILayerCell cell, IConfigRegistry configRegistry) {
            int grpSizeValue = getGrpSize(cell);
            if (grpSizeValue == 0) {// default color when no
                return GUIHelper.COLOR_LIST_BACKGROUND;
            }
            return COLOR_LIST[grpSizeValue % COLOR_LIST.length];
        }

        /**
         * first: if the row of the cell is a master row, return its group size second: if the row of the cell is not a
         * master one, return the previous one
         * 
         * @param cell
         * @return
         */
        private int getGrpSize(ILayerCell cell) {
            Object[] rowObject = (Object[]) bodyDataProvider.getRowObject(cell.getRowIndex());
            // if the row record contains the group size info, continue
            if (rowObject != null && isContainGID) {
                // find the group size from the map first, GID index = grp_size_index-1
                Integer groupSize = rowOfGIDWithColor.get(rowObject[additionalColumnPosition.GIDindex]);
                if (groupSize != null) {
                    return groupSize;
                } else {
                    // if the group id has no related group size, get it
                    return getGroupSize(rowObject);
                }
            }
            return 0;
        }

        // get the group id of the cell if any
        private String getGID(ILayerCell cell) {
            Object[] rowObject = (Object[]) bodyDataProvider.getRowObject(cell.getRowIndex());
            // if the row record contains the group size info, continue
            if (rowObject != null && isContainGID) {
                return (String) rowObject[additionalColumnPosition.GIDindex];
            } else {
                return null;
            }
        }

        /**
         * check if the group size of the two group is equal or not.
         * 
         * @param previousGID2
         * @param currentGID
         * @return
         */
        private boolean isEqualGroupSize(String groupId1, String groupId2) {
            Integer size1 = rowOfGIDWithColor.get(groupId1);
            Integer size2 = rowOfGIDWithColor.get(groupId2);
            if (size1 != null && size2 != null && size1 == size2) {
                return true;
            }
            return false;
        }

    }

    class BodyLayerStack extends AbstractLayerTransform {

        private SelectionLayer selectionLayer;

        public BodyLayerStack(IDataProvider dataProvider) {
            DataLayer bodyDataLayer = new DataLayer(dataProvider);
            ColumnReorderLayer columnReorderLayer = new ColumnReorderLayer(bodyDataLayer);
            ColumnHideShowLayer columnHideShowLayer = new ColumnHideShowLayer(columnReorderLayer);
            this.selectionLayer = new SelectionLayer(columnHideShowLayer);

            ViewportLayer viewportLayer = new ViewportLayer(this.selectionLayer);
            setUnderlyingLayer(viewportLayer);
        }

        public SelectionLayer getSelectionLayer() {
            return this.selectionLayer;
        }
    }

    class ColumnHeaderLayerStack extends AbstractLayerTransform {

        public ColumnHeaderLayerStack(IDataProvider dataProvider) {
            DataLayer dataLayer = new DataLayer(dataProvider);
            ColumnHeaderLayer colHeaderLayer = new ColumnHeaderLayer(dataLayer, bodyLayer, bodyLayer.getSelectionLayer());

            setUnderlyingLayer(colHeaderLayer);
            dataLayer.setColumnPercentageSizing(true);
        }

        @Override
        public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
            // use columnPosition to get its property name, if the name is selected, return new LabelStack, else
            // return super
            String currentColumnName = getCurrentColumnName(columnPosition, rowPosition);
            // if the current column is used as key, return its labelstack.(the color of it will keep)
            if (isColumnMarkedAsKeys(currentColumnName)) {
                if (columnPosition < propertyNames.length + 1) {
                    return addSortArrow(currentColumnName, new LabelStack(currentColumnName));
                }
            }
            return addSortArrow(currentColumnName, super.getConfigLabelsByPosition(columnPosition, rowPosition));

        }

        private LabelStack addSortArrow(String currentColumnName, LabelStack configLabels) {

            // if current is sorting, add sort icon, else no need to add
            if (sortState.isSortActive()) {
                if (sortState.getSelectedColumnIndex() != -1 && sortState.isSelectedColumn(currentColumnName)) {
                    switch (sortState.getCurrentSortDirection()) {
                    case ASC:
                        configLabels.addLabelOnTop(DefaultSortConfiguration.SORT_UP_CONFIG_TYPE);
                        break;
                    case DESC:
                        configLabels.addLabelOnTop(DefaultSortConfiguration.SORT_DOWN_CONFIG_TYPE);
                        break;
                    default:
                    }
                }
            }
            return configLabels;
        }

        /**
         * get Current Column Name.
         * 
         * @param columnPosition
         * @return
         */
        private String getCurrentColumnName(int columnPosition, int rowPosition) {
            ILayerCell cell = this.getCellByPosition(columnPosition, rowPosition);
            Object dataValue = cell.getDataValue();
            return String.valueOf(dataValue);
        }
    }

    class RowHeaderLayerStack extends AbstractLayerTransform {

        public RowHeaderLayerStack(IDataProvider dataProvider) {
            DataLayer dataLayer = new DataLayer(dataProvider, 50, 20);
            RowHeaderLayer rowHeaderLayer = new RowHeaderLayer(dataLayer, bodyLayer, bodyLayer.getSelectionLayer());
            setUnderlyingLayer(rowHeaderLayer);
        }
    }

    /**
     * check if the current column is marked as block/match keys.
     * 
     * @param additionalColumnPosition
     * @return
     */
    public boolean isColumnMarkedAsKeys(String column) {
        boolean isMarked = false;
        if (this.markedAsBlockKey != null && this.markedAsBlockKey.size() > 0) {
            if (this.markedAsBlockKey.contains(column)) {
                isMarked = true;
            }
        }
        if (this.markedAsMatchKey != null && this.markedAsMatchKey.size() > 0) {
            if (this.markedAsMatchKey.contains(column)) {
                isMarked = true;
            }
        }
        return isMarked;
    }

    /**
     * DOC sizhaoliu Comment method "setMinGroupSize".
     * 
     * @param valueOf
     */
    public void setMinGroupSize(int size) {
        this.minGrpSize = size;
    }

    public void resetSortSelection() {
        this.sortState.resetSelectedColumn();
    }

    /**
     * A control and it's width.
     */
    public class TControl {

        Control control;

        Integer width;

        /**
         * Getter for control.
         * 
         * @return the control
         */
        public Control getControl() {
            return this.control;
        }

        /**
         * Sets the control.
         * 
         * @param control the control to set
         */
        public void setControl(Control control) {
            this.control = control;
        }

        /**
         * Getter for width.
         * 
         * @return the width
         */
        public Integer getWidth() {
            return this.width;
        }

        /**
         * Sets the width.
         * 
         * @param width the width to set
         */
        public void setWidth(Integer width) {
            this.width = width;
        }
    }

    private class ColumnPosition {

        protected final int GIDindex;

        public ColumnPosition(int GIDIndex) {
            GIDindex = GIDIndex;
        }
    }
}
