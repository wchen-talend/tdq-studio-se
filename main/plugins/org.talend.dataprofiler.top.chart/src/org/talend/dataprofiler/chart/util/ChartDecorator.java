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
package org.talend.dataprofiler.chart.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.ui.TextAnchor;

/**
 * DOC bzhou class global comment. Detailled comment
 */
public final class ChartDecorator {

    private static final int BASE_ITEM_LABEL_SIZE = 10;

    private static final int BASE_LABEL_SIZE = 12;

    private static final int BASE_TICK_LABEL_SIZE = 10;

    private static final int BASE_LEGEND_LABEL_SIZE = 10;

    private static final int BASE_TITLE_LABEL_SIZE = 14;

    /**
     * New format string. ADD yyi 2009-09-24 9243
     * */
    public static final String NEW_TOOL_TIP_FORMAT_STRING = "{0} = {2}"; //$NON-NLS-1$

    /**
     * Added TDQ-8673 yyin 20140415
     */
    private static final String DOUBLE_FORMAT = "0.00"; //$NON-NLS-1$

    // used for display double value in the chart
    private static final String PERCENT_FORMAT = "0.00%"; //$NON-NLS-1$

    /**
     * DOC bZhou ChartDecorator constructor comment.
     */
    private ChartDecorator() {
    }

    /**
     * DOC bZhou Comment method "decorate".
     * 
     * @param chart
     */
    public static void decorate(JFreeChart chart, PlotOrientation orientation) {
        if (chart != null) {
            Plot plot = chart.getPlot();
            if (plot instanceof CategoryPlot) {
                decorateCategoryPlot(chart, orientation);

                int rowCount = chart.getCategoryPlot().getDataset().getRowCount();

                for (int i = 0; i < rowCount; i++) {
                    // by zshen bug 14173 add the color in the colorList when chart neend more the color than 8.
                    if (i >= colorList.size()) {
                        colorList.add(generateRandomColor(colorList));
                    }
                    // ~14173
                    ((CategoryPlot) plot).getRenderer().setSeriesPaint(i, colorList.get(i));
                }

            }

            if (plot instanceof XYPlot) {
                decorateXYPlot(chart);

                int count = chart.getXYPlot().getDataset().getSeriesCount();
                for (int i = 0; i < count; i++) {
                    // by zshen bug 14173 add the color in the colorList when chart need the colors more than 8.
                    if (i >= colorList.size()) {
                        colorList.add(generateRandomColor(colorList));
                    }
                    // ~14173
                    ((XYPlot) plot).getRenderer().setSeriesPaint(i, colorList.get(i));
                }
            }

            if (plot instanceof PiePlot) {
                decoratePiePlot(chart);

                // ADD msjian TDQ-8046 2013-10-17: add the color's control for pie chart
                PieDataset piedataset = ((PiePlot) plot).getDataset();
                for (int i = 0; i < piedataset.getItemCount(); i++) {
                    if (i >= pieColorList.size()) {
                        pieColorList.add(generateRandomColor(pieColorList));
                    }
                    Comparable<?> key = piedataset.getKey(i);
                    ((PiePlot) plot).setSectionPaint(key, pieColorList.get(i));
                }
                // TDQ-8046~
            }
        }
    }

    /**
     * 
     * generate a Random Color.
     * 
     * @return a object of color which don't contain in list
     */
    private static Color generateRandomColor(List<Color> list) {
        Random rad = new Random();
        Color newColor = null;
        do {
            newColor = new Color(rad.nextInt(255), rad.nextInt(255), rad.nextInt(255));
        } while (list.contains(newColor) || Color.white.equals(newColor));
        return newColor;
    }

    /**
     * DOC xqliu Comment method "decorateColumnDependency".
     * 
     * @param chart
     */
    public static void decorateColumnDependency(JFreeChart chart) {
        decorate(chart, PlotOrientation.HORIZONTAL);
        CategoryItemRenderer renderer = ((CategoryPlot) chart.getPlot()).getRenderer();
        renderer.setSeriesPaint(0, colorList.get(1));
        renderer.setSeriesPaint(1, colorList.get(0));
    }

    /**
     * DOC bZhou Comment method "decorateCategoryPlot".
     * 
     * @param chart
     */
    public static void decorateCategoryPlot(JFreeChart chart, PlotOrientation orientation) {

        CategoryPlot plot = chart.getCategoryPlot();
        CategoryItemRenderer render = plot.getRenderer();
        CategoryAxis domainAxis = plot.getDomainAxis();
        // ADD msjian TDQ-5111 2012-4-9: set something look it well
        domainAxis.setCategoryMargin(0.1);
        domainAxis.setUpperMargin(0.05);
        domainAxis.setLowerMargin(0.05);
        // TDQ-5111~

        ValueAxis valueAxis = plot.getRangeAxis();

        Font font = new Font("Tahoma", Font.BOLD, BASE_ITEM_LABEL_SIZE);//$NON-NLS-1$

        render.setBaseItemLabelFont(font);
        // MOD zshen 10998: change the font name 2010-01-16
        font = new Font("sans-serif", Font.BOLD, BASE_LABEL_SIZE);//$NON-NLS-1$
        domainAxis.setLabelFont(font);

        font = new Font("sans-serif", Font.BOLD, BASE_LABEL_SIZE);//$NON-NLS-1$
        valueAxis.setLabelFont(font);

        font = new Font("sans-serif", Font.PLAIN, BASE_TICK_LABEL_SIZE);//$NON-NLS-1$
        domainAxis.setTickLabelFont(font);
        valueAxis.setTickLabelFont(font);

        font = new Font("Tahoma", Font.PLAIN, BASE_LEGEND_LABEL_SIZE);//$NON-NLS-1$
        LegendTitle legend = chart.getLegend();
        if (legend != null) {
            legend.setItemFont(font);
        }

        font = new Font("sans-serif", Font.BOLD, BASE_TITLE_LABEL_SIZE);//$NON-NLS-1$
        TextTitle title = chart.getTitle();
        if (title != null) {
            title.setFont(font);
        }

        font = null;

        if (render instanceof BarRenderer) {

            int rowCount = chart.getCategoryPlot().getDataset().getRowCount();
            if (!isContainsChineseColumn(chart)) {
                domainAxis.setTickLabelFont(new Font("Tahoma", Font.PLAIN, 10));//$NON-NLS-1$
            }
            domainAxis.setUpperMargin(0.1);
            // MOD klliu bug 14570: Label size too long in Text statistics graph 2010-08-09
            domainAxis.setMaximumCategoryLabelLines(10);
            ((BarRenderer) render).setItemMargin(-0.40 * rowCount);

            // ADD msjian TDQ-5111 2012-4-9: set Bar Width and let it look well
            // not do this when the bar is horizontal Orientation
            if (orientation == null) {
                ((BarRenderer) render).setMaximumBarWidth(0.2);
            }
            // TDQ-5111~
        }
        // ~10998
    }

    /**
     * DOC bZhou Comment method "decorateXYPlot".
     * 
     * @param chart
     */
    private static void decorateXYPlot(JFreeChart chart) {

        Font font = null;
        XYPlot plot = chart.getXYPlot();
        XYItemRenderer render = plot.getRenderer();
        ValueAxis domainAxis = plot.getDomainAxis();
        ValueAxis valueAxis = plot.getRangeAxis();

        font = new Font("Tahoma", Font.BOLD, BASE_ITEM_LABEL_SIZE);//$NON-NLS-1$

        render.setBaseItemLabelFont(font);

        font = new Font("sans-serif", Font.BOLD, BASE_LABEL_SIZE);//$NON-NLS-1$
        domainAxis.setLabelFont(font);

        font = new Font("sans-serif", Font.BOLD, BASE_LABEL_SIZE);//$NON-NLS-1$
        valueAxis.setLabelFont(font);

        font = new Font("sans-serif", Font.PLAIN, BASE_TICK_LABEL_SIZE);//$NON-NLS-1$
        domainAxis.setTickLabelFont(font);
        valueAxis.setTickLabelFont(font);

        font = new Font("Tahoma", Font.PLAIN, BASE_LEGEND_LABEL_SIZE);//$NON-NLS-1$
        LegendTitle legend = chart.getLegend();
        if (legend != null) {
            legend.setItemFont(font);
        }

        font = new Font("sans-serif", Font.BOLD, BASE_TITLE_LABEL_SIZE);//$NON-NLS-1$
        TextTitle title = chart.getTitle();
        if (title != null) {
            title.setFont(font);
        }

        font = null;
    }

    /**
     * 
     * DOC qiongli Comment method "decoratePiePlot".
     * 
     * @param chart
     */
    private static void decoratePiePlot(JFreeChart chart) {

        Font font = new Font("sans-serif", Font.BOLD, BASE_TITLE_LABEL_SIZE);//$NON-NLS-1$
        TextTitle textTitle = chart.getTitle();
        // MOD msjian TDQ-5213 2012-5-7: fixed NPE
        if (textTitle != null) {
            textTitle.setFont(font);
        }
        font = new Font("Tahoma", Font.PLAIN, BASE_ITEM_LABEL_SIZE);//$NON-NLS-1$
        LegendTitle legend = chart.getLegend();
        if (legend != null) {
            legend.setItemFont(font);
        }
        // TDQ-5213~
        PiePlot plot = (PiePlot) chart.getPlot();
        font = new Font("Monospaced", Font.PLAIN, 10);//$NON-NLS-1$
        plot.setLabelFont(font);
        plot.setNoDataMessage("No data available"); //$NON-NLS-1$
        StandardPieSectionLabelGenerator standardPieSectionLabelGenerator = new StandardPieSectionLabelGenerator(("{0}:{2}"),//$NON-NLS-1$
                NumberFormat.getNumberInstance(), new DecimalFormat(PERCENT_FORMAT));
        plot.setLabelGenerator(standardPieSectionLabelGenerator);
        plot.setLabelLinkPaint(Color.GRAY);
        plot.setLabelOutlinePaint(Color.WHITE);
        plot.setLabelGap(0.02D);
        plot.setOutlineVisible(false);
        plot.setMaximumLabelWidth(0.2D);
        plot.setCircular(false);
        // remove the shadow of the pie chart
        plot.setShadowXOffset(0);
        plot.setShadowYOffset(0);
    }

    /**
     * create bar chart with customized bar render class which can be adapted in JFreeChart class.
     * 
     * @param chart
     */
    public static void decorateBarChart(JFreeChart chart) {
        CategoryPlot plot = chart.getCategoryPlot();
        plot.getRangeAxis().setUpperMargin(0.08);
        // plot.getRangeAxis().setLowerBound(-0.08);

        plot.setRangeGridlinesVisible(true);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBaseItemLabelsVisible(true);
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_LEFT));
        renderer.setBaseNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_LEFT));
        // MOD klliu 2010-09-25 bug15514: The chart of summary statistic indicators not beautiful
        renderer.setMaximumBarWidth(0.1);
        // renderer.setItemMargin(0.000000005);
        // renderer.setBase(0.04);
        // ADD yyi 2009-09-24 9243
        renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator(NEW_TOOL_TIP_FORMAT_STRING, NumberFormat
                .getInstance()));

        // ADD TDQ-5251 msjian 2012-7-31: do not display the shadow
        renderer.setShadowVisible(false);
        // TDQ-5251~

        // CategoryAxis domainAxis = plot.getDomainAxis();
        // domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));

    }

    /**
     * Added TDQ-8673: set the display decimal format as: x.xx
     * 
     * @param chart
     */
    public static void setDisplayDecimalFormat(JFreeChart chart) {
        CategoryPlot plot = chart.getCategoryPlot();

        plot.getRenderer().setBaseItemLabelGenerator(
                new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat(DOUBLE_FORMAT))); //$NON-NLS-1$

    }

    /**
     * Decorate the benford law chart. in this method the line chart will be overlay on top of bar chart.
     * 
     * @param dataset
     * @param barChart
     * @param title
     * @param categoryAxisLabel
     * @param dotChartLabels
     * @param formalValues
     * @return JFreeChart
     */
    @SuppressWarnings("deprecation")
    public static JFreeChart decorateBenfordLawChart(CategoryDataset dataset, JFreeChart barChart, String title,
            String categoryAxisLabel, List<String> dotChartLabels, double[] formalValues) {
        CategoryPlot barplot = barChart.getCategoryPlot();
        barplot.setRenderer(new BenfordLawLineAndShapeRenderer());
        decorateBarChart(barChart);
        // display percentage on top of the bar
        DecimalFormat df = new DecimalFormat(PERCENT_FORMAT);
        barplot.getRenderer().setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", df)); //$NON-NLS-1$
        barplot.getRenderer().setBasePositiveItemLabelPosition(
                new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_CENTER));
        // set the display of Y axis
        NumberAxis numAxis = (NumberAxis) barplot.getRangeAxis();
        numAxis.setNumberFormatOverride(df);

        CategoryDataset lineDataset = getLineDataset(dotChartLabels, formalValues);
        JFreeChart lineChart = ChartFactory.createLineChart(null, title, categoryAxisLabel, lineDataset,
                PlotOrientation.VERTICAL, false, false, false);
        CategoryPlot plot = lineChart.getCategoryPlot();
        // show the value on the right axis of the chart(keep the comment)
        // NumberAxis numberaxis = new NumberAxis(DefaultMessagesImpl.getString("TopChartFactory.Value"));
        // plot.setRangeAxis(10, numberaxis);

        NumberAxis vn = (NumberAxis) plot.getRangeAxis();
        vn.setNumberFormatOverride(df);
        // set points format
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setPaint(Color.BLUE);
        renderer.setSeriesShape(1, new Rectangle2D.Double(-1.5, -1.5, 3, 3));
        renderer.setShapesVisible(true); // show the point shape
        renderer.setBaseLinesVisible(false);// do not show the line

        // add the bar chart into the line chart
        CategoryItemRenderer barChartRender = barplot.getRenderer();
        barplot.setDataset(0, lineDataset);
        barplot.setRenderer(0, plot.getRenderer());
        barplot.setDataset(1, dataset);
        barplot.setRenderer(1, barChartRender);
        return barChart;
    }

    /**
     * 
     * created by mzhao on 2012-9-21 The customer render to paint bar color.
     * 
     */
    private static class BenfordLawLineAndShapeRenderer extends BarRenderer {

        private static final long serialVersionUID = 1L;

        @Override
        public Paint getItemPaint(final int row, final int column) {
            return (column > 8) ? Color.RED : new Color(193, 216, 047);
        }
    }

    /**
     * get the dataset of standard points.
     * 
     * @param dotChartLabels
     * @param formalValues
     * @return CategoryDataset
     */
    private static CategoryDataset getLineDataset(List<String> dotChartLabels, double[] formalValues) {
        DefaultCategoryDataset linedataset = new DefaultCategoryDataset();
        for (int i = 0; i < dotChartLabels.size(); i++) {
            linedataset.addValue(formalValues[i], "Expected(%)", dotChartLabels.get(i)); //$NON-NLS-1$
        }
        return linedataset;
    }

    private static final Color COLOR_0 = new Color(244, 147, 32);

    private static final Color COLOR_1 = new Color(128, 119, 178);

    private static final Color COLOR_2 = new Color(190, 213, 48);

    private static final Color COLOR_3 = new Color(236, 23, 133);

    private static final Color COLOR_4 = new Color(35, 157, 190);

    private static final Color COLOR_5 = new Color(164, 155, 100);

    private static final Color COLOR_6 = new Color(250, 212, 16);

    private static final Color COLOR_7 = new Color(234, 28, 36);

    private static final Color COLOR_8 = new Color(192, 131, 91);

    private static List<Color> colorList = new ArrayList<Color>();

    private static List<Color> pieColorList = new ArrayList<Color>();

    /**
     * 
     * DOC mzhao 2009-07-28 Bind the indicator with specific color.
     */
    public static enum IndiBindColor {
        INDICATOR_ROW_COUNT("Row Count", COLOR_7), //$NON-NLS-1$
        INDICATOR_NULL_COUNT("Null Count", COLOR_2), //$NON-NLS-1$
        INDICATOR_DISTINCT_COUNT("Distinct Count", COLOR_0), //$NON-NLS-1$
        INDICATOR_UNIQUE_COUNT("Unique Count", COLOR_1), //$NON-NLS-1$
        INDICATOR_DUPLICATE_COUNT("Duplicate Count", COLOR_3);//$NON-NLS-1$

        String indLabel = null;

        Color color = null;

        public Color getColor() {
            return color;
        }

        IndiBindColor(String indicatorLabel, Color bindColor) {
            indLabel = indicatorLabel;
            color = bindColor;
        }
    }

    static {
        colorList.add(COLOR_7);
        colorList.add(COLOR_2);
        colorList.add(COLOR_0);
        colorList.add(COLOR_1);
        colorList.add(COLOR_3);
        colorList.add(COLOR_4);
        colorList.add(COLOR_5);
        colorList.add(COLOR_6);
        colorList.add(COLOR_8);
    }

    static {
        pieColorList.add(COLOR_2);
        pieColorList.add(COLOR_7);
        pieColorList.add(COLOR_0);
        pieColorList.add(COLOR_1);
        pieColorList.add(COLOR_3);
        pieColorList.add(COLOR_4);
        pieColorList.add(COLOR_5);
        pieColorList.add(COLOR_6);
        pieColorList.add(COLOR_8);
    }

    /**
     * Returns true if this string contains the chinese char values. DOC yyi Comment method
     * "isContainsChinese".2010-09-26:14692.
     * 
     * @param str
     * @return
     */
    private static boolean isContainsChineseColumn(JFreeChart chart) {
        Object[] columnNames = chart.getCategoryPlot().getDataset().getColumnKeys().toArray();
        String regEx = "[\u4e00-\u9fa5]";//$NON-NLS-1$
        Pattern pat = Pattern.compile(regEx);
        boolean flg = false;
        for (Object str : columnNames) {
            Matcher matcher = pat.matcher(str.toString());
            if (matcher.find()) {
                flg = true;
                break;
            }
        }
        return flg;
    }
}
