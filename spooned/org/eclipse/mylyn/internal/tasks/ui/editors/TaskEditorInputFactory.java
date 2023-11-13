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
package org.eclipse.mylyn.internal.tasks.ui.editors;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
/**
 *
 * @author Rob Elves
 */
public class TaskEditorInputFactory implements org.eclipse.ui.IElementFactory {
    public static final java.lang.String TAG_TASK_HANDLE = "taskHandle";// $NON-NLS-1$


    public static final java.lang.String ID_FACTORY = "org.eclipse.mylyn.tasks.ui.elementFactories.task.editor";// $NON-NLS-1$


    public org.eclipse.core.runtime.IAdaptable createElement(org.eclipse.ui.IMemento memento) {
        java.lang.String handle = memento.getString(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorInputFactory.TAG_TASK_HANDLE);
        org.eclipse.mylyn.internal.tasks.core.AbstractTask task = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().getTask(handle);
        if (task != null) {
            org.eclipse.mylyn.tasks.core.TaskRepository taskRepository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(task.getConnectorKind(), task.getRepositoryUrl());
            if (taskRepository != null) {
                return new org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput(taskRepository, task);
            } else {
                org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.WARNING, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, ((("Repository for connector kind "// $NON-NLS-1$
                 + task.getConnectorKind()) + " with url ") + task.getRepositoryUrl()) + " cannont be found."));// $NON-NLS-1$ //$NON-NLS-2$

            }
        } else {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.WARNING, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, ("Failed to restore task editor: Task with handle \"" + handle)// $NON-NLS-1$
             + "\" could not be found in task list."));// $NON-NLS-1$

        }
        return null;
    }

    public static void saveState(org.eclipse.ui.IMemento memento, org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput input) {
        if (input.getTask() != null) {
            memento.putString(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorInputFactory.TAG_TASK_HANDLE, input.getTask().getHandleIdentifier());
        }
    }
}