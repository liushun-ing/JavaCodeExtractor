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
public class QueryPageFilter {
    private final java.lang.String key;

    private final java.util.List<java.lang.String> values = new java.util.ArrayList<java.lang.String>();

    public QueryPageFilter(java.lang.String key, java.lang.String value) {
        this.key = key;
        values.add(value);
    }

    public java.lang.String getKey() {
        return key;
    }

    public java.lang.String getValue() {
        return values.get(0);
    }

    public java.util.List<java.lang.String> getValues() {
        return values;
    }

    public void setValue(java.lang.String value) {
        values.clear();
        values.add(value);
    }

    public void setValues(java.util.List<java.lang.String> value) {
        values.clear();
        values.addAll(value);
    }

    public void addValue(java.lang.String value) {
        values.add(value);
    }
}