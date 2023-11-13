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
import org.eclipse.mylyn.internal.tasks.ui.actions.NewSubTaskAction;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.handlers.HandlerUtil;
/**
 *
 * @author Shawn Minto
 */
public class NewSubTaskHandler extends org.eclipse.core.commands.AbstractHandler {
    public java.lang.Object execute(org.eclipse.core.commands.ExecutionEvent event) throws org.eclipse.core.commands.ExecutionException {
        org.eclipse.ui.IWorkbenchSite site = org.eclipse.ui.handlers.HandlerUtil.getActiveSite(event);
        if (site instanceof org.eclipse.ui.IViewSite) {
            org.eclipse.ui.IViewSite viewSite = ((org.eclipse.ui.IViewSite) (site));
            org.eclipse.ui.IWorkbenchPart part = viewSite.getPart();
            if (part instanceof org.eclipse.mylyn.internal.tasks.ui.views.TaskListView) {
                org.eclipse.mylyn.internal.tasks.ui.views.TaskListView taskListView = ((org.eclipse.mylyn.internal.tasks.ui.views.TaskListView) (part));
                org.eclipse.mylyn.internal.tasks.ui.actions.NewSubTaskAction action = new org.eclipse.mylyn.internal.tasks.ui.actions.NewSubTaskAction();
                action.selectionChanged(action, taskListView.getViewer().getSelection());
                if (action.isEnabled()) {
                    action.run();
                }
                return null;
            }
        }
        return null;
    }
}