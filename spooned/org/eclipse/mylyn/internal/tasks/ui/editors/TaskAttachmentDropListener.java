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
 *     Maarten Meijer - improvements
 *     Robert Munteanu - fix for bug 359539
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.editors;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource;
import org.eclipse.mylyn.internal.tasks.core.data.TextTaskAttachmentSource;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.wizards.NewAttachmentWizardDialog;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.ui.TaskDropListener.Operation;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.TransferData;
/**
 *
 * @author Mik Kersten
 * @author Maarten Meijer
 * @author Steffen Pingel
 */
public class TaskAttachmentDropListener implements org.eclipse.swt.dnd.DropTargetListener {
    private final org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage page;

    public TaskAttachmentDropListener(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage page) {
        this.page = page;
    }

    public void dragEnter(org.eclipse.swt.dnd.DropTargetEvent event) {
        if (event.detail == org.eclipse.swt.dnd.DND.DROP_DEFAULT) {
            if ((event.operations & org.eclipse.swt.dnd.DND.DROP_COPY) != 0) {
                event.detail = org.eclipse.swt.dnd.DND.DROP_COPY;
            } else {
                event.detail = org.eclipse.swt.dnd.DND.DROP_NONE;
            }
        }
        // will accept text but prefer to have files dropped
        for (org.eclipse.swt.dnd.TransferData dataType : event.dataTypes) {
            if (org.eclipse.jface.util.LocalSelectionTransfer.getTransfer().isSupportedType(dataType) || org.eclipse.swt.dnd.FileTransfer.getInstance().isSupportedType(dataType)) {
                event.currentDataType = dataType;
                // files and tasks should only be copied
                if (event.detail != org.eclipse.swt.dnd.DND.DROP_COPY) {
                    event.detail = org.eclipse.swt.dnd.DND.DROP_NONE;
                }
                break;
            }
        }
    }

    public void dragOver(org.eclipse.swt.dnd.DropTargetEvent event) {
        event.feedback = org.eclipse.swt.dnd.DND.FEEDBACK_SELECT | org.eclipse.swt.dnd.DND.FEEDBACK_SCROLL;
    }

    public void dragOperationChanged(org.eclipse.swt.dnd.DropTargetEvent event) {
        if ((event.detail == org.eclipse.swt.dnd.DND.DROP_DEFAULT) || ((event.operations & org.eclipse.swt.dnd.DND.DROP_COPY) != 0)) {
            event.detail = org.eclipse.swt.dnd.DND.DROP_COPY;
        } else {
            event.detail = org.eclipse.swt.dnd.DND.DROP_NONE;
        }
        // allow text to be moved but files should only be copied
        if (org.eclipse.swt.dnd.FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
            if (event.detail != org.eclipse.swt.dnd.DND.DROP_COPY) {
                event.detail = org.eclipse.swt.dnd.DND.DROP_NONE;
            }
        }
    }

    public void dragLeave(org.eclipse.swt.dnd.DropTargetEvent event) {
    }

    public void dropAccept(org.eclipse.swt.dnd.DropTargetEvent event) {
    }

    public void drop(org.eclipse.swt.dnd.DropTargetEvent event) {
        if (org.eclipse.jface.util.LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType)) {
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskDropHandler().loadTaskDropListeners();
            org.eclipse.jface.viewers.ISelection selection = org.eclipse.jface.util.LocalSelectionTransfer.getTransfer().getSelection();
            java.util.List<org.eclipse.mylyn.tasks.core.ITask> tasksToMove = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTasksFromSelection(selection);
            if (!tasksToMove.isEmpty()) {
                org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskDropHandler().fireTaskDropped(tasksToMove, page.getTask(), org.eclipse.mylyn.tasks.ui.TaskDropListener.Operation.DROP_ON_TASK_EDITOR);
            } else {
                attachFirstFile(org.eclipse.mylyn.internal.tasks.ui.editors.TaskAttachmentDropListener.getFilesFromSelection(selection));
            }
        }
        if (org.eclipse.swt.dnd.TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
            java.lang.String text = ((java.lang.String) (event.data));
            org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.openNewAttachmentWizard(page, null, new org.eclipse.mylyn.internal.tasks.core.data.TextTaskAttachmentSource(text));
        }
        if (org.eclipse.swt.dnd.FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
            java.lang.String[] files = ((java.lang.String[]) (event.data));
            attachFirstFile(files);
        }
    }

    protected void attachFirstFile(java.lang.String[] files) {
        if ((files != null) && (files.length > 0)) {
            java.io.File file = new java.io.File(files[0]);
            org.eclipse.mylyn.internal.tasks.ui.wizards.NewAttachmentWizardDialog dialog = org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.openNewAttachmentWizard(page, null, new org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource(file));
            if (files.length > 1) {
                dialog.setMessage(Messages.TaskAttachmentDropListener_Note_that_only_the_first_file_dragged_will_be_attached, org.eclipse.jface.dialogs.IMessageProvider.WARNING);
            }
        }
    }

    public static java.lang.String[] getFilesFromSelection(org.eclipse.jface.viewers.ISelection selection) {
        java.util.List<java.lang.String> files = new java.util.ArrayList<java.lang.String>();
        if (selection instanceof org.eclipse.jface.viewers.IStructuredSelection) {
            for (java.lang.Object element : ((org.eclipse.jface.viewers.IStructuredSelection) (selection)).toList()) {
                org.eclipse.core.resources.IResource resource = null;
                if (element instanceof org.eclipse.core.resources.IResource) {
                    resource = ((org.eclipse.core.resources.IResource) (element));
                } else if (element instanceof org.eclipse.core.runtime.IAdaptable) {
                    org.eclipse.core.runtime.IAdaptable adaptable = ((org.eclipse.core.runtime.IAdaptable) (element));
                    resource = ((org.eclipse.core.resources.IResource) (adaptable.getAdapter(org.eclipse.core.resources.IResource.class)));
                }
                if ((resource != null) && (resource.getRawLocation() != null)) {
                    files.add(resource.getRawLocation().toOSString());
                }
            }
        }
        return files.toArray(new java.lang.String[files.size()]);
    }
}