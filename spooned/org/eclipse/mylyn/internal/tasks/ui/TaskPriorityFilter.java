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
 *     Ken Sueda - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylyn.tasks.core.ITask.PriorityLevel;
/**
 *
 * @author Mik Kersten
 * @author Ken Sueda
 */
public class TaskPriorityFilter extends org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter {
    private static final java.lang.String PRIORITY_PREFIX = "P";// $NON-NLS-1$


    private java.lang.String priorityLevel = org.eclipse.mylyn.tasks.core.ITask.PriorityLevel.P5.toString();

    public TaskPriorityFilter() {
        displayPrioritiesAbove(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.getCurrentPriorityLevel());
    }

    public void displayPrioritiesAbove(java.lang.String level) {
        priorityLevel = level;
    }

    @java.lang.Override
    public boolean select(java.lang.Object parent, java.lang.Object element) {
        if (element instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer) {
            org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer taskContainer = ((org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer) (element));
            java.lang.String priority = taskContainer.getPriority();
            if ((priority == null) || (!priority.startsWith(org.eclipse.mylyn.internal.tasks.ui.TaskPriorityFilter.PRIORITY_PREFIX))) {
                return true;
            }
            if (priorityLevel.compareTo(taskContainer.getPriority()) >= 0) {
                return true;
            }
            return false;
        }
        return true;
    }
}