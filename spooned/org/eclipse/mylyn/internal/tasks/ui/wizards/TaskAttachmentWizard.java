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
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.wizards;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.commons.ui.wizard.ScreenshotCreationPage;
import org.eclipse.mylyn.internal.tasks.core.sync.SubmitTaskAttachmentJob;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentModel;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.sync.SubmitJob;
import org.eclipse.mylyn.tasks.core.sync.SubmitJobEvent;
import org.eclipse.mylyn.tasks.core.sync.SubmitJobListener;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;
/**
 * A wizard to add a new attachment to a task report.
 *
 * @since 3.0
 * @author Steffen Pingel
 */
public class TaskAttachmentWizard extends org.eclipse.jface.wizard.Wizard {
    static class ClipboardTaskAttachmentSource extends org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource {
        private byte[] data;

        public static boolean isSupportedType(org.eclipse.swt.widgets.Display display) {
            org.eclipse.swt.dnd.Clipboard clipboard = new org.eclipse.swt.dnd.Clipboard(display);
            org.eclipse.swt.dnd.TransferData[] types = clipboard.getAvailableTypes();
            for (org.eclipse.swt.dnd.TransferData transferData : types) {
                java.util.List<org.eclipse.swt.dnd.Transfer> transfers = org.eclipse.mylyn.internal.tasks.ui.wizards.TaskAttachmentWizard.ClipboardTaskAttachmentSource.getTransfers();
                for (org.eclipse.swt.dnd.Transfer transfer : transfers) {
                    if (transfer.isSupportedType(transferData)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private static java.util.List<org.eclipse.swt.dnd.Transfer> transfers;

        private static java.util.List<org.eclipse.swt.dnd.Transfer> getTransfers() {
            if (org.eclipse.mylyn.internal.tasks.ui.wizards.TaskAttachmentWizard.ClipboardTaskAttachmentSource.transfers != null) {
                return org.eclipse.mylyn.internal.tasks.ui.wizards.TaskAttachmentWizard.ClipboardTaskAttachmentSource.transfers;
            }
            org.eclipse.mylyn.internal.tasks.ui.wizards.TaskAttachmentWizard.ClipboardTaskAttachmentSource.transfers = new java.util.ArrayList<org.eclipse.swt.dnd.Transfer>();
            try {
                java.lang.Class<?> clazz = java.lang.Class.forName("org.eclipse.swt.dnd.ImageTransfer");// $NON-NLS-1$

                java.lang.reflect.Method method = clazz.getMethod("getInstance");// $NON-NLS-1$

                if (method != null) {
                    org.eclipse.mylyn.internal.tasks.ui.wizards.TaskAttachmentWizard.ClipboardTaskAttachmentSource.transfers.add(((org.eclipse.swt.dnd.Transfer) (method.invoke(null))));
                }
            } catch (java.lang.Exception e) {
                // ignore
            } catch (java.lang.LinkageError e) {
                // ignore
            }
            org.eclipse.mylyn.internal.tasks.ui.wizards.TaskAttachmentWizard.ClipboardTaskAttachmentSource.transfers.add(org.eclipse.swt.dnd.TextTransfer.getInstance());
            return org.eclipse.mylyn.internal.tasks.ui.wizards.TaskAttachmentWizard.ClipboardTaskAttachmentSource.transfers;
        }

        private java.lang.Object contents;

        public ClipboardTaskAttachmentSource() {
            org.eclipse.swt.custom.BusyIndicator.showWhile(org.eclipse.ui.PlatformUI.getWorkbench().getDisplay(), new java.lang.Runnable() {
                public void run() {
                    org.eclipse.swt.dnd.Clipboard clipboard = new org.eclipse.swt.dnd.Clipboard(org.eclipse.ui.PlatformUI.getWorkbench().getDisplay());
                    java.util.List<org.eclipse.swt.dnd.Transfer> transfers = org.eclipse.mylyn.internal.tasks.ui.wizards.TaskAttachmentWizard.ClipboardTaskAttachmentSource.getTransfers();
                    for (org.eclipse.swt.dnd.Transfer transfer : transfers) {
                        contents = clipboard.getContents(transfer);
                        if (contents != null) {
                            break;
                        }
                    }
                    clipboard.dispose();
                }
            });
        }

        @java.lang.Override
        public java.io.InputStream createInputStream(org.eclipse.core.runtime.IProgressMonitor monitor) throws org.eclipse.core.runtime.CoreException {
            byte[] bytes = getData();
            if (bytes != null) {
                return new java.io.ByteArrayInputStream(data);
            }
            throw new org.eclipse.core.runtime.CoreException(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Invalid content type."));// $NON-NLS-1$

        }

        @java.lang.Override
        public java.lang.String getContentType() {
            if (contents instanceof java.lang.String) {
                return "text/plain";// $NON-NLS-1$

            } else if (contents instanceof org.eclipse.swt.graphics.ImageData) {
                return "image/png";// $NON-NLS-1$

            }
            return "application/octet-stream";// $NON-NLS-1$

        }

        @java.lang.Override
        public java.lang.String getDescription() {
            return null;
        }

        @java.lang.Override
        public long getLength() {
            byte[] bytes = getData();
            return bytes != null ? bytes.length : -1;
        }

        private byte[] getData() {
            if (data == null) {
                if (contents instanceof java.lang.String) {
                    data = ((java.lang.String) (contents)).getBytes();
                } else if (contents instanceof org.eclipse.swt.graphics.ImageData) {
                    org.eclipse.swt.graphics.ImageLoader loader = new org.eclipse.swt.graphics.ImageLoader();
                    loader.data = new org.eclipse.swt.graphics.ImageData[]{ ((org.eclipse.swt.graphics.ImageData) (contents)) };
                    java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                    loader.save(out, org.eclipse.swt.SWT.IMAGE_PNG);
                    data = out.toByteArray();
                }
            }
            return data;
        }

        @java.lang.Override
        public java.lang.String getName() {
            if (contents instanceof java.lang.String) {
                return "clipboard.txt";// $NON-NLS-1$

            } else if (contents instanceof org.eclipse.swt.graphics.ImageData) {
                return "clipboard.png";// $NON-NLS-1$

            }
            return "";// $NON-NLS-1$

        }

        @java.lang.Override
        public boolean isLocal() {
            return true;
        }
    }

    static class ImageSource extends org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource {
        private java.io.File file;

        private final org.eclipse.mylyn.commons.ui.wizard.ScreenshotCreationPage page;

        public ImageSource(org.eclipse.mylyn.commons.ui.wizard.ScreenshotCreationPage page) {
            this.page = page;
        }

        @java.lang.Override
        public java.io.InputStream createInputStream(org.eclipse.core.runtime.IProgressMonitor monitor) throws org.eclipse.core.runtime.CoreException {
            try {
                if ((file == null) || page.isImageDirty()) {
                    org.eclipse.swt.graphics.Image image = page.createImage();
                    page.setImageDirty(false);
                    try {
                        file = java.io.File.createTempFile("screenshot", ".png");// $NON-NLS-1$ //$NON-NLS-2$

                        file.deleteOnExit();
                        org.eclipse.swt.graphics.ImageLoader loader = new org.eclipse.swt.graphics.ImageLoader();
                        loader.data = new org.eclipse.swt.graphics.ImageData[]{ image.getImageData() };
                        // TODO create image in memory?
                        java.io.FileOutputStream out = new java.io.FileOutputStream(file);
                        try {
                            loader.save(out, org.eclipse.swt.SWT.IMAGE_PNG);
                        } finally {
                            out.close();
                        }
                    } finally {
                        image.dispose();
                    }
                }
                return new java.io.FileInputStream(file);
            } catch (java.io.IOException e) {
                throw new org.eclipse.core.runtime.CoreException(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, e.getMessage(), e));
            }
        }

        @java.lang.Override
        public java.lang.String getContentType() {
            return "image/png";// $NON-NLS-1$

        }

        @java.lang.Override
        public java.lang.String getDescription() {
            return Messages.TaskAttachmentWizard_Screenshot;
        }

        @java.lang.Override
        public long getLength() {
            return file != null ? file.length() : -1;
        }

        @java.lang.Override
        public java.lang.String getName() {
            return "screenshot.png";// $NON-NLS-1$

        }

        @java.lang.Override
        public boolean isLocal() {
            return true;
        }
    }

    public enum Mode {

        DEFAULT,
        SCREENSHOT;}

    private static final java.lang.String DIALOG_SETTINGS_KEY = "AttachmentWizard";// $NON-NLS-1$


    private final org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector;

    private org.eclipse.jface.wizard.IWizardPage editPage;

    private org.eclipse.mylyn.internal.tasks.ui.wizards.TaskAttachmentWizard.Mode mode = org.eclipse.mylyn.internal.tasks.ui.wizards.TaskAttachmentWizard.Mode.DEFAULT;

    private final org.eclipse.mylyn.tasks.core.data.TaskAttachmentModel model;

    private org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentPreviewPage previewPage;

    public TaskAttachmentWizard(org.eclipse.mylyn.tasks.core.TaskRepository taskRepository, org.eclipse.mylyn.tasks.core.ITask task, org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttachment) {
        org.eclipse.core.runtime.Assert.isNotNull(taskRepository);
        org.eclipse.core.runtime.Assert.isNotNull(taskAttachment);
        this.model = new org.eclipse.mylyn.tasks.core.data.TaskAttachmentModel(taskRepository, task, taskAttachment);
        this.connector = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryManager().getRepositoryConnector(taskRepository.getConnectorKind());
        setMode(org.eclipse.mylyn.internal.tasks.ui.wizards.TaskAttachmentWizard.Mode.DEFAULT);
        setNeedsProgressMonitor(true);
        setDialogSettings(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getDialogSettings().getSection(org.eclipse.mylyn.internal.tasks.ui.wizards.TaskAttachmentWizard.DIALOG_SETTINGS_KEY));
    }

    @java.lang.Override
    public void addPages() {
        if (model.getSource() == null) {
            if (mode == org.eclipse.mylyn.internal.tasks.ui.wizards.TaskAttachmentWizard.Mode.SCREENSHOT) {
                org.eclipse.mylyn.commons.ui.wizard.ScreenshotCreationPage page = new org.eclipse.mylyn.commons.ui.wizard.ScreenshotCreationPage() {
                    @java.lang.Override
                    protected org.eclipse.jface.dialogs.IDialogSettings getDialogSettings() {
                        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin plugin = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault();
                        org.eclipse.jface.dialogs.IDialogSettings settings = plugin.getDialogSettings();
                        java.lang.String DIALOG_SETTINGS = org.eclipse.mylyn.commons.ui.wizard.ScreenshotCreationPage.class.getCanonicalName();
                        org.eclipse.jface.dialogs.IDialogSettings section = settings.getSection(DIALOG_SETTINGS);
                        if (section == null) {
                            section = settings.addNewSection(DIALOG_SETTINGS);
                        }
                        return section;
                    }
                };
                org.eclipse.mylyn.internal.tasks.ui.wizards.TaskAttachmentWizard.ImageSource source = new org.eclipse.mylyn.internal.tasks.ui.wizards.TaskAttachmentWizard.ImageSource(page);
                model.setSource(source);
                model.setContentType(source.getContentType());
                addPage(page);
            } else {
                addPage(new org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentSourcePage(model));
            }
        }
        previewPage = new org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentPreviewPage(model);
        addPage(previewPage);
        org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnectorUi(model.getTaskRepository().getConnectorKind());
        editPage = connectorUi.getTaskAttachmentPage(model);
        addPage(editPage);
    }

    public org.eclipse.mylyn.internal.tasks.ui.wizards.TaskAttachmentWizard.Mode getMode() {
        return mode;
    }

    public org.eclipse.mylyn.tasks.core.data.TaskAttachmentModel getModel() {
        return model;
    }

    public org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource getSource() {
        return model.getSource();
    }

    private void handleDone(org.eclipse.mylyn.tasks.core.sync.SubmitJob job) {
        org.eclipse.core.runtime.IStatus status = job.getStatus();
        if ((status != null) && (status.getSeverity() != org.eclipse.core.runtime.IStatus.CANCEL)) {
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayStatus(Messages.TaskAttachmentWizard_Attachment_Failed, job.getStatus());
        }
    }

    @java.lang.Override
    public boolean canFinish() {
        // InputAttachmentSourcePage relies on getNextPage() being called, do not allow wizard to finish on first page
        if ((getContainer() != null) && (getContainer().getCurrentPage() instanceof org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentSourcePage)) {
            return false;
        }
        return super.canFinish();
    }

    @java.lang.Override
    public boolean performFinish() {
        org.eclipse.mylyn.tasks.core.sync.SubmitJob job = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getJobFactory().createSubmitTaskAttachmentJob(connector, model.getTaskRepository(), model.getTask(), model.getSource(), model.getComment(), model.getAttribute());
        final boolean attachContext = model.getAttachContext();
        job.addSubmitJobListener(new org.eclipse.mylyn.tasks.core.sync.SubmitJobListener() {
            @java.lang.Override
            public void done(org.eclipse.mylyn.tasks.core.sync.SubmitJobEvent event) {
                // ignore
            }

            @java.lang.Override
            public void taskSubmitted(org.eclipse.mylyn.tasks.core.sync.SubmitJobEvent event, org.eclipse.core.runtime.IProgressMonitor monitor) throws org.eclipse.core.runtime.CoreException {
                if (attachContext) {
                    monitor.subTask(Messages.TaskAttachmentWizard_Attaching_context);
                    org.eclipse.mylyn.tasks.core.data.TaskData taskData = model.getAttribute().getTaskData();
                    org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute = taskData.getRoot().createMappedAttribute(org.eclipse.mylyn.tasks.core.data.TaskAttribute.NEW_ATTACHMENT);
                    org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.postContext(connector, model.getTaskRepository(), model.getTask(), null, taskAttribute, monitor);
                }
            }

            @java.lang.Override
            public void taskSynchronized(org.eclipse.mylyn.tasks.core.sync.SubmitJobEvent event, org.eclipse.core.runtime.IProgressMonitor monitor) throws org.eclipse.core.runtime.CoreException {
                // ignore
            }
        });
        if (previewPage.runInBackground()) {
            runInBackground(job);
            return false;
        } else {
            return runInWizard(job);
        }
    }

    private void runInBackground(final org.eclipse.mylyn.tasks.core.sync.SubmitJob job) {
        getContainer().getShell().setVisible(false);
        job.addJobChangeListener(new org.eclipse.core.runtime.jobs.JobChangeAdapter() {
            @java.lang.Override
            public void done(org.eclipse.core.runtime.jobs.IJobChangeEvent event) {
                org.eclipse.swt.widgets.Display.getDefault().asyncExec(new java.lang.Runnable() {
                    public void run() {
                        if (job.getStatus() != null) {
                            getContainer().getShell().setVisible(true);
                        }
                        handleDone(job);
                        if (job.getStatus() == null) {
                            getContainer().getShell().close();
                        }
                    }
                });
            }
        });
        job.schedule();
    }

    private boolean runInWizard(final org.eclipse.mylyn.tasks.core.sync.SubmitJob job) {
        try {
            getContainer().run(true, true, new org.eclipse.jface.operation.IRunnableWithProgress() {
                public void run(org.eclipse.core.runtime.IProgressMonitor monitor) throws java.lang.reflect.InvocationTargetException, java.lang.InterruptedException {
                    if (((org.eclipse.mylyn.internal.tasks.core.sync.SubmitTaskAttachmentJob) (job)).run(monitor) == org.eclipse.core.runtime.Status.CANCEL_STATUS) {
                        throw new java.lang.InterruptedException();
                    }
                }
            });
            handleDone(job);
            return job.getStatus() == null;
        } catch (java.lang.reflect.InvocationTargetException e) {
            org.eclipse.ui.statushandlers.StatusManager.getManager().handle(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Unexpected error", e), org.eclipse.ui.statushandlers.StatusManager.SHOW | org.eclipse.ui.statushandlers.StatusManager.LOG);// $NON-NLS-1$

            return false;
        } catch (java.lang.InterruptedException e) {
            // canceled
            return false;
        }
    }

    public void setMode(org.eclipse.mylyn.internal.tasks.ui.wizards.TaskAttachmentWizard.Mode mode) {
        this.mode = mode;
        if (mode == org.eclipse.mylyn.internal.tasks.ui.wizards.TaskAttachmentWizard.Mode.SCREENSHOT) {
            setWindowTitle(Messages.TaskAttachmentWizard_Attach_Screenshot);
            setDefaultPageImageDescriptor(org.eclipse.mylyn.commons.ui.CommonImages.BANNER_SCREENSHOT);
        } else {
            setWindowTitle(Messages.TaskAttachmentWizard_Add_Attachment);
            setDefaultPageImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.BANNER_REPOSITORY);
        }
    }

    public void setSource(org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource source) {
        this.model.setSource(source);
    }
}