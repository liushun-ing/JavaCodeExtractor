/**
 * *****************************************************************************
 * Copyright (c) 2013 Tasktop Technologies and others.
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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.workbench.DelayedRefreshJob;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.ITaskListChangeListener;
import org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta;
import org.eclipse.mylyn.internal.tasks.core.UncategorizedTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.UnmatchedTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.UnsubmittedTaskContainer;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskListChangeAdapter;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker;
import org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker.TreeVisitor;
import org.eclipse.mylyn.tasks.core.ITaskContainer;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
public final class TaskListRefreshJob extends org.eclipse.mylyn.commons.workbench.DelayedRefreshJob {
    private final org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListView taskListView;

    private final org.eclipse.mylyn.internal.tasks.core.ITaskListChangeListener TASKLIST_CHANGE_LISTENER = new org.eclipse.mylyn.internal.tasks.ui.editors.TaskListChangeAdapter() {
        @java.lang.Override
        public void containersChanged(final java.util.Set<org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta> deltas) {
            org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                public void run() {
                    for (org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta taskContainerDelta : deltas) {
                        if (taskListView.isScheduledPresentation()) {
                            // TODO: implement refresh policy for scheduled presentation
                            refresh();
                        } else {
                            switch (taskContainerDelta.getKind()) {
                                case ROOT :
                                    refresh();
                                    break;
                                case ADDED :
                                case REMOVED :
                                    if (isFilteredContainer(taskContainerDelta)) {
                                        // container may have changed visibility, refresh root
                                        refresh();
                                    } else {
                                        if (taskContainerDelta.getElement() != null) {
                                            refreshElement(taskContainerDelta.getElement());
                                        }
                                        if (taskContainerDelta.getParent() != null) {
                                            refreshElement(taskContainerDelta.getParent());
                                        } else {
                                            // element was added/removed from the root
                                            refresh();
                                        }
                                    }
                                    break;
                                case CONTENT :
                                    refreshElement(taskContainerDelta.getElement());
                            }
                        }
                    }
                }

                private boolean isFilteredContainer(org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta taskContainerDelta) {
                    org.eclipse.mylyn.tasks.core.ITaskContainer parent = taskContainerDelta.getParent();
                    return ((parent instanceof org.eclipse.mylyn.internal.tasks.core.UnsubmittedTaskContainer) || (parent instanceof org.eclipse.mylyn.internal.tasks.core.UnmatchedTaskContainer)) || (parent instanceof org.eclipse.mylyn.internal.tasks.core.UncategorizedTaskContainer);
                }
            });
        }
    };

    public TaskListRefreshJob(org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListView taskListView, org.eclipse.jface.viewers.TreeViewer treeViewer, java.lang.String name) {
        super(treeViewer, name);
        this.taskListView = taskListView;
        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().addChangeListener(TASKLIST_CHANGE_LISTENER);
    }

    @java.lang.Override
    protected void doRefresh(java.lang.Object[] items) {
        org.eclipse.jface.viewers.TreePath selection = preserveSelection();
        if (items == null) {
            viewer.refresh(true);
        } else if (items.length > 0) {
            try {
                if (taskListView.isFocusedMode()) {
                    java.util.Set<java.lang.Object> children = new java.util.HashSet<java.lang.Object>(java.util.Arrays.asList(items));
                    java.util.Set<org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer> parents = new java.util.HashSet<org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer>();
                    for (java.lang.Object item : items) {
                        if (item instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask) {
                            parents.addAll(((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (item)).getParentContainers());
                        }
                    }
                    // 1. refresh parents
                    children.removeAll(parents);
                    for (org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer parent : parents) {
                        viewer.refresh(parent, false);
                        // only refresh label of parent
                        viewer.update(parent, null);
                    }
                    // 2. refresh children
                    for (java.lang.Object item : children) {
                        viewer.refresh(item, true);
                    }
                    // 3. update states of all changed items
                    for (java.lang.Object item : items) {
                        updateExpansionState(item);
                    }
                } else {
                    java.util.Set<org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer> parents = new java.util.HashSet<org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer>();
                    for (java.lang.Object item : items) {
                        if (item instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask) {
                            parents.addAll(((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (item)).getParentContainers());
                        }
                        viewer.refresh(item, true);
                        updateExpansionState(item);
                    }
                    // refresh labels of parents for task activation or incoming indicators
                    for (org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer parent : parents) {
                        // only refresh label
                        viewer.update(parent, null);
                    }
                }
            } catch (org.eclipse.swt.SWTException e) {
                org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Failed to refresh viewer: "// $NON-NLS-1$
                 + viewer, e));
            }
        }
        taskListView.updateToolTip(false);
        restoreSelection(selection);
    }

    private org.eclipse.jface.viewers.TreePath preserveSelection() {
        if (viewer instanceof org.eclipse.jface.viewers.TreeViewer) {
            org.eclipse.jface.viewers.TreeViewer treeViewer = ((org.eclipse.jface.viewers.TreeViewer) (viewer));
            // in case the refresh removes the currently selected item,
            // remember the next item in the tree to restore the selection
            // TODO: consider making this optional
            org.eclipse.swt.widgets.TreeItem[] selection = treeViewer.getTree().getSelection();
            if (selection.length > 0) {
                org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker treeWalker = new org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker(treeViewer);
                return treeWalker.walk(new org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker.TreeVisitor() {
                    @java.lang.Override
                    public boolean visit(java.lang.Object object) {
                        return true;
                    }
                }, selection[selection.length - 1]);
            }
        }
        return null;
    }

    private void restoreSelection(org.eclipse.jface.viewers.TreePath treePath) {
        if (treePath != null) {
            org.eclipse.jface.viewers.ISelection newSelection = viewer.getSelection();
            if ((newSelection == null) || newSelection.isEmpty()) {
                viewer.setSelection(new org.eclipse.jface.viewers.TreeSelection(treePath), true);
            }
        }
    }

    protected void updateExpansionState(java.lang.Object item) {
        if (taskListView.isFocusedMode() && taskListView.isAutoExpandMode()) {
            taskListView.getViewer().expandToLevel(item, 3);
        }
    }

    public void dispose() {
        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().removeChangeListener(TASKLIST_CHANGE_LISTENER);
    }
}