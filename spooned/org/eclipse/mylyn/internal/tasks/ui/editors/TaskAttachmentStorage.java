/**
 * *****************************************************************************
 * Copyright (c) 2004, 2010 Jeff Pound and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jeff Pound - initial API and implementation
 *     Tasktop Technologies - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.editors;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskAttachment;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.TasksUi;
/**
 *
 * @author Jeff Pound
 * @author Steffen Pingel
 * @deprecated use TaskAttachmentViewersManager instead
 */
@java.lang.Deprecated
public class TaskAttachmentStorage extends org.eclipse.core.runtime.PlatformObject implements org.eclipse.core.resources.IStorage {
    private static final java.lang.String ATTACHMENT_DEFAULT_NAME = "attachment";// $NON-NLS-1$


    private static final java.lang.String CTYPE_ZIP = "zip";// $NON-NLS-1$


    private static final java.lang.String CTYPE_OCTET_STREAM = "octet-stream";// $NON-NLS-1$


    private static final java.lang.String CTYPE_TEXT = "text";// $NON-NLS-1$


    private static final java.lang.String CTYPE_HTML = "html";// $NON-NLS-1$


    private final org.eclipse.mylyn.tasks.core.TaskRepository taskRepository;

    private final org.eclipse.mylyn.tasks.core.ITask task;

    private final org.eclipse.mylyn.tasks.core.data.TaskAttribute attachmentAttribute;

    private final java.lang.String name;

    public TaskAttachmentStorage(org.eclipse.mylyn.tasks.core.TaskRepository taskRepository, org.eclipse.mylyn.tasks.core.ITask task, org.eclipse.mylyn.tasks.core.data.TaskAttribute attachmentAttribute, java.lang.String name) {
        this.taskRepository = taskRepository;
        this.task = task;
        this.attachmentAttribute = attachmentAttribute;
        this.name = name;
    }

    public static org.eclipse.core.resources.IStorage create(org.eclipse.mylyn.tasks.core.ITaskAttachment attachment) throws org.eclipse.core.runtime.CoreException {
        org.eclipse.core.runtime.Assert.isNotNull(attachment);
        org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute = attachment.getTaskAttribute();
        if (taskAttribute == null) {
            throw new org.eclipse.core.runtime.CoreException(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Failed to find attachment: "// $NON-NLS-1$
             + attachment.getUrl()));
        }
        return new org.eclipse.mylyn.internal.tasks.ui.editors.TaskAttachmentStorage(attachment.getTaskRepository(), attachment.getTask(), taskAttribute, org.eclipse.mylyn.internal.tasks.ui.editors.TaskAttachmentStorage.getName(attachment));
    }

    private static java.lang.String getName(org.eclipse.mylyn.tasks.core.ITaskAttachment attachment) {
        java.lang.String name = attachment.getFileName();
        // if no filename is set, make one up with the proper extension so
        // we can support opening in that filetype's default editor
        if ((name == null) || "".equals(name)) {
            // $NON-NLS-1$
            java.lang.String ctype = attachment.getContentType();
            if (ctype.endsWith(org.eclipse.mylyn.internal.tasks.ui.editors.TaskAttachmentStorage.CTYPE_HTML)) {
                name = org.eclipse.mylyn.internal.tasks.ui.editors.TaskAttachmentStorage.ATTACHMENT_DEFAULT_NAME + ".html";// $NON-NLS-1$

            } else if (ctype.startsWith(org.eclipse.mylyn.internal.tasks.ui.editors.TaskAttachmentStorage.CTYPE_TEXT)) {
                name = org.eclipse.mylyn.internal.tasks.ui.editors.TaskAttachmentStorage.ATTACHMENT_DEFAULT_NAME + ".txt";// $NON-NLS-1$

            } else if (ctype.endsWith(org.eclipse.mylyn.internal.tasks.ui.editors.TaskAttachmentStorage.CTYPE_OCTET_STREAM)) {
                name = org.eclipse.mylyn.internal.tasks.ui.editors.TaskAttachmentStorage.ATTACHMENT_DEFAULT_NAME;
            } else if (ctype.endsWith(org.eclipse.mylyn.internal.tasks.ui.editors.TaskAttachmentStorage.CTYPE_ZIP)) {
                name = (org.eclipse.mylyn.internal.tasks.ui.editors.TaskAttachmentStorage.ATTACHMENT_DEFAULT_NAME + ".") + org.eclipse.mylyn.internal.tasks.ui.editors.TaskAttachmentStorage.CTYPE_ZIP;// $NON-NLS-1$

            } else {
                name = (org.eclipse.mylyn.internal.tasks.ui.editors.TaskAttachmentStorage.ATTACHMENT_DEFAULT_NAME + ".") + ctype.substring(ctype.indexOf("/") + 1);// $NON-NLS-1$ //$NON-NLS-2$

            }
        }
        // treat .patch files as text files
        if (name.endsWith(".patch")) {
            // $NON-NLS-1$
            name += ".txt";// $NON-NLS-1$

        }
        return name;
    }

    public java.io.InputStream getContents() throws org.eclipse.core.runtime.CoreException {
        org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(taskRepository.getConnectorKind());
        org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler handler = connector.getTaskAttachmentHandler();
        return handler.getContent(taskRepository, task, attachmentAttribute, new org.eclipse.core.runtime.NullProgressMonitor());
    }

    public org.eclipse.core.runtime.IPath getFullPath() {
        // ignore
        return null;
    }

    public java.lang.String getName() {
        return name;
    }

    public boolean isReadOnly() {
        return true;
    }
}