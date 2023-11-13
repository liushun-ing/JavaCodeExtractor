/**
 * *****************************************************************************
 * Copyright (c) 2004, 2012 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.context;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.commands.AbstractTaskHandler;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.ui.PlatformUI;
/**
 *
 * @author Steffen Pingel
 */
public class ClearContextHandler extends org.eclipse.mylyn.internal.tasks.ui.commands.AbstractTaskHandler {
    @java.lang.Override
    protected void execute(org.eclipse.core.commands.ExecutionEvent event, org.eclipse.mylyn.tasks.core.ITask task) throws org.eclipse.core.commands.ExecutionException {
        org.eclipse.mylyn.internal.tasks.ui.context.ClearContextHandler.run(task);
    }

    public static void run(org.eclipse.mylyn.tasks.core.ITask task) {
        boolean deleteConfirmed = org.eclipse.jface.dialogs.MessageDialog.openQuestion(org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages.ClearContextHandler_Confirm_clear_context, Messages.ClearContextHandler_CLEAR_THE_CONTEXT_THE_FOR_SELECTED_TASK);
        if (!deleteConfirmed) {
            return;
        }
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getContextStore().clearContext(task);
        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().notifyElementChanged(task);
    }
}