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
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.core.TaskCategory;
import org.eclipse.mylyn.internal.tasks.core.UnmatchedTaskContainer;
import org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskContainer;
/**
 * Provides custom content for the task list, e.g. guaranteed visibility of some elements, ability to suppress
 * containers showing if nothing should show under them. TODO: move to viewer filter architecture?
 *
 * @author Mik Kersten
 * @author Rob Elves
 */
public class TaskListContentProvider extends org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListContentProvider {
    protected static java.lang.Object[] EMPTY_ARRRY = new java.lang.Object[0];

    public TaskListContentProvider(org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListView taskListView) {
        super(taskListView);
    }

    public void inputChanged(org.eclipse.jface.viewers.Viewer v, java.lang.Object oldInput, java.lang.Object newInput) {
        this.taskListView.expandToActiveTasks();
    }

    public void dispose() {
    }

    public java.lang.Object[] getElements(java.lang.Object parent) {
        if (parent.equals(this.taskListView.getViewSite())) {
            return applyFilter(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().getRootElements()).toArray();
        }
        return getChildren(parent);
    }

    /**
     *
     * @return first parent found
     */
    public java.lang.Object getParent(java.lang.Object child) {
        // return first parent found, first search within categories then queries
        if (child instanceof org.eclipse.mylyn.tasks.core.ITask) {
            org.eclipse.mylyn.tasks.core.ITask task = ((org.eclipse.mylyn.tasks.core.ITask) (child));
            org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory parent = org.eclipse.mylyn.internal.tasks.core.TaskCategory.getParentTaskCategory(task);
            if (parent != null) {
                return parent;
            }
            java.util.Set<org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer> parents = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (task)).getParentContainers();
            java.util.Iterator<org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer> it = parents.iterator();
            if (it.hasNext()) {
                return parents.iterator().next();
            }
        }
        // no parent found
        return null;
    }

    public java.lang.Object[] getChildren(java.lang.Object parent) {
        return getFilteredChildrenFor(parent).toArray();
    }

    /**
     * NOTE: If parent is an ITask, this method checks if parent has unfiltered children (see bug 145194).
     */
    public boolean hasChildren(java.lang.Object parent) {
        java.lang.Object[] children = getChildren(parent);
        return (children != null) && (children.length > 0);
        // if (parent instanceof AbstractRepositoryQuery) {
        // AbstractRepositoryQuery query = (AbstractRepositoryQuery) parent;
        // return !getFilteredChildrenFor(query).isEmpty();
        // //return !query.isEmpty();
        // } else if (parent instanceof AbstractTask) {
        // return taskHasUnfilteredChildren((AbstractTask) parent);
        // } else if (parent instanceof AbstractTaskContainer) {
        // AbstractTaskContainer container = (AbstractTaskContainer) parent;
        // return !getFilteredChildrenFor(container).isEmpty();
        // //return !container.getChildren().isEmpty();
        // }
        // return false;
    }

    protected java.util.List<org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer> applyFilter(java.util.Set<org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer> roots) {
        java.util.List<org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer> filteredRoots = new java.util.ArrayList<org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer>();
        for (org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer element : roots) {
            // NOTE: tasks can no longer appear as root elements
            if (selectContainer(element)) {
                filteredRoots.add(element);
            }
        }
        return filteredRoots;
    }

    /**
     * See bug 109693
     */
    private boolean containsNoFilterText(java.lang.String filterText) {
        return (filterText == null) || (filterText.trim().length() == 0);
    }

    private boolean selectContainer(org.eclipse.mylyn.tasks.core.ITaskContainer container) {
        if (filter(null, container)) {
            return false;
        }
        return true;
    }

    protected java.util.List<org.eclipse.mylyn.tasks.core.IRepositoryElement> getFilteredChildrenFor(java.lang.Object parent) {
        if (containsNoFilterText(this.taskListView.getFilteredTree().getFilterString())) {
            java.util.List<org.eclipse.mylyn.tasks.core.IRepositoryElement> children = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.IRepositoryElement>();
            if (parent instanceof org.eclipse.mylyn.tasks.core.ITask) {
                java.util.Collection<org.eclipse.mylyn.tasks.core.ITask> subTasks = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (parent)).getChildren();
                for (org.eclipse.mylyn.tasks.core.ITask task : subTasks) {
                    if (!filter(parent, task)) {
                        children.add(task);
                    }
                }
                return children;
            } else if (parent instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) {
                return getFilteredRootChildren(((org.eclipse.mylyn.tasks.core.ITaskContainer) (parent)));
            }
        } else {
            java.util.List<org.eclipse.mylyn.tasks.core.IRepositoryElement> children = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.IRepositoryElement>();
            if (parent instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) {
                children.addAll(((org.eclipse.mylyn.tasks.core.ITaskContainer) (parent)).getChildren());
                return children;
            }
        }
        return java.util.Collections.emptyList();
    }

    /**
     *
     * @return all children who aren't already revealed as a sub task
     */
    private java.util.List<org.eclipse.mylyn.tasks.core.IRepositoryElement> getFilteredRootChildren(org.eclipse.mylyn.tasks.core.ITaskContainer parent) {
        java.util.List<org.eclipse.mylyn.tasks.core.IRepositoryElement> result = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.IRepositoryElement>();
        if (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().groupSubtasks(parent)) {
            java.util.Collection<org.eclipse.mylyn.tasks.core.ITask> parentTasks = parent.getChildren();
            java.util.Set<org.eclipse.mylyn.tasks.core.IRepositoryElement> parents = new java.util.HashSet<org.eclipse.mylyn.tasks.core.IRepositoryElement>();
            java.util.Set<org.eclipse.mylyn.tasks.core.ITask> children = new java.util.HashSet<org.eclipse.mylyn.tasks.core.ITask>();
            // get all children
            for (org.eclipse.mylyn.tasks.core.ITask element : parentTasks) {
                if (element instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) {
                    for (org.eclipse.mylyn.tasks.core.ITask abstractTask : ((org.eclipse.mylyn.tasks.core.ITaskContainer) (element)).getChildren()) {
                        children.add(abstractTask);
                    }
                }
            }
            for (org.eclipse.mylyn.tasks.core.ITask task : parentTasks) {
                if ((!filter(parent, task)) && (!children.contains(task))) {
                    parents.add(task);
                }
            }
            result.addAll(parents);
        } else {
            for (org.eclipse.mylyn.tasks.core.IRepositoryElement element : parent.getChildren()) {
                if (!filter(parent, element)) {
                    result.add(element);
                }
            }
        }
        return result;
    }

    protected boolean filter(java.lang.Object parent, java.lang.Object object) {
        boolean notSearching = containsNoFilterText(this.taskListView.getFilteredTree().getFilterString());
        for (org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter filter : this.taskListView.getFilters()) {
            if (notSearching || filter.applyToFilteredText()) {
                if (!filter.select(parent, object)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean filter(org.eclipse.jface.viewers.TreePath path, java.lang.Object parent, java.lang.Object object) {
        boolean emptyFilterText = containsNoFilterText(this.taskListView.getFilteredTree().getFilterString());
        for (org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter filter : this.taskListView.getFilters()) {
            if (emptyFilterText || filter.applyToFilteredText()) {
                if (filter instanceof org.eclipse.mylyn.internal.tasks.ui.views.TaskListInterestFilter) {
                    if (!((org.eclipse.mylyn.internal.tasks.ui.views.TaskListInterestFilter) (filter)).select(path.getLastSegment(), object)) {
                        return true;
                    }
                } else if (!filter.select(parent, object)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isSearching() {
        return !containsNoFilterText(this.taskListView.getFilteredTree().getFilterString());
    }

    public java.lang.Object[] getChildren(org.eclipse.jface.viewers.TreePath parentPath) {
        java.lang.Object parent = parentPath.getLastSegment();
        if (org.eclipse.mylyn.internal.tasks.ui.views.PresentationFilter.getInstance().isFilterNonMatching()) {
            org.eclipse.mylyn.tasks.core.ITaskContainer container = ((org.eclipse.mylyn.tasks.core.ITaskContainer) (parentPath.getFirstSegment()));
            if ((container instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery) || (container instanceof org.eclipse.mylyn.internal.tasks.core.UnmatchedTaskContainer)) {
                if (parent instanceof org.eclipse.mylyn.tasks.core.ITask) {
                    if ((container instanceof org.eclipse.mylyn.internal.tasks.core.RepositoryQuery) && (!org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().groupSubtasks(container))) {
                        return org.eclipse.mylyn.internal.tasks.ui.views.TaskListContentProvider.EMPTY_ARRRY;
                    }
                }
                java.util.List<org.eclipse.mylyn.tasks.core.IRepositoryElement> children = getFilteredChildrenFor(parent);
                if (!isSearching()) {
                    if (parent instanceof org.eclipse.mylyn.tasks.core.ITask) {
                        // scope subtasks by query results
                        for (java.util.Iterator<org.eclipse.mylyn.tasks.core.IRepositoryElement> it = children.iterator(); it.hasNext();) {
                            org.eclipse.mylyn.tasks.core.IRepositoryElement element = it.next();
                            if (!container.getChildren().contains(element)) {
                                it.remove();
                            }
                        }
                    }
                }
                return children.toArray();
            }
        }
        return getFilteredChildrenFor(parent).toArray();
    }

    public boolean hasChildren(org.eclipse.jface.viewers.TreePath path) {
        return getChildren(path).length > 0;
    }

    public org.eclipse.jface.viewers.TreePath[] getParents(java.lang.Object element) {
        return new org.eclipse.jface.viewers.TreePath[0];
    }
}