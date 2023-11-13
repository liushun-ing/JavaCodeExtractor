/**
 * *****************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
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
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.ITask;
/**
 *
 * @author Steffen Pingel
 */
public class OpenSelectedTaskHandler extends org.eclipse.mylyn.internal.tasks.ui.commands.AbstractTaskListViewHandler {
    @java.lang.Override
    protected void execute(org.eclipse.core.commands.ExecutionEvent event, org.eclipse.mylyn.internal.tasks.ui.views.TaskListView taskListView, org.eclipse.mylyn.tasks.core.IRepositoryElement item) {
        if (item instanceof org.eclipse.mylyn.tasks.core.ITask) {
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.openTaskInBackground(((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (item)), true);
        }
    }
}