/**
 * *****************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.wizards;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylyn.internal.tasks.core.LocalTask;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
/**
 *
 * @author Rob Elves
 * @author Steffen Pingel
 */
public class NewLocalTaskWizard extends org.eclipse.jface.wizard.Wizard implements org.eclipse.ui.INewWizard {
    private final org.eclipse.mylyn.tasks.core.ITaskMapping taskSelection;

    public NewLocalTaskWizard(org.eclipse.mylyn.tasks.core.ITaskMapping taskSelection) {
        this.taskSelection = taskSelection;
        setDefaultPageImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.BANNER_REPOSITORY);
        setNeedsProgressMonitor(true);
    }

    public NewLocalTaskWizard() {
        this(null);
    }

    public void init(org.eclipse.ui.IWorkbench workbench, org.eclipse.jface.viewers.IStructuredSelection selection) {
        setForcePreviousAndNextButtons(false);
    }

    @java.lang.Override
    public void addPages() {
        // ignore
    }

    @java.lang.Override
    public boolean canFinish() {
        return true;
    }

    @java.lang.Override
    public boolean performFinish() {
        org.eclipse.mylyn.internal.tasks.core.LocalTask task = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.createNewLocalTask(null);
        if (taskSelection != null) {
            if (taskSelection.getSummary() != null) {
                task.setSummary(taskSelection.getSummary());
            }
            if (taskSelection.getDescription() != null) {
                task.setNotes(taskSelection.getDescription());
            }
        }
        org.eclipse.mylyn.tasks.ui.TasksUiUtil.openTask(task);
        return true;
    }
}