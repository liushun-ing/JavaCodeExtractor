/**
 * *****************************************************************************
 * Copyright (c) 2010, 2014 Tasktop Technologies and others.
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
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.ui.editors.AttributeEditorToolkit;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
/**
 *
 * @author Thomas Ehrnhoefer
 */
public class IntegerAttributeEditor extends org.eclipse.mylyn.internal.tasks.ui.editors.TextAttributeEditor {
    public IntegerAttributeEditor(org.eclipse.mylyn.tasks.core.data.TaskDataModel manager, org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute) {
        super(manager, taskAttribute);
    }

    @java.lang.Override
    public void createControl(org.eclipse.swt.widgets.Composite parent, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        super.createControl(parent, toolkit);
        org.eclipse.mylyn.tasks.ui.editors.AttributeEditorToolkit.createValidator(this, getText(), getAttributeTypeValidator());
    }

    org.eclipse.jface.dialogs.IInputValidator getAttributeTypeValidator() {
        return new org.eclipse.jface.dialogs.IInputValidator() {
            public java.lang.String isValid(java.lang.String newText) {
                if (org.apache.commons.lang.StringUtils.isNotBlank(newText)) {
                    try {
                        java.lang.Integer.parseInt(newText);
                    } catch (java.lang.NumberFormatException e) {
                        return Messages.IntegerAttributeEditor_this_field_requires_integer_value;
                    }
                }
                return null;
            }
        };
    }
}