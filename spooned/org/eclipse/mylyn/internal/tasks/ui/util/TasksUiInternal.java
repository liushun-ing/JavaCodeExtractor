/**
 * *****************************************************************************
 * Copyright (c) 2004, 2016 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Steve Elsemore - fix for bug 296963
 *     Atlassian - fix for bug 319699
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.util;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.ObjectUndoContext;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.mylyn.commons.core.CoreUtil;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.identity.core.Account;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.commons.ui.CommonUiUtil;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.DateRange;
import org.eclipse.mylyn.internal.tasks.core.ITaskJobFactory;
import org.eclipse.mylyn.internal.tasks.core.ITaskList;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector;
import org.eclipse.mylyn.internal.tasks.core.LocalTask;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil;
import org.eclipse.mylyn.internal.tasks.core.TaskCategory;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.core.UncategorizedTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.UnsubmittedTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataState;
import org.eclipse.mylyn.internal.tasks.core.sync.SynchronizationScheduler;
import org.eclipse.mylyn.internal.tasks.core.sync.SynchronizationScheduler.Synchronizer;
import org.eclipse.mylyn.internal.tasks.core.util.TasksCoreUtil;
import org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants;
import org.eclipse.mylyn.internal.tasks.ui.OpenRepositoryTaskJob;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylyn.internal.tasks.ui.wizards.MultiRepositoryAwareWizard;
import org.eclipse.mylyn.internal.tasks.ui.wizards.NewAttachmentWizardDialog;
import org.eclipse.mylyn.internal.tasks.ui.wizards.NewTaskWizardInternal;
import org.eclipse.mylyn.internal.tasks.ui.wizards.QueryWizardDialog;
import org.eclipse.mylyn.internal.tasks.ui.wizards.TaskAttachmentWizard;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.IRepositoryManager;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITask.PriorityLevel;
import org.eclipse.mylyn.tasks.core.ITask.SynchronizationState;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.ITaskDataWorkingCopy;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskCommentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskHistory;
import org.eclipse.mylyn.tasks.core.data.TaskRevision;
import org.eclipse.mylyn.tasks.core.data.TaskRevision.Change;
import org.eclipse.mylyn.tasks.core.sync.SynchronizationJob;
import org.eclipse.mylyn.tasks.core.sync.TaskJob;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.statushandlers.IStatusAdapterConstants;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;
/**
 *
 * @author Steffen Pingel
 */
public class TasksUiInternal {
    private static org.eclipse.mylyn.internal.tasks.core.sync.SynchronizationScheduler synchronizationScheduler = new org.eclipse.mylyn.internal.tasks.core.sync.SynchronizationScheduler();

    private static final org.eclipse.mylyn.internal.tasks.ui.util.TaskDropHandler taskDropHandler = new org.eclipse.mylyn.internal.tasks.ui.util.TaskDropHandler();

    /**
     *
     * @deprecated use SWT.SHEET instead
     */
    @java.lang.Deprecated
    public static final int SWT_SHEET = 1 << 28;

    public static final java.lang.String ID_MENU_ACTIVE_TASK = "org.eclipse.mylyn.tasks.ui.menus.activeTask";// $NON-NLS-1$


    private static org.eclipse.core.commands.operations.ObjectUndoContext undoContext;

    public static org.eclipse.mylyn.internal.tasks.ui.wizards.MultiRepositoryAwareWizard createNewTaskWizard(org.eclipse.mylyn.tasks.core.ITaskMapping taskSelection) {
        return new org.eclipse.mylyn.internal.tasks.ui.wizards.NewTaskWizardInternal(taskSelection);
    }

    /**
     * get the connector discovery wizard command. Calling code should check {@link Command#isEnabled()} on return.
     *
     * @return the command, or null if it is not available.
     */
    public static org.eclipse.core.commands.Command getConfiguredDiscoveryWizardCommand() {
        org.eclipse.ui.commands.ICommandService service = ((org.eclipse.ui.commands.ICommandService) (org.eclipse.ui.PlatformUI.getWorkbench().getService(org.eclipse.ui.commands.ICommandService.class)));
        final org.eclipse.core.commands.Command discoveryWizardCommand = service.getCommand("org.eclipse.mylyn.tasks.ui.discoveryWizardCommand");// $NON-NLS-1$

        if (discoveryWizardCommand != null) {
            org.eclipse.ui.handlers.IHandlerService handlerService = ((org.eclipse.ui.handlers.IHandlerService) (org.eclipse.ui.PlatformUI.getWorkbench().getService(org.eclipse.ui.handlers.IHandlerService.class)));
            org.eclipse.core.expressions.EvaluationContext evaluationContext = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.createDiscoveryWizardEvaluationContext(handlerService);
            if (!discoveryWizardCommand.isEnabled()) {
                // update enabled state in case something has changed (ProxyHandler caches state)
                discoveryWizardCommand.setEnabled(evaluationContext);
            }
        }
        return discoveryWizardCommand;
    }

    public static org.eclipse.core.expressions.EvaluationContext createDiscoveryWizardEvaluationContext(org.eclipse.ui.handlers.IHandlerService handlerService) {
        org.eclipse.core.expressions.EvaluationContext evaluationContext = new org.eclipse.core.expressions.EvaluationContext(handlerService.getCurrentState(), org.eclipse.core.runtime.Platform.class);
        // must specify this variable otherwise the PlatformPropertyTester won't work
        evaluationContext.addVariable("platform", org.eclipse.core.runtime.Platform.class);// $NON-NLS-1$

        return evaluationContext;
    }

    public static org.eclipse.jface.resource.ImageDescriptor getPriorityImage(org.eclipse.mylyn.tasks.core.ITask task) {
        if (task.isCompleted()) {
            return org.eclipse.mylyn.commons.ui.CommonImages.COMPLETE;
        } else {
            return org.eclipse.mylyn.tasks.ui.TasksUiImages.getImageDescriptorForPriority(org.eclipse.mylyn.tasks.core.ITask.PriorityLevel.fromString(task.getPriority()));
        }
    }

    public static java.util.List<org.eclipse.mylyn.tasks.ui.editors.TaskEditor> getActiveRepositoryTaskEditors() {
        java.util.List<org.eclipse.mylyn.tasks.ui.editors.TaskEditor> repositoryTaskEditors = new java.util.ArrayList<org.eclipse.mylyn.tasks.ui.editors.TaskEditor>();
        org.eclipse.ui.IWorkbenchWindow[] windows = org.eclipse.ui.PlatformUI.getWorkbench().getWorkbenchWindows();
        for (org.eclipse.ui.IWorkbenchWindow window : windows) {
            org.eclipse.ui.IEditorReference[] editorReferences = window.getActivePage().getEditorReferences();
            for (org.eclipse.ui.IEditorReference editorReference : editorReferences) {
                try {
                    if (editorReference.getEditorInput() instanceof org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput) {
                        org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput input = ((org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput) (editorReference.getEditorInput()));
                        if (input.getTask() != null) {
                            org.eclipse.ui.IEditorPart editorPart = editorReference.getEditor(false);
                            if (editorPart instanceof org.eclipse.mylyn.tasks.ui.editors.TaskEditor) {
                                repositoryTaskEditors.add(((org.eclipse.mylyn.tasks.ui.editors.TaskEditor) (editorPart)));
                            }
                        }
                    }
                } catch (org.eclipse.ui.PartInitException e) {
                    // ignore
                }
            }
        }
        return repositoryTaskEditors;
    }

    public static org.eclipse.core.runtime.IProgressMonitor getUiMonitor(org.eclipse.core.runtime.IProgressMonitor monitor) {
        return new org.eclipse.core.runtime.ProgressMonitorWrapper(monitor) {
            @java.lang.Override
            public void beginTask(final java.lang.String name, final int totalWork) {
                org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                    public void run() {
                        getWrappedProgressMonitor().beginTask(name, totalWork);
                    }
                });
            }

            @java.lang.Override
            public void done() {
                org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                    public void run() {
                        getWrappedProgressMonitor().done();
                    }
                });
            }

            @java.lang.Override
            public void subTask(final java.lang.String name) {
                org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                    public void run() {
                        getWrappedProgressMonitor().subTask(name);
                    }
                });
            }

            @java.lang.Override
            public void worked(final int work) {
                org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                    public void run() {
                        getWrappedProgressMonitor().worked(work);
                    }
                });
            }
        };
    }

    public static void openEditor(org.eclipse.mylyn.internal.tasks.core.TaskCategory category) {
        final java.lang.String name = category.getSummary();
        org.eclipse.jface.dialogs.InputDialog dialog = new org.eclipse.jface.dialogs.InputDialog(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell(), Messages.TasksUiInternal_Rename_Category_Title, Messages.TasksUiInternal_Rename_Category_Message, name, new org.eclipse.jface.dialogs.IInputValidator() {
            public java.lang.String isValid(java.lang.String newName) {
                if ((newName.trim().length() == 0) || newName.equals(name)) {
                    return "";// $NON-NLS-1$

                }
                java.util.Set<org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory> categories = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().getCategories();
                for (org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory category : categories) {
                    if (newName.equals(category.getSummary())) {
                        return Messages.TasksUiInternal_Rename_Category_Name_already_exists_Error;
                    }
                }
                return null;
            }
        });
        if (dialog.open() == org.eclipse.jface.window.Window.OK) {
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().renameContainer(category, dialog.getValue());
        }
    }

    public static void refreshAndOpenTaskListElement(org.eclipse.mylyn.tasks.core.IRepositoryElement element) {
        if (element instanceof org.eclipse.mylyn.tasks.core.ITask) {
            final org.eclipse.mylyn.internal.tasks.core.AbstractTask task = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (element));
            if (task instanceof org.eclipse.mylyn.internal.tasks.core.LocalTask) {
                org.eclipse.mylyn.tasks.ui.TasksUiUtil.openTask(task);
            } else {
                java.lang.String repositoryKind = task.getConnectorKind();
                final org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(repositoryKind);
                org.eclipse.mylyn.tasks.core.TaskRepository repository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(repositoryKind, task.getRepositoryUrl());
                if (repository == null) {
                    org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayStatus(Messages.TasksUiInternal_Failed_to_open_task, new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, Messages.TasksUiInternal_No_repository_found));
                    return;
                }
                if (connector != null) {
                    boolean opened = false;
                    if (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskDataManager().hasTaskData(task)) {
                        opened = org.eclipse.mylyn.tasks.ui.TasksUiUtil.openTask(task);
                    }
                    if (!opened) {
                        if (connector.canSynchronizeTask(repository, task)) {
                            // TODO consider moving this into the editor, i.e. have the editor refresh the task if task data is missing
                            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.synchronizeTask(connector, task, true, new org.eclipse.core.runtime.jobs.JobChangeAdapter() {
                                @java.lang.Override
                                public void done(org.eclipse.core.runtime.jobs.IJobChangeEvent event) {
                                    org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                                        public void run() {
                                            org.eclipse.mylyn.tasks.ui.TasksUiUtil.openTask(task);
                                        }
                                    });
                                }
                            });
                        } else {
                            org.eclipse.mylyn.tasks.ui.TasksUiUtil.openTask(task);
                        }
                    }
                }
            }
        } else if (element instanceof org.eclipse.mylyn.internal.tasks.core.TaskCategory) {
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.openEditor(((org.eclipse.mylyn.internal.tasks.core.TaskCategory) (element)));
        } else if (element instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery) {
            org.eclipse.mylyn.internal.tasks.core.RepositoryQuery query = ((org.eclipse.mylyn.internal.tasks.core.RepositoryQuery) (element));
            org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnectorUi(query.getConnectorKind());
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.openEditQueryDialog(connectorUi, query);
        }
    }

    public static org.eclipse.mylyn.tasks.core.sync.TaskJob updateRepositoryConfiguration(final org.eclipse.mylyn.tasks.core.TaskRepository taskRepository) {
        synchronized(taskRepository) {
            taskRepository.setUpdating(true);
        }
        org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(taskRepository.getConnectorKind());
        final org.eclipse.mylyn.tasks.core.sync.TaskJob job = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getJobFactory().createUpdateRepositoryConfigurationJob(connector, taskRepository, null);
        job.addJobChangeListener(new org.eclipse.core.runtime.jobs.JobChangeAdapter() {
            @java.lang.Override
            public void done(org.eclipse.core.runtime.jobs.IJobChangeEvent event) {
                synchronized(taskRepository) {
                    taskRepository.setUpdating(false);
                }
                if (job.getStatus() != null) {
                    org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.asyncLogAndDisplayStatus(Messages.TasksUiInternal_Configuration_Refresh_Failed, job.getStatus());
                }
            }
        });
        job.schedule();
        return job;
    }

    private static void joinIfInTestMode(org.eclipse.mylyn.tasks.core.sync.SynchronizationJob job) {
        // FIXME the client code should join the job
        if (org.eclipse.mylyn.commons.core.CoreUtil.TEST_MODE) {
            try {
                job.join();
            } catch (java.lang.InterruptedException e) {
                throw new java.lang.RuntimeException(e);
            }
        }
    }

    public static final org.eclipse.core.runtime.jobs.Job synchronizeQueries(org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector, org.eclipse.mylyn.tasks.core.TaskRepository repository, java.util.Set<org.eclipse.mylyn.internal.tasks.core.RepositoryQuery> queries, org.eclipse.core.runtime.jobs.IJobChangeListener listener, boolean force) {
        org.eclipse.core.runtime.Assert.isTrue(queries.size() > 0);
        org.eclipse.mylyn.internal.tasks.core.TaskList taskList = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList();
        for (org.eclipse.mylyn.internal.tasks.core.RepositoryQuery query : queries) {
            query.setSynchronizing(true);
        }
        taskList.notifySynchronizationStateChanged(queries);
        org.eclipse.mylyn.tasks.core.sync.SynchronizationJob job = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskJobFactory().createSynchronizeQueriesJob(connector, repository, queries);
        job.setUser(force);
        if (force) {
            // show the progress in the system task bar if this is a user job (i.e. forced)
            job.setProperty(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.SHOW_IN_TASKBAR_ICON_PROPERTY, java.lang.Boolean.TRUE);
        }
        if (listener != null) {
            job.addJobChangeListener(listener);
        }
        if (force) {
            final org.eclipse.mylyn.internal.tasks.core.RepositoryQuery query = queries.iterator().next();
            job.addJobChangeListener(new org.eclipse.core.runtime.jobs.JobChangeAdapter() {
                @java.lang.Override
                public void done(org.eclipse.core.runtime.jobs.IJobChangeEvent event) {
                    if (query.getStatus() != null) {
                        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.asyncLogAndDisplayStatus(Messages.TasksUiInternal_Query_Synchronization_Failed, query.getStatus());
                    }
                }
            });
        }
        job.schedule();
        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.joinIfInTestMode(job);
        return job;
    }

    /**
     * For synchronizing a single query. Use synchronize(Set, IJobChangeListener) if synchronizing multiple queries at a
     * time.
     */
    public static final org.eclipse.core.runtime.jobs.Job synchronizeQuery(org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector, org.eclipse.mylyn.internal.tasks.core.RepositoryQuery repositoryQuery, org.eclipse.core.runtime.jobs.IJobChangeListener listener, boolean force) {
        org.eclipse.mylyn.tasks.core.TaskRepository repository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(repositoryQuery.getConnectorKind(), repositoryQuery.getRepositoryUrl());
        return org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.synchronizeQueries(connector, repository, java.util.Collections.singleton(repositoryQuery), listener, force);
    }

    public static org.eclipse.mylyn.tasks.core.sync.SynchronizationJob synchronizeAllRepositories(boolean force) {
        org.eclipse.mylyn.tasks.core.sync.SynchronizationJob job = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskJobFactory().createSynchronizeRepositoriesJob(null);
        job.setUser(force);
        if (force) {
            // show the progress in the system task bar if this is a user job (i.e. forced)
            job.setProperty(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.SHOW_IN_TASKBAR_ICON_PROPERTY, java.lang.Boolean.TRUE);
        }
        job.schedule();
        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.joinIfInTestMode(job);
        return job;
    }

    public static void synchronizeRepositoryInBackground(final org.eclipse.mylyn.tasks.core.TaskRepository repository) {
        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.synchronizationScheduler.schedule(repository, new org.eclipse.mylyn.internal.tasks.core.sync.SynchronizationScheduler.Synchronizer<org.eclipse.mylyn.tasks.core.sync.SynchronizationJob>() {
            @java.lang.Override
            public org.eclipse.mylyn.tasks.core.sync.SynchronizationJob createJob() {
                return org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.synchronizeRepositoryInternal(repository, false);
            }
        });
    }

    public static org.eclipse.mylyn.tasks.core.sync.SynchronizationJob synchronizeRepository(org.eclipse.mylyn.tasks.core.TaskRepository repository, boolean force) {
        org.eclipse.mylyn.tasks.core.sync.SynchronizationJob job = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.synchronizeRepositoryInternal(repository, force);
        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.synchronizationScheduler.cancel(repository);
        job.schedule();
        return job;
    }

    private static org.eclipse.mylyn.tasks.core.sync.SynchronizationJob synchronizeRepositoryInternal(org.eclipse.mylyn.tasks.core.TaskRepository repository, boolean force) {
        org.eclipse.mylyn.tasks.core.sync.SynchronizationJob job = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getJobFactory().createSynchronizeRepositoriesJob(java.util.Collections.singleton(repository));
        // do not show in progress view by default
        job.setSystem(true);
        job.setUser(force);
        if (force) {
            // show the progress in the system task bar if this is a user job (i.e. forced)
            job.setProperty(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.SHOW_IN_TASKBAR_ICON_PROPERTY, java.lang.Boolean.TRUE);
        }
        job.setFullSynchronization(false);
        return job;
    }

    public static void synchronizeTaskInBackground(final org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector, final org.eclipse.mylyn.tasks.core.ITask task) {
        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.synchronizationScheduler.schedule(task, new org.eclipse.mylyn.internal.tasks.core.sync.SynchronizationScheduler.Synchronizer<org.eclipse.core.runtime.jobs.Job>() {
            @java.lang.Override
            public org.eclipse.core.runtime.jobs.Job createJob() {
                org.eclipse.mylyn.tasks.core.sync.SynchronizationJob job = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskJobFactory().createSynchronizeTasksJob(connector, java.util.Collections.singleton(task));
                job.setUser(false);
                job.setSystem(true);
                return job;
            }
        });
    }

    /**
     * Synchronize a single task. Note that if you have a collection of tasks to synchronize with this connector then
     * you should call synchronize(Set<Set<AbstractTask> repositoryTasks, ...)
     *
     * @param listener
     * 		can be null
     */
    public static org.eclipse.core.runtime.jobs.Job synchronizeTask(org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector, org.eclipse.mylyn.tasks.core.ITask task, boolean force, org.eclipse.core.runtime.jobs.IJobChangeListener listener) {
        return org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.synchronizeTasks(connector, java.util.Collections.singleton(task), force, listener);
    }

    /**
     *
     * @param listener
     * 		can be null
     */
    public static org.eclipse.core.runtime.jobs.Job synchronizeTasks(org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector, java.util.Set<org.eclipse.mylyn.tasks.core.ITask> tasks, boolean force, org.eclipse.core.runtime.jobs.IJobChangeListener listener) {
        org.eclipse.mylyn.internal.tasks.core.ITaskList taskList = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList();
        for (org.eclipse.mylyn.tasks.core.ITask task : tasks) {
            ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (task)).setSynchronizing(true);
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.synchronizationScheduler.cancel(task);
        }
        ((org.eclipse.mylyn.internal.tasks.core.TaskList) (taskList)).notifySynchronizationStateChanged(tasks);
        // TODO notify task list?
        org.eclipse.mylyn.tasks.core.sync.SynchronizationJob job = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskJobFactory().createSynchronizeTasksJob(connector, tasks);
        job.setUser(force);
        job.setSystem(!force);
        job.setPriority(org.eclipse.core.runtime.jobs.Job.DECORATE);
        if (listener != null) {
            job.addJobChangeListener(listener);
        }
        if (force && (tasks.size() == 1)) {
            final org.eclipse.mylyn.tasks.core.ITask task = tasks.iterator().next();
            job.addJobChangeListener(new org.eclipse.core.runtime.jobs.JobChangeAdapter() {
                @java.lang.Override
                public void done(org.eclipse.core.runtime.jobs.IJobChangeEvent event) {
                    if ((task instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask) && (((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (task)).getStatus() != null)) {
                        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.asyncLogAndDisplayStatus(Messages.TasksUiInternal_Task_Synchronization_Failed, ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (task)).getStatus());
                    }
                }
            });
        }
        job.schedule();
        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.joinIfInTestMode(job);
        return job;
    }

    public static org.eclipse.mylyn.internal.tasks.core.ITaskJobFactory getJobFactory() {
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskJobFactory();
    }

    public static org.eclipse.mylyn.internal.tasks.ui.wizards.NewAttachmentWizardDialog openNewAttachmentWizard(org.eclipse.swt.widgets.Shell shell, org.eclipse.mylyn.tasks.core.TaskRepository taskRepository, org.eclipse.mylyn.tasks.core.ITask task, org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute, org.eclipse.mylyn.internal.tasks.ui.wizards.TaskAttachmentWizard.Mode mode, org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource source) {
        org.eclipse.mylyn.internal.tasks.ui.wizards.TaskAttachmentWizard attachmentWizard = new org.eclipse.mylyn.internal.tasks.ui.wizards.TaskAttachmentWizard(taskRepository, task, taskAttribute);
        attachmentWizard.setSource(source);
        attachmentWizard.setMode(mode);
        org.eclipse.mylyn.internal.tasks.ui.wizards.NewAttachmentWizardDialog dialog = new org.eclipse.mylyn.internal.tasks.ui.wizards.NewAttachmentWizardDialog(shell, attachmentWizard, false);
        dialog.setBlockOnOpen(false);
        dialog.create();
        dialog.open();
        return dialog;
    }

    private static org.eclipse.jface.dialogs.MessageDialog createDialog(org.eclipse.swt.widgets.Shell shell, java.lang.String title, java.lang.String message, int type) {
        return new org.eclipse.jface.dialogs.MessageDialog(shell, title, null, message, type, new java.lang.String[]{ org.eclipse.jface.dialogs.IDialogConstants.OK_LABEL }, 0);
    }

    private static void displayStatus(org.eclipse.swt.widgets.Shell shell, final java.lang.String title, final org.eclipse.core.runtime.IStatus status, boolean showLinkToErrorLog) {
        // avoid blocking ui when in test mode
        if (org.eclipse.mylyn.commons.core.CoreUtil.TEST_MODE) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(status);
            return;
        }
        if ((status instanceof org.eclipse.mylyn.tasks.core.RepositoryStatus) && ((org.eclipse.mylyn.tasks.core.RepositoryStatus) (status)).isHtmlMessage()) {
            org.eclipse.mylyn.internal.tasks.ui.util.WebBrowserDialog.openAcceptAgreement(shell, title, status.getMessage(), ((org.eclipse.mylyn.tasks.core.RepositoryStatus) (status)).getHtmlMessage());
        } else {
            java.lang.String message = status.getMessage();
            if ((message == null) || (message.trim().length() == 0)) {
                message = Messages.TasksUiInternal_An_unknown_error_occurred;
            }
            if (message.length() > 256) {
                message = message.substring(0, 256) + "...";// $NON-NLS-1$

            }
            if (showLinkToErrorLog) {
                message += "\n\n" + Messages.TasksUiInternal_See_error_log_for_details;// $NON-NLS-1$

            }
            switch (status.getSeverity()) {
                case org.eclipse.core.runtime.IStatus.CANCEL :
                case org.eclipse.core.runtime.IStatus.INFO :
                    org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.createDialog(shell, title, message, org.eclipse.jface.dialogs.MessageDialog.INFORMATION).open();
                    break;
                case org.eclipse.core.runtime.IStatus.WARNING :
                    org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.createDialog(shell, title, message, org.eclipse.jface.dialogs.MessageDialog.WARNING).open();
                    break;
                case org.eclipse.core.runtime.IStatus.ERROR :
                default :
                    org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.createDialog(shell, title, message, org.eclipse.jface.dialogs.MessageDialog.ERROR).open();
                    break;
            }
        }
    }

    public static void asyncDisplayStatus(final java.lang.String title, final org.eclipse.core.runtime.IStatus status) {
        org.eclipse.swt.widgets.Display display = org.eclipse.ui.PlatformUI.getWorkbench().getDisplay();
        if (!display.isDisposed()) {
            display.asyncExec(new java.lang.Runnable() {
                public void run() {
                    org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayStatus(title, status);
                }
            });
        } else {
            org.eclipse.mylyn.commons.core.StatusHandler.log(status);
        }
    }

    public static void asyncLogAndDisplayStatus(final java.lang.String title, final org.eclipse.core.runtime.IStatus status) {
        org.eclipse.swt.widgets.Display display = org.eclipse.ui.PlatformUI.getWorkbench().getDisplay();
        if (!display.isDisposed()) {
            display.asyncExec(new java.lang.Runnable() {
                public void run() {
                    org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.logAndDisplayStatus(title, status);
                }
            });
        } else {
            org.eclipse.mylyn.commons.core.StatusHandler.log(status);
        }
    }

    public static void logAndDisplayStatus(final java.lang.String title, final org.eclipse.core.runtime.IStatus status) {
        org.eclipse.mylyn.commons.core.StatusHandler.log(status);
        org.eclipse.ui.IWorkbench workbench = org.eclipse.ui.PlatformUI.getWorkbench();
        if ((workbench != null) && (!workbench.getDisplay().isDisposed())) {
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayStatus(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell(), title, status, false);
        }
    }

    public static void displayStatus(final java.lang.String title, final org.eclipse.core.runtime.IStatus status) {
        org.eclipse.ui.IWorkbench workbench = org.eclipse.ui.PlatformUI.getWorkbench();
        if ((workbench != null) && (!workbench.getDisplay().isDisposed())) {
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayStatus(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell(), title, status, false);
        } else {
            org.eclipse.mylyn.commons.core.StatusHandler.log(status);
        }
    }

    /**
     * Creates a new local task and schedules for today
     *
     * @param summary
     * 		if null DEFAULT_SUMMARY (New Task) used.
     */
    public static org.eclipse.mylyn.internal.tasks.core.LocalTask createNewLocalTask(java.lang.String summary) {
        if (summary == null) {
            summary = org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector.DEFAULT_SUMMARY;
        }
        org.eclipse.mylyn.internal.tasks.core.TaskList taskList = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList();
        org.eclipse.mylyn.internal.tasks.core.LocalTask newTask = new org.eclipse.mylyn.internal.tasks.core.LocalTask("" + taskList.getNextLocalTaskId(), summary);// $NON-NLS-1$

        newTask.setPriority(org.eclipse.mylyn.tasks.core.ITask.PriorityLevel.P3.toString());
        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().addTask(newTask);
        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.scheduleNewTask(newTask);
        org.eclipse.mylyn.internal.tasks.ui.views.TaskListView view = org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.getFromActivePerspective();
        org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory category = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getSelectedCategory(view);
        if (((view != null) && (view.getDrilledIntoCategory() != null)) && (view.getDrilledIntoCategory() != category)) {
            org.eclipse.jface.dialogs.MessageDialog.openInformation(org.eclipse.swt.widgets.Display.getCurrent().getActiveShell(), Messages.TasksUiInternal_Create_Task, java.text.MessageFormat.format(Messages.TasksUiInternal_The_new_task_will_be_added_to_the_X_container, org.eclipse.mylyn.internal.tasks.core.UncategorizedTaskContainer.LABEL));
        }
        taskList.addTask(newTask, category);
        return newTask;
    }

    /**
     * Schedules the new task according to the Tasks UI preferences
     *
     * @param newTask
     * 		the task to schedule
     */
    public static void scheduleNewTask(org.eclipse.mylyn.internal.tasks.core.LocalTask newTask) {
        org.eclipse.jface.preference.IPreferenceStore preferenceStore = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore();
        java.lang.String preference = preferenceStore.getString(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.SCHEDULE_NEW_TASKS_FOR);
        org.eclipse.mylyn.internal.tasks.core.DateRange dateRange = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getNewTaskScheduleDateRange(preference);
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().scheduleNewTask(newTask, dateRange);
    }

    private static org.eclipse.mylyn.internal.tasks.core.DateRange getNewTaskScheduleDateRange(java.lang.String preference) {
        switch (preference) {
            case org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.SCHEDULE_NEW_TASKS_FOR_NOT_SCHEDULED :
                return null;
            case org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.SCHEDULE_NEW_TASKS_FOR_TODAY :
                return org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil.getCurrentWeek().getToday();
            case org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.SCHEDULE_NEW_TASKS_FOR_TOMORROW :
                return org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil.getCurrentWeek().getToday().next();
            case org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.SCHEDULE_NEW_TASKS_FOR_THIS_WEEK :
                return org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil.getCurrentWeek();
        }
        return org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil.getCurrentWeek();
    }

    public static org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory getSelectedCategory(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView view) {
        java.lang.Object selectedObject = null;
        if (view != null) {
            selectedObject = ((org.eclipse.jface.viewers.IStructuredSelection) (view.getViewer().getSelection())).getFirstElement();
        }
        if (selectedObject instanceof org.eclipse.mylyn.internal.tasks.core.TaskCategory) {
            return ((org.eclipse.mylyn.internal.tasks.core.TaskCategory) (selectedObject));
        } else if (selectedObject instanceof org.eclipse.mylyn.tasks.core.ITask) {
            org.eclipse.mylyn.tasks.core.ITask task = ((org.eclipse.mylyn.tasks.core.ITask) (selectedObject));
            org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer container = org.eclipse.mylyn.internal.tasks.core.TaskCategory.getParentTaskCategory(task);
            if (container instanceof org.eclipse.mylyn.internal.tasks.core.TaskCategory) {
                return ((org.eclipse.mylyn.internal.tasks.core.TaskCategory) (container));
            } else if ((view != null) && (view.getDrilledIntoCategory() instanceof org.eclipse.mylyn.internal.tasks.core.TaskCategory)) {
                return ((org.eclipse.mylyn.internal.tasks.core.TaskCategory) (view.getDrilledIntoCategory()));
            }
        } else if ((view != null) && (view.getDrilledIntoCategory() instanceof org.eclipse.mylyn.internal.tasks.core.TaskCategory)) {
            return ((org.eclipse.mylyn.internal.tasks.core.TaskCategory) (view.getDrilledIntoCategory()));
        }
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().getDefaultCategory();
    }

    public static java.util.Set<org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer> getContainersFromWorkingSet(java.util.Set<org.eclipse.ui.IWorkingSet> containers) {
        java.util.Set<org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer> allTaskContainersInWorkingSets = new java.util.HashSet<org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer>();
        for (org.eclipse.ui.IWorkingSet workingSet : containers) {
            org.eclipse.core.runtime.IAdaptable[] elements = workingSet.getElements();
            for (org.eclipse.core.runtime.IAdaptable adaptable : elements) {
                if (adaptable instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer) {
                    allTaskContainersInWorkingSets.add(((org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer) (adaptable)));
                }
            }
        }
        return allTaskContainersInWorkingSets;
    }

    /**
     *
     * @param connectorUi
     * 		- repository connector ui
     * @param query
     * 		- repository query
     * @return - true if dialog was opened successfully and not canceled, false otherwise
     * @since 3.0
     */
    public static boolean openEditQueryDialog(org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi, final org.eclipse.mylyn.tasks.core.IRepositoryQuery query) {
        try {
            org.eclipse.mylyn.tasks.core.TaskRepository repository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(query.getConnectorKind(), query.getRepositoryUrl());
            if (repository == null) {
                return false;
            }
            org.eclipse.jface.wizard.IWizard wizard = connectorUi.getQueryWizard(repository, query);
            org.eclipse.swt.widgets.Shell shell = org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
            if (((wizard != null) && (shell != null)) && (!shell.isDisposed())) {
                org.eclipse.mylyn.internal.tasks.ui.wizards.QueryWizardDialog dialog = new org.eclipse.mylyn.internal.tasks.ui.wizards.QueryWizardDialog(shell, wizard) {
                    private static final java.lang.String DIALOG_SETTINGS = "EditQueryWizardWizard";// $NON-NLS-1$


                    @java.lang.Override
                    protected org.eclipse.jface.dialogs.IDialogSettings getDialogBoundsSettings() {
                        org.eclipse.jface.dialogs.IDialogSettings settings = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getDialogSettings();
                        java.lang.String settingsSectionId = (DIALOG_SETTINGS + '.') + query.getRepositoryUrl();
                        org.eclipse.jface.dialogs.IDialogSettings section = settings.getSection(settingsSectionId);
                        if (section == null) {
                            section = settings.addNewSection(settingsSectionId);
                        }
                        return section;
                    }
                };
                dialog.create();
                dialog.setBlockOnOpen(true);
                if (dialog.open() == org.eclipse.jface.window.Window.CANCEL) {
                    return false;
                } else {
                    return true;
                }
            }
        } catch (java.lang.Exception e) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Failed to open query dialog", e));// $NON-NLS-1$

        }
        return false;
    }

    public static org.eclipse.mylyn.internal.tasks.core.ITaskList getTaskList() {
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList();
    }

    public static boolean isAnimationsEnabled() {
        org.eclipse.jface.preference.IPreferenceStore store = org.eclipse.ui.PlatformUI.getPreferenceStore();
        return store.getBoolean(org.eclipse.ui.IWorkbenchPreferenceConstants.ENABLE_ANIMATIONS);
    }

    public static boolean hasValidUrl(org.eclipse.mylyn.tasks.core.ITask task) {
        return org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.isValidUrl(task.getUrl());
    }

    public static boolean isValidUrl(java.lang.String url) {
        if ((((url != null) && (!url.equals(""))) && (!url.equals("http://"))) && (!url.equals("https://"))) {
            // $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            try {
                new java.net.URL(url);
                return true;
            } catch (java.net.MalformedURLException e) {
                return false;
            }
        }
        return false;
    }

    /**
     *
     * @deprecated use {@link #closeTaskEditorInAllPages(ITask, boolean)}
     */
    @java.lang.Deprecated
    public static void closeEditorInActivePage(org.eclipse.mylyn.tasks.core.ITask task, boolean save) {
        org.eclipse.core.runtime.Assert.isNotNull(task);
        org.eclipse.ui.IWorkbenchWindow window = org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return;
        }
        org.eclipse.ui.IWorkbenchPage page = window.getActivePage();
        if (page == null) {
            return;
        }
        org.eclipse.mylyn.tasks.core.TaskRepository taskRepository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(task.getConnectorKind(), task.getRepositoryUrl());
        org.eclipse.ui.IEditorInput input = new org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput(taskRepository, task);
        org.eclipse.ui.IEditorPart editor = page.findEditor(input);
        if (editor != null) {
            page.closeEditor(editor, save);
        }
    }

    public static void closeTaskEditorInAllPages(org.eclipse.mylyn.tasks.core.ITask task, boolean save) {
        org.eclipse.core.runtime.Assert.isNotNull(task);
        org.eclipse.mylyn.tasks.core.TaskRepository taskRepository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(task.getConnectorKind(), task.getRepositoryUrl());
        org.eclipse.ui.IEditorInput input = new org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput(taskRepository, task);
        org.eclipse.ui.IWorkbenchWindow[] windows = org.eclipse.ui.PlatformUI.getWorkbench().getWorkbenchWindows();
        for (org.eclipse.ui.IWorkbenchWindow window : windows) {
            org.eclipse.ui.IWorkbenchPage[] pages = window.getPages();
            for (org.eclipse.ui.IWorkbenchPage page : pages) {
                org.eclipse.ui.IEditorPart editor = page.findEditor(input);
                if (editor != null) {
                    page.closeEditor(editor, save);
                }
            }
        }
    }

    public static boolean hasLocalCompletionState(org.eclipse.mylyn.tasks.core.ITask task) {
        org.eclipse.mylyn.tasks.core.TaskRepository taskRepository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(task.getConnectorKind(), task.getRepositoryUrl());
        org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(task.getConnectorKind());
        return connector.hasLocalCompletionState(taskRepository, task);
    }

    /**
     * Utility method to get the best parenting possible for a dialog. If there is a modal shell create it so as to
     * avoid two modal dialogs. If not then return the shell of the active workbench window. If neither can be found
     * return null.
     * <p>
     * <b>Note: Applied from patch on bug 99472.</b>
     *
     * @return Shell or <code>null</code>
     * @deprecated Use {@link WorkbenchUtil#getShell()} instead
     */
    @java.lang.Deprecated
    public static org.eclipse.swt.widgets.Shell getShell() {
        return org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell();
    }

    public static org.eclipse.mylyn.tasks.core.data.TaskData createTaskData(org.eclipse.mylyn.tasks.core.TaskRepository taskRepository, org.eclipse.mylyn.tasks.core.ITaskMapping initializationData, org.eclipse.mylyn.tasks.core.ITaskMapping selectionData, org.eclipse.core.runtime.IProgressMonitor monitor) throws org.eclipse.core.runtime.CoreException {
        org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(taskRepository.getConnectorKind());
        org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler taskDataHandler = connector.getTaskDataHandler();
        org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper mapper = taskDataHandler.getAttributeMapper(taskRepository);
        org.eclipse.mylyn.tasks.core.data.TaskData taskData = new org.eclipse.mylyn.tasks.core.data.TaskData(mapper, taskRepository.getConnectorKind(), taskRepository.getRepositoryUrl(), "");// $NON-NLS-1$

        boolean result = taskDataHandler.initializeTaskData(taskRepository, taskData, initializationData, monitor);
        if (!result) {
            throw new org.eclipse.core.runtime.CoreException(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Initialization of task failed. The provided data is insufficient."));// $NON-NLS-1$

        }
        if (selectionData != null) {
            connector.getTaskMapping(taskData).merge(selectionData);
        }
        return taskData;
    }

    public static void createAndOpenNewTask(org.eclipse.mylyn.tasks.core.data.TaskData taskData) throws org.eclipse.core.runtime.CoreException {
        org.eclipse.mylyn.tasks.core.ITask task = org.eclipse.mylyn.tasks.ui.TasksUiUtil.createOutgoingNewTask(taskData.getConnectorKind(), taskData.getRepositoryUrl());
        org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(taskData.getConnectorKind());
        org.eclipse.mylyn.tasks.core.ITaskMapping mapping = connector.getTaskMapping(taskData);
        java.lang.String summary = mapping.getSummary();
        if ((summary != null) && (summary.length() > 0)) {
            task.setSummary(summary);
        }
        java.lang.String taskKind = mapping.getTaskKind();
        if ((taskKind != null) && (taskKind.length() > 0)) {
            task.setTaskKind(taskKind);
        }
        org.eclipse.mylyn.internal.tasks.core.UnsubmittedTaskContainer unsubmitted = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().getUnsubmittedContainer(taskData.getRepositoryUrl());
        if (unsubmitted != null) {
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().addTask(task, unsubmitted);
        }
        org.eclipse.mylyn.tasks.core.data.ITaskDataWorkingCopy workingCopy = org.eclipse.mylyn.tasks.ui.TasksUi.getTaskDataManager().createWorkingCopy(task, taskData);
        workingCopy.save(null, null);
        org.eclipse.mylyn.tasks.core.TaskRepository taskRepository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(taskData.getConnectorKind(), taskData.getRepositoryUrl());
        connector.updateNewTaskFromTaskData(taskRepository, task, taskData);
        org.eclipse.mylyn.tasks.core.TaskRepository localTaskRepository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(task.getConnectorKind(), task.getRepositoryUrl());
        org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput editorInput = new org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput(localTaskRepository, task);
        org.eclipse.mylyn.tasks.ui.TasksUiUtil.openEditor(editorInput, org.eclipse.mylyn.tasks.ui.editors.TaskEditor.ID_EDITOR, null);
    }

    public static boolean openTask(org.eclipse.mylyn.tasks.core.TaskRepository repository, java.lang.String taskId, org.eclipse.mylyn.internal.tasks.ui.util.TaskOpenListener listener) {
        org.eclipse.mylyn.internal.tasks.core.AbstractTask task = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskFromTaskList(repository, taskId);
        if (task != null) {
            // task is known, open in task editor
            return org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.openKnownTaskInEditor(task, taskId, listener);
        } else {
            // search for task
            org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnectorUi(repository.getConnectorKind());
            if (connectorUi != null) {
                try {
                    return org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.openRepositoryTask(connectorUi.getConnectorKind(), repository.getRepositoryUrl(), taskId, listener);
                } catch (java.lang.Exception e) {
                    org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Internal error while opening repository task", e));// $NON-NLS-1$

                }
            }
        }
        return false;
    }

    public static boolean openTaskByIdOrKey(org.eclipse.mylyn.tasks.core.TaskRepository repository, java.lang.String taskIdOrKey, org.eclipse.mylyn.internal.tasks.ui.util.TaskOpenListener listener) {
        org.eclipse.mylyn.internal.tasks.core.AbstractTask task = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskFromTaskList(repository, taskIdOrKey);
        if (task != null) {
            // task is known, open in task editor
            return org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.openKnownTaskInEditor(task, taskIdOrKey, listener);
        } else {
            // search for task
            org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnectorUi(repository.getConnectorKind());
            if (connectorUi != null) {
                try {
                    return org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.openRepositoryTaskByIdOrKey(connectorUi.getConnectorKind(), repository.getRepositoryUrl(), taskIdOrKey, listener);
                } catch (java.lang.Exception e) {
                    org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Internal error while opening repository task", e));// $NON-NLS-1$

                }
            }
        }
        return false;
    }

    private static boolean openKnownTaskInEditor(org.eclipse.mylyn.internal.tasks.core.AbstractTask task, java.lang.String taskIdOrKey, org.eclipse.mylyn.internal.tasks.ui.util.TaskOpenListener listener) {
        org.eclipse.mylyn.internal.tasks.ui.util.TaskOpenEvent event = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.openTask(task, taskIdOrKey);
        if ((listener != null) && (event != null)) {
            listener.taskOpened(event);
        }
        return event != null;
    }

    private static org.eclipse.mylyn.internal.tasks.core.AbstractTask getTaskFromTaskList(org.eclipse.mylyn.tasks.core.TaskRepository repository, java.lang.String taskIdOrKey) {
        org.eclipse.core.runtime.Assert.isNotNull(repository);
        org.eclipse.core.runtime.Assert.isNotNull(taskIdOrKey);
        org.eclipse.mylyn.internal.tasks.core.AbstractTask task = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().getTask(repository.getRepositoryUrl(), taskIdOrKey)));
        if (task == null) {
            task = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().getTaskByKey(repository.getRepositoryUrl(), taskIdOrKey);
        }
        return task;
    }

    public static org.eclipse.mylyn.internal.tasks.ui.util.TaskOpenEvent openTask(org.eclipse.mylyn.tasks.core.ITask task, java.lang.String taskId) {
        org.eclipse.core.runtime.Assert.isNotNull(task);
        org.eclipse.ui.IWorkbenchWindow window = org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            org.eclipse.mylyn.tasks.core.TaskRepository taskRepository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(task.getConnectorKind(), task.getRepositoryUrl());
            boolean openWithBrowser = !org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.EDITOR_TASKS_RICH);
            if (openWithBrowser && (!(task instanceof org.eclipse.mylyn.internal.tasks.core.LocalTask))) {
                org.eclipse.mylyn.tasks.ui.TasksUiUtil.openWithBrowser(taskRepository, task);
                return new org.eclipse.mylyn.internal.tasks.ui.util.TaskOpenEvent(taskRepository, task, taskId, null, true);
            } else {
                org.eclipse.ui.IEditorInput editorInput = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskEditorInput(taskRepository, task);
                org.eclipse.ui.IEditorPart editor = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.refreshEditorContentsIfOpen(task, editorInput);
                if (editor != null) {
                    org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.synchronizeTask(taskRepository, task);
                    return new org.eclipse.mylyn.internal.tasks.ui.util.TaskOpenEvent(taskRepository, task, taskId, editor, false);
                } else {
                    org.eclipse.ui.IWorkbenchPage page = window.getActivePage();
                    editor = org.eclipse.mylyn.tasks.ui.TasksUiUtil.openEditor(editorInput, org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskEditorId(task), page);
                    if (editor != null) {
                        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.synchronizeTask(taskRepository, task);
                        return new org.eclipse.mylyn.internal.tasks.ui.util.TaskOpenEvent(taskRepository, task, taskId, editor, false);
                    }
                }
            }
        } else {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, ("Unable to open editor for \""// $NON-NLS-1$
             + task.getSummary()) + "\": no active workbench window"));// $NON-NLS-1$

        }
        return null;
    }

    private static org.eclipse.ui.IEditorInput getTaskEditorInput(org.eclipse.mylyn.tasks.core.TaskRepository repository, org.eclipse.mylyn.tasks.core.ITask task) {
        org.eclipse.core.runtime.Assert.isNotNull(task);
        org.eclipse.core.runtime.Assert.isNotNull(repository);
        org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnectorUi(task.getConnectorKind());
        org.eclipse.ui.IEditorInput editorInput = connectorUi.getTaskEditorInput(repository, task);
        if (editorInput != null) {
            return editorInput;
        } else {
            return new org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput(repository, task);
        }
    }

    private static java.lang.String getTaskEditorId(final org.eclipse.mylyn.tasks.core.ITask task) {
        java.lang.String taskEditorId = org.eclipse.mylyn.tasks.ui.editors.TaskEditor.ID_EDITOR;
        if (task != null) {
            org.eclipse.mylyn.tasks.core.ITask repositoryTask = task;
            org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi repositoryUi = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnectorUi(repositoryTask.getConnectorKind());
            java.lang.String customTaskEditorId = repositoryUi.getTaskEditorId(repositoryTask);
            if (customTaskEditorId != null) {
                taskEditorId = customTaskEditorId;
            }
        }
        return taskEditorId;
    }

    /**
     * If task is already open and has incoming, must force refresh in place
     */
    private static org.eclipse.ui.IEditorPart refreshEditorContentsIfOpen(org.eclipse.mylyn.tasks.core.ITask task, org.eclipse.ui.IEditorInput editorInput) {
        if (task != null) {
            if ((task.getSynchronizationState() == org.eclipse.mylyn.tasks.core.ITask.SynchronizationState.INCOMING) || (task.getSynchronizationState() == org.eclipse.mylyn.tasks.core.ITask.SynchronizationState.CONFLICT)) {
                for (org.eclipse.mylyn.tasks.ui.editors.TaskEditor editor : org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getActiveRepositoryTaskEditors()) {
                    if (editor.getEditorInput().equals(editorInput)) {
                        editor.refreshPages();
                        editor.getEditorSite().getPage().activate(editor);
                        return editor;
                    }
                }
            }
        }
        return null;
    }

    private static void synchronizeTask(org.eclipse.mylyn.tasks.core.TaskRepository taskRepository, org.eclipse.mylyn.tasks.core.ITask task) {
        if (task instanceof org.eclipse.mylyn.internal.tasks.core.LocalTask) {
            return;
        }
        org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(task.getConnectorKind());
        if (connector.canSynchronizeTask(taskRepository, task)) {
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.synchronizeTaskInBackground(connector, task);
        }
    }

    public static boolean openRepositoryTask(java.lang.String connectorKind, java.lang.String repositoryUrl, java.lang.String id) {
        return org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.openRepositoryTask(connectorKind, repositoryUrl, id, null);
    }

    /**
     * Only override if task should be opened by a custom editor, default behavior is to open with a rich editor,
     * falling back to the web browser if not available.
     *
     * @return true if the task was successfully opened
     */
    public static boolean openRepositoryTask(java.lang.String connectorKind, java.lang.String repositoryUrl, java.lang.String id, org.eclipse.mylyn.internal.tasks.ui.util.TaskOpenListener listener) {
        return org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.openRepositoryTask(connectorKind, repositoryUrl, id, listener, 0);
    }

    /**
     * Only override if task should be opened by a custom editor, default behavior is to open with a rich editor,
     * falling back to the web browser if not available.
     *
     * @return true if the task was successfully opened
     */
    public static boolean openRepositoryTask(java.lang.String connectorKind, java.lang.String repositoryUrl, java.lang.String id, org.eclipse.mylyn.internal.tasks.ui.util.TaskOpenListener listener, long timestamp) {
        java.lang.String taskUrl = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskUrl(connectorKind, repositoryUrl, id);
        if (taskUrl == null) {
            return false;
        }
        org.eclipse.ui.IWorkbenchWindow window = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getWorkbenchWindow();
        if (window == null) {
            return false;
        }
        org.eclipse.ui.IWorkbenchPage page = window.getActivePage();
        org.eclipse.mylyn.internal.tasks.ui.OpenRepositoryTaskJob job = new org.eclipse.mylyn.internal.tasks.ui.OpenRepositoryTaskJob(connectorKind, repositoryUrl, id, taskUrl, timestamp, page);
        job.setListener(listener);
        job.schedule();
        return true;
    }

    /**
     * Opens a task with a given search string that can be a task id or task key.
     *
     * @return true if the task was successfully opened
     */
    public static boolean openRepositoryTaskByIdOrKey(java.lang.String connectorKind, java.lang.String repositoryUrl, java.lang.String idOrKey, org.eclipse.mylyn.internal.tasks.ui.util.TaskOpenListener listener) {
        java.lang.String taskUrl = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskUrl(connectorKind, repositoryUrl, idOrKey);
        if (taskUrl == null) {
            return false;
        }
        org.eclipse.ui.IWorkbenchWindow window = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getWorkbenchWindow();
        if (window == null) {
            return false;
        }
        org.eclipse.ui.IWorkbenchPage page = window.getActivePage();
        org.eclipse.mylyn.internal.tasks.ui.OpenRepositoryTaskJob job = new org.eclipse.mylyn.internal.tasks.ui.OpenRepositoryTaskJob(page, connectorKind, repositoryUrl, idOrKey, taskUrl);
        job.setListener(listener);
        job.schedule();
        return true;
    }

    private static org.eclipse.ui.IWorkbenchWindow getWorkbenchWindow() {
        org.eclipse.ui.IWorkbenchWindow window = org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            org.eclipse.ui.IWorkbenchWindow[] windows = org.eclipse.ui.PlatformUI.getWorkbench().getWorkbenchWindows();
            if ((windows != null) && (windows.length > 0)) {
                window = windows[0];
            }
        }
        return window;
    }

    private static java.lang.String getTaskUrl(java.lang.String connectorKind, java.lang.String repositoryUrl, java.lang.String idOrKey) {
        org.eclipse.mylyn.tasks.core.IRepositoryManager repositoryManager = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager();
        org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = repositoryManager.getRepositoryConnector(connectorKind);
        return connector.getTaskUrl(repositoryUrl, idOrKey);
    }

    /**
     *
     * @since 3.0
     */
    public static boolean openTaskInBackground(org.eclipse.mylyn.tasks.core.ITask task, boolean bringToTop) {
        org.eclipse.ui.IWorkbenchWindow window = org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            org.eclipse.ui.IEditorPart activeEditor = null;
            org.eclipse.ui.IWorkbenchPart activePart = null;
            org.eclipse.ui.IWorkbenchPage activePage = window.getActivePage();
            if (activePage != null) {
                activeEditor = activePage.getActiveEditor();
                activePart = activePage.getActivePart();
            }
            boolean opened = org.eclipse.mylyn.tasks.ui.TasksUiUtil.openTask(task);
            if (opened && (activePage != null)) {
                if ((!bringToTop) && (activeEditor != null)) {
                    activePage.bringToTop(activeEditor);
                }
                if (activePart != null) {
                    activePage.activate(activePart);
                }
            }
            return opened;
        } else {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, ("Unable to open editor for \""// $NON-NLS-1$
             + task.getSummary()) + "\": no active workbench window"));// $NON-NLS-1$

        }
        return false;
    }

    /**
     * Returns text masking the &amp;-character from decoration as an accelerator in SWT labels.
     *
     * @deprecated Use {@link CommonUiUtil#toLabel(String)} instead
     */
    @java.lang.Deprecated
    public static java.lang.String escapeLabelText(java.lang.String text) {
        return org.eclipse.mylyn.commons.ui.CommonUiUtil.toLabel(text);
    }

    public static void preservingSelection(final org.eclipse.jface.viewers.TreeViewer viewer, java.lang.Runnable runnable) {
        final org.eclipse.jface.viewers.ISelection selection = viewer.getSelection();
        runnable.run();
        if (selection != null) {
            org.eclipse.jface.viewers.ISelection newSelection = viewer.getSelection();
            if (((newSelection == null) || newSelection.isEmpty()) && (!((selection == null) || selection.isEmpty()))) {
                // delay execution to ensure that any delayed tree updates such as expand all have been processed and the selection is revealed properly
                org.eclipse.swt.widgets.Display.getDefault().asyncExec(new java.lang.Runnable() {
                    public void run() {
                        viewer.setSelection(selection, true);
                    }
                });
            } else if ((newSelection instanceof org.eclipse.jface.viewers.ITreeSelection) && (!newSelection.isEmpty())) {
                viewer.reveal(((org.eclipse.jface.viewers.ITreeSelection) (newSelection)).getFirstElement());
            }
        }
    }

    public static java.lang.String getFormattedDuration(long duration, boolean includeSeconds) {
        long seconds = duration / 1000;
        long minutes = 0;
        long hours = 0;
        // final long SECOND = 1000;
        final long MIN = 60;
        final long HOUR = MIN * 60;
        java.lang.String formatted = "";// $NON-NLS-1$

        java.lang.String hour = "";// $NON-NLS-1$

        java.lang.String min = "";// $NON-NLS-1$

        java.lang.String sec = "";// $NON-NLS-1$

        if (seconds >= HOUR) {
            hours = seconds / HOUR;
            if (hours == 1) {
                hour = hours + Messages.TasksUiInternal__hour_;
            } else if (hours > 1) {
                hour = hours + Messages.TasksUiInternal__hours_;
            }
            seconds -= hours * HOUR;
            minutes = seconds / MIN;
            if (minutes == 1) {
                min = minutes + Messages.TasksUiInternal__minute_;
            } else if (minutes != 1) {
                min = minutes + Messages.TasksUiInternal__minutes_;
            }
            seconds -= minutes * MIN;
            if (seconds == 1) {
                sec = seconds + Messages.TasksUiInternal__second;
            } else if (seconds > 1) {
                sec = seconds + Messages.TasksUiInternal__seconds;
            }
            formatted += hour + min;
            if (includeSeconds) {
                formatted += sec;
            }
        } else if (seconds >= MIN) {
            minutes = seconds / MIN;
            if (minutes == 1) {
                min = minutes + Messages.TasksUiInternal__minute_;
            } else if (minutes != 1) {
                min = minutes + Messages.TasksUiInternal__minutes_;
            }
            seconds -= minutes * MIN;
            if (seconds == 1) {
                sec = seconds + Messages.TasksUiInternal__second;
            } else if (seconds > 1) {
                sec = seconds + Messages.TasksUiInternal__seconds;
            }
            formatted += min;
            if (includeSeconds) {
                formatted += sec;
            }
        } else {
            if (seconds == 1) {
                sec = seconds + Messages.TasksUiInternal__second;
            } else if (seconds > 1) {
                sec = seconds + Messages.TasksUiInternal__seconds;
            }
            if (includeSeconds) {
                formatted += sec;
            }
        }
        return formatted;
    }

    public static org.eclipse.mylyn.internal.tasks.core.AbstractTask getTask(java.lang.String repositoryUrl, java.lang.String taskId, java.lang.String fullUrl) {
        org.eclipse.mylyn.internal.tasks.core.AbstractTask task = null;
        if ((repositoryUrl != null) && (taskId != null)) {
            task = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().getTask(repositoryUrl, taskId)));
        }
        if ((task == null) && (fullUrl != null)) {
            task = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskByUrl(fullUrl);
        }
        if (((task == null) && (repositoryUrl != null)) && (taskId != null)) {
            task = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().getTaskByKey(repositoryUrl, taskId);
        }
        return task;
    }

    /**
     * Searches for a task whose URL matches
     *
     * @return first task with a matching URL.
     */
    public static org.eclipse.mylyn.internal.tasks.core.AbstractTask getTaskByUrl(java.lang.String taskUrl) {
        return org.eclipse.mylyn.internal.tasks.core.util.TasksCoreUtil.getTaskByUrl(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList(), org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager(), taskUrl);
    }

    public static boolean isTaskUrl(java.lang.String taskUrl) {
        org.eclipse.core.runtime.Assert.isNotNull(taskUrl);
        java.util.List<org.eclipse.mylyn.tasks.core.TaskRepository> repositories = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryManager().getAllRepositories();
        for (org.eclipse.mylyn.tasks.core.TaskRepository repository : repositories) {
            if (taskUrl.startsWith(repository.getUrl())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Cleans text for use as the text of an action to ensure that it is displayed properly.
     *
     * @return the cleaned text
     * @deprecated use {@link CommonUiUtil#toMenuLabel(String)} instead
     */
    @java.lang.Deprecated
    public static java.lang.String cleanTextForAction(java.lang.String label) {
        return org.eclipse.mylyn.commons.ui.CommonUiUtil.toMenuLabel(label);
    }

    public static void executeCommand(org.eclipse.ui.services.IServiceLocator serviceLocator, java.lang.String commandId, java.lang.String title, java.lang.Object object, org.eclipse.swt.widgets.Event event) throws org.eclipse.core.commands.NotEnabledException {
        org.eclipse.ui.handlers.IHandlerService service = ((org.eclipse.ui.handlers.IHandlerService) (serviceLocator.getService(org.eclipse.ui.handlers.IHandlerService.class)));
        if (service != null) {
            org.eclipse.ui.commands.ICommandService commandService = ((org.eclipse.ui.commands.ICommandService) (serviceLocator.getService(org.eclipse.ui.commands.ICommandService.class)));
            if (commandService != null) {
                org.eclipse.core.commands.Command command = commandService.getCommand(commandId);
                if (command != null) {
                    try {
                        if (object != null) {
                            org.eclipse.core.expressions.IEvaluationContext context = service.createContextSnapshot(false);
                            context.addVariable(org.eclipse.ui.ISources.ACTIVE_CURRENT_SELECTION_NAME, new org.eclipse.jface.viewers.StructuredSelection(object));
                            service.executeCommandInContext(new org.eclipse.core.commands.ParameterizedCommand(command, null), event, context);
                        } else {
                            service.executeCommand(commandId, event);
                        }
                    } catch (org.eclipse.core.commands.ExecutionException e) {
                        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayStatus(title, new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Command execution failed", e));// $NON-NLS-1$

                    } catch (org.eclipse.core.commands.common.NotDefinedException e) {
                        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayStatus(title, new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.osgi.util.NLS.bind("The command with the id ''{0}'' is not defined.", commandId), e));// $NON-NLS-1$

                    } catch (org.eclipse.core.commands.NotHandledException e) {
                        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayStatus(title, new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.osgi.util.NLS.bind("The command with the id ''{0}'' is not bound.", commandId), e));// $NON-NLS-1$

                    }
                } else {
                    org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayStatus(title, new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.osgi.util.NLS.bind("The command with the id ''{0}'' does not exist.", commandId)));// $NON-NLS-1$

                }
            } else {
                org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayStatus(title, new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, // $NON-NLS-1$
                org.eclipse.osgi.util.NLS.bind("Command service is not available to execute command with the id ''{0}''.", commandId), new java.lang.Exception()));
            }
        } else {
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayStatus(title, new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, // $NON-NLS-1$
            org.eclipse.osgi.util.NLS.bind("Handler service is not available to execute command with the id ''{0}''.", commandId), new java.lang.Exception()));
        }
    }

    public static void activateTaskThroughCommand(org.eclipse.mylyn.tasks.core.ITask task) {
        try {
            // $NON-NLS-1$
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.executeCommand(org.eclipse.ui.PlatformUI.getWorkbench(), "org.eclipse.mylyn.tasks.ui.command.activateSelectedTask", Messages.TasksUiInternal_Activate_Task, task, null);
        } catch (org.eclipse.core.commands.NotEnabledException e) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.osgi.util.NLS.bind("Failed to activate task ''{0}''.", task.getSummary()), e));// $NON-NLS-1$

        }
    }

    public static long getActiveTime(org.eclipse.mylyn.tasks.core.ITask task) {
        if (org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.isActivityTrackingEnabled()) {
            return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().getElapsedTime(task);
        }
        return 0;
    }

    public static java.lang.String getTaskPrefix(java.lang.String connectorKind) {
        org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnector(connectorKind);
        if (connector != null) {
            java.lang.String prefix = connector.getTaskIdPrefix();
            // work around short prefixes which are not separated by space, e.g. "#" for Trac
            return prefix.length() > 1 ? prefix + " " : prefix;// $NON-NLS-1$

        }
        return "";// $NON-NLS-1$

    }

    public static void displayFrameworkError(java.lang.String message) {
        java.lang.RuntimeException exception = new java.lang.RuntimeException(message);
        if (!org.eclipse.mylyn.commons.core.CoreUtil.TEST_MODE) {
            org.eclipse.ui.statushandlers.StatusAdapter status = new org.eclipse.ui.statushandlers.StatusAdapter(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, message, exception));
            status.setProperty(org.eclipse.ui.statushandlers.IStatusAdapterConstants.TITLE_PROPERTY, "Framework Error");// $NON-NLS-1$

            org.eclipse.ui.statushandlers.StatusManager.getManager().handle(status, (org.eclipse.ui.statushandlers.StatusManager.SHOW | org.eclipse.ui.statushandlers.StatusManager.LOG) | org.eclipse.ui.statushandlers.StatusManager.BLOCK);
        }
        throw exception;
    }

    public static java.lang.String getAuthenticatedUrl(org.eclipse.mylyn.tasks.core.TaskRepository repository, org.eclipse.mylyn.tasks.core.IRepositoryElement element) {
        org.eclipse.core.runtime.Assert.isNotNull(repository);
        org.eclipse.core.runtime.Assert.isNotNull(element);
        org.eclipse.mylyn.tasks.core.IRepositoryManager repositoryManager = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager();
        org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = repositoryManager.getRepositoryConnector(repository.getConnectorKind());
        if (connector != null) {
            java.net.URL authenticatedUrl = connector.getAuthenticatedUrl(repository, element);
            if (authenticatedUrl != null) {
                return authenticatedUrl.toString();
            } else {
                java.lang.String url = element.getUrl();
                if (org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.isValidUrl(url)) {
                    return url;
                }
            }
        }
        return null;
    }

    public static org.eclipse.mylyn.tasks.core.TaskRepository getRepository(org.eclipse.mylyn.tasks.core.IRepositoryElement element) {
        org.eclipse.mylyn.tasks.core.IRepositoryManager repositoryManager = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager();
        org.eclipse.mylyn.tasks.core.TaskRepository repository = null;
        if (element instanceof org.eclipse.mylyn.tasks.core.ITask) {
            repository = repositoryManager.getRepository(((org.eclipse.mylyn.tasks.core.ITask) (element)).getConnectorKind(), ((org.eclipse.mylyn.tasks.core.ITask) (element)).getRepositoryUrl());
        } else if (element instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery) {
            repository = repositoryManager.getRepository(((org.eclipse.mylyn.tasks.core.IRepositoryQuery) (element)).getConnectorKind(), ((org.eclipse.mylyn.tasks.core.IRepositoryQuery) (element)).getRepositoryUrl());
        }
        return repository;
    }

    /**
     * Returns all the tasks in the given selection, or an empty list if the selection contains no tasks.
     */
    public static java.util.List<org.eclipse.mylyn.tasks.core.ITask> getTasksFromSelection(org.eclipse.jface.viewers.ISelection selection) {
        org.eclipse.core.runtime.Assert.isNotNull(selection);
        if (selection.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        java.util.List<org.eclipse.mylyn.tasks.core.ITask> tasks = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.ITask>();
        if (selection instanceof org.eclipse.jface.viewers.IStructuredSelection) {
            for (java.lang.Object element : ((org.eclipse.jface.viewers.IStructuredSelection) (selection)).toList()) {
                org.eclipse.mylyn.tasks.core.ITask task = null;
                if (element instanceof org.eclipse.mylyn.tasks.core.ITask) {
                    task = ((org.eclipse.mylyn.tasks.core.ITask) (element));
                } else if (element instanceof org.eclipse.core.runtime.IAdaptable) {
                    org.eclipse.core.runtime.IAdaptable adaptable = ((org.eclipse.core.runtime.IAdaptable) (element));
                    task = ((org.eclipse.mylyn.tasks.core.ITask) (adaptable.getAdapter(org.eclipse.mylyn.tasks.core.ITask.class)));
                }
                if (task != null) {
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }

    public static boolean shouldShowIncoming(org.eclipse.mylyn.tasks.core.ITask task) {
        org.eclipse.mylyn.tasks.core.ITask.SynchronizationState state = task.getSynchronizationState();
        if ((((state == org.eclipse.mylyn.tasks.core.ITask.SynchronizationState.INCOMING) && (!java.lang.Boolean.valueOf(task.getAttribute(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.ATTRIBUTE_TASK_SUPPRESS_INCOMING)))) || (state == org.eclipse.mylyn.tasks.core.ITask.SynchronizationState.INCOMING_NEW)) || (state == org.eclipse.mylyn.tasks.core.ITask.SynchronizationState.CONFLICT)) {
            return true;
        }
        return false;
    }

    public static org.eclipse.mylyn.tasks.core.data.TaskData computeTaskData(org.eclipse.mylyn.tasks.core.data.TaskData taskData, org.eclipse.mylyn.tasks.core.data.TaskHistory history, java.lang.String revisionId, org.eclipse.core.runtime.IProgressMonitor monitor) throws org.eclipse.core.runtime.CoreException {
        org.eclipse.mylyn.tasks.core.data.TaskData newTaskData = org.eclipse.mylyn.internal.tasks.core.data.TaskDataState.createCopy(taskData);
        java.util.List<org.eclipse.mylyn.tasks.core.data.TaskRevision> revisions = history.getRevisions();
        java.util.Collections.reverse(revisions);
        org.eclipse.mylyn.tasks.core.data.TaskRevision lastRevision = null;
        for (org.eclipse.mylyn.tasks.core.data.TaskRevision revision : revisions) {
            for (org.eclipse.mylyn.tasks.core.data.TaskRevision.Change change : revision.getChanges()) {
                org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute = newTaskData.getRoot().getAttribute(change.getAttributeId());
                if (attribute != null) {
                    attribute.setValue(change.getRemoved());
                }
            }
            // only apply changes up to this revision
            if (revisionId.equals(revision.getId())) {
                lastRevision = revision;
                break;
            }
        }
        if ((lastRevision != null) && (lastRevision.getDate() != null)) {
            // remove attachments and comments that are newer than lastRevision
            java.util.List<org.eclipse.mylyn.tasks.core.data.TaskAttribute> attributes = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.data.TaskAttribute>(newTaskData.getRoot().getAttributes().values());
            for (org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute : attributes) {
                if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_COMMENT.equals(attribute.getMetaData().getType())) {
                    org.eclipse.mylyn.tasks.core.data.TaskCommentMapper mapper = org.eclipse.mylyn.tasks.core.data.TaskCommentMapper.createFrom(attribute);
                    if ((mapper.getCreationDate() != null) && mapper.getCreationDate().after(lastRevision.getDate())) {
                        newTaskData.getRoot().removeAttribute(attribute.getId());
                    }
                } else if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_ATTACHMENT.equals(attribute.getMetaData().getType())) {
                    org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper mapper = org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper.createFrom(attribute);
                    if ((mapper.getCreationDate() != null) && mapper.getCreationDate().after(lastRevision.getDate())) {
                        newTaskData.getRoot().removeAttribute(attribute.getId());
                    }
                }
            }
        }
        return newTaskData;
    }

    public static boolean canGetTaskHistory(org.eclipse.mylyn.tasks.core.ITask task) {
        org.eclipse.mylyn.tasks.core.TaskRepository repository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(task.getConnectorKind(), task.getRepositoryUrl());
        org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(repository.getConnectorKind());
        return connector.canGetTaskHistory(repository, task);
    }

    public static org.eclipse.mylyn.commons.identity.core.Account getAccount(org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute) {
        org.eclipse.mylyn.commons.identity.core.Account account;
        if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_PERSON.equals(attribute.getMetaData().getType())) {
            org.eclipse.mylyn.tasks.core.IRepositoryPerson person = attribute.getTaskData().getAttributeMapper().getRepositoryPerson(attribute);
            account = org.eclipse.mylyn.commons.identity.core.Account.id(person.getPersonId()).name(person.getName());
        } else {
            account = org.eclipse.mylyn.commons.identity.core.Account.id(attribute.getValue());
        }
        org.eclipse.mylyn.tasks.core.TaskRepository repository = attribute.getTaskData().getAttributeMapper().getTaskRepository();
        return account.kind(repository.getConnectorKind()).url(repository.getRepositoryUrl());
    }

    public static boolean isActivityTrackingEnabled() {
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityMonitor().isEnabled() && org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin.getDefault().isActivityTrackingEnabled();
    }

    public static org.eclipse.mylyn.internal.tasks.ui.util.TaskDropHandler getTaskDropHandler() {
        return org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.taskDropHandler;
    }

    public static org.eclipse.jface.resource.ImageDescriptor getIconFromStatusOfQuery(org.eclipse.mylyn.internal.tasks.core.RepositoryQuery query) {
        org.eclipse.jface.resource.ImageDescriptor image;
        boolean showError = false;
        java.lang.Throwable exception = query.getStatus().getException();
        showError = query.getLastSynchronizedTimeStamp().equals("<never>")// $NON-NLS-1$
         && ((((org.eclipse.mylyn.tasks.core.RepositoryStatus.ERROR_IO == query.getStatus().getCode()) && (exception != null)) && (exception instanceof java.net.SocketTimeoutException))// 
         || // only when we change SocketTimeout or Eclipse.org change there timeout for long running Queries
        ((org.eclipse.mylyn.tasks.core.RepositoryStatus.ERROR_NETWORK == query.getStatus().getCode()) && query.getStatus().getMessage().equals("Http error: Internal Server Error")));// $NON-NLS-1$

        if (showError) {
            image = org.eclipse.mylyn.commons.ui.CommonImages.OVERLAY_SYNC_ERROR;
        } else {
            image = org.eclipse.mylyn.commons.ui.CommonImages.OVERLAY_SYNC_WARNING;
        }
        return image;
    }

    public static synchronized org.eclipse.core.commands.operations.IUndoContext getUndoContext() {
        if (org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.undoContext == null) {
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.undoContext = new org.eclipse.core.commands.operations.ObjectUndoContext(new java.lang.Object(), "Tasks Context");// $NON-NLS-1$

        }
        return org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.undoContext;
    }
}