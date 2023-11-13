/**
 * *****************************************************************************
 *
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
package org.eclipse.mylyn.internal.tasks.ui.actions;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
/**
 *
 * @author Mik Kersten
 */
public class TaskActivateAction extends org.eclipse.ui.actions.BaseSelectionListenerAction implements org.eclipse.ui.IViewActionDelegate {
    public static final java.lang.String ID = "org.eclipse.mylyn.tasklist.actions.context.activate";// $NON-NLS-1$


    public TaskActivateAction() {
        super(Messages.TaskActivateAction_Activate);
        setId(org.eclipse.mylyn.internal.tasks.ui.actions.TaskActivateAction.ID);
        setActionDefinitionId("org.eclipse.mylyn.tasks.ui.command.activateSelectedTask");// $NON-NLS-1$

        setImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.CONTEXT_ACTIVE_CENTERED);
    }

    public void init(org.eclipse.ui.IViewPart view) {
        // ignore
    }

    @java.lang.Override
    public void run() {
        org.eclipse.mylyn.internal.tasks.ui.views.TaskListView taskListView = org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.getFromActivePerspective();
        if (taskListView != null) {
            run(taskListView.getSelectedTask());
        }
    }

    @java.lang.Deprecated
    public void run(org.eclipse.mylyn.tasks.core.ITask task) {
        if ((task != null) && (!task.isActive())) {
            org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().activateTask(task);
        }
    }

    public void run(org.eclipse.jface.action.IAction action) {
        run();
    }

    public void selectionChanged(org.eclipse.jface.action.IAction action, org.eclipse.jface.viewers.ISelection selection) {
        if (selection instanceof org.eclipse.jface.viewers.IStructuredSelection) {
            selectionChanged(((org.eclipse.jface.viewers.IStructuredSelection) (selection)));
        } else {
            selectionChanged(org.eclipse.jface.viewers.StructuredSelection.EMPTY);
        }
        action.setEnabled(isEnabled());
    }

    @java.lang.Override
    protected boolean updateSelection(org.eclipse.jface.viewers.IStructuredSelection selection) {
        return ((selection != null) && (selection.size() == 1)) && (selection.getFirstElement() instanceof org.eclipse.mylyn.tasks.core.ITask);
    }
}