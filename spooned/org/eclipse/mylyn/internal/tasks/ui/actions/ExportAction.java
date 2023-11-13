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
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.actions;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.ImportExportUtil;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
/**
 *
 * @author Steffen Pingel
 */
public class ExportAction implements org.eclipse.ui.IViewActionDelegate {
    private org.eclipse.jface.viewers.ISelection selection;

    public void init(org.eclipse.ui.IViewPart view) {
        // ignore
    }

    public void run(org.eclipse.jface.action.IAction action) {
        if (selection.isEmpty() || (!(selection instanceof org.eclipse.jface.viewers.StructuredSelection))) {
            org.eclipse.jface.dialogs.MessageDialog.openError(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell(), Messages.ExportAction_Dialog_Title, Messages.ExportAction_Nothing_selected);
            return;
        }
        org.eclipse.swt.widgets.FileDialog dialog = new org.eclipse.swt.widgets.FileDialog(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell(), org.eclipse.swt.SWT.PRIMARY_MODAL | org.eclipse.swt.SWT.SAVE);
        dialog.setText(Messages.ExportAction_Dialog_Title);
        org.eclipse.mylyn.internal.tasks.ui.util.ImportExportUtil.configureFilter(dialog);
        dialog.setFileName(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.EXPORT_FILE_NAME + org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.FILE_EXTENSION);
        java.lang.String path = dialog.open();
        if (path != null) {
            java.io.File file = new java.io.File(path);
            // Prompt the user to confirm if save operation will cause an overwrite
            if (file.exists()) {
                if (!org.eclipse.jface.dialogs.MessageDialog.openConfirm(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell(), Messages.ExportAction_Dialog_Title, org.eclipse.osgi.util.NLS.bind(Messages.ExportAction_X_exists_Do_you_wish_to_overwrite, file.getPath()))) {
                    return;
                }
            }
            try {
                org.eclipse.mylyn.internal.tasks.ui.util.ImportExportUtil.export(file, ((org.eclipse.jface.viewers.IStructuredSelection) (selection)));
            } catch (org.eclipse.core.runtime.CoreException e) {
                org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Problems encountered during export", e));// $NON-NLS-1$

                org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayStatus(Messages.ExportAction_Dialog_Title, new org.eclipse.core.runtime.MultiStatus(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.ID_PLUGIN, 0, new org.eclipse.core.runtime.IStatus[]{ e.getStatus() }, Messages.ExportAction_Problems_encountered, e));
            }
        }
    }

    public void selectionChanged(org.eclipse.jface.action.IAction action, org.eclipse.jface.viewers.ISelection selection) {
        this.selection = selection;
    }
}