/**
 * *****************************************************************************
 * Copyright (c) 2004, 2013 Maarten Meijer and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Maarten Meijer - initial API and implementation
 *     Tasktop Technologies - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.actions;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.LocalTask;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskInitializationData;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
/**
 *
 * @author Maarten Meijer
 * @author Mik Kersten
 * @author Steffen Pingel
 */
public class CloneTaskAction extends org.eclipse.ui.actions.BaseSelectionListenerAction implements org.eclipse.ui.IViewActionDelegate {
    private static final java.lang.String ID = "org.eclipse.mylyn.tasklist.actions.clone";// $NON-NLS-1$


    protected org.eclipse.jface.viewers.ISelection selection;

    public CloneTaskAction() {
        super(Messages.CloneTaskAction_Clone_Label);
        setId(org.eclipse.mylyn.internal.tasks.ui.actions.CloneTaskAction.ID);
        setImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.TASK_NEW);
    }

    public void init(org.eclipse.ui.IViewPart view) {
        // ignore
    }

    @java.lang.Override
    public void run() {
        for (java.lang.Object selectedObject : getStructuredSelection().toList()) {
            if (selectedObject instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask) {
                org.eclipse.mylyn.internal.tasks.core.AbstractTask task = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (selectedObject));
                java.lang.String description = Messages.CloneTaskAction_Cloned_from_ + org.eclipse.mylyn.internal.tasks.ui.actions.CopyTaskDetailsAction.getTextForTask(task);
                if (task instanceof org.eclipse.mylyn.internal.tasks.core.LocalTask) {
                    java.lang.String notes = task.getNotes();
                    if (!"".equals(notes)) {
                        // $NON-NLS-1$
                        description += "\n\n" + notes;// $NON-NLS-1$

                    }
                }
                org.eclipse.mylyn.tasks.core.TaskInitializationData initializationData = new org.eclipse.mylyn.tasks.core.TaskInitializationData();
                initializationData.setDescription(description);
                org.eclipse.mylyn.tasks.core.data.TaskData taskData;
                try {
                    taskData = org.eclipse.mylyn.tasks.ui.TasksUi.getTaskDataManager().getTaskData(task);
                } catch (org.eclipse.core.runtime.CoreException e) {
                    org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayStatus(Messages.CloneTaskAction_Clone_Task_Failed, e.getStatus());
                    continue;
                }
                org.eclipse.mylyn.tasks.core.ITaskMapping taskSelection = getTaskMapping(initializationData, taskData);
                org.eclipse.swt.widgets.Shell shell = org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                if (!org.eclipse.mylyn.tasks.ui.TasksUiUtil.openNewTaskEditor(shell, taskSelection, null)) {
                    // do not process other tasks if canceled
                    return;
                }
            }
        }
    }

    public org.eclipse.mylyn.tasks.core.ITaskMapping getTaskMapping(org.eclipse.mylyn.tasks.core.TaskInitializationData initializationData, org.eclipse.mylyn.tasks.core.data.TaskData taskData) {
        if (taskData != null) {
            org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryConnector(taskData.getConnectorKind());
            org.eclipse.mylyn.tasks.core.ITaskMapping mapping = connector.getTaskMapping(taskData);
            if (mapping.getDescription() != null) {
                initializationData.setDescription((initializationData.getDescription() + "\n\n")// $NON-NLS-1$
                 + mapping.getDescription());
                org.eclipse.mylyn.tasks.core.data.TaskAttribute attrDescription = mapping.getTaskData().getRoot().getMappedAttribute(org.eclipse.mylyn.tasks.core.data.TaskAttribute.DESCRIPTION);
                if (attrDescription != null) {
                    attrDescription.getMetaData().setReadOnly(false);
                }
            }
            mapping.merge(initializationData);
            return mapping;
        } else {
            return initializationData;
        }
    }

    public void run(org.eclipse.jface.action.IAction action) {
        run();
    }

    public void selectionChanged(org.eclipse.jface.action.IAction action, org.eclipse.jface.viewers.ISelection selection) {
        if (selection instanceof org.eclipse.jface.viewers.IStructuredSelection) {
            super.selectionChanged(((org.eclipse.jface.viewers.IStructuredSelection) (selection)));
        }
    }

    @java.lang.Override
    protected boolean updateSelection(org.eclipse.jface.viewers.IStructuredSelection selection) {
        if (!selection.isEmpty()) {
            for (java.lang.Object element : selection.toList()) {
                if (!(element instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }
}