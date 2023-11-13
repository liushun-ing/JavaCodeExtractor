/**
 * *****************************************************************************
 * Copyright (c) 2004, 2010 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Eric Booth - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.tasks.ui.editors;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorInputFactory;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
/**
 * Input for task editors.
 *
 * @author Eric Booth
 * @author Rob Elves
 * @author Mik Kersten
 * @author Steffen Pingel
 * @since 2.0
 */
public class TaskEditorInput extends org.eclipse.core.runtime.PlatformObject implements org.eclipse.ui.IEditorInput , org.eclipse.ui.IPersistableElement {
    private static final int MAX_LABEL_LENGTH = 60;

    private final org.eclipse.mylyn.tasks.core.ITask task;

    private final org.eclipse.mylyn.tasks.core.TaskRepository taskRepository;

    private java.lang.Object data;

    /**
     *
     * @since 3.0
     */
    @java.lang.Deprecated
    public TaskEditorInput(org.eclipse.mylyn.tasks.core.ITask task, boolean newTask) {
        this(org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(task.getConnectorKind(), task.getRepositoryUrl()), task);
    }

    /**
     *
     * @since 3.0
     */
    public TaskEditorInput(org.eclipse.mylyn.tasks.core.TaskRepository taskRepository, org.eclipse.mylyn.tasks.core.ITask task) {
        org.eclipse.core.runtime.Assert.isNotNull(taskRepository);
        org.eclipse.core.runtime.Assert.isNotNull(task);
        this.taskRepository = taskRepository;
        this.task = task;
    }

    /**
     *
     * @since 2.0
     */
    @java.lang.Override
    public boolean equals(java.lang.Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput other = ((org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput) (obj));
        return task.equals(other.task);
    }

    /**
     *
     * @since 2.0
     */
    public boolean exists() {
        return task != null;
    }

    /**
     *
     * @since 2.0
     */
    @java.lang.Override
    @java.lang.SuppressWarnings("rawtypes")
    public java.lang.Object getAdapter(java.lang.Class adapter) {
        if (adapter == org.eclipse.ui.IEditorInput.class) {
            return this;
        }
        return super.getAdapter(adapter);
    }

    /**
     *
     * @since 2.0
     */
    public java.lang.String getFactoryId() {
        return org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorInputFactory.ID_FACTORY;
    }

    /**
     *
     * @since 2.0
     */
    public org.eclipse.jface.resource.ImageDescriptor getImageDescriptor() {
        return null;
    }

    /**
     *
     * @deprecated use {@link #getName()}
     * @since 2.0
     */
    @java.lang.Deprecated
    public java.lang.String getLabel() {
        return getName();
    }

    /**
     *
     * @since 2.0
     */
    public java.lang.String getName() {
        java.lang.String toolTipText = getToolTipText();
        if (toolTipText == null) {
            return null;
        }
        if (task != null) {
            java.lang.String taskKey = task.getTaskKey();
            if (taskKey != null) {
                return truncate((taskKey + ": ") + toolTipText);// $NON-NLS-1$

            }
        }
        return truncate(toolTipText);
    }

    /**
     *
     * @since 2.0
     */
    public org.eclipse.ui.IPersistableElement getPersistable() {
        if ((task != null) && (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().getTask(task.getHandleIdentifier()) != null)) {
            return this;
        }
        return null;
    }

    /**
     * Returns the task if the task is in the task list; returns <code>null</code> otherwise.
     *
     * @since 3.0
     */
    public org.eclipse.mylyn.tasks.core.ITask getTask() {
        return task;
    }

    /**
     *
     * @since 3.0
     */
    public org.eclipse.mylyn.tasks.core.TaskRepository getTaskRepository() {
        return taskRepository;
    }

    /**
     *
     * @since 2.0
     */
    public java.lang.String getToolTipText() {
        return task.getSummary();
    }

    /**
     *
     * @since 2.0
     */
    @java.lang.Override
    public int hashCode() {
        return task.getTaskId().hashCode();
    }

    /**
     *
     * @since 2.0
     */
    @java.lang.Deprecated
    public boolean isNewTask() {
        return false;
    }

    /**
     *
     * @since 2.0
     */
    public void saveState(org.eclipse.ui.IMemento memento) {
        org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorInputFactory.saveState(memento, this);
    }

    private java.lang.String truncate(java.lang.String description) {
        if ((description == null) || (description.length() <= org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput.MAX_LABEL_LENGTH)) {
            return description;
        } else {
            return description.substring(0, org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput.MAX_LABEL_LENGTH) + "...";// $NON-NLS-1$

        }
    }

    /**
     *
     * @since 3.0
     */
    public java.lang.Object getData() {
        return data;
    }

    /**
     *
     * @since 3.0
     */
    public void setData(java.lang.Object data) {
        this.data = data;
    }
}