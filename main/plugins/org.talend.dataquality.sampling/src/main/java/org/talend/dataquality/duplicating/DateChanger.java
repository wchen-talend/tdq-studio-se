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
package org.talend.dataquality.duplicating;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * This class provides the following ways to modify a date:
 * <p>
 * 1. modify one value among the day, month and the year.
 * <p>
 * 2. switch the day and month value. if the day value is greater than 12, then use day%12 as month value.
 * <p>
 * 3. replace with a random date.
 * <p>
 * 4. modifies the date forward or backward according to the int parameter.
 */
public class DateChanger {

    private static final Long nb_ms_per_day = 86400000L;

    private static final Random random = new Random();

    /**
     * Set random seed
     * 
     * @param seed
     */
    public void setSeed(long seed) {
        random.setSeed(seed);
    }

    @SuppressWarnings("deprecation")
    Date modifyDateValue(Date date) {

        int choice = random.nextInt(3);
        switch (choice) {
        case 0:
            date.setYear(random.nextInt(200));
            break;
        case 1:
            date.setMonth(random.nextInt(12));
            break;
        case 2:
            date.setDate(random.nextInt(31) + 1);
            break;
        default:
            break;
        }
        return date;
    }

    @SuppressWarnings("deprecation")
    Date switchDayMonthValue(Date date) {
        int tempMonth = date.getMonth();
        date.setMonth((date.getDate() - 1) % 12);
        date.setDate(tempMonth + 1);
        return date;
    }

    @SuppressWarnings("deprecation")
    Date replaceWithRandomDate(Date date) {
        date.setYear(random.nextInt(100));
        date.setMonth(random.nextInt(12));
        date.setDate(random.nextInt(31));
        return date;
    }

    public Date dateVariance(Date date, Integer rate) {
        int variation = 0;
        do {
            variation = random.nextInt(2 * rate) - rate;
        } while (variation == 0);
        Long originalDate = date.getTime();
        Date newDate = new Date(originalDate + variation * nb_ms_per_day);
        return newDate;
    }

    public Date generateDateBetween(String minString, String maxString, RandomWrapper rnd) {
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy"); //$NON-NLS-1$
        Date minDate = null;
        Date maxDate = null;
        try {
            minDate = df.parse(minString);
            maxDate = df.parse(maxString);
        } catch (ParseException e) {
            return new Date(System.currentTimeMillis());
        }
        if (minDate.after(maxDate)) {
            Date tmp = minDate;
            minDate = maxDate;
            maxDate = tmp;
        } else if (minDate.equals(maxDate)) {
            return minDate;
        }
        long min = minDate.getTime();
        long max = maxDate.getTime();
        long number = min + ((long) (rnd.nextDouble() * (max - min)));
        Date newDate = new Date(number);
        return newDate;
    }
}
