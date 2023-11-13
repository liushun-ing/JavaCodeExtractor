/**
 * *****************************************************************************
 * Copyright (c) 2004, 2011 Tasktop Technologies and others.
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
package org.eclipse.mylyn.internal.tasks.ui.actions;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil;
import org.eclipse.mylyn.internal.tasks.ui.util.DownloadAttachmentJob;
import org.eclipse.mylyn.internal.tasks.ui.util.Messages;
import org.eclipse.mylyn.tasks.core.ITaskAttachment;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
/**
 *
 * @author Peter Stibrany
 */
public class SaveAttachmentsAction extends org.eclipse.jface.action.Action {
    public SaveAttachmentsAction(java.lang.String text) {
        super(text);
    }

    @java.lang.Override
    public void run() {
        java.util.List<org.eclipse.mylyn.tasks.core.ITaskAttachment> attachments = org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.getSelectedAttachments(null);
        if (attachments.isEmpty()) {
            return;
        } else if (attachments.size() == 1) {
            saveSingleAttachment(attachments.get(0));
        } else {
            saveAllAttachments(attachments);
        }
    }

    /**
     * Displays Save File dialog, then downloads and saves single attachment.
     */
    private void saveSingleAttachment(org.eclipse.mylyn.tasks.core.ITaskAttachment attachment) {
        org.eclipse.swt.widgets.FileDialog fileChooser = new org.eclipse.swt.widgets.FileDialog(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell(), org.eclipse.swt.SWT.SAVE);
        fileChooser.setFileName(org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.getAttachmentFilename(attachment));
        java.io.File initDirectory = getInitialDirectory();
        if (initDirectory != null) {
            fileChooser.setFilterPath(initDirectory.getAbsolutePath());
        }
        java.lang.String filePath = fileChooser.open();
        // check if the dialog was canceled or an error occurred
        if (filePath == null) {
            return;
        }
        java.io.File file = new java.io.File(filePath);
        if (file.exists()) {
            if (!org.eclipse.jface.dialogs.MessageDialog.openConfirm(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell(), org.eclipse.mylyn.internal.tasks.ui.util.Messages.TasksUiMenus_File_exists_, org.eclipse.mylyn.internal.tasks.ui.util.Messages.TasksUiMenus_Overwrite_existing_file_ + file.getName())) {
                return;
            }
        }
        initDirectory = file.getParentFile();
        if (initDirectory != null) {
            saveInitialDirectory(initDirectory.getAbsolutePath());
        }
        org.eclipse.mylyn.internal.tasks.ui.util.DownloadAttachmentJob job = new org.eclipse.mylyn.internal.tasks.ui.util.DownloadAttachmentJob(attachment, file);
        job.setUser(true);
        job.schedule();
    }

    private void saveAllAttachments(java.util.List<org.eclipse.mylyn.tasks.core.ITaskAttachment> attachments) {
        org.eclipse.swt.widgets.DirectoryDialog dialog = new org.eclipse.swt.widgets.DirectoryDialog(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell());
        dialog.setText(org.eclipse.mylyn.internal.tasks.ui.util.Messages.SaveAttachmentsAction_selectDirectory);
        dialog.setMessage(org.eclipse.mylyn.internal.tasks.ui.util.Messages.SaveAttachmentsAction_selectDirectoryHint);
        java.io.File initDirectory = getInitialDirectory();
        if (initDirectory != null) {
            dialog.setFilterPath(initDirectory.getAbsolutePath());
        }
        java.lang.String directoryPath = dialog.open();
        if (directoryPath == null) {
            return;
        }
        saveInitialDirectory(directoryPath);
        final java.io.File directory = new java.io.File(directoryPath);
        if (!directory.exists()) {
            org.eclipse.jface.dialogs.MessageDialog.openError(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell(), org.eclipse.mylyn.internal.tasks.ui.util.Messages.SaveAttachmentsAction_directoryDoesntExist, org.eclipse.osgi.util.NLS.bind(org.eclipse.mylyn.internal.tasks.ui.util.Messages.SaveAttachmentsAction_directoryDoesntExist0, directoryPath));
            return;
        }
        for (org.eclipse.mylyn.tasks.core.ITaskAttachment attachment : attachments) {
            java.lang.String filename = org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.getAttachmentFilename(attachment);
            java.io.File file = getTargetFile(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell(), directory, filename);
            if (file != null) {
                org.eclipse.mylyn.internal.tasks.ui.util.DownloadAttachmentJob job = new org.eclipse.mylyn.internal.tasks.ui.util.DownloadAttachmentJob(attachment, file);
                job.setUser(true);
                job.schedule();
            }
        }
    }

    private java.io.File getTargetFile(org.eclipse.swt.widgets.Shell shell, java.io.File directory, java.lang.String filename) {
        java.io.File attachFile = new java.io.File(directory, filename);
        while (true) {
            if (!attachFile.exists()) {
                return attachFile;
            }
            boolean overwrite = org.eclipse.jface.dialogs.MessageDialog.openQuestion(shell, org.eclipse.osgi.util.NLS.bind(org.eclipse.mylyn.internal.tasks.ui.util.Messages.SaveAttachmentsAction_overwriteFile0, attachFile.getName()), org.eclipse.osgi.util.NLS.bind(org.eclipse.mylyn.internal.tasks.ui.util.Messages.SaveAttachmentsAction_fileExists_doYouWantToOverwrite0, attachFile.getAbsolutePath()));
            if (overwrite) {
                return attachFile;
            }
            org.eclipse.swt.widgets.FileDialog fileDialog = new org.eclipse.swt.widgets.FileDialog(shell, org.eclipse.swt.SWT.SAVE);
            fileDialog.setFilterPath(directory.getAbsolutePath());
            fileDialog.setFileName(attachFile.getName());
            filename = fileDialog.open();
            if (filename == null) {
                return null;
            }
            attachFile = new java.io.File(filename);
        } 
    }

    private java.io.File getInitialDirectory() {
        java.lang.String dirName = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().getString(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.DEFAULT_ATTACHMENTS_DIRECTORY);
        if ((dirName == null) || (dirName.trim().length() == 0)) {
            return null;
        }
        java.io.File dirFile = new java.io.File(dirName).getAbsoluteFile();
        // if file
        while ((dirFile != null) && (!dirFile.exists())) {
            dirFile = dirFile.getParentFile();
        } 
        return dirFile;
    }

    private void saveInitialDirectory(java.lang.String directory) {
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().putValue(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.DEFAULT_ATTACHMENTS_DIRECTORY, directory);
    }
}