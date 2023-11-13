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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory;
import org.eclipse.mylyn.internal.tasks.core.TaskCategory;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.commands.RemoteTaskSelectionDialog;
import org.eclipse.mylyn.internal.tasks.ui.util.TaskOpenEvent;
import org.eclipse.mylyn.internal.tasks.ui.util.TaskOpenListener;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
/**
 *
 * @author Mik Kersten
 */
public class OpenRepositoryTaskAction extends org.eclipse.jface.action.Action implements org.eclipse.ui.IWorkbenchWindowActionDelegate , org.eclipse.ui.IViewActionDelegate {
    private static final java.lang.String OPEN_REMOTE_TASK_DIALOG_DIALOG_SETTINGS = "org.eclipse.mylyn.tasks.ui.open.remote";// $NON-NLS-1$


    public void run(org.eclipse.jface.action.IAction action) {
        org.eclipse.mylyn.internal.tasks.ui.commands.RemoteTaskSelectionDialog dlg = new org.eclipse.mylyn.internal.tasks.ui.commands.RemoteTaskSelectionDialog(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell());
        dlg.setTitle(Messages.OpenRepositoryTask_Open_Repository_Task);
        org.eclipse.jface.dialogs.IDialogSettings settings = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getDialogSettings();
        org.eclipse.jface.dialogs.IDialogSettings dlgSettings = settings.getSection(org.eclipse.mylyn.internal.tasks.ui.actions.OpenRepositoryTaskAction.OPEN_REMOTE_TASK_DIALOG_DIALOG_SETTINGS);
        if (dlgSettings == null) {
            dlgSettings = settings.addNewSection(org.eclipse.mylyn.internal.tasks.ui.actions.OpenRepositoryTaskAction.OPEN_REMOTE_TASK_DIALOG_DIALOG_SETTINGS);
        }
        dlg.setDialogBoundsSettings(dlgSettings, org.eclipse.jface.dialogs.Dialog.DIALOG_PERSISTLOCATION | org.eclipse.jface.dialogs.Dialog.DIALOG_PERSISTSIZE);
        if (dlg.open() == org.eclipse.jface.window.Window.OK) {
            if (dlg.getSelectedTask() != null) {
                openExistingTask(dlg);
            } else {
                openRemoteTask(dlg);
            }
        }
    }

    /**
     * Selected a existing task; handle category move, if needed
     */
    private void openExistingTask(org.eclipse.mylyn.internal.tasks.ui.commands.RemoteTaskSelectionDialog dlg) {
        if (dlg.shouldAddToTaskList()) {
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().addTask(dlg.getSelectedTask(), dlg.getSelectedCategory());
        }
        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.refreshAndOpenTaskListElement(dlg.getSelectedTask());
    }

    /**
     * Selected a repository, so try to obtain the task using taskId
     */
    private void openRemoteTask(org.eclipse.mylyn.internal.tasks.ui.commands.RemoteTaskSelectionDialog dlg) {
        final org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory finalCategory;
        if (dlg.shouldAddToTaskList()) {
            org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory category = dlg.getSelectedCategory();
            org.eclipse.mylyn.internal.tasks.ui.views.TaskListView taskListView = org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.getFromActivePerspective();
            if (category == null) {
                java.lang.Object selectedObject = ((org.eclipse.jface.viewers.IStructuredSelection) (taskListView.getViewer().getSelection())).getFirstElement();
                if (selectedObject instanceof org.eclipse.mylyn.internal.tasks.core.TaskCategory) {
                    category = ((org.eclipse.mylyn.internal.tasks.core.TaskCategory) (selectedObject));
                }
            }
            finalCategory = category;
        } else {
            finalCategory = null;
        }
        java.lang.String[] selectedIds = dlg.getSelectedIds();
        boolean openSuccessful = false;
        for (java.lang.String id : selectedIds) {
            boolean opened = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.openTaskByIdOrKey(dlg.getSelectedTaskRepository(), id, new org.eclipse.mylyn.internal.tasks.ui.util.TaskOpenListener() {
                @java.lang.Override
                public void taskOpened(org.eclipse.mylyn.internal.tasks.ui.util.TaskOpenEvent event) {
                    if ((finalCategory != null) && (event.getTask() != null)) {
                        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().addTask(event.getTask(), finalCategory);
                    }
                }
            });
            if (opened) {
                openSuccessful = true;
            }
            if (!openSuccessful) {
                org.eclipse.jface.dialogs.MessageDialog.openInformation(org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages.OpenRepositoryTask_Open_Task, Messages.OpenRepositoryTask_Could_not_find_matching_repository_task);
            }
        }
    }

    public void dispose() {
    }

    public void init(org.eclipse.ui.IWorkbenchWindow window) {
    }

    public void selectionChanged(org.eclipse.jface.action.IAction action, org.eclipse.jface.viewers.ISelection selection) {
    }

    public void init(org.eclipse.ui.IViewPart view) {
    }
}