/**
 * *****************************************************************************
 * Copyright (c) 2004, 2010 Tasktop Technologies and others.
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
import org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
/**
 *
 * @author Steffen Pingel
 */
public class ShowInSearchViewAction extends org.eclipse.ui.actions.BaseSelectionListenerAction {
    public static final java.lang.String ID = "org.eclipse.mylyn.tasklist.actions.open.browser";// $NON-NLS-1$


    public ShowInSearchViewAction() {
        super(Messages.ShowInSearchViewAction_Open_in_Search_Label);
        setId(org.eclipse.mylyn.internal.tasks.ui.actions.ShowInSearchViewAction.ID);
    }

    @java.lang.Override
    public void run() {
        if (super.getStructuredSelection() != null) {
            for (java.util.Iterator<?> iter = super.getStructuredSelection().iterator(); iter.hasNext();) {
                runWithSelection(iter.next());
            }
        }
    }

    private void runWithSelection(java.lang.Object selectedObject) {
        if (selectedObject instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery) {
            org.eclipse.mylyn.tasks.core.IRepositoryQuery query = ((org.eclipse.mylyn.tasks.core.IRepositoryQuery) (selectedObject));
            org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(query.getConnectorKind());
            org.eclipse.mylyn.tasks.core.TaskRepository taskRepository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(query.getConnectorKind(), query.getRepositoryUrl());
            if (connector != null) {
                org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil.runSearchQuery(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList(), taskRepository, query);
            }
        }
    }

    @java.lang.Override
    protected boolean updateSelection(org.eclipse.jface.viewers.IStructuredSelection selection) {
        return (org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil.supportsTaskSearch() && (selection.size() == 1)) && (selection.getFirstElement() instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery);
    }
}