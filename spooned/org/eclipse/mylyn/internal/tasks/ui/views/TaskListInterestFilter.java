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
package org.eclipse.mylyn.internal.tasks.ui.views;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector;
import org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil;
import org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.Completed;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.Incoming;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.Outgoing;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.Unscheduled;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskContainer;
/**
 * Goal is to have this reuse as much of the super as possible.
 *
 * @author Mik Kersten
 * @author Rob Elves
 */
public class TaskListInterestFilter extends org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter {
    @java.lang.Override
    public boolean select(java.lang.Object parent, java.lang.Object child) {
        try {
            if (child instanceof org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer) {
                org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer dateRangeTaskContainer = ((org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer) (child));
                return isDateRangeInteresting(dateRangeTaskContainer);
            }
            if (child instanceof org.eclipse.mylyn.tasks.core.ITask) {
                org.eclipse.mylyn.internal.tasks.core.AbstractTask task = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (child));
                return isInteresting(parent, task);
            }
            if (child instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) {
                java.util.Collection<org.eclipse.mylyn.tasks.core.ITask> children = ((org.eclipse.mylyn.tasks.core.ITaskContainer) (child)).getChildren();
                // Always display empty containers
                if (children.size() == 0) {
                    return false;
                }
                for (org.eclipse.mylyn.tasks.core.ITask task : children) {
                    if (shouldAlwaysShow(child, ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (task)), org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.MAX_SUBTASK_DEPTH)) {
                        return true;
                    }
                }
            }
        } catch (java.lang.Throwable t) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Interest filter failed", t));// $NON-NLS-1$

        }
        return false;
    }

    private boolean isDateRangeInteresting(org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer scheduleContainer) {
        if (scheduleContainer instanceof org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.Unscheduled) {
            return false;
        }
        if ((scheduleContainer instanceof org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.Incoming) || (scheduleContainer instanceof org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.Outgoing)) {
            return hasChildren(scheduleContainer, scheduleContainer.getChildren());
        }
        if (org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil.getCurrentWeek().isCurrentWeekDay(scheduleContainer.getDateRange())) {
            if (scheduleContainer.isPresent() || scheduleContainer.isFuture()) {
                return true;
            }
        } else /* && scheduleContainer.isCaptureFloating() */
        if (scheduleContainer.isPresent()) {
            return true;
        }
        return false;
    }

    private boolean hasChildren(java.lang.Object parent, java.util.Collection<org.eclipse.mylyn.tasks.core.ITask> children) {
        for (org.eclipse.mylyn.tasks.core.ITask task : children) {
            if (org.eclipse.mylyn.internal.tasks.ui.views.PresentationFilter.getInstance().select(parent, task)) {
                return true;
            }
        }
        return false;
    }

    // TODO: make meta-context more explicit
    protected boolean isInteresting(java.lang.Object parent, org.eclipse.mylyn.internal.tasks.core.AbstractTask task) {
        return shouldAlwaysShow(parent, task, org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.MAX_SUBTASK_DEPTH);
    }

    public boolean shouldAlwaysShow(java.lang.Object parent, org.eclipse.mylyn.internal.tasks.core.AbstractTask task, int depth) {
        if (!org.eclipse.mylyn.internal.tasks.ui.views.PresentationFilter.getInstance().select(parent, task)) {
            return false;
        }
        return (((task.isActive() || org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().isCompletedToday(task)) || hasChanges(parent, task)) || hasInterestingSubTasks(parent, task, depth)) || // note that following condition is wrapped in ()!
        ((!task.isCompleted()) && (((org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector.DEFAULT_SUMMARY.equals(task.getSummary()) || shouldShowInFocusedWorkweekDateContainer(parent, task)) || org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().isOverdue(task)) || isInterestingForThisWeek(parent, task)));
    }

    private boolean hasInterestingSubTasks(java.lang.Object parent, org.eclipse.mylyn.internal.tasks.core.AbstractTask task, int depth) {
        if (depth > 0) {
            if (!org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().groupSubtasks(task)) {
                return false;
            }
            if ((task.getChildren() != null) && (task.getChildren().size() > 0)) {
                for (org.eclipse.mylyn.tasks.core.ITask subTask : task.getChildren()) {
                    if (shouldAlwaysShow(parent, ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (subTask)), depth - 1)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean shouldShowInFocusedWorkweekDateContainer(java.lang.Object parent, org.eclipse.mylyn.tasks.core.ITask task) {
        if (parent instanceof org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.Unscheduled) {
            return false;
        }
        if (parent instanceof org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.Incoming) {
            return true;
        }
        if (parent instanceof org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.Outgoing) {
            return true;
        }
        if (parent instanceof org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.Completed) {
            return true;
        }
        if (parent instanceof org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer) {
            return isDateRangeInteresting(((org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer) (parent)));
        }
        return false;
    }

    public boolean isInterestingForThisWeek(java.lang.Object parent, org.eclipse.mylyn.internal.tasks.core.AbstractTask task) {
        if (parent instanceof org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer) {
            return shouldShowInFocusedWorkweekDateContainer(parent, task);
        } else {
            return (((org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().isPastReminder(task) || org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().isSheduledForPastWeek(task)) || org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().isScheduledForThisWeek(task)) || org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().isDueThisWeek(task)) || org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().isScheduledForToday(task);
        }
    }

    public boolean hasChanges(java.lang.Object parent, org.eclipse.mylyn.tasks.core.ITask task) {
        if ((parent instanceof org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer) && (!(parent instanceof org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.Unscheduled))) {
            if (!shouldShowInFocusedWorkweekDateContainer(parent, task)) {
                return false;
            }
        }
        return hasChangesHelper(parent, task);
    }

    private boolean hasChangesHelper(java.lang.Object parent, org.eclipse.mylyn.tasks.core.ITask task) {
        if (task.getSynchronizationState().isOutgoing()) {
            return true;
        } else if (org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.shouldShowIncoming(task)) {
            return true;
        }
        if (task instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) {
            for (org.eclipse.mylyn.tasks.core.ITask child : ((org.eclipse.mylyn.tasks.core.ITaskContainer) (task)).getChildren()) {
                if (org.eclipse.mylyn.internal.tasks.ui.views.PresentationFilter.getInstance().select(task, child) && hasChangesHelper(parent, child)) {
                    return true;
                }
            }
        }
        return false;
    }
}