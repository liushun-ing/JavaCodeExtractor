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
package org.eclipse.mylyn.internal.tasks.ui.context;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog;
import org.eclipse.mylyn.internal.tasks.ui.commands.AbstractTaskHandler;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.ui.PlatformUI;
/**
 *
 * @author Steffen Pingel
 */
public class CopyContextHandler extends org.eclipse.mylyn.internal.tasks.ui.commands.AbstractTaskHandler {
    private static final java.lang.String TITLE_DIALOG = Messages.CopyContextHandler_Copy_Context;

    @java.lang.Override
    protected void execute(org.eclipse.core.commands.ExecutionEvent event, org.eclipse.mylyn.tasks.core.ITask sourceTask) throws org.eclipse.core.commands.ExecutionException {
        org.eclipse.mylyn.internal.tasks.ui.context.CopyContextHandler.run(sourceTask);
    }

    public static void run(org.eclipse.mylyn.tasks.core.ITask sourceTask) {
        if (sourceTask == null) {
            org.eclipse.jface.dialogs.MessageDialog.openInformation(org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), org.eclipse.mylyn.internal.tasks.ui.context.CopyContextHandler.TITLE_DIALOG, Messages.CopyContextHandler_No_source_task_selected);
            return;
        }
        org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog dialog = new org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell());
        dialog.setNeedsCreateTask(false);
        dialog.setTitle(Messages.CopyContextHandler_Select_Target_Task);
        dialog.setMessage(Messages.CopyContextHandler_Select_the_target_task__);
        if (dialog.open() != org.eclipse.jface.window.Window.OK) {
            return;
        }
        java.lang.Object result = dialog.getFirstResult();
        if (result instanceof org.eclipse.mylyn.tasks.core.ITask) {
            org.eclipse.mylyn.tasks.core.ITask targetTask = ((org.eclipse.mylyn.tasks.core.ITask) (result));
            org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().deactivateActiveTask();
            if (targetTask.equals(sourceTask)) {
                org.eclipse.jface.dialogs.MessageDialog.openInformation(org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), org.eclipse.mylyn.internal.tasks.ui.context.CopyContextHandler.TITLE_DIALOG, Messages.CopyContextHandler_TARGET_TASK_CON_NOT_BE_THE_SAME_AS_SOURCE_TASK);
            } else {
                final int REPLACE = 0;
                final int MERGE = 1;
                final int CANCEL = 2;
                int action = REPLACE;
                if (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getContextStore().hasContext(targetTask)) {
                    org.eclipse.jface.dialogs.MessageDialog dialog2 = new org.eclipse.jface.dialogs.MessageDialog(org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), org.eclipse.mylyn.internal.tasks.ui.context.CopyContextHandler.TITLE_DIALOG, null, Messages.CopyContextHandler_SELECTED_TASK_ALREADY_HAS_CONTEXT, org.eclipse.jface.dialogs.MessageDialog.QUESTION, new java.lang.String[]{ Messages.CopyContextHandler_Replace, Messages.CopyContextHandler_Merge, org.eclipse.jface.dialogs.IDialogConstants.CANCEL_LABEL }, 1);
                    action = dialog2.open();
                }
                switch (action) {
                    case REPLACE :
                        org.eclipse.core.runtime.IAdaptable context = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getContextStore().copyContext(sourceTask, targetTask);
                        if (context == null) {
                            org.eclipse.jface.dialogs.MessageDialog.openInformation(org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), org.eclipse.mylyn.internal.tasks.ui.context.CopyContextHandler.TITLE_DIALOG, Messages.CopyContextHandler_SOURCE_TASK_DOES_HAVE_A_CONTEXT);
                            return;
                        }
                        break;
                    case MERGE :
                        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getContextStore().mergeContext(sourceTask, targetTask);
                        break;
                    case CANCEL :
                        return;
                }
                org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.activateTaskThroughCommand(targetTask);
            }
        } else {
            org.eclipse.jface.dialogs.MessageDialog.openInformation(org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), org.eclipse.mylyn.internal.tasks.ui.context.CopyContextHandler.TITLE_DIALOG, Messages.CopyContextHandler_No_target_task_selected);
        }
    }
}