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
package org.talend.dataprofiler.chart.i18n;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * created by yyin on 2014-12-8 Detailled comment
 * 
 */
public class Messages {

    private static Logger log = Logger.getLogger("topchart");

    private static final String BUNDLE_NAME = "org.talend.dataprofiler.chart.i18n.messages"; //$NON-NLS-1$

    private static final String PLUGIN_ID = "org.talend.dataprofiler.chart"; //$NON-NLS-1$

    private static ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME);

    public static final String KEY_NOT_FOUND_PREFIX = "!!!"; //$NON-NLS-1$

    public static final String KEY_NOT_FOUND_SUFFIX = "!!!"; //$NON-NLS-1$

    // add by wzhang for 13249, MessageFormat will not indicate {0} as i18n args if in couple single quotes.
    public static final String SINGLE_QUOTE = "'"; //$NON-NLS-1$

    public static final String SINGLE_QUOTE_MUTI = "''"; //$NON-NLS-1$

    public static String getString(final String key) {
        return getString(key, PLUGIN_ID, resourceBundle);
    }

    public static String getString(final String key, final Object... args) {
        return getString(key, PLUGIN_ID, resourceBundle, args);
    }

    /**
     * Returns the i18n formatted message for <i>key</i> in the specified bundle.
     * 
     * @param key - the key for the desired string
     * @param resourceBundle - the ResourceBundle to search in
     * @return the string for the given key in the given resource bundle
     */
    public static String getString(String key, String pluginId, ResourceBundle resourceBundle) {
        if (resourceBundle == null) {
            return KEY_NOT_FOUND_PREFIX + key + KEY_NOT_FOUND_SUFFIX;
        }
        //        log.trace("Getting key " + key + "in" + resourceBundle.toString()); //$NON-NLS-1$ //$NON-NLS-2$
        try {
            return resourceBundle.getString(key);

        } catch (MissingResourceException e) {
            return KEY_NOT_FOUND_PREFIX + key + KEY_NOT_FOUND_SUFFIX;
        }
    }

    /**
     * Returns the i18n formatted message for <i>key</i> and <i>args</i> in the specified bundle.
     * 
     * @param key - the key for the desired string
     * @param resourceBundle - the ResourceBundle to search in
     * @param args - arg to include in the string
     * @return the string for the given key in the given resource bundle
     */
    // modified by wzhang. add a pluginId parameter
    public static String getString(String key, String pluginId, ResourceBundle resourceBundle, Object... args) {
        try {
            return MessageFormat.format(getString(key, pluginId, resourceBundle).replaceAll(SINGLE_QUOTE, SINGLE_QUOTE_MUTI),
                    args);
        } catch (Exception e) {
            return KEY_NOT_FOUND_PREFIX + key + KEY_NOT_FOUND_SUFFIX;
        }
    }
}
