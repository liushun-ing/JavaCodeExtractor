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
package org.eclipse.mylyn.internal.tasks.ui.actions;
import org.eclipse.jface.action.Action;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
public class FilterMyTasksAction extends org.eclipse.jface.action.Action {
    public static final java.lang.String ID = "org.eclipse.mylyn.tasklist.actions.filter.myTasks";// $NON-NLS-1$


    private final org.eclipse.mylyn.internal.tasks.ui.views.TaskListView view;

    public FilterMyTasksAction(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView view) {
        this.view = view;
        setText(Messages.FilterMyTasksAction_My_Tasks);
        setToolTipText(Messages.FilterMyTasksAction_My_Tasks);
        setId(org.eclipse.mylyn.internal.tasks.ui.actions.FilterMyTasksAction.ID);
        setImageDescriptor(org.eclipse.mylyn.commons.ui.CommonImages.FILTER_MY_TASKS);
        setChecked(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().contains(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.FILTER_MY_TASKS_MODE));
    }

    @java.lang.Override
    public void run() {
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().setValue(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.FILTER_MY_TASKS_MODE, isChecked());
        if (isChecked()) {
            view.addFilter(view.getMyTasksFilter());
        } else {
            view.removeFilter(view.getMyTasksFilter());
        }
        this.view.refresh();
    }
}