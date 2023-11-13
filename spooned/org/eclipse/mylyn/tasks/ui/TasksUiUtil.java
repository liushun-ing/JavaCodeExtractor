/**
 * *****************************************************************************
 * Copyright (c) 2004, 2014 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.tasks.ui;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.workbench.EditorHandle;
import org.eclipse.mylyn.commons.workbench.browser.BrowserUtil;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector;
import org.eclipse.mylyn.internal.tasks.core.LocalTask;
import org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TaskOpenEvent;
import org.eclipse.mylyn.internal.tasks.ui.util.TaskOpenListener;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoriesView;
import org.eclipse.mylyn.internal.tasks.ui.wizards.EditRepositoryWizard;
import org.eclipse.mylyn.internal.tasks.ui.wizards.MultiRepositoryAwareWizard;
import org.eclipse.mylyn.internal.tasks.ui.wizards.NewLocalTaskWizard;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITask.SynchronizationState;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.TaskRepositoryWizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;
/**
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @since 2.0
 * @author Mik Kersten
 * @author Steffen Pingel
 * @author Shawn Minto
 */
public class TasksUiUtil {
    /**
     * Flag that is passed along to the workbench browser support when a task is opened in a browser because no rich
     * editor was available.
     *
     * @see #openTask(String)
     * @deprecated use {@link BrowserUtil#NO_RICH_EDITOR} instead
     */
    @java.lang.Deprecated
    public static final int FLAG_NO_RICH_EDITOR = 1 << 17;

    /**
     *
     * @since 3.0
     */
    public static org.eclipse.mylyn.tasks.core.ITask createOutgoingNewTask(java.lang.String connectorKind, java.lang.String repositoryUrl) {
        org.eclipse.core.runtime.Assert.isNotNull(connectorKind);
        org.eclipse.mylyn.internal.tasks.core.LocalTask task = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.createNewLocalTask(null);
        task.setAttribute(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.ATTRIBUTE_OUTGOING_NEW_CONNECTOR_KIND, connectorKind);
        task.setAttribute(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.ATTRIBUTE_OUTGOING_NEW_REPOSITORY_URL, repositoryUrl);
        task.setSynchronizationState(org.eclipse.mylyn.tasks.core.ITask.SynchronizationState.OUTGOING_NEW);
        return task;
    }

    /**
     *
     * @since 3.0
     */
    public static boolean isOutgoingNewTask(org.eclipse.mylyn.tasks.core.ITask task, java.lang.String connectorKind) {
        org.eclipse.core.runtime.Assert.isNotNull(task);
        org.eclipse.core.runtime.Assert.isNotNull(connectorKind);
        return connectorKind.equals(task.getAttribute(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.ATTRIBUTE_OUTGOING_NEW_CONNECTOR_KIND));
    }

    /**
     *
     * @since 3.1
     */
    public static org.eclipse.mylyn.tasks.core.TaskRepository getOutgoingNewTaskRepository(org.eclipse.mylyn.tasks.core.ITask task) {
        org.eclipse.core.runtime.Assert.isNotNull(task);
        java.lang.String connectorKind = task.getAttribute(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.ATTRIBUTE_OUTGOING_NEW_CONNECTOR_KIND);
        java.lang.String repositoryUrl = task.getAttribute(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.ATTRIBUTE_OUTGOING_NEW_REPOSITORY_URL);
        if ((connectorKind != null) && (repositoryUrl != null)) {
            return org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(connectorKind, repositoryUrl);
        }
        return null;
    }

    public static org.eclipse.mylyn.tasks.core.TaskRepository getSelectedRepository() {
        return org.eclipse.mylyn.tasks.ui.TasksUiUtil.getSelectedRepository(null);
    }

    /**
     * Will use the workbench window's selection if viewer's selection is null
     */
    public static org.eclipse.mylyn.tasks.core.TaskRepository getSelectedRepository(org.eclipse.jface.viewers.StructuredViewer viewer) {
        org.eclipse.jface.viewers.IStructuredSelection selection = null;
        if (viewer != null) {
            selection = ((org.eclipse.jface.viewers.IStructuredSelection) (viewer.getSelection()));
        }
        if ((selection == null) || selection.isEmpty()) {
            org.eclipse.ui.IWorkbenchWindow window = org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            org.eclipse.jface.viewers.ISelection windowSelection = window.getSelectionService().getSelection();
            if (windowSelection instanceof org.eclipse.jface.viewers.IStructuredSelection) {
                selection = ((org.eclipse.jface.viewers.IStructuredSelection) (windowSelection));
            }
        }
        if (selection == null) {
            return null;
        }
        java.lang.Object element = selection.getFirstElement();
        if (element instanceof org.eclipse.mylyn.tasks.core.TaskRepository) {
            return ((org.eclipse.mylyn.tasks.core.TaskRepository) (selection.getFirstElement()));
        } else if (element instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery) {
            org.eclipse.mylyn.tasks.core.IRepositoryQuery query = ((org.eclipse.mylyn.tasks.core.IRepositoryQuery) (element));
            return org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(query.getConnectorKind(), query.getRepositoryUrl());
        } else if (element instanceof org.eclipse.mylyn.tasks.core.ITask) {
            org.eclipse.mylyn.tasks.core.ITask task = ((org.eclipse.mylyn.tasks.core.ITask) (element));
            return org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(task.getConnectorKind(), task.getRepositoryUrl());
        } else if (element instanceof org.eclipse.core.resources.IResource) {
            org.eclipse.core.resources.IResource resource = ((org.eclipse.core.resources.IResource) (element));
            return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getRepositoryForResource(resource);
        } else if (element instanceof org.eclipse.core.runtime.IAdaptable) {
            org.eclipse.core.runtime.IAdaptable adaptable = ((org.eclipse.core.runtime.IAdaptable) (element));
            org.eclipse.core.resources.IResource resource = ((org.eclipse.core.resources.IResource) (adaptable.getAdapter(org.eclipse.core.resources.IResource.class)));
            if (resource != null) {
                return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getRepositoryForResource(resource);
            } else {
                org.eclipse.mylyn.tasks.core.ITask task = ((org.eclipse.mylyn.tasks.core.ITask) (adaptable.getAdapter(org.eclipse.mylyn.internal.tasks.core.AbstractTask.class)));
                if (task != null) {
                    org.eclipse.mylyn.tasks.core.ITask rtask = task;
                    return org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(rtask.getConnectorKind(), rtask.getRepositoryUrl());
                }
            }
        }
        // TODO mapping between LogEntry.pliginId and repositories
        // TODO handle other selection types
        return null;
    }

    public static org.eclipse.ui.IEditorPart openEditor(org.eclipse.ui.IEditorInput input, java.lang.String editorId, org.eclipse.ui.IWorkbenchPage page) {
        if (page == null) {
            org.eclipse.ui.IWorkbenchWindow window = org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if (window != null) {
                page = window.getActivePage();
            }
        }
        if (page == null) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, ("Unable to open editor for \"" + input)// $NON-NLS-1$
             + "\": no active workbench window"));// $NON-NLS-1$

            return null;
        }
        try {
            return page.openEditor(input, editorId);
        } catch (org.eclipse.ui.PartInitException e) {
            org.eclipse.ui.statushandlers.StatusManager.getManager().handle(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, (("Open for editor failed: " + input)// $NON-NLS-1$
             + ", taskId: ") + editorId, e), org.eclipse.ui.statushandlers.StatusManager.SHOW | org.eclipse.ui.statushandlers.StatusManager.LOG);// $NON-NLS-1$

        }
        return null;
    }

    public static int openEditRepositoryWizard(org.eclipse.mylyn.tasks.core.TaskRepository repository) {
        org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(repository.getConnectorKind());
        if ((connector == null) || ((!connector.isUserManaged()) && (!connector.getConnectorKind().equals(org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector.CONNECTOR_KIND)))) {
            return org.eclipse.jface.window.Window.CANCEL;
        }
        org.eclipse.mylyn.internal.tasks.ui.wizards.EditRepositoryWizard wizard = new org.eclipse.mylyn.internal.tasks.ui.wizards.EditRepositoryWizard(repository, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnectorUi(repository.getConnectorKind()));
        org.eclipse.swt.widgets.Shell shell = org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        if ((shell != null) && (!shell.isDisposed())) {
            org.eclipse.jface.wizard.WizardDialog dialog = new org.eclipse.mylyn.tasks.ui.wizards.TaskRepositoryWizardDialog(shell, wizard);
            dialog.create();
            dialog.setBlockOnOpen(true);
            if (dialog.open() == org.eclipse.jface.window.Window.CANCEL) {
                return org.eclipse.jface.window.Window.CANCEL;
            }
        }
        if (org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoriesView.getFromActivePerspective() != null) {
            org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoriesView.getFromActivePerspective().getViewer().refresh();
        }
        return org.eclipse.jface.window.Window.OK;
    }

    /**
     *
     * @since 3.0
     */
    public static boolean openNewLocalTaskEditor(org.eclipse.swt.widgets.Shell shell, org.eclipse.mylyn.tasks.core.ITaskMapping taskSelection) {
        return org.eclipse.mylyn.tasks.ui.TasksUiUtil.openNewTaskEditor(shell, new org.eclipse.mylyn.internal.tasks.ui.wizards.NewLocalTaskWizard(taskSelection), taskSelection);
    }

    private static boolean openNewTaskEditor(org.eclipse.swt.widgets.Shell shell, org.eclipse.jface.wizard.IWizard wizard, org.eclipse.mylyn.tasks.core.ITaskMapping taskSelection) {
        org.eclipse.jface.wizard.WizardDialog dialog = new org.eclipse.jface.wizard.WizardDialog(shell, wizard);
        dialog.setBlockOnOpen(true);
        // make sure the wizard has created its pages
        dialog.create();
        if ((!(wizard instanceof org.eclipse.mylyn.internal.tasks.ui.wizards.MultiRepositoryAwareWizard)) && wizard.canFinish()) {
            wizard.performFinish();
            return true;
        }
        int result = dialog.open();
        return result == org.eclipse.jface.window.Window.OK;
    }

    /**
     *
     * @since 3.0
     */
    public static boolean openNewTaskEditor(org.eclipse.swt.widgets.Shell shell, org.eclipse.mylyn.tasks.core.ITaskMapping taskSelection, org.eclipse.mylyn.tasks.core.TaskRepository taskRepository) {
        final org.eclipse.jface.wizard.IWizard wizard;
        // List<TaskRepository> repositories = TasksUi.getRepositoryManager().getAllRepositories();
        // if (taskRepository == null && repositories.size() == 1) {
        // // only the Local repository connector is available
        // taskRepository = repositories.get(0);
        // }
        if (taskRepository != null) {
            org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnectorUi(taskRepository.getConnectorKind());
            wizard = connectorUi.getNewTaskWizard(taskRepository, taskSelection);
        } else {
            wizard = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.createNewTaskWizard(taskSelection);
        }
        return org.eclipse.mylyn.tasks.ui.TasksUiUtil.openNewTaskEditor(shell, wizard, taskSelection);
    }

    /**
     * Either pass in a repository and taskId, or fullUrl, or all of them
     *
     * @deprecated Use {@link #openTask(String,String,String)} instead
     */
    @java.lang.Deprecated
    public static boolean openRepositoryTask(java.lang.String repositoryUrl, java.lang.String taskId, java.lang.String fullUrl) {
        return org.eclipse.mylyn.tasks.ui.TasksUiUtil.openTask(repositoryUrl, taskId, fullUrl);
    }

    /**
     *
     * @deprecated Use {@link #openTask(TaskRepository,String)} instead
     */
    @java.lang.Deprecated
    public static boolean openRepositoryTask(org.eclipse.mylyn.tasks.core.TaskRepository repository, java.lang.String taskId) {
        return org.eclipse.mylyn.tasks.ui.TasksUiUtil.openTask(repository, taskId);
    }

    /**
     *
     * @since 3.0
     */
    public static boolean openTask(org.eclipse.mylyn.tasks.core.ITask task) {
        return org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.openTask(task, task.getTaskId()) != null;
    }

    /**
     * Resolves a rich editor for the task if available.
     *
     * @since 3.0
     */
    public static void openTask(java.lang.String url) {
        org.eclipse.mylyn.internal.tasks.core.AbstractTask task = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskByUrl(url);
        if ((task != null) && (!(task instanceof org.eclipse.mylyn.internal.tasks.core.LocalTask))) {
            org.eclipse.mylyn.tasks.ui.TasksUiUtil.openTask(task);
        } else {
            boolean opened = false;
            if (url != null) {
                org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryManager().getConnectorForRepositoryTaskUrl(url);
                if (connector != null) {
                    java.lang.String repositoryUrl = connector.getRepositoryUrlFromTaskUrl(url);
                    if (repositoryUrl != null) {
                        java.lang.String id = connector.getTaskIdFromTaskUrl(url);
                        if (id != null) {
                            org.eclipse.mylyn.tasks.core.TaskRepository repository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(connector.getConnectorKind(), repositoryUrl);
                            if (repository != null) {
                                opened = org.eclipse.mylyn.tasks.ui.TasksUiUtil.openTask(repository, id);
                            }
                        }
                    }
                }
            }
            if (!opened) {
                org.eclipse.mylyn.commons.workbench.browser.BrowserUtil.openUrl(url, 0);
            }
        }
    }

    /**
     * Either pass in a repository and taskId, or fullUrl, or all of them
     *
     * @since 3.0
     */
    public static boolean openTask(java.lang.String repositoryUrl, java.lang.String taskId, java.lang.String fullUrl) {
        return org.eclipse.mylyn.tasks.ui.TasksUiUtil.openTask(repositoryUrl, taskId, fullUrl, 0);
    }

    /**
     * Either pass in a repository and taskId, or fullUrl, or all of them the time stamp is used for selecting the
     * correct comment
     *
     * @since 3.4
     */
    public static boolean openTask(java.lang.String repositoryUrl, java.lang.String taskId, java.lang.String fullUrl, long timestamp) {
        org.eclipse.mylyn.internal.tasks.core.AbstractTask task = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTask(repositoryUrl, taskId, fullUrl);
        if (task != null) {
            return org.eclipse.mylyn.tasks.ui.TasksUiUtil.openTask(task);
        }
        boolean opened = false;
        org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryManager().getConnectorForRepositoryTaskUrl(fullUrl);
        if (connector != null) {
            if ((repositoryUrl != null) && (taskId != null)) {
                opened = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.openRepositoryTask(connector.getConnectorKind(), repositoryUrl, taskId, null, timestamp);
            } else {
                repositoryUrl = connector.getRepositoryUrlFromTaskUrl(fullUrl);
                taskId = connector.getTaskIdFromTaskUrl(fullUrl);
                if ((repositoryUrl != null) && (taskId != null)) {
                    opened = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.openRepositoryTask(connector.getConnectorKind(), repositoryUrl, taskId, null, timestamp);
                }
            }
        }
        if (!opened) {
            org.eclipse.mylyn.tasks.ui.TasksUiUtil.openUrl(fullUrl);
        }
        return true;
    }

    /**
     *
     * @since 3.0
     */
    public static boolean openTask(org.eclipse.mylyn.tasks.core.TaskRepository repository, java.lang.String taskId) {
        org.eclipse.core.runtime.Assert.isNotNull(repository);
        org.eclipse.core.runtime.Assert.isNotNull(taskId);
        return org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.openTask(repository, taskId, null);
    }

    /**
     *
     * @since 3.7
     */
    public static org.eclipse.mylyn.commons.workbench.EditorHandle openTaskWithResult(org.eclipse.mylyn.tasks.core.TaskRepository repository, java.lang.String taskId) {
        org.eclipse.core.runtime.Assert.isNotNull(repository);
        org.eclipse.core.runtime.Assert.isNotNull(taskId);
        final org.eclipse.mylyn.commons.workbench.EditorHandle handle = new org.eclipse.mylyn.commons.workbench.EditorHandle();
        boolean opened = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.openTask(repository, taskId, new org.eclipse.mylyn.internal.tasks.ui.util.TaskOpenListener() {
            @java.lang.Override
            public void taskOpened(org.eclipse.mylyn.internal.tasks.ui.util.TaskOpenEvent event) {
                handle.setPart(event.getEditor());
                handle.setItem(event.getTask());
                handle.setStatus(org.eclipse.core.runtime.Status.OK_STATUS);
            }
        });
        return opened ? handle : null;
    }

    /**
     *
     * @since 3.0
     */
    public static void openUrl(java.lang.String location) {
        org.eclipse.mylyn.commons.workbench.browser.BrowserUtil.openUrl(location, org.eclipse.mylyn.commons.workbench.browser.BrowserUtil.NO_RICH_EDITOR);
    }

    /**
     * Opens <code>element</code> in a browser using an authenticated URL if available.
     *
     * @since 3.4
     */
    public static boolean openWithBrowser(org.eclipse.mylyn.tasks.core.IRepositoryElement element) {
        org.eclipse.mylyn.tasks.core.TaskRepository repository = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getRepository(element);
        return repository != null ? org.eclipse.mylyn.tasks.ui.TasksUiUtil.openWithBrowser(repository, element) : false;
    }

    /**
     * Opens <code>element</code> in a browser using an authenticated URL if available.
     *
     * @since 3.4
     */
    public static boolean openWithBrowser(org.eclipse.mylyn.tasks.core.TaskRepository repository, org.eclipse.mylyn.tasks.core.IRepositoryElement element) {
        java.lang.String url = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getAuthenticatedUrl(repository, element);
        if (url != null) {
            org.eclipse.mylyn.tasks.ui.TasksUiUtil.openUrl(url);
            return true;
        }
        return false;
    }

    /**
     *
     * @since 3.0
     */
    public static org.eclipse.ui.IViewPart openTasksViewInActivePerspective() {
        try {
            return org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(ITasksUiConstants.ID_VIEW_TASKS);
        } catch (java.lang.Exception e) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Could not show Task List view", e));// $NON-NLS-1$

            return null;
        }
    }

    /**
     * Returns if the current line in the task editor should be highlighted.
     *
     * @return true, if line highlighting is enabled
     * @since 3.4
     */
    public static boolean getHighlightCurrentLine() {
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.EDITOR_CURRENT_LINE_HIGHLIGHT);
    }
}