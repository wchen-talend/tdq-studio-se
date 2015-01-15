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
package org.talend.dataquality.record.linkage.ui.i18n.internal;

import java.util.ResourceBundle;

import org.talend.commons.i18n.MessagesCore;

/**
 * Default implementation of MessageCore from org.talend.commons plug-in.<br/>
 *
 * Developpers can copy this class in their plug-in and change :
 * <ul>
 * <li>the BUNDLE_NAME constant</li>
 * </ul>
 *
 * $Id: DefaultMessagesImpl.java 3351 2007-05-04 12:14:00 +0000 (星期五, 04 五月 2007) plegall $
 *
 */

public class DefaultMessagesImpl extends MessagesCore {

    private static final String BUNDLE_NAME = "messages"; //$NON-NLS-1$

    private static final String PLUGIN_ID = "org.talend.dataquality.record.linkage.ui"; //$NON-NLS-1$

    private static ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME);

    /**
     * Returns the i18n formatted message for <i>key</i> in the class bundle.
     *
     * @param key - the key for the desired string
     * @return the string for the given key in the class resource bundle
     * @see MessagesCore#getString(String, ResourceBundle)
     */
    public static String getString(String key) {
        return getString(key, PLUGIN_ID, resourceBundle);
    }

    /**
     * Returns the i18n formatted message for <i>key</i> and <i>args</i> in the specified bundle.
     *
     * @param key - the key for the desired string
     * @param args - arg to include in the string
     * @return the string for the given key in the given resource bundle
     * @see MessagesCore#getString(String, ResourceBundle, Object[])
     */
    public static String getString(String key, Object... args) {
        return getString(key, PLUGIN_ID, resourceBundle, args);
    }
}
