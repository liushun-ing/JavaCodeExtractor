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
import org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
/**
 *
 * @author Steffen Pingel
 */
public class TextAttributeEditor extends org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor {
    private org.eclipse.swt.widgets.Text text;

    private boolean suppressRefresh;

    public TextAttributeEditor(org.eclipse.mylyn.tasks.core.data.TaskDataModel manager, org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute) {
        super(manager, taskAttribute);
        setLayoutHint(new org.eclipse.mylyn.tasks.ui.editors.LayoutHint(org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan.SINGLE, org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan.SINGLE));
    }

    protected org.eclipse.swt.widgets.Text getText() {
        return text;
    }

    @java.lang.Override
    public void createControl(org.eclipse.swt.widgets.Composite parent, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        createControl(parent, toolkit, org.eclipse.swt.SWT.NONE);
    }

    protected void createControl(org.eclipse.swt.widgets.Composite parent, org.eclipse.ui.forms.widgets.FormToolkit toolkit, int style) {
        if (isReadOnly()) {
            text = new org.eclipse.swt.widgets.Text(parent, org.eclipse.swt.SWT.FLAT | org.eclipse.swt.SWT.READ_ONLY);
            text.setFont(EditorUtil.TEXT_FONT);
            text.setData(org.eclipse.ui.forms.widgets.FormToolkit.KEY_DRAW_BORDER, java.lang.Boolean.FALSE);
            text.setToolTipText(getDescription());
            text.setText(getValue());
        } else {
            text = toolkit.createText(parent, getValue(), org.eclipse.swt.SWT.FLAT | style);
            text.setFont(EditorUtil.TEXT_FONT);
            text.setData(org.eclipse.ui.forms.widgets.FormToolkit.KEY_DRAW_BORDER, org.eclipse.ui.forms.widgets.FormToolkit.TREE_BORDER);
            text.setToolTipText(getDescription());
            text.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
                public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
                    try {
                        suppressRefresh = true;
                        setValue(text.getText());
                    } finally {
                        suppressRefresh = false;
                    }
                    org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil.ensureVisible(text);
                }
            });
        }
        toolkit.adapt(text, false, false);
        setControl(text);
    }

    public java.lang.String getValue() {
        return getAttributeMapper().getValue(getTaskAttribute());
    }

    public void setValue(java.lang.String text) {
        getAttributeMapper().setValue(getTaskAttribute(), text);
        attributeChanged();
    }

    @java.lang.Override
    public void refresh() {
        if ((text != null) && (!text.isDisposed())) {
            text.setText(getValue());
        }
    }

    @java.lang.Override
    public boolean shouldAutoRefresh() {
        return !suppressRefresh;
    }
}