/**
 * *****************************************************************************
 * Copyright (c) 2009 Tasktop Technologies and others.
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
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.ui.TasksUi;
/**
 *
 * @author Steffen Pingel
 */
public class DeactivateTaskHandler extends org.eclipse.mylyn.internal.tasks.ui.commands.AbstractTaskHandler {
    public DeactivateTaskHandler() {
        singleTask = true;
    }

    @java.lang.Override
    protected void execute(org.eclipse.core.commands.ExecutionEvent event, org.eclipse.mylyn.tasks.core.ITask task) throws org.eclipse.core.commands.ExecutionException {
        org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().deactivateTask(task);
    }
}