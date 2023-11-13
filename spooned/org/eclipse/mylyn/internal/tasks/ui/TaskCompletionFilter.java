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
package org.eclipse.mylyn.internal.tasks.ui;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.ui.views.PresentationFilter;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.Completed;
import org.eclipse.mylyn.tasks.core.ITask;
/**
 *
 * @author Mik Kersten
 * @author Shawn Minto
 */
public class TaskCompletionFilter extends org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter {
    @java.lang.Override
    public boolean select(java.lang.Object parent, java.lang.Object element) {
        if (element instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask) {
            org.eclipse.mylyn.internal.tasks.core.AbstractTask task = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (element));
            if (task.isCompleted()) {
                java.util.Collection<org.eclipse.mylyn.tasks.core.ITask> children = task.getChildren();
                for (org.eclipse.mylyn.tasks.core.ITask child : children) {
                    if (org.eclipse.mylyn.internal.tasks.ui.views.PresentationFilter.getInstance().select(element, child) && select(element, child)) {
                        return true;
                    }
                }
                // hide completed task
                return false;
            }
        }
        // hide the completed category when not showing completed tasks
        if (element instanceof org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.Completed) {
            return false;
        }
        return true;
    }
}