/**
 * *****************************************************************************
 * Copyright (c) 2004, 2011 Willian Mitsuda and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Willian Mitsuda - initial API and implementation
 *     Tasktop Technologies - improvements
 *     Tomasz Zarna, IBM Corporation - improvements for bug 261648
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.commands;
import org.eclipse.core.commands.common.CommandException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.ITaskRepositoryFilter;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.internal.tasks.ui.ITaskCommandIds;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoryLabelProvider;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TaskElementLabelProvider;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionStatusDialog;
import org.eclipse.ui.handlers.IHandlerService;
/**
 *
 * @author Willian Mitsuda
 */
public class RemoteTaskSelectionDialog extends org.eclipse.ui.dialogs.SelectionStatusDialog {
    public RemoteTaskSelectionDialog(org.eclipse.swt.widgets.Shell parent) {
        super(parent);
        setShellStyle(getShellStyle() | org.eclipse.swt.SWT.RESIZE);
        setStatusLineAboveButtons(true);
    }

    private org.eclipse.swt.widgets.Text idText;

    private org.eclipse.jface.viewers.TableViewer tasksViewer;

    private org.eclipse.jface.viewers.ComboViewer repositoriesViewer;

    private org.eclipse.swt.widgets.Button addToTaskListCheck;

    private org.eclipse.jface.viewers.ComboViewer categoryViewer;

    // TODO: copy'n pasted code; make API?
    private java.util.List<org.eclipse.mylyn.tasks.core.TaskRepository> getTaskRepositories() {
        java.util.List<org.eclipse.mylyn.tasks.core.TaskRepository> repositories = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.TaskRepository>();
        org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager repositoryManager = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryManager();
        for (org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector : repositoryManager.getRepositoryConnectors()) {
            java.util.Set<org.eclipse.mylyn.tasks.core.TaskRepository> connectorRepositories = repositoryManager.getRepositories(connector.getConnectorKind());
            for (org.eclipse.mylyn.tasks.core.TaskRepository repository : connectorRepositories) {
                if (org.eclipse.mylyn.internal.tasks.core.ITaskRepositoryFilter.CAN_CREATE_TASK_FROM_KEY.accept(repository, connector)) {
                    repositories.add(repository);
                }
            }
        }
        return repositories;
    }

    @java.lang.Override
    protected org.eclipse.swt.widgets.Control createDialogArea(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.widgets.Composite area = ((org.eclipse.swt.widgets.Composite) (super.createDialogArea(parent)));
        org.eclipse.swt.widgets.Label idLabel = new org.eclipse.swt.widgets.Label(area, org.eclipse.swt.SWT.NULL);
        idLabel.setText(Messages.RemoteTaskSelectionDialog_Enter_Key_ID__use_comma_for_multiple_);
        idText = new org.eclipse.swt.widgets.Text(area, org.eclipse.swt.SWT.BORDER);
        idText.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.FILL, true, false));
        org.eclipse.swt.widgets.Label matchingTasksLabel = new org.eclipse.swt.widgets.Label(area, org.eclipse.swt.SWT.NONE);
        matchingTasksLabel.setText(Messages.RemoteTaskSelectionDialog_Matching_tasks);
        tasksViewer = new org.eclipse.jface.viewers.TableViewer(area, ((org.eclipse.swt.SWT.SINGLE | org.eclipse.swt.SWT.BORDER) | org.eclipse.swt.SWT.H_SCROLL) | org.eclipse.swt.SWT.V_SCROLL);
        tasksViewer.getControl().setLayoutData(org.eclipse.jface.layout.GridDataFactory.fillDefaults().grab(true, true).hint(400, 400).create());
        tasksViewer.setLabelProvider(new org.eclipse.jface.viewers.DecoratingLabelProvider(new org.eclipse.mylyn.tasks.ui.TaskElementLabelProvider(true), org.eclipse.ui.PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator()));
        tasksViewer.setContentProvider(new org.eclipse.jface.viewers.ArrayContentProvider());
        tasksViewer.addFilter(new org.eclipse.jface.viewers.ViewerFilter() {
            @java.lang.Override
            public boolean select(org.eclipse.jface.viewers.Viewer viewer, java.lang.Object parentElement, java.lang.Object element) {
                if (selectedIds == null) {
                    return false;
                }
                // Only shows exact task matches
                if (!(element instanceof org.eclipse.mylyn.tasks.core.ITask)) {
                    return false;
                }
                org.eclipse.mylyn.tasks.core.ITask task = ((org.eclipse.mylyn.tasks.core.ITask) (element));
                java.lang.String taskId = task.getTaskKey();
                for (java.lang.String id : selectedIds) {
                    if (id.equals(taskId)) {
                        return true;
                    }
                }
                return false;
            }
        });
        tasksViewer.setInput(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().getAllTasks());
        idText.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
            public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
                computeIds();
                validate();
                tasksViewer.refresh(false);
            }
        });
        tasksViewer.addSelectionChangedListener(new org.eclipse.jface.viewers.ISelectionChangedListener() {
            public void selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent event) {
                validate();
            }
        });
        tasksViewer.addOpenListener(new org.eclipse.jface.viewers.IOpenListener() {
            public void open(org.eclipse.jface.viewers.OpenEvent event) {
                if (getOkButton().getEnabled()) {
                    okPressed();
                }
            }
        });
        org.eclipse.swt.widgets.Table table = tasksViewer.getTable();
        table.showSelection();
        org.eclipse.swt.widgets.Composite repositoriesComposite = new org.eclipse.swt.widgets.Composite(area, org.eclipse.swt.SWT.NONE);
        repositoriesComposite.setLayoutData(org.eclipse.jface.layout.GridDataFactory.fillDefaults().grab(true, false).create());
        repositoriesComposite.setLayout(org.eclipse.jface.layout.GridLayoutFactory.fillDefaults().numColumns(3).create());
        org.eclipse.swt.widgets.Label repositoriesLabel = new org.eclipse.swt.widgets.Label(repositoriesComposite, org.eclipse.swt.SWT.NONE);
        repositoriesLabel.setText(Messages.RemoteTaskSelectionDialog_Select_a_task_repository);
        repositoriesViewer = new org.eclipse.jface.viewers.ComboViewer(repositoriesComposite, org.eclipse.swt.SWT.DROP_DOWN | org.eclipse.swt.SWT.READ_ONLY);
        repositoriesViewer.setLabelProvider(new org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoryLabelProvider());
        repositoriesViewer.setContentProvider(new org.eclipse.jface.viewers.ArrayContentProvider());
        java.util.List<org.eclipse.mylyn.tasks.core.TaskRepository> taskRepositories = getTaskRepositories();
        repositoriesViewer.setInput(taskRepositories);
        if (taskRepositories.size() == 1) {
            repositoriesViewer.setSelection(new org.eclipse.jface.viewers.StructuredSelection(taskRepositories.get(0)));
        }
        org.eclipse.mylyn.tasks.core.TaskRepository currentRepository = org.eclipse.mylyn.tasks.ui.TasksUiUtil.getSelectedRepository(null);
        if (currentRepository != null) {
            repositoriesViewer.setSelection(new org.eclipse.jface.viewers.StructuredSelection(currentRepository), true);
        }
        repositoriesViewer.getControl().setLayoutData(org.eclipse.jface.layout.GridDataFactory.fillDefaults().grab(true, false).create());
        repositoriesViewer.addSelectionChangedListener(new org.eclipse.jface.viewers.ISelectionChangedListener() {
            public void selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent event) {
                tasksViewer.setSelection(org.eclipse.jface.viewers.StructuredSelection.EMPTY);
                validate();
            }
        });
        org.eclipse.swt.widgets.Button addRepositoryButton = new org.eclipse.swt.widgets.Button(repositoriesComposite, org.eclipse.swt.SWT.NONE);
        addRepositoryButton.setText(Messages.RemoteTaskSelectionDialog_Add_);
        addRepositoryButton.setEnabled(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryManager().hasUserManagedRepositoryConnectors());
        addRepositoryButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
                org.eclipse.ui.handlers.IHandlerService hndSvc = ((org.eclipse.ui.handlers.IHandlerService) (org.eclipse.ui.PlatformUI.getWorkbench().getService(org.eclipse.ui.handlers.IHandlerService.class)));
                try {
                    hndSvc.executeCommand(org.eclipse.mylyn.internal.tasks.ui.ITaskCommandIds.ADD_TASK_REPOSITORY, null);
                    repositoriesViewer.setInput(getTaskRepositories());
                } catch (org.eclipse.core.commands.common.CommandException e) {
                    org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, e.getMessage(), e));
                }
            }
        });
        org.eclipse.swt.widgets.Composite addToTaskListComposite = new org.eclipse.swt.widgets.Composite(area, org.eclipse.swt.SWT.NONE);
        addToTaskListComposite.setLayout(org.eclipse.jface.layout.GridLayoutFactory.fillDefaults().numColumns(2).create());
        addToTaskListCheck = new org.eclipse.swt.widgets.Button(addToTaskListComposite, org.eclipse.swt.SWT.CHECK);
        addToTaskListCheck.setText(Messages.RemoteTaskSelectionDialog_Add_to_Task_List_category);
        categoryViewer = new org.eclipse.jface.viewers.ComboViewer(addToTaskListComposite, org.eclipse.swt.SWT.DROP_DOWN | org.eclipse.swt.SWT.READ_ONLY);
        categoryViewer.setContentProvider(new org.eclipse.jface.viewers.ArrayContentProvider());
        org.eclipse.mylyn.internal.tasks.core.TaskList taskList = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList();
        java.util.LinkedList<org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer> categories = new java.util.LinkedList<org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer>(taskList.getCategories());
        categories.addFirst(taskList.getDefaultCategory());
        categoryViewer.setInput(categories);
        categoryViewer.setLabelProvider(new org.eclipse.jface.viewers.LabelProvider() {
            @java.lang.Override
            public java.lang.String getText(java.lang.Object element) {
                if (element instanceof org.eclipse.mylyn.tasks.core.IRepositoryElement) {
                    return ((org.eclipse.mylyn.tasks.core.IRepositoryElement) (element)).getSummary();
                }
                return super.getText(element);
            }
        });
        categoryViewer.setSelection(new org.eclipse.jface.viewers.StructuredSelection(taskList.getDefaultCategory()));
        categoryViewer.getControl().setEnabled(addToTaskListCheck.getSelection());
        addToTaskListCheck.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                categoryViewer.getControl().setEnabled(addToTaskListCheck.getSelection());
            }
        });
        idText.addKeyListener(new org.eclipse.swt.events.KeyAdapter() {
            @java.lang.Override
            public void keyPressed(org.eclipse.swt.events.KeyEvent e) {
                if (e.keyCode == org.eclipse.swt.SWT.ARROW_DOWN) {
                    tasksViewer.getControl().setFocus();
                }
            }
        });
        validate();
        org.eclipse.jface.dialogs.Dialog.applyDialogFont(area);
        return area;
    }

    private void validate() {
        if (idText.getText().trim().equals("")) {
            // $NON-NLS-1$
            updateStatus(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.INFO, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, 0, Messages.RemoteTaskSelectionDialog_Enter_a_valid_task_ID, null));
            return;
        }
        if (tasksViewer.getSelection().isEmpty() && repositoriesViewer.getSelection().isEmpty()) {
            updateStatus(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.INFO, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, 0, Messages.RemoteTaskSelectionDialog_Select_a_task_or_repository, null));
            return;
        }
        updateStatus(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.OK, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, 0, "", null));// $NON-NLS-1$

    }

    @java.lang.Override
    protected void updateStatus(org.eclipse.core.runtime.IStatus status) {
        super.updateStatus(status);
        // support disabling button for non-error statuses
        org.eclipse.swt.widgets.Button okButton = getOkButton();
        if ((okButton != null) && (!okButton.isDisposed())) {
            okButton.setEnabled(status.isOK());
        }
    }

    private java.lang.String[] selectedIds;

    private org.eclipse.mylyn.tasks.core.TaskRepository selectedRepository;

    private org.eclipse.mylyn.internal.tasks.core.AbstractTask selectedTask;

    private boolean shouldAddToTaskList;

    private org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory selectedCategory;

    public java.lang.String[] getSelectedIds() {
        return selectedIds;
    }

    public org.eclipse.mylyn.tasks.core.TaskRepository getSelectedTaskRepository() {
        return selectedRepository;
    }

    public org.eclipse.mylyn.internal.tasks.core.AbstractTask getSelectedTask() {
        return selectedTask;
    }

    public boolean shouldAddToTaskList() {
        return shouldAddToTaskList;
    }

    public org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory getSelectedCategory() {
        return selectedCategory;
    }

    @java.lang.Override
    protected void computeResult() {
        computeIds();
        org.eclipse.jface.viewers.ISelection taskSelection = tasksViewer.getSelection();
        if (!taskSelection.isEmpty()) {
            selectedTask = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (((org.eclipse.jface.viewers.IStructuredSelection) (taskSelection)).getFirstElement()));
        } else {
            selectedRepository = ((org.eclipse.mylyn.tasks.core.TaskRepository) (((org.eclipse.jface.viewers.IStructuredSelection) (repositoriesViewer.getSelection())).getFirstElement()));
        }
        shouldAddToTaskList = addToTaskListCheck.getSelection();
        if (shouldAddToTaskList) {
            selectedCategory = ((org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory) (((org.eclipse.jface.viewers.IStructuredSelection) (categoryViewer.getSelection())).getFirstElement()));
        }
    }

    private void computeIds() {
        selectedIds = idText.getText().split(",");// $NON-NLS-1$

        for (java.lang.String id : selectedIds) {
            id = id.trim();
        }
    }
}