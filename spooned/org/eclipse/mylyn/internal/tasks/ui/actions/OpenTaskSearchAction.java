/**
 * *****************************************************************************
 * Copyright (c) 2004, 2010 Tasktop Technologies and others.
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
import org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
/**
 *
 * @author Mik Kersten
 * @author Steffen Pingel
 */
public class OpenTaskSearchAction extends org.eclipse.jface.action.Action implements org.eclipse.ui.IViewActionDelegate {
    public OpenTaskSearchAction() {
    }

    public void init(org.eclipse.ui.IViewPart view) {
        // ignore
    }

    @java.lang.Override
    public void run() {
        org.eclipse.ui.IWorkbenchWindow window = org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil.openSearchDialog(window);
        }
    }

    public void run(org.eclipse.jface.action.IAction action) {
        run();
    }

    public void selectionChanged(org.eclipse.jface.action.IAction action, org.eclipse.jface.viewers.ISelection selection) {
        // ignore
    }
}