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
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskActivationListener;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
/**
 *
 * @author Mik Kersten
 */
public class ToggleTaskActivationAction extends org.eclipse.jface.action.Action implements org.eclipse.mylyn.tasks.core.ITaskActivationListener {
    public static final java.lang.String ID = "org.eclipse.mylyn.tasks.ui.actions.task.activation.toggle";// $NON-NLS-1$


    private final org.eclipse.mylyn.tasks.core.ITask task;

    /**
     *
     * @param task
     * 		cannot be null
     * @param toolBarManager
     * 		cannot be null
     */
    public ToggleTaskActivationAction(org.eclipse.mylyn.tasks.core.ITask task) {
        org.eclipse.core.runtime.Assert.isNotNull(task);
        this.task = task;
        setId(org.eclipse.mylyn.internal.tasks.ui.actions.ToggleTaskActivationAction.ID);
        setImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.CONTEXT_ACTIVE_CENTERED);
        update();
        org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().addActivationListener(this);
    }

    public void dispose() {
        org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().removeActivationListener(this);
    }

    private void update() {
        setChecked(task.isActive());
        if (task.isActive()) {
            setText(Messages.ToggleTaskActivationAction_Deactivate_Task);
            setToolTipText(Messages.ToggleTaskActivationAction_Deactivate_Task);
        } else {
            setText(Messages.ToggleTaskActivationAction_Activate_Task);
            setToolTipText(Messages.ToggleTaskActivationAction_Activate_Task);
        }
    }

    @java.lang.Override
    public void run() {
        if (!task.isActive()) {
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.activateTaskThroughCommand(task);
        } else {
            org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().deactivateTask(task);
        }
        update();
    }

    public void taskActivated(org.eclipse.mylyn.tasks.core.ITask task) {
        update();
    }

    public void taskDeactivated(org.eclipse.mylyn.tasks.core.ITask task) {
        update();
    }

    public void preTaskActivated(org.eclipse.mylyn.tasks.core.ITask task) {
        // ignore
    }

    public void preTaskDeactivated(org.eclipse.mylyn.tasks.core.ITask task) {
        // ignore
    }
}