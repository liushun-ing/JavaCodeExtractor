/**
 * *****************************************************************************
 * Copyright (c) 2004, 2011 Eugene Kuleshov and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugene Kuleshov - initial API and implementation
 *     Tasktop Technologies - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.TaskActivationHistory;
import org.eclipse.mylyn.internal.tasks.ui.actions.ActivateTaskDialogAction;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
/**
 *
 * @author Eugene Kuleshov
 * @author Steffen Pingel
 */
public class TaskHistoryHandler extends org.eclipse.core.commands.AbstractHandler implements org.eclipse.ui.commands.IElementUpdater {
    public java.lang.Object execute(final org.eclipse.core.commands.ExecutionEvent event) throws org.eclipse.core.commands.ExecutionException {
        if (org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().getActiveTask() != null) {
            org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().deactivateActiveTask();
        } else {
            org.eclipse.mylyn.internal.tasks.core.TaskActivationHistory taskHistory = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().getTaskActivationHistory();
            if (taskHistory.hasPrevious()) {
                org.eclipse.mylyn.internal.tasks.core.AbstractTask previousTask = taskHistory.getPreviousTask();
                if ((previousTask != null) && (!previousTask.isActive())) {
                    org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.activateTaskThroughCommand(previousTask);
                }
            } else {
                org.eclipse.ui.IWorkbenchWindow window = org.eclipse.ui.handlers.HandlerUtil.getActiveWorkbenchWindow(event);
                if (window != null) {
                    org.eclipse.mylyn.internal.tasks.ui.actions.ActivateTaskDialogAction action = new org.eclipse.mylyn.internal.tasks.ui.actions.ActivateTaskDialogAction();
                    action.init(window);
                    action.run(null);
                }
            }
        }
        return null;
    }

    @java.lang.SuppressWarnings("rawtypes")
    public void updateElement(org.eclipse.ui.menus.UIElement element, java.util.Map parameters) {
        if (org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().getActiveTask() == null) {
            element.setIcon(org.eclipse.mylyn.tasks.ui.TasksUiImages.CONTEXT_HISTORY_PREVIOUS);
        } else {
            element.setIcon(org.eclipse.mylyn.tasks.ui.TasksUiImages.CONTEXT_HISTORY_PREVIOUS_ACTIVE);
        }
    }
}