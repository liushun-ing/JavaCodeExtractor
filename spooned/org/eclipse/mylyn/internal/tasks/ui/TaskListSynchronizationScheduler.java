/**
 * *****************************************************************************
 * Copyright (c) 2004, 2013 Tasktop Technologies and others.
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
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.mylyn.commons.core.DateUtil;
import org.eclipse.mylyn.monitor.ui.IUserAttentionListener;
import org.eclipse.ui.PlatformUI;
/**
 *
 * @author Steffen Pingel
 */
public class TaskListSynchronizationScheduler implements org.eclipse.mylyn.monitor.ui.IUserAttentionListener {
    private static final boolean TRACE_ENABLED = java.lang.Boolean.valueOf(org.eclipse.core.runtime.Platform.getDebugOption("org.eclipse.mylyn.tasks.ui/debug/synchronization"));// $NON-NLS-1$


    private long interval;

    private long inactiveInterval;

    private final org.eclipse.core.runtime.jobs.Job refreshJob;

    private boolean userActive;

    /**
     * Absolute time in milliseconds when refresh job will next run.
     */
    private long scheduledTime;

    /**
     * Absolute time in milliseconds when refresh job last completed.
     */
    private long lastSyncTime;

    private final org.eclipse.core.runtime.jobs.JobChangeAdapter jobListener;

    public TaskListSynchronizationScheduler(org.eclipse.core.runtime.jobs.Job refreshJob) {
        this.userActive = true;
        this.jobListener = new org.eclipse.core.runtime.jobs.JobChangeAdapter() {
            @java.lang.Override
            public void done(org.eclipse.core.runtime.jobs.IJobChangeEvent event) {
                jobDone();
            }
        };
        this.refreshJob = refreshJob;
        // do not show in progress view by default
        this.refreshJob.setSystem(true);
        this.refreshJob.setUser(false);
    }

    private synchronized void reschedule() {
        long delay = this.interval;
        if ((delay != 0) && org.eclipse.ui.PlatformUI.isWorkbenchRunning()) {
            if (!userActive) {
                // triple scheduling interval each time
                this.inactiveInterval *= 3;
                delay = this.inactiveInterval;
                if (org.eclipse.mylyn.internal.tasks.ui.TaskListSynchronizationScheduler.TRACE_ENABLED) {
                    trace("Set inactive interval to " + org.eclipse.mylyn.commons.core.DateUtil.getFormattedDurationShort(this.inactiveInterval));// $NON-NLS-1$

                }
            }
            if (this.scheduledTime != 0) {
                if (this.scheduledTime < (java.lang.System.currentTimeMillis() + delay)) {
                    // already scheduled, nothing to do
                    if (org.eclipse.mylyn.internal.tasks.ui.TaskListSynchronizationScheduler.TRACE_ENABLED) {
                        trace("Synchronization already scheduled in "// $NON-NLS-1$
                         + org.eclipse.mylyn.commons.core.DateUtil.getFormattedDurationShort(this.scheduledTime - java.lang.System.currentTimeMillis()));
                    }
                    return;
                } else {
                    // reschedule for an earlier time
                    cancel();
                }
            }
            schedule(delay);
        }
    }

    private synchronized void cancel() {
        // prevent listener from rescheduling due to cancel
        if (org.eclipse.mylyn.internal.tasks.ui.TaskListSynchronizationScheduler.TRACE_ENABLED) {
            trace("Canceling synchronization scheduled to run in "// $NON-NLS-1$
             + org.eclipse.mylyn.commons.core.DateUtil.getFormattedDurationShort(this.scheduledTime - java.lang.System.currentTimeMillis()));
        }
        refreshJob.removeJobChangeListener(jobListener);
        refreshJob.cancel();
        refreshJob.addJobChangeListener(jobListener);
    }

    private void schedule(long interval) {
        if (org.eclipse.mylyn.internal.tasks.ui.TaskListSynchronizationScheduler.TRACE_ENABLED) {
            trace("Scheduling synchronization in " + org.eclipse.mylyn.commons.core.DateUtil.getFormattedDurationShort(interval));// $NON-NLS-1$

        }
        this.scheduledTime = java.lang.System.currentTimeMillis() + interval;
        refreshJob.schedule(interval);
    }

    public synchronized void setInterval(long interval) {
        setInterval(interval, interval);
    }

    public synchronized void setInterval(long delay, long interval) {
        if (this.interval != interval) {
            this.interval = interval;
            this.inactiveInterval = interval;
            this.scheduledTime = 0;
            cancel();
            if (interval > 0) {
                schedule(delay);
            }
        }
    }

    public void userAttentionGained() {
        synchronized(this) {
            if (!userActive) {
                if (org.eclipse.mylyn.internal.tasks.ui.TaskListSynchronizationScheduler.TRACE_ENABLED) {
                    trace("User activity detected");// $NON-NLS-1$

                }
                this.userActive = true;
                // reset inactive interval each time the user becomes active
                this.inactiveInterval = interval;
                if (interval != 0) {
                    if ((java.lang.System.currentTimeMillis() - lastSyncTime) > interval) {
                        // the last sync was long ago, sync right away
                        cancel();
                        schedule(0);
                    } else {
                        reschedule();
                    }
                }
            }
        }
    }

    private void trace(java.lang.String message) {
        java.lang.System.err.println((("[" + new java.util.Date()) + "] ") + message);// $NON-NLS-1$ //$NON-NLS-2$

    }

    public void userAttentionLost() {
        synchronized(this) {
            this.userActive = false;
        }
    }

    synchronized void jobDone() {
        this.scheduledTime = 0;
        this.lastSyncTime = java.lang.System.currentTimeMillis();
        reschedule();
    }

    public void dispose() {
        refreshJob.removeJobChangeListener(jobListener);
        refreshJob.cancel();
    }
}