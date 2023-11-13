/**
 * *****************************************************************************
 * Copyright (c) 2012, 2013 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.util;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.ExtensionPointReader;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.ui.TaskDropListener;
import org.eclipse.mylyn.tasks.ui.TaskDropListener.TaskDropEvent;
/**
 *
 * @author Sam Davis
 */
public class TaskDropHandler {
    private java.util.List<org.eclipse.mylyn.tasks.ui.TaskDropListener> taskDropListeners;

    TaskDropHandler() {
        // package visible
    }

    public void loadTaskDropListeners() {
        if (taskDropListeners == null) {
            org.eclipse.mylyn.commons.core.ExtensionPointReader<org.eclipse.mylyn.tasks.ui.TaskDropListener> reader = new org.eclipse.mylyn.commons.core.ExtensionPointReader<org.eclipse.mylyn.tasks.ui.TaskDropListener>(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "taskDropListener", "listener", org.eclipse.mylyn.tasks.ui.TaskDropListener.class);// $NON-NLS-1$//$NON-NLS-2$

            reader.read();
            taskDropListeners = reader.getItems();
        }
    }

    public void fireTaskDropped(final java.util.List<org.eclipse.mylyn.tasks.core.ITask> tasksToMove, final org.eclipse.mylyn.tasks.core.ITask currentTarget, org.eclipse.mylyn.tasks.ui.TaskDropListener.Operation operation) {
        final org.eclipse.mylyn.tasks.ui.TaskDropListener.TaskDropEvent event = new org.eclipse.mylyn.tasks.ui.TaskDropListener.TaskDropEvent(tasksToMove, currentTarget, operation);
        for (final org.eclipse.mylyn.tasks.ui.TaskDropListener listener : taskDropListeners) {
            org.eclipse.core.runtime.SafeRunner.run(new org.eclipse.core.runtime.ISafeRunnable() {
                public void run() throws java.lang.Exception {
                    listener.tasksDropped(event);
                }

                public void handleException(java.lang.Throwable exception) {
                    org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, exception.getMessage(), exception));
                }
            });
        }
    }
}