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
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.internal.tasks.ui.wizards.TaskDataImportWizard;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
/**
 *
 * @author Mik Kersten
 */
public class RestoreTaskListAction extends org.eclipse.jface.action.Action implements org.eclipse.ui.IViewActionDelegate , org.eclipse.ui.IWorkbenchWindowActionDelegate {
    public void init(org.eclipse.ui.IViewPart view) {
    }

    public void run(org.eclipse.jface.action.IAction action) {
        run();
    }

    @java.lang.Override
    public void run() {
        org.eclipse.jface.wizard.IWizard wizard = new org.eclipse.mylyn.internal.tasks.ui.wizards.TaskDataImportWizard();
        org.eclipse.jface.wizard.WizardDialog dialog = new org.eclipse.jface.wizard.WizardDialog(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell(), wizard);
        dialog.create();
        dialog.setBlockOnOpen(true);
        dialog.open();
    }

    public void selectionChanged(org.eclipse.jface.action.IAction action, org.eclipse.jface.viewers.ISelection selection) {
    }

    public void dispose() {
        // ignore
    }

    public void init(org.eclipse.ui.IWorkbenchWindow window) {
        // ignore
    }
}