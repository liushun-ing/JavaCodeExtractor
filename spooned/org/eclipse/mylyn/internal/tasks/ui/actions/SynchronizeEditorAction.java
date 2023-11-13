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
package org.eclipse.mylyn.internal.tasks.ui.actions;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.core.LocalTask;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
/**
 *
 * @author Rob Elves
 * @author Steffen Pingel
 */
public class SynchronizeEditorAction extends org.eclipse.ui.actions.BaseSelectionListenerAction {
    public static final java.lang.String ID = "org.eclipse.mylyn.tasklist.actions.synchronize.editor";// $NON-NLS-1$


    public SynchronizeEditorAction() {
        super(Messages.SynchronizeEditorAction_Synchronize);
        setToolTipText(Messages.SynchronizeEditorAction_Synchronize_Incoming_Changes);
        setId(org.eclipse.mylyn.internal.tasks.ui.actions.SynchronizeEditorAction.ID);
        setImageDescriptor(org.eclipse.mylyn.commons.ui.CommonImages.REFRESH_SMALL);
        // setAccelerator(SWT.MOD1 + 'r');
    }

    @java.lang.Override
    public void run() {
        org.eclipse.jface.viewers.IStructuredSelection selection = getStructuredSelection();
        if (selection == null) {
            return;
        }
        java.lang.Object selectedObject = selection.getFirstElement();
        if (!(selectedObject instanceof org.eclipse.mylyn.tasks.ui.editors.TaskEditor)) {
            return;
        }
        final org.eclipse.mylyn.tasks.ui.editors.TaskEditor editor = ((org.eclipse.mylyn.tasks.ui.editors.TaskEditor) (selectedObject));
        final org.eclipse.mylyn.tasks.core.ITask task = editor.getTaskEditorInput().getTask();
        if (task == null) {
            return;
        }
        org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(task.getConnectorKind());
        if (connector == null) {
            return;
        }
        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.synchronizeTask(connector, task, true, new org.eclipse.core.runtime.jobs.JobChangeAdapter() {
            @java.lang.Override
            public void done(org.eclipse.core.runtime.jobs.IJobChangeEvent event) {
                org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                    public void run() {
                        try {
                            editor.refreshPages();
                        } finally {
                            if (editor != null) {
                                editor.showBusy(false);
                            }
                        }
                    }
                });
            }
        });
        if (editor != null) {
            editor.showBusy(true);
        }
    }

    @java.lang.Override
    protected boolean updateSelection(org.eclipse.jface.viewers.IStructuredSelection selection) {
        java.lang.Object selectedObject = selection.getFirstElement();
        if (selectedObject instanceof org.eclipse.mylyn.tasks.ui.editors.TaskEditor) {
            org.eclipse.mylyn.tasks.ui.editors.TaskEditor editor = ((org.eclipse.mylyn.tasks.ui.editors.TaskEditor) (selectedObject));
            org.eclipse.mylyn.tasks.core.ITask task = editor.getTaskEditorInput().getTask();
            return !(task instanceof org.eclipse.mylyn.internal.tasks.core.LocalTask);
        }
        return false;
    }
}