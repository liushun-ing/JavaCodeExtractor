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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
/**
 *
 * @author Mik Kersten
 */
public class GoIntoAction extends org.eclipse.jface.action.Action implements org.eclipse.ui.IViewActionDelegate {
    public static final java.lang.String ID = "org.eclipse.mylyn.tasklist.actions.view.go.into";// $NON-NLS-1$


    public GoIntoAction() {
        setId(org.eclipse.mylyn.internal.tasks.ui.actions.GoIntoAction.ID);
        setText(Messages.GoIntoAction_Go_Into);
        setToolTipText(Messages.GoIntoAction_Go_Into);
        setImageDescriptor(org.eclipse.mylyn.commons.ui.CommonImages.GO_INTO);
    }

    public void init(org.eclipse.ui.IViewPart view) {
        // TODO Auto-generated method stub
    }

    @java.lang.Override
    public void run() {
        if (org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.getFromActivePerspective() != null) {
            org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.getFromActivePerspective().getFilteredTree().setFilterText("");// $NON-NLS-1$

            org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.getFromActivePerspective().goIntoCategory();
        }
    }

    public void run(org.eclipse.jface.action.IAction action) {
        run();
    }

    public void selectionChanged(org.eclipse.jface.action.IAction action, org.eclipse.jface.viewers.ISelection selection) {
        // TODO Auto-generated method stub
    }
}