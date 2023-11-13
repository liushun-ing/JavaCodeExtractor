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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.core.ITaskListRunnable;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
/**
 *
 * @author Steffen Pingel
 */
public class HideQueryAction extends org.eclipse.ui.actions.BaseSelectionListenerAction {
    public HideQueryAction() {
        super(Messages.HideQueryAction_Hidden_Label);
        setChecked(false);
        setEnabled(false);
    }

    @java.lang.Override
    protected boolean updateSelection(org.eclipse.jface.viewers.IStructuredSelection selection) {
        if (selection.isEmpty()) {
            return false;
        }
        boolean hidden = true;
        for (java.lang.Object element : selection.toList()) {
            if (element instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery) {
                hidden &= java.lang.Boolean.parseBoolean(((org.eclipse.mylyn.tasks.core.IRepositoryQuery) (element)).getAttribute(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.ATTRIBUTE_HIDDEN));
            } else {
                return false;
            }
        }
        setChecked(hidden);
        return true;
    }

    @java.lang.Override
    public void run() {
        for (java.lang.Object element : getStructuredSelection().toList()) {
            if (element instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery) {
                try {
                    final org.eclipse.mylyn.tasks.core.IRepositoryQuery query = ((org.eclipse.mylyn.tasks.core.IRepositoryQuery) (element));
                    org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().run(new org.eclipse.mylyn.internal.tasks.core.ITaskListRunnable() {
                        public void execute(org.eclipse.core.runtime.IProgressMonitor monitor) throws org.eclipse.core.runtime.CoreException {
                            query.setAttribute(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.ATTRIBUTE_HIDDEN, java.lang.Boolean.toString(isChecked()));
                        }
                    });
                    org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().notifyElementsChanged(java.util.Collections.singleton(((org.eclipse.mylyn.tasks.core.IRepositoryElement) (query))));
                } catch (org.eclipse.core.runtime.CoreException e) {
                    org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Failed to set hidden status for query", e));// $NON-NLS-1$

                }
            }
        }
    }
}