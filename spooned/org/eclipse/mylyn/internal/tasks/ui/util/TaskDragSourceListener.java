/**
 * *****************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Jevgeni Holodkov - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.util;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.actions.CopyTaskDetailsAction;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
/**
 *
 * @author Mik Kersten
 * @author Leo Dos Santos
 * @author Steffen Pingel
 */
public class TaskDragSourceListener extends org.eclipse.swt.dnd.DragSourceAdapter {
    private org.eclipse.jface.viewers.IStructuredSelection currentSelection;

    private final org.eclipse.jface.viewers.ISelectionProvider selectionProvider;

    public TaskDragSourceListener(org.eclipse.jface.viewers.ISelectionProvider selectionProvider) {
        this.selectionProvider = selectionProvider;
    }

    @java.lang.Override
    public void dragStart(org.eclipse.swt.dnd.DragSourceEvent event) {
        org.eclipse.jface.viewers.ISelection selection = selectionProvider.getSelection();
        if ((selection instanceof org.eclipse.jface.viewers.IStructuredSelection) && (!selection.isEmpty())) {
            this.currentSelection = ((org.eclipse.jface.viewers.IStructuredSelection) (selection));
        } else {
            this.currentSelection = null;
            event.doit = false;
        }
    }

    @java.lang.Override
    public void dragSetData(org.eclipse.swt.dnd.DragSourceEvent event) {
        if ((currentSelection == null) || currentSelection.isEmpty()) {
            return;
        }
        if (org.eclipse.jface.util.LocalSelectionTransfer.getTransfer().isSupportedType(event.dataType)) {
            org.eclipse.jface.util.LocalSelectionTransfer.getTransfer().setSelection(currentSelection);
        } else if (org.eclipse.swt.dnd.FileTransfer.getInstance().isSupportedType(event.dataType)) {
            try {
                java.io.File file = java.io.File.createTempFile(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.EXPORT_FILE_NAME, org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.FILE_EXTENSION);
                file.deleteOnExit();
                org.eclipse.mylyn.internal.tasks.ui.util.ImportExportUtil.export(file, currentSelection);
                java.lang.String[] paths = new java.lang.String[1];
                paths[0] = file.getAbsolutePath();
                event.data = paths;
            } catch (org.eclipse.core.runtime.CoreException e) {
                org.eclipse.mylyn.commons.core.StatusHandler.log(// $NON-NLS-1$
                new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Problems encountered dragging task", e));
            } catch (java.io.IOException e) {
                org.eclipse.mylyn.commons.core.StatusHandler.log(// $NON-NLS-1$
                new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Problems encountered dragging task", e));
            }
        } else if (org.eclipse.swt.dnd.TextTransfer.getInstance().isSupportedType(event.dataType)) {
            event.data = org.eclipse.mylyn.internal.tasks.ui.actions.CopyTaskDetailsAction.getTextForTask(currentSelection.getFirstElement());
        }
    }

    @java.lang.Override
    public void dragFinished(org.eclipse.swt.dnd.DragSourceEvent event) {
        if (org.eclipse.jface.util.LocalSelectionTransfer.getTransfer().isSupportedType(event.dataType)) {
            org.eclipse.jface.util.LocalSelectionTransfer.getTransfer().setSelection(null);
        }
    }
}