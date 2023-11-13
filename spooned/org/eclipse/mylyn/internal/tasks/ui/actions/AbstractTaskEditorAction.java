/**
 * *****************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.actions;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITask.SynchronizationState;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
/**
 *
 * @author Rob Elves
 */
public abstract class AbstractTaskEditorAction extends org.eclipse.ui.actions.BaseSelectionListenerAction {
    protected org.eclipse.mylyn.tasks.ui.editors.TaskEditor editor;

    public AbstractTaskEditorAction(java.lang.String text) {
        super(text);
    }

    public void setEditor(org.eclipse.mylyn.tasks.ui.editors.TaskEditor taskEditor) {
        this.editor = taskEditor;
    }

    protected boolean taskDirty(org.eclipse.mylyn.tasks.core.ITask task) {
        return (((editor != null) && editor.isDirty()) || task.getSynchronizationState().equals(org.eclipse.mylyn.tasks.core.ITask.SynchronizationState.OUTGOING)) || task.getSynchronizationState().equals(org.eclipse.mylyn.tasks.core.ITask.SynchronizationState.CONFLICT);
    }

    protected void openInformationDialog(java.lang.String label, java.lang.String text) {
        org.eclipse.swt.widgets.Shell shell = null;
        if (editor != null) {
            shell = editor.getSite().getShell();
        }
        org.eclipse.jface.dialogs.MessageDialog.openInformation(shell, label, text);
        if (editor != null) {
            editor.showBusy(false);
        }
        return;
    }
}