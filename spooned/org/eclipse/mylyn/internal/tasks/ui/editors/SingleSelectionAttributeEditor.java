/**
 * *****************************************************************************
 * Copyright (c) 2004, 2011 Tasktop Technologies and others.
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
import org.eclipse.core.runtime.Assert;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
/**
 *
 * @author Steffen Pingel
 */
public class SingleSelectionAttributeEditor extends org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor {
    private org.eclipse.swt.custom.CCombo combo;

    private boolean ignoreNotification;

    private org.eclipse.swt.widgets.Text text;

    private java.lang.String[] values;

    public SingleSelectionAttributeEditor(org.eclipse.mylyn.tasks.core.data.TaskDataModel manager, org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute) {
        super(manager, taskAttribute);
    }

    @java.lang.Override
    public void createControl(final org.eclipse.swt.widgets.Composite parent, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        if (isReadOnly()) {
            text = new org.eclipse.swt.widgets.Text(parent, org.eclipse.swt.SWT.FLAT | org.eclipse.swt.SWT.READ_ONLY);
            text.setFont(EditorUtil.TEXT_FONT);
            toolkit.adapt(text, false, false);
            text.setData(org.eclipse.ui.forms.widgets.FormToolkit.KEY_DRAW_BORDER, java.lang.Boolean.FALSE);
            text.setToolTipText(getDescription());
            setControl(text);
        } else {
            combo = new org.eclipse.swt.custom.CCombo(parent, org.eclipse.swt.SWT.FLAT | org.eclipse.swt.SWT.READ_ONLY);
            combo.setVisibleItemCount(10);
            toolkit.adapt(combo, false, false);
            combo.setFont(EditorUtil.TEXT_FONT);
            combo.setData(org.eclipse.ui.forms.widgets.FormToolkit.KEY_DRAW_BORDER, org.eclipse.ui.forms.widgets.FormToolkit.TREE_BORDER);
            combo.setToolTipText(getDescription());
            combo.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                @java.lang.Override
                public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
                    if (!ignoreNotification) {
                        int index = combo.getSelectionIndex();
                        if (index > (-1)) {
                            org.eclipse.core.runtime.Assert.isNotNull(values);
                            org.eclipse.core.runtime.Assert.isLegal((index >= 0) && (index <= (values.length - 1)));
                            setValue(values[index]);
                        }
                    }
                }
            });
            org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.addScrollListener(combo);
            setControl(combo);
        }
        refresh();
    }

    public java.lang.String getValue() {
        return getAttributeMapper().getValue(getTaskAttribute());
    }

    public java.lang.String getValueLabel() {
        return getAttributeMapper().getValueLabel(getTaskAttribute());
    }

    @java.lang.Override
    public void refresh() {
        try {
            ignoreNotification = true;
            if ((text != null) && (!text.isDisposed())) {
                java.lang.String label = getValueLabel();
                if ("".equals(label)) {
                    // $NON-NLS-1$
                    // if set to the empty string the label will use 64px on GTK
                    text.setText(" ");// $NON-NLS-1$

                } else {
                    text.setText(label);
                }
            } else if ((combo != null) && (!combo.isDisposed())) {
                combo.removeAll();
                java.util.Map<java.lang.String, java.lang.String> labelByValue = getAttributeMapper().getOptions(getTaskAttribute());
                if (labelByValue != null) {
                    values = labelByValue.keySet().toArray(new java.lang.String[0]);
                    for (java.lang.String value : values) {
                        combo.add(labelByValue.get(value));
                    }
                }
                select(getValue(), getValueLabel());
                combo.redraw();
            }
        } finally {
            ignoreNotification = false;
        }
    }

    @java.lang.Override
    public boolean shouldAutoRefresh() {
        return true;
    }

    private void select(java.lang.String value, java.lang.String label) {
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                if (values[i].equals(value)) {
                    combo.select(i);
                    break;
                }
            }
        } else {
            combo.setText(label);
        }
    }

    void selectDefaultValue() {
        if ((combo.getSelectionIndex() == (-1)) && (values.length > 0)) {
            combo.select(0);
            setValue(values[0]);
        }
    }

    public void setValue(java.lang.String value) {
        java.lang.String oldValue = getAttributeMapper().getValue(getTaskAttribute());
        if (!oldValue.equals(value)) {
            getAttributeMapper().setValue(getTaskAttribute(), value);
            attributeChanged();
        }
    }
}