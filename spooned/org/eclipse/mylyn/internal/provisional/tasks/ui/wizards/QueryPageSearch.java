/**
 * *****************************************************************************
 * Copyright (c) 2015 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.provisional.tasks.ui.wizards;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
@java.lang.SuppressWarnings("nls")
public class QueryPageSearch {
    /**
     * Stores search criteria in the order entered by the user.
     */
    private final java.util.Map<java.lang.String, org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.QueryPageFilter> filterByFieldName = new java.util.LinkedHashMap<java.lang.String, org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.QueryPageFilter>();

    public QueryPageSearch() {
    }

    public QueryPageSearch(java.lang.String queryParameter) {
        fromUrl(queryParameter);
    }

    public void addFilter(java.lang.String key, java.lang.String value) {
        org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.QueryPageFilter filter = filterByFieldName.get(key);
        if (filter == null) {
            filter = new org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.QueryPageFilter(key, value);
            filterByFieldName.put(key, filter);
        }
    }

    public void addFilter(org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.QueryPageFilter filter) {
        filterByFieldName.put(filter.getKey(), filter);
    }

    public java.util.List<org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.QueryPageFilter> getFilters() {
        return new java.util.ArrayList<org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.QueryPageFilter>(filterByFieldName.values());
    }

    public org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.QueryPageFilter getFilter(java.lang.String key) {
        return filterByFieldName.get(key);
    }

    public void fromUrl(java.lang.String url) {
        int idx = url.indexOf('?');
        java.util.StringTokenizer t = new java.util.StringTokenizer(idx != (-1) ? url.substring(idx + 1) : url, "&");// $NON-NLS-1$

        while (t.hasMoreTokens()) {
            java.lang.String token = t.nextToken();
            int i = token.indexOf("=");// $NON-NLS-1$

            if (i != (-1)) {
                try {
                    java.lang.String key = java.net.URLDecoder.decode(token.substring(0, i), "UTF-8");
                    java.lang.String value = java.net.URLDecoder.decode(token.substring(i + 1), "UTF-8");
                    org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.QueryPageFilter filter = filterByFieldName.get(key);
                    if (filter == null) {
                        addFilter(key, value);
                    } else {
                        filter.addValue(value);
                    }
                } catch (java.io.UnsupportedEncodingException e) {
                    org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.WARNING, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Unexpected exception while decoding URL", e));// $NON-NLS-1$

                }
            }
        } 
    }

    public java.lang.String toQuery() {
        java.lang.StringBuilder sb = new java.lang.StringBuilder();
        int count = 0;
        for (org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.QueryPageFilter filter : filterByFieldName.values()) {
            for (java.lang.String actualValue : filter.getValues()) {
                java.lang.String encodedValue = null;
                try {
                    encodedValue = java.net.URLEncoder.encode(actualValue, "UTF-8");
                    if ((count++) > 0) {
                        sb.append("&");
                    }
                    sb.append(filter.getKey());
                    sb.append("=");
                    sb.append(encodedValue);
                } catch (java.io.UnsupportedEncodingException e) {
                    org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.WARNING, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Unexpected exception while encoding URL", e));// $NON-NLS-1$

                }
            }
        }
        return sb.toString();
    }
}