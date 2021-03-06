package org.talend.dataquality.datamasking;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

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
        KEEP_YEAR,
        NUMERIC_VARIANCE,
        GENERATE_CREDIT_CARD,
        GENERATE_CREDIT_CARD_FORMAT,
        GENERATE_ACCOUNT_NUMBER,
        GENERATE_ACCOUNT_NUMBER_FORMAT,
        GENERATE_PHONE_NUMBER,
        GENERATE_BETWEEN,
        GENERATE_FROM_LIST,
        GENERATE_FROM_FILE,
        HASH_GENERATE_LIST,
        HASH_GENERATE_FILE,
        REPLACE_ALL,
        REPLACE_NUMERIC,
        REPLACE_CHARACTERS,
        REPLACE_SSN,
        REPLACE_BETWEEN_INDEXES,
        KEEP_BETWEEN_INDEXES,
        REMOVE_BETWEEN_INDEXES,
        REMOVE_FIRST_CHARS,
        REMOVE_LAST_CHARS,
        REPLACE_FIRST_CHARS,
        REPLACE_LAST_CHARS,
        GENERATE_UUID
    };

    private DateChanger dateChanger = new DateChanger();

    private String EMPTY_STRING = ""; //$NON-NLS-1$

    private String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"; //$NON-NLS-1$

    private String LOWER = "abcdefghijklmnopqrstuvwxyz"; //$NON-NLS-1$

    private RandomWrapper rnd = new RandomWrapper();

    private boolean keepNull = false;

    public void setSeed(long seed) {
        dateChanger.setSeed(seed);
        rnd = new RandomWrapper(seed);
    }

    public void init(boolean keep_null) {
        keepNull = keep_null;
    }

    /**
     * Method "generateMaskedRow". This method is called when the input is a Date.
     * 
     * @param date The input sent to the function.
     * @param function The function used on the date parameter (see the enum Function).
     * @param extraParameter A parameter required by some functions (eg, Date Variance).
     * @return This method returns a Date after the application of the parameter function.
     */
    public Date generateMaskedRow(Date date, Function function, String extraParameter) {
        if (function == Function.SET_TO_NULL || date == null && keepNull) {
            return null;
        }
        Date newDate = new Date(System.currentTimeMillis());
        switch (function) {
        case DATE_VARIANCE:
            if (date != null) {
                Integer extraParam;
                try {
                    extraParam = Integer.parseInt(extraParameter);
                } catch (NumberFormatException e) {
                    extraParam = 31;
                }
                if (extraParam < 0) {
                    extraParam *= -1;
                } else if (extraParam == 0) {
                    extraParam = 31;
                }
                newDate = dateChanger.dateVariance(date, extraParam);
            }
            break;
        case GENERATE_BETWEEN:
            String[] parameters = extraParameter.split(","); //$NON-NLS-1$
            if (parameters.length != 2) {
                return new Date(System.currentTimeMillis());
            } else {
                newDate = dateChanger.generateDateBetween(parameters[0], parameters[1], rnd);
            }
            break;
        case KEEP_YEAR:
            Calendar c = Calendar.getInstance();
            if (date != null) {
                c.setTime(date);
            } else {
                c.setTime(newDate);
            }
            c.set(Calendar.DAY_OF_MONTH, 1);
            c.set(Calendar.MONTH, Calendar.JANUARY);
            newDate = c.getTime();
            break;
        default:
            return new Date(System.currentTimeMillis());
        }
        return newDate;
    }

    /**
     * Method "generateMaskedRow". This method is called when the input is a String.
     * 
     * @param str The input sent to the function.
     * @param function The function used on the str parameter (see the enum Function).
     * @param extraParameter A parameter required by some functions.
     * @return This method returns a String after the application of the parameter function.
     */

    public String generateMaskedRow(String str, Function function, String extraParameter) {
        if (function == Function.SET_TO_NULL || str == null && keepNull) {
            return null;
        }
        StringBuilder sb = new StringBuilder(EMPTY_STRING);
        switch (function) {
        case GENERATE_CREDIT_CARD:
            CreditCardGenerator ccg = new CreditCardGenerator(rnd);
            CreditCardType cct = ccg.chooseCreditCardType();
            sb = new StringBuilder(ccg.generateCreditCard(cct).toString());
            break;
        case GENERATE_CREDIT_CARD_FORMAT:
            boolean keep_format = ("true").equals(extraParameter); //$NON-NLS-1$ 
            CreditCardGenerator ccgf = new CreditCardGenerator(rnd);
            CreditCardType cct_format = null;
            if (str == null) {
                cct_format = ccgf.chooseCreditCardType();
                sb = new StringBuilder(ccgf.generateCreditCard(cct_format).toString());
            } else {
                try {
                    cct_format = ccgf.getCreditCardType(Long.parseLong(str.replaceAll("\\s+", EMPTY_STRING))); //$NON-NLS-1$ 
                } catch (NumberFormatException e) {
                    cct_format = ccgf.chooseCreditCardType();
                    sb = new StringBuilder(ccgf.generateCreditCard(cct_format).toString());
                    break;
                }
                if (cct_format != null) {
                    sb = new StringBuilder(ccgf.generateCreditCardFormat(cct_format, str, keep_format));
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
            boolean keepFormat = ("true").equals(extraParameter); //$NON-NLS-1$
            AccountNumberGenerator angf = new AccountNumberGenerator(rnd);
            String accountNumberFormat = EMPTY_STRING;
            if (str != null && str.length() > 9) {
                try {
                    accountNumberFormat = angf.generateIban(str, keepFormat);
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
        case REPLACE_ALL:
            if (str == null || extraParameter == null || !extraParameter.matches("[0-9]|[a-zA-Z]")) { //$NON-NLS-1$
                sb = new StringBuilder(EMPTY_STRING);
            } else {
                sb = new StringBuilder(str.replaceAll(".", extraParameter)); //$NON-NLS-1$
            }
            break;
        case REPLACE_NUMERIC:
            if (str == null || extraParameter == null || !extraParameter.matches("[0-9]|[a-zA-Z]| ")) { //$NON-NLS-1$
                sb = new StringBuilder(EMPTY_STRING);
            } else {
                if (extraParameter.equals(" ")) { //$NON-NLS-1$
                    sb = new StringBuilder(str.replaceAll("\\d", extraParameter).replaceAll(" ", EMPTY_STRING)); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    sb = new StringBuilder(str.replaceAll("\\d", extraParameter)); //$NON-NLS-1$
                }
            }
            break;
        case REPLACE_CHARACTERS:
            if (str == null || extraParameter == null || !extraParameter.matches("[0-9]|[a-zA-Z]| ")) { //$NON-NLS-1$
                sb = new StringBuilder(EMPTY_STRING);
            } else {
                if (extraParameter.equals(" ")) { //$NON-NLS-1$
                    sb = new StringBuilder(str.replaceAll("[a-zA-Z]", extraParameter).replaceAll(" ", EMPTY_STRING)); //$NON-NLS-1$ //$NON-NLS-2$   
                } else {
                    sb = new StringBuilder(str.replaceAll("[a-zA-Z]", extraParameter)); //$NON-NLS-1$
                }
            }
            break;
        case REPLACE_SSN:
            if (str == null || extraParameter == null || !extraParameter.matches("[0-9]|[a-zA-Z]")) { //$NON-NLS-1$
                sb = new StringBuilder(EMPTY_STRING);
            } else {
                int digits_to_keep = 0;
                String str_nospaces = str.replaceAll("\\s+", EMPTY_STRING); //$NON-NLS-1$
                if (str_nospaces.replaceAll("\\D", EMPTY_STRING).length() == 9) {//$NON-NLS-1$
                    digits_to_keep = 4;
                } else if (str_nospaces.replaceAll("\\D", EMPTY_STRING).length() == 15) { //$NON-NLS-1$
                    digits_to_keep = 5;
                }
                String res = str_nospaces.substring(0, str_nospaces.length() - digits_to_keep)
                        .replaceAll("[0-9]", extraParameter); //$NON-NLS-1$ 
                res = res + str_nospaces.substring(str_nospaces.length() - digits_to_keep, str_nospaces.length());
                sb = new StringBuilder(res);
            }
            break;
        case GENERATE_BETWEEN:
            String[] parameters = extraParameter.split(","); //$NON-NLS-1$
            if (parameters.length != 2) {
                sb = new StringBuilder(EMPTY_STRING);
            } else {
                int a = 0;
                int b = 0;
                try {
                    a = Integer.valueOf(parameters[0].trim());
                    b = Integer.valueOf(parameters[1].trim());
                } catch (NumberFormatException e) {
                    sb = new StringBuilder(EMPTY_STRING);
                    break;
                }
                int min = (a < b) ? a : b;
                int max = (a < b) ? b : a;
                sb = new StringBuilder(String.valueOf(rnd.nextInt((max - min) + 1) + min));
            }
            break;
        case GENERATE_FROM_LIST:
            String[] parameterss = extraParameter.split(","); //$NON-NLS-1$
            if (parameterss.length > 0) {
                for (int i = 0; i < parameterss.length; ++i) {
                    String tmp = parameterss[i].trim();
                    parameterss[i] = tmp;
                }
                sb = new StringBuilder(parameterss[rnd.nextInt(parameterss.length)]);
            } else {
                sb = new StringBuilder(EMPTY_STRING);
            }
            break;
        case GENERATE_FROM_FILE:
            try {
                @SuppressWarnings("resource")
                Scanner in = new Scanner(new FileReader(extraParameter));
                List<String> tokens = new ArrayList<String>();
                while (in.hasNext()) {
                    tokens.add(in.next());
                }
                sb = new StringBuilder(tokens.get(rnd.nextInt(tokens.size())));
            } catch (FileNotFoundException e) {
                sb = new StringBuilder(EMPTY_STRING);
            }
            break;
        case HASH_GENERATE_LIST:
            String[] parametersss = extraParameter.split(","); //$NON-NLS-1$
            if (parametersss.length > 0) {
                for (int i = 0; i < parametersss.length; ++i) {
                    String tmp = parametersss[i].trim();
                    parametersss[i] = tmp;
                }
                if (str == null) {
                    sb = new StringBuilder(parametersss[rnd.nextInt(parametersss.length)]);
                } else {
                    sb = new StringBuilder(parametersss[(Math.abs(str.hashCode()) % parametersss.length)]);
                }
            } else {
                sb = new StringBuilder(EMPTY_STRING);
            }
            break;
        case HASH_GENERATE_FILE:
            try {
                @SuppressWarnings("resource")
                Scanner in = new Scanner(new FileReader(extraParameter));
                List<String> tokens = new ArrayList<String>();
                while (in.hasNext()) {
                    tokens.add(in.next());
                }
                if (str == null) {
                    sb = new StringBuilder(tokens.get(rnd.nextInt(tokens.size())));
                } else {
                    sb = new StringBuilder(tokens.get(Math.abs(str.hashCode()) % tokens.size()));
                }
            } catch (FileNotFoundException e) {
                sb = new StringBuilder(EMPTY_STRING);
            }
            break;
        case KEEP_BETWEEN_INDEXES:
            String[] indexes = extraParameter.split(","); //$NON-NLS-1$
            if (str == null || indexes.length != 2) {
                sb = new StringBuilder(EMPTY_STRING);
            } else {
                int a = 0, b = 0;
                try {
                    a = Integer.valueOf(indexes[0].trim());
                    b = Integer.valueOf(indexes[1].trim());
                } catch (NumberFormatException e) {
                    sb = new StringBuilder(EMPTY_STRING);
                    break;
                }
                int begin = (a < b) ? a : b;
                int end = (a > b) ? a : b;
                begin = (begin < 1) ? 1 : begin;
                end = (end > str.length()) ? str.length() : end;
                sb = new StringBuilder(str.substring(begin - 1, end));
            }
            break;
        case REMOVE_BETWEEN_INDEXES:
            String[] indexess = extraParameter.split(","); //$NON-NLS-1$
            if (str == null || indexess.length != 2) {
                sb = new StringBuilder(EMPTY_STRING);
            } else {
                int a = 0, b = 0;
                try {
                    a = Integer.valueOf(indexess[0].trim());
                    b = Integer.valueOf(indexess[1].trim());
                } catch (NumberFormatException e) {
                    sb = new StringBuilder(EMPTY_STRING);
                    break;
                }
                int begin = (a < b) ? a : b;
                int end = (a > b) ? a : b;
                begin = (begin < 1) ? 1 : begin;
                end = (end > str.length()) ? str.length() : end;
                sb = new StringBuilder(str.substring(0, begin - 1) + str.substring(end, str.length()));
            }
            break;
        case REPLACE_BETWEEN_INDEXES:
            String[] indexesss = extraParameter.split(","); //$NON-NLS-1$
            if (str == null || indexesss.length < 2 || indexesss.length > 3) {
                sb = new StringBuilder(EMPTY_STRING);
            } else {
                int a = 0, b = 0;
                try {
                    a = Integer.valueOf(indexesss[0].trim());
                    b = Integer.valueOf(indexesss[1].trim());
                } catch (NumberFormatException e) {
                    sb = new StringBuilder(EMPTY_STRING);
                }
                int begin = (a < b) ? a : b;
                int end = (a > b) ? a : b;
                String s = null;
                boolean isThird = true;
                try {
                    s = indexesss[2].trim();
                    if (!s.matches("[0-9]|[a-zA-Z]")) { //$NON-NLS-1$                                                                                                               
                        isThird = false;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    isThird = false;
                }
                begin = (begin < 1) ? 1 : begin;
                end = (end > str.length()) ? str.length() : end;
                sb = new StringBuilder(str);
                if (!isThird) {
                    for (int i = begin - 1; i < end; ++i) {
                        if (Character.isDigit(str.charAt(i))) {
                            sb.setCharAt(i, Character.forDigit(rnd.nextInt(9), 10));
                        } else if (Character.isUpperCase(str.charAt(i))) {
                            sb.setCharAt(i, UPPER.charAt(rnd.nextInt(26)));
                        } else if (Character.isLowerCase(str.charAt(i))) {
                            sb.setCharAt(i, LOWER.charAt(rnd.nextInt(26)));
                        } else {
                            sb.setCharAt(i, str.charAt(i));
                        }
                    }
                } else {
                    @SuppressWarnings("null")
                    char c = s.toCharArray()[0];
                    for (int i = begin - 1; i < end; ++i) {
                        sb.setCharAt(i, c);
                    }
                }

            }
            break;
        case REMOVE_FIRST_CHARS:
            Integer extra = null;
            try {
                extra = Integer.parseInt(extraParameter);
            } catch (NumberFormatException e) {
                sb = new StringBuilder(EMPTY_STRING);
                break;
            }
            if (str == null || extra < 0) {
                sb = new StringBuilder(EMPTY_STRING);
                break;
            }
            extra = (extra > str.length()) ? str.length() : extra;
            sb = new StringBuilder(str.substring(extra));
            break;
        case REMOVE_LAST_CHARS:
            Integer extraP = null;
            try {
                extraP = Integer.parseInt(extraParameter);
            } catch (NumberFormatException e) {
                sb = new StringBuilder(EMPTY_STRING);
                break;
            }
            if (str == null || extraP < 0) {
                sb = new StringBuilder(EMPTY_STRING);
                break;
            }
            extraP = (extraP > str.length()) ? str.length() : extraP;
            sb = new StringBuilder(str.substring(0, str.length() - extraP));
            break;
        case REPLACE_FIRST_CHARS:
            Integer extraPa = null;
            try {
                extraPa = Integer.parseInt(extraParameter);
            } catch (NumberFormatException e) {
                sb = new StringBuilder(EMPTY_STRING);
                break;
            }
            if (str == null || extraPa < 0) {
                sb = new StringBuilder(EMPTY_STRING);
                break;
            }
            extraPa = (extraPa > str.length()) ? str.length() : extraPa;
            sb = new StringBuilder(str);
            StringBuilder repl = new StringBuilder(EMPTY_STRING);
            for (int i = 0; i < extraPa; ++i) {
                if (Character.isDigit(str.charAt(i))) {
                    repl.append(rnd.nextInt(9));
                } else if (Character.isUpperCase(str.charAt(i))) {
                    repl.append(UPPER.charAt(rnd.nextInt(26)));
                } else if (Character.isLowerCase(str.charAt(i))) {
                    repl.append(LOWER.charAt(rnd.nextInt(26)));
                } else {
                    repl.append(str.charAt(i));
                }
            }
            sb.replace(0, extraPa, repl.toString());
            break;
        case REPLACE_LAST_CHARS:
            Integer extraPar = null;
            try {
                extraPar = Integer.parseInt(extraParameter);
            } catch (NumberFormatException e) {
                sb = new StringBuilder(EMPTY_STRING);
                break;
            }
            if (str == null || extraPar < 0) {
                sb = new StringBuilder(EMPTY_STRING);
                break;
            }
            extraPar = (extraPar > str.length()) ? str.length() : extraPar;
            sb = new StringBuilder(str);
            StringBuilder repla = new StringBuilder(EMPTY_STRING);
            for (int i = sb.length() - extraPar; i < sb.length(); ++i) {
                if (Character.isDigit(str.charAt(i))) {
                    repla.append(rnd.nextInt(9));
                } else if (Character.isUpperCase(str.charAt(i))) {
                    repla.append(UPPER.charAt(rnd.nextInt(26)));
                } else if (Character.isLowerCase(str.charAt(i))) {
                    repla.append(LOWER.charAt(rnd.nextInt(26)));
                } else {
                    repla.append(str.charAt(i));
                }
            }
            sb.replace(str.length() - extraPar, str.length(), repla.toString());
            break;
        case GENERATE_UUID:
            sb = new StringBuilder(UUID.randomUUID().toString());
            break;
        default:
            return sb.toString();
        }
        return sb.toString();
    }

    /**
     * Method "generateMaskedRow". This method is called when the input is a Double.
     * 
     * @param valueIn The input sent to the function.
     * @param function The function used on the date parameter (see the enum Function).
     * @param extraParameter A parameter required by some functions (eg, Numeric Variance).
     * @return This method returns a Double after the application of the parameter function.
     */

    public Double generateMaskedRow(Double valueIn, Function function, String extraParameter) {
        if (function == Function.SET_TO_NULL || valueIn == null && keepNull) {
            return null;
        }
        Double finalValue = 0.0;
        Integer extraParam = null;
        switch (function) {
        case NUMERIC_VARIANCE:
            if (valueIn != null) {
                try {
                    extraParam = Integer.parseInt(extraParameter);
                } catch (NumberFormatException e) {
                    extraParam = 10;
                }
                if (extraParam <= 0) {
                    extraParam *= -1;
                } else if (extraParam == 0) {
                    extraParam = 10;
                }
                int rate = 0;
                do {
                    rate = rnd.nextInt(2 * extraParam) - extraParam;
                } while (rate == 0);
                Float value = Float.parseFloat(valueIn.toString());
                value *= ((float) rate + 100) / 100;
                finalValue = new Double(value);
            }
            break;
        case REPLACE_NUMERIC:
            if (valueIn != null) {
                try {
                    extraParam = Integer.parseInt(extraParameter);
                } catch (NumberFormatException e) {
                    extraParam = 0;
                }
                if (extraParam < 0 || extraParam > 9) {
                    extraParam = 0;
                }
                String str = valueIn.toString();
                String res = str.replaceAll("\\d", extraParam.toString()); //$NON-NLS-1$
                finalValue = Double.valueOf(res);
            }
            break;
        case GENERATE_BETWEEN:
            String[] parameters = extraParameter.split(","); //$NON-NLS-1$
            if (parameters.length != 2) {
                break;
            } else {
                int a = 0;
                int b = 0;
                try {
                    a = Integer.valueOf(parameters[0].trim());
                    b = Integer.valueOf(parameters[1].trim());
                } catch (NumberFormatException e) {
                    finalValue = 0.0;
                    break;
                }
                int min = (a < b) ? a : b;
                int max = (a < b) ? b : a;
                finalValue = (double) rnd.nextInt((max - min) + 1) + min;
            }
            break;
        default:
            return finalValue;
        }
        return finalValue;
    }

    /**
     * Method "generateMaskedRow". This method is called when the input is a Float.
     * 
     * @param valueIn The input sent to the function.
     * @param function The function used on the date parameter (see the enum Function).
     * @param extraParameter A parameter required by some functions (eg, Numeric Variance).
     * @return This method returns a Float after the application of the parameter function.
     */

    public Float generateMaskedRow(Float valueIn, Function function, String extraParameter) {
        if (function == Function.SET_TO_NULL || valueIn == null && keepNull) {
            return null;
        }
        Float finalValue = 0.0f;
        Integer extraParam = null;
        switch (function) {
        case NUMERIC_VARIANCE:
            if (valueIn != null) {
                try {
                    extraParam = Integer.parseInt(extraParameter);
                } catch (NumberFormatException e) {
                    extraParam = 10;
                }
                if (extraParam < 0) {
                    extraParam *= -1;
                } else if (extraParam == 0) {
                    extraParam = 10;
                }
                int rate = 0;
                do {
                    rate = rnd.nextInt(2 * extraParam) - extraParam;
                } while (rate == 0);
                Float value = Float.parseFloat(valueIn.toString());
                value *= ((float) rate + 100) / 100;
                finalValue = new Float(value);
            }
            break;
        case REPLACE_NUMERIC:
            if (valueIn != null) {
                try {
                    extraParam = Integer.parseInt(extraParameter);
                } catch (NumberFormatException e) {
                    extraParam = 0;
                }
                if (extraParam < 0 || extraParam > 9) {
                    extraParam = 0;
                }
                String str = valueIn.toString();
                String res = str.replaceAll("\\d", extraParam.toString()); //$NON-NLS-1$
                finalValue = Float.valueOf(res);
            }
            break;
        case GENERATE_BETWEEN:
            String[] parameters = extraParameter.split(","); //$NON-NLS-1$
            if (parameters.length != 2) {
                finalValue = 0f;
            } else {
                int a = 0;
                int b = 0;
                try {
                    a = Integer.valueOf(parameters[0].trim());
                    b = Integer.valueOf(parameters[1].trim());
                } catch (NumberFormatException e) {
                    finalValue = 0f;
                    break;
                }
                int min = (a < b) ? a : b;
                int max = (a < b) ? b : a;
                finalValue = (float) rnd.nextInt((max - min) + 1) + min;
            }
            break;
        default:
            return finalValue;
        }
        return finalValue;
    }

    /**
     * Method "generateMaskedRow". This method is called when the input is a Long.
     * 
     * @param valueIn The input sent to the function.
     * @param function The function used on the date parameter (see the enum Function).
     * @param extraParameter A parameter required by some functions (eg, Numeric Variance).
     * @return This method returns a Long after the application of the parameter function.
     */

    public Long generateMaskedRow(Long valueIn, Function function, String extraParameter) {
        if (function == Function.SET_TO_NULL || valueIn == null && keepNull) {
            return null;
        }
        Long finalValue = null;
        switch (function) {
        case NUMERIC_VARIANCE:
            if (valueIn == null) {
                return 0L;
            }
            Integer extraParam;
            try {
                extraParam = Integer.parseInt(extraParameter);
            } catch (NumberFormatException e) {
                extraParam = 10;
            }
            if (extraParam < 0) {
                extraParam *= -1;
            } else if (extraParam == 0) {
                extraParam = 10;
            }
            int rate = 0;
            do {
                rate = rnd.nextInt(2 * extraParam) - extraParam;
            } while (rate == 0);
            Double value = Double.parseDouble(valueIn.toString());
            value *= ((double) rate + 100) / 100;
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
        case REPLACE_NUMERIC:
            if (valueIn == null || !extraParameter.matches("[0-9]")) { //$NON-NLS-1$
                return 0L;
            }
            try {
                extraParam = Integer.parseInt(extraParameter);
            } catch (NumberFormatException e) {
                extraParam = 0;
            }
            if (extraParam < 0 || extraParam > 9) {
                extraParam = 0;
            }
            String str = valueIn.toString();
            String res = str.replaceAll("\\d", extraParam.toString()); //$NON-NLS-1$
            finalValue = Long.valueOf(res);
            break;
        case GENERATE_BETWEEN:
            String[] parameters = extraParameter.split(","); //$NON-NLS-1$
            if (parameters.length != 2) {
                finalValue = 0L;
            } else {
                int a = 0;
                int b = 0;
                try {
                    a = Integer.valueOf(parameters[0].trim());
                    b = Integer.valueOf(parameters[1].trim());
                } catch (NumberFormatException e) {
                    finalValue = 0L;
                    break;
                }
                int min = (a < b) ? a : b;
                int max = (a < b) ? b : a;
                finalValue = (long) rnd.nextInt((max - min) + 1) + min;
            }
            break;
        case REPLACE_SSN:
            if (valueIn == null || extraParameter == null || !extraParameter.matches("[0-9]")) { //$NON-NLS-1$
                finalValue = 0L;
            } else {
                int digits_to_keep = 0;
                String strI = valueIn.toString();
                if ((int) Math.log10(valueIn) + 1 == 9) {
                    digits_to_keep = 4;
                } else if ((int) Math.log10(valueIn) + 1 == 15) {
                    digits_to_keep = 5;
                }
                String res_ssn = strI.substring(0, strI.length() - digits_to_keep).replaceAll("[0-9]", extraParameter); //$NON-NLS-1$ 
                res_ssn = res_ssn + strI.substring(strI.length() - digits_to_keep, strI.length());
                finalValue = Long.parseLong(res_ssn);
            }
            break;
        case GENERATE_FROM_LIST:
            String[] parameterss = extraParameter.split(","); //$NON-NLS-1$
            long[] parametersI = new long[parameterss.length];
            if (parameterss.length > 0) {
                for (int i = 0; i < parameterss.length; ++i) {
                    String tmp = parameterss[i].replaceAll("\\s+", EMPTY_STRING); //$NON-NLS-1$
                    try {
                        parametersI[i] = Long.parseLong(tmp);
                    } catch (NumberFormatException e) {
                        finalValue = 0L;
                        break;
                    }
                }
                finalValue = parametersI[rnd.nextInt(parametersI.length)];
            } else {
                finalValue = 0L;
            }
            break;
        case GENERATE_FROM_FILE:
            try {
                @SuppressWarnings("resource")
                Scanner in = new Scanner(new FileReader(extraParameter));
                List<Long> tokens = new ArrayList<Long>();
                while (in.hasNext()) {
                    try {
                        tokens.add(Long.parseLong(in.next()));
                    } catch (NumberFormatException e) {
                        finalValue = 0L;
                        break;
                    }
                }
                finalValue = tokens.get(rnd.nextInt(tokens.size()));
            } catch (FileNotFoundException e) {
                finalValue = 0L;
            }
            break;
        case HASH_GENERATE_LIST:
            String[] parametersh = extraParameter.split(","); //$NON-NLS-1$
            int[] parametersIh = new int[parametersh.length];
            if (parametersIh.length > 0) {
                for (int i = 0; i < parametersh.length; ++i) {
                    String tmp = parametersh[i].replaceAll("\\s+", EMPTY_STRING); //$NON-NLS-1$
                    try {
                        parametersIh[i] = Integer.parseInt(tmp);
                    } catch (NumberFormatException e) {
                        finalValue = 0L;
                        break;
                    }
                }
                if (valueIn == null) {
                    finalValue = (long) parametersIh[rnd.nextInt(parametersIh.length)];
                } else {
                    finalValue = (long) parametersIh[Math.abs(valueIn.hashCode()) % parametersIh.length];
                }
            } else {
                finalValue = 0L;
            }
            break;
        case HASH_GENERATE_FILE:
            try {
                @SuppressWarnings("resource")
                Scanner in = new Scanner(new FileReader(extraParameter));
                List<Integer> tokens = new ArrayList<Integer>();
                while (in.hasNext()) {
                    try {
                        tokens.add(Integer.parseInt(in.next()));
                    } catch (NumberFormatException e) {
                        finalValue = 0L;
                        break;
                    }
                }
                if (valueIn == null) {
                    finalValue = (long) tokens.get(rnd.nextInt(tokens.size()));
                } else {
                    finalValue = (long) tokens.get(Math.abs(valueIn.hashCode()) % tokens.size());
                }
            } catch (FileNotFoundException e) {
                finalValue = 0L;
            }
            break;
        case REMOVE_FIRST_CHARS:
            Integer extra = null;
            try {
                extra = Integer.parseInt(extraParameter);
            } catch (NumberFormatException e) {
                finalValue = 0L;
                break;
            }
            if (valueIn == null || (int) Math.log10(valueIn) + 1 <= extra || extra < 0) {
                finalValue = 0L;
                break;
            }
            StringBuilder sb = new StringBuilder(valueIn.toString().substring(extra));
            finalValue = Long.parseLong(sb.toString());
            break;
        case REMOVE_LAST_CHARS:
            Double extraP = null;
            try {
                extraP = Double.parseDouble(extraParameter);
            } catch (NumberFormatException e) {
                finalValue = 0L;
                break;
            }
            if (valueIn == null || (int) Math.log10(valueIn) + 1 <= extraP || extraP < 0) {
                finalValue = 0L;
                break;
            }
            finalValue = valueIn / (long) Math.pow(10.0, extraP);
            break;
        case REPLACE_FIRST_CHARS:
            Integer extraPa = null;
            try {
                extraPa = Integer.parseInt(extraParameter);
            } catch (NumberFormatException e) {
                finalValue = 0L;
                break;
            }
            if (valueIn == null || extraPa < 0) {
                finalValue = 0L;
                break;
            }
            extraPa = ((int) Math.log10(valueIn) + 1 <= extraPa) ? (int) Math.log10(valueIn) + 1 : extraPa;
            StringBuilder sbu = new StringBuilder(valueIn.toString());
            StringBuilder remp = new StringBuilder(EMPTY_STRING);
            for (int i = 0; i < extraPa; ++i) {
                remp.append(rnd.nextInt(9));
            }
            sbu.replace(0, extraPa, remp.toString());
            finalValue = Long.parseLong(sbu.toString());
            break;
        case REPLACE_LAST_CHARS:
            Integer extraPar = null;
            try {
                extraPar = Integer.parseInt(extraParameter);
            } catch (NumberFormatException e) {
                finalValue = 0L;
                break;
            }
            if (valueIn == null || extraPar < 0) {
                finalValue = 0L;
                break;
            }
            extraPar = ((int) Math.log10(valueIn) + 1 <= extraPar) ? (int) Math.log10(valueIn) + 1 : extraPar;
            StringBuilder sbui = new StringBuilder(valueIn.toString());
            StringBuilder rempl = new StringBuilder(EMPTY_STRING);
            for (int i = 0; i < extraPar; ++i) {
                rempl.append(rnd.nextInt(9));
            }
            sbui.replace(sbui.length() - extraPar, sbui.length(), rempl.toString());
            finalValue = Long.parseLong(sbui.toString());
            break;
        default:
            finalValue = 0L;
        }
        return finalValue;
    }

    /**
     * Method "generateMaskedRow". This method is called when the input is a Integer.
     * 
     * @param valueIn The input sent to the function.
     * @param function The function used on the date parameter (see the enum Function).
     * @param extraParameter A parameter required by some functions (eg, Numeric Variance).
     * @return This method returns a Integer after the application of the parameter function.
     */

    public Integer generateMaskedRow(Integer valueIn, Function function, String extraParameter) {
        if (function == Function.SET_TO_NULL || valueIn == null && keepNull) {
            return null;
        }
        Integer finalValue = 0;
        Integer extraParam = null;
        switch (function) {
        case NUMERIC_VARIANCE:
            if (valueIn != null) {
                try {
                    extraParam = Integer.parseInt(extraParameter);
                } catch (NumberFormatException e) {
                    extraParam = 10;
                }
                if (extraParam < 0) {
                    extraParam *= -1;
                } else if (extraParam == 0) {
                    extraParam = 10;
                }
                int rate = 0;
                do {
                    rate = rnd.nextInt(2 * extraParam) - extraParam;
                } while (rate == 0);
                Float value = Float.parseFloat(valueIn.toString());
                value *= ((float) rate + 100) / 100;
                finalValue = new Integer(Math.round(value));
            }
            break;
        case REPLACE_NUMERIC:
            if (valueIn != null) {
                try {
                    extraParam = Integer.parseInt(extraParameter);
                } catch (NumberFormatException e) {
                    extraParam = 0;
                }
                if (extraParam < 0 || extraParam > 9) {
                    extraParam = 0;
                }
                String str = valueIn.toString();
                String res = str.replaceAll("\\d", extraParam.toString()); //$NON-NLS-1$
                finalValue = Integer.valueOf(res);
            }
            break;
        case GENERATE_BETWEEN:
            String[] parameters = extraParameter.split(","); //$NON-NLS-1$
            if (parameters.length != 2) {
                finalValue = 0;
            } else {
                int a = 0;
                int b = 0;
                try {
                    a = Integer.valueOf(parameters[0].trim());
                    b = Integer.valueOf(parameters[1].trim());
                } catch (NumberFormatException e) {
                    finalValue = 0;
                    break;
                }
                int min = (a < b) ? a : b;
                int max = (a < b) ? b : a;
                finalValue = rnd.nextInt((max - min) + 1) + min;
            }
            break;
        case GENERATE_FROM_LIST:
            String[] parameterss = extraParameter.split(","); //$NON-NLS-1$
            int[] parametersI = new int[parameterss.length];
            if (parameterss.length > 0) {
                for (int i = 0; i < parameterss.length; ++i) {
                    String tmp = parameterss[i].replaceAll("\\s+", EMPTY_STRING); //$NON-NLS-1$
                    try {
                        parametersI[i] = Integer.parseInt(tmp);
                    } catch (NumberFormatException e) {
                        finalValue = 0;
                        break;
                    }
                }
                finalValue = parametersI[rnd.nextInt(parametersI.length)];
            } else {
                finalValue = 0;
            }
            break;
        case GENERATE_FROM_FILE:
            try {
                @SuppressWarnings("resource")
                Scanner in = new Scanner(new FileReader(extraParameter));
                List<Integer> tokens = new ArrayList<Integer>();
                while (in.hasNext()) {
                    try {
                        tokens.add(Integer.parseInt(in.next()));
                    } catch (NumberFormatException e) {
                        finalValue = 0;
                        break;
                    }
                }
                finalValue = tokens.get(rnd.nextInt(tokens.size()));
            } catch (FileNotFoundException e) {
                finalValue = 0;
            }
            break;
        case HASH_GENERATE_LIST:
            String[] parametersh = extraParameter.split(","); //$NON-NLS-1$
            int[] parametersIh = new int[parametersh.length];
            if (parametersIh.length > 0) {
                for (int i = 0; i < parametersh.length; ++i) {
                    String tmp = parametersh[i].replaceAll("\\s+", EMPTY_STRING); //$NON-NLS-1$
                    try {
                        parametersIh[i] = Integer.parseInt(tmp);
                    } catch (NumberFormatException e) {
                        finalValue = 0;
                        break;
                    }
                }
                if (valueIn == null) {
                    finalValue = parametersIh[rnd.nextInt(parametersIh.length)];
                } else {
                    finalValue = parametersIh[Math.abs(valueIn.hashCode()) % parametersIh.length];
                }
            } else {
                finalValue = 0;
            }
            break;
        case HASH_GENERATE_FILE:
            try {
                @SuppressWarnings("resource")
                Scanner in = new Scanner(new FileReader(extraParameter));
                List<Integer> tokens = new ArrayList<Integer>();
                while (in.hasNext()) {
                    try {
                        tokens.add(Integer.parseInt(in.next()));
                    } catch (NumberFormatException e) {
                        finalValue = 0;
                        break;
                    }
                }
                if (valueIn == null) {
                    finalValue = tokens.get(rnd.nextInt(tokens.size()));
                } else {
                    finalValue = tokens.get(Math.abs(valueIn.hashCode()) % tokens.size());
                }
            } catch (FileNotFoundException e) {
                finalValue = 0;
            }
            break;
        case REMOVE_FIRST_CHARS:
            if (valueIn != null) {
                Integer extra = null;
                try {
                    extra = Integer.parseInt(extraParameter.trim());
                } catch (NumberFormatException e) {
                    finalValue = 0;
                    break;
                }
                if ((int) Math.log10(valueIn) + 1 <= extra || extra < 0) {
                    finalValue = 0;
                    break;
                }
                StringBuilder sb = new StringBuilder(valueIn.toString().substring(extra));
                finalValue = Integer.parseInt(sb.toString());
            }
            break;
        case REMOVE_LAST_CHARS:
            if (valueIn != null) {
                Integer extraP = null;
                try {
                    extraP = Integer.parseInt(extraParameter.trim());
                } catch (NumberFormatException e) {
                    finalValue = 0;
                    break;
                }
                if ((int) Math.log10(valueIn) + 1 <= extraP || extraP < 0) {
                    finalValue = 0;
                    break;
                }
                finalValue = valueIn / (int) Math.pow(10.0, extraP);
            }
            break;
        case REPLACE_FIRST_CHARS:
            if (valueIn != null) {
                Integer extraPa = null;
                try {
                    extraPa = Integer.parseInt(extraParameter.trim());
                } catch (NumberFormatException e) {
                    finalValue = 0;
                    break;
                }
                if (extraPa < 0) {
                    finalValue = 0;
                    break;
                }
                extraPa = ((int) Math.log10(valueIn) + 1 <= extraPa) ? (int) Math.log10(valueIn) + 1 : extraPa;
                StringBuilder sbu = new StringBuilder(valueIn.toString());
                StringBuilder remp = new StringBuilder(EMPTY_STRING);
                for (int i = 0; i < extraPa; ++i) {
                    remp.append(rnd.nextInt(9));
                }
                sbu.replace(0, extraPa, remp.toString());
                finalValue = Integer.parseInt(sbu.toString());
            }
            break;
        case REPLACE_LAST_CHARS:
            if (valueIn != null) {
                Integer extraPar = null;
                try {
                    extraPar = Integer.parseInt(extraParameter.trim());
                } catch (NumberFormatException e) {
                    finalValue = 0;
                    break;
                }
                if (extraPar < 0) {
                    finalValue = 0;
                    break;
                }
                extraPar = ((int) Math.log10(valueIn) + 1 <= extraPar) ? (int) Math.log10(valueIn) + 1 : extraPar;
                StringBuilder sbui = new StringBuilder(valueIn.toString());
                StringBuilder rempl = new StringBuilder(EMPTY_STRING);
                for (int i = 0; i < extraPar; ++i) {
                    rempl.append(rnd.nextInt(9));
                }
                sbui.replace(sbui.length() - extraPar, sbui.length(), rempl.toString());
                finalValue = Integer.parseInt(sbui.toString());
            }
            break;
        default:
            return finalValue;
        }
        return finalValue;
    }
}