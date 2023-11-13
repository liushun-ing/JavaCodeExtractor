/**
 * *****************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.mylyn.commons.net.WebLocation;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.eclipse.mylyn.internal.tasks.ui.Messages;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.ui.PlatformUI;
/**
 * Retrieves a title for a web page.
 *
 * @author Wesley Coelho
 * @author Mik Kersten
 * @author Steffen Pingel
 */
public abstract class AbstractRetrieveTitleFromUrlJob extends org.eclipse.core.runtime.jobs.Job {
    private volatile java.lang.String pageTitle;

    private final java.lang.String url;

    public AbstractRetrieveTitleFromUrlJob(java.lang.String url) {
        super(org.eclipse.mylyn.internal.tasks.ui.Messages.AbstractRetrieveTitleFromUrlJob_Retrieving_summary_from_URL);
        this.url = url;
    }

    public java.lang.String getPageTitle() {
        return pageTitle;
    }

    public java.lang.String getUrl() {
        return url;
    }

    @java.lang.Override
    public org.eclipse.core.runtime.IStatus run(org.eclipse.core.runtime.IProgressMonitor monitor) {
        try {
            pageTitle = org.eclipse.mylyn.commons.net.WebUtil.getTitleFromUrl(new org.eclipse.mylyn.commons.net.WebLocation(getUrl()), monitor);
            if (pageTitle != null) {
                org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                    public void run() {
                        titleRetrieved(pageTitle);
                    }
                });
            }
        } catch (java.io.IOException e) {
            return new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Retrieving summary from URL failed", e);// $NON-NLS-1$

        }
        return org.eclipse.core.runtime.Status.OK_STATUS;
    }

    protected void titleRetrieved(java.lang.String pageTitle) {
    }
}