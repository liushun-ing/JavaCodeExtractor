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
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.ITaskListChangeListener;
import org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil;
import org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta;
import org.eclipse.mylyn.internal.tasks.ui.ScheduleTaskMenuContributor;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskListChangeAdapter;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
/**
 * <p>
 * <b>Note:</b> this action must be disposed.
 *
 * @author Mik Kersten
 * @author Steffen Pingel
 */
public class TaskEditorScheduleAction extends org.eclipse.jface.action.Action implements org.eclipse.jface.action.IMenuCreator {
    private final org.eclipse.mylyn.tasks.core.ITask task;

    private org.eclipse.jface.action.MenuManager menuManager;

    private final org.eclipse.mylyn.internal.tasks.ui.ScheduleTaskMenuContributor scheduleMenuContributor = new org.eclipse.mylyn.internal.tasks.ui.ScheduleTaskMenuContributor();

    private final org.eclipse.mylyn.internal.tasks.core.ITaskListChangeListener TASK_LIST_LISTENER = new org.eclipse.mylyn.internal.tasks.ui.editors.TaskListChangeAdapter() {
        @java.lang.Override
        public void containersChanged(java.util.Set<org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta> containers) {
            for (org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta taskContainerDelta : containers) {
                if (taskContainerDelta.getElement() instanceof org.eclipse.mylyn.tasks.core.ITask) {
                    final org.eclipse.mylyn.internal.tasks.core.AbstractTask updateTask = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (taskContainerDelta.getElement()));
                    if (task.equals(updateTask)) {
                        if ((org.eclipse.ui.PlatformUI.getWorkbench() != null) && (!org.eclipse.ui.PlatformUI.getWorkbench().isClosing())) {
                            org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                                public void run() {
                                    updateImageDescriptor();
                                }
                            });
                        }
                    }
                }
            }
        }
    };

    public TaskEditorScheduleAction(org.eclipse.mylyn.tasks.core.ITask task) {
        org.eclipse.core.runtime.Assert.isNotNull(task);
        this.task = task;
        updateImageDescriptor();
        setMenuCreator(this);
        setToolTipText(Messages.TaskEditorScheduleAction_Private_Scheduling);
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().addChangeListener(TASK_LIST_LISTENER);
    }

    @java.lang.Override
    public void run() {
        if (((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (task)).getScheduledForDate() == null) {
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().addTaskIfAbsent(task);
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().setScheduledFor(((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (task)), org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil.getCurrentWeek().getToday());
        } else {
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().setScheduledFor(((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (task)), null);
        }
    }

    public void updateImageDescriptor() {
        if ((task instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask) && (((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (task)).getScheduledForDate() != null)) {
            setImageDescriptor(org.eclipse.mylyn.commons.ui.CommonImages.SCHEDULE_DAY);
        } else {
            setImageDescriptor(org.eclipse.mylyn.commons.ui.CommonImages.SCHEDULE);
        }
        setEnabled(!task.isCompleted());
    }

    public org.eclipse.swt.widgets.Menu getMenu(org.eclipse.swt.widgets.Control parent) {
        if (menuManager != null) {
            menuManager.dispose();
        }
        menuManager = scheduleMenuContributor.getSubMenuManager(java.util.Collections.singletonList(((org.eclipse.mylyn.tasks.core.IRepositoryElement) (task))));
        menuManager.createContextMenu(parent);
        return menuManager.getMenu();
    }

    public org.eclipse.swt.widgets.Menu getMenu(org.eclipse.swt.widgets.Menu parent) {
        if (menuManager != null) {
            return menuManager.getMenu();
        }
        return null;
    }

    public void dispose() {
        if (menuManager != null) {
            menuManager.dispose();
        }
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().removeChangeListener(TASK_LIST_LISTENER);
    }
}