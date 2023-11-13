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
 *     Frank Becker - improvements for bug 231336
 *     Julio Gesser - fixes for bug 303509
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.util;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.DateRange;
import org.eclipse.mylyn.internal.tasks.core.DayDateRange;
import org.eclipse.mylyn.internal.tasks.ui.CategorizedPresentation;
import org.eclipse.mylyn.internal.tasks.ui.ScheduledPresentation;
import org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion.SortKey;
import org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListPresentation;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskKeyComparator;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.ui.IMemento;
/**
 *
 * @author Mik Kersten
 * @author Frank Becker
 */
public class TaskComparator implements java.util.Comparator<org.eclipse.mylyn.tasks.core.ITask> {
    private static final java.lang.String MEMENTO_KEY_SORT = "sort";// $NON-NLS-1$


    private final com.google.common.collect.ListMultimap<java.lang.String, org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion> sortCriteria;

    private java.lang.String currentPresentation;

    /**
     * Return a array of values to pass to taskKeyComparator.compare() for sorting
     *
     * @param element
     * 		the element to sort
     * @return String array[component, taskId, summary]
     */
    public static java.lang.String[] getSortableFromElement(org.eclipse.mylyn.tasks.core.IRepositoryElement element) {
        final java.lang.String a[] = new java.lang.String[]{ "", null, element.getSummary() };// $NON-NLS-1$

        if (element instanceof org.eclipse.mylyn.tasks.core.ITask) {
            org.eclipse.mylyn.tasks.core.ITask task1 = ((org.eclipse.mylyn.tasks.core.ITask) (element));
            if (task1.getTaskKey() != null) {
                a[1] = task1.getTaskKey();
            }
        }
        return a;
    }

    private final org.eclipse.mylyn.internal.tasks.ui.views.TaskKeyComparator taskKeyComparator = new org.eclipse.mylyn.internal.tasks.ui.views.TaskKeyComparator();

    public static final int CRITERIA_COUNT = org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion.SortKey.values().length - 1;

    public TaskComparator() {
        sortCriteria = com.google.common.collect.ArrayListMultimap.create();
        for (org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListPresentation presentation : org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.getPresentations()) {
            java.lang.String presentationId = presentation.getId();
            for (int i = 0; i < org.eclipse.mylyn.internal.tasks.ui.util.TaskComparator.CRITERIA_COUNT; i++) {
                sortCriteria.put(presentationId, new org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion());
            }
        }
        for (java.lang.String id : sortCriteria.keySet()) {
            java.util.List<org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion> presentationCriteria = sortCriteria.get(id);
            if (id.equals(org.eclipse.mylyn.internal.tasks.ui.ScheduledPresentation.ID)) {
                // scheduled presentation has specific defaults
                presentationCriteria.get(0).setKey(org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion.SortKey.DUE_DATE);
                presentationCriteria.get(0).setDirection(SortCriterion.ASCENDING);
                presentationCriteria.get(1).setKey(org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion.SortKey.SCHEDULED_DATE);
                presentationCriteria.get(1).setDirection(SortCriterion.ASCENDING);
                presentationCriteria.get(2).setKey(org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion.SortKey.PRIORITY);
                presentationCriteria.get(3).setKey(org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion.SortKey.RANK);
                presentationCriteria.get(4).setKey(org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion.SortKey.DATE_CREATED);
            } else {
                // standard defaults
                presentationCriteria.get(0).setKey(org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion.SortKey.PRIORITY);
                presentationCriteria.get(1).setKey(org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion.SortKey.RANK);
                presentationCriteria.get(2).setKey(org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion.SortKey.DATE_CREATED);
            }
        }
        currentPresentation = org.eclipse.mylyn.internal.tasks.ui.CategorizedPresentation.ID;
    }

    public int compare(org.eclipse.mylyn.tasks.core.ITask element1, org.eclipse.mylyn.tasks.core.ITask element2) {
        for (org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion key : getCurrentCriteria()) {
            int result;
            switch (key.getKey()) {
                case DATE_CREATED :
                    result = sortByCreationDate(element1, element2, key.getDirection());
                    break;
                case RANK :
                    result = sortByRank(element1, element2, key.getDirection());
                    break;
                case PRIORITY :
                    result = sortByPriority(element1, element2, key.getDirection());
                    break;
                case SUMMARY :
                    result = sortBySummary(element1, element2, key.getDirection());
                    break;
                case TASK_ID :
                    result = sortByID(element1, element2, key.getDirection());
                    break;
                case TASK_TYPE :
                    result = compare(element1.getTaskKind(), element2.getTaskKind(), key.getDirection());
                    break;
                case DUE_DATE :
                    result = sortByDueDate(element1, element2, key.getDirection());
                    break;
                case MODIFICATION_DATE :
                    result = sortByModificationDate(element1, element2, key.getDirection());
                    break;
                case SCHEDULED_DATE :
                    result = sortByScheduledDate(element1, element2, key.getDirection());
                    break;
                default :
                    // NONE
                    return 0;
            }
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

    public org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion getSortCriterion(int index) {
        return getCurrentCriteria().get(index);
    }

    public void restoreState(org.eclipse.ui.IMemento memento) {
        if (memento != null) {
            for (java.lang.String presentationId : sortCriteria.keySet()) {
                java.util.List<org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion> criteria = sortCriteria.get(presentationId);
                for (int i = 0; i < criteria.size(); i++) {
                    org.eclipse.ui.IMemento child = memento.getChild((org.eclipse.mylyn.internal.tasks.ui.util.TaskComparator.MEMENTO_KEY_SORT + presentationId) + i);
                    if (child != null) {
                        criteria.get(i).restoreState(child);
                    } else if (org.eclipse.mylyn.internal.tasks.ui.CategorizedPresentation.ID.equals(presentationId)) {
                        // attempt to read memento as it would have recorded before sort criteria were stored by presentation
                        child = memento.getChild(org.eclipse.mylyn.internal.tasks.ui.util.TaskComparator.MEMENTO_KEY_SORT + i);
                        if (child != null) {
                            criteria.get(i).restoreState(child);
                        }
                    }
                }
            }
        }
    }

    public void saveState(org.eclipse.ui.IMemento memento) {
        if (memento != null) {
            for (java.lang.String presentationId : sortCriteria.keySet()) {
                java.util.List<org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion> criteria = sortCriteria.get(presentationId);
                for (int i = 0; i < criteria.size(); i++) {
                    org.eclipse.ui.IMemento child = memento.createChild((org.eclipse.mylyn.internal.tasks.ui.util.TaskComparator.MEMENTO_KEY_SORT + presentationId) + i);
                    if (child != null) {
                        criteria.get(i).saveState(child);
                    }
                }
            }
        }
    }

    public void presentationChanged(org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListPresentation presentation) {
        currentPresentation = presentation.getId();
    }

    private java.util.List<org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion> getCurrentCriteria() {
        return sortCriteria.get(currentPresentation);
    }

    private int sortByCreationDate(org.eclipse.mylyn.tasks.core.ITask task1, org.eclipse.mylyn.tasks.core.ITask task2, int sortDirection) {
        java.util.Date date1 = task1.getCreationDate();
        java.util.Date date2 = task2.getCreationDate();
        return compare(date1, date2, sortDirection);
    }

    private int sortByDueDate(org.eclipse.mylyn.tasks.core.ITask task1, org.eclipse.mylyn.tasks.core.ITask task2, int sortDirection) {
        java.util.Date date1 = task1.getDueDate();
        java.util.Date date2 = task2.getDueDate();
        return compare(date1, date2, sortDirection);
    }

    private int sortByModificationDate(org.eclipse.mylyn.tasks.core.ITask task1, org.eclipse.mylyn.tasks.core.ITask task2, int sortDirection) {
        java.util.Date date1 = task1.getModificationDate();
        java.util.Date date2 = task2.getModificationDate();
        return compare(date1, date2, sortDirection);
    }

    private int sortByScheduledDate(org.eclipse.mylyn.tasks.core.ITask task1, org.eclipse.mylyn.tasks.core.ITask task2, int sortDirection) {
        if ((task1 instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask) && (task2 instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask)) {
            org.eclipse.mylyn.internal.tasks.core.DateRange date1 = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (task1)).getScheduledForDate();
            org.eclipse.mylyn.internal.tasks.core.DateRange date2 = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (task2)).getScheduledForDate();
            return compare(date1, date2, sortDirection);
        }
        return 0;
    }

    private int compare(org.eclipse.mylyn.internal.tasks.core.DateRange date1, org.eclipse.mylyn.internal.tasks.core.DateRange date2, int sortDirection) {
        if (date1 == null) {
            return date2 == null ? 0 : 1;
        } else if (date2 == null) {
            return -1;
        } else if ((date1 instanceof org.eclipse.mylyn.internal.tasks.core.DayDateRange) && (!(date2 instanceof org.eclipse.mylyn.internal.tasks.core.DayDateRange))) {
            return -1;
        } else if ((date2 instanceof org.eclipse.mylyn.internal.tasks.core.DayDateRange) && (!(date1 instanceof org.eclipse.mylyn.internal.tasks.core.DayDateRange))) {
            return 1;
        }
        return compare(date1.getEndDate(), date2.getEndDate(), sortDirection);
    }

    private <T> int compare(java.lang.Comparable<T> key1, T key2, int sortDirection) {
        if (key1 == null) {
            return key2 != null ? sortDirection : 0;
        } else if (key2 == null) {
            return -sortDirection;
        }
        return sortDirection * key1.compareTo(key2);
    }

    private int sortByID(org.eclipse.mylyn.tasks.core.ITask task1, org.eclipse.mylyn.tasks.core.ITask task2, int sortDirection) {
        java.lang.String key1 = task1.getTaskKey();
        java.lang.String key2 = task2.getTaskKey();
        if (key1 == null) {
            return key2 != null ? sortDirection : 0;
        } else if (key2 == null) {
            return -sortDirection;
        }
        return sortDirection * taskKeyComparator.compare2(key1, key2);
    }

    private int sortByRank(org.eclipse.mylyn.tasks.core.ITask task1, org.eclipse.mylyn.tasks.core.ITask task2, int sortDirection) {
        if (((task1.getConnectorKind() != null) && (task2.getConnectorKind() != null)) && task1.getConnectorKind().equals(task2.getConnectorKind())) {
            // only compare rank of elements from the same connector
            if (((task1.getRepositoryUrl() != null) && (task2.getRepositoryUrl() != null)) && task1.getRepositoryUrl().equals(task2.getRepositoryUrl())) {
                // only compare the rank of elements in the same repository
                java.lang.String rankString1 = task1.getAttribute(org.eclipse.mylyn.tasks.core.data.TaskAttribute.RANK);
                java.lang.String rankString2 = task2.getAttribute(org.eclipse.mylyn.tasks.core.data.TaskAttribute.RANK);
                try {
                    java.lang.Double rank1 = ((rankString1 == null) || (rankString1.length() == 0)) ? java.lang.Double.valueOf(0) : java.lang.Double.valueOf(rankString1);
                    java.lang.Double rank2 = ((rankString2 == null) || (rankString2.length() == 0)) ? java.lang.Double.valueOf(0) : java.lang.Double.valueOf(rankString2);
                    return compare(rank1, rank2, sortDirection);
                } catch (java.lang.NumberFormatException e) {
                    return compare(rankString1, rankString2, sortDirection);
                }
            }
        }
        return 0;
    }

    private int sortByPriority(org.eclipse.mylyn.tasks.core.ITask task1, org.eclipse.mylyn.tasks.core.ITask task2, int sortDirection) {
        return sortDirection * task1.getPriority().compareToIgnoreCase(task2.getPriority());
    }

    private int sortBySummary(org.eclipse.mylyn.tasks.core.ITask task1, org.eclipse.mylyn.tasks.core.ITask task2, int sortDirection) {
        java.lang.String key1 = task1.getSummary();
        java.lang.String key2 = task2.getSummary();
        if (key1 == null) {
            return key2 != null ? sortDirection : 0;
        } else if (key2 == null) {
            return -sortDirection;
        }
        return sortDirection * key1.compareToIgnoreCase(key2);
    }
}