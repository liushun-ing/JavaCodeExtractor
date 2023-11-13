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
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.intro.IIntroPart;
/**
 *
 * @author Mik Kersten
 * @author Leo Dos Santos
 */
public class WelcomeIntroAction implements org.eclipse.ui.IWorkbenchWindowActionDelegate , org.eclipse.ui.IViewActionDelegate {
    private org.eclipse.ui.IWorkbenchWindow wbWindow;

    public void dispose() {
        // ignore
    }

    public void init(org.eclipse.ui.IViewPart view) {
        wbWindow = view.getViewSite().getWorkbenchWindow();
    }

    public void init(org.eclipse.ui.IWorkbenchWindow window) {
        wbWindow = window;
    }

    public void run(org.eclipse.jface.action.IAction action) {
        org.eclipse.ui.intro.IIntroManager introMgr = wbWindow.getWorkbench().getIntroManager();
        org.eclipse.ui.intro.IIntroPart intro = introMgr.getIntro();
        if (intro != null) {
            try {
                introMgr.setIntroStandby(intro, true);
            } catch (java.lang.NullPointerException e) {
                // bug 270351: ignore exception
            }
        }
        org.eclipse.mylyn.tasks.ui.TasksUiUtil.openTasksViewInActivePerspective();
        new org.eclipse.mylyn.internal.tasks.ui.actions.AddRepositoryAction().showWizard(getShell(), null);
    }

    private org.eclipse.swt.widgets.Shell getShell() {
        return wbWindow != null ? wbWindow.getShell() : org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell();
    }

    public void selectionChanged(org.eclipse.jface.action.IAction action, org.eclipse.jface.viewers.ISelection selection) {
        // ignore
    }
}