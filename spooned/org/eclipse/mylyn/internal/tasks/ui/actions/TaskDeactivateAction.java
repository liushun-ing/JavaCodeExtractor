/**
 * *****************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.actions;
import org.eclipse.jface.action.Action;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
/**
 *
 * @author Mik Kersten
 */
public class TaskDeactivateAction extends org.eclipse.jface.action.Action {
    public static final java.lang.String ID = "org.eclipse.mylyn.tasklist.actions.context.deactivate";// $NON-NLS-1$


    public TaskDeactivateAction() {
        setId(org.eclipse.mylyn.internal.tasks.ui.actions.TaskDeactivateAction.ID);
        setActionDefinitionId("org.eclipse.mylyn.tasks.ui.command.deactivateSelectedTask");// $NON-NLS-1$

        setText(Messages.TaskDeactivateAction_Deactivate);
        setImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.CONTEXT_INACTIVE_EMPTY);
    }

    @java.lang.Deprecated
    public void run(org.eclipse.mylyn.tasks.core.ITask task) {
        org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().deactivateTask(task);
    }

    @java.lang.Override
    public void run() {
        org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().deactivateActiveTask();
    }
}