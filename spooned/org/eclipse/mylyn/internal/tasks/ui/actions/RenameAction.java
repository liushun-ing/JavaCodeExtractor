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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory;
import org.eclipse.mylyn.internal.tasks.core.LocalTask;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.swt.SWT;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
/**
 *
 * @author Mik Kersten
 */
public class RenameAction extends org.eclipse.ui.actions.BaseSelectionListenerAction {
    public static final java.lang.String ID = "org.eclipse.mylyn.tasklist.actions.rename";// $NON-NLS-1$


    private final org.eclipse.mylyn.internal.tasks.ui.views.TaskListView view;

    public RenameAction(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView view) {
        super(Messages.RenameAction_Rename);
        this.view = view;
        setId(org.eclipse.mylyn.internal.tasks.ui.actions.RenameAction.ID);
        setAccelerator(org.eclipse.swt.SWT.F2);
    }

    @java.lang.Override
    public void run() {
        java.lang.Object selectedObject = ((org.eclipse.jface.viewers.IStructuredSelection) (this.view.getViewer().getSelection())).getFirstElement();
        if (selectedObject instanceof org.eclipse.mylyn.tasks.core.IRepositoryElement) {
            org.eclipse.mylyn.tasks.core.IRepositoryElement element = ((org.eclipse.mylyn.tasks.core.IRepositoryElement) (selectedObject));
            view.setInRenameAction(true);
            view.getViewer().editElement(element, 0);
            view.setInRenameAction(false);
        }
    }

    @java.lang.Override
    protected boolean updateSelection(org.eclipse.jface.viewers.IStructuredSelection selection) {
        java.lang.Object selectedObject = selection.getFirstElement();
        if (selectedObject instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory) {
            org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory container = ((org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory) (selectedObject));
            return container.isUserManaged();
        } else if (selectedObject instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery) {
            return true;
        }
        return selectedObject instanceof org.eclipse.mylyn.internal.tasks.core.LocalTask;
    }
}