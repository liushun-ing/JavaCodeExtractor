/**
 * *****************************************************************************
 * Copyright (c) 2004, 2013 Tasktop Technologies and others.
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
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.commons.core.DateUtil;
import org.eclipse.mylyn.commons.workbench.forms.DatePicker;
import org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
/**
 *
 * @author Steffen Pingel
 * @author Robert Elves
 */
public class DateAttributeEditor extends org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor {
    private org.eclipse.mylyn.commons.workbench.forms.DatePicker datePicker;

    private boolean showTime;

    private boolean showDateRelative;

    private org.eclipse.swt.widgets.Text text;

    public DateAttributeEditor(org.eclipse.mylyn.tasks.core.data.TaskDataModel manager, org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute) {
        super(manager, taskAttribute);
        setLayoutHint(new org.eclipse.mylyn.tasks.ui.editors.LayoutHint(org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan.SINGLE, org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan.SINGLE));
        setShowDateRelative(false);
    }

    @java.lang.Override
    public void createControl(org.eclipse.swt.widgets.Composite composite, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        if (isReadOnly()) {
            text = new org.eclipse.swt.widgets.Text(composite, org.eclipse.swt.SWT.FLAT | org.eclipse.swt.SWT.READ_ONLY);
            text.setFont(EditorUtil.TEXT_FONT);
            toolkit.adapt(text, false, false);
            text.setData(org.eclipse.ui.forms.widgets.FormToolkit.KEY_DRAW_BORDER, java.lang.Boolean.FALSE);
            text.setText(getTextValue());
            text.setToolTipText(getDescription());
            setControl(text);
        } else {
            datePicker = new org.eclipse.mylyn.commons.workbench.forms.DatePicker(composite, org.eclipse.swt.SWT.FLAT, getTextValue(), showTime, 0);
            datePicker.setFont(EditorUtil.TEXT_FONT);
            datePicker.setToolTipText(getDescription());
            if (!showTime) {
                datePicker.setDateFormat(org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.getDateFormat());
            } else {
                datePicker.setDateFormat(org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.getDateTimeFormat());
            }
            updateDatePicker();
            datePicker.addPickerSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                @java.lang.Override
                public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                    java.util.Calendar cal = datePicker.getDate();
                    if (cal != null) {
                        if (!showTime) {
                            org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil.snapStartOfDay(cal);
                        }
                        java.util.Date value = cal.getTime();
                        if (!value.equals(getValue())) {
                            setValue(value);
                        }
                    } else {
                        if (getValue() != null) {
                            setValue(null);
                        }
                        datePicker.setDate(null);
                    }
                }
            });
            org.eclipse.jface.layout.GridDataFactory.fillDefaults().hint(120, org.eclipse.swt.SWT.DEFAULT).grab(false, false).applyTo(datePicker);
            datePicker.setData(org.eclipse.ui.forms.widgets.FormToolkit.KEY_DRAW_BORDER, org.eclipse.ui.forms.widgets.FormToolkit.TREE_BORDER);
            toolkit.adapt(datePicker, false, false);
            setControl(datePicker);
        }
    }

    private void updateDatePicker() {
        if (getValue() != null) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(getValue());
            datePicker.setDate(cal);
        }
    }

    @java.lang.Override
    protected void decorateIncoming(org.eclipse.swt.graphics.Color color) {
        if (datePicker != null) {
            datePicker.setBackground(color);
        }
    }

    public boolean getShowTime() {
        return showTime;
    }

    private java.lang.String getTextValue() {
        java.util.Date date = getValue();
        if (date != null) {
            if (getShowTime()) {
                return org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.formatDateTime(date);
            } else if (getShowDateRelative()) {
                return org.eclipse.mylyn.commons.core.DateUtil.getRelative(date.getTime());
            } else {
                return org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.formatDate(date);
            }
        } else {
            return "";// $NON-NLS-1$

        }
    }

    public java.util.Date getValue() {
        return getAttributeMapper().getDateValue(getTaskAttribute());
    }

    public void setShowTime(boolean showTime) {
        this.showTime = showTime;
    }

    public void setValue(java.util.Date date) {
        getAttributeMapper().setDateValue(getTaskAttribute(), date);
        attributeChanged();
    }

    @java.lang.Override
    public void refresh() {
        if ((text != null) && (!text.isDisposed())) {
            text.setText(getTextValue());
        }
        if ((datePicker != null) && (!datePicker.isDisposed())) {
            updateDatePicker();
        }
    }

    @java.lang.Override
    public boolean shouldAutoRefresh() {
        return true;
    }

    public boolean getShowDateRelative() {
        return showDateRelative;
    }

    public void setShowDateRelative(boolean showDateRelative) {
        this.showDateRelative = showDateRelative;
    }
}