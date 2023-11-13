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
package org.eclipse.mylyn.internal.tasks.ui.actions;
import org.eclipse.jface.action.Action;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
/**
 *
 * @author Mik Kersten
 */
public class FilterCompletedTasksAction extends org.eclipse.jface.action.Action {
    public static final java.lang.String ID = "org.eclipse.mylyn.tasklist.actions.filter.completed";// $NON-NLS-1$


    private final org.eclipse.mylyn.internal.tasks.ui.views.TaskListView view;

    public FilterCompletedTasksAction(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView view) {
        this.view = view;
        setText(Messages.FilterCompletedTasksAction_Filter_Completed_Tasks);
        setToolTipText(Messages.FilterCompletedTasksAction_Filter_Completed_Tasks);
        setId(org.eclipse.mylyn.internal.tasks.ui.actions.FilterCompletedTasksAction.ID);
        setImageDescriptor(org.eclipse.mylyn.commons.ui.CommonImages.FILTER_COMPLETE);
        setChecked(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().contains(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.FILTER_COMPLETE_MODE));
    }

    @java.lang.Override
    public void run() {
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().setValue(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.FILTER_COMPLETE_MODE, isChecked());
        if (isChecked()) {
            view.addFilter(view.getCompleteFilter());
        } else {
            view.removeFilter(view.getCompleteFilter());
        }
        this.view.refresh();
    }
}