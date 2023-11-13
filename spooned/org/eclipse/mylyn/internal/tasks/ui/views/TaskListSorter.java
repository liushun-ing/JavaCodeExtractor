/**
 * *****************************************************************************
 * Copyright (c) 2004, 2016 Tasktop Technologies and others.
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
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.ITaskRepositoryElement;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.TaskCategory;
import org.eclipse.mylyn.internal.tasks.core.TaskGroup;
import org.eclipse.mylyn.internal.tasks.core.UncategorizedTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.UnmatchedTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.UnsubmittedTaskContainer;
import org.eclipse.mylyn.internal.tasks.ui.util.TaskComparator;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.ui.IMemento;
/**
 *
 * @author Mik Kersten
 */
public class TaskListSorter extends org.eclipse.jface.viewers.ViewerSorter {
    private static final java.lang.String MEMENTO_KEY_SORTER = "sorter";// $NON-NLS-1$


    private static final java.lang.String MEMENTO_KEY_GROUP_BY = "groupBy";// $NON-NLS-1$


    private static final org.eclipse.mylyn.internal.tasks.ui.views.TaskListSorter.GroupBy DEFAULT_GROUP_BY = org.eclipse.mylyn.internal.tasks.ui.views.TaskListSorter.GroupBy.NONE;

    public enum GroupBy {

        NONE,
        CATEGORY_QUERY,
        CATEGORY_REPOSITORY;
        public java.lang.String getLabel() {
            switch (this) {
                case NONE :
                    return Messages.TaskListSorter_No_Grouping;
                case CATEGORY_QUERY :
                    return Messages.TaskListSorter_Catagory_and_Query;
                case CATEGORY_REPOSITORY :
                    return Messages.TaskListSorter_Catagory_and_Repository;
                default :
                    return null;
            }
        }

        public static org.eclipse.mylyn.internal.tasks.ui.views.TaskListSorter.GroupBy valueOfLabel(java.lang.String label) {
            for (org.eclipse.mylyn.internal.tasks.ui.views.TaskListSorter.GroupBy value : org.eclipse.mylyn.internal.tasks.ui.views.TaskListSorter.GroupBy.values()) {
                if (value.getLabel().equals(label)) {
                    return value;
                }
            }
            return null;
        }
    }

    private class SortElement {
        private int weight;

        private final java.lang.String[] values = new java.lang.String[3];
    }

    private final org.eclipse.mylyn.internal.tasks.ui.util.TaskComparator taskComparator;

    private org.eclipse.mylyn.internal.tasks.ui.views.TaskListSorter.GroupBy groupBy;

    private final org.eclipse.mylyn.internal.tasks.ui.views.TaskListSorter.SortElement key1;

    private final org.eclipse.mylyn.internal.tasks.ui.views.TaskListSorter.SortElement key2;

    public TaskListSorter() {
        this.taskComparator = new org.eclipse.mylyn.internal.tasks.ui.util.TaskComparator();
        this.groupBy = org.eclipse.mylyn.internal.tasks.ui.views.TaskListSorter.GroupBy.CATEGORY_QUERY;
        this.key1 = new org.eclipse.mylyn.internal.tasks.ui.views.TaskListSorter.SortElement();
        this.key2 = new org.eclipse.mylyn.internal.tasks.ui.views.TaskListSorter.SortElement();
    }

    /**
     * compare - invoked when column is selected calls the actual comparison method for particular criteria
     */
    @java.lang.Override
    public int compare(org.eclipse.jface.viewers.Viewer compareViewer, java.lang.Object o1, java.lang.Object o2) {
        if ((o1 instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask) && (o2 instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask)) {
            // sort of the tasks within the container using the setting from the Sort Dialog
            org.eclipse.mylyn.tasks.core.ITask element1 = ((org.eclipse.mylyn.tasks.core.ITask) (o1));
            org.eclipse.mylyn.tasks.core.ITask element2 = ((org.eclipse.mylyn.tasks.core.ITask) (o2));
            return taskComparator.compare(element1, element2);
        } else if ((o1 instanceof org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer) && (o2 instanceof org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer)) {
            // scheduled Mode compare
            org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer dateRangeTaskContainer1 = ((org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer) (o1));
            org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer dateRangeTaskContainer2 = ((org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer) (o2));
            return dateRangeTaskContainer1.getDateRange().compareTo(dateRangeTaskContainer2.getDateRange());
        } else {
            updateKey(key1, o1);
            updateKey(key2, o2);
            if (key1.weight != key2.weight) {
                return (key1.weight - key2.weight) < 0 ? -1 : 1;
            }
            int result = compare(key1.values[0], key2.values[0]);
            if (result != 0) {
                return result;
            }
            result = compare(key1.values[1], key2.values[1]);
            return result != 0 ? result : compare(key1.values[2], key2.values[2]);
        }
    }

    private int compare(java.lang.String key1, java.lang.String key2) {
        if (key1 == null) {
            return key2 != null ? 1 : 0;
        } else if (key2 == null) {
            return -1;
        }
        return java.text.Collator.getInstance().compare(key1, key2);
    }

    private void updateKey(org.eclipse.mylyn.internal.tasks.ui.views.TaskListSorter.SortElement key, java.lang.Object object) {
        int weight;
        if (object instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask) {
            weight = 0;
        } else if (object instanceof org.eclipse.mylyn.internal.tasks.core.UncategorizedTaskContainer) {
            weight = 1;
        } else if (object instanceof org.eclipse.mylyn.internal.tasks.core.UnsubmittedTaskContainer) {
            weight = 2;
        } else if (object instanceof org.eclipse.mylyn.internal.tasks.core.TaskCategory) {
            weight = 3;
        } else if (object instanceof org.eclipse.mylyn.internal.tasks.core.RepositoryQuery) {
            weight = 4;
        } else if (object instanceof org.eclipse.mylyn.internal.tasks.core.TaskGroup) {
            // support for the experimental grouping of tasks
            weight = 5;
        } else if (object instanceof org.eclipse.mylyn.internal.tasks.core.UnmatchedTaskContainer) {
            weight = 6;
        } else {
            weight = 99;
        }
        key.values[0] = ((org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer) (object)).getSummary();
        key.values[1] = null;
        key.values[2] = null;
        switch (groupBy) {
            case NONE :
                weight = 1;
                break;
            case CATEGORY_QUERY :
                break;
            case CATEGORY_REPOSITORY :
                if (weight == 1) {
                    // keep
                } else if (weight == 3) {
                    weight = 2;
                } else {
                    key.values[0] = getRepositoryUrl(object);
                    key.values[1] = java.lang.Integer.toString(weight);
                    key.values[2] = ((org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer) (object)).getSummary();
                    weight = 3;
                }
                break;
        }
        key.weight = weight;
    }

    private java.lang.String getRepositoryUrl(java.lang.Object object) {
        if (object instanceof org.eclipse.mylyn.internal.tasks.core.ITaskRepositoryElement) {
            org.eclipse.mylyn.internal.tasks.core.ITaskRepositoryElement repositoryElement = ((org.eclipse.mylyn.internal.tasks.core.ITaskRepositoryElement) (object));
            java.lang.String repositoryUrl = repositoryElement.getRepositoryUrl();
            org.eclipse.mylyn.tasks.core.TaskRepository taskRepository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(repositoryElement.getConnectorKind(), repositoryUrl);
            return taskRepository != null ? taskRepository.getRepositoryLabel() : null;
        }
        return null;
    }

    public org.eclipse.mylyn.internal.tasks.ui.util.TaskComparator getTaskComparator() {
        return taskComparator;
    }

    public org.eclipse.mylyn.internal.tasks.ui.views.TaskListSorter.GroupBy getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(org.eclipse.mylyn.internal.tasks.ui.views.TaskListSorter.GroupBy sortByIndex) {
        org.eclipse.core.runtime.Assert.isNotNull(sortByIndex);
        this.groupBy = sortByIndex;
    }

    public void restoreState(org.eclipse.ui.IMemento memento) {
        org.eclipse.ui.IMemento child = memento.getChild(org.eclipse.mylyn.internal.tasks.ui.views.TaskListSorter.MEMENTO_KEY_SORTER);
        if (child != null) {
            taskComparator.restoreState(child);
        }
        setGroupBy(getGroupBy(memento, org.eclipse.mylyn.internal.tasks.ui.views.TaskListSorter.MEMENTO_KEY_GROUP_BY, org.eclipse.mylyn.internal.tasks.ui.views.TaskListSorter.DEFAULT_GROUP_BY));
    }

    public void saveState(org.eclipse.ui.IMemento memento) {
        org.eclipse.ui.IMemento child = memento.createChild(org.eclipse.mylyn.internal.tasks.ui.views.TaskListSorter.MEMENTO_KEY_SORTER);
        if (child != null) {
            taskComparator.saveState(child);
        }
        memento.putString(org.eclipse.mylyn.internal.tasks.ui.views.TaskListSorter.MEMENTO_KEY_GROUP_BY, getGroupBy().name());
    }

    private org.eclipse.mylyn.internal.tasks.ui.views.TaskListSorter.GroupBy getGroupBy(org.eclipse.ui.IMemento memento, java.lang.String key, org.eclipse.mylyn.internal.tasks.ui.views.TaskListSorter.GroupBy defaultValue) {
        java.lang.String value = memento.getString(key);
        if (value != null) {
            try {
                return org.eclipse.mylyn.internal.tasks.ui.views.TaskListSorter.GroupBy.valueOf(value);
            } catch (java.lang.IllegalArgumentException e) {
                // ignore
            }
        }
        return defaultValue;
    }
}