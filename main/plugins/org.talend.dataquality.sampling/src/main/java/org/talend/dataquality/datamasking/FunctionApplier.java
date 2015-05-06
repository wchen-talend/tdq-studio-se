package org.talend.dataquality.datamasking;

import java.util.Date;

import org.talend.dataquality.datamasking.CreditCardGenerator.CreditCardType;
import org.talend.dataquality.duplicating.DateChanger;
import org.talend.dataquality.duplicating.RandomWrapper;

public class FunctionApplier {

    /**
     * This enum holds all the function that can be applied when masking data.
     *
     * Set_To_Null : This function will return null Date_variance(n) : This function will change a date by a random
     * number of days between -n and n. Numeric_Variance(n) : This function will multiply a numeric value by a random
     * number between -n and n. Generate_Credit_Card : This function will create a new credit card number which will be
     * false but still pass the checksum algorithm. Generate_Credit_Card_Format : Similar to the previous function but
     * will keep the input format (type, length and prefix). Generate_Account_Number : This function will create a valid
     * but false French IBAN number. Generate_Account_Number_Format : Similar to the previous function but will keep the
     * original country of the input (if given). Generate_Phone_Number : This function generates a correct randomly
     * generated French phone number in international format. Generate_Phone_Number_Format(n) : Similar to the previous
     * one, but will keep the n first digits.
     * 
     */
    public enum Function {
        SET_TO_NULL,
        DATE_VARIANCE,
        NUMERIC_VARIANCE,
        GENERATE_CREDIT_CARD,
        GENERATE_CREDIT_CARD_FORMAT,
        GENERATE_ACCOUNT_NUMBER,
        GENERATE_ACCOUNT_NUMBER_FORMAT,
        GENERATE_PHONE_NUMBER,
        GENERATE_PHONE_NUMBER_FORMAT
    };

    private DateChanger dateChanger = new DateChanger();

    private RandomWrapper rnd = new RandomWrapper();

    public void setSeed(long seed) {
        dateChanger.setSeed(seed);
        rnd = new RandomWrapper(seed);
    }

    /**
     * Method "generateDuplicate". This method is called when the input is a Date.
     * 
     * @param date The input sent to the function.
     * @param function The function used on the date parameter (see the enum Function).
     * @param extraParameter A parameter required by some functions (eg, Date Variance).
     * @return This method returns a Date after the application of the parameter function.
     */
    public Date generateDuplicate(Date date, Function function, String extraParameter) {
        if (date == null || function == Function.SET_TO_NULL) {
            return null;
        }
        Date newDate = new Date(date.getTime());
        switch (function) {
        case DATE_VARIANCE:
            Integer extraParam;
            try {
                extraParam = Integer.parseInt(extraParameter);
            } catch (NumberFormatException e) {
                extraParam = 31;
            }
            if (extraParam <= 0) {
                extraParam *= -1;
            }
            dateChanger.dateVariance(newDate, extraParam);
            return newDate;
        default:
            break;
        }
        return date;
    }

    /**
     * Method "generateDuplicate". This method is called when the input is a String.
     * 
     * @param str The input sent to the function.
     * @param function The function used on the str parameter (see the enum Function).
     * @param extraParameter A parameter required by some functions.
     * @return This method returns a String after the application of the parameter function.
     */

    public String generateDuplicate(String str, Function function, String extraParameter) {
        if (function == Function.SET_TO_NULL) {
            return null;
        }
        StringBuilder sb = new StringBuilder(""); //$NON-NLS-1$
        switch (function) {
        case GENERATE_CREDIT_CARD:
            CreditCardGenerator ccg = new CreditCardGenerator(rnd);
            CreditCardType cct = ccg.chooseCreditCardType();
            sb = new StringBuilder(ccg.generateCreditCard(cct).toString());
            break;
        case GENERATE_CREDIT_CARD_FORMAT:
            CreditCardGenerator ccgf = new CreditCardGenerator(rnd);
            CreditCardType cct_format = null;
            if (str == null) {
                cct_format = ccgf.chooseCreditCardType();
                sb = new StringBuilder(ccgf.generateCreditCard(cct_format).toString());
                break;
            } else if (str != null) {
                try {
                    cct_format = ccgf.getCreditCardType(Long.parseLong(str.replaceAll("\\s+", ""))); //$NON-NLS-1$ //$NON-NLS-2$
                } catch (NumberFormatException e) {
                    cct_format = ccgf.chooseCreditCardType();
                    sb = new StringBuilder(ccgf.generateCreditCard(cct_format).toString());
                    break;
                }
                if (cct_format != null) {
                    sb = new StringBuilder(ccgf
                            .generateCreditCardFormat(cct_format, Long.parseLong(str.replaceAll("\\s+", ""))).toString()); //$NON-NLS-1$ //$NON-NLS-2$
                    break;
                } else {
                    cct_format = ccgf.chooseCreditCardType();
                    sb = new StringBuilder(ccgf.generateCreditCard(cct_format).toString());
                }
            }
            break;
        case GENERATE_ACCOUNT_NUMBER:
            AccountNumberGenerator ang = new AccountNumberGenerator(rnd);
            String accountNumber = ang.generateIban();
            sb = new StringBuilder(accountNumber);
            break;
        case GENERATE_ACCOUNT_NUMBER_FORMAT:
            AccountNumberGenerator angf = new AccountNumberGenerator(rnd);
            String accountNumberFormat = ""; //$NON-NLS-1$
            if (str != null) {
                try {
                    accountNumberFormat = angf.generateIban(str);
                } catch (NumberFormatException e) {
                    accountNumberFormat = angf.generateIban();
                }
            } else {
                accountNumberFormat = angf.generateIban();
            }
            sb = new StringBuilder(accountNumberFormat);
            break;
        case GENERATE_PHONE_NUMBER:
            PhoneNumberGenerator png = new PhoneNumberGenerator(rnd);
            String phoneNumber = png.generatePhoneNumber();
            sb = new StringBuilder(phoneNumber);
            break;
        case GENERATE_PHONE_NUMBER_FORMAT:
            PhoneNumberGenerator pngf = new PhoneNumberGenerator(rnd);
            String phoneNumberFormat = ""; //$NON-NLS-1$
            int extraParam = 0;
            try {
                extraParam = Integer.parseInt(extraParameter);
            } catch (NumberFormatException e) {
                phoneNumberFormat = pngf.generatePhoneNumber();
                sb = new StringBuilder(phoneNumberFormat);
            }
            if (str == null || extraParam <= 0
                    || !(str.replaceAll("\\s+", "").matches("^(\\+)?[0-9]+(-[0-9]+)+$|^(\\+)?[0-9]+$")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    || extraParam >= str.length()) {
                phoneNumberFormat = pngf.generatePhoneNumber();
            } else {
                phoneNumberFormat = pngf.generatePhoneNumber(str, extraParam);
            }
            sb = new StringBuilder(phoneNumberFormat);
            break;
        default:
            break;
        }
        return sb.toString();
    }

    /**
     * Method "generateDuplicate". This method is called when the input is a Double.
     * 
     * @param valueIn The input sent to the function.
     * @param function The function used on the date parameter (see the enum Function).
     * @param extraParameter A parameter required by some functions (eg, Numeric Variance).
     * @return This method returns a Double after the application of the parameter function.
     */

    public Double generateDuplicate(Double valueIn, Function function, String extraParameter) {
        if (valueIn == null || function == Function.SET_TO_NULL) {
            return null;
        }
        Double finalValue = null;
        switch (function) {
        case NUMERIC_VARIANCE:
            Integer extraParam;
            try {
                extraParam = Integer.parseInt(extraParameter);
            } catch (NumberFormatException e) {
                extraParam = 10;
            }
            if (extraParam <= 0) {
                extraParam *= -1;
            }
            int rate = 0;
            do {
                rate = rnd.nextInt(2 * extraParam) - extraParam;
            } while (rate == 0);
            Float value = Float.parseFloat(valueIn.toString());
            value *= ((float) rate + 100) / 100;
            finalValue = new Double(value);
            break;
        default:
            return valueIn;
        }
        return finalValue;
    }

    /**
     * Method "generateDuplicate". This method is called when the input is a Float.
     * 
     * @param valueIn The input sent to the function.
     * @param function The function used on the date parameter (see the enum Function).
     * @param extraParameter A parameter required by some functions (eg, Numeric Variance).
     * @return This method returns a Float after the application of the parameter function.
     */

    public Float generateDuplicate(Float valueIn, Function function, String extraParameter) {
        if (valueIn == null || function == Function.SET_TO_NULL) {
            return null;
        }
        Float finalValue = null;
        switch (function) {
        case NUMERIC_VARIANCE:
            Integer extraParam;
            try {
                extraParam = Integer.parseInt(extraParameter);
            } catch (NumberFormatException e) {
                extraParam = 10;
            }
            if (extraParam <= 0) {
                extraParam *= -1;
            }
            int rate = 0;
            do {
                rate = rnd.nextInt(2 * extraParam) - extraParam;
            } while (rate == 0);
            Float value = Float.parseFloat(valueIn.toString());
            value *= ((float) rate + 100) / 100;
            finalValue = new Float(value);
            break;
        default:
            return valueIn;
        }
        return finalValue;
    }

    /**
     * Method "generateDuplicate". This method is called when the input is a Long.
     * 
     * @param valueIn The input sent to the function.
     * @param function The function used on the date parameter (see the enum Function).
     * @param extraParameter A parameter required by some functions (eg, Numeric Variance).
     * @return This method returns a Long after the application of the parameter function.
     */

    public Long generateDuplicate(Long valueIn, Function function, String extraParameter) {
        if (function == Function.SET_TO_NULL) {
            return null;
        }
        Long finalValue = null;
        switch (function) {
        case NUMERIC_VARIANCE:
            if (valueIn == null) {
                return null;
            }
            Integer extraParam;
            try {
                extraParam = Integer.parseInt(extraParameter);
            } catch (NumberFormatException e) {
                extraParam = 10;
            }
            if (extraParam <= 0) {
                extraParam *= -1;
            }
            int rate = 0;
            do {
                rate = rnd.nextInt(2 * extraParam) - extraParam;
            } while (rate == 0);
            Float value = Float.parseFloat(valueIn.toString());
            value *= ((float) rate + 100) / 100;
            finalValue = new Long(Math.round(value));
            break;
        case GENERATE_CREDIT_CARD:
            CreditCardGenerator ccg = new CreditCardGenerator(rnd);
            CreditCardType cct = ccg.chooseCreditCardType();
            finalValue = ccg.generateCreditCard(cct);
            break;
        case GENERATE_CREDIT_CARD_FORMAT:
            CreditCardGenerator ccgf = new CreditCardGenerator(rnd);
            CreditCardType cct_format = null;
            if (valueIn == null) {
                cct_format = ccgf.chooseCreditCardType();
                finalValue = ccgf.generateCreditCard(cct_format);
                break;
            } else {
                cct_format = ccgf.getCreditCardType(valueIn);
                if (cct_format == null) {
                    cct_format = ccgf.chooseCreditCardType();
                    finalValue = ccgf.generateCreditCard(cct_format);
                    break;
                }
                finalValue = ccgf.generateCreditCardFormat(cct_format, valueIn);
            }
            break;
        default:
            return valueIn;
        }
        return finalValue;
    }

    /**
     * Method "generateDuplicate". This method is called when the input is a Integer.
     * 
     * @param valueIn The input sent to the function.
     * @param function The function used on the date parameter (see the enum Function).
     * @param extraParameter A parameter required by some functions (eg, Numeric Variance).
     * @return This method returns a Integer after the application of the parameter function.
     */

    public Integer generateDuplicate(Integer valueIn, Function function, String extraParameter) {
        if (valueIn == null || function == Function.SET_TO_NULL) {
            return null;
        }
        Integer finalValue = null;
        switch (function) {
        case NUMERIC_VARIANCE:
            Integer extraParam;
            try {
                extraParam = Integer.parseInt(extraParameter);
            } catch (NumberFormatException e) {
                extraParam = 10;
            }
            if (extraParam <= 0) {
                extraParam *= -1;
            }
            int rate = 0;
            do {
                rate = rnd.nextInt(2 * extraParam) - extraParam;
            } while (rate == 0);
            Float value = Float.parseFloat(valueIn.toString());
            value *= ((float) rate + 100) / 100;
            finalValue = new Integer(Math.round(value));
            break;
        default:
            return valueIn;
        }
        return finalValue;
    }
}