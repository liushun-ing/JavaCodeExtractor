/**
 * *****************************************************************************
 * Copyright (c) 2004, 2008 Eugene Kuleshov and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugene Kuleshov - initial API and implementation
 *     Tasktop Technologies - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.tasks.ui.wizards;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylyn.internal.tasks.ui.wizards.Messages;
import org.eclipse.mylyn.internal.tasks.ui.wizards.NewWebTaskPage;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
/**
 * Wizard for creating new tickets through a web browser.
 *
 * @author Eugene Kuleshov
 * @author Mik Kersten
 * @author Steffen Pingel
 * @since 2.0
 */
public class NewWebTaskWizard extends org.eclipse.jface.wizard.Wizard implements org.eclipse.ui.INewWizard {
    protected org.eclipse.mylyn.tasks.core.TaskRepository taskRepository;

    protected java.lang.String newTaskUrl;

    private final org.eclipse.mylyn.tasks.core.ITaskMapping taskSelection;

    /**
     *
     * @since 3.0
     */
    public NewWebTaskWizard(org.eclipse.mylyn.tasks.core.TaskRepository taskRepository, java.lang.String newTaskUrl, org.eclipse.mylyn.tasks.core.ITaskMapping taskSelection) {
        this.taskRepository = taskRepository;
        this.newTaskUrl = newTaskUrl;
        this.taskSelection = taskSelection;
        setWindowTitle(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.NewWebTaskWizard_New_Task);
        setDefaultPageImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.BANNER_REPOSITORY);
    }

    public void init(org.eclipse.ui.IWorkbench workbench, org.eclipse.jface.viewers.IStructuredSelection selection) {
    }

    @java.lang.Override
    public void addPages() {
        addPage(new org.eclipse.mylyn.internal.tasks.ui.wizards.NewWebTaskPage(taskSelection));
    }

    @java.lang.Override
    public boolean canFinish() {
        return true;
    }

    @java.lang.Override
    public boolean performFinish() {
        handleSelection(taskSelection);
        org.eclipse.mylyn.tasks.ui.TasksUiUtil.openUrl(newTaskUrl);
        return true;
    }

    private void handleSelection(final org.eclipse.mylyn.tasks.core.ITaskMapping taskSelection) {
        if (taskSelection == null) {
            return;
        }
        java.lang.String summary = taskSelection.getSummary();
        java.lang.String description = taskSelection.getDescription();
        org.eclipse.swt.dnd.Clipboard clipboard = new org.eclipse.swt.dnd.Clipboard(getShell().getDisplay());
        // $NON-NLS-1$
        clipboard.setContents(new java.lang.Object[]{ (summary + "\n") + description }, new org.eclipse.swt.dnd.Transfer[]{ org.eclipse.swt.dnd.TextTransfer.getInstance() });
        org.eclipse.jface.dialogs.MessageDialog.openInformation(getShell(), org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.NewWebTaskWizard_New_Task, org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.NewWebTaskWizard_This_connector_does_not_provide_a_rich_task_editor_for_creating_tasks);
    }
}