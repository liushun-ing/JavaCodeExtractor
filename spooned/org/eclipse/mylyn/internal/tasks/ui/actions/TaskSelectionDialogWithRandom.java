/**
 * *****************************************************************************
 * Copyright (c) 2004, 2011 Tasktop Technologies and others.
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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.internal.tasks.core.TaskActivityManager;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITask.PriorityLevel;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
/**
 *
 * @author Jakub Jurkiewicz
 * @author Mik Kersten
 */
public class TaskSelectionDialogWithRandom extends org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog {
    private static final int RANDOM_ID = org.eclipse.jface.dialogs.IDialogConstants.CLIENT_ID + 1;

    private org.eclipse.swt.widgets.Button randomTaskButton;

    private boolean activateTask = false;

    public TaskSelectionDialogWithRandom(org.eclipse.swt.widgets.Shell parent, boolean multi) {
        super(parent, multi);
    }

    public TaskSelectionDialogWithRandom(org.eclipse.swt.widgets.Shell parent) {
        super(parent);
    }

    @java.lang.Override
    protected void createAdditionalButtons(org.eclipse.swt.widgets.Composite parent) {
        randomTaskButton = createButton(parent, org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialogWithRandom.RANDOM_ID, Messages.TaskSelectionDialog_Random_Task, false);
        randomTaskButton.setToolTipText(Messages.TaskSelectionDialogWithRandom_Feeling_Lazy_Tooltip);
        randomTaskButton.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
            public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
                // ignore
            }

            public void widgetSelected(org.eclipse.swt.events.SelectionEvent se) {
                try {
                    java.util.Set<org.eclipse.mylyn.tasks.core.ITask> selectedTasks = new java.util.HashSet<org.eclipse.mylyn.tasks.core.ITask>();
                    java.util.Set<org.eclipse.mylyn.tasks.core.ITask> allScheduled = ((org.eclipse.mylyn.internal.tasks.core.TaskActivityManager) (org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager())).getAllScheduledTasks();
                    if (!allScheduled.isEmpty()) {
                        selectedTasks.addAll(allScheduled);
                        // XXX bug 280939 make sure all scheduled tasks actually exist
                        selectedTasks.retainAll(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().getAllTasks());
                    }
                    if (selectedTasks.isEmpty()) {
                        selectedTasks.addAll(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().getAllTasks());
                    }
                    java.util.Set<org.eclipse.mylyn.tasks.core.ITask> potentialTasks = new java.util.HashSet<org.eclipse.mylyn.tasks.core.ITask>();
                    addLowEnergyTasks(selectedTasks, potentialTasks, org.eclipse.mylyn.tasks.core.ITask.PriorityLevel.P5);
                    addLowEnergyTasks(selectedTasks, potentialTasks, org.eclipse.mylyn.tasks.core.ITask.PriorityLevel.P4);
                    if (potentialTasks.isEmpty()) {
                        addLowEnergyTasks(selectedTasks, potentialTasks, org.eclipse.mylyn.tasks.core.ITask.PriorityLevel.P3);
                    }
                    if (potentialTasks.isEmpty()) {
                        addLowEnergyTasks(selectedTasks, potentialTasks, org.eclipse.mylyn.tasks.core.ITask.PriorityLevel.P2);
                    }
                    int randomTaskIndex = new java.util.Random().nextInt(potentialTasks.size());
                    org.eclipse.mylyn.tasks.core.ITask randomTask = potentialTasks.toArray(new org.eclipse.mylyn.tasks.core.ITask[potentialTasks.size()])[randomTaskIndex];
                    if (activateTask) {
                        org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().activateTask(randomTask);
                    }
                    org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.refreshAndOpenTaskListElement(randomTask);
                    close();
                } catch (java.lang.Exception e) {
                    org.eclipse.jface.dialogs.MessageDialog.openInformation(org.eclipse.swt.widgets.Display.getDefault().getActiveShell(), Messages.TaskSelectionDialogWithRandom_Feeling_Lazy_Error_Title, Messages.TaskSelectionDialogWithRandom_Feeling_Lazy_Error);
                }
            }

            private void addLowEnergyTasks(java.util.Set<org.eclipse.mylyn.tasks.core.ITask> selectedTasks, java.util.Set<org.eclipse.mylyn.tasks.core.ITask> potentialTasks, org.eclipse.mylyn.tasks.core.ITask.PriorityLevel priorityLevel) {
                for (org.eclipse.mylyn.tasks.core.ITask task : selectedTasks) {
                    if (task.getSynchronizationState().isSynchronized() && (!task.isCompleted())) {
                        if (priorityLevel.toString().equals(task.getPriority())) {
                            potentialTasks.add(task);
                        }
                    }
                }
            }
        });
    }

    public boolean isActivateTask() {
        return activateTask;
    }

    public void setActivateTask(boolean activateTask) {
        this.activateTask = activateTask;
    }
}