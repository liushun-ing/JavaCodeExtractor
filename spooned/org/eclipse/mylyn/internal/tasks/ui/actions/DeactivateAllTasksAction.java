/**
 * *****************************************************************************
 * Copyright (c) 2004, 2008 Willian Mitsuda and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Willian Mitsuda - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.actions;
import org.eclipse.jface.action.IAction;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.actions.ActionDelegate;
/**
 *
 * @author Willian Mitsuda
 */
public class DeactivateAllTasksAction extends org.eclipse.ui.actions.ActionDelegate implements org.eclipse.ui.IWorkbenchWindowActionDelegate {
    public void init(org.eclipse.ui.IWorkbenchWindow window) {
        // ignore
    }

    @java.lang.Override
    public void run(org.eclipse.jface.action.IAction action) {
        org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().deactivateActiveTask();
    }
}