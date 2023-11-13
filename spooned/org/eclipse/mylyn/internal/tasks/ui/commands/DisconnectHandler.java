/**
 * *****************************************************************************
 * Copyright (c) 2012, 2013 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *     Tasktop Technologies - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.commands;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.menus.UIElement;
/**
 *
 * @author Frank Becker
 * @author Steffen Pingel
 */
public class DisconnectHandler extends org.eclipse.core.commands.AbstractHandler implements org.eclipse.ui.commands.IElementUpdater {
    public java.lang.Object execute(org.eclipse.core.commands.ExecutionEvent event) throws org.eclipse.core.commands.ExecutionException {
        org.eclipse.core.commands.Command command0 = event.getCommand();
        boolean oldValue = org.eclipse.ui.handlers.HandlerUtil.toggleCommandState(command0);
        org.eclipse.jface.viewers.ISelection selection = org.eclipse.ui.handlers.HandlerUtil.getCurrentSelection(event);
        if (selection instanceof org.eclipse.jface.viewers.IStructuredSelection) {
            for (java.util.Iterator<?> iter = ((org.eclipse.jface.viewers.IStructuredSelection) (selection)).iterator(); iter.hasNext();) {
                java.lang.Object item = iter.next();
                if (item instanceof org.eclipse.mylyn.tasks.core.TaskRepository) {
                    ((org.eclipse.mylyn.tasks.core.TaskRepository) (item)).setOffline(!oldValue);
                    org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryManager().notifyRepositorySettingsChanged(((org.eclipse.mylyn.tasks.core.TaskRepository) (item)));
                }
            }
        }
        return null;
    }

    public void updateElement(org.eclipse.ui.menus.UIElement element, @java.lang.SuppressWarnings("rawtypes")
    java.util.Map parameters) {
        org.eclipse.ui.IWorkbenchWindow window = ((org.eclipse.ui.IWorkbenchWindow) (element.getServiceLocator().getService(org.eclipse.ui.IWorkbenchWindow.class)));
        if (window != null) {
            org.eclipse.ui.IWorkbenchPage activePage = ((org.eclipse.ui.internal.WorkbenchWindow) (window)).getActivePage();
            if (activePage != null) {
                org.eclipse.ui.IWorkbenchPart activePart = activePage.getActivePart();
                if (activePart != null) {
                    org.eclipse.jface.viewers.ISelectionProvider selectionProvider = activePart.getSite().getSelectionProvider();
                    if (selectionProvider != null) {
                        org.eclipse.jface.viewers.ISelection selection = selectionProvider.getSelection();
                        if (selection instanceof org.eclipse.jface.viewers.IStructuredSelection) {
                            // only for enabled Handlers the updateElement is called
                            // so we only need the first repository for set the state
                            java.lang.Object firstRepository = ((org.eclipse.jface.viewers.IStructuredSelection) (selection)).getFirstElement();
                            if (firstRepository instanceof org.eclipse.mylyn.tasks.core.TaskRepository) {
                                boolean checked = ((org.eclipse.mylyn.tasks.core.TaskRepository) (firstRepository)).isOffline();
                                element.setChecked(checked);
                            }
                        }
                    }
                }
            }
        }
    }
}