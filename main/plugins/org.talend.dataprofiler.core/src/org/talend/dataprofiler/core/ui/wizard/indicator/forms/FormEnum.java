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
package org.talend.dataprofiler.core.ui.wizard.indicator.forms;

import org.eclipse.ui.IEditorPart;
import org.talend.dataprofiler.core.CorePlugin;
import org.talend.dataprofiler.core.ui.editor.analysis.AnalysisEditor;
import org.talend.dataprofiler.core.ui.editor.preview.IndicatorUnit;
import org.talend.dataprofiler.core.ui.editor.preview.TableIndicatorUnit;
import org.talend.dataprofiler.help.HelpPlugin;
import org.talend.dataquality.analysis.ExecutionLanguage;
import org.talend.dataquality.indicators.DataminingType;
import org.talend.dataquality.indicators.definition.IndicatorDefinition;
import org.talend.dq.helper.UDIHelper;
import org.talend.dq.nodes.indicator.type.IndicatorEnum;
import org.talend.utils.sql.Java2SqlType;

/**
 * DOC zqin class global comment. Detailled comment
 */
public enum FormEnum {

    BinsDesignerForm("Bins Designer", "html/wizard/indicator/BinsDesigner.html"), //$NON-NLS-1$ //$NON-NLS-2$
    FreqBinsDesignerForm("Bins Designer", "html/wizard/indicator/BinsDesigner.html"), //$NON-NLS-1$ //$NON-NLS-2$
    TextParametersForm("Text Parameters", "html/wizard/indicator/TextParameter.html"), //$NON-NLS-1$ //$NON-NLS-2$
    FreqTextParametersForm("Text Parameters", "html/wizard/indicator/TextParameter.html"), //$NON-NLS-1$ //$NON-NLS-2$
    TimeSlicesForm("Time Slices", "html/wizard/indicator/TimeSlice.html"), //$NON-NLS-1$ //$NON-NLS-2$
    FreqTimeSliceForm("Time Slices", "html/wizard/indicator/TimeSlice.html"), //$NON-NLS-1$ //$NON-NLS-2$
    DataThresholdsForm("Data Thresholds", "html/wizard/indicator/DataThreshold.html"), //$NON-NLS-1$ //$NON-NLS-2$
    IndicatorThresholdsForm("Indicator Thresholds", "html/wizard/indicator/IndicatorThresholds.html"), //$NON-NLS-1$ //$NON-NLS-2$
    TextLengthForm("Text Length", "html/wizard/indicator/TextLength.html"), //$NON-NLS-1$ //$NON-NLS-2$
    FreqTextLengthForm("Blank Options", "html/wizard/indicator/BlankOptions.html"), //$NON-NLS-1$ //$NON-NLS-2$
    NumbericNominalForm("Frequency Table Parameters", ""), //$NON-NLS-1$ //$NON-NLS-2$
    JavaUDIParametersForm("Java UDI Parameters", ""), //$NON-NLS-1$ //$NON-NLS-2$
    ExpectedValueForm("Expected Value", "html/wizard/indicator/ExpectedValue.html"), //$NON-NLS-1$ //$NON-NLS-2$
    JavaOptionsForm("Java Options", "html/wizard/indicator/JavaOptions.html"), //$NON-NLS-1$ //$NON-NLS-2$
    PhoneNumberForm("Phone Number", "html/wizard/indicator/TextParameter.html"); //$NON-NLS-1$ //$NON-NLS-2$

    private String formName;

    private String helpHref;

    FormEnum(String formName, String helpHref) {
        this.formName = formName;
        this.helpHref = helpHref;
    }

    public String getFormName() {
        return formName;
    }

    public String getHelpHref() {
        return "/" + HelpPlugin.PLUGIN_ID + "/" + helpHref; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * 
     * use {@link IndicatorUnit#getHelpHref()} instead of it
     * 
     * @deprecated
     * @param indicatorUnit
     * @return
     */
    @Deprecated
    public static String[] getHelpHref(IndicatorUnit indicatorUnit) {

        return indicatorUnit.getHelpHref();
    }

    /**
     * 
     * use {@link IndicatorUnit#getHelpHref()} instead of it
     * 
     * @deprecated
     * @param indicatorUnit
     * @return
     */
    @Deprecated
    public static String[] getHelpHref(TableIndicatorUnit indicatorUnit) {

        return indicatorUnit.getHelpHref();
    }

    /**
     * 
     * use {@link IndicatorUnit#getFirstFormHelpHref()} instead of it
     * 
     * @param indicatorUnit
     * @return
     */
    public static String getFirstFormHelpHref(IndicatorUnit indicatorUnit) {
        return indicatorUnit.getFirstFormHelpHref();
    }

    /**
     * 
     * use {@link IndicatorUnit#getFirstFormHelpHref()} instead of it
     * 
     * @param indicatorUnit
     * @return
     */
    public static String getFirstFormHelpHref(TableIndicatorUnit indicatorUnit) {
        return indicatorUnit.getFirstFormHelpHref();
    }

    /**
     * use {@link IndicatorUnit#getForms()} instead of it
     * 
     * @deprecated
     * @param dataminingType
     * @param sqlType
     */
    @Deprecated
    public static FormEnum[] getForms(IndicatorUnit indicatorUnit) {
        return indicatorUnit.getForms();
    }

    /**
     * DOC talend Comment method "getFormEnumArray".
     * 
     * @param indicatorUnit
     * @param sqlType
     * @param dataminingType
     * @param indicatorType
     * @return
     */
    public static FormEnum[] getFormEnumArray(IndicatorDefinition indicatorDefinition, int sqlType,
            DataminingType dataminingType, IndicatorEnum indicatorType, String executionLanguage) {
        boolean isJavaEngine = ExecutionLanguage.JAVA.getLiteral().equals(executionLanguage);
        boolean isSqlEngine = ExecutionLanguage.SQL.getLiteral().equals(executionLanguage);
        FormEnum[] forms = null;
        switch (indicatorType) {

        case RowCountIndicatorEnum:
        case NullCountIndicatorEnum:
        case BlankCountIndicatorEnum:
        case DefValueCountIndicatorEnum:

            forms = new FormEnum[] { IndicatorThresholdsForm };

            break;
        case DistinctCountIndicatorEnum:
        case UniqueIndicatorEnum:
        case DuplicateCountIndicatorEnum:

            forms = new FormEnum[] { IndicatorThresholdsForm };

            if (Java2SqlType.isTextInSQL(sqlType)) {
                forms = new FormEnum[] { TextParametersForm, IndicatorThresholdsForm };
            }

            break;
        case MinLengthIndicatorEnum:
        case MaxLengthIndicatorEnum:
        case AverageLengthIndicatorEnum:
            // MOD gdbu 2011-10-9 TDQ-3549
            forms = new FormEnum[] { IndicatorThresholdsForm };
            // ~TDQ-3549
            break;
        case MinLengthWithBlankIndicatorEnum:
        case MinLengthWithNullIndicatorEnum:
        case MinLengthWithBlankNullIndicatorEnum:
        case MaxLengthWithBlankIndicatorEnum:
        case MaxLengthWithBlankNullIndicatorEnum:
        case MaxLengthWithNullIndicatorEnum:
        case AverageLengthWithBlankIndicatorEnum:
        case AverageLengthWithNullBlankIndicatorEnum:
        case AverageLengthWithNullIndicatorEnum:
            // MOD yyi 2010-12-23 17740:enable thresholds
            forms = new FormEnum[] { IndicatorThresholdsForm };

            break;
        case FrequencyIndicatorEnum:
        case LowFrequencyIndicatorEnum:
        case PatternFreqIndicatorEnum:
        case PatternLowFreqIndicatorEnum:
        case DatePatternFreqIndicatorEnum:
            if (dataminingType == DataminingType.INTERVAL) {
                if (Java2SqlType.isNumbericInSQL(sqlType)) {

                    forms = new FormEnum[] { FreqBinsDesignerForm };
                }

                if (Java2SqlType.isDateInSQL(sqlType)) {

                    // MOD qiongli 2011-11-8,TDQ-3864,remove TimeSlice from UI
                    forms = new FormEnum[] { NumbericNominalForm };
                }
            } else if (Java2SqlType.isTextInSQL(sqlType)) {
                // MOD qiongli 2012-2-7 TDQ-4627 javaOptionForm just for Java engine and Pattern Frequency.
                IEditorPart activeEditor = CorePlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage()
                        .getActiveEditor();
                ExecutionLanguage exeLanguage = null;
                if (activeEditor != null && (activeEditor instanceof AnalysisEditor)) {
                    exeLanguage = ((AnalysisEditor) activeEditor).getUIExecuteEngin();
                }
                if (exeLanguage != null
                        && ExecutionLanguage.JAVA.equals(exeLanguage)
                        && (indicatorType == IndicatorEnum.PatternFreqIndicatorEnum || indicatorType == IndicatorEnum.PatternLowFreqIndicatorEnum)) {
                    forms = new FormEnum[] { FreqTextParametersForm, FreqTextLengthForm, JavaOptionsForm };
                } else {
                    forms = new FormEnum[] { FreqTextParametersForm, FreqTextLengthForm };
                }
            } else if (dataminingType == DataminingType.NOMINAL) {

                if (Java2SqlType.isNumbericInSQL(sqlType)) {

                    forms = new FormEnum[] { NumbericNominalForm };
                }
            }

            break;
        case ModeIndicatorEnum:
            if (dataminingType == DataminingType.INTERVAL && Java2SqlType.isNumbericInSQL(sqlType)) {

                forms = new FormEnum[] { BinsDesignerForm, ExpectedValueForm };

            } else if (Java2SqlType.isTextInSQL(sqlType)) {

                forms = new FormEnum[] { TextParametersForm, ExpectedValueForm };
            }

            break;
        case BoxIIndicatorEnum:
        case RangeIndicatorEnum:
        case IQRIndicatorEnum:
            forms = new FormEnum[] { DataThresholdsForm };

            break;
        case MeanIndicatorEnum:
        case MedianIndicatorEnum:
        case LowerQuartileIndicatorEnum:
        case UpperQuartileIndicatorEnum:
        case MinValueIndicatorEnum:
        case MaxValueIndicatorEnum:
            forms = new FormEnum[] { IndicatorThresholdsForm };

            break;
        case RegexpMatchingIndicatorEnum:
        case SqlPatternMatchingIndicatorEnum:
        case AllMatchIndicatorEnum:
            forms = new FormEnum[] { IndicatorThresholdsForm };

            break;
        case SoundexIndicatorEnum:
        case SoundexLowIndicatorEnum:
        case DateFrequencyIndicatorEnum:
        case DateLowFrequencyIndicatorEnum:
        case YearFrequencyIndicatorEnum:
        case YearLowFrequencyIndicatorEnum:
        case QuarterFrequencyIndicatorEnum:
        case QuarterLowFrequencyIndicatorEnum:
        case MonthFrequencyIndicatorEnum:
        case MonthLowFrequencyIndicatorEnum:
        case WeekFrequencyIndicatorEnum:
        case WeekLowFrequencyIndicatorEnum:
            forms = new FormEnum[] { NumbericNominalForm };

            break;

        case UserDefinedIndicatorEnum:
            if (indicatorDefinition != null && UDIHelper.isJUDIValid(indicatorDefinition) && isJavaEngine) {
                forms = new FormEnum[] { JavaUDIParametersForm };
            } else if (UDIHelper.isFrequency((indicatorDefinition)) && isSqlEngine) {
                forms = new FormEnum[] { NumbericNominalForm };
            }

            break;
        case BinFrequencyIndicatorEnum:
        case BinLowFrequencyIndicatorEnum:
            if (Java2SqlType.isNumbericInSQL(sqlType)) {

                forms = new FormEnum[] { FreqBinsDesignerForm };
            }
            break;
        case ValidPhoneCountIndicatorEnum:
        case PossiblePhoneCountIndicatorEnum:
        case WellFormNationalPhoneCountIndicatorEnum:
        case WellFormIntePhoneCountIndicatorEnum:
        case WellFormE164PhoneCountIndicatorEnum:
            forms = new FormEnum[] { PhoneNumberForm, IndicatorThresholdsForm };
            break;
        case PhoneNumbStatisticsIndicatorEnum:
        case FormatFreqPieIndictorEnum:
            forms = new FormEnum[] { PhoneNumberForm };
            break;
        default:
        }
        return forms;
    }

    /**
     * 
     * use {@link TableIndicatorUnit#getForms()} instead of it
     * 
     * @deprecated
     * @param indicatorUnit
     * @return
     */
    @Deprecated
    public static FormEnum[] getForms(TableIndicatorUnit indicatorUnit) {
        return indicatorUnit.getForms();
    }

    /**
     * 
     * use {@link IndicatorUnit#isExsitingForm()} instead of it
     * 
     * @deprecated
     * @param indicatorUnit
     * @return
     */
    @Deprecated
    public static boolean isExsitingForm(IndicatorUnit indicatorUnit) {
        return indicatorUnit.isExsitingForm();
    }

    /**
     * 
     * use {@link IndicatorUnit#isExsitingForm()} instead of it
     * 
     * @param indicatorUnit
     * @return
     */
    public static boolean isExsitingForm(TableIndicatorUnit indicatorUnit) {
        return indicatorUnit.isExsitingForm();
    }
}
