package org.talend.dataquality.datamasking;

import org.talend.dataquality.duplicating.RandomWrapper;

public class PhoneNumberGenerator {

    @SuppressWarnings("unused")
    private static com.google.i18n.phonenumbers.PhoneNumberUtil phoneUtil = com.google.i18n.phonenumbers.PhoneNumberUtil
            .getInstance();

    private static RandomWrapper rnd = null;

    public PhoneNumberGenerator(RandomWrapper rnd) {
        PhoneNumberGenerator.rnd = rnd;
    }

    /**
     * This function calculates the number of digits in a string.
     * 
     * @param str The string given as parameter.
     * @return The number of digits in the parameter.
     */
    private static int nb_digits(String str) {
        int ret = 0;
        for (int i = 0; i < str.length(); ++i) {
            if (Character.isDigit(str.charAt(i))) {
                ret++;
            }
        }
        return ret;
    }

    /**
     * This function generates a valid french phone number written in international format.
     * 
     * @return A string holding the phone number.
     */
    public String generatePhoneNumber() {
        StringBuilder result = new StringBuilder("+33 "); //$NON-NLS-1$
        result.append(rnd.nextInt(5) + 1);
        for (int i = 0; i < 9; ++i) {
            result.append(rnd.nextInt(9));
        }
        return result.toString();
    }

    /**
     * This function generates a phone number, preserving the firsts digits and adding a starting '+'.
     * 
     * @param number The original phone number.
     * @param digits_to_save The number of digits to keep.
     * @return A string holding the phone number.
     */
    public String generatePhoneNumber(String number, int digits_to_save) {
        String new_number = number.replaceAll("\\s+", ""); //$NON-NLS-1$ //$NON-NLS-2$                                                                                          
        int length = nb_digits(new_number);
        int start_index = 0;
        int digits_to_skip = digits_to_save;
        if (new_number.charAt(0) == '+') {
            length++;
            digits_to_skip++;
            start_index++;
        }
        StringBuilder result = new StringBuilder("+"); //$NON-NLS-1$                                                                                                            
        for (int i = start_index; i < length + 1; ++i) {
            if (i < digits_to_skip) {
                result.append(new_number.charAt(i));
            } else if (i == digits_to_skip) {
                result.append(" "); //$NON-NLS-1$                                                                                                                               
            } else {
                result.append(rnd.nextInt(10));
            }
        }
        return result.toString();
    }

}
