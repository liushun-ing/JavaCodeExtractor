/**
 * *****************************************************************************
 * Copyright (c) 2004, 2009 Willian Mitsuda and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Willian Mitsuda - initial API and implementation
 *     Tasktop Technologies - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.actions;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.actions.ActionDelegate;
/**
 *
 * @author Willian Mitsuda
 * @author Mik Kersten
 */
public class ActivateTaskDialogAction extends org.eclipse.ui.actions.ActionDelegate implements org.eclipse.ui.IWorkbenchWindowActionDelegate {
    private org.eclipse.ui.IWorkbenchWindow window;

    public void init(org.eclipse.ui.IWorkbenchWindow window) {
        this.window = window;
    }

    @java.lang.Override
    public void run(org.eclipse.jface.action.IAction action) {
        org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialogWithRandom dialog = new org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialogWithRandom(window.getShell());
        dialog.setActivateTask(true);
        dialog.setTitle(Messages.ActivateTaskDialogAction_Activate_Task);
        dialog.setMessage(Messages.ActivateTaskDialogAction_Select_a_task_to_activate__);
        if (dialog.open() != org.eclipse.jface.window.Window.OK) {
            return;
        }
        java.lang.Object result = dialog.getFirstResult();
        if (result instanceof org.eclipse.mylyn.tasks.core.ITask) {
            org.eclipse.mylyn.internal.tasks.core.AbstractTask task = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (result));
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.activateTaskThroughCommand(task);
        }
        if (org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.getFromActivePerspective() != null) {
            org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.getFromActivePerspective().refresh();
        }
    }
}