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
package org.eclipse.mylyn.internal.tasks.ui.actions;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
/**
 *
 * @author Mik Kersten
 * @author Steffen Pingel
 */
public class EditRepositoryPropertiesAction extends org.eclipse.mylyn.internal.tasks.ui.actions.AbstractTaskRepositoryAction implements org.eclipse.ui.IViewActionDelegate {
    private static final java.lang.String ID = "org.eclipse.mylyn.tasklist.repositories.properties";// $NON-NLS-1$


    public EditRepositoryPropertiesAction() {
        super(Messages.EditRepositoryPropertiesAction_Properties);
        setId(org.eclipse.mylyn.internal.tasks.ui.actions.EditRepositoryPropertiesAction.ID);
        setEnabled(false);
    }

    @java.lang.Override
    public void run() {
        org.eclipse.mylyn.tasks.core.TaskRepository taskRepository = getTaskRepository(getStructuredSelection());
        if (taskRepository != null) {
            org.eclipse.mylyn.tasks.ui.TasksUiUtil.openEditRepositoryWizard(taskRepository);
        }
    }

    public void init(org.eclipse.ui.IViewPart view) {
    }

    public void run(org.eclipse.jface.action.IAction action) {
        run();
    }

    public void selectionChanged(org.eclipse.jface.action.IAction action, org.eclipse.jface.viewers.ISelection selection) {
        if (selection instanceof org.eclipse.jface.viewers.IStructuredSelection) {
            selectionChanged(((org.eclipse.jface.viewers.IStructuredSelection) (selection)));
            action.setEnabled(this.isEnabled());
        } else {
            clearCache();
            action.setEnabled(false);
        }
    }
}