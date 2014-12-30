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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.talend.dataprofiler.core.CorePlugin;
import org.talend.dataprofiler.core.i18n.internal.DefaultMessagesImpl;
import org.talend.dataprofiler.core.model.DelimitedFileIndicator;
import org.talend.dataprofiler.core.model.ModelElementIndicator;
import org.talend.dataprofiler.core.ui.editor.analysis.AnalysisEditor;
import org.talend.dataprofiler.core.ui.editor.analysis.ColumnMasterDetailsPage;
import org.talend.dataprofiler.core.ui.utils.ModelElementIndicatorRule;
import org.talend.dataquality.analysis.ExecutionLanguage;
import org.talend.dataquality.helpers.MetadataHelper;
import org.talend.dataquality.indicators.DatePatternFreqIndicator;
import org.talend.dataquality.indicators.Indicator;
import org.talend.dataquality.indicators.PhoneNumbStatisticsIndicator;
import org.talend.dataquality.indicators.definition.IndicatorDefinition;
import org.talend.dq.nodes.indicator.IIndicatorNode;

/**
 * This dialog is used to select indicator objects for different columns.
 */
public class IndicatorSelectDialog2 extends TrayDialog implements IIndicatorSelectDialog {

    private static final String DESCRIPTION = DefaultMessagesImpl.getString("IndicatorSelectDialog.description"); //$NON-NLS-1$

    private static final String PURPOSE = DefaultMessagesImpl.getString("IndicatorSelectDialog.purpose"); //$NON-NLS-1$

    private ModelElementIndicator[] modelElementIndicators;

    private final String title;

    // ADD by zshen:need language to decide DatePatternFrequencyIndicator whether can be choose bu user.
    private ExecutionLanguage language;

    private Label purposeLabel;

    private Label descriptionLabel;

    private IndicatorSelectGrid grid;

    /**
     * IndicatorSelectDialog2 constructor.
     * 
     * @param parentShell
     * @param title
     * @param modelElementIndicators
     */
    public IndicatorSelectDialog2(Shell parentShell, String title, ModelElementIndicator[] modelElementIndicators) {
        super(parentShell);
        this.title = title;
        this.modelElementIndicators = modelElementIndicators;

        int shellStyle = getShellStyle();
        setShellStyle(shellStyle | SWT.MAX | SWT.RESIZE);// MOD zshen: obtain language.
        Object editorPart = CorePlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        if (editorPart instanceof AnalysisEditor) {
            AnalysisEditor analyEditor = (AnalysisEditor) editorPart;
            if (analyEditor.getMasterPage() instanceof ColumnMasterDetailsPage) {
                this.language = ExecutionLanguage.get(((ColumnMasterDetailsPage) analyEditor.getMasterPage()).getExecCombo()
                        .getText());
            }
        }
        // ~

        for (ModelElementIndicator modelElementIndicator : modelElementIndicators) {
            modelElementIndicator.copyOldIndicatorEnum();
        }
    }

    /*
     * (non-Javadoc) Method declared in Window.
     */
    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        if (title != null) {
            shell.setText(title);
        }
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite comp = (Composite) super.createDialogArea(parent);

        int style = SWT.NONE;
        style |= SWT.V_SCROLL;
        style |= SWT.H_SCROLL;
        style |= SWT.BORDER;
        style |= SWT.SINGLE;
        grid = new IndicatorSelectGrid(this, comp, style, modelElementIndicators);
        // GridTreeViewer gridTreeViewer = new GridTreeViewer(grid);
        // gridTreeViewer.setLabelProvider(new GridTreeLabelProvider());
        // gridTreeViewer.setContentProvider(new GridTreeContentProvider());
        // gridTreeViewer.setRowHeaderLabelProvider(new GridTreeRowHeaderLabelProvider());
        // gridTreeViewer.setInput(IndicatorTreeModelBuilder.getRootNode());
        GridData controlGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        controlGridData.minimumWidth = 650;
        controlGridData.minimumHeight = 570;
        controlGridData.widthHint = Math.min(IndicatorSelectGrid.COLUMN_WIDTH * modelElementIndicators.length + 400,
                getParentShell().getClientArea().width - 350);
        controlGridData.heightHint = getParentShell().getClientArea().height - 150;

        grid.setLayoutData(controlGridData);

        Composite buttomComp = new Composite(comp, SWT.NONE);
        buttomComp.setLayout(new GridLayout());
        buttomComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        purposeLabel = new Label(buttomComp, SWT.WRAP);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(purposeLabel);
        descriptionLabel = new Label(buttomComp, SWT.WRAP);
        GridDataFactory.fillDefaults().minSize(400, 30).grab(true, true).applyTo(descriptionLabel);

        return comp;
    }

    public boolean isMatchCurrentIndicator(ModelElementIndicator currentIndicator, IIndicatorNode indicatorNode) {
        boolean returnCurrentIndicator = true;
        IIndicatorNode parentNode = indicatorNode.getParent();
        boolean isParentPhoneStatistics = parentNode != null && parentNode.getIndicatorInstance() != null
                && parentNode.getIndicatorInstance() instanceof PhoneNumbStatisticsIndicator;
        if (!ModelElementIndicatorRule.match(indicatorNode, currentIndicator, this.language)) {
            returnCurrentIndicator = false;
        }
        Indicator indicatorInstance = indicatorNode.getIndicatorInstance();
        if (null != indicatorInstance && !(indicatorInstance instanceof DatePatternFreqIndicator)
                && null != indicatorInstance.getIndicatorDefinition()
                && indicatorInstance.getIndicatorDefinition().getSqlGenericExpression().size() < 1
                && !indicatorNode.hasChildren() && !(currentIndicator instanceof DelimitedFileIndicator)
                && !isParentPhoneStatistics) {
            returnCurrentIndicator = false;
        }
        return returnCurrentIndicator;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        parent.setLayout(new GridLayout(4, false));
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    public ModelElementIndicator[] getResult() {
        return grid.getResult();
    }

    public void updateIndicatorInfo(GridItem item) {
        IIndicatorNode indicatorNode = ((IIndicatorNode) item.getData());
        if (indicatorNode != null) {
            Indicator indicator = indicatorNode.getIndicatorInstance();
            if (indicator != null) {
                IndicatorDefinition indicatorDefinition = indicator.getIndicatorDefinition();
                if ("".equals(MetadataHelper.getPurpose(indicatorDefinition))) {
                    purposeLabel.setText(PURPOSE + " " + indicator.getName());
                } else {
                    purposeLabel.setText(PURPOSE + " " + MetadataHelper.getPurpose(indicatorDefinition));
                }
                String description = DESCRIPTION + " " + MetadataHelper.getDescription(indicatorDefinition);
                descriptionLabel.setText(description);
                return;
            }
        }
        purposeLabel.setText(PURPOSE + " " + item.getText());
        descriptionLabel.setText(DESCRIPTION + " " + item.getText());
    }

    /**
     * select/deselect all enabled indicators when Ctrl+Shift+[A|N] is down.
     * 
     * @param selected
     */
    public void selectAllIndicators(boolean selected) {
        grid.setAllIndicators(selected);
    }

}
