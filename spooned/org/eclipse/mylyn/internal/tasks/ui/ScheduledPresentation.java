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
package org.eclipse.mylyn.internal.tasks.ui;
import org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListContentProvider;
import org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListPresentation;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider;
/**
 *
 * @author Mik Kersten
 */
public class ScheduledPresentation extends org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListPresentation {
    public static final java.lang.String ID = "org.eclipse.mylyn.tasks.ui.scheduled";// $NON-NLS-1$


    public ScheduledPresentation() {
        super(org.eclipse.mylyn.internal.tasks.ui.ScheduledPresentation.ID);
    }

    @java.lang.Override
    public org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListContentProvider createContentProvider(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView taskListView) {
        return new org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider(taskListView);
    }
}