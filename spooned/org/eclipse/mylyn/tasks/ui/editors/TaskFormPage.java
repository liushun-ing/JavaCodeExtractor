/**
 * *****************************************************************************
 * Copyright (c) 2004, 2010 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.tasks.ui.editors;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.swt.SWT;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
/**
 *
 * @author Steffen Pingel
 * @since 3.1
 */
public class TaskFormPage extends org.eclipse.ui.forms.editor.FormPage {
    public TaskFormPage(java.lang.String id, java.lang.String title) {
        super(id, title);
    }

    public TaskFormPage(org.eclipse.ui.forms.editor.FormEditor editor, java.lang.String id, java.lang.String title) {
        super(editor, id, title);
    }

    protected void fillToolBar(org.eclipse.jface.action.IToolBarManager toolBarManager) {
    }

    @java.lang.Override
    protected void createFormContent(org.eclipse.ui.forms.IManagedForm managedForm) {
        super.createFormContent(managedForm);
        managedForm.getToolkit().setBorderStyle(org.eclipse.swt.SWT.NULL);
        org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.initializeScrollbars(managedForm.getForm());
    }

    /**
     * Invoked when the task opened in the editor is opened while the editor was already open or if a synchronization
     * completes.
     * <p>
     * Clients may override.
     *
     * @since 3.4
     */
    protected void refresh() {
        // ignore
    }
}