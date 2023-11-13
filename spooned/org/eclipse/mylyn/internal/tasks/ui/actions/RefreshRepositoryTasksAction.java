/**
 * *****************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
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
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
/**
 *
 * @author Steffen Pingel
 */
public class RefreshRepositoryTasksAction extends org.eclipse.mylyn.internal.tasks.ui.actions.AbstractTaskRepositoryAction implements org.eclipse.ui.IViewActionDelegate {
    private static final java.lang.String ID = "org.eclipse.mylyn.tasklist.repositories.refreshAllTasks";// $NON-NLS-1$


    public RefreshRepositoryTasksAction() {
        super(Messages.RefreshRepositoryTasksAction_Refresh_All_Tasks);
        setId(org.eclipse.mylyn.internal.tasks.ui.actions.RefreshRepositoryTasksAction.ID);
        setEnabled(false);
    }

    @java.lang.Override
    public void run() {
        org.eclipse.jface.viewers.IStructuredSelection selection = getStructuredSelection();
        for (java.util.Iterator<?> iter = selection.iterator(); iter.hasNext();) {
            java.lang.Object selectedObject = iter.next();
            if (selectedObject instanceof org.eclipse.mylyn.tasks.core.TaskRepository) {
                org.eclipse.mylyn.tasks.core.TaskRepository repository = ((org.eclipse.mylyn.tasks.core.TaskRepository) (selectedObject));
                synchronizeAllTasks(repository);
            }
        }
    }

    private void synchronizeAllTasks(org.eclipse.mylyn.tasks.core.TaskRepository repository) {
        org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(repository.getConnectorKind());
        if (connector != null) {
            java.util.Set<org.eclipse.mylyn.tasks.core.ITask> repositoryTasks = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().getTasks(repository.getRepositoryUrl());
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.synchronizeTasks(connector, repositoryTasks, true, null);
        }
    }

    public void init(org.eclipse.ui.IViewPart view) {
    }

    public void run(org.eclipse.jface.action.IAction action) {
        run();
    }

    public void selectionChanged(org.eclipse.jface.action.IAction action, org.eclipse.jface.viewers.ISelection selection) {
        selectionChanged(((org.eclipse.jface.viewers.IStructuredSelection) (selection)));
    }
}