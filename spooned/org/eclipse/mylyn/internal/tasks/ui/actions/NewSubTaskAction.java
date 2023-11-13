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
package org.eclipse.mylyn.internal.tasks.ui.actions;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.commons.core.ICoreRunnable;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.ui.CommonUiUtil;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector;
import org.eclipse.mylyn.internal.tasks.core.LocalTask;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITask.PriorityLevel;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.progress.IProgressService;
/**
 *
 * @author Steffen Pingel
 */
public class NewSubTaskAction extends org.eclipse.ui.actions.BaseSelectionListenerAction implements org.eclipse.ui.IViewActionDelegate , org.eclipse.core.runtime.IExecutableExtension {
    public static final java.lang.String ID = "org.eclipse.mylyn.tasks.ui.new.subtask";// $NON-NLS-1$


    private org.eclipse.mylyn.internal.tasks.core.AbstractTask selectedTask;

    public NewSubTaskAction() {
        super(Messages.NewSubTaskAction_Subtask);
        setToolTipText(Messages.NewSubTaskAction_Create_a_new_subtask);
        setId(org.eclipse.mylyn.internal.tasks.ui.actions.NewSubTaskAction.ID);
        setImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.TASK_NEW_SUB);
    }

    @java.lang.Override
    public void run() {
        if (selectedTask == null) {
            return;
        }
        if (selectedTask instanceof org.eclipse.mylyn.internal.tasks.core.LocalTask) {
            // XXX code copied from NewLocalTaskWizard.performFinish() and TaskListManager.createNewLocalTask()
            org.eclipse.mylyn.internal.tasks.core.TaskList taskList = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList();
            org.eclipse.mylyn.internal.tasks.core.LocalTask newTask = // $NON-NLS-1$
            new org.eclipse.mylyn.internal.tasks.core.LocalTask("" + taskList.getNextLocalTaskId(), org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector.DEFAULT_SUMMARY);
            newTask.setPriority(org.eclipse.mylyn.tasks.core.ITask.PriorityLevel.P3.toString());
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.scheduleNewTask(newTask);
            taskList.addTask(newTask, selectedTask);
            org.eclipse.mylyn.tasks.ui.TasksUiUtil.openTask(newTask);
            return;
        }
        org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(selectedTask.getConnectorKind());
        org.eclipse.jface.wizard.IWizard wizard = getNewSubTaskWizard();
        if (wizard != null) {
            org.eclipse.jface.wizard.WizardDialog dialog = new org.eclipse.jface.wizard.WizardDialog(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell(), wizard);
            dialog.setBlockOnOpen(true);
            dialog.open();
            return;
        }
        org.eclipse.mylyn.tasks.core.data.TaskData taskData = createTaskData(connector);
        if (taskData != null) {
            try {
                org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.createAndOpenNewTask(taskData);
            } catch (org.eclipse.core.runtime.CoreException e) {
                org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Failed to open new sub task", e));// $NON-NLS-1$

                org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayStatus(Messages.NewSubTaskAction_Unable_to_create_subtask, new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, Messages.NewSubTaskAction_Failed_to_create_new_sub_task_ + e.getMessage()));
            }
        }
    }

    private org.eclipse.jface.wizard.IWizard getNewSubTaskWizard() {
        org.eclipse.mylyn.tasks.core.TaskRepository repository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(selectedTask.getConnectorKind(), selectedTask.getRepositoryUrl());
        org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryConnectorUi(selectedTask.getConnectorKind());
        return connectorUi.getNewSubTaskWizard(repository, selectedTask);
    }

    private org.eclipse.mylyn.tasks.core.data.TaskData createTaskData(org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector) {
        final org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler taskDataHandler = connector.getTaskDataHandler();
        if (taskDataHandler == null) {
            return null;
        }
        java.lang.String repositoryUrl = selectedTask.getRepositoryUrl();
        org.eclipse.mylyn.tasks.core.data.TaskData parentTaskData = null;
        try {
            parentTaskData = org.eclipse.mylyn.tasks.ui.TasksUi.getTaskDataManager().getTaskData(selectedTask);
        } catch (org.eclipse.core.runtime.CoreException e) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Could not retrieve task data for task:" + selectedTask.getUrl(), e));// $NON-NLS-1$

        }
        if (parentTaskData == null) {
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayStatus(Messages.NewSubTaskAction_Unable_to_create_subtask, new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.WARNING, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, Messages.NewSubTaskAction_Could_not_retrieve_task_data_for_task_ + selectedTask.getUrl()));
            return null;
        }
        final org.eclipse.mylyn.tasks.core.TaskRepository taskRepository = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryManager().getRepository(repositoryUrl);
        if (!taskDataHandler.canInitializeSubTaskData(taskRepository, selectedTask)) {
            return null;
        }
        final org.eclipse.mylyn.tasks.core.data.TaskData selectedTaskData = parentTaskData;
        final org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper attributeMapper = taskDataHandler.getAttributeMapper(taskRepository);
        final org.eclipse.mylyn.tasks.core.data.TaskData taskData = new org.eclipse.mylyn.tasks.core.data.TaskData(attributeMapper, taskRepository.getConnectorKind(), taskRepository.getRepositoryUrl(), "");// $NON-NLS-1$

        final boolean[] result = new boolean[1];
        org.eclipse.ui.progress.IProgressService service = org.eclipse.ui.PlatformUI.getWorkbench().getProgressService();
        try {
            org.eclipse.mylyn.commons.ui.CommonUiUtil.run(service, new org.eclipse.mylyn.commons.core.ICoreRunnable() {
                public void run(org.eclipse.core.runtime.IProgressMonitor monitor) throws org.eclipse.core.runtime.CoreException {
                    result[0] = taskDataHandler.initializeSubTaskData(taskRepository, taskData, selectedTaskData, monitor);
                }
            });
        } catch (org.eclipse.core.runtime.CoreException e) {
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayStatus(Messages.NewSubTaskAction_Unable_to_create_subtask, e.getStatus());
            return null;
        } catch (org.eclipse.core.runtime.OperationCanceledException e) {
            // canceled
            return null;
        }
        if (result[0]) {
            // open editor
            return taskData;
        } else {
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayStatus(Messages.NewSubTaskAction_Unable_to_create_subtask, new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.INFO, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, Messages.NewSubTaskAction_The_connector_does_not_support_creating_subtasks_for_this_task));
        }
        return null;
    }

    public void run(org.eclipse.jface.action.IAction action) {
        run();
    }

    public void init(org.eclipse.ui.IViewPart view) {
    }

    @java.lang.Override
    protected boolean updateSelection(org.eclipse.jface.viewers.IStructuredSelection selection) {
        selectedTask = null;
        if (selection.size() == 1) {
            java.lang.Object selectedObject = selection.getFirstElement();
            if (selectedObject instanceof org.eclipse.mylyn.internal.tasks.core.LocalTask) {
                selectedTask = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (selectedObject));
            } else if (selectedObject instanceof org.eclipse.mylyn.tasks.core.ITask) {
                selectedTask = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (selectedObject));
                org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(selectedTask.getConnectorKind());
                org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler taskDataHandler = connector.getTaskDataHandler();
                org.eclipse.mylyn.tasks.core.TaskRepository repository = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryManager().getRepository(selectedTask.getRepositoryUrl());
                if ((taskDataHandler == null) || (!taskDataHandler.canInitializeSubTaskData(repository, selectedTask))) {
                    selectedTask = null;
                }
            }
        }
        return selectedTask != null;
    }

    public void selectionChanged(org.eclipse.jface.action.IAction action, org.eclipse.jface.viewers.ISelection selection) {
        if (selection instanceof org.eclipse.jface.viewers.StructuredSelection) {
            selectionChanged(((org.eclipse.jface.viewers.IStructuredSelection) (selection)));
        } else {
            setEnabled(false);
        }
        action.setEnabled(isEnabled());
    }

    public void setInitializationData(org.eclipse.core.runtime.IConfigurationElement config, java.lang.String propertyName, java.lang.Object data) throws org.eclipse.core.runtime.CoreException {
    }
}