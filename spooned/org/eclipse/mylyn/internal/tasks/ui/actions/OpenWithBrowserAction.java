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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
/**
 *
 * @author Mik Kersten
 * @author Rob Elves
 * @author Steffen Pingel
 */
public class OpenWithBrowserAction extends org.eclipse.ui.actions.BaseSelectionListenerAction {
    public static final java.lang.String ID = "org.eclipse.mylyn.tasklist.actions.open.browser";// $NON-NLS-1$


    public OpenWithBrowserAction() {
        super(Messages.OpenWithBrowserAction_Open_with_Browser);
        setToolTipText(Messages.OpenWithBrowserAction_Open_with_Browser);
        setId(org.eclipse.mylyn.internal.tasks.ui.actions.OpenWithBrowserAction.ID);
    }

    @java.lang.Override
    public void run() {
        if (super.getStructuredSelection() != null) {
            for (java.util.Iterator<?> iter = super.getStructuredSelection().iterator(); iter.hasNext();) {
                runWithSelection(iter.next());
            }
        }
    }

    private void runWithSelection(java.lang.Object selectedObject) {
        if (selectedObject instanceof org.eclipse.mylyn.tasks.core.IRepositoryElement) {
            org.eclipse.mylyn.tasks.ui.TasksUiUtil.openWithBrowser(((org.eclipse.mylyn.tasks.core.IRepositoryElement) (selectedObject)));
        }
    }

    @java.lang.Override
    protected boolean updateSelection(org.eclipse.jface.viewers.IStructuredSelection selection) {
        if (!selection.isEmpty()) {
            for (java.lang.Object element : selection.toList()) {
                if (element instanceof org.eclipse.mylyn.tasks.core.IRepositoryElement) {
                    org.eclipse.mylyn.tasks.core.TaskRepository repository = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getRepository(((org.eclipse.mylyn.tasks.core.IRepositoryElement) (element)));
                    if (repository != null) {
                        java.lang.String url = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getAuthenticatedUrl(repository, ((org.eclipse.mylyn.tasks.core.IRepositoryElement) (element)));
                        if (org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.isValidUrl(url)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}