/**
 * *****************************************************************************
 * Copyright (c) 2008, 2011 Tasktop Technologies and others.
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
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
/**
 *
 * @author Steffen Pingel
 */
public class MultiSelectionAttributeEditor extends org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor {
    private java.lang.String[] allValues;

    private org.eclipse.swt.widgets.Text text;

    private org.eclipse.swt.widgets.List list;

    protected boolean suppressRefresh;

    public MultiSelectionAttributeEditor(org.eclipse.mylyn.tasks.core.data.TaskDataModel manager, org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute) {
        super(manager, taskAttribute);
        if (isReadOnly()) {
            setLayoutHint(new org.eclipse.mylyn.tasks.ui.editors.LayoutHint(org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan.SINGLE, org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan.SINGLE));
        } else {
            setLayoutHint(new org.eclipse.mylyn.tasks.ui.editors.LayoutHint(org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan.MULTIPLE, org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan.SINGLE));
        }
    }

    @java.lang.Override
    public void createControl(org.eclipse.swt.widgets.Composite parent, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        if (isReadOnly()) {
            text = new org.eclipse.swt.widgets.Text(parent, (org.eclipse.swt.SWT.FLAT | org.eclipse.swt.SWT.READ_ONLY) | org.eclipse.swt.SWT.WRAP);
            text.setFont(EditorUtil.TEXT_FONT);
            toolkit.adapt(text, false, false);
            text.setData(org.eclipse.ui.forms.widgets.FormToolkit.KEY_DRAW_BORDER, java.lang.Boolean.FALSE);
            select(getValues(), getValuesLabels());
            text.setToolTipText(getDescription());
            setControl(text);
        } else {
            list = new org.eclipse.swt.widgets.List(parent, (org.eclipse.swt.SWT.FLAT | org.eclipse.swt.SWT.MULTI) | org.eclipse.swt.SWT.V_SCROLL);
            toolkit.adapt(list, false, false);
            list.setData(org.eclipse.ui.forms.widgets.FormToolkit.KEY_DRAW_BORDER, org.eclipse.ui.forms.widgets.FormToolkit.TREE_BORDER);
            list.setFont(EditorUtil.TEXT_FONT);
            list.setToolTipText(getDescription());
            updateListWithOptions();
            select(getValues(), getValuesLabels());
            if (allValues != null) {
                list.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                    @java.lang.Override
                    public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                        org.eclipse.core.runtime.Assert.isNotNull(allValues);
                        int[] indices = list.getSelectionIndices();
                        java.lang.String[] selectedValues = new java.lang.String[indices.length];
                        for (int i = 0; i < indices.length; i++) {
                            int index = indices[i];
                            org.eclipse.core.runtime.Assert.isLegal((index >= 0) && (index <= (allValues.length - 1)));
                            selectedValues[i] = allValues[index];
                        }
                        try {
                            suppressRefresh = true;
                            setValues(selectedValues);
                        } finally {
                            suppressRefresh = false;
                        }
                    }
                });
                list.showSelection();
            }
            setControl(list);
        }
    }

    private void updateListWithOptions() {
        if ((list != null) && (!list.isDisposed())) {
            java.util.Map<java.lang.String, java.lang.String> labelByValue = getAttributeMapper().getOptions(getTaskAttribute());
            if (labelByValue != null) {
                int topIndex = list.getTopIndex();
                list.removeAll();
                allValues = labelByValue.keySet().toArray(new java.lang.String[0]);
                for (java.lang.String value : allValues) {
                    list.add(labelByValue.get(value));
                }
                if (topIndex > 0) {
                    list.setTopIndex(topIndex);
                }
            }
        }
    }

    public java.lang.String[] getValues() {
        return getAttributeMapper().getValues(getTaskAttribute()).toArray(new java.lang.String[0]);
    }

    public java.lang.String[] getValuesLabels() {
        return getAttributeMapper().getValueLabels(getTaskAttribute()).toArray(new java.lang.String[0]);
    }

    private void select(java.lang.String[] values, java.lang.String[] labels) {
        if ((text != null) && (!text.isDisposed())) {
            java.lang.StringBuilder valueString = new java.lang.StringBuilder();
            if (labels != null) {
                for (int i = 0; i < labels.length; i++) {
                    valueString.append(labels[i]);
                    if (i != (labels.length - 1)) {
                        valueString.append(", ");// $NON-NLS-1$

                    }
                }
            }
            text.setText(valueString.toString());
        } else if ((list != null) && (!list.isDisposed())) {
            if (values != null) {
                list.deselectAll();
                java.util.Set<java.lang.String> selectedValues = new java.util.HashSet<java.lang.String>(java.util.Arrays.asList(values));
                for (int i = 0; i < allValues.length; i++) {
                    if (selectedValues.contains(allValues[i])) {
                        list.select(i);
                    }
                }
            } else {
                list.setItems(labels);
                list.setSelection(labels);
            }
        }
    }

    public void setValues(java.lang.String[] values) {
        getAttributeMapper().setValues(getTaskAttribute(), java.util.Arrays.asList(values));
        attributeChanged();
    }

    @java.lang.Override
    public void refresh() {
        updateListWithOptions();
        select(getValues(), getValuesLabels());
    }

    @java.lang.Override
    public boolean shouldAutoRefresh() {
        return !suppressRefresh;
    }
}