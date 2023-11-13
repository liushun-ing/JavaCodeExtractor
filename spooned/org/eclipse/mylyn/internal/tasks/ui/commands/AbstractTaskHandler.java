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
package org.eclipse.mylyn.internal.tasks.ui.commands;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskContainer;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
/**
 *
 * @author Steffen Pingel
 */
public abstract class AbstractTaskHandler extends org.eclipse.core.commands.AbstractHandler {
    protected boolean recurse;

    protected boolean singleTask;

    private boolean filterBasedOnActiveTaskList;

    public AbstractTaskHandler() {
    }

    public java.lang.Object execute(org.eclipse.core.commands.ExecutionEvent event) throws org.eclipse.core.commands.ExecutionException {
        org.eclipse.jface.viewers.ISelection selection = org.eclipse.ui.handlers.HandlerUtil.getActiveMenuSelection(event);
        if ((selection == null) || selection.isEmpty()) {
            selection = org.eclipse.ui.handlers.HandlerUtil.getCurrentSelection(event);
        }
        boolean processed = process(event, selection);
        if (!processed) {
            // fall back to processing task currently visible in the editor
            org.eclipse.ui.IWorkbenchPart part = org.eclipse.ui.handlers.HandlerUtil.getActivePart(event);
            if (part instanceof org.eclipse.mylyn.tasks.ui.editors.TaskEditor) {
                selection = new org.eclipse.jface.viewers.StructuredSelection(((org.eclipse.mylyn.tasks.ui.editors.TaskEditor) (part)).getTaskEditorInput().getTask());
                processed = process(event, selection);
            }
        }
        return null;
    }

    private boolean process(org.eclipse.core.commands.ExecutionEvent event, org.eclipse.jface.viewers.ISelection selection) throws org.eclipse.core.commands.ExecutionException {
        boolean processed = false;
        if (selection instanceof org.eclipse.jface.viewers.IStructuredSelection) {
            java.lang.Object[] items = ((org.eclipse.jface.viewers.IStructuredSelection) (selection)).toArray();
            if (singleTask) {
                if ((items.length == 1) && (items[0] instanceof org.eclipse.mylyn.tasks.core.ITask)) {
                    processed |= process(event, items, false);
                }
            } else {
                processed |= process(event, items, recurse);
            }
        }
        return processed;
    }

    private boolean process(org.eclipse.core.commands.ExecutionEvent event, java.lang.Object[] items, boolean recurse) throws org.eclipse.core.commands.ExecutionException {
        org.eclipse.mylyn.tasks.core.ITask[] tasks = collectTasks(items, recurse);
        if (tasks != null) {
            execute(event, tasks);
            return true;
        }
        return false;
    }

    private org.eclipse.mylyn.tasks.core.ITask[] collectTasks(java.lang.Object[] items, boolean recurse) {
        java.util.Set<org.eclipse.mylyn.tasks.core.ITask> result = new java.util.HashSet<org.eclipse.mylyn.tasks.core.ITask>(items.length);
        for (int i = 0; i < items.length; i++) {
            if (!(items[i] instanceof org.eclipse.mylyn.tasks.core.IRepositoryElement)) {
                items[i] = org.eclipse.core.runtime.Platform.getAdapterManager().getAdapter(items[i], org.eclipse.mylyn.tasks.core.ITask.class);
            }
        }
        getChildren(items, recurse, result);
        getTasks(items, result);
        return result.toArray(new org.eclipse.mylyn.tasks.core.ITask[result.size()]);
    }

    private void getChildren(java.lang.Object[] items, boolean recurse, java.util.Set<org.eclipse.mylyn.tasks.core.ITask> result) {
        for (java.lang.Object item : items) {
            if ((item instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) && (recurse || (!(item instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask)))) {
                getFilteredChildren(((org.eclipse.mylyn.tasks.core.ITaskContainer) (item)), result);
            }
        }
    }

    private void getTasks(java.lang.Object[] items, java.util.Set<org.eclipse.mylyn.tasks.core.ITask> result) {
        for (java.lang.Object item : items) {
            if (item instanceof org.eclipse.mylyn.tasks.core.ITask) {
                result.add(((org.eclipse.mylyn.tasks.core.ITask) (item)));
            }
        }
    }

    protected void getFilteredChildren(org.eclipse.mylyn.tasks.core.ITaskContainer item, java.util.Set<org.eclipse.mylyn.tasks.core.ITask> result) {
        for (org.eclipse.mylyn.tasks.core.ITask task : item.getChildren()) {
            if ((!filterBasedOnActiveTaskList) || org.eclipse.mylyn.internal.tasks.ui.commands.AbstractTaskHandler.isVisibleInTaskList(item, task)) {
                result.add(task);
            }
        }
    }

    public static boolean isVisibleInTaskList(org.eclipse.mylyn.tasks.core.ITaskContainer item, org.eclipse.mylyn.tasks.core.ITask task) {
        org.eclipse.mylyn.internal.tasks.ui.views.TaskListView taskListView = org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.getFromActivePerspective();
        if (taskListView == null) {
            return false;
        }
        java.util.Set<org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter> filters = taskListView.getFilters();
        for (org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter filter : filters) {
            if (!filter.select(item, task)) {
                return false;
            }
        }
        return true;
    }

    protected void execute(org.eclipse.core.commands.ExecutionEvent event, org.eclipse.mylyn.tasks.core.ITask task) throws org.eclipse.core.commands.ExecutionException {
    }

    protected void execute(org.eclipse.core.commands.ExecutionEvent event, org.eclipse.mylyn.tasks.core.ITask[] tasks) throws org.eclipse.core.commands.ExecutionException {
        for (org.eclipse.mylyn.tasks.core.ITask task : tasks) {
            execute(event, task);
        }
    }

    public boolean getFilterBasedOnActiveTaskList() {
        return filterBasedOnActiveTaskList;
    }

    protected void setFilterBasedOnActiveTaskList(boolean filterBasedOnActiveTaskList) {
        this.filterBasedOnActiveTaskList = filterBasedOnActiveTaskList;
    }
}