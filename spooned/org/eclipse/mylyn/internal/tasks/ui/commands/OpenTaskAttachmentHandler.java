/**
 * *****************************************************************************
 * Copyright (c) 2010 Peter Stibrany and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Peter Stibrany - initial API and implementation
 *     Tasktop Technologies - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.commands;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.internal.tasks.ui.ITaskAttachmentViewer;
import org.eclipse.mylyn.internal.tasks.ui.TaskAttachmentViewerManager;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.ITaskAttachment;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
/**
 *
 * @author Peter Stibrany
 */
public class OpenTaskAttachmentHandler extends org.eclipse.core.commands.AbstractHandler implements org.eclipse.core.commands.IHandler {
    public java.lang.Object execute(org.eclipse.core.commands.ExecutionEvent event) throws org.eclipse.core.commands.ExecutionException {
        org.eclipse.ui.IWorkbenchWindow window = org.eclipse.ui.handlers.HandlerUtil.getActiveWorkbenchWindow(event);
        if (window != null) {
            org.eclipse.ui.IWorkbenchPage page = window.getActivePage();
            if (page != null) {
                java.util.List<org.eclipse.mylyn.tasks.core.ITaskAttachment> attachments = org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.getSelectedAttachments(event);
                try {
                    org.eclipse.mylyn.internal.tasks.ui.commands.OpenTaskAttachmentHandler.openAttachments(page, attachments);
                } catch (org.eclipse.core.runtime.OperationCanceledException e) {
                    // canceled
                }
            }
        }
        return null;
    }

    public static void openAttachments(org.eclipse.ui.IWorkbenchPage page, java.util.List<org.eclipse.mylyn.tasks.core.ITaskAttachment> attachments) {
        org.eclipse.mylyn.internal.tasks.ui.TaskAttachmentViewerManager manager = new org.eclipse.mylyn.internal.tasks.ui.TaskAttachmentViewerManager();
        for (org.eclipse.mylyn.tasks.core.ITaskAttachment attachment : attachments) {
            org.eclipse.mylyn.internal.tasks.ui.ITaskAttachmentViewer viewer = manager.getPreferredViewer(attachment);
            if (viewer == null) {
                org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.logAndDisplayStatus(Messages.OpenTaskAttachmentHandler_failedToOpenViewer, new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.WARNING, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, Messages.OpenTaskAttachmentHandler_noAttachmentViewerFound));
                continue;
            }
            try {
                viewer.openAttachment(page, attachment);
            } catch (org.eclipse.core.runtime.CoreException e) {
                org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.logAndDisplayStatus(Messages.OpenTaskAttachmentHandler_failedToOpenViewer, e.getStatus());
            }
        }
    }
}