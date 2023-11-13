/**
 * *****************************************************************************
 * Copyright (c) 2010, 2013 Peter Stibrany and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Peter Stibrany - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil;
import org.eclipse.mylyn.tasks.core.ITaskAttachment;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbenchPage;
/**
 *
 * @author Peter Stibrany
 */
public class TaskAttachmentEditorViewer implements org.eclipse.mylyn.internal.tasks.ui.ITaskAttachmentViewer {
    private static final java.lang.String PREF_DO_NOT_WARN_BEFORE_OPENING_ATTACHMENTS = "do.not.warn.before.opening.attachments";// $NON-NLS-1$


    private final org.eclipse.ui.IEditorDescriptor descriptor;

    private final boolean isWorkbenchDefault;

    private final boolean isSystem;

    TaskAttachmentEditorViewer(org.eclipse.ui.IEditorDescriptor descriptor) {
        this(descriptor, false);
    }

    TaskAttachmentEditorViewer(org.eclipse.ui.IEditorDescriptor descriptor, boolean isWorkbenchDefault) {
        this(descriptor, isWorkbenchDefault, false);
    }

    TaskAttachmentEditorViewer(org.eclipse.ui.IEditorDescriptor descriptor, boolean isWorkbenchDefault, boolean isSystem) {
        this.descriptor = descriptor;
        this.isWorkbenchDefault = isWorkbenchDefault;
        this.isSystem = isSystem;
    }

    public java.lang.String getId() {
        return descriptor.getId();
    }

    public java.lang.String getLabel() {
        return descriptor.getLabel();
    }

    public void openAttachment(final org.eclipse.ui.IWorkbenchPage page, final org.eclipse.mylyn.tasks.core.ITaskAttachment attachment) throws org.eclipse.core.runtime.CoreException {
        if (promptToConfirmOpen(attachment)) {
            org.eclipse.mylyn.internal.tasks.ui.DownloadAndOpenTaskAttachmentJob job = new org.eclipse.mylyn.internal.tasks.ui.DownloadAndOpenTaskAttachmentJob(java.text.MessageFormat.format(Messages.TaskAttachmentEditorViewer_openingAttachment, org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.getAttachmentFilename(attachment)), attachment, page, descriptor.getId());
            org.eclipse.mylyn.commons.workbench.WorkbenchUtil.busyCursorWhile(job);
        }
    }

    private boolean promptToConfirmOpen(final org.eclipse.mylyn.tasks.core.ITaskAttachment attachment) {
        if (isSystem()) {
            org.eclipse.jface.preference.IPreferenceStore store = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore();
            if (!store.getBoolean(org.eclipse.mylyn.internal.tasks.ui.TaskAttachmentEditorViewer.PREF_DO_NOT_WARN_BEFORE_OPENING_ATTACHMENTS)) {
                org.eclipse.jface.dialogs.MessageDialogWithToggle dialog = org.eclipse.jface.dialogs.MessageDialogWithToggle.openYesNoQuestion(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell(), Messages.TaskAttachmentEditorViewer_Open_Attachment, org.eclipse.osgi.util.NLS.bind(Messages.TaskAttachmentEditorViewer_Some_files_can_harm_your_computer, attachment.getFileName()), Messages.TaskAttachmentEditorViewer_Do_not_warn_me_again, false, null, null);
                if (dialog.getReturnCode() == org.eclipse.jface.dialogs.IDialogConstants.YES_ID) {
                    if (dialog.getToggleState()) {
                        store.setValue(org.eclipse.mylyn.internal.tasks.ui.TaskAttachmentEditorViewer.PREF_DO_NOT_WARN_BEFORE_OPENING_ATTACHMENTS, true);
                        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().savePluginPreferences();
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isWorkbenchDefault() {
        return isWorkbenchDefault;
    }

    protected boolean isSystem() {
        return isSystem;
    }
}