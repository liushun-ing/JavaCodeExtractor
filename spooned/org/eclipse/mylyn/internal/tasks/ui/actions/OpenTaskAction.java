/**
 * *****************************************************************************
 * Copyright (c) 2004, 2011 Willian Mitsuda and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Willian Mitsuda - initial API and implementation
 *     Abner Ballardo - fixes for bug 349003
 *     Tasktop Technologies - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.actions;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.actions.ActionDelegate;
/**
 *
 * @author Willian Mitsuda
 * @author Abner Ballardo
 * @author Steffen Pingel
 */
public class OpenTaskAction extends org.eclipse.ui.actions.ActionDelegate implements org.eclipse.ui.IWorkbenchWindowActionDelegate {
    private org.eclipse.ui.IWorkbenchWindow window;

    public void init(org.eclipse.ui.IWorkbenchWindow window) {
        this.window = window;
    }

    @java.lang.Override
    public void run(org.eclipse.jface.action.IAction action) {
        org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog dlg = new org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog(window.getShell(), true);
        dlg.setTitle(Messages.OpenTaskAction_Open_Task);
        dlg.setMessage(Messages.OpenTaskAction_Select_a_task_to_open__);
        dlg.setShowExtendedOpeningOptions(true);
        if (dlg.open() != org.eclipse.jface.window.Window.OK) {
            return;
        }
        if (dlg.getResult() != null) {
            for (java.lang.Object result : dlg.getResult()) {
                if (result instanceof org.eclipse.mylyn.tasks.core.ITask) {
                    org.eclipse.mylyn.internal.tasks.core.AbstractTask task = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (result));
                    if (dlg.getOpenInBrowser()) {
                        org.eclipse.mylyn.tasks.ui.TasksUiUtil.openWithBrowser(task);
                    } else {
                        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.refreshAndOpenTaskListElement(task);
                    }
                }
            }
        }
    }
}