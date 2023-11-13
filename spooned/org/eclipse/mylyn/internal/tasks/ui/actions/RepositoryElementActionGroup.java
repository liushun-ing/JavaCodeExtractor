/**
 * *****************************************************************************
 * Copyright (c) 2009, 2015 Tasktop Technologies and others.
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
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory;
import org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector;
import org.eclipse.mylyn.internal.tasks.core.TaskCategory;
import org.eclipse.mylyn.internal.tasks.core.UncategorizedTaskContainer;
import org.eclipse.mylyn.internal.tasks.ui.IDynamicSubMenuContributor;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.actions.CopyTaskDetailsAction.Mode;
import org.eclipse.mylyn.internal.tasks.ui.views.Messages;
import org.eclipse.mylyn.internal.tasks.ui.views.UpdateRepositoryConfigurationAction;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskContainer;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;
/**
 *
 * @author Steffen Pingel
 */
public class RepositoryElementActionGroup {
    protected static final java.lang.String ID_SEPARATOR_NEW = "new";// $NON-NLS-1$


    private static final java.lang.String ID_SEPARATOR_OPERATIONS = "operations";// $NON-NLS-1$


    private static final java.lang.String ID_SEPARATOR_TASKS = "tasks";// $NON-NLS-1$


    protected static final java.lang.String ID_SEPARATOR_REPOSITORY = "repository";// $NON-NLS-1$


    private static final java.lang.String ID_SEPARATOR_PROPERTIES = "properties";// $NON-NLS-1$


    protected static final java.lang.String ID_SEPARATOR_NAVIGATE = "navigate";// $NON-NLS-1$


    private static final java.lang.String ID_SEPARATOR_OPEN = "open";// $NON-NLS-1$


    private static final java.lang.String ID_SEPARATOR_SHOW_IN = "showIn";// $NON-NLS-1$


    protected static final java.lang.String ID_SEPARATOR_EDIT = "edit";// $NON-NLS-1$


    private final org.eclipse.mylyn.internal.tasks.ui.actions.CopyTaskDetailsAction copyUrlAction;

    private final org.eclipse.mylyn.internal.tasks.ui.actions.CopyTaskDetailsAction copyKeyAction;

    private final org.eclipse.mylyn.internal.tasks.ui.actions.CopyTaskDetailsAction copyKeySummaryAction;

    private final org.eclipse.mylyn.internal.tasks.ui.actions.CopyTaskDetailsAction copyKeySummaryURLAction;

    private final org.eclipse.mylyn.internal.tasks.ui.actions.OpenTaskListElementAction openAction;

    private final org.eclipse.mylyn.internal.tasks.ui.actions.OpenWithBrowserAction openWithBrowserAction;

    private final org.eclipse.mylyn.internal.tasks.ui.actions.DeleteAction deleteAction;

    private final org.eclipse.mylyn.internal.tasks.ui.actions.DeleteTaskEditorAction deleteTaskEditorAction;

    private final org.eclipse.mylyn.internal.tasks.ui.actions.RemoveFromCategoryAction removeFromCategoryAction;

    private final org.eclipse.mylyn.internal.tasks.ui.actions.ShowInSearchViewAction showInSearchViewAction;

    private final org.eclipse.mylyn.internal.tasks.ui.actions.ShowInTaskListAction showInTaskListAction;

    private final org.eclipse.mylyn.internal.tasks.ui.actions.TaskActivateAction activateAction;

    private final org.eclipse.mylyn.internal.tasks.ui.actions.TaskDeactivateAction deactivateAction;

    private org.eclipse.jface.viewers.ISelectionProvider selectionProvider;

    private final java.util.List<org.eclipse.jface.viewers.ISelectionChangedListener> actions;

    private final org.eclipse.mylyn.internal.tasks.ui.actions.AutoUpdateQueryAction autoUpdateAction;

    private final org.eclipse.mylyn.internal.tasks.ui.actions.NewSubTaskAction newSubTaskAction;

    private final org.eclipse.mylyn.internal.tasks.ui.actions.CloneTaskAction cloneTaskAction;

    public RepositoryElementActionGroup() {
        actions = new java.util.ArrayList<org.eclipse.jface.viewers.ISelectionChangedListener>();
        newSubTaskAction = add(new org.eclipse.mylyn.internal.tasks.ui.actions.NewSubTaskAction());
        cloneTaskAction = add(new org.eclipse.mylyn.internal.tasks.ui.actions.CloneTaskAction());
        activateAction = add(new org.eclipse.mylyn.internal.tasks.ui.actions.TaskActivateAction());
        deactivateAction = new org.eclipse.mylyn.internal.tasks.ui.actions.TaskDeactivateAction();
        copyKeyAction = add(new org.eclipse.mylyn.internal.tasks.ui.actions.CopyTaskDetailsAction(org.eclipse.mylyn.internal.tasks.ui.actions.CopyTaskDetailsAction.Mode.KEY));
        copyUrlAction = add(new org.eclipse.mylyn.internal.tasks.ui.actions.CopyTaskDetailsAction(org.eclipse.mylyn.internal.tasks.ui.actions.CopyTaskDetailsAction.Mode.URL));
        copyKeySummaryAction = add(new org.eclipse.mylyn.internal.tasks.ui.actions.CopyTaskDetailsAction(org.eclipse.mylyn.internal.tasks.ui.actions.CopyTaskDetailsAction.Mode.ID_SUMMARY));
        copyKeySummaryURLAction = add(new org.eclipse.mylyn.internal.tasks.ui.actions.CopyTaskDetailsAction(org.eclipse.mylyn.internal.tasks.ui.actions.CopyTaskDetailsAction.Mode.ID_SUMMARY_URL));
        if (!isInEditor()) {
            copyKeySummaryURLAction.setActionDefinitionId(org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds.COPY);
        }
        removeFromCategoryAction = add(new org.eclipse.mylyn.internal.tasks.ui.actions.RemoveFromCategoryAction());
        deleteAction = add(new org.eclipse.mylyn.internal.tasks.ui.actions.DeleteAction());
        deleteTaskEditorAction = add(new org.eclipse.mylyn.internal.tasks.ui.actions.DeleteTaskEditorAction());
        openAction = add(new org.eclipse.mylyn.internal.tasks.ui.actions.OpenTaskListElementAction());
        openWithBrowserAction = add(new org.eclipse.mylyn.internal.tasks.ui.actions.OpenWithBrowserAction());
        showInSearchViewAction = add(new org.eclipse.mylyn.internal.tasks.ui.actions.ShowInSearchViewAction());
        showInTaskListAction = add(new org.eclipse.mylyn.internal.tasks.ui.actions.ShowInTaskListAction());
        autoUpdateAction = add(new org.eclipse.mylyn.internal.tasks.ui.actions.AutoUpdateQueryAction());
    }

    protected <T extends org.eclipse.jface.viewers.ISelectionChangedListener> T add(T action) {
        actions.add(action);
        return action;
    }

    public void setSelectionProvider(org.eclipse.jface.viewers.ISelectionProvider selectionProvider) {
        if (this.selectionProvider != null) {
            for (org.eclipse.jface.viewers.ISelectionChangedListener action : actions) {
                this.selectionProvider.removeSelectionChangedListener(action);
            }
        }
        this.selectionProvider = selectionProvider;
        if (selectionProvider != null) {
            for (org.eclipse.jface.viewers.ISelectionChangedListener action : actions) {
                this.selectionProvider.addSelectionChangedListener(action);
                org.eclipse.jface.viewers.ISelection selection = selectionProvider.getSelection();
                if (selection == null) {
                    selection = org.eclipse.jface.viewers.StructuredSelection.EMPTY;
                }
                action.selectionChanged(new org.eclipse.jface.viewers.SelectionChangedEvent(selectionProvider, selection));
            }
        }
    }

    public void fillContextMenu(final org.eclipse.jface.action.IMenuManager manager) {
        manager.add(new org.eclipse.jface.action.Separator(org.eclipse.mylyn.internal.tasks.ui.actions.RepositoryElementActionGroup.ID_SEPARATOR_NEW));// new, schedule

        manager.add(new org.eclipse.jface.action.GroupMarker(org.eclipse.mylyn.internal.tasks.ui.actions.RepositoryElementActionGroup.ID_SEPARATOR_NAVIGATE));// mark, go into, go up

        manager.add(new org.eclipse.jface.action.Separator(org.eclipse.mylyn.internal.tasks.ui.actions.RepositoryElementActionGroup.ID_SEPARATOR_OPEN));// open, activate

        manager.add(new org.eclipse.jface.action.GroupMarker(org.eclipse.mylyn.internal.tasks.ui.actions.RepositoryElementActionGroup.ID_SEPARATOR_SHOW_IN));// open, activate

        manager.add(new org.eclipse.jface.action.Separator(org.eclipse.mylyn.internal.tasks.ui.actions.RepositoryElementActionGroup.ID_SEPARATOR_EDIT));// cut, copy paste, delete, rename

        manager.add(new org.eclipse.jface.action.Separator(org.eclipse.mylyn.internal.tasks.ui.actions.RepositoryElementActionGroup.ID_SEPARATOR_TASKS));// move to

        manager.add(new org.eclipse.jface.action.GroupMarker(org.eclipse.mylyn.internal.tasks.ui.actions.RepositoryElementActionGroup.ID_SEPARATOR_OPERATIONS));// repository properties, import/export, context

        manager.add(new org.eclipse.jface.action.Separator(org.eclipse.mylyn.internal.tasks.ui.actions.RepositoryElementActionGroup.ID_SEPARATOR_REPOSITORY));// synchronize

        manager.add(new org.eclipse.jface.action.Separator(org.eclipse.ui.IWorkbenchActionConstants.MB_ADDITIONS));
        manager.add(new org.eclipse.jface.action.Separator(org.eclipse.mylyn.internal.tasks.ui.actions.RepositoryElementActionGroup.ID_SEPARATOR_PROPERTIES));// properties

        final org.eclipse.mylyn.tasks.core.ITaskContainer element;
        org.eclipse.jface.viewers.IStructuredSelection selection = getSelection();
        final java.lang.Object firstSelectedObject = selection.getFirstElement();
        if (firstSelectedObject instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) {
            element = ((org.eclipse.mylyn.tasks.core.ITaskContainer) (firstSelectedObject));
        } else {
            element = null;
        }
        final java.util.List<org.eclipse.mylyn.tasks.core.IRepositoryElement> selectedElements = getSelectedTaskContainers(selection);
        org.eclipse.mylyn.internal.tasks.core.AbstractTask task = null;
        if (element instanceof org.eclipse.mylyn.tasks.core.ITask) {
            task = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (element));
        }
        if (!isInTaskList()) {
            org.eclipse.jface.action.MenuManager newSubMenu = new org.eclipse.jface.action.MenuManager(org.eclipse.mylyn.internal.tasks.ui.views.Messages.RepositoryElementActionGroup_New);
            if (newSubTaskAction.isEnabled()) {
                newSubMenu.add(newSubTaskAction);
            }
            if (cloneTaskAction.isEnabled()) {
                newSubMenu.add(new org.eclipse.jface.action.Separator());
                newSubMenu.add(cloneTaskAction);
            }
            manager.appendToGroup(org.eclipse.mylyn.internal.tasks.ui.actions.RepositoryElementActionGroup.ID_SEPARATOR_NEW, newSubMenu);
        }
        if ((element instanceof org.eclipse.mylyn.tasks.core.ITask) && (!isInEditor())) {
            addAction(org.eclipse.mylyn.internal.tasks.ui.actions.RepositoryElementActionGroup.ID_SEPARATOR_OPEN, openAction, manager, element);
        }
        if (openWithBrowserAction.isEnabled()) {
            manager.appendToGroup(org.eclipse.mylyn.internal.tasks.ui.actions.RepositoryElementActionGroup.ID_SEPARATOR_OPEN, openWithBrowserAction);
        }
        showInSearchViewAction.selectionChanged(selection);
        if (showInSearchViewAction.isEnabled()) {
            manager.appendToGroup(org.eclipse.mylyn.internal.tasks.ui.actions.RepositoryElementActionGroup.ID_SEPARATOR_OPEN, showInSearchViewAction);
        }
        showInTaskListAction.selectionChanged(selection);
        if (showInTaskListAction.isEnabled() && (!isInTaskList())) {
            manager.appendToGroup(org.eclipse.mylyn.internal.tasks.ui.actions.RepositoryElementActionGroup.ID_SEPARATOR_OPEN, showInTaskListAction);
        }
        if (task != null) {
            if (task.isActive()) {
                manager.appendToGroup(org.eclipse.mylyn.internal.tasks.ui.actions.RepositoryElementActionGroup.ID_SEPARATOR_SHOW_IN, deactivateAction);
            } else {
                manager.appendToGroup(org.eclipse.mylyn.internal.tasks.ui.actions.RepositoryElementActionGroup.ID_SEPARATOR_SHOW_IN, activateAction);
            }
        }
        if (!selection.isEmpty()) {
            org.eclipse.jface.action.MenuManager copyDetailsSubMenu = new org.eclipse.jface.action.MenuManager(org.eclipse.mylyn.internal.tasks.ui.views.Messages.RepositoryElementActionGroup_Copy_Detail_Menu_Label, CopyTaskDetailsAction.ID);
            copyDetailsSubMenu.add(copyKeyAction);
            copyDetailsSubMenu.add(copyUrlAction);
            copyDetailsSubMenu.add(copyKeySummaryAction);
            copyDetailsSubMenu.add(copyKeySummaryURLAction);
            manager.appendToGroup(org.eclipse.mylyn.internal.tasks.ui.actions.RepositoryElementActionGroup.ID_SEPARATOR_EDIT, copyDetailsSubMenu);
        }
        if (isInTaskList() && (!selection.isEmpty())) {
            manager.appendToGroup(org.eclipse.mylyn.internal.tasks.ui.actions.RepositoryElementActionGroup.ID_SEPARATOR_EDIT, deleteAction);
        }
        if (isInEditor()) {
            manager.appendToGroup(org.eclipse.mylyn.internal.tasks.ui.actions.RepositoryElementActionGroup.ID_SEPARATOR_TASKS, deleteTaskEditorAction);
        }
        removeFromCategoryAction.selectionChanged(selection);
        removeFromCategoryAction.setEnabled(isRemoveFromCategoryEnabled(selectedElements));
        if (removeFromCategoryAction.isEnabled()) {
            manager.appendToGroup(org.eclipse.mylyn.internal.tasks.ui.actions.RepositoryElementActionGroup.ID_SEPARATOR_EDIT, removeFromCategoryAction);
        }
        if (autoUpdateAction.isEnabled()) {
            manager.appendToGroup(org.eclipse.mylyn.internal.tasks.ui.actions.RepositoryElementActionGroup.ID_SEPARATOR_REPOSITORY, autoUpdateAction);
        }
        if (element instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery) {
            org.eclipse.mylyn.internal.tasks.ui.actions.EditRepositoryPropertiesAction repositoryPropertiesAction = new org.eclipse.mylyn.internal.tasks.ui.actions.EditRepositoryPropertiesAction();
            repositoryPropertiesAction.selectionChanged(new org.eclipse.jface.viewers.StructuredSelection(element));
            if (repositoryPropertiesAction.isEnabled()) {
                org.eclipse.jface.action.MenuManager subMenu = new org.eclipse.jface.action.MenuManager(org.eclipse.mylyn.internal.tasks.ui.views.Messages.TaskListView_Repository);
                manager.appendToGroup(org.eclipse.mylyn.internal.tasks.ui.actions.RepositoryElementActionGroup.ID_SEPARATOR_OPERATIONS, subMenu);
                org.eclipse.mylyn.internal.tasks.ui.views.UpdateRepositoryConfigurationAction resetRepositoryConfigurationAction = new org.eclipse.mylyn.internal.tasks.ui.views.UpdateRepositoryConfigurationAction();
                resetRepositoryConfigurationAction.selectionChanged(new org.eclipse.jface.viewers.StructuredSelection(element));
                subMenu.add(resetRepositoryConfigurationAction);
                subMenu.add(new org.eclipse.jface.action.Separator());
                subMenu.add(repositoryPropertiesAction);
            }
        }
        java.util.Map<java.lang.String, java.util.List<org.eclipse.mylyn.internal.tasks.ui.IDynamicSubMenuContributor>> dynamicMenuMap = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getDynamicMenuMap();
        for (final java.lang.String menuPath : dynamicMenuMap.keySet()) {
            for (final org.eclipse.mylyn.internal.tasks.ui.IDynamicSubMenuContributor contributor : dynamicMenuMap.get(menuPath)) {
                org.eclipse.jface.util.SafeRunnable.run(new org.eclipse.core.runtime.ISafeRunnable() {
                    public void handleException(java.lang.Throwable e) {
                        org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Menu contributor failed"));// $NON-NLS-1$

                    }

                    public void run() throws java.lang.Exception {
                        org.eclipse.jface.action.MenuManager subMenuManager = contributor.getSubMenuManager(selectedElements);
                        if (subMenuManager != null) {
                            addMenuManager(menuPath, subMenuManager, manager, element);
                        }
                    }
                });
            }
        }
    }

    private boolean isInTaskList() {
        return this instanceof org.eclipse.mylyn.internal.tasks.ui.actions.TaskListViewActionGroup;
    }

    private org.eclipse.jface.viewers.IStructuredSelection getSelection() {
        org.eclipse.jface.viewers.ISelection selection = (selectionProvider != null) ? selectionProvider.getSelection() : null;
        if (selection instanceof org.eclipse.jface.viewers.IStructuredSelection) {
            return ((org.eclipse.jface.viewers.IStructuredSelection) (selection));
        }
        return org.eclipse.jface.viewers.StructuredSelection.EMPTY;
    }

    private boolean isInEditor() {
        return this instanceof org.eclipse.mylyn.internal.tasks.ui.actions.TaskEditorActionGroup;
    }

    private boolean isRemoveFromCategoryEnabled(final java.util.List<org.eclipse.mylyn.tasks.core.IRepositoryElement> selectedElements) {
        if (selectedElements.isEmpty()) {
            return false;
        }
        for (org.eclipse.mylyn.tasks.core.IRepositoryElement element : selectedElements) {
            if (element instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask) {
                boolean hasCategory = false;
                for (org.eclipse.mylyn.tasks.core.ITaskContainer container : ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (element)).getParentContainers()) {
                    if (container instanceof org.eclipse.mylyn.internal.tasks.core.TaskCategory) {
                        hasCategory = true;
                    }
                    if ((container instanceof org.eclipse.mylyn.internal.tasks.core.UncategorizedTaskContainer) && (!org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector.CONNECTOR_KIND.equals(((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (element)).getConnectorKind()))) {
                        hasCategory = true;
                    }
                }
                if (!hasCategory) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private void addMenuManager(java.lang.String path, org.eclipse.jface.action.IMenuManager menuToAdd, org.eclipse.jface.action.IMenuManager manager, org.eclipse.mylyn.tasks.core.ITaskContainer element) {
        if ((element instanceof org.eclipse.mylyn.tasks.core.ITask) || (element instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery)) {
            manager.appendToGroup(path, menuToAdd);
        }
    }

    private void addAction(java.lang.String path, org.eclipse.jface.action.Action action, org.eclipse.jface.action.IMenuManager manager, org.eclipse.mylyn.tasks.core.ITaskContainer element) {
        action.setEnabled(false);
        if (element != null) {
            updateActionEnablement(action, element);
        }
        manager.appendToGroup(path, action);
    }

    // TODO move the enablement to the action classes
    private void updateActionEnablement(org.eclipse.jface.action.Action action, org.eclipse.mylyn.tasks.core.ITaskContainer element) {
        if (element instanceof org.eclipse.mylyn.tasks.core.ITask) {
            if (action instanceof org.eclipse.mylyn.internal.tasks.ui.actions.OpenTaskListElementAction) {
                action.setEnabled(true);
            } else if (action instanceof org.eclipse.mylyn.internal.tasks.ui.actions.CopyTaskDetailsAction) {
                action.setEnabled(true);
            } else if (action instanceof org.eclipse.mylyn.internal.tasks.ui.actions.RenameAction) {
                action.setEnabled(true);
            }
        } else if (element != null) {
            if (action instanceof org.eclipse.mylyn.internal.tasks.ui.actions.GoIntoAction) {
                org.eclipse.mylyn.internal.tasks.core.TaskCategory cat = ((org.eclipse.mylyn.internal.tasks.core.TaskCategory) (element));
                if (cat.getChildren().size() > 0) {
                    action.setEnabled(true);
                } else {
                    action.setEnabled(false);
                }
            } else if (action instanceof org.eclipse.mylyn.internal.tasks.ui.actions.OpenTaskListElementAction) {
                action.setEnabled(true);
            } else if (action instanceof org.eclipse.mylyn.internal.tasks.ui.actions.CopyTaskDetailsAction) {
                action.setEnabled(true);
            } else if (action instanceof org.eclipse.mylyn.internal.tasks.ui.actions.RenameAction) {
                if (element instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory) {
                    org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory container = ((org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory) (element));
                    action.setEnabled(container.isUserManaged());
                } else if (element instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery) {
                    action.setEnabled(true);
                }
            }
        } else {
            action.setEnabled(true);
        }
    }

    public java.util.List<org.eclipse.mylyn.tasks.core.IRepositoryElement> getSelectedTaskContainers(org.eclipse.jface.viewers.IStructuredSelection selection) {
        java.util.List<org.eclipse.mylyn.tasks.core.IRepositoryElement> selectedElements = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.IRepositoryElement>();
        for (java.util.Iterator<?> i = selection.iterator(); i.hasNext();) {
            java.lang.Object object = i.next();
            if (object instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) {
                selectedElements.add(((org.eclipse.mylyn.tasks.core.IRepositoryElement) (object)));
            }
        }
        return selectedElements;
    }

    public org.eclipse.mylyn.internal.tasks.ui.actions.OpenTaskListElementAction getOpenAction() {
        return openAction;
    }

    public org.eclipse.mylyn.internal.tasks.ui.actions.TaskActivateAction getActivateAction() {
        return activateAction;
    }

    public org.eclipse.mylyn.internal.tasks.ui.actions.DeleteAction getDeleteAction() {
        return deleteAction;
    }

    public org.eclipse.mylyn.internal.tasks.ui.actions.CopyTaskDetailsAction getCopyDetailsAction() {
        return copyKeySummaryURLAction;
    }
}