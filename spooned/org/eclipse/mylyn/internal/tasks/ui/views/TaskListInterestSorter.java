/**
 * *****************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
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
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.UncategorizedTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.UnmatchedTaskContainer;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TaskComparator;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskContainer;
/**
 *
 * @author Mik Kersten
 */
public class TaskListInterestSorter extends org.eclipse.jface.viewers.ViewerSorter {
    private final org.eclipse.mylyn.internal.tasks.ui.views.TaskKeyComparator taskKeyComparator = new org.eclipse.mylyn.internal.tasks.ui.views.TaskKeyComparator();

    private org.eclipse.jface.viewers.ViewerSorter configuredSorter;

    public void setconfiguredSorter(org.eclipse.jface.viewers.ViewerSorter configuredSorter) {
        this.configuredSorter = configuredSorter;
    }

    @java.lang.Override
    public int compare(org.eclipse.jface.viewers.Viewer compareViewer, java.lang.Object o1, java.lang.Object o2) {
        if ((o1 instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) && (o2 instanceof org.eclipse.mylyn.internal.tasks.core.UnmatchedTaskContainer)) {
            return -1;
        } else if ((o2 instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) && (o1 instanceof org.eclipse.mylyn.internal.tasks.core.UnmatchedTaskContainer)) {
            return 1;
        }
        if ((o1 instanceof org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer) && (o2 instanceof org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer)) {
            org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer dateRangeTaskContainer1 = ((org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer) (o1));
            org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer dateRangeTaskContainer2 = ((org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer) (o2));
            return dateRangeTaskContainer1.getDateRange().compareTo(dateRangeTaskContainer2.getDateRange());
        } else if ((o1 instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) && (o2 instanceof org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer)) {
            return -1;
        } else if ((o1 instanceof org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer) && (o2 instanceof org.eclipse.mylyn.tasks.core.ITaskContainer)) {
            return 1;
        }
        if ((o1 instanceof org.eclipse.mylyn.internal.tasks.core.UncategorizedTaskContainer) && (o2 instanceof org.eclipse.mylyn.tasks.core.ITaskContainer)) {
            return -1;
        } else if ((o1 instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) && (o2 instanceof org.eclipse.mylyn.internal.tasks.core.UncategorizedTaskContainer)) {
            return 1;
        }
        if ((!(o1 instanceof org.eclipse.mylyn.tasks.core.ITask)) && (o2 instanceof org.eclipse.mylyn.tasks.core.ITask)) {
            return 1;
        }
        if (!(o1 instanceof org.eclipse.mylyn.tasks.core.ITask)) {
            // o1 instanceof AbstractTaskContainer || o1 instanceof AbstractRepositoryQuery) {
            if (!(o2 instanceof org.eclipse.mylyn.tasks.core.ITask)) {
                // o2 instanceof AbstractTaskContainer || o2 instanceof AbstractRepositoryQuery) {
                return ((org.eclipse.mylyn.tasks.core.IRepositoryElement) (o1)).getSummary().compareToIgnoreCase(((org.eclipse.mylyn.tasks.core.IRepositoryElement) (o2)).getSummary());
            } else {
                return -1;
            }
        } else if (o1 instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) {
            if (!(o2 instanceof org.eclipse.mylyn.tasks.core.ITask)) {
                // o2 instanceof AbstractTaskContainer || o2 instanceof AbstractRepositoryQuery) {
                return -1;
            } else if (o2 instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) {
                org.eclipse.mylyn.tasks.core.IRepositoryElement element1 = ((org.eclipse.mylyn.tasks.core.IRepositoryElement) (o1));
                org.eclipse.mylyn.tasks.core.IRepositoryElement element2 = ((org.eclipse.mylyn.tasks.core.IRepositoryElement) (o2));
                org.eclipse.mylyn.internal.tasks.core.AbstractTask task1 = null;
                org.eclipse.mylyn.internal.tasks.core.AbstractTask task2 = null;
                if (element1 instanceof org.eclipse.mylyn.tasks.core.ITask) {
                    task1 = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (element1));
                }
                if (element2 instanceof org.eclipse.mylyn.tasks.core.ITask) {
                    task2 = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (element2));
                }
                if ((task1 == null) && (task2 == null)) {
                    return comparePrioritiesAndKeys(element1, element2);
                } else if (task1 == null) {
                    return 1;
                } else if (task2 == null) {
                    return -1;
                }
                int complete = compareCompleted(task1, task2);
                if (complete != 0) {
                    return complete;
                }
                int due = compareDueDates(task1, task2);
                if (due != 0) {
                    return due;
                }
                int today = compareScheduledDate(task1, task2);
                if (today != 0) {
                    return today;
                }
            }
        }
        if (configuredSorter != null) {
            return configuredSorter.compare(compareViewer, o1, o2);
        }
        return 0;
    }

    private int compareDueDates(org.eclipse.mylyn.tasks.core.ITask task1, org.eclipse.mylyn.tasks.core.ITask task2) {
        if (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().isOverdue(task1) && (!org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().isOverdue(task2))) {
            return -1;
        } else if ((!org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().isOverdue(task1)) && org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().isOverdue(task2)) {
            return 1;
        }
        return 0;
    }

    private int compareScheduledDate(org.eclipse.mylyn.internal.tasks.core.AbstractTask task1, org.eclipse.mylyn.internal.tasks.core.AbstractTask task2) {
        if (isToday(task1) && (!isToday(task2))) {
            return -1;
        } else if ((!isToday(task1)) && isToday(task2)) {
            return 1;
        } else {
            return 0;
        }
    }

    private boolean isToday(org.eclipse.mylyn.internal.tasks.core.AbstractTask task) {
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().isPastReminder(task) || org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().isScheduledForToday(task);
    }

    private int compareCompleted(org.eclipse.mylyn.tasks.core.ITask task1, org.eclipse.mylyn.tasks.core.ITask task2) {
        if (task1.isCompleted() && (!task2.isCompleted())) {
            return 1;
        } else if ((!task1.isCompleted()) && task2.isCompleted()) {
            return -1;
        } else {
            return 0;
        }
    }

    private int comparePrioritiesAndKeys(org.eclipse.mylyn.tasks.core.IRepositoryElement element1, org.eclipse.mylyn.tasks.core.IRepositoryElement element2) {
        int priority = comparePriorities(element1, element2);
        if (priority != 0) {
            return priority;
        }
        int key = compareKeys(element1, element2);
        if (key != 0) {
            return key;
        }
        return 0;
    }

    private int compareKeys(org.eclipse.mylyn.tasks.core.IRepositoryElement element1, org.eclipse.mylyn.tasks.core.IRepositoryElement element2) {
        return taskKeyComparator.compare(org.eclipse.mylyn.internal.tasks.ui.util.TaskComparator.getSortableFromElement(element1), org.eclipse.mylyn.internal.tasks.ui.util.TaskComparator.getSortableFromElement(element2));
    }

    private int comparePriorities(org.eclipse.mylyn.tasks.core.IRepositoryElement element1, org.eclipse.mylyn.tasks.core.IRepositoryElement element2) {
        if ((element1 instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer) && (element2 instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer)) {
            return ((org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer) (element1)).getPriority().compareTo(((org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer) (element2)).getPriority());
        } else {
            // TODO: consider implementing
            return 0;
        }
    }
}