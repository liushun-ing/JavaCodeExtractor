/**
 * *****************************************************************************
 * Copyright (c) 2015 Tasktop Technologies.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.migrator;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
public class JobListener {
    private final java.util.Set<org.eclipse.core.runtime.jobs.Job> jobs = java.util.Collections.synchronizedSet(new java.util.HashSet<org.eclipse.core.runtime.jobs.Job>());

    private final java.lang.Runnable allJobsDone;

    private boolean started;

    private boolean complete;

    public JobListener(java.lang.Runnable allJobsDone) {
        this.allJobsDone = allJobsDone;
    }

    /**
     * Must be called once when all jobs have been added.
     */
    public void start() {
        synchronized(jobs) {
            started = true;
            if (jobs.isEmpty()) {
                allJobsDone.run();
                complete = true;
            }
        }
    }

    public boolean isComplete() {
        return complete;
    }

    /**
     * This method should only be called from a single thread.
     */
    public void add(final org.eclipse.core.runtime.jobs.Job job, final java.lang.Runnable jobDone) {
        jobs.add(job);
        job.addJobChangeListener(new org.eclipse.core.runtime.jobs.JobChangeAdapter() {
            @java.lang.Override
            public void done(org.eclipse.core.runtime.jobs.IJobChangeEvent event) {
                jobDone.run();
                synchronized(jobs) {
                    jobs.remove(job);
                    if (jobs.isEmpty() && started) {
                        allJobsDone.run();
                        complete = true;
                    }
                }
            }
        });
    }
}