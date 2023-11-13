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
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.actions;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.core.ITaskListRunnable;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
/**
 *
 * @author Steffen Pingel
 */
public class AutoUpdateQueryAction extends org.eclipse.ui.actions.BaseSelectionListenerAction {
    public AutoUpdateQueryAction() {
        super(Messages.AutoUpdateQueryAction_Synchronize_Automatically_Label);
        setChecked(false);
        setEnabled(false);
    }

    @java.lang.Override
    protected boolean updateSelection(org.eclipse.jface.viewers.IStructuredSelection selection) {
        if (selection.size() == 1) {
            java.lang.Object element = selection.getFirstElement();
            if (element instanceof org.eclipse.mylyn.internal.tasks.core.RepositoryQuery) {
                setChecked(java.lang.Boolean.valueOf(((org.eclipse.mylyn.internal.tasks.core.RepositoryQuery) (element)).getAutoUpdate()));
                return true;
            }
        }
        setChecked(false);
        return false;
    }

    @java.lang.Override
    public void run() {
        final java.lang.Object element = getStructuredSelection().getFirstElement();
        if (element instanceof org.eclipse.mylyn.internal.tasks.core.RepositoryQuery) {
            try {
                final org.eclipse.mylyn.internal.tasks.core.RepositoryQuery query = ((org.eclipse.mylyn.internal.tasks.core.RepositoryQuery) (element));
                org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().run(new org.eclipse.mylyn.internal.tasks.core.ITaskListRunnable() {
                    public void execute(org.eclipse.core.runtime.IProgressMonitor monitor) throws org.eclipse.core.runtime.CoreException {
                        query.setAutoUpdate(isChecked());
                    }
                });
                org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().notifyElementsChanged(java.util.Collections.singleton(query));
            } catch (org.eclipse.core.runtime.CoreException e) {
                org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Failed to set offline status for query", e));// $NON-NLS-1$

            }
        }
    }
}