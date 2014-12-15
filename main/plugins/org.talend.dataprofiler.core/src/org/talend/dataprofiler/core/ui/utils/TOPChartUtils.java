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
package org.talend.dataprofiler.core.ui.utils;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.cwm.helper.SwitchHelpers;
import org.talend.cwm.management.i18n.Messages;
import org.talend.dataprofiler.core.CorePlugin;
import org.talend.dataprofiler.core.ui.editor.analysis.drilldown.DrillDownEditorInput;
import org.talend.dataprofiler.core.ui.editor.preview.model.ChartTableFactory;
import org.talend.dataprofiler.core.ui.editor.preview.model.ChartTableMenuGenerator;
import org.talend.dataprofiler.core.ui.editor.preview.model.MenuItemEntity;
import org.talend.dataprofiler.service.ITOPChartService;
import org.talend.dataquality.analysis.Analysis;
import org.talend.dataquality.analysis.ExecutionLanguage;
import org.talend.dataquality.indicators.Indicator;
import org.talend.dq.CWMPlugin;
import org.talend.dq.analysis.explore.IDataExplorer;
import org.talend.dq.helper.SqlExplorerUtils;
import org.talend.dq.indicators.preview.table.ChartDataEntity;

/**
 * created by yyin on 2014-12-8 Detailled comment
 * 
 */
public class TOPChartUtils {

    private static Logger log = Logger.getLogger(TOPChartUtils.class);

    private static TOPChartUtils instance;

    private ITOPChartService chartService;

    public static TOPChartUtils getInstance() {
        if (instance == null) {
            instance = new TOPChartUtils();
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
            BundleContext context = CWMPlugin.getDefault().getBundleContext();
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

    public Object createBarChart(String title, Object dataset, boolean showLegend) {
        if (this.chartService != null) {
            return chartService.createBarChart(title, dataset, showLegend);
        }
        return null;
    }

    public Object createBarChart(String title, Object dataset) {
        if (this.chartService != null) {
            return chartService.createBarChart(title, dataset);
        }
        return null;
    }

    public Object createBenfordChart(String axisXLabel, String categoryAxisLabel, Object dataset, List<String> dotChartLabels,
            double[] formalValues, String title) {
        if (chartService != null) {
            return chartService.createBenfordChart(axisXLabel, categoryAxisLabel, dataset, dotChartLabels, formalValues, title);
        }
        return null;
    }

    public Object createTalendChartComposite(Object parentComponent, int style, Object chart, boolean useBuffer) {
        if (chartService != null) {
            return chartService.createTalendChartComposite(parentComponent, style, chart, useBuffer);
        }
        return null;
    }

    public Object createChartComposite(Object composite, int style, Object chart, boolean useBuffer) {
        if (chartService != null) {
            return chartService.createChartComposite(composite, style, chart, useBuffer);
        }
        return null;
    }

    public Object getDatasetFromChart(Object chart, int datasetIndex) {
        if (chartService != null) {
            return chartService.getDatasetFromChart(chart, datasetIndex);
        }
        return null;
    }

    public void decorateChart(Object chart, boolean withPlot) {
        if (chartService != null) {
            chartService.decorateChart(chart, withPlot);
        }
    }

    public void decorateColumnDependency(Object chart) {
        if (chartService != null) {
            chartService.decorateColumnDependency(chart);
        }
    }

    public void setOrientation(Object chart, boolean isHorizontal) {
        if (chartService != null) {
            chartService.setOrientation(chart, isHorizontal);
        }
    }

    public void setDisplayDecimalFormatOfChart(Object chart) {
        if (chartService != null) {
            chartService.setDisplayDecimalFormatOfChart(chart);
        }
    }

    public void addMouseListenerForChart(Object chartComposite, final Map<String, Object> menuMap) {
        if (chartService != null) {
            chartService.addMouseListenerForChart(chartComposite, menuMap);
        }
    }

    public Object createPieChart(String title, Object dataset, boolean showLegend, boolean toolTips, boolean urls) {
        if (chartService != null) {
            return chartService.createPieChart(title, dataset, showLegend, toolTips, urls);
        }
        return null;
    }

    public Object createBoxAndWhiskerChart(String title, Object dataset) {
        if (chartService != null) {
            return chartService.createBoxAndWhiskerChart(title, dataset);
        }
        return null;
    }

    public Object createStackedBarChart(String title, Object dataset, boolean showLegend) {
        if (chartService != null) {
            return chartService.createStackedBarChart(title, dataset, showLegend);
        }
        return null;
    }

    public Object createStackedBarChart(String title, Object dataset, boolean isHorizatal, boolean showLegend) {
        if (chartService != null) {
            return chartService.createStackedBarChart(title, dataset, isHorizatal, showLegend);
        }
        return null;
    }

    public void addListenerToChartComp(Object chartComposite, final String referenceLink, final String menuText) {
        if (chartService != null) {
            chartService.addListenerToChartComp(chartComposite, referenceLink, menuText);
        }
    }

    // checkSql: =true , use the check sql service as the judgement, = false, come from the column ana, use the input
    // compute as the judgement
    public Menu createMenu(final Shell shell, final IDataExplorer explorer, final Analysis analysis,
            final ExecutionLanguage currentEngine, final ChartDataEntity currentDataEntity, final Indicator currentIndicator,
            final boolean checkSql) {
        Menu menu = new Menu(shell, SWT.POP_UP);

        int createPatternFlag = 0;
        MenuItemEntity[] itemEntities = ChartTableMenuGenerator.generate(explorer, analysis, currentDataEntity);
        for (final MenuItemEntity itemEntity : itemEntities) {
            MenuItem item = new MenuItem(menu, SWT.PUSH);
            item.setText(itemEntity.getLabel());
            item.setImage(itemEntity.getIcon());
            item.setEnabled(DrillDownUtils.isMenuItemEnable(currentDataEntity, itemEntity, analysis));
            item.addSelectionListener(createSelectionAdapter(analysis, currentEngine, currentDataEntity, currentIndicator,
                    itemEntity, checkSql));

            if (ChartTableFactory.isPatternFrequencyIndicator(currentIndicator) && createPatternFlag == 0) {
                ChartTableFactory.createMenuOfGenerateRegularPattern(analysis, menu, currentDataEntity);
            }

            createPatternFlag++;
        }
        return menu;
    }

    private SelectionAdapter createSelectionAdapter(final Analysis analysis1, final ExecutionLanguage currentEngine,
            final ChartDataEntity currentDataEntity, final Indicator currentIndicator, final MenuItemEntity itemEntity,
            final boolean checkSql) {
        return new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (ExecutionLanguage.JAVA == currentEngine) {
                    try {
                        DrillDownEditorInput input = new DrillDownEditorInput(analysis1, currentDataEntity, itemEntity);
                        Boolean check = checkSql ? SqlExplorerUtils.getDefault().getSqlexplorerService() != null : input
                                .computeColumnValueLength(input.filterAdaptDataList());

                        if (check) {
                            CorePlugin
                                    .getDefault()
                                    .getWorkbench()
                                    .getActiveWorkbenchWindow()
                                    .getActivePage()
                                    .openEditor(input,
                                            "org.talend.dataprofiler.core.ui.editor.analysis.drilldown.drillDownResultEditor");//$NON-NLS-1$
                        } else {
                            if (!checkSql) {
                                MessageDialog.openWarning(null,
                                        Messages.getString("DelimitedFileIndicatorEvaluator.badlyForm.Title"),//$NON-NLS-1$
                                        Messages.getString("DelimitedFileIndicatorEvaluator.badlyForm.Message"));//$NON-NLS-1$
                            }
                        }

                    } catch (PartInitException e1) {
                        log.error(e1, e1);
                    }
                } else {
                    Display.getDefault().asyncExec(new Runnable() {

                        public void run() {
                            Connection tdDataProvider = SwitchHelpers.CONNECTION_SWITCH.doSwitch(analysis1.getContext()
                                    .getConnection());
                            String query = itemEntity.getQuery();
                            String editorName = currentIndicator.getName();
                            SqlExplorerUtils.getDefault().runInDQViewer(tdDataProvider, query, editorName);
                        }

                    });
                }
            }
        };
    }
}
