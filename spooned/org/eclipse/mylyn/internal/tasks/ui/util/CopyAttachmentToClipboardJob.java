/**
 * *****************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
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
import org.eclipse.mylyn.tasks.core.ITaskAttachment;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.PlatformUI;
/**
 *
 * @author Steffen Pingel
 */
public class CopyAttachmentToClipboardJob extends org.eclipse.core.runtime.jobs.Job {
    private final org.eclipse.mylyn.tasks.core.ITaskAttachment attachment;

    public CopyAttachmentToClipboardJob(org.eclipse.mylyn.tasks.core.ITaskAttachment attachment) {
        super(Messages.CopyAttachmentToClipboardJob_Copying_Attachment_to_Clipboard);
        this.attachment = attachment;
    }

    @java.lang.Override
    protected org.eclipse.core.runtime.IStatus run(org.eclipse.core.runtime.IProgressMonitor monitor) {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        try {
            org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.downloadAttachment(attachment, out, monitor);
        } catch (final org.eclipse.core.runtime.CoreException e) {
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.asyncDisplayStatus(Messages.CopyAttachmentToClipboardJob_Copy_Attachment_to_Clipboard, e.getStatus());
            return org.eclipse.core.runtime.Status.OK_STATUS;
        }
        java.lang.String contents = new java.lang.String(out.toByteArray());
        contents = contents.replaceAll("\r\n|\n", java.lang.System.getProperty("line.separator"));// $NON-NLS-1$ //$NON-NLS-2$

        copyToClipboard(contents);
        return org.eclipse.core.runtime.Status.OK_STATUS;
    }

    private void copyToClipboard(final java.lang.String contents) {
        org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().syncExec(new java.lang.Runnable() {
            public void run() {
                org.eclipse.swt.dnd.Clipboard clipboard = new org.eclipse.swt.dnd.Clipboard(org.eclipse.ui.PlatformUI.getWorkbench().getDisplay());
                clipboard.setContents(new java.lang.Object[]{ contents }, new org.eclipse.swt.dnd.Transfer[]{ org.eclipse.swt.dnd.TextTransfer.getInstance() });
                clipboard.dispose();
            }
        });
    }
}