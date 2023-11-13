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
package org.eclipse.mylyn.tasks.ui.wizards;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.wizards.Messages;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;
/**
 * Extend for customizing how new tasks editors are created.
 *
 * @author Steffen Pingel
 * @since 2.0
 */
public class NewTaskWizard extends org.eclipse.jface.wizard.Wizard implements org.eclipse.ui.INewWizard {
    private final org.eclipse.mylyn.tasks.core.TaskRepository taskRepository;

    private org.eclipse.mylyn.tasks.core.ITaskMapping taskSelection;

    /**
     *
     * @since 3.0
     */
    public NewTaskWizard(org.eclipse.mylyn.tasks.core.TaskRepository taskRepository, org.eclipse.mylyn.tasks.core.ITaskMapping taskSelection) {
        org.eclipse.core.runtime.Assert.isNotNull(taskRepository);
        this.taskRepository = taskRepository;
        this.taskSelection = taskSelection;
        setDefaultPageImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.BANNER_REPOSITORY);
        setNeedsProgressMonitor(true);
    }

    @java.lang.Deprecated
    public NewTaskWizard(org.eclipse.mylyn.tasks.core.TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public void init(org.eclipse.ui.IWorkbench workbench, org.eclipse.jface.viewers.IStructuredSelection selection) {
    }

    @java.lang.Override
    public void addPages() {
    }

    /**
     *
     * @since 3.0
     */
    protected org.eclipse.mylyn.tasks.core.ITaskMapping getInitializationData() {
        return null;
    }

    /**
     *
     * @since 3.0
     */
    public org.eclipse.mylyn.tasks.core.TaskRepository getTaskRepository() {
        return taskRepository;
    }

    /**
     *
     * @since 3.0
     */
    public org.eclipse.mylyn.tasks.core.ITaskMapping getTaskSelection() {
        return taskSelection;
    }

    @java.lang.Override
    public boolean performFinish() {
        final org.eclipse.mylyn.tasks.core.data.TaskData[] taskData = new org.eclipse.mylyn.tasks.core.data.TaskData[1];
        final org.eclipse.mylyn.tasks.core.ITaskMapping initializationData = getInitializationData();
        final org.eclipse.mylyn.tasks.core.ITaskMapping selectionData = getTaskSelection();
        try {
            org.eclipse.jface.operation.IRunnableWithProgress runnable = new org.eclipse.jface.operation.IRunnableWithProgress() {
                public void run(org.eclipse.core.runtime.IProgressMonitor monitor) throws java.lang.reflect.InvocationTargetException, java.lang.InterruptedException {
                    try {
                        taskData[0] = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.createTaskData(taskRepository, initializationData, selectionData, monitor);
                    } catch (org.eclipse.core.runtime.OperationCanceledException e) {
                        throw new java.lang.InterruptedException();
                    } catch (org.eclipse.core.runtime.CoreException e) {
                        throw new java.lang.reflect.InvocationTargetException(e);
                    }
                }
            };
            if (getContainer().getShell().isVisible()) {
                getContainer().run(true, true, runnable);
            } else {
                org.eclipse.ui.PlatformUI.getWorkbench().getProgressService().busyCursorWhile(runnable);
            }
        } catch (java.lang.reflect.InvocationTargetException e) {
            if (e.getCause() instanceof org.eclipse.core.runtime.CoreException) {
                org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayStatus(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.NewTaskWizard_Error_creating_new_task, ((org.eclipse.core.runtime.CoreException) (e.getCause())).getStatus());
            } else {
                org.eclipse.ui.statushandlers.StatusManager.getManager().handle(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.NewTaskWizard_Error_creating_new_task, e), org.eclipse.ui.statushandlers.StatusManager.SHOW | org.eclipse.ui.statushandlers.StatusManager.LOG);
            }
            return false;
        } catch (java.lang.InterruptedException e) {
            return false;
        }
        try {
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.createAndOpenNewTask(taskData[0]);
            return true;
        } catch (org.eclipse.core.runtime.CoreException e) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Failed to open new task", e));// $NON-NLS-1$

            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayStatus(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.NewTaskWizard_Create_Task, new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.NewTaskWizard_Failed_to_create_new_task_ + e.getMessage()));
            return false;
        }
    }
}