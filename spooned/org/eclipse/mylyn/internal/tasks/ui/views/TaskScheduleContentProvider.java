/**
 * *****************************************************************************
 * Copyright (c) 2004, 2013 Tasktop Technologies and others.
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.DateRange;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.TaskActivityManager;
import org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil;
import org.eclipse.mylyn.internal.tasks.core.WeekDateRange;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITask.SynchronizationState;
import org.eclipse.mylyn.tasks.core.ITaskActivityListener;
import org.eclipse.ui.PlatformUI;
/**
 * Used by Scheduled task list presentation
 *
 * @author Rob Elves
 */
public class TaskScheduleContentProvider extends org.eclipse.mylyn.internal.tasks.ui.views.TaskListContentProvider implements org.eclipse.mylyn.tasks.core.ITaskActivityListener {
    private final org.eclipse.mylyn.internal.tasks.core.TaskActivityManager taskActivityManager;

    private final org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.Unscheduled unscheduled;

    private final java.util.Calendar END_OF_TIME;

    private final java.util.Calendar INCOMING_TIME;

    private final java.util.Calendar OUTGOING_TIME;

    private final java.util.Calendar COMPLETED_TIME;

    private org.eclipse.core.runtime.jobs.Job rolloverJob;

    private final org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.Incoming incoming;

    private final org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.Outgoing outgoing;

    private final org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.Completed completed;

    public TaskScheduleContentProvider(org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListView taskListView) {
        super(taskListView);
        this.taskActivityManager = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager();
        taskActivityManager.addActivityListener(this);
        END_OF_TIME = org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil.getCalendar();
        END_OF_TIME.add(java.util.Calendar.YEAR, 5000);
        END_OF_TIME.getTime();
        unscheduled = new org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.Unscheduled();
        INCOMING_TIME = org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil.getCalendar();
        INCOMING_TIME.setTimeInMillis(END_OF_TIME.getTimeInMillis() - 1);
        incoming = new org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.Incoming();
        OUTGOING_TIME = org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil.getCalendar();
        // OUTGOING_TIME.setTimeInMillis(TaskActivityUtil.getCurrentWeek().getToday().getStartDate().getTimeInMillis() - 1);
        OUTGOING_TIME.setTimeInMillis(END_OF_TIME.getTimeInMillis() - 2);
        outgoing = new org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.Outgoing();
        COMPLETED_TIME = org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil.getCalendar();
        COMPLETED_TIME.setTimeInMillis(END_OF_TIME.getTimeInMillis() + 2);
        completed = new org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.Completed();
    }

    @java.lang.Override
    public java.lang.Object[] getElements(java.lang.Object parent) {
        if ((parent != null) && parent.equals(this.taskListView.getViewSite())) {
            java.util.Set<org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer> containers = new java.util.HashSet<org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer>();
            org.eclipse.mylyn.internal.tasks.core.WeekDateRange week = org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil.getCurrentWeek();
            org.eclipse.mylyn.internal.tasks.core.WeekDateRange nextWeek = org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil.getNextWeek();
            synchronized(this) {
                if (rolloverJob != null) {
                    rolloverJob.cancel();
                    rolloverJob = null;
                }
                long delay = week.getToday().getEndDate().getTime().getTime() - new java.util.Date().getTime();
                rolloverJob = new org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.RolloverCheck();
                rolloverJob.setSystem(true);
                rolloverJob.setPriority(org.eclipse.core.runtime.jobs.Job.SHORT);
                rolloverJob.schedule(delay);
            }
            for (org.eclipse.mylyn.internal.tasks.core.DateRange day : week.getRemainingDays()) {
                containers.add(new org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager(), day));
            }
            // This Week
            containers.add(new org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager(), week));
            for (org.eclipse.mylyn.internal.tasks.core.DateRange day : nextWeek.getDaysOfWeek()) {
                containers.add(new org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager(), day));
            }
            org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer nextWeekContainer = new org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer(taskActivityManager, nextWeek);
            containers.add(nextWeekContainer);
            org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer twoWeeksContainer = new org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer(taskActivityManager, week.next().next(), Messages.TaskScheduleContentProvider_Two_Weeks);
            containers.add(twoWeeksContainer);
            java.util.Calendar startDate = org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil.getCalendar();
            startDate.setTimeInMillis(twoWeeksContainer.getEnd().getTimeInMillis());
            org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil.snapNextDay(startDate);
            java.util.Calendar endDate = org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil.getCalendar();
            endDate.add(java.util.Calendar.YEAR, 4999);
            org.eclipse.mylyn.internal.tasks.core.DateRange future = new org.eclipse.mylyn.internal.tasks.core.DateRange(startDate, endDate);
            org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer futureContainer = new org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer(taskActivityManager, future, Messages.TaskScheduleContentProvider_Future, Messages.TaskScheduleContentProvider_Future);
            containers.add(futureContainer);
            // Outgoing
            containers.add(outgoing);
            // Incoming
            containers.add(incoming);
            // Unscheduled
            containers.add(unscheduled);
            // Completed
            containers.add(completed);
            return applyFilter(containers).toArray();
        } else {
            return getChildren(parent);
        }
    }

    @java.lang.Override
    public java.lang.Object getParent(java.lang.Object child) {
        // for (Object o : getElements(null)) {
        // ScheduledTaskContainer container = ((ScheduledTaskContainer) o);
        // if (container.getChildren().contains(((ITask) child).getHandleIdentifier())) {
        // return container;
        // }
        // }
        return null;
    }

    @java.lang.Override
    public boolean hasChildren(java.lang.Object parent) {
        return getChildren(parent).length > 0;
    }

    @java.lang.Override
    protected java.util.List<org.eclipse.mylyn.tasks.core.IRepositoryElement> getFilteredChildrenFor(java.lang.Object parent) {
        if (parent instanceof org.eclipse.mylyn.tasks.core.ITask) {
            // flat presentation (no subtasks revealed in Scheduled mode)
            return java.util.Collections.emptyList();
        } else if (parent instanceof org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer) {
            // always apply working set filter
            java.util.Collection<org.eclipse.mylyn.tasks.core.ITask> children = ((org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer) (parent)).getChildren();
            java.util.List<org.eclipse.mylyn.tasks.core.IRepositoryElement> result = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.IRepositoryElement>(children.size());
            for (org.eclipse.mylyn.tasks.core.ITask child : children) {
                if (!filter(parent, child)) {
                    result.add(child);
                }
            }
            return result;
        }
        return super.getFilteredChildrenFor(parent);
    }

    // @Override
    // public Object[] getChildren(Object parent) {
    // Set<ITask> result = new HashSet<ITask>();
    // if (parent instanceof ITask) {
    // // flat presentation (no subtasks revealed in Scheduled mode)
    // } else if (parent instanceof ScheduledTaskContainer) {
    // for (ITask child : ((ScheduledTaskContainer) parent).getChildren()) {
    // 
    // if (!filter(parent, child)) {
    // if (((AbstractTask) child).getTaskId().equals("143577")) {
    // System.err.println();
    // }
    // result.add(child);
    // }
    // }
    // } else if (parent instanceof ITaskContainer) {
    // for (ITask child : ((ITaskContainer) parent).getChildren()) {
    // result.add(child);
    // }
    // }
    // return result.toArray();
    // }
    private void refresh() {
        if ((org.eclipse.core.runtime.Platform.isRunning() && (org.eclipse.ui.PlatformUI.getWorkbench() != null)) && (!org.eclipse.ui.PlatformUI.getWorkbench().isClosing())) {
            org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                public void run() {
                    taskListView.refresh();
                }
            });
        }
    }

    @java.lang.Override
    public void dispose() {
        synchronized(this) {
            if (rolloverJob != null) {
                rolloverJob.cancel();
            }
        }
        taskActivityManager.removeActivityListener(this);
        super.dispose();
    }

    public void activityReset() {
        refresh();
    }

    public void elapsedTimeUpdated(org.eclipse.mylyn.tasks.core.ITask task, long newElapsedTime) {
        // ignore
    }

    private static boolean notScheduled(org.eclipse.mylyn.tasks.core.ITask task) {
        org.eclipse.mylyn.tasks.core.ITask.SynchronizationState state = task.getSynchronizationState();
        return (state == org.eclipse.mylyn.tasks.core.ITask.SynchronizationState.SYNCHRONIZED) || ((state == org.eclipse.mylyn.tasks.core.ITask.SynchronizationState.INCOMING) && java.lang.Boolean.parseBoolean(task.getAttribute(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.ATTRIBUTE_TASK_SUPPRESS_INCOMING)));
    }

    public class Unscheduled extends org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.StateTaskContainer {
        public Unscheduled() {
            super(new org.eclipse.mylyn.internal.tasks.core.DateRange(END_OF_TIME), Messages.TaskScheduleContentProvider_Unscheduled);
        }

        @java.lang.Override
        protected boolean select(org.eclipse.mylyn.tasks.core.ITask task) {
            return (!task.isCompleted()) && org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.notScheduled(task);
        }
    }

    public abstract class StateTaskContainer extends org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer {
        java.util.Calendar temp = org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil.getCalendar();

        public StateTaskContainer(org.eclipse.mylyn.internal.tasks.core.DateRange range, java.lang.String summary) {
            super(taskActivityManager, range, summary, summary);
        }

        @java.lang.Override
        public java.util.Collection<org.eclipse.mylyn.tasks.core.ITask> getChildren() {
            java.util.Set<org.eclipse.mylyn.tasks.core.ITask> children = new java.util.HashSet<org.eclipse.mylyn.tasks.core.ITask>();
            for (org.eclipse.mylyn.tasks.core.ITask task : org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().getAllTasks()) {
                if (select(task) && include(task)) {
                    children.add(task);
                }
            }
            return children;
        }

        private boolean include(org.eclipse.mylyn.tasks.core.ITask task) {
            // ensure that completed tasks always show somewhere
            if (task.isCompleted()) {
                return true;
            }
            org.eclipse.mylyn.internal.tasks.core.DateRange scheduledForDate = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (task)).getScheduledForDate();
            // in focused mode fewer container are displayed, include additional tasks
            if (taskListView.isFocusedMode()) {
                if (scheduledForDate != null) {
                    if (org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil.isAfterCurrentWeek(scheduledForDate.getStartDate())) {
                        // scheduled for next week or later
                        return true;
                    } else {
                        // scheduled for this week or earlier
                        return false;
                    }
                } else if ((task.getDueDate() != null) && taskActivityManager.isOwnedByUser(task)) {
                    temp.setTime(task.getDueDate());
                    if (org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil.isAfterCurrentWeek(temp)) {
                        // not scheduled, due next week or later
                        return true;
                    } else {
                        // due this week or earlier
                        return false;
                    }
                }
            }
            if (scheduledForDate == null) {
                if ((task.getDueDate() != null) && (!taskActivityManager.isOwnedByUser(task))) {
                    // not scheduled, due but not owned by user
                    return true;
                } else if (task.getDueDate() == null) {
                    // not scheduled, not due
                    return true;
                }
            }
            return false;
        }

        protected abstract boolean select(org.eclipse.mylyn.tasks.core.ITask task);
    }

    public class Incoming extends org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.StateTaskContainer {
        public Incoming() {
            super(new org.eclipse.mylyn.internal.tasks.core.DateRange(INCOMING_TIME), Messages.TaskScheduleContentProvider_Incoming);
        }

        @java.lang.Override
        protected boolean select(org.eclipse.mylyn.tasks.core.ITask task) {
            org.eclipse.mylyn.tasks.core.ITask.SynchronizationState state = task.getSynchronizationState();
            return (state == org.eclipse.mylyn.tasks.core.ITask.SynchronizationState.INCOMING_NEW) || ((state == org.eclipse.mylyn.tasks.core.ITask.SynchronizationState.INCOMING) && (!java.lang.Boolean.parseBoolean(task.getAttribute(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.ATTRIBUTE_TASK_SUPPRESS_INCOMING))));
        }
    }

    public class Outgoing extends org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.StateTaskContainer {
        public Outgoing() {
            super(new org.eclipse.mylyn.internal.tasks.core.DateRange(OUTGOING_TIME), Messages.TaskScheduleContentProvider_Outgoing);
        }

        @java.lang.Override
        public boolean select(org.eclipse.mylyn.tasks.core.ITask task) {
            org.eclipse.mylyn.tasks.core.ITask.SynchronizationState state = task.getSynchronizationState();
            return ((state == org.eclipse.mylyn.tasks.core.ITask.SynchronizationState.OUTGOING) || (state == org.eclipse.mylyn.tasks.core.ITask.SynchronizationState.OUTGOING_NEW)) || (state == org.eclipse.mylyn.tasks.core.ITask.SynchronizationState.CONFLICT);
        }
    }

    public class Completed extends org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.StateTaskContainer {
        public Completed() {
            super(new org.eclipse.mylyn.internal.tasks.core.DateRange(COMPLETED_TIME), Messages.TaskScheduleContentProvider_Completed);
        }

        @java.lang.Override
        public boolean select(org.eclipse.mylyn.tasks.core.ITask task) {
            return task.isCompleted() && org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.notScheduled(task);
        }
    }

    private class RolloverCheck extends org.eclipse.core.runtime.jobs.Job {
        public RolloverCheck() {
            super("Calendar Rollover Job");// $NON-NLS-1$

        }

        @java.lang.Override
        protected org.eclipse.core.runtime.IStatus run(org.eclipse.core.runtime.IProgressMonitor monitor) {
            refresh();
            return org.eclipse.core.runtime.Status.OK_STATUS;
        }
    }
}