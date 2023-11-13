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
 *     Xiaoyang Guan - browser preview
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.editors;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.mylyn.commons.workbench.browser.BrowserUtil;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.editors.AbstractRenderingEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
/**
 *
 * @author Steffen Pingel
 * @author Xiaoyang Guan
 */
public class BrowserPreviewViewer {
    private org.eclipse.swt.browser.Browser browser;

    private boolean ignoreLocationEvents;

    private final org.eclipse.mylyn.tasks.ui.editors.AbstractRenderingEngine renderingEngine;

    private final org.eclipse.mylyn.tasks.core.TaskRepository taskRepository;

    public BrowserPreviewViewer(org.eclipse.mylyn.tasks.core.TaskRepository taskRepository, org.eclipse.mylyn.tasks.ui.editors.AbstractRenderingEngine renderingEngine) {
        org.eclipse.core.runtime.Assert.isNotNull(taskRepository);
        org.eclipse.core.runtime.Assert.isNotNull(renderingEngine);
        this.taskRepository = taskRepository;
        this.renderingEngine = renderingEngine;
    }

    public void createControl(org.eclipse.swt.widgets.Composite parent, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        browser = new org.eclipse.swt.browser.Browser(parent, org.eclipse.swt.SWT.NONE);
        // intercept links to open tasks in rich editor and urls in separate browser
        browser.addLocationListener(new org.eclipse.swt.browser.LocationAdapter() {
            @java.lang.Override
            public void changing(org.eclipse.swt.browser.LocationEvent event) {
                // ignore events that are caused by manually setting the contents of the browser
                if (ignoreLocationEvents) {
                    return;
                }
                if ((event.location != null) && (!event.location.startsWith("about"))) {
                    // $NON-NLS-1$
                    event.doit = false;
                    org.eclipse.mylyn.commons.workbench.browser.BrowserUtil.openUrl(event.location);
                }
            }
        });
    }

    public org.eclipse.swt.browser.Browser getControl() {
        return browser;
    }

    private void previewWiki(final org.eclipse.swt.browser.Browser browser, java.lang.String sourceText) {
        final class PreviewWikiJob extends org.eclipse.core.runtime.jobs.Job {
            private java.lang.String htmlText;

            private org.eclipse.core.runtime.IStatus jobStatus;

            private final java.lang.String sourceText;

            public PreviewWikiJob(java.lang.String sourceText) {
                super(Messages.BrowserPreviewViewer_Formatting_Wiki_Text);
                if (sourceText == null) {
                    throw new java.lang.IllegalArgumentException("source text must not be null");// $NON-NLS-1$

                }
                this.sourceText = sourceText;
            }

            public java.lang.String getHtmlText() {
                return htmlText;
            }

            public org.eclipse.core.runtime.IStatus getStatus() {
                return jobStatus;
            }

            @java.lang.Override
            protected org.eclipse.core.runtime.IStatus run(org.eclipse.core.runtime.IProgressMonitor monitor) {
                if (renderingEngine == null) {
                    jobStatus = new org.eclipse.mylyn.tasks.core.RepositoryStatus(taskRepository, org.eclipse.core.runtime.IStatus.INFO, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.mylyn.tasks.core.RepositoryStatus.ERROR_INTERNAL, Messages.BrowserPreviewViewer_The_repository_does_not_support_HTML_preview);
                    return org.eclipse.core.runtime.Status.OK_STATUS;
                }
                jobStatus = org.eclipse.core.runtime.Status.OK_STATUS;
                try {
                    htmlText = renderingEngine.renderAsHtml(taskRepository, sourceText, monitor);
                } catch (org.eclipse.core.runtime.CoreException e) {
                    jobStatus = e.getStatus();
                }
                return org.eclipse.core.runtime.Status.OK_STATUS;
            }
        }
        final PreviewWikiJob job = new PreviewWikiJob(sourceText);
        job.addJobChangeListener(new org.eclipse.core.runtime.jobs.JobChangeAdapter() {
            @java.lang.Override
            public void done(final org.eclipse.core.runtime.jobs.IJobChangeEvent event) {
                if (!browser.isDisposed()) {
                    if (job.getStatus().isOK()) {
                        browser.getDisplay().asyncExec(new java.lang.Runnable() {
                            public void run() {
                                setText(browser, job.getHtmlText());
                                // TODO 3.5 error handling
                                // getAttributeEditorManager().setMessage(null, IMessageProvider.NONE);
                            }
                        });
                    } else {
                        browser.getDisplay().asyncExec(new java.lang.Runnable() {
                            public void run() {
                                org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayStatus(Messages.BrowserPreviewViewer_Error, job.getStatus());
                                // TODO 3.5 error handling
                                // getAttributeEditorManager().setMessage(job.getStatus().getMessage(), IMessageProvider.ERROR);
                            }
                        });
                    }
                }
                super.done(event);
            }
        });
        job.setUser(true);
        job.schedule();
    }

    private void setText(org.eclipse.swt.browser.Browser browser, java.lang.String html) {
        try {
            ignoreLocationEvents = true;
            browser.setText(html != null ? html : "");// $NON-NLS-1$

        } finally {
            ignoreLocationEvents = false;
        }
    }

    public void update(java.lang.String value) {
        setText(browser, Messages.BrowserPreviewViewer_Loading_preview_);
        previewWiki(browser, value);
    }
}