/**
 * *****************************************************************************
 * Copyright (c) 2015 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.editors;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
public class LabelsAttributeEditor extends org.eclipse.mylyn.internal.tasks.ui.editors.TextAttributeEditor {
    private static final java.lang.String VALUE_SEPARATOR = ",";// $NON-NLS-1$


    private final boolean isMultiSelect;

    public LabelsAttributeEditor(org.eclipse.mylyn.tasks.core.data.TaskDataModel manager, org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute) {
        super(manager, taskAttribute);
        this.isMultiSelect = org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_MULTI_SELECT.equals(taskAttribute.getMetaData().getType()) || org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_MULTI_LABEL.equals(taskAttribute.getMetaData().getType());
        if ((!isReadOnly()) && isMultiSelect) {
            setLayoutHint(new org.eclipse.mylyn.tasks.ui.editors.LayoutHint(org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan.MULTIPLE, org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan.SINGLE));
        }
    }

    @java.lang.Override
    public void createControl(org.eclipse.swt.widgets.Composite parent, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        super.createControl(parent, toolkit, (getLayoutHint() != null) && (getLayoutHint().rowSpan == org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan.MULTIPLE) ? org.eclipse.swt.SWT.WRAP : org.eclipse.swt.SWT.NONE);
        if ((!isReadOnly()) && isMultiSelect) {
            getText().setToolTipText("Separate multiple values with a comma");// $NON-NLS-1$

        }
    }

    @java.lang.Override
    public java.lang.String getValue() {
        if (isMultiSelect) {
            java.util.List<java.lang.String> values = getAttributeMapper().getValues(getTaskAttribute());
            return com.google.common.base.Joiner.on(org.eclipse.mylyn.internal.tasks.ui.editors.LabelsAttributeEditor.VALUE_SEPARATOR + " ").skipNulls().join(values);// $NON-NLS-1$

        } else {
            return getAttributeMapper().getValue(getTaskAttribute());
        }
    }

    @java.lang.Override
    public void setValue(java.lang.String text) {
        if (isMultiSelect) {
            java.lang.String[] values = text.split(org.eclipse.mylyn.internal.tasks.ui.editors.LabelsAttributeEditor.VALUE_SEPARATOR);
            getAttributeMapper().setValues(getTaskAttribute(), org.eclipse.mylyn.internal.tasks.ui.editors.LabelsAttributeEditor.getTrimmedValues(values));
        } else {
            getAttributeMapper().setValue(getTaskAttribute(), text);
        }
        attributeChanged();
    }

    public static java.util.List<java.lang.String> getTrimmedValues(java.lang.String[] values) {
        return com.google.common.collect.FluentIterable.from(java.util.Arrays.asList(values)).transform(new com.google.common.base.Function<java.lang.String, java.lang.String>() {
            @java.lang.Override
            public java.lang.String apply(java.lang.String input) {
                return com.google.common.base.Strings.nullToEmpty(input).trim();
            }
        }).filter(new com.google.common.base.Predicate<java.lang.String>() {
            @java.lang.Override
            public boolean apply(java.lang.String input) {
                return !com.google.common.base.Strings.isNullOrEmpty(input);
            }
        }).toList();
    }
}