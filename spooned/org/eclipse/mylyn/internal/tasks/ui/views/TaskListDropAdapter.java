/**
 * *****************************************************************************
 * Copyright (c) 2004, 2012 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Jevgeni Holodkov - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.views;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.DateRange;
import org.eclipse.mylyn.internal.tasks.core.LocalTask;
import org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.TaskCategory;
import org.eclipse.mylyn.internal.tasks.core.UncategorizedTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.UnmatchedTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.UnsubmittedTaskContainer;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.AbstractRetrieveTitleFromUrlJob;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.StateTaskContainer;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.Unscheduled;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskContainer;
import org.eclipse.mylyn.tasks.ui.TaskDropListener.Operation;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.dnd.URLTransfer;
import org.eclipse.ui.PlatformUI;
/**
 *
 * @author Mik Kersten
 * @author Rob Elves (added URL based task creation support)
 * @author Jevgeni Holodkov
 * @author Sam Davis
 */
public class TaskListDropAdapter extends org.eclipse.jface.viewers.ViewerDropAdapter {
    private boolean fileTransfer;

    private boolean localTransfer;

    public TaskListDropAdapter(org.eclipse.jface.viewers.Viewer viewer) {
        super(viewer);
        setFeedbackEnabled(true);
    }

    @java.lang.Override
    public boolean performDrop(final java.lang.Object data) {
        java.util.List<org.eclipse.mylyn.tasks.core.ITask> tasksToMove = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.ITask>();
        if (localTransfer) {
            org.eclipse.jface.viewers.ISelection selection = org.eclipse.jface.util.LocalSelectionTransfer.getTransfer().getSelection();
            java.util.List<org.eclipse.mylyn.tasks.core.ITask> tasks = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTasksFromSelection(selection);
            tasksToMove.addAll(tasks);
        } else if (fileTransfer) {
            // TODO implement dropping of files
        } else if (data instanceof java.lang.String) {
            java.lang.String text = ((java.lang.String) (data));
            org.eclipse.mylyn.internal.tasks.core.AbstractTask task = createTaskFromUrl(text);
            if (task == null) {
                task = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.createNewLocalTask(text);
            }
            if (task != null) {
                tasksToMove.add(task);
                final org.eclipse.mylyn.tasks.core.ITask newTask = task;
                org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                    public void run() {
                        org.eclipse.mylyn.tasks.ui.TasksUiUtil.openTask(newTask);
                    }
                });
            }
        }
        java.lang.Object currentTarget = getCurrentTarget();
        if (((currentTarget instanceof org.eclipse.mylyn.internal.tasks.core.LocalTask) && areAllLocalTasks(tasksToMove)) && (getCurrentLocation() == LOCATION_ON)) {
            for (org.eclipse.mylyn.tasks.core.ITask task : tasksToMove) {
                if (!((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (task)).contains(((org.eclipse.mylyn.internal.tasks.core.LocalTask) (currentTarget)).getHandleIdentifier())) {
                    org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().addTask(task, ((org.eclipse.mylyn.internal.tasks.core.LocalTask) (currentTarget)));
                }
            }
        } else if (((currentTarget instanceof org.eclipse.mylyn.tasks.core.ITask) && (getCurrentLocation() == org.eclipse.jface.viewers.ViewerDropAdapter.LOCATION_ON)) && (getCurrentOperation() != org.eclipse.swt.dnd.DND.DROP_MOVE)) {
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskDropHandler().loadTaskDropListeners();
            org.eclipse.mylyn.tasks.ui.TaskDropListener.Operation operation = (getCurrentOperation() == org.eclipse.swt.dnd.DND.DROP_COPY) ? org.eclipse.mylyn.tasks.ui.TaskDropListener.Operation.COPY : org.eclipse.mylyn.tasks.ui.TaskDropListener.Operation.LINK;
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskDropHandler().fireTaskDropped(tasksToMove, ((org.eclipse.mylyn.tasks.core.ITask) (currentTarget)), operation);
        } else {
            for (org.eclipse.mylyn.tasks.core.ITask task : tasksToMove) {
                if (currentTarget instanceof org.eclipse.mylyn.internal.tasks.core.UncategorizedTaskContainer) {
                    moveTask(task, ((org.eclipse.mylyn.internal.tasks.core.UncategorizedTaskContainer) (currentTarget)));
                } else if (currentTarget instanceof org.eclipse.mylyn.internal.tasks.core.TaskCategory) {
                    moveTask(task, ((org.eclipse.mylyn.internal.tasks.core.TaskCategory) (currentTarget)));
                } else if (currentTarget instanceof org.eclipse.mylyn.internal.tasks.core.UnmatchedTaskContainer) {
                    if (((org.eclipse.mylyn.internal.tasks.core.UnmatchedTaskContainer) (currentTarget)).getRepositoryUrl().equals(task.getRepositoryUrl())) {
                        moveTask(task, ((org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory) (currentTarget)));
                    }
                } else if (currentTarget instanceof org.eclipse.mylyn.tasks.core.ITask) {
                    org.eclipse.mylyn.tasks.core.ITask targetTask = ((org.eclipse.mylyn.tasks.core.ITask) (currentTarget));
                    org.eclipse.mylyn.internal.tasks.ui.views.TaskListView view = org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.getFromActivePerspective();
                    if ((((getCurrentLocation() == LOCATION_BEFORE) || (getCurrentLocation() == LOCATION_AFTER)) && (view != null)) && view.isScheduledPresentation()) {
                        if (targetTask instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask) {
                            org.eclipse.mylyn.internal.tasks.core.DateRange targetDate = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (targetTask)).getScheduledForDate();
                            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().setScheduledFor(((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (task)), targetDate);
                        }
                    } else {
                        org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory targetCategory = null;
                        // TODO: TaskCategory only used what about AbstractTaskCategory descendants?
                        org.eclipse.mylyn.tasks.core.ITaskContainer container = org.eclipse.mylyn.internal.tasks.core.TaskCategory.getParentTaskCategory(targetTask);
                        if ((container instanceof org.eclipse.mylyn.internal.tasks.core.TaskCategory) || (container instanceof org.eclipse.mylyn.internal.tasks.core.UncategorizedTaskContainer)) {
                            targetCategory = ((org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory) (container));
                        } else if (container instanceof org.eclipse.mylyn.internal.tasks.core.UnmatchedTaskContainer) {
                            if (((org.eclipse.mylyn.internal.tasks.core.UnmatchedTaskContainer) (container)).getRepositoryUrl().equals(task.getRepositoryUrl())) {
                                targetCategory = ((org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory) (container));
                            }
                        }
                        if (targetCategory != null) {
                            moveTask(task, targetCategory);
                        }
                    }
                } else if (currentTarget instanceof org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer) {
                    org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer container = ((org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer) (currentTarget));
                    if (container instanceof org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.Unscheduled) {
                        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().setScheduledFor(((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (task)), null);
                    } else if (isValidTarget(container)) {
                        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().setScheduledFor(((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (task)), container.getDateRange());
                    }
                } else if (currentTarget == null) {
                    moveTask(task, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().getDefaultCategory());
                }
            }
        }
        if (tasksToMove.size() == 1) {
            getViewer().setSelection(new org.eclipse.jface.viewers.StructuredSelection(tasksToMove.get(0)));
        }
        return true;
    }

    private boolean isValidTarget(org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer container) {
        // ignore incoming, outgoing, completed
        return (container instanceof org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.Unscheduled) || (!(container instanceof org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.StateTaskContainer));
    }

    private void moveTask(org.eclipse.mylyn.tasks.core.ITask task, org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer container) {
        if (!isUnsubmittedTask(task)) {
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().addTask(task, container);
        }
    }

    private boolean isUnsubmittedTask(org.eclipse.mylyn.tasks.core.ITask task) {
        if (task instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask) {
            for (org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer parent : ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (task)).getParentContainers()) {
                if (parent instanceof org.eclipse.mylyn.internal.tasks.core.UnsubmittedTaskContainer) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean areAllLocalTasks(java.util.List<org.eclipse.mylyn.tasks.core.ITask> tasksToMove) {
        for (org.eclipse.mylyn.tasks.core.ITask task : tasksToMove) {
            if ((!(task instanceof org.eclipse.mylyn.internal.tasks.core.LocalTask)) || isUnsubmittedTask(task)) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @param data
     * 		string containing url and title separated by <quote>\n</quote>
     */
    private org.eclipse.mylyn.internal.tasks.core.AbstractTask createTaskFromUrl(java.lang.String data) {
        if ((!data.startsWith("http://")) && (!data.startsWith("https://"))) {
            // $NON-NLS-1$ //$NON-NLS-2$
            return null;
        }
        java.lang.String[] urlTransfer = data.split("\n");// $NON-NLS-1$

        if (urlTransfer.length > 0) {
            java.lang.String url = urlTransfer[0];
            org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryManager().getConnectorForRepositoryTaskUrl(url);
            if (connector != null) {
                // attempt to find task in task list
                java.lang.String repositoryUrl = connector.getRepositoryUrlFromTaskUrl(url);
                java.lang.String taskId = connector.getTaskIdFromTaskUrl(url);
                org.eclipse.mylyn.internal.tasks.core.AbstractTask task = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTask(repositoryUrl, taskId, url);
                if (task != null) {
                    return task;
                }
                if ((repositoryUrl != null) && (taskId != null)) {
                    // attempt to open task in background
                    // TODO: consider attaching a listener to OpenRepsitoryTaskJob to move task to drop target
                    org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.openRepositoryTask(connector.getConnectorKind(), repositoryUrl, taskId, null, 0);
                }
            } else {
                // create local task, using title of web page as a summary
                final java.lang.String summary = Messages.TaskListDropAdapter__retrieving_from_URL_;
                final org.eclipse.mylyn.internal.tasks.core.LocalTask newTask = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.createNewLocalTask(summary);
                newTask.setUrl(url);
                org.eclipse.mylyn.internal.tasks.ui.util.AbstractRetrieveTitleFromUrlJob job = new org.eclipse.mylyn.internal.tasks.ui.util.AbstractRetrieveTitleFromUrlJob(url) {
                    @java.lang.Override
                    protected void titleRetrieved(final java.lang.String pageTitle) {
                        // make sure summary was not changed in the mean time
                        if (newTask.getSummary().equals(summary)) {
                            newTask.setSummary(pageTitle);
                            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().notifyElementChanged(newTask);
                        }
                    }
                };
                job.schedule();
                return newTask;
            }
        }
        return null;
    }

    @java.lang.Override
    public boolean validateDrop(java.lang.Object targetObject, int operation, org.eclipse.swt.dnd.TransferData transferType) {
        fileTransfer = false;
        localTransfer = false;
        if (org.eclipse.swt.dnd.FileTransfer.getInstance().isSupportedType(transferType)) {
            fileTransfer = true;
            // TODO handle all files
            return false;
        } else if (org.eclipse.jface.util.LocalSelectionTransfer.getTransfer().isSupportedType(transferType)) {
            localTransfer = true;
            java.lang.Object target = getCurrentTarget();
            if ((((target instanceof org.eclipse.mylyn.internal.tasks.core.UncategorizedTaskContainer) || (target instanceof org.eclipse.mylyn.internal.tasks.core.TaskCategory)) || (target instanceof org.eclipse.mylyn.internal.tasks.core.UnmatchedTaskContainer)) || ((target instanceof org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer) && isValidTarget(((org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer) (target))))) {
                return true;
            } else if ((target instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) && ((getCurrentLocation() == org.eclipse.jface.viewers.ViewerDropAdapter.LOCATION_AFTER) || (getCurrentLocation() == org.eclipse.jface.viewers.ViewerDropAdapter.LOCATION_BEFORE))) {
                return true;
            } else if ((target instanceof org.eclipse.mylyn.internal.tasks.core.LocalTask) && (getCurrentLocation() == org.eclipse.jface.viewers.ViewerDropAdapter.LOCATION_ON)) {
                return true;
            } else if (((target instanceof org.eclipse.mylyn.tasks.core.ITask) && (getCurrentLocation() == org.eclipse.jface.viewers.ViewerDropAdapter.LOCATION_ON)) && (getCurrentOperation() != org.eclipse.swt.dnd.DND.DROP_MOVE)) {
                return true;
            } else {
                return false;
            }
        } else if (org.eclipse.swt.dnd.URLTransfer.getInstance().isSupportedType(transferType)) {
            return true;
        }
        return org.eclipse.swt.dnd.TextTransfer.getInstance().isSupportedType(transferType);
    }
}