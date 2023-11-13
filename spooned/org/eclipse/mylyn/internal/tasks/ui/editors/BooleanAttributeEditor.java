/**
 * *****************************************************************************
 * Copyright (c) 2004, 2015 Tasktop Technologies and others.
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
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
/**
 *
 * @author Steffen Pingel
 */
public class BooleanAttributeEditor extends org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor {
    private org.eclipse.swt.widgets.Button button;

    private boolean ignoreNotification;

    private boolean suppressRefresh;

    public BooleanAttributeEditor(org.eclipse.mylyn.tasks.core.data.TaskDataModel manager, org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute) {
        super(manager, taskAttribute);
    }

    @java.lang.Override
    public void createControl(org.eclipse.swt.widgets.Composite parent, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        button = toolkit.createButton(parent, super.getLabel(), org.eclipse.swt.SWT.CHECK);
        button.setEnabled(!isReadOnly());
        button.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                if (!ignoreNotification) {
                    try {
                        suppressRefresh = true;
                        setValue(button.getSelection());
                    } finally {
                        suppressRefresh = false;
                    }
                }
            }
        });
        button.setToolTipText(getDescription());
        refresh();
        setControl(button);
        if (!getTaskAttribute().hasValue()) {
            // set initial value to false to match what the editor shows
            // use asyncExec to ensure this happens after decorating, otherwise this appears as an incoming change
            org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                public void run() {
                    if (!getTaskAttribute().hasValue()) {
                        getAttributeMapper().setBooleanValue(getTaskAttribute(), false);
                    }
                }
            });
        }
    }

    @java.lang.Override
    public java.lang.String getLabel() {
        return "";// $NON-NLS-1$

    }

    public boolean getValue() {
        return getAttributeMapper().getBooleanValue(getTaskAttribute());
    }

    @java.lang.Override
    protected boolean needsValue() {
        return false;
    }

    public void setValue(boolean value) {
        getAttributeMapper().setBooleanValue(getTaskAttribute(), value);
        attributeChanged();
    }

    @java.lang.Override
    public void refresh() {
        if ((button == null) || button.isDisposed()) {
            return;
        }
        try {
            ignoreNotification = true;
            button.setSelection(getValue());
        } finally {
            ignoreNotification = false;
        }
    }

    @java.lang.Override
    public boolean shouldAutoRefresh() {
        return !suppressRefresh;
    }
}