/**
 * *****************************************************************************
 * Copyright (c) 2009, 2013 Tasktop Technologies and others.
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
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.views.PresentationFilter;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskContainer;
import org.eclipse.ui.operations.RedoActionHandler;
import org.eclipse.ui.operations.UndoActionHandler;
import org.eclipse.ui.part.DrillDownAdapter;
/**
 *
 * @author Steffen Pingel
 */
public class TaskListViewActionGroup extends org.eclipse.mylyn.internal.tasks.ui.actions.RepositoryElementActionGroup {
    private final org.eclipse.mylyn.internal.tasks.ui.actions.RenameAction renameAction;

    private final org.eclipse.mylyn.internal.tasks.ui.views.TaskListView view;

    private final org.eclipse.mylyn.internal.tasks.ui.actions.GoIntoAction goIntoAction;

    private final org.eclipse.mylyn.internal.tasks.ui.actions.GoUpAction goUpAction;

    private final org.eclipse.ui.part.DrillDownAdapter drillDownAdapter;

    private final org.eclipse.mylyn.internal.tasks.ui.actions.HideQueryAction hideQueryAction;

    private final org.eclipse.jface.action.IAction undoAction;

    private final org.eclipse.jface.action.IAction redoAction;

    public TaskListViewActionGroup(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView view, org.eclipse.ui.part.DrillDownAdapter drillDownAdapter) {
        this.view = view;
        this.drillDownAdapter = drillDownAdapter;
        goIntoAction = new org.eclipse.mylyn.internal.tasks.ui.actions.GoIntoAction();
        goUpAction = new org.eclipse.mylyn.internal.tasks.ui.actions.GoUpAction(drillDownAdapter);
        renameAction = add(new org.eclipse.mylyn.internal.tasks.ui.actions.RenameAction(view));
        hideQueryAction = add(new org.eclipse.mylyn.internal.tasks.ui.actions.HideQueryAction());
        org.eclipse.core.commands.operations.IUndoContext undoContext = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getUndoContext();
        undoAction = new org.eclipse.ui.operations.UndoActionHandler(view.getSite(), undoContext);
        redoAction = new org.eclipse.ui.operations.RedoActionHandler(view.getSite(), undoContext);
        setSelectionProvider(view.getViewer());
    }

    public void dispose() {
        setSelectionProvider(null);
    }

    public void updateDrillDownActions() {
        if (drillDownAdapter.canGoBack()) {
            goUpAction.setEnabled(true);
        } else {
            goUpAction.setEnabled(false);
        }
    }

    @java.lang.Override
    public void fillContextMenu(final org.eclipse.jface.action.IMenuManager manager) {
        super.fillContextMenu(manager);
        if (hideQueryAction.isEnabled() && (!org.eclipse.mylyn.internal.tasks.ui.views.PresentationFilter.getInstance().isFilterHiddenQueries())) {
            manager.appendToGroup(org.eclipse.mylyn.internal.tasks.ui.actions.ID_SEPARATOR_REPOSITORY, new org.eclipse.jface.action.Separator());
            manager.appendToGroup(org.eclipse.mylyn.internal.tasks.ui.actions.ID_SEPARATOR_REPOSITORY, hideQueryAction);
        }
        updateDrillDownActions();
        java.lang.Object element = ((org.eclipse.jface.viewers.IStructuredSelection) (view.getViewer().getSelection())).getFirstElement();
        if ((element instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) && (!(element instanceof org.eclipse.mylyn.tasks.core.ITask))) {
            org.eclipse.mylyn.tasks.core.ITaskContainer cat = ((org.eclipse.mylyn.tasks.core.ITaskContainer) (element));
            if (cat.getChildren().size() > 0) {
                goIntoAction.setEnabled(true);
            } else {
                goIntoAction.setEnabled(false);
            }
        } else {
            goIntoAction.setEnabled(false);
        }
        if (goIntoAction.isEnabled()) {
            manager.appendToGroup(org.eclipse.mylyn.internal.tasks.ui.actions.ID_SEPARATOR_NAVIGATE, goIntoAction);
        }
        if (goUpAction.isEnabled()) {
            manager.appendToGroup(org.eclipse.mylyn.internal.tasks.ui.actions.ID_SEPARATOR_NAVIGATE, goUpAction);
        }
        if (((!(element instanceof org.eclipse.mylyn.tasks.core.ITask)) && renameAction.isEnabled()) && (element != null)) {
            manager.appendToGroup(org.eclipse.mylyn.internal.tasks.ui.actions.ID_SEPARATOR_EDIT, renameAction);
        }
    }

    public org.eclipse.mylyn.internal.tasks.ui.actions.GoUpAction getGoUpAction() {
        return goUpAction;
    }

    public org.eclipse.mylyn.internal.tasks.ui.actions.GoIntoAction getGoIntoAction() {
        return goIntoAction;
    }

    public org.eclipse.mylyn.internal.tasks.ui.actions.RenameAction getRenameAction() {
        return renameAction;
    }

    public org.eclipse.jface.action.IAction getUndoAction() {
        return undoAction;
    }

    public org.eclipse.jface.action.IAction getRedoAction() {
        return redoAction;
    }
}