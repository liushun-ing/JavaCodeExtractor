/**
 * *****************************************************************************
 * Copyright (c) 2004, 2012 Eugene Kuleshov and others.
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
package org.eclipse.mylyn.internal.tasks.ui.workingsets;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
/**
 * Element factory used to restore task containers and projects for Task+Resource working sets.
 *
 * @author Eugene Kuleshov
 * @author Mik Kersten
 */
public class TaskWorkingSetElementFactory implements org.eclipse.ui.IElementFactory {
    static final java.lang.String HANDLE_TASK = "handle.task";// $NON-NLS-1$


    static final java.lang.String HANDLE_PROJECT = "handle.project";// $NON-NLS-1$


    public org.eclipse.core.runtime.IAdaptable createElement(org.eclipse.ui.IMemento memento) {
        java.lang.String taskHandle = memento.getString(org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetElementFactory.HANDLE_TASK);
        if (taskHandle != null) {
            // TOOD: this does not support projects and categories/queries have the same name
            org.eclipse.mylyn.internal.tasks.core.TaskList taskList = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList();
            for (org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer element : taskList.getRootElements()) {
                if (element.getHandleIdentifier().equals(taskHandle)) {
                    return element;
                }
            }
        }
        java.lang.String projectHandle = memento.getString(org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetElementFactory.HANDLE_PROJECT);
        if (projectHandle != null) {
            try {
                org.eclipse.core.resources.IProject project = org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot().getProject(projectHandle);
                if (project != null) {
                    return project;
                }
            } catch (java.lang.Throwable t) {
                org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Could not not determine project for handle: " + projectHandle, t));// $NON-NLS-1$

            }
        }
        // prior to mylyn 3.0.2 task handles and project handles were identical
        if (taskHandle != null) {
            try {
                org.eclipse.core.resources.IProject project = org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot().getProject(taskHandle);
                if (project != null) {
                    return project;
                }
            } catch (java.lang.Throwable t) {
                // ignore, happens when a restoring a query handle from a URL for instance
                // StatusHandler.log(new Status(IStatus.ERROR, TasksUiPlugin.ID_PLUGIN,
                // "Could not not determine project for handle: " + taskHandle, t)); //$NON-NLS-1$
            }
        }
        return null;
    }
}