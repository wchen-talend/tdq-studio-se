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
package org.talend.dataquality.datamasking;

import org.talend.dataquality.duplicating.RandomWrapper;

public class CreditCardGenerator {

    private RandomWrapper rnd = null;

    private static final String DIGITS = "0123456789"; //$NON-NLS-1$

    /**
     * The enum stores the different types of Credit Card that can be generated.
     *
     */
    public enum CreditCardType {
        AMERICAN_EXPRESS,
        MASTER_CARD,
        VISA
    };

    public CreditCardGenerator(RandomWrapper rnd) {
        this.rnd = rnd;
    }

    /**
     * This function tests a credit card number if valid.
     * 
     * @param number represents the credit card number to be testes.
     * @return a boolean : true if the number is valid, false if not
     */
    private static boolean luhnTest(StringBuilder number) {
        int s1 = 0, s2 = 0;
        String reverse = new StringBuffer(number).reverse().toString();
        for (int i = 0; i < reverse.length(); i++) {
            int digit = Character.digit(reverse.charAt(i), 10);
            if (i % 2 == 0) {
                s1 += digit;
            } else {
                s2 += 2 * digit;
                if (digit >= 5) {
                    s2 -= 9;
                }
            }
        }
        return ((s1 + s2) % 10 == 0);
    }

    /**
     * This function creates a new Credit Card Number.
     * 
     * @param cct represents the credit card type to generate.
     * @param valueIn represents the credit card number sent in input.
     * @return a new number with the same length and prefix as @param valueIn.
     */
    public Long generateCreditCardFormat(CreditCardType cct, Long valueIn) {
        StringBuilder res = new StringBuilder(""); //$NON-NLS-1$
        int len = valueIn.toString().length();
        String prefix = ""; //$NON-NLS-1$
        if (cct == CreditCardType.VISA) {
            prefix = valueIn.toString().substring(0, 1);
        } else {
            prefix = valueIn.toString().substring(0, 2);
        }
        res.append(prefix);
        for (int i = prefix.length(); i < len; ++i) {
            int tmp = rnd.nextInt(10);
            res.append(tmp);
        }

        for (int i = 0; i < 10; ++i) {
            res.setCharAt(res.length() - 1, DIGITS.charAt(i));
            if (luhnTest(res) == true) {
                break;
            }
        }
        Long result = Long.parseLong(res.toString());
        return result;
    }

    /**
     * This function creates a new Credit Card Number.
     * 
     * @param cct represents the credit card type to generate.
     * @return a new credit card number.
     */
    public Long generateCreditCard(CreditCardType cct) {
        int len = 0;
        String init = ""; //$NON-NLS-1$
        StringBuilder res = new StringBuilder(""); //$NON-NLS-1$

        switch (cct) {
        case VISA:
            init = "4"; //$NON-NLS-1$
            len = 15;
            break;
        case MASTER_CARD:
            init = "53"; //$NON-NLS-1$
            len = 14;
            break;
        case AMERICAN_EXPRESS:
            init = "34"; //$NON-NLS-1$
            len = 13;
            break;
        default:
            return null;
        }
        res.append(init);
        for (int i = 0; i < len; ++i) {
            int tmp = rnd.nextInt(10);
            res.append(tmp);
        }

        for (int i = 0; i < 10; ++i) {
            res.setCharAt(res.length() - 1, DIGITS.charAt(i));
            if (luhnTest(res) == true) {
                break;
            }
        }
        Long result = Long.parseLong(res.toString());
        return result;
    }

    /**
     * This function checks if the input number is one of the known type. (see the enum)
     * 
     * @param ccn The credit card number sent in input.
     * @return Either null if the type is unknown, or the type of the card if known.
     */
    public CreditCardType getCreditCardType(Long ccn) {
        StringBuilder number = new StringBuilder(ccn.toString());
        if (number.length() == 15) {
            if (number.substring(0, 2).equals("34") || number.substring(0, 2).equals("37")) { //$NON-NLS-1$ //$NON-NLS-2$
                return CreditCardType.AMERICAN_EXPRESS;
            }
        } else if (number.length() == 16 || number.length() == 13) {
            Integer iin = Integer.parseInt(number.substring(0, 2));
            if (iin >= 51 && iin <= 56 && number.length() == 16) {
                return CreditCardType.MASTER_CARD;
            } else {
                Integer iin_short = iin / 10;
                if (iin_short == 4) {
                    return CreditCardType.VISA;
                }
            }
        }
        return null;
    }

    /**
     * The function is called if the input is null or if its type is unkwown.
     * 
     * @return One of the types from the enum, chosen randomly.
     */
    public CreditCardType chooseCreditCardType() {
        int choice = rnd.nextInt(3);
        if (choice == 0) {
            return CreditCardType.AMERICAN_EXPRESS;
        } else if (choice == 1) {
            return CreditCardType.MASTER_CARD;
        } else {
            return CreditCardType.VISA;
        }
    }
}
