/**
 * *****************************************************************************
 * Copyright (c) 2009, 2013 Tasktop Technologies and others.
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
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
/**
 *
 * @author Steffen Pingel
 */
public class PriorityAttributeEditor extends org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor {
    private org.eclipse.mylyn.internal.tasks.ui.editors.PriorityEditor editor;

    private org.eclipse.mylyn.tasks.core.ITaskMapping mapping;

    public PriorityAttributeEditor(org.eclipse.mylyn.tasks.core.data.TaskDataModel manager, org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute) {
        super(manager, taskAttribute);
        boolean noOptions = getAttributeMapper().getOptions(getTaskAttribute()).size() == 0;
        setReadOnly(isReadOnly() || noOptions);
    }

    @java.lang.Override
    public void createControl(final org.eclipse.swt.widgets.Composite parent, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryConnector(getModel().getTaskRepository().getConnectorKind());
        mapping = connector.getTaskMapping(getModel().getTaskData());
        editor = new org.eclipse.mylyn.internal.tasks.ui.editors.PriorityEditor(getTaskAttribute()) {
            @java.lang.Override
            protected void valueChanged(java.lang.String value) {
                setValue(value);
            }
        };
        editor.setReadOnly(isReadOnly());
        editor.createControl(parent, toolkit);
        setControl(editor.getControl());
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
        if ((editor.getControl() != null) && (!editor.getControl().isDisposed())) {
            editor.setLabelByValue(getAttributeMapper().getOptions(getTaskAttribute()));
            updateEditor();
        }
    }

    @java.lang.Override
    public boolean shouldAutoRefresh() {
        return true;
    }

    public void setValue(java.lang.String value) {
        java.lang.String oldValue = getAttributeMapper().getValue(getTaskAttribute());
        if (!oldValue.equals(value)) {
            getAttributeMapper().setValue(getTaskAttribute(), value);
            attributeChanged();
        }
    }

    private void updateEditor() {
        editor.select(getValue(), mapping.getPriorityLevel());
        editor.setToolTipText(java.text.MessageFormat.format(Messages.PriorityAttributeEditor_Priority_Tooltip, getValueLabel()));
    }
}