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
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.handlers.HandlerUtil;
/**
 *
 * @author Steffen Pingel
 */
public abstract class AbstractTaskListViewHandler extends org.eclipse.core.commands.AbstractHandler {
    public java.lang.Object execute(org.eclipse.core.commands.ExecutionEvent event) throws org.eclipse.core.commands.ExecutionException {
        org.eclipse.ui.IWorkbenchSite site = org.eclipse.ui.handlers.HandlerUtil.getActiveSite(event);
        if (site instanceof org.eclipse.ui.IViewSite) {
            org.eclipse.ui.IViewSite viewSite = ((org.eclipse.ui.IViewSite) (site));
            org.eclipse.ui.IWorkbenchPart part = viewSite.getPart();
            if (part instanceof org.eclipse.mylyn.internal.tasks.ui.views.TaskListView) {
                org.eclipse.mylyn.internal.tasks.ui.views.TaskListView taskListView = ((org.eclipse.mylyn.internal.tasks.ui.views.TaskListView) (part));
                execute(event, taskListView);
            }
        }
        return null;
    }

    protected void execute(org.eclipse.core.commands.ExecutionEvent event, org.eclipse.mylyn.internal.tasks.ui.views.TaskListView taskListView) throws org.eclipse.core.commands.ExecutionException {
        org.eclipse.jface.viewers.ITreeSelection selection = ((org.eclipse.jface.viewers.ITreeSelection) (taskListView.getViewer().getSelection()));
        for (java.util.Iterator<?> it = selection.iterator(); it.hasNext();) {
            java.lang.Object item = it.next();
            if (item instanceof org.eclipse.mylyn.tasks.core.IRepositoryElement) {
                execute(event, taskListView, ((org.eclipse.mylyn.tasks.core.IRepositoryElement) (item)));
            }
        }
    }

    protected void execute(org.eclipse.core.commands.ExecutionEvent event, org.eclipse.mylyn.internal.tasks.ui.views.TaskListView taskListView, org.eclipse.mylyn.tasks.core.IRepositoryElement item) throws org.eclipse.core.commands.ExecutionException {
    }
}