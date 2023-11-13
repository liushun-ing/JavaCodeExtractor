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
package org.eclipse.mylyn.internal.tasks.ui.actions;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.mylyn.commons.workbench.AdaptiveRefreshPolicy.IFilteredTreeListener;
import org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter;
import org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListInterestFilter;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListInterestSorter;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
/**
 *
 * @author Mik Kersten
 * @author Steffen Pingel
 */
public class FocusTaskListAction implements org.eclipse.mylyn.commons.workbench.AdaptiveRefreshPolicy.IFilteredTreeListener , org.eclipse.ui.IViewActionDelegate , org.eclipse.ui.IActionDelegate2 {
    private java.util.Set<org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter> previousFilters = new java.util.HashSet<org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter>();

    private org.eclipse.jface.viewers.ViewerSorter previousSorter;

    private final org.eclipse.mylyn.internal.tasks.ui.views.TaskListInterestFilter taskListInterestFilter = new org.eclipse.mylyn.internal.tasks.ui.views.TaskListInterestFilter();

    private final org.eclipse.mylyn.internal.tasks.ui.views.TaskListInterestSorter taskListInterestSorter = new org.eclipse.mylyn.internal.tasks.ui.views.TaskListInterestSorter();

    private org.eclipse.mylyn.internal.tasks.ui.views.TaskListView taskListView;

    private org.eclipse.jface.action.IAction action;

    public FocusTaskListAction() {
    }

    public void filterTextChanged(final java.lang.String text) {
        if (taskListView.isFocusedMode() && ((text == null) || "".equals(text.trim()))) {
            // $NON-NLS-1$
            taskListView.getViewer().expandAll();
        }
    }

    public void init(org.eclipse.jface.action.IAction action) {
        this.action = action;
        initAction();
    }

    public void init(org.eclipse.ui.IViewPart view) {
        if (view instanceof org.eclipse.mylyn.internal.tasks.ui.views.TaskListView) {
            taskListView = ((org.eclipse.mylyn.internal.tasks.ui.views.TaskListView) (view));
            taskListView.getFilteredTree().getRefreshPolicy().addListener(this);
            taskListView.getFilteredTree().addDisposeListener(new org.eclipse.swt.events.DisposeListener() {
                public void widgetDisposed(org.eclipse.swt.events.DisposeEvent e) {
                    taskListView.getFilteredTree().getRefreshPolicy().removeListener(FocusTaskListAction.this);
                }
            });
            if (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.TASK_LIST_FOCUSED)) {
                installInterestFilter();
            }
            initAction();
            showProgressBar(taskListView.isFocusedMode());
        }
    }

    private void initAction() {
        if ((action != null) && (taskListView != null)) {
            action.setChecked(taskListView.isFocusedMode());
        }
    }

    public void selectionChanged(org.eclipse.jface.action.IAction action, org.eclipse.jface.viewers.ISelection selection) {
        // ignore
    }

    private void showProgressBar(boolean visible) {
        taskListView.getFilteredTree().setShowProgress(visible);
    }

    protected void installInterestFilter() {
        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.preservingSelection(taskListView.getViewer(), new java.lang.Runnable() {
            public void run() {
                try {
                    taskListView.getFilteredTree().setRedraw(false);
                    taskListView.setFocusedMode(true);
                    previousSorter = taskListView.getViewer().getSorter();
                    previousFilters = taskListView.clearFilters();
                    if (!taskListView.getFilters().contains(taskListInterestFilter)) {
                        taskListView.addFilter(taskListInterestFilter);
                    }
                    // Setting sorter causes root refresh
                    taskListInterestSorter.setconfiguredSorter(previousSorter);
                    taskListView.getViewer().setSorter(taskListInterestSorter);
                    taskListView.getViewer().expandAll();
                    showProgressBar(true);
                } finally {
                    taskListView.getFilteredTree().setRedraw(true);
                }
            }
        });
    }

    protected void uninstallInterestFilter() {
        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.preservingSelection(taskListView.getViewer(), new java.lang.Runnable() {
            public void run() {
                try {
                    taskListView.getViewer().getControl().setRedraw(false);
                    taskListView.setFocusedMode(false);
                    for (org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter filter : previousFilters) {
                        taskListView.addFilter(filter);
                    }
                    taskListView.removeFilter(taskListInterestFilter);
                    org.eclipse.swt.widgets.Text textControl = taskListView.getFilteredTree().getFilterControl();
                    if ((textControl != null) && (textControl.getText().length() > 0)) {
                        taskListView.getViewer().expandAll();
                    } else {
                        taskListView.getViewer().collapseAll();
                        // expand first element (Today) in scheduled mode
                        if ((taskListView.getViewer().getContentProvider() instanceof org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider) && (taskListView.getViewer().getTree().getItemCount() > 0)) {
                            org.eclipse.swt.widgets.TreeItem item = taskListView.getViewer().getTree().getItem(0);
                            if (item.getData() != null) {
                                taskListView.getViewer().expandToLevel(item.getData(), 1);
                            }
                        }
                    }
                    taskListView.getViewer().setSorter(previousSorter);
                    showProgressBar(false);
                } finally {
                    taskListView.getViewer().getControl().setRedraw(true);
                }
            }
        });
    }

    public void run() {
        if (taskListView == null) {
            return;
        }
        if (!taskListView.isFocusedMode()) {
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().setValue(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.TASK_LIST_FOCUSED, true);
            installInterestFilter();
        } else {
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().setValue(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.TASK_LIST_FOCUSED, false);
            uninstallInterestFilter();
        }
    }

    public void run(org.eclipse.jface.action.IAction action) {
        run();
    }

    public void runWithEvent(org.eclipse.jface.action.IAction action, org.eclipse.swt.widgets.Event event) {
        run();
    }

    public void dispose() {
        // ignore
    }
}