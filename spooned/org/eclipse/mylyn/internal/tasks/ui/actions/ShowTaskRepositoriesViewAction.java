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
import org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoriesView;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
/**
 *
 * @author Mik Kersten
 */
public class ShowTaskRepositoriesViewAction implements org.eclipse.ui.IViewActionDelegate {
    public void init(org.eclipse.ui.IViewPart view) {
    }

    public void run(org.eclipse.jface.action.IAction action) {
        org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoriesView.openInActivePerspective();
    }

    public void selectionChanged(org.eclipse.jface.action.IAction action, org.eclipse.jface.viewers.ISelection selection) {
        // ignore
    }
}