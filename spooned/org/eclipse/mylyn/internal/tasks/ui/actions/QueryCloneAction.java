/**
 * *****************************************************************************
 * Copyright (c) 2004, 2010 Jevgeni Holodkov and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jevgeni Holodkov - initial API and implementation
 *     Tasktop Technologies - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.actions;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
/**
 * Allow to clone a selected query.
 *
 * @author Jevgeni Holodkov
 */
public class QueryCloneAction extends org.eclipse.jface.action.Action implements org.eclipse.ui.IViewActionDelegate {
    protected org.eclipse.jface.viewers.ISelection selection;

    public void init(org.eclipse.ui.IViewPart view) {
        // ignore
    }

    public void run(org.eclipse.jface.action.IAction action) {
        run(getSelectedQuery(selection));
    }

    public void selectionChanged(org.eclipse.jface.action.IAction action, org.eclipse.jface.viewers.ISelection selection) {
        this.selection = selection;
        org.eclipse.mylyn.tasks.core.IRepositoryQuery selectedQuery = getSelectedQuery(selection);
        action.setEnabled(true);
        if (selectedQuery != null) {
            action.setEnabled(true);
        } else {
            action.setEnabled(false);
        }
    }

    protected org.eclipse.mylyn.internal.tasks.core.RepositoryQuery getSelectedQuery(org.eclipse.jface.viewers.ISelection newSelection) {
        if (selection instanceof org.eclipse.jface.viewers.StructuredSelection) {
            // allow to select only one element
            if (((org.eclipse.jface.viewers.StructuredSelection) (selection)).size() == 1) {
                java.lang.Object selectedObject = ((org.eclipse.jface.viewers.StructuredSelection) (selection)).getFirstElement();
                if (selectedObject instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery) {
                    return ((org.eclipse.mylyn.internal.tasks.core.RepositoryQuery) (selectedObject));
                }
            }
        }
        return null;
    }

    public void run(org.eclipse.mylyn.internal.tasks.core.RepositoryQuery selectedQuery) {
        if (selectedQuery == null) {
            org.eclipse.jface.dialogs.MessageDialog.openInformation(org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages.QueryCloneAction_Clone_Query, Messages.QueryCloneAction_No_query_selected);
            return;
        }
        java.util.List<org.eclipse.mylyn.internal.tasks.core.RepositoryQuery> queries = new java.util.ArrayList<org.eclipse.mylyn.internal.tasks.core.RepositoryQuery>();
        queries.add(selectedQuery);
        java.util.List<org.eclipse.mylyn.internal.tasks.core.RepositoryQuery> clonedQueries = new java.util.ArrayList<org.eclipse.mylyn.internal.tasks.core.RepositoryQuery>(queries.size());
        for (org.eclipse.mylyn.internal.tasks.core.RepositoryQuery query : queries) {
            org.eclipse.mylyn.tasks.core.TaskRepository repository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(query.getConnectorKind(), query.getRepositoryUrl());
            org.eclipse.mylyn.internal.tasks.core.RepositoryQuery clonedQuery = ((org.eclipse.mylyn.internal.tasks.core.RepositoryQuery) (org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryModel().createRepositoryQuery(repository)));
            clonedQuery.setSummary(org.eclipse.osgi.util.NLS.bind(Messages.QueryCloneAction_Copy_of_X, query.getSummary()));
            clonedQuery.setUrl(query.getUrl());
            java.util.Map<java.lang.String, java.lang.String> attributes = query.getAttributes();
            for (java.util.Map.Entry<java.lang.String, java.lang.String> entry : attributes.entrySet()) {
                clonedQuery.setAttribute(entry.getKey(), entry.getValue());
            }
            clonedQueries.add(clonedQuery);
        }
        for (org.eclipse.mylyn.internal.tasks.core.RepositoryQuery query : clonedQueries) {
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().addQuery(query);
            org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnectorUi(query.getConnectorKind());
            if (!org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.openEditQueryDialog(connectorUi, query)) {
                org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().deleteQuery(query);
            }
        }
    }
}