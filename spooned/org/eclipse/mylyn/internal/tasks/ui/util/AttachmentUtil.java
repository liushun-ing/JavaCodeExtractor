/**
 * *****************************************************************************
 * Copyright (c) 2004, 2014 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Peter Stibrany - improvements for bug 271197
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.util;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.commons.core.CoreUtil;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.internal.tasks.core.TaskAttachment;
import org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource;
import org.eclipse.mylyn.internal.tasks.core.sync.SubmitTaskAttachmentJob;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskAttachment;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.sync.SubmitJob;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.statushandlers.StatusManager;
/**
 *
 * @author Steffen Pingel
 */
public class AttachmentUtil {
    protected static final int BUFFER_SIZE = 1024;

    public static final java.lang.String CONTEXT_DESCRIPTION = "mylyn/context/zip";// $NON-NLS-1$


    private static final java.lang.String CONTEXT_DESCRIPTION_LEGACY = "mylar/context/zip";// $NON-NLS-1$


    private static final java.lang.String CONTEXT_FILENAME = "mylyn-context.zip";// $NON-NLS-1$


    private static final java.lang.String CONTEXT_CONTENT_TYPE = "application/octet-stream";// $NON-NLS-1$


    private static final java.lang.String ATTACHMENT_DEFAULT_NAME = "attachment";// $NON-NLS-1$


    private static final java.lang.String CTYPE_ZIP = "zip";// $NON-NLS-1$


    private static final java.lang.String CTYPE_TEXT = "text";// $NON-NLS-1$


    private static final java.lang.String CTYPE_HTML = "html";// $NON-NLS-1$


    public static boolean postContext(org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector, org.eclipse.mylyn.tasks.core.TaskRepository repository, org.eclipse.mylyn.tasks.core.ITask task, java.lang.String comment, org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute, org.eclipse.core.runtime.IProgressMonitor monitor) throws org.eclipse.core.runtime.CoreException {
        org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler attachmentHandler = connector.getTaskAttachmentHandler();
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getContextStore().saveActiveContext();
        java.io.File file = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getContextStore().getFileForContext(task);
        if (((attachmentHandler != null) && (file != null)) && file.exists()) {
            org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource attachment = new org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource(file);
            attachment.setDescription(org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.CONTEXT_DESCRIPTION);
            attachment.setName(org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.CONTEXT_FILENAME);
            attachmentHandler.postContent(repository, task, attachment, comment, attribute, monitor);
            return true;
        }
        return false;
    }

    public static java.util.List<org.eclipse.mylyn.tasks.core.ITaskAttachment> getContextAttachments(org.eclipse.mylyn.tasks.core.TaskRepository repository, org.eclipse.mylyn.tasks.core.ITask task) {
        java.util.List<org.eclipse.mylyn.tasks.core.ITaskAttachment> contextAttachments = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.ITaskAttachment>();
        org.eclipse.mylyn.tasks.core.data.TaskData taskData;
        try {
            taskData = org.eclipse.mylyn.tasks.ui.TasksUi.getTaskDataManager().getTaskData(task);
        } catch (org.eclipse.core.runtime.CoreException e) {
            // ignore
            return contextAttachments;
        }
        if (taskData != null) {
            java.util.List<org.eclipse.mylyn.tasks.core.data.TaskAttribute> taskAttachments = taskData.getAttributeMapper().getAttributesByType(taskData, org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_ATTACHMENT);
            for (org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute : taskAttachments) {
                org.eclipse.mylyn.internal.tasks.core.TaskAttachment taskAttachment = new org.eclipse.mylyn.internal.tasks.core.TaskAttachment(repository, task, attribute);
                taskData.getAttributeMapper().updateTaskAttachment(taskAttachment, attribute);
                if (org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.isContext(taskAttachment)) {
                    contextAttachments.add(taskAttachment);
                }
            }
        }
        return contextAttachments;
    }

    public static boolean hasContextAttachment(org.eclipse.mylyn.tasks.core.ITask task) {
        org.eclipse.core.runtime.Assert.isNotNull(task);
        org.eclipse.mylyn.tasks.core.TaskRepository repository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(task.getConnectorKind(), task.getRepositoryUrl());
        java.util.List<org.eclipse.mylyn.tasks.core.ITaskAttachment> contextAttachments = org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.getContextAttachments(repository, task);
        return contextAttachments.size() > 0;
    }

    public static boolean downloadContext(final org.eclipse.mylyn.tasks.core.ITask task, final org.eclipse.mylyn.tasks.core.ITaskAttachment attachment, final org.eclipse.jface.operation.IRunnableContext context) {
        if (task.isActive()) {
            org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().deactivateTask(task);
        }
        try {
            context.run(true, true, new org.eclipse.jface.operation.IRunnableWithProgress() {
                public void run(org.eclipse.core.runtime.IProgressMonitor monitor) throws java.lang.reflect.InvocationTargetException, java.lang.InterruptedException {
                    java.io.File targetFile = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getContextStore().getFileForContext(task);
                    try {
                        boolean exceptionThrown = true;
                        java.io.OutputStream out = new java.io.BufferedOutputStream(new java.io.FileOutputStream(targetFile));
                        try {
                            org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.downloadAttachment(attachment, out, monitor);
                            exceptionThrown = false;
                        } catch (org.eclipse.core.runtime.CoreException e) {
                            throw new java.lang.reflect.InvocationTargetException(e);
                        } finally {
                            out.close();
                            if (exceptionThrown) {
                                targetFile.delete();
                            }
                        }
                    } catch (org.eclipse.core.runtime.OperationCanceledException e) {
                        throw new java.lang.InterruptedException();
                    } catch (java.io.IOException e) {
                        throw new java.lang.reflect.InvocationTargetException(new org.eclipse.core.runtime.CoreException(new org.eclipse.mylyn.tasks.core.RepositoryStatus(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.mylyn.tasks.core.RepositoryStatus.ERROR_IO, "Error writing to context file", e)));// $NON-NLS-1$

                    }
                }
            });
        } catch (java.lang.reflect.InvocationTargetException e) {
            if (e.getCause() instanceof org.eclipse.core.runtime.CoreException) {
                org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayStatus(Messages.AttachmentUtil_Mylyn_Information, ((org.eclipse.core.runtime.CoreException) (e.getCause())).getStatus());
            } else {
                org.eclipse.ui.statushandlers.StatusManager.getManager().handle(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Unexpected error while retrieving context", e), org.eclipse.ui.statushandlers.StatusManager.SHOW | org.eclipse.ui.statushandlers.StatusManager.LOG);// $NON-NLS-1$

            }
            return false;
        } catch (java.lang.InterruptedException ignored) {
            // canceled
            return false;
        }
        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().notifyElementChanged(task);
        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.activateTaskThroughCommand(task);
        return true;
    }

    public static boolean uploadContext(final org.eclipse.mylyn.tasks.core.TaskRepository repository, final org.eclipse.mylyn.tasks.core.ITask task, final java.lang.String comment, final org.eclipse.jface.operation.IRunnableContext context) {
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getContextStore().saveActiveContext();
        java.io.File sourceContextFile = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getContextStore().getFileForContext(task);
        if (!sourceContextFile.exists()) {
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayStatus(Messages.AttachmentUtil_Mylyn_Information, new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.WARNING, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, Messages.AttachmentUtil_The_context_is_empty));
            return false;
        }
        org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource source = new org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource(sourceContextFile);
        source.setName(org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.CONTEXT_FILENAME);
        source.setDescription(org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.CONTEXT_DESCRIPTION);
        source.setContentType(org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.CONTEXT_CONTENT_TYPE);
        org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(repository.getConnectorKind());
        org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute = null;
        try {
            org.eclipse.mylyn.tasks.core.data.TaskData taskData = org.eclipse.mylyn.tasks.ui.TasksUi.getTaskDataManager().getTaskData(task);
            if (taskData != null) {
                taskAttribute = taskData.getRoot().createMappedAttribute(org.eclipse.mylyn.tasks.core.data.TaskAttribute.NEW_ATTACHMENT);
            }
        } catch (org.eclipse.core.runtime.CoreException e) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.ID_PLUGIN, "Unexpected error while attaching context. Continuing with task submission.", e));// $NON-NLS-1$

        }
        final org.eclipse.mylyn.tasks.core.sync.SubmitJob submitJob = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getJobFactory().createSubmitTaskAttachmentJob(connector, repository, task, source, comment, taskAttribute);
        try {
            context.run(true, true, new org.eclipse.jface.operation.IRunnableWithProgress() {
                public void run(org.eclipse.core.runtime.IProgressMonitor monitor) throws java.lang.reflect.InvocationTargetException, java.lang.InterruptedException {
                    if (((org.eclipse.mylyn.internal.tasks.core.sync.SubmitTaskAttachmentJob) (submitJob)).run(monitor) == org.eclipse.core.runtime.Status.CANCEL_STATUS) {
                        throw new java.lang.InterruptedException();
                    }
                }
            });
        } catch (java.lang.reflect.InvocationTargetException e) {
            org.eclipse.ui.statushandlers.StatusManager.getManager().handle(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Unexpected error while attaching context", e), org.eclipse.ui.statushandlers.StatusManager.SHOW | org.eclipse.ui.statushandlers.StatusManager.LOG);// $NON-NLS-1$

            return false;
        } catch (java.lang.InterruptedException ignored) {
            // canceled
            return false;
        }
        org.eclipse.core.runtime.IStatus status = submitJob.getStatus();
        if ((status != null) && (status.getSeverity() != org.eclipse.core.runtime.IStatus.CANCEL)) {
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayStatus(Messages.AttachmentUtil_Mylyn_Information, submitJob.getStatus());
            return false;
        }
        return true;
    }

    public static boolean hasLocalContext(org.eclipse.mylyn.tasks.core.ITask task) {
        org.eclipse.core.runtime.Assert.isNotNull(task);
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getContextStore().hasContext(task);
    }

    public static boolean isContext(org.eclipse.mylyn.tasks.core.ITaskAttachment attachment) {
        return org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.CONTEXT_DESCRIPTION.equals(attachment.getDescription()) || org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.CONTEXT_DESCRIPTION_LEGACY.equals(attachment.getDescription());
    }

    public static boolean canUploadAttachment(org.eclipse.mylyn.tasks.core.ITask task) {
        org.eclipse.mylyn.tasks.core.TaskRepository repository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(task.getConnectorKind(), task.getRepositoryUrl());
        if (repository != null) {
            org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(repository.getConnectorKind());
            org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler attachmentHandler = connector.getTaskAttachmentHandler();
            if (attachmentHandler != null) {
                return attachmentHandler.canPostContent(repository, task);
            }
        }
        return false;
    }

    public static boolean canDownloadAttachment(org.eclipse.mylyn.tasks.core.ITask task) {
        org.eclipse.mylyn.tasks.core.TaskRepository repository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(task.getConnectorKind(), task.getRepositoryUrl());
        if (repository != null) {
            org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(repository.getConnectorKind());
            org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler attachmentHandler = connector.getTaskAttachmentHandler();
            if (attachmentHandler != null) {
                return attachmentHandler.canGetContent(repository, task);
            }
        }
        return false;
    }

    public static void downloadAttachment(org.eclipse.mylyn.tasks.core.ITaskAttachment attachment, java.io.OutputStream out, org.eclipse.core.runtime.IProgressMonitor monitor) throws org.eclipse.core.runtime.CoreException {
        try {
            monitor.beginTask(Messages.AttachmentUtil_Downloading_attachment, org.eclipse.core.runtime.IProgressMonitor.UNKNOWN);
            org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(attachment.getConnectorKind());
            org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler handler = connector.getTaskAttachmentHandler();
            if (handler == null) {
                throw new org.eclipse.core.runtime.CoreException(new org.eclipse.mylyn.tasks.core.RepositoryStatus(org.eclipse.core.runtime.IStatus.INFO, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.mylyn.tasks.core.RepositoryStatus.ERROR_INTERNAL, "The repository does not support attachments."));// $NON-NLS-1$

            }
            java.io.InputStream in = handler.getContent(attachment.getTaskRepository(), attachment.getTask(), attachment.getTaskAttribute(), monitor);
            try {
                byte[] buffer = new byte[org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.BUFFER_SIZE];
                while (true) {
                    org.eclipse.mylyn.commons.net.Policy.checkCanceled(monitor);
                    int count = in.read(buffer);
                    if (count == (-1)) {
                        return;
                    }
                    out.write(buffer, 0, count);
                } 
            } catch (java.io.IOException e) {
                throw new org.eclipse.core.runtime.CoreException(new org.eclipse.mylyn.tasks.core.RepositoryStatus(attachment.getTaskRepository(), org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.mylyn.tasks.core.RepositoryStatus.ERROR_IO, "IO error reading attachment: "// $NON-NLS-1$
                 + e.getMessage(), e));
            } finally {
                try {
                    in.close();
                } catch (java.io.IOException e) {
                    org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.ID_PLUGIN, "Error closing attachment stream", e));// $NON-NLS-1$

                }
            }
        } finally {
            monitor.done();
        }
    }

    public static org.eclipse.mylyn.tasks.core.ITaskAttachment getSelectedAttachment() {
        java.util.List<org.eclipse.mylyn.tasks.core.ITaskAttachment> attachments = org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.getSelectedAttachments(null);
        if (attachments.isEmpty()) {
            return null;
        }
        return attachments.get(0);
    }

    public static java.util.List<org.eclipse.mylyn.tasks.core.ITaskAttachment> getSelectedAttachments(org.eclipse.core.commands.ExecutionEvent event) {
        if (event == null) {
            // create bogus execution event to obtain active menu selection
            org.eclipse.ui.handlers.IHandlerService service = ((org.eclipse.ui.handlers.IHandlerService) (org.eclipse.ui.PlatformUI.getWorkbench().getService(org.eclipse.ui.handlers.IHandlerService.class)));
            if (service != null) {
                event = new org.eclipse.core.commands.ExecutionEvent(null, java.util.Collections.emptyMap(), null, service.getCurrentState());
            } else {
                // failed
                return java.util.Collections.emptyList();
            }
        }
        // prefer local selection, e.g from table in Task Editor
        org.eclipse.jface.viewers.ISelection selection = org.eclipse.ui.handlers.HandlerUtil.getActiveMenuSelection(event);
        if ((selection == null) || selection.isEmpty()) {
            selection = org.eclipse.ui.handlers.HandlerUtil.getCurrentSelection(event);
        }
        if ((selection instanceof org.eclipse.jface.viewers.IStructuredSelection) && (!selection.isEmpty())) {
            java.util.List<?> items = ((org.eclipse.jface.viewers.IStructuredSelection) (selection)).toList();
            java.util.List<org.eclipse.mylyn.tasks.core.ITaskAttachment> attachments = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.ITaskAttachment>();
            for (java.lang.Object item : items) {
                if (item instanceof org.eclipse.mylyn.tasks.core.ITaskAttachment) {
                    attachments.add(((org.eclipse.mylyn.tasks.core.ITaskAttachment) (item)));
                }
            }
            return attachments;
        }
        return java.util.Collections.emptyList();
    }

    /**
     *
     * @deprecated use {@link #getSelectedAttachments(ExecutionEvent)} instead
     */
    @java.lang.Deprecated
    public static java.util.List<org.eclipse.mylyn.tasks.core.ITaskAttachment> getSelectedAttachments() {
        org.eclipse.ui.IWorkbenchWindow window = org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            org.eclipse.jface.viewers.ISelection windowSelection = window.getSelectionService().getSelection();
            if ((windowSelection instanceof org.eclipse.jface.viewers.IStructuredSelection) && (!windowSelection.isEmpty())) {
                org.eclipse.jface.viewers.IStructuredSelection selection = ((org.eclipse.jface.viewers.IStructuredSelection) (windowSelection));
                java.util.List<?> items = selection.toList();
                java.util.List<org.eclipse.mylyn.tasks.core.ITaskAttachment> attachments = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.ITaskAttachment>();
                for (java.lang.Object item : items) {
                    if (item instanceof org.eclipse.mylyn.tasks.core.ITaskAttachment) {
                        attachments.add(((org.eclipse.mylyn.tasks.core.ITaskAttachment) (item)));
                    }
                }
                return attachments;
            }
        }
        return java.util.Collections.emptyList();
    }

    public static java.lang.String getAttachmentFilename(org.eclipse.mylyn.tasks.core.ITaskAttachment attachment) {
        org.eclipse.core.runtime.Assert.isNotNull(attachment);
        java.lang.String name = attachment.getFileName();
        if ((name != null) && (name.length() > 0)) {
            return org.eclipse.mylyn.commons.core.CoreUtil.asFileName(name);
        } else {
            java.lang.String extension = "";// $NON-NLS-1$

            // if no filename is set, make one up with the proper extension so
            // we can support opening in that filetype's default editor
            java.lang.String ctype = attachment.getContentType();
            if (ctype != null) {
                if (ctype.endsWith(org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.CTYPE_HTML)) {
                    extension = ".html";// $NON-NLS-1$

                } else if (ctype.startsWith(org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.CTYPE_TEXT)) {
                    extension = ".txt";// $NON-NLS-1$

                } else if (ctype.endsWith(org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.CTYPE_ZIP)) {
                    extension = ".zip";// $NON-NLS-1$

                } else {
                    int i = ctype.lastIndexOf("/");// $NON-NLS-1$

                    if (i != (-1)) {
                        extension = "." + ctype.substring(i + 1);// $NON-NLS-1$

                    }
                }
            }
            return org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.ATTACHMENT_DEFAULT_NAME + extension;
        }
    }
}