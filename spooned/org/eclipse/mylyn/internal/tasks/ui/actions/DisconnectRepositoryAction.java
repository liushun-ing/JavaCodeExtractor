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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryDelta;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryDelta.Type;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.views.Messages;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
/**
 *
 * @author Steffen Pingel
 */
public class DisconnectRepositoryAction extends org.eclipse.jface.action.Action implements org.eclipse.jface.viewers.ISelectionChangedListener {
    public static final java.lang.String LABEL = org.eclipse.mylyn.internal.tasks.ui.views.Messages.DisconnectRepositoryAction_Disconnected;

    private static final java.lang.String ID = "org.eclipse.mylyn.tasklist.repositories.offline";// $NON-NLS-1$


    private org.eclipse.mylyn.tasks.core.TaskRepository repository;

    public DisconnectRepositoryAction() {
        super(org.eclipse.mylyn.internal.tasks.ui.actions.DisconnectRepositoryAction.LABEL, org.eclipse.jface.action.IAction.AS_CHECK_BOX);
        setId(org.eclipse.mylyn.internal.tasks.ui.actions.DisconnectRepositoryAction.ID);
        setEnabled(false);
    }

    @java.lang.Override
    public void run() {
        repository.setOffline(isChecked());
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryManager().notifyRepositorySettingsChanged(repository, new org.eclipse.mylyn.internal.tasks.core.TaskRepositoryDelta(org.eclipse.mylyn.internal.tasks.core.TaskRepositoryDelta.Type.OFFLINE));
    }

    @java.lang.Deprecated
    public void selectionChanged(org.eclipse.jface.action.IAction action, org.eclipse.jface.viewers.ISelection selection) {
    }

    public void selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent event) {
        org.eclipse.jface.viewers.ISelection selection = event.getSelection();
        if ((selection instanceof org.eclipse.jface.viewers.IStructuredSelection) && (((org.eclipse.jface.viewers.IStructuredSelection) (selection)).size() == 1)) {
            java.lang.Object selectedObject = ((org.eclipse.jface.viewers.IStructuredSelection) (selection)).getFirstElement();
            if (selectedObject instanceof org.eclipse.mylyn.tasks.core.TaskRepository) {
                org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(((org.eclipse.mylyn.tasks.core.TaskRepository) (selectedObject)).getConnectorKind());
                if (connector.isUserManaged()) {
                    this.repository = ((org.eclipse.mylyn.tasks.core.TaskRepository) (selectedObject));
                    setChecked(this.repository.isOffline());
                    setEnabled(true);
                    return;
                }
            }
        }
        this.repository = null;
        setChecked(false);
        setEnabled(false);
    }
}