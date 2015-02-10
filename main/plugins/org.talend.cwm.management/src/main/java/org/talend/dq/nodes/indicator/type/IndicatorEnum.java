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
package org.talend.dq.nodes.indicator.type;

import org.eclipse.emf.ecore.EClass;
import org.talend.dataquality.indicators.IndicatorsPackage;
import org.talend.dataquality.indicators.columnset.ColumnsetPackage;
import org.talend.dataquality.indicators.sql.IndicatorSqlPackage;

/**
 * @author rli
 */
public enum IndicatorEnum {
    SqlPatternMatchingIndicatorEnum(
                                    IndicatorsPackage.SQL_PATTERN_MATCHING_INDICATOR,
                                    IndicatorsPackage.Literals.SQL_PATTERN_MATCHING_INDICATOR,
                                    "SQL Pattern Matching", //$NON-NLS-1$
                                    null),
    RegexpMatchingIndicatorEnum(
                                IndicatorsPackage.REGEXP_MATCHING_INDICATOR,
                                IndicatorsPackage.Literals.REGEXP_MATCHING_INDICATOR,
                                "regexp matching", //$NON-NLS-1$
                                null),
    RowCountIndicatorEnum(IndicatorsPackage.ROW_COUNT_INDICATOR, IndicatorsPackage.Literals.ROW_COUNT_INDICATOR, "Row Count", //$NON-NLS-1$
                          null),
    NullCountIndicatorEnum(IndicatorsPackage.NULL_COUNT_INDICATOR, IndicatorsPackage.Literals.NULL_COUNT_INDICATOR, "Null Count", //$NON-NLS-1$
                           null),
    DistinctCountIndicatorEnum(
                               IndicatorsPackage.DISTINCT_COUNT_INDICATOR,
                               IndicatorsPackage.Literals.DISTINCT_COUNT_INDICATOR,
                               "Distinct Count", //$NON-NLS-1$
                               null),
    UniqueIndicatorEnum(
                        IndicatorsPackage.UNIQUE_COUNT_INDICATOR,
                        IndicatorsPackage.Literals.UNIQUE_COUNT_INDICATOR,
                        "Unique Count", //$NON-NLS-1$
                        null),
    DuplicateCountIndicatorEnum(
                                IndicatorsPackage.DUPLICATE_COUNT_INDICATOR,
                                IndicatorsPackage.Literals.DUPLICATE_COUNT_INDICATOR,
                                "Duplicate Count", //$NON-NLS-1$
                                null),
    BlankCountIndicatorEnum(
                            IndicatorsPackage.BLANK_COUNT_INDICATOR,
                            IndicatorsPackage.Literals.BLANK_COUNT_INDICATOR,
                            "Blank Count", //$NON-NLS-1$
                            null),
    DefValueCountIndicatorEnum(
                               IndicatorsPackage.DEF_VALUE_COUNT_INDICATOR,
                               IndicatorsPackage.Literals.DEF_VALUE_COUNT_INDICATOR,
                               "Default Value Count", //$NON-NLS-1$
                               null),

    MinLengthIndicatorEnum(
                           IndicatorsPackage.MIN_LENGTH_INDICATOR,
                           IndicatorsPackage.Literals.MIN_LENGTH_INDICATOR,
                           "Minimal Length", //$NON-NLS-1$
                           null),
    MinLengthWithNullIndicatorEnum(
                                   IndicatorsPackage.MIN_LENGTH_WITH_NULL_INDICATOR,
                                   IndicatorsPackage.Literals.MIN_LENGTH_WITH_NULL_INDICATOR,
                                   "Minimal Length With Null", //$NON-NLS-1$
                                   null),

    MinLengthWithBlankIndicatorEnum(
                                    IndicatorsPackage.MIN_LENGTH_WITH_BLANK_INDICATOR,
                                    IndicatorsPackage.Literals.MIN_LENGTH_WITH_BLANK_INDICATOR,
                                    "Minimal Length With Blank", //$NON-NLS-1$
                                    null),

    MinLengthWithBlankNullIndicatorEnum(
                                        IndicatorsPackage.MIN_LENGTH_WITH_BLANK_NULL_INDICATOR,
                                        IndicatorsPackage.Literals.MIN_LENGTH_WITH_BLANK_NULL_INDICATOR,
                                        "Minimal Length With Blank and Null", //$NON-NLS-1$
                                        null),

    MaxLengthIndicatorEnum(
                           IndicatorsPackage.MAX_LENGTH_INDICATOR,
                           IndicatorsPackage.Literals.MAX_LENGTH_INDICATOR,
                           "Maximal Length", //$NON-NLS-1$
                           null),
    MaxLengthWithNullIndicatorEnum(
                                   IndicatorsPackage.MAX_LENGTH_WITH_NULL_INDICATOR,
                                   IndicatorsPackage.Literals.MAX_LENGTH_WITH_NULL_INDICATOR,
                                   "Maximal Length With Null", //$NON-NLS-1$
                                   null),
    MaxLengthWithBlankIndicatorEnum(
                                    IndicatorsPackage.MAX_LENGTH_WITH_BLANK_INDICATOR,
                                    IndicatorsPackage.Literals.MAX_LENGTH_WITH_BLANK_INDICATOR,
                                    "Maximal Length With Blank", //$NON-NLS-1$
                                    null),
    MaxLengthWithBlankNullIndicatorEnum(
                                        IndicatorsPackage.MAX_LENGTH_WITH_BLANK_NULL_INDICATOR,
                                        IndicatorsPackage.Literals.MAX_LENGTH_WITH_BLANK_NULL_INDICATOR,
                                        "Maximal Length With Blank and Null", //$NON-NLS-1$
                                        null),

    AverageLengthIndicatorEnum(
                               IndicatorsPackage.AVERAGE_LENGTH_INDICATOR,
                               IndicatorsPackage.Literals.AVERAGE_LENGTH_INDICATOR,
                               "Average Length", //$NON-NLS-1$
                               null),
    AverageLengthWithNullIndicatorEnum(
                                       IndicatorsPackage.AVG_LENGTH_WITH_NULL_INDICATOR,
                                       IndicatorsPackage.Literals.AVG_LENGTH_WITH_NULL_INDICATOR,
                                       "Average Length With Null", //$NON-NLS-1$
                                       null),
    AverageLengthWithBlankIndicatorEnum(
                                        IndicatorsPackage.AVG_LENGTH_WITH_BLANK_INDICATOR,
                                        IndicatorsPackage.Literals.AVG_LENGTH_WITH_BLANK_INDICATOR,
                                        "Average Length With Blank", //$NON-NLS-1$
                                        null),
    AverageLengthWithNullBlankIndicatorEnum(
                                            IndicatorsPackage.AVG_LENGTH_WITH_BLANK_NULL_INDICATOR,
                                            IndicatorsPackage.Literals.AVG_LENGTH_WITH_BLANK_NULL_INDICATOR,
                                            "Average Length With Blank and Null", //$NON-NLS-1$
                                            null),

    ModeIndicatorEnum(IndicatorsPackage.MODE_INDICATOR, IndicatorsPackage.Literals.MODE_INDICATOR, "Mode", null), //$NON-NLS-1$

    FrequencyIndicatorEnum(
                           IndicatorsPackage.FREQUENCY_INDICATOR,
                           IndicatorsPackage.Literals.FREQUENCY_INDICATOR,
                           "Frequency Table", //$NON-NLS-1$
                           null),

    DateFrequencyIndicatorEnum(
                               IndicatorsPackage.DATE_FREQUENCY_INDICATOR,
                               IndicatorsPackage.Literals.DATE_FREQUENCY_INDICATOR,
                               "Date Frequency Table", //$NON-NLS-1$
                               null),
    WeekFrequencyIndicatorEnum(
                               IndicatorsPackage.WEEK_FREQUENCY_INDICATOR,
                               IndicatorsPackage.Literals.WEEK_FREQUENCY_INDICATOR,
                               "Week Frequency Table", //$NON-NLS-1$
                               null),
    MonthFrequencyIndicatorEnum(
                                IndicatorsPackage.MONTH_FREQUENCY_INDICATOR,
                                IndicatorsPackage.Literals.MONTH_FREQUENCY_INDICATOR,
                                "Month Frequency Table", //$NON-NLS-1$
                                null),
    QuarterFrequencyIndicatorEnum(
                                  IndicatorsPackage.QUARTER_FREQUENCY_INDICATOR,
                                  IndicatorsPackage.Literals.QUARTER_FREQUENCY_INDICATOR,
                                  "Quarter Frequency Table", //$NON-NLS-1$
                                  null),
    YearFrequencyIndicatorEnum(
                               IndicatorsPackage.YEAR_FREQUENCY_INDICATOR,
                               IndicatorsPackage.Literals.YEAR_FREQUENCY_INDICATOR,
                               "Year Frequency Table", //$NON-NLS-1$
                               null),
    BinFrequencyIndicatorEnum(
                              IndicatorsPackage.BIN_FREQUENCY_INDICATOR,
                              IndicatorsPackage.Literals.BIN_FREQUENCY_INDICATOR,
                              "Bin Frequency Table", //$NON-NLS-1$
                              null),

    LowFrequencyIndicatorEnum(
                              IndicatorsPackage.LOW_FREQUENCY_INDICATOR,
                              IndicatorsPackage.Literals.LOW_FREQUENCY_INDICATOR,
                              "Low Frequency Table", //$NON-NLS-1$
                              null),

    DateLowFrequencyIndicatorEnum(
                                  IndicatorsPackage.DATE_LOW_FREQUENCY_INDICATOR,
                                  IndicatorsPackage.Literals.DATE_LOW_FREQUENCY_INDICATOR,
                                  "Date Low Frequency Table", //$NON-NLS-1$
                                  null),
    WeekLowFrequencyIndicatorEnum(
                                  IndicatorsPackage.WEEK_LOW_FREQUENCY_INDICATOR,
                                  IndicatorsPackage.Literals.WEEK_LOW_FREQUENCY_INDICATOR,
                                  "Week Low Frequency Table", //$NON-NLS-1$
                                  null),
    MonthLowFrequencyIndicatorEnum(
                                   IndicatorsPackage.MONTH_LOW_FREQUENCY_INDICATOR,
                                   IndicatorsPackage.Literals.MONTH_LOW_FREQUENCY_INDICATOR,
                                   "Month Low Frequency Table", //$NON-NLS-1$
                                   null),
    QuarterLowFrequencyIndicatorEnum(
                                     IndicatorsPackage.QUARTER_LOW_FREQUENCY_INDICATOR,
                                     IndicatorsPackage.Literals.QUARTER_LOW_FREQUENCY_INDICATOR,
                                     "Quarter Low Frequency Table", //$NON-NLS-1$
                                     null),
    YearLowFrequencyIndicatorEnum(
                                  IndicatorsPackage.YEAR_LOW_FREQUENCY_INDICATOR,
                                  IndicatorsPackage.Literals.YEAR_LOW_FREQUENCY_INDICATOR,
                                  "Year Low Frequency Table", //$NON-NLS-1$
                                  null),
    BinLowFrequencyIndicatorEnum(
                                 IndicatorsPackage.BIN_LOW_FREQUENCY_INDICATOR,
                                 IndicatorsPackage.Literals.BIN_LOW_FREQUENCY_INDICATOR,
                                 "Bin Low Frequency Table", //$NON-NLS-1$
                                 null),

    // MOD mzhao 2009-03-05 Soundex frequency indicator.
    SoundexIndicatorEnum(
                         IndicatorsPackage.SOUNDEX_FREQ_INDICATOR,
                         IndicatorsPackage.Literals.SOUNDEX_FREQ_INDICATOR,
                         "Soundex Frequency Table", //$NON-NLS-1$
                         null),
    SoundexLowIndicatorEnum(
                            IndicatorsPackage.SOUNDEX_LOW_FREQ_INDICATOR,
                            IndicatorsPackage.Literals.SOUNDEX_LOW_FREQ_INDICATOR,
                            "Soundex Low Frequency Table", //$NON-NLS-1$
                            null),

    PatternFreqIndicatorEnum(
                             IndicatorsPackage.PATTERN_FREQ_INDICATOR,
                             IndicatorsPackage.Literals.PATTERN_FREQ_INDICATOR,
                             "Pattern Frequency Table", //$NON-NLS-1$
                             null),
    PatternLowFreqIndicatorEnum(
                                IndicatorsPackage.PATTERN_LOW_FREQ_INDICATOR,
                                IndicatorsPackage.Literals.PATTERN_LOW_FREQ_INDICATOR,
                                "Pattern Low Frequency Table", //$NON-NLS-1$
                                null),
    DatePatternFreqIndicatorEnum(
                                 IndicatorsPackage.DATE_PATTERN_FREQ_INDICATOR,
                                 IndicatorsPackage.Literals.DATE_PATTERN_FREQ_INDICATOR,
                                 "Date Pattern Frequency Table", //$NON-NLS-1$
                                 null),

    MeanIndicatorEnum(IndicatorsPackage.MEAN_INDICATOR, IndicatorsPackage.Literals.MEAN_INDICATOR, "Mean", null), //$NON-NLS-1$
    MedianIndicatorEnum(IndicatorsPackage.MEDIAN_INDICATOR, IndicatorsPackage.Literals.MEDIAN_INDICATOR, "Median", null), //$NON-NLS-1$
    MinValueIndicatorEnum(IndicatorsPackage.MIN_VALUE_INDICATOR, IndicatorsPackage.Literals.MIN_VALUE_INDICATOR, "Minimum", //$NON-NLS-1$
                          null),
    MaxValueIndicatorEnum(IndicatorsPackage.MAX_VALUE_INDICATOR, IndicatorsPackage.Literals.MAX_VALUE_INDICATOR, "Maximum", //$NON-NLS-1$
                          null),
    LowerQuartileIndicatorEnum(
                               IndicatorsPackage.LOWER_QUARTILE_INDICATOR,
                               IndicatorsPackage.Literals.LOWER_QUARTILE_INDICATOR,
                               "Lower Quartile", //$NON-NLS-1$
                               null),
    UpperQuartileIndicatorEnum(
                               IndicatorsPackage.UPPER_QUARTILE_INDICATOR,
                               IndicatorsPackage.Literals.UPPER_QUARTILE_INDICATOR,
                               "Upper Quartile", //$NON-NLS-1$
                               null),
    CountsIndicatorEnum(IndicatorsPackage.COUNTS_INDICATOR, IndicatorsPackage.Literals.COUNTS_INDICATOR, "Simple Statistics", //$NON-NLS-1$
                        new IndicatorEnum[] { RowCountIndicatorEnum, NullCountIndicatorEnum, DistinctCountIndicatorEnum,
                                UniqueIndicatorEnum, DuplicateCountIndicatorEnum, BlankCountIndicatorEnum,
                                DefValueCountIndicatorEnum }),
    TextIndicatorEnum(IndicatorsPackage.TEXT_INDICATOR, IndicatorsPackage.Literals.TEXT_INDICATOR, "Text Statistics", //$NON-NLS-1$
                      new IndicatorEnum[] { MinLengthIndicatorEnum, MinLengthWithNullIndicatorEnum,
                              MinLengthWithBlankIndicatorEnum, MinLengthWithBlankNullIndicatorEnum, MaxLengthIndicatorEnum,
                              MaxLengthWithNullIndicatorEnum, MaxLengthWithBlankIndicatorEnum,
                              MaxLengthWithBlankNullIndicatorEnum, AverageLengthIndicatorEnum,
                              AverageLengthWithNullIndicatorEnum, AverageLengthWithBlankIndicatorEnum,
                              AverageLengthWithNullBlankIndicatorEnum }),
    IQRIndicatorEnum(IndicatorsPackage.IQR_INDICATOR, IndicatorsPackage.Literals.IQR_INDICATOR, "Inter Quartile Range", //$NON-NLS-1$
                     new IndicatorEnum[] { LowerQuartileIndicatorEnum, UpperQuartileIndicatorEnum }),
    RangeIndicatorEnum(IndicatorsPackage.RANGE_INDICATOR, IndicatorsPackage.Literals.RANGE_INDICATOR, "Range", //$NON-NLS-1$
                       new IndicatorEnum[] { MinValueIndicatorEnum, MaxValueIndicatorEnum }),
    BoxIIndicatorEnum(IndicatorsPackage.BOX_INDICATOR, IndicatorsPackage.Literals.BOX_INDICATOR, "Summary Statistics", //$NON-NLS-1$
                      new IndicatorEnum[] { MeanIndicatorEnum, MedianIndicatorEnum, IQRIndicatorEnum, RangeIndicatorEnum }),
    WhereRuleIndicatorEnum(
                           IndicatorSqlPackage.WHERE_RULE_INDICATOR,
                           IndicatorSqlPackage.Literals.WHERE_RULE_INDICATOR,
                           "where rule", //$NON-NLS-1$
                           null),
    UserDefinedIndicatorEnum(
                             IndicatorSqlPackage.USER_DEF_INDICATOR,
                             IndicatorSqlPackage.Literals.USER_DEF_INDICATOR,
                             "User Defined Indicator", //$NON-NLS-1$
                             null),
    ColumnDependencyIndicatorEnum(
                                  ColumnsetPackage.COLUMN_DEPENDENCY_INDICATOR,
                                  ColumnsetPackage.Literals.COLUMN_DEPENDENCY_INDICATOR,
                                  // MOD msjian 2011-06-07 22021: fixed running the Function dependency analysis
                                  // imported from 4.0 works well.
                                  "Functional Dependency Indicator", //$NON-NLS-1$
                                  null),
    ColumnSetMultiValueIndicatorEnum(
                                     ColumnsetPackage.COLUMN_SET_MULTI_VALUE_INDICATOR,
                                     ColumnsetPackage.Literals.COLUMN_SET_MULTI_VALUE_INDICATOR,
                                     "Column Set Multi Value Indicator", //$NON-NLS-1$
                                     null),
    SimpleStatIndicatorEnum(
                            ColumnsetPackage.SIMPLE_STAT_INDICATOR,
                            ColumnsetPackage.Literals.SIMPLE_STAT_INDICATOR,
                            "Simple Stat Indicator", //$NON-NLS-1$
                            null),

    AllMatchIndicatorEnum(
                          ColumnsetPackage.ALL_MATCH_INDICATOR,
                          ColumnsetPackage.Literals.ALL_MATCH_INDICATOR,
                          "All Match Indicator", //$NON-NLS-1$
                          null),
    ValidPhoneCountIndicatorEnum(
                                 IndicatorsPackage.VALID_PHONE_COUNT_INDICATOR,
                                 IndicatorsPackage.Literals.VALID_PHONE_COUNT_INDICATOR,
                                 "Valid Phone Number Count", //$NON-NLS-1$
                                 null),
    PossiblePhoneCountIndicatorEnum(
                                    IndicatorsPackage.POSSIBLE_PHONE_COUNT_INDICATOR,
                                    IndicatorsPackage.Literals.POSSIBLE_PHONE_COUNT_INDICATOR,
                                    "Possible Phone Number Count", //$NON-NLS-1$
                                    null),
    ValidRegCodeCountIndicatorEnum(
                                   IndicatorsPackage.VALID_REG_CODE_COUNT_INDICATOR,
                                   IndicatorsPackage.Literals.VALID_REG_CODE_COUNT_INDICATOR,
                                   "Valid Region Code Count", //$NON-NLS-1$
                                   null),
    InvalidRegCodeCountIndicatorEnum(
                                     IndicatorsPackage.INVALID_REG_CODE_COUNT_INDICATOR,
                                     IndicatorsPackage.Literals.INVALID_REG_CODE_COUNT_INDICATOR,
                                     "Invalid Region Code Count", //$NON-NLS-1$
                                     null),
    WellFormIntePhoneCountIndicatorEnum(
                                        IndicatorsPackage.WELL_FORM_INTE_PHONE_COUNT_INDICATOR,
                                        IndicatorsPackage.Literals.WELL_FORM_INTE_PHONE_COUNT_INDICATOR,
                                        "Well Formed International Phone Number Count", //$NON-NLS-1$
                                        null),
    WellFormNationalPhoneCountIndicatorEnum(
                                            IndicatorsPackage.WELL_FORM_NATIONAL_PHONE_COUNT_INDICATOR,
                                            IndicatorsPackage.Literals.WELL_FORM_NATIONAL_PHONE_COUNT_INDICATOR,
                                            "Well Formed National Phone Number Count", //$NON-NLS-1$
                                            null),
    WellFormE164PhoneCountIndicatorEnum(
                                        IndicatorsPackage.WELL_FORM_E164_PHONE_COUNT_INDICATOR,
                                        IndicatorsPackage.Literals.WELL_FORM_E164_PHONE_COUNT_INDICATOR,
                                        "Well Formed E164 Phone Number Count", //$NON-NLS-1$
                                        null),
    FormatFreqPieIndictorEnum(
                              IndicatorsPackage.FORMAT_FREQ_PIE_INDICATOR,
                              IndicatorsPackage.Literals.FORMAT_FREQ_PIE_INDICATOR,
                              "Format Phone Number Frequency", //$NON-NLS-1$
                              null),
    PhoneNumbStatisticsIndicatorEnum(
                                     IndicatorsPackage.PHONE_NUMB_STATISTICS_INDICATOR,
                                     IndicatorsPackage.Literals.PHONE_NUMB_STATISTICS_INDICATOR,
                                     "Phone Number Statistics", //$NON-NLS-1$
                                     new IndicatorEnum[] { ValidPhoneCountIndicatorEnum, ValidRegCodeCountIndicatorEnum,
                                             InvalidRegCodeCountIndicatorEnum, PossiblePhoneCountIndicatorEnum,
                                             WellFormIntePhoneCountIndicatorEnum, WellFormNationalPhoneCountIndicatorEnum,
                                             WellFormE164PhoneCountIndicatorEnum, FormatFreqPieIndictorEnum }),
    BenfordLawFrequencyIndicatorEnum(
                                     IndicatorsPackage.BENFORD_LAW_FREQUENCY_INDICATOR,
                                     IndicatorsPackage.Literals.BENFORD_LAW_FREQUENCY_INDICATOR,
                                     "Benford Law Frequency", //$NON-NLS-1$
                                     null),

    PatternIndicatorEnum(
                         IndicatorsPackage.PATTERN_MATCHING_INDICATOR,
                         IndicatorsPackage.Literals.PATTERN_MATCHING_INDICATOR,
                         "Patterns", //$NON-NLS-1$
                         null);

    private EClass indicatorType;

    private String label;

    private IndicatorEnum[] children;

    private IndicatorEnum parent;

    private final int indicatorClassifierId;

    IndicatorEnum(int indicatorClassifierId, EClass indicatorType, String label, IndicatorEnum[] children) {
        this.indicatorClassifierId = indicatorClassifierId;
        this.indicatorType = indicatorType;
        this.label = label;
        setChildren(children);
    }

    /**
     * DOC rli Comment method "setChildren".
     * 
     * @param children
     */
    private void setChildren(IndicatorEnum[] children) {
        this.children = children;
        if (this.children == null) {
            return;
        }
        for (IndicatorEnum indicatorEnum : children) {
            indicatorEnum.setParent(this);
        }
    }

    /**
     * @return the indicator
     */
    public EClass getIndicatorType() {
        return indicatorType;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return the children
     */
    public IndicatorEnum[] getChildren() {
        return children;
    }

    /**
     * Getter for indicatorClassifierId.
     * 
     * @return the indicatorClassifierId
     */
    public int getIndicatorClassifierId() {
        return indicatorClassifierId;
    }

    public boolean hasChildren() {
        return children != null;
    }

    public boolean hasParent() {
        return parent != null;
    }

    private void setParent(IndicatorEnum parent) {
        this.parent = parent;
    }

    /**
     * Getter for parent.
     * 
     * @return the parent
     */
    public IndicatorEnum getParent() {
        return parent;
    }

    public static IndicatorEnum findIndicatorEnum(EClass indicatorType) {
        IndicatorEnum returnEnum = null;

        if (indicatorType == RowCountIndicatorEnum.getIndicatorType()) {
            returnEnum = RowCountIndicatorEnum;
        } else if (indicatorType == NullCountIndicatorEnum.getIndicatorType()) {
            returnEnum = NullCountIndicatorEnum;
        } else if (indicatorType == DistinctCountIndicatorEnum.getIndicatorType()) {
            returnEnum = DistinctCountIndicatorEnum;
        } else if (indicatorType == UniqueIndicatorEnum.getIndicatorType()) {
            returnEnum = UniqueIndicatorEnum;
        } else if (indicatorType == DuplicateCountIndicatorEnum.getIndicatorType()) {
            returnEnum = DuplicateCountIndicatorEnum;
        } else if (indicatorType == BlankCountIndicatorEnum.getIndicatorType()) {
            returnEnum = BlankCountIndicatorEnum;
        } else if (indicatorType == MinLengthIndicatorEnum.getIndicatorType()) {
            returnEnum = MinLengthIndicatorEnum;
        } else if (indicatorType == MinLengthWithNullIndicatorEnum.getIndicatorType()) {
            returnEnum = MinLengthWithNullIndicatorEnum;
        } else if (indicatorType == MinLengthWithBlankIndicatorEnum.getIndicatorType()) {
            returnEnum = MinLengthWithBlankIndicatorEnum;
        } else if (indicatorType == MinLengthWithBlankNullIndicatorEnum.getIndicatorType()) {
            returnEnum = MinLengthWithBlankNullIndicatorEnum;
        } else if (indicatorType == MaxLengthWithNullIndicatorEnum.getIndicatorType()) {
            returnEnum = MaxLengthWithNullIndicatorEnum;
        } else if (indicatorType == MaxLengthIndicatorEnum.getIndicatorType()) {
            returnEnum = MaxLengthIndicatorEnum;
        } else if (indicatorType == MaxLengthWithBlankIndicatorEnum.getIndicatorType()) {
            returnEnum = MaxLengthWithBlankIndicatorEnum;
        } else if (indicatorType == MaxLengthWithBlankNullIndicatorEnum.getIndicatorType()) {
            returnEnum = MaxLengthWithBlankNullIndicatorEnum;
        } else if (indicatorType == AverageLengthIndicatorEnum.getIndicatorType()) {
            returnEnum = AverageLengthIndicatorEnum;
        } else if (indicatorType == AverageLengthWithBlankIndicatorEnum.getIndicatorType()) {
            returnEnum = AverageLengthWithBlankIndicatorEnum;
        } else if (indicatorType == AverageLengthWithNullIndicatorEnum.getIndicatorType()) {
            returnEnum = AverageLengthWithNullIndicatorEnum;
        } else if (indicatorType == AverageLengthWithNullBlankIndicatorEnum.getIndicatorType()) {
            returnEnum = AverageLengthWithNullBlankIndicatorEnum;
        } else if (indicatorType == ModeIndicatorEnum.getIndicatorType()) {
            returnEnum = ModeIndicatorEnum;
        } else if (indicatorType == MeanIndicatorEnum.getIndicatorType()) {
            returnEnum = MeanIndicatorEnum;
        } else if (indicatorType == MedianIndicatorEnum.getIndicatorType()) {
            returnEnum = MedianIndicatorEnum;
        } else if (indicatorType == MinValueIndicatorEnum.getIndicatorType()) {
            returnEnum = MinValueIndicatorEnum;
        } else if (indicatorType == MaxValueIndicatorEnum.getIndicatorType()) {
            returnEnum = MaxValueIndicatorEnum;
        } else if (indicatorType == LowerQuartileIndicatorEnum.getIndicatorType()) {
            returnEnum = LowerQuartileIndicatorEnum;
        } else if (indicatorType == UpperQuartileIndicatorEnum.getIndicatorType()) {
            returnEnum = UpperQuartileIndicatorEnum;
        } else if (indicatorType == IQRIndicatorEnum.getIndicatorType()) {
            returnEnum = IQRIndicatorEnum;
        } else if (indicatorType == RangeIndicatorEnum.getIndicatorType()) {
            returnEnum = RangeIndicatorEnum;
        } else if (indicatorType == SoundexIndicatorEnum.getIndicatorType()) {
            returnEnum = SoundexIndicatorEnum;
        } else if (indicatorType == SoundexLowIndicatorEnum.getIndicatorType()) {
            returnEnum = SoundexLowIndicatorEnum;
        } else if (indicatorType == FrequencyIndicatorEnum.getIndicatorType()) {
            returnEnum = FrequencyIndicatorEnum;
        } else if (indicatorType == DateFrequencyIndicatorEnum.getIndicatorType()) {
            returnEnum = DateFrequencyIndicatorEnum;
        } else if (indicatorType == WeekFrequencyIndicatorEnum.getIndicatorType()) {
            returnEnum = WeekFrequencyIndicatorEnum;
        } else if (indicatorType == MonthFrequencyIndicatorEnum.getIndicatorType()) {
            returnEnum = MonthFrequencyIndicatorEnum;
        } else if (indicatorType == QuarterFrequencyIndicatorEnum.getIndicatorType()) {
            returnEnum = QuarterFrequencyIndicatorEnum;
        } else if (indicatorType == YearFrequencyIndicatorEnum.getIndicatorType()) {
            returnEnum = YearFrequencyIndicatorEnum;
        } else if (indicatorType == BinFrequencyIndicatorEnum.getIndicatorType()) {
            returnEnum = BinFrequencyIndicatorEnum;
        } else if (indicatorType == LowFrequencyIndicatorEnum.getIndicatorType()) {
            returnEnum = LowFrequencyIndicatorEnum;
        } else if (indicatorType == DateLowFrequencyIndicatorEnum.getIndicatorType()) {
            returnEnum = DateLowFrequencyIndicatorEnum;
        } else if (indicatorType == WeekLowFrequencyIndicatorEnum.getIndicatorType()) {
            returnEnum = WeekLowFrequencyIndicatorEnum;
        } else if (indicatorType == MonthLowFrequencyIndicatorEnum.getIndicatorType()) {
            returnEnum = MonthLowFrequencyIndicatorEnum;
        } else if (indicatorType == QuarterLowFrequencyIndicatorEnum.getIndicatorType()) {
            returnEnum = QuarterLowFrequencyIndicatorEnum;
        } else if (indicatorType == YearLowFrequencyIndicatorEnum.getIndicatorType()) {
            returnEnum = YearLowFrequencyIndicatorEnum;
        } else if (indicatorType == BinLowFrequencyIndicatorEnum.getIndicatorType()) {
            returnEnum = BinLowFrequencyIndicatorEnum;
        } else if (indicatorType == PatternFreqIndicatorEnum.getIndicatorType()) {
            returnEnum = PatternFreqIndicatorEnum;
        } else if (indicatorType == PatternLowFreqIndicatorEnum.getIndicatorType()) {
            returnEnum = PatternLowFreqIndicatorEnum;
        } else if (indicatorType == DatePatternFreqIndicatorEnum.getIndicatorType()) {
            returnEnum = DatePatternFreqIndicatorEnum;
        } else if (indicatorType == TextIndicatorEnum.getIndicatorType()) {
            returnEnum = TextIndicatorEnum;
        } else if (indicatorType == BoxIIndicatorEnum.getIndicatorType()) {
            returnEnum = BoxIIndicatorEnum;
        } else if (indicatorType == CountsIndicatorEnum.getIndicatorType()) {
            returnEnum = CountsIndicatorEnum;
        } else if (indicatorType == RegexpMatchingIndicatorEnum.getIndicatorType()) {
            returnEnum = RegexpMatchingIndicatorEnum;
        } else if (indicatorType == SqlPatternMatchingIndicatorEnum.getIndicatorType()) {
            returnEnum = SqlPatternMatchingIndicatorEnum;
        } else if (indicatorType == WhereRuleIndicatorEnum.getIndicatorType()) {
            returnEnum = WhereRuleIndicatorEnum;
        } else if (indicatorType == DefValueCountIndicatorEnum.getIndicatorType()) {
            returnEnum = DefValueCountIndicatorEnum;
        } else if (indicatorType == UserDefinedIndicatorEnum.getIndicatorType()) {
            returnEnum = UserDefinedIndicatorEnum;
        } else if (indicatorType == ColumnDependencyIndicatorEnum.getIndicatorType()) {
            returnEnum = ColumnDependencyIndicatorEnum;
        } else if (indicatorType == AllMatchIndicatorEnum.getIndicatorType()) {
            returnEnum = AllMatchIndicatorEnum;
        } else if (indicatorType == ValidPhoneCountIndicatorEnum.getIndicatorType()) {
            returnEnum = ValidPhoneCountIndicatorEnum;
        } else if (indicatorType == ValidRegCodeCountIndicatorEnum.getIndicatorType()) {
            returnEnum = ValidRegCodeCountIndicatorEnum;
        } else if (indicatorType == PossiblePhoneCountIndicatorEnum.getIndicatorType()) {
            returnEnum = PossiblePhoneCountIndicatorEnum;
        } else if (indicatorType == InvalidRegCodeCountIndicatorEnum.getIndicatorType()) {
            returnEnum = InvalidRegCodeCountIndicatorEnum;
        } else if (indicatorType == WellFormE164PhoneCountIndicatorEnum.getIndicatorType()) {
            returnEnum = WellFormE164PhoneCountIndicatorEnum;
        } else if (indicatorType == WellFormIntePhoneCountIndicatorEnum.getIndicatorType()) {
            returnEnum = WellFormIntePhoneCountIndicatorEnum;
        } else if (indicatorType == WellFormNationalPhoneCountIndicatorEnum.getIndicatorType()) {
            returnEnum = WellFormNationalPhoneCountIndicatorEnum;
        } else if (indicatorType == PhoneNumbStatisticsIndicatorEnum.getIndicatorType()) {
            returnEnum = PhoneNumbStatisticsIndicatorEnum;
        } else if (indicatorType == FormatFreqPieIndictorEnum.getIndicatorType()) {
            returnEnum = FormatFreqPieIndictorEnum;
        } else if (indicatorType == BenfordLawFrequencyIndicatorEnum.getIndicatorType()) {
            returnEnum = BenfordLawFrequencyIndicatorEnum;
        }

        return returnEnum;

    }

    /**
     * 
     * Judge the enum is plainIndicatorEnum
     * 
     * @param indicatorEnumn
     * @return True if indicatorEnumn is plainIndicatorEnum
     */
    public static boolean isPlainIndicatorEnum(IndicatorEnum indicatorEnumn) {
        return indicatorEnumn != RegexpMatchingIndicatorEnum && indicatorEnumn != SqlPatternMatchingIndicatorEnum
                && indicatorEnumn != WhereRuleIndicatorEnum && indicatorEnumn != UserDefinedIndicatorEnum;
    }

    /**
     * 
     * Judge the enum is SpecialIndicatorEnum
     * 
     * @param indicatorEnumn
     * @return True if indicatorEnumn is SpecialIndicatorEnum
     */
    public static boolean isSpecialIndicatorEnum(IndicatorEnum indicatorEnumn) {
        return indicatorEnumn == RegexpMatchingIndicatorEnum || indicatorEnumn == SqlPatternMatchingIndicatorEnum
                || indicatorEnumn == UserDefinedIndicatorEnum;
    }

    public boolean isAChildOf(IndicatorEnum parent) {
        if (parent.getChildren() != null) {
            for (IndicatorEnum child : parent.getChildren()) {
                if (child == this) {
                    return true;
                }
            }
        }

        return false;
    }
}
