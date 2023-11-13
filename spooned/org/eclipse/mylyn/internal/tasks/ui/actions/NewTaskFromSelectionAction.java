/**
 * *****************************************************************************
 * Copyright (c) 2004, 2011 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *     Tasktop Technologies - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.actions;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.tasks.core.ITaskComment;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskMapping;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
/**
 *
 * @author Frank Becker
 * @author Steffen Pingel
 */
public class NewTaskFromSelectionAction extends org.eclipse.jface.action.Action {
    public static final java.lang.String ID = "org.eclipse.mylyn.tasks.ui.actions.newTaskFromSelection";// $NON-NLS-1$


    private org.eclipse.mylyn.tasks.core.ITaskMapping taskMapping;

    public NewTaskFromSelectionAction() {
        super(Messages.NewTaskFromSelectionAction_New_Task_from_Selection);
        setId(org.eclipse.mylyn.internal.tasks.ui.actions.NewTaskFromSelectionAction.ID);
        setImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.TASK_NEW);
    }

    public org.eclipse.mylyn.tasks.core.ITaskMapping getTaskMapping() {
        return taskMapping;
    }

    public void run(org.eclipse.jface.action.IAction action) {
        run();
    }

    @java.lang.Override
    public void run() {
        if (taskMapping == null) {
            org.eclipse.jface.dialogs.MessageDialog.openError(null, Messages.NewTaskFromSelectionAction_New_Task_from_Selection, Messages.NewTaskFromSelectionAction_Nothing_selected_to_create_task_from);
            return;
        }
        org.eclipse.swt.widgets.Shell shell = org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        org.eclipse.mylyn.tasks.ui.TasksUiUtil.openNewTaskEditor(shell, taskMapping, null);
    }

    public void selectionChanged(org.eclipse.jface.viewers.ISelection selection) {
        if (selection instanceof org.eclipse.jface.text.TextSelection) {
            org.eclipse.jface.text.TextSelection textSelection = ((org.eclipse.jface.text.TextSelection) (selection));
            final java.lang.String text = textSelection.getText();
            if ((text != null) && (text.length() > 0)) {
                taskMapping = new org.eclipse.mylyn.tasks.core.TaskMapping() {
                    @java.lang.Override
                    public java.lang.String getDescription() {
                        return text;
                    }
                };
            } else {
                taskMapping = null;
            }
            // } else if (selection instanceof RepositoryTaskSelection) {
            // RepositoryTaskSelection repositoryTaskSelection = (RepositoryTaskSelection) selection;
            // IRepositoryManager repositoryManager = TasksUi.getRepositoryManager();
            // AbstractRepositoryConnector connector = repositoryManager.getRepositoryConnector(repositoryTaskSelection.getRepositoryKind());
            // 
            // TaskComment comment = repositoryTaskSelection.getComment();
            // if (comment != null) {
            // StringBuilder sb = new StringBuilder();
            // sb.append("\n-- Created from Comment --");
            // if (connector != null) {
            // sb.append("\nURL: ");
            // sb.append(connector.getTaskUrl(repositoryTaskSelection.getRepositoryUrl(),
            // repositoryTaskSelection.getId()));
            // }
            // sb.append("\nComment: ");
            // sb.append(comment.getNumber());
            // 
            // sb.append("\n\n");
            // if (taskSelection != null) {
            // // if text was selected, prefer that
            // sb.append(taskSelection.getLegacyTaskData().getDescription());
            // } else {
            // sb.append(comment.getText());
            // }
            // 
            // taskSelection = new TaskSelection("", sb.toString());
            // } else if (taskSelection != null) {
            // StringBuilder sb = new StringBuilder();
            // if (connector != null) {
            // sb.append("\n-- Created from Task --");
            // sb.append("\nURL: ");
            // sb.append(connector.getTaskUrl(repositoryTaskSelection.getRepositoryUrl(),
            // repositoryTaskSelection.getId()));
            // }
            // 
            // sb.append("\n\n");
            // sb.append(taskSelection.getLegacyTaskData().getDescription());
            // 
            // taskSelection = new TaskSelection("", sb.toString());
            // }
        } else if (selection instanceof org.eclipse.jface.viewers.StructuredSelection) {
            java.lang.Object element = ((org.eclipse.jface.viewers.StructuredSelection) (selection)).getFirstElement();
            if (element instanceof org.eclipse.mylyn.tasks.core.ITaskComment) {
                org.eclipse.mylyn.tasks.core.ITaskComment comment = ((org.eclipse.mylyn.tasks.core.ITaskComment) (element));
                final java.lang.StringBuilder sb = new java.lang.StringBuilder();
                sb.append("\n" + Messages.NewTaskFromSelectionAction____Created_from_Comment___);// $NON-NLS-1$

                if (comment.getUrl() == null) {
                    sb.append("\n" + Messages.NewTaskFromSelectionAction_URL_);// $NON-NLS-1$

                    sb.append(comment.getTask().getUrl());
                    sb.append("\n" + Messages.NewTaskFromSelectionAction_Comment_);// $NON-NLS-1$

                    sb.append(comment.getNumber());
                } else {
                    sb.append("\n" + Messages.NewTaskFromSelectionAction_URL_);// $NON-NLS-1$

                    sb.append(comment.getUrl());
                }
                sb.append("\n\n");// $NON-NLS-1$

                sb.append(comment.getText());
                taskMapping = new org.eclipse.mylyn.tasks.core.TaskMapping() {
                    @java.lang.Override
                    public java.lang.String getDescription() {
                        return sb.toString();
                    }
                };
            }
        }
        setEnabled(taskMapping != null);
    }
}