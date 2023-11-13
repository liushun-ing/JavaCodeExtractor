/**
 * *****************************************************************************
 * Copyright (c) 2004, 2012 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.notifications;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.mylyn.commons.notifications.core.AbstractNotification;
import org.eclipse.mylyn.commons.notifications.ui.AbstractUiNotification;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITask.SynchronizationState;
import org.eclipse.mylyn.tasks.ui.TaskElementLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
/**
 *
 * @author Rob Elves
 * @author Mik Kersten
 */
public class TaskListNotification extends org.eclipse.mylyn.commons.notifications.ui.AbstractUiNotification {
    private static final java.lang.String ID_EVENT_TASK_CHANGED = "org.eclipse.mylyn.tasks.ui.events.TaskChanged";// $NON-NLS-1$


    private static org.eclipse.jface.viewers.DecoratingLabelProvider labelProvider;

    protected final org.eclipse.mylyn.tasks.core.ITask task;

    protected java.util.Date date;

    private java.lang.String description;

    private final java.lang.Object token;

    public TaskListNotification(org.eclipse.mylyn.tasks.core.ITask task) {
        this(org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotification.ID_EVENT_TASK_CHANGED, task, null);
    }

    public TaskListNotification(java.lang.String eventId, org.eclipse.mylyn.tasks.core.ITask task) {
        this(eventId, task, null);
    }

    public TaskListNotification(org.eclipse.mylyn.tasks.core.ITask task, java.lang.Object token) {
        this(org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotification.ID_EVENT_TASK_CHANGED, task, token);
    }

    public TaskListNotification(java.lang.String eventId, org.eclipse.mylyn.tasks.core.ITask task, java.lang.Object token) {
        super(eventId);
        org.eclipse.core.runtime.Assert.isNotNull(task);
        this.task = task;
        this.token = token;
    }

    private org.eclipse.jface.viewers.LabelProvider getLabelProvider() {
        // lazily instantiate on UI thread
        if (org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotification.labelProvider == null) {
            org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotification.labelProvider = new org.eclipse.jface.viewers.DecoratingLabelProvider(new org.eclipse.mylyn.tasks.ui.TaskElementLabelProvider(true), org.eclipse.ui.PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator());
        }
        return org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotification.labelProvider;
    }

    @java.lang.Override
    public java.lang.String getDescription() {
        return description;
    }

    @java.lang.Override
    public java.lang.String getLabel() {
        return getLabelProvider().getText(task);
    }

    public void setDescription(java.lang.String description) {
        this.description = description;
    }

    @java.lang.Override
    public void open() {
        org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().syncExec(new java.lang.Runnable() {
            public void run() {
                org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.refreshAndOpenTaskListElement(task);
            }
        });
    }

    @java.lang.Override
    public org.eclipse.swt.graphics.Image getNotificationImage() {
        return getLabelProvider().getImage(task);
    }

    protected org.eclipse.mylyn.tasks.core.ITask getTask() {
        return task;
    }

    @java.lang.Override
    public org.eclipse.swt.graphics.Image getNotificationKindImage() {
        if (task.getSynchronizationState() == org.eclipse.mylyn.tasks.core.ITask.SynchronizationState.INCOMING_NEW) {
            return org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.OVERLAY_SYNC_INCOMMING_NEW);
        } else if (task.getSynchronizationState() == org.eclipse.mylyn.tasks.core.ITask.SynchronizationState.OUTGOING_NEW) {
            return org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.OVERLAY_SYNC_OUTGOING_NEW);
        } else {
            return org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.OVERLAY_SYNC_INCOMMING);
        }
    }

    @java.lang.Override
    public java.util.Date getDate() {
        return date;
    }

    public void setDate(java.util.Date date) {
        this.date = date;
    }

    @java.lang.Override
    public int compareTo(org.eclipse.mylyn.commons.notifications.core.AbstractNotification anotherNotification) throws java.lang.ClassCastException {
        java.util.Date anotherDate = anotherNotification.getDate();
        if ((date != null) && (anotherDate != null)) {
            return date.compareTo(anotherDate) * (-1);
        } else if (date == null) {
            return 1;
        } else {
            return -1;
        }
    }

    @java.lang.Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (date == null ? 0 : date.hashCode());
        result = (prime * result) + (description == null ? 0 : description.hashCode());
        result = (prime * result) + (task == null ? 0 : task.hashCode());
        return result;
    }

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
        org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotification other = ((org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotification) (obj));
        if (date == null) {
            if (other.date != null) {
                return false;
            }
        } else if (!date.equals(other.date)) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (task == null) {
            if (other.task != null) {
                return false;
            }
        } else if (!task.equals(other.task)) {
            return false;
        }
        return true;
    }

    @java.lang.SuppressWarnings("rawtypes")
    public java.lang.Object getAdapter(java.lang.Class adapter) {
        if (adapter == org.eclipse.mylyn.internal.tasks.core.AbstractTask.class) {
            return task;
        }
        return null;
    }

    @java.lang.Override
    public java.lang.Object getToken() {
        return token;
    }
}