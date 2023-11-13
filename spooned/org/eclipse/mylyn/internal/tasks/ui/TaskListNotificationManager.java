/**
 * *****************************************************************************
 * Copyright (c) 2004, 2011 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.commons.notifications.ui.AbstractUiNotification;
import org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotificationPopup;
import org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotifier;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
/**
 *
 * @author Rob Elves
 */
public class TaskListNotificationManager implements org.eclipse.jface.util.IPropertyChangeListener {
    private static final long DELAY_OPEN = 5 * 1000;

    private static final boolean runSystem = true;

    private org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotificationPopup popup;

    private final java.util.Set<org.eclipse.mylyn.commons.notifications.ui.AbstractUiNotification> notifications = new java.util.HashSet<org.eclipse.mylyn.commons.notifications.ui.AbstractUiNotification>();

    private final java.util.Set<org.eclipse.mylyn.commons.notifications.ui.AbstractUiNotification> currentlyNotifying = java.util.Collections.synchronizedSet(notifications);

    private final java.util.List<org.eclipse.mylyn.internal.tasks.ui.ITaskListNotificationProvider> notificationProviders = new java.util.ArrayList<org.eclipse.mylyn.internal.tasks.ui.ITaskListNotificationProvider>();

    private final java.util.WeakHashMap<java.lang.Object, java.lang.Object> cancelledTokens = new java.util.WeakHashMap<java.lang.Object, java.lang.Object>();

    private final org.eclipse.core.runtime.jobs.Job openJob = new org.eclipse.core.runtime.jobs.Job(Messages.TaskListNotificationManager_Open_Notification_Job) {
        @java.lang.Override
        protected org.eclipse.core.runtime.IStatus run(org.eclipse.core.runtime.IProgressMonitor monitor) {
            try {
                if (((org.eclipse.core.runtime.Platform.isRunning() && (org.eclipse.ui.PlatformUI.getWorkbench() != null)) && (org.eclipse.ui.PlatformUI.getWorkbench().getDisplay() != null)) && (!org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().isDisposed())) {
                    org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                        public void run() {
                            collectNotifications();
                            if ((popup != null) && (popup.getReturnCode() == org.eclipse.jface.window.Window.CANCEL)) {
                                java.util.List<org.eclipse.mylyn.commons.notifications.ui.AbstractUiNotification> notifications = popup.getNotifications();
                                for (org.eclipse.mylyn.commons.notifications.ui.AbstractUiNotification notification : notifications) {
                                    if (notification.getToken() != null) {
                                        cancelledTokens.put(notification.getToken(), null);
                                    }
                                }
                            }
                            for (java.util.Iterator<org.eclipse.mylyn.commons.notifications.ui.AbstractUiNotification> it = currentlyNotifying.iterator(); it.hasNext();) {
                                org.eclipse.mylyn.commons.notifications.ui.AbstractUiNotification notification = it.next();
                                if ((notification.getToken() != null) && cancelledTokens.containsKey(notification.getToken())) {
                                    it.remove();
                                }
                            }
                            synchronized(org.eclipse.mylyn.internal.tasks.ui.TaskListNotificationManager.class) {
                                if (currentlyNotifying.size() > 0) {
                                    // popup.close();
                                    showPopup();
                                }
                            }
                        }
                    });
                }
            } finally {
                if (popup != null) {
                    schedule(popup.getDelayClose() / 2);
                } else {
                    schedule(org.eclipse.mylyn.internal.tasks.ui.TaskListNotificationManager.DELAY_OPEN);
                }
            }
            if (monitor.isCanceled()) {
                return org.eclipse.core.runtime.Status.CANCEL_STATUS;
            }
            return org.eclipse.core.runtime.Status.OK_STATUS;
        }
    };

    public TaskListNotificationManager() {
    }

    public void showPopup() {
        if (popup != null) {
            popup.close();
        }
        org.eclipse.swt.widgets.Shell shell = new org.eclipse.swt.widgets.Shell(org.eclipse.ui.PlatformUI.getWorkbench().getDisplay());
        popup = new org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotificationPopup(shell);
        popup.setFadingEnabled(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.isAnimationsEnabled());
        java.util.List<org.eclipse.mylyn.commons.notifications.ui.AbstractUiNotification> toDisplay = new java.util.ArrayList<org.eclipse.mylyn.commons.notifications.ui.AbstractUiNotification>(currentlyNotifying);
        java.util.Collections.sort(toDisplay);
        popup.setContents(toDisplay);
        cleanNotified();
        popup.setBlockOnOpen(false);
        popup.open();
    }

    private void cleanNotified() {
        currentlyNotifying.clear();
    }

    /**
     * public for testing
     */
    public void collectNotifications() {
        for (org.eclipse.mylyn.internal.tasks.ui.ITaskListNotificationProvider provider : notificationProviders) {
            currentlyNotifying.addAll(provider.getNotifications());
        }
    }

    public void startNotification(long initialStartupTime) {
        if (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().getBoolean(ITasksUiPreferenceConstants.NOTIFICATIONS_ENABLED)) {
            if (!openJob.cancel()) {
                try {
                    openJob.join();
                } catch (java.lang.InterruptedException e) {
                    // ignore
                }
            }
            openJob.setSystem(org.eclipse.mylyn.internal.tasks.ui.TaskListNotificationManager.runSystem);
            openJob.schedule(initialStartupTime);
            for (org.eclipse.mylyn.internal.tasks.ui.ITaskListNotificationProvider provider : notificationProviders) {
                if (provider instanceof org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotifier) {
                    ((org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotifier) (provider)).setEnabled(true);
                }
            }
        }
    }

    public void stopNotification() {
        for (org.eclipse.mylyn.internal.tasks.ui.ITaskListNotificationProvider provider : notificationProviders) {
            if (provider instanceof org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotifier) {
                ((org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotifier) (provider)).setEnabled(false);
            }
        }
        openJob.cancel();
        // closeJob.cancel();
        // if (popup != null) {
        // popup.close();
        // }
    }

    public void addNotificationProvider(org.eclipse.mylyn.internal.tasks.ui.ITaskListNotificationProvider notification_provider) {
        notificationProviders.add(notification_provider);
    }

    public void removeNotificationProvider(org.eclipse.mylyn.internal.tasks.ui.ITaskListNotificationProvider notification_provider) {
        notificationProviders.remove(notification_provider);
    }

    /**
     * public for testing purposes
     */
    public java.util.Set<org.eclipse.mylyn.commons.notifications.ui.AbstractUiNotification> getNotifications() {
        synchronized(org.eclipse.mylyn.internal.tasks.ui.TaskListNotificationManager.class) {
            return currentlyNotifying;
        }
    }

    public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
        if (event.getProperty().equals(ITasksUiPreferenceConstants.NOTIFICATIONS_ENABLED)) {
            java.lang.Object newValue = event.getNewValue();
            if (!(newValue instanceof java.lang.Boolean)) {
                // default if no preference value
                startNotification(0);
            } else if (((java.lang.Boolean) (newValue)) == true) {
                startNotification(0);
            } else {
                stopNotification();
            }
        }
    }
}