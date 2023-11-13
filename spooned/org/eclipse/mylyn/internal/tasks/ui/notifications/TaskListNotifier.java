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
import com.google.common.base.Strings;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.notifications.ui.AbstractUiNotification;
import org.eclipse.mylyn.internal.tasks.core.data.ITaskDataManagerListener;
import org.eclipse.mylyn.internal.tasks.core.data.SynchronizationManger;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataDiff;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataManager;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataManagerEvent;
import org.eclipse.mylyn.internal.tasks.core.sync.SynchronizeTasksJob;
import org.eclipse.mylyn.internal.tasks.ui.ITaskListNotificationProvider;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.views.PresentationFilter;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITask.SynchronizationState;
import org.eclipse.mylyn.tasks.core.data.ITaskDataWorkingCopy;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.TasksUi;
/**
 *
 * @author Steffen Pingel
 */
public class TaskListNotifier implements org.eclipse.mylyn.internal.tasks.core.data.ITaskDataManagerListener , org.eclipse.mylyn.internal.tasks.ui.ITaskListNotificationProvider {
    public static final java.lang.String KEY_INCOMING_NOTIFICATION_TEXT = "org.eclipse.mylyn.tasks.ui.TaskNotificationText";// $NON-NLS-1$


    private final org.eclipse.mylyn.internal.tasks.core.data.TaskDataManager taskDataManager;

    private final java.util.List<org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotification> notificationQueue = new java.util.ArrayList<org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotification>();

    public boolean enabled;

    private final org.eclipse.mylyn.internal.tasks.core.data.SynchronizationManger synchronizationManger;

    public TaskListNotifier(org.eclipse.mylyn.internal.tasks.core.data.TaskDataManager taskDataManager, org.eclipse.mylyn.internal.tasks.core.data.SynchronizationManger synchronizationManger) {
        this.taskDataManager = taskDataManager;
        this.synchronizationManger = synchronizationManger;
    }

    public org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotification getNotification(org.eclipse.mylyn.tasks.core.ITask task, java.lang.Object token) {
        if (task.getSynchronizationState() == org.eclipse.mylyn.tasks.core.ITask.SynchronizationState.INCOMING_NEW) {
            org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotification notification = new org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotification(task, token);
            notification.setDescription(Messages.TaskListNotifier_New_unread_task);
            return notification;
        } else if (task.getSynchronizationState().isIncoming()) {
            java.lang.String notificationText = task.getAttribute(org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotifier.KEY_INCOMING_NOTIFICATION_TEXT);
            if (!com.google.common.base.Strings.isNullOrEmpty(notificationText)) {
                org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotification notification = new org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotification(task, token);
                notification.setDescription(notificationText);
                return notification;
            }
        }
        return null;
    }

    public org.eclipse.mylyn.internal.tasks.core.data.TaskDataDiff getDiff(org.eclipse.mylyn.tasks.core.ITask task) {
        org.eclipse.mylyn.tasks.core.data.ITaskDataWorkingCopy workingCopy;
        try {
            workingCopy = taskDataManager.getTaskDataState(task);
            if (workingCopy != null) {
                return synchronizationManger.createDiff(workingCopy.getRepositoryData(), workingCopy.getLastReadData(), new org.eclipse.core.runtime.NullProgressMonitor());
            }
        } catch (org.eclipse.core.runtime.CoreException e) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, ("Failed to get task data for task: \""// $NON-NLS-1$
             + task) + "\"", e));// $NON-NLS-1$

        }
        return null;
    }

    @java.lang.Override
    public void taskDataUpdated(org.eclipse.mylyn.internal.tasks.core.data.TaskDataManagerEvent event) {
        // Events from SynchronizeQueryJobs end up with incorrect diffs
        // Only notify for the subsequent SynchronizeTasksJobs
        if (event.getTaskChanged() && (event.getData() instanceof org.eclipse.mylyn.internal.tasks.core.sync.SynchronizeTasksJob)) {
            // Always record the notification text, it may be read outside of the notification
            // The text does not need a token to be recorded, and manually synchronized tasks will not have an associated token
            recordNotificationText(event);
            if (shouldDisplayNotification(event)) {
                queueNotification(event);
            }
        }
    }

    private void recordNotificationText(org.eclipse.mylyn.internal.tasks.core.data.TaskDataManagerEvent event) {
        org.eclipse.mylyn.tasks.core.ITask task = event.getTask();
        org.eclipse.mylyn.tasks.core.ITask.SynchronizationState state = task.getSynchronizationState();
        java.lang.String notificationText = null;
        if (state.isIncoming()) {
            notificationText = computeNotificationText(task);
        }
        task.setAttribute(org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotifier.KEY_INCOMING_NOTIFICATION_TEXT, notificationText);
    }

    public java.lang.String computeNotificationText(org.eclipse.mylyn.tasks.core.ITask task) {
        org.eclipse.mylyn.internal.tasks.core.data.TaskDataDiff diff = getDiff(task);
        if ((diff != null) && diff.hasChanged()) {
            return org.eclipse.mylyn.internal.tasks.ui.notifications.TaskDiffUtil.toString(diff, true);
        }
        return null;
    }

    private boolean shouldDisplayNotification(org.eclipse.mylyn.internal.tasks.core.data.TaskDataManagerEvent event) {
        return ((event.getToken() != null) && isEnabled()) && org.eclipse.mylyn.internal.tasks.ui.views.PresentationFilter.getInstance().isInVisibleQuery(event.getTask());
    }

    private void queueNotification(org.eclipse.mylyn.internal.tasks.core.data.TaskDataManagerEvent event) {
        org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryConnectorUi(event.getTaskData().getConnectorKind());
        if (!connectorUi.hasCustomNotifications()) {
            org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotification notification = getNotification(event.getTask(), event.getToken());
            if (notification != null) {
                synchronized(notificationQueue) {
                    if (enabled) {
                        notificationQueue.add(notification);
                    }
                }
            }
        }
    }

    public java.util.Set<org.eclipse.mylyn.commons.notifications.ui.AbstractUiNotification> getNotifications() {
        synchronized(notificationQueue) {
            if (notificationQueue.isEmpty()) {
                return java.util.Collections.emptySet();
            }
            java.util.HashSet<org.eclipse.mylyn.commons.notifications.ui.AbstractUiNotification> result = new java.util.HashSet<org.eclipse.mylyn.commons.notifications.ui.AbstractUiNotification>(notificationQueue);
            notificationQueue.clear();
            return result;
        }
    }

    @java.lang.Override
    public void editsDiscarded(org.eclipse.mylyn.internal.tasks.core.data.TaskDataManagerEvent event) {
        // ignore
    }

    public void setEnabled(boolean enabled) {
        synchronized(notificationQueue) {
            if (!enabled) {
                notificationQueue.clear();
            }
            this.enabled = enabled;
        }
    }

    public boolean isEnabled() {
        synchronized(notificationQueue) {
            return enabled;
        }
    }
}