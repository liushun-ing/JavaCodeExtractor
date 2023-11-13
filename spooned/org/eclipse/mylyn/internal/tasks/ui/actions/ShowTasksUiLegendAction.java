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
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.internal.tasks.ui.dialogs.UiLegendDialog;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
/**
 *
 * @author Mik Kersten
 * @author Leo Dos Santos
 */
public class ShowTasksUiLegendAction implements org.eclipse.ui.IWorkbenchWindowActionDelegate , org.eclipse.ui.IViewActionDelegate {
    public void dispose() {
        // ignore
    }

    public void init(org.eclipse.ui.IWorkbenchWindow window) {
    }

    public void run(org.eclipse.jface.action.IAction action) {
        org.eclipse.mylyn.internal.tasks.ui.dialogs.UiLegendDialog uiLegendDialog = new org.eclipse.mylyn.internal.tasks.ui.dialogs.UiLegendDialog(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell());
        uiLegendDialog.open();
    }

    public void selectionChanged(org.eclipse.jface.action.IAction action, org.eclipse.jface.viewers.ISelection selection) {
        // ignore
    }

    public void init(org.eclipse.ui.IViewPart view) {
    }
}