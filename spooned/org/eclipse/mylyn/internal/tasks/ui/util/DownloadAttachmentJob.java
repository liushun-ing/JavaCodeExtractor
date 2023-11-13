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
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.util;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.ITaskAttachment;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
/**
 *
 * @author Steffen Pingel
 */
public class DownloadAttachmentJob extends org.eclipse.core.runtime.jobs.Job {
    private final org.eclipse.mylyn.tasks.core.ITaskAttachment attachment;

    private final java.io.File targetFile;

    public DownloadAttachmentJob(org.eclipse.mylyn.tasks.core.ITaskAttachment attachment, java.io.File targetFile) {
        super(Messages.DownloadAttachmentJob_Downloading_Attachment);
        this.attachment = attachment;
        this.targetFile = targetFile;
    }

    @java.lang.Override
    protected org.eclipse.core.runtime.IStatus run(org.eclipse.core.runtime.IProgressMonitor monitor) {
        try {
            try {
                boolean exceptionThrown = true;
                java.io.OutputStream out = new java.io.BufferedOutputStream(new java.io.FileOutputStream(targetFile));
                try {
                    org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.downloadAttachment(attachment, out, monitor);
                    exceptionThrown = false;
                } finally {
                    out.close();
                    if (exceptionThrown) {
                        targetFile.delete();
                    }
                }
            } catch (java.io.IOException e) {
                throw new org.eclipse.core.runtime.CoreException(new org.eclipse.mylyn.tasks.core.RepositoryStatus(attachment.getTaskRepository(), org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.mylyn.tasks.core.RepositoryStatus.ERROR_IO, "IO error writing attachment: "// $NON-NLS-1$
                 + e.getMessage(), e));
            }
        } catch (final org.eclipse.core.runtime.CoreException e) {
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.asyncDisplayStatus(Messages.DownloadAttachmentJob_Copy_Attachment_to_Clipboard, e.getStatus());
            return org.eclipse.core.runtime.Status.OK_STATUS;
        }
        return org.eclipse.core.runtime.Status.OK_STATUS;
    }
}