/**
 * *****************************************************************************
 * Copyright (c) 2004, 2015 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.actions;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.mylyn.internal.commons.ui.TaskListImageDescriptor;
import org.eclipse.mylyn.internal.tasks.core.ITaskRepositoryFilter;
import org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoriesSorter;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
/**
 *
 * @author Mik Kersten
 * @author Eugene Kuleshov
 * @author Steffen Pingel
 * @author Rob Elves
 */
@java.lang.SuppressWarnings("restriction")
public class NewTaskAction extends org.eclipse.ui.actions.BaseSelectionListenerAction implements org.eclipse.jface.action.IMenuCreator , org.eclipse.ui.IViewActionDelegate , org.eclipse.core.runtime.IExecutableExtension {
    private static final java.lang.String LABEL_NEW_TASK = Messages.NewTaskAction_new_task;

    public static final java.lang.String ID = "org.eclipse.mylyn.tasklist.ui.repositories.actions.create";// $NON-NLS-1$


    private boolean skipRepositoryPage = false;

    private boolean localTask = false;

    private org.eclipse.swt.widgets.Menu dropDownMenu;

    public NewTaskAction(java.lang.String label, boolean alwaysShowWizard) {
        super(label);
        if (!alwaysShowWizard) {
            setMenuCreator(this);
        }
        setText(label);
        setToolTipText(label);
        setId(org.eclipse.mylyn.internal.tasks.ui.actions.NewTaskAction.ID);
        setEnabled(true);
        setImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.TASK_NEW);
    }

    public NewTaskAction() {
        this(org.eclipse.mylyn.internal.tasks.ui.actions.NewTaskAction.LABEL_NEW_TASK, false);
    }

    @java.lang.Override
    public void run() {
        org.eclipse.swt.widgets.Shell shell = org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        if ((shell != null) && (!shell.isDisposed())) {
            if (localTask) {
                org.eclipse.mylyn.tasks.ui.TasksUiUtil.openNewLocalTaskEditor(shell, null);
            } else if (skipRepositoryPage) {
                org.eclipse.mylyn.tasks.ui.TasksUiUtil.openNewTaskEditor(shell, null, org.eclipse.mylyn.tasks.ui.TasksUiUtil.getSelectedRepository());
            } else {
                org.eclipse.mylyn.tasks.ui.TasksUiUtil.openNewTaskEditor(shell, null, null);
            }
        }
    }

    public void run(org.eclipse.jface.action.IAction action) {
        run();
    }

    public void init(org.eclipse.ui.IViewPart view) {
    }

    public void selectionChanged(org.eclipse.jface.action.IAction action, org.eclipse.jface.viewers.ISelection selection) {
    }

    public void setInitializationData(org.eclipse.core.runtime.IConfigurationElement config, java.lang.String propertyName, java.lang.Object data) throws org.eclipse.core.runtime.CoreException {
        if ("skipFirstPage".equals(data)) {
            // $NON-NLS-1$
            this.skipRepositoryPage = true;
        }
        if ("local".equals(data)) {
            // $NON-NLS-1$
            this.localTask = true;
        }
    }

    public void dispose() {
        if (dropDownMenu != null) {
            dropDownMenu.dispose();
            dropDownMenu = null;
        }
    }

    public org.eclipse.swt.widgets.Menu getMenu(org.eclipse.swt.widgets.Control parent) {
        if (dropDownMenu != null) {
            dropDownMenu.dispose();
        }
        dropDownMenu = new org.eclipse.swt.widgets.Menu(parent);
        addActionsToMenu();
        return dropDownMenu;
    }

    public org.eclipse.swt.widgets.Menu getMenu(org.eclipse.swt.widgets.Menu parent) {
        if (dropDownMenu != null) {
            dropDownMenu.dispose();
        }
        dropDownMenu = new org.eclipse.swt.widgets.Menu(parent);
        addActionsToMenu();
        return dropDownMenu;
    }

    private void addActionsToMenu() {
        org.eclipse.mylyn.internal.tasks.ui.actions.NewTaskAction newTaskAction = new org.eclipse.mylyn.internal.tasks.ui.actions.NewTaskAction(org.eclipse.mylyn.internal.tasks.ui.actions.NewTaskAction.LABEL_NEW_TASK, true);
        newTaskAction.setText(Messages.NewTaskAction_Show_Wizard_Label);
        new org.eclipse.jface.action.ActionContributionItem(newTaskAction).fill(dropDownMenu, -1);
        new org.eclipse.jface.action.Separator().fill(dropDownMenu, -1);
        java.util.Set<org.eclipse.mylyn.tasks.core.TaskRepository> includedRepositories = new java.util.HashSet<org.eclipse.mylyn.tasks.core.TaskRepository>();
        org.eclipse.mylyn.tasks.core.TaskRepository localRepository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector.CONNECTOR_KIND, org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector.REPOSITORY_URL);
        addRepositoryAction(localRepository);
        org.eclipse.ui.IWorkingSet workingSet = org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getAggregateWorkingSet();
        if ((workingSet != null) && (!workingSet.isEmpty())) {
            // only add repositories in working set
            for (org.eclipse.core.runtime.IAdaptable iterable_element : workingSet.getElements()) {
                if (iterable_element instanceof org.eclipse.mylyn.internal.tasks.core.RepositoryQuery) {
                    java.lang.String repositoryUrl = ((org.eclipse.mylyn.internal.tasks.core.RepositoryQuery) (iterable_element)).getRepositoryUrl();
                    java.lang.String connectorKind = ((org.eclipse.mylyn.internal.tasks.core.RepositoryQuery) (iterable_element)).getConnectorKind();
                    org.eclipse.mylyn.tasks.core.TaskRepository repository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(connectorKind, repositoryUrl);
                    markForInclusion(includedRepositories, repository);
                }
            }
        }
        if (includedRepositories.isEmpty()) {
            // No repositories were added from working sets so show all
            for (org.eclipse.mylyn.tasks.core.TaskRepository repository : org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getAllRepositories()) {
                markForInclusion(includedRepositories, repository);
            }
        }
        if (!includedRepositories.isEmpty()) {
            // new Separator().fill(dropDownMenu, -1);
            java.util.ArrayList<org.eclipse.mylyn.tasks.core.TaskRepository> listOfRepositories = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.TaskRepository>(includedRepositories);
            final org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoriesSorter comparator = new org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoriesSorter();
            java.util.Collections.sort(listOfRepositories, new java.util.Comparator<org.eclipse.mylyn.tasks.core.TaskRepository>() {
                public int compare(org.eclipse.mylyn.tasks.core.TaskRepository arg0, org.eclipse.mylyn.tasks.core.TaskRepository arg1) {
                    return comparator.compare(null, arg0, arg1);
                }
            });
            for (org.eclipse.mylyn.tasks.core.TaskRepository taskRepository : listOfRepositories) {
                addRepositoryAction(taskRepository);
            }
        }
        new org.eclipse.jface.action.Separator().fill(dropDownMenu, -1);
        new org.eclipse.jface.action.ActionContributionItem(new org.eclipse.mylyn.internal.tasks.ui.actions.NewQueryAction()).fill(dropDownMenu, -1);
        new org.eclipse.jface.action.ActionContributionItem(new org.eclipse.mylyn.internal.tasks.ui.actions.NewCategoryAction()).fill(dropDownMenu, -1);
        new org.eclipse.jface.action.Separator().fill(dropDownMenu, -1);
        org.eclipse.mylyn.internal.tasks.ui.actions.AddRepositoryAction action = new org.eclipse.mylyn.internal.tasks.ui.actions.AddRepositoryAction();
        action.setText(Messages.NewTaskAction_Add_Repository);
        action.setImageDescriptor(null);
        new org.eclipse.jface.action.ActionContributionItem(action).fill(dropDownMenu, -1);
        new org.eclipse.jface.action.Separator(org.eclipse.ui.IWorkbenchActionConstants.MB_ADDITIONS);
    }

    private void markForInclusion(java.util.Set<org.eclipse.mylyn.tasks.core.TaskRepository> includedRepositories, org.eclipse.mylyn.tasks.core.TaskRepository repository) {
        if ((repository != null) && (!repository.getConnectorKind().equals(org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector.CONNECTOR_KIND))) {
            org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryConnector(repository.getConnectorKind());
            if (connector != null) {
                if (org.eclipse.mylyn.internal.tasks.core.ITaskRepositoryFilter.CAN_CREATE_NEW_TASK.accept(repository, connector)) {
                    includedRepositories.add(repository);
                }
            }
        }
    }

    private org.eclipse.mylyn.internal.tasks.ui.actions.NewTaskAction.RepositorySelectionAction addRepositoryAction(org.eclipse.mylyn.tasks.core.TaskRepository repository) {
        if (repository == null) {
            return null;
        }
        org.eclipse.mylyn.internal.tasks.ui.actions.NewTaskAction.RepositorySelectionAction action = new org.eclipse.mylyn.internal.tasks.ui.actions.NewTaskAction.RepositorySelectionAction(repository);
        org.eclipse.jface.action.ActionContributionItem item = new org.eclipse.jface.action.ActionContributionItem(action);
        action.setText(repository.getRepositoryLabel());
        org.eclipse.jface.resource.ImageDescriptor overlay = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getBrandManager().getOverlayIcon(repository);
        org.eclipse.jface.resource.ImageDescriptor compositeDescriptor = new org.eclipse.mylyn.internal.commons.ui.TaskListImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.TASK_NEW, overlay, false, false);
        action.setImageDescriptor(compositeDescriptor);
        item.fill(dropDownMenu, -1);
        return action;
    }

    private class RepositorySelectionAction extends org.eclipse.jface.action.Action {
        private final org.eclipse.mylyn.tasks.core.TaskRepository repository;

        public RepositorySelectionAction(org.eclipse.mylyn.tasks.core.TaskRepository repository) {
            this.repository = repository;
            setText(repository.getRepositoryLabel());
        }

        @java.lang.Override
        public void run() {
            org.eclipse.swt.widgets.Shell shell = org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
            if (repository.getConnectorKind().equalsIgnoreCase(org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector.CONNECTOR_KIND)) {
                org.eclipse.mylyn.tasks.ui.TasksUiUtil.openNewLocalTaskEditor(shell, null);
            } else {
                org.eclipse.mylyn.tasks.ui.TasksUiUtil.openNewTaskEditor(shell, null, repository);
            }
        }
    }
}