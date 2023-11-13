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
package org.eclipse.mylyn.internal.tasks.ui;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.commons.ui.CommonUiUtil;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.TaskActivationHistory;
import org.eclipse.mylyn.internal.tasks.ui.actions.ActivateTaskDialogAction;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskContainer;
import org.eclipse.mylyn.tasks.ui.TaskElementLabelProvider;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
/**
 *
 * @author Wesley Coelho
 * @author Mik Kersten
 * @author Leo Dos Santos
 * @author Steffen Pingel
 * @author Sam Davis
 */
public class TaskHistoryDropDown extends org.eclipse.ui.actions.CompoundContributionItem {
    private static final int MAX_ITEMS_TO_DISPLAY = 16;

    private class ActivateDialogAction extends org.eclipse.jface.action.Action {
        private final org.eclipse.mylyn.internal.tasks.ui.actions.ActivateTaskDialogAction dialogAction;

        public ActivateDialogAction(org.eclipse.mylyn.internal.tasks.ui.actions.ActivateTaskDialogAction action) {
            dialogAction = action;
            dialogAction.init(org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow());
            setText(Messages.TaskHistoryDropDown_Activate_Task_);
            setToolTipText(Messages.TaskHistoryDropDown_Activate_Task_);
            setEnabled(true);
            setChecked(false);
            setImageDescriptor(null);
            // TasksUiImages.TASK_ACTIVE);
        }

        @java.lang.Override
        public void run() {
            dialogAction.run(null);
        }
    }

    private class DeactivateTaskAction extends org.eclipse.jface.action.Action {
        public DeactivateTaskAction() {
            setText(Messages.TaskHistoryDropDown_Deactivate_Task);
            setToolTipText(Messages.TaskHistoryDropDown_Deactivate_Task);
            setEnabled(true);
            setChecked(false);
            setImageDescriptor(null);
            // TasksUiImages.TASK_INACTIVE);
        }

        @java.lang.Override
        public void run() {
            org.eclipse.mylyn.tasks.core.ITask active = org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().getActiveTask();
            if (active != null) {
                org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().deactivateTask(active);
            }
        }
    }

    /**
     * Action for navigating to a specified task. This class should be protected but has been made public for testing
     * only
     */
    private class ActivateTaskAction extends org.eclipse.jface.action.Action {
        private static final int MAX_LABEL_LENGTH = 40;

        private final org.eclipse.mylyn.internal.tasks.core.AbstractTask targetTask;

        public ActivateTaskAction(org.eclipse.mylyn.internal.tasks.core.AbstractTask task) {
            targetTask = task;
            java.lang.String taskDescription = task.getSummary();
            if (taskDescription.length() > org.eclipse.mylyn.internal.tasks.ui.TaskHistoryDropDown.ActivateTaskAction.MAX_LABEL_LENGTH) {
                taskDescription = taskDescription.subSequence(0, org.eclipse.mylyn.internal.tasks.ui.TaskHistoryDropDown.ActivateTaskAction.MAX_LABEL_LENGTH - 3) + "...";// $NON-NLS-1$

            }
            taskDescription = org.eclipse.mylyn.commons.ui.CommonUiUtil.toMenuLabel(taskDescription);
            setText(taskDescription);
            setEnabled(true);
            setToolTipText(task.getSummary());
            org.eclipse.swt.graphics.Image image = labelProvider.getImage(task);
            setImageDescriptor(org.eclipse.jface.resource.ImageDescriptor.createFromImage(image));
        }

        @java.lang.Override
        public void run() {
            if (targetTask.isActive()) {
                return;
            }
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.activateTaskThroughCommand(targetTask);
        }

        @java.lang.Override
        public void runWithEvent(org.eclipse.swt.widgets.Event event) {
            run();
            if ((event.stateMask & org.eclipse.swt.SWT.SHIFT) != 0) {
                org.eclipse.mylyn.tasks.ui.TasksUiUtil.openTask(targetTask);
            }
        }
    }

    private final org.eclipse.mylyn.tasks.ui.TaskElementLabelProvider labelProvider = new org.eclipse.mylyn.tasks.ui.TaskElementLabelProvider(false);

    private boolean scopedToWorkingSet;

    private final org.eclipse.mylyn.internal.tasks.core.TaskActivationHistory taskHistory;

    public TaskHistoryDropDown() {
        this(null);
    }

    public TaskHistoryDropDown(java.lang.String id) {
        this(id, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().getTaskActivationHistory());
    }

    public TaskHistoryDropDown(java.lang.String id, org.eclipse.mylyn.internal.tasks.core.TaskActivationHistory taskHistory) {
        super(id);
        this.taskHistory = taskHistory;
    }

    @java.lang.Override
    protected org.eclipse.jface.action.IContributionItem[] getContributionItems() {
        java.util.List<org.eclipse.mylyn.internal.tasks.core.AbstractTask> tasks = new java.util.ArrayList<org.eclipse.mylyn.internal.tasks.core.AbstractTask>(taskHistory.getPreviousTasks());
        java.util.Set<org.eclipse.ui.IWorkingSet> sets = org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater.getActiveWorkingSets(org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow());
        if (scopedToWorkingSet && (!sets.isEmpty())) {
            java.util.Set<org.eclipse.mylyn.tasks.core.ITask> allWorkingSetTasks = new java.util.HashSet<org.eclipse.mylyn.tasks.core.ITask>();
            for (org.eclipse.ui.IWorkingSet workingSet : sets) {
                org.eclipse.core.runtime.IAdaptable[] elements = workingSet.getElements();
                for (org.eclipse.core.runtime.IAdaptable adaptable : elements) {
                    if (adaptable instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) {
                        allWorkingSetTasks.addAll(((org.eclipse.mylyn.tasks.core.ITaskContainer) (adaptable)).getChildren());
                    }
                }
            }
            java.util.List<org.eclipse.mylyn.internal.tasks.core.AbstractTask> allScopedTasks = new java.util.ArrayList<org.eclipse.mylyn.internal.tasks.core.AbstractTask>(tasks);
            for (org.eclipse.mylyn.tasks.core.ITask task : tasks) {
                if (!allWorkingSetTasks.contains(task)) {
                    allScopedTasks.remove(task);
                }
            }
            tasks = allScopedTasks;
        }
        if (tasks.size() > org.eclipse.mylyn.internal.tasks.ui.TaskHistoryDropDown.MAX_ITEMS_TO_DISPLAY) {
            tasks = tasks.subList(tasks.size() - org.eclipse.mylyn.internal.tasks.ui.TaskHistoryDropDown.MAX_ITEMS_TO_DISPLAY, tasks.size());
        }
        java.util.List<org.eclipse.jface.action.IContributionItem> items = new java.util.ArrayList<org.eclipse.jface.action.IContributionItem>();
        for (int i = tasks.size() - 1; i >= 0; i--) {
            org.eclipse.mylyn.internal.tasks.core.AbstractTask currTask = tasks.get(i);
            org.eclipse.jface.action.Action taskNavAction = new org.eclipse.mylyn.internal.tasks.ui.TaskHistoryDropDown.ActivateTaskAction(currTask);
            org.eclipse.jface.action.ActionContributionItem item = new org.eclipse.jface.action.ActionContributionItem(taskNavAction);
            if (currTask.isActive()) {
                taskNavAction.setChecked(true);
            }
            items.add(item);
        }
        if (items.size() > 0) {
            org.eclipse.jface.action.Separator separator = new org.eclipse.jface.action.Separator();
            items.add(separator);
        }
        final org.eclipse.mylyn.tasks.core.ITask active = org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().getActiveTask();
        if (active != null) {
            org.eclipse.jface.action.Action deactivateAction = new org.eclipse.mylyn.internal.tasks.ui.TaskHistoryDropDown.DeactivateTaskAction();
            org.eclipse.jface.action.ActionContributionItem item = new org.eclipse.jface.action.ActionContributionItem(deactivateAction);
            items.add(item);
            items.add(new org.eclipse.jface.action.ActionContributionItem(new org.eclipse.jface.action.Action(Messages.TaskHistoryDropDown_Open_Active_Task) {
                @java.lang.Override
                public void run() {
                    org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.openTask(active, active.getTaskId());
                }
            }));
        } else {
            org.eclipse.jface.action.Action activateDialogAction = new org.eclipse.mylyn.internal.tasks.ui.TaskHistoryDropDown.ActivateDialogAction(new org.eclipse.mylyn.internal.tasks.ui.actions.ActivateTaskDialogAction());
            org.eclipse.jface.action.ActionContributionItem item = new org.eclipse.jface.action.ActionContributionItem(activateDialogAction);
            items.add(item);
        }
        return items.toArray(new org.eclipse.jface.action.IContributionItem[items.size()]);
    }

    public boolean isScopedToWorkingSet() {
        return scopedToWorkingSet;
    }

    /**
     * If <code>scopedToWorkingSet</code> is set to true only tasks from the current working set are contributed.
     */
    public void setScopedToWorkingSet(boolean scopedToWorkingSet) {
        this.scopedToWorkingSet = scopedToWorkingSet;
    }
}