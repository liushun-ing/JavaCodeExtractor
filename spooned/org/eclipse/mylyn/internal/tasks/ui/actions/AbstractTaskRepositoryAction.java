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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
/**
 *
 * @author Shawn Minto
 * @author Steffen Pingel
 */
public abstract class AbstractTaskRepositoryAction extends org.eclipse.ui.actions.BaseSelectionListenerAction {
    private boolean singleSelect;

    public AbstractTaskRepositoryAction(java.lang.String text) {
        super(text);
    }

    @java.lang.Override
    protected boolean updateSelection(org.eclipse.jface.viewers.IStructuredSelection selection) {
        if ((selection != null) && (!selection.isEmpty())) {
            if (singleSelect) {
                java.lang.Object[] array = selection.toArray();
                if (array.length != 1) {
                    return false;
                }
            }
            return getTaskRepository(selection.getFirstElement()) != null;
        }
        return false;
    }

    protected org.eclipse.mylyn.tasks.core.TaskRepository getTaskRepository(org.eclipse.jface.viewers.IStructuredSelection selection) {
        if ((selection != null) && (!selection.isEmpty())) {
            return getTaskRepository(selection.getFirstElement());
        }
        return null;
    }

    protected org.eclipse.mylyn.tasks.core.TaskRepository getTaskRepository(java.lang.Object selectedObject) {
        org.eclipse.mylyn.tasks.core.TaskRepository taskRepository = null;
        if (selectedObject instanceof org.eclipse.mylyn.tasks.core.TaskRepository) {
            taskRepository = ((org.eclipse.mylyn.tasks.core.TaskRepository) (selectedObject));
        } else if (selectedObject instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery) {
            org.eclipse.mylyn.tasks.core.IRepositoryQuery query = ((org.eclipse.mylyn.tasks.core.IRepositoryQuery) (selectedObject));
            taskRepository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(query.getConnectorKind(), query.getRepositoryUrl());
        }
        if ((taskRepository != null) && isUserManaged(taskRepository)) {
            return taskRepository;
        }
        return null;
    }

    protected boolean isUserManaged(org.eclipse.mylyn.tasks.core.TaskRepository taskRepository) {
        org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(taskRepository.getConnectorKind());
        return (connector != null) && connector.isUserManaged();
    }

    protected boolean isSingleSelect() {
        return singleSelect;
    }

    protected void setSingleSelect(boolean singleSelect) {
        this.singleSelect = singleSelect;
    }
}