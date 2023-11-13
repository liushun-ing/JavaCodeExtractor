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
package org.eclipse.mylyn.internal.tasks.ui.actions;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.internal.tasks.ui.dialogs.TaskListSortDialog;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.ui.IWorkbenchPartSite;
/**
 *
 * @author Mik Kersten
 */
public class TaskListSortAction extends org.eclipse.jface.action.Action {
    private final org.eclipse.mylyn.internal.tasks.ui.dialogs.TaskListSortDialog dialog;

    private final org.eclipse.mylyn.internal.tasks.ui.views.TaskListView taskListView;

    public TaskListSortAction(org.eclipse.ui.IWorkbenchPartSite site, org.eclipse.mylyn.internal.tasks.ui.views.TaskListView taskListView) {
        super(Messages.TaskListSortAction_Sort_);
        this.taskListView = taskListView;
        setEnabled(true);
        dialog = new org.eclipse.mylyn.internal.tasks.ui.dialogs.TaskListSortDialog(site, taskListView);
    }

    @java.lang.Override
    public void run() {
        if (dialog.open() == org.eclipse.jface.window.Window.OK) {
            taskListView.getViewer().refresh();
        }
    }
}