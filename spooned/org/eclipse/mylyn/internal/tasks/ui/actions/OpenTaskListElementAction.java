/**
 * *****************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
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
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
/**
 *
 * @author Mik Kersten
 */
public class OpenTaskListElementAction extends org.eclipse.ui.actions.BaseSelectionListenerAction {
    public static final java.lang.String ID = "org.eclipse.mylyn.tasklist.actions.open";// $NON-NLS-1$


    private org.eclipse.jface.viewers.AbstractTreeViewer viewer;

    public OpenTaskListElementAction() {
        super(Messages.OpenTaskListElementAction_Open);
        setToolTipText(Messages.OpenTaskListElementAction_Open_Task_List_Element);
        setId(org.eclipse.mylyn.internal.tasks.ui.actions.OpenTaskListElementAction.ID);
    }

    public org.eclipse.jface.viewers.AbstractTreeViewer getViewer() {
        return viewer;
    }

    public void setViewer(org.eclipse.jface.viewers.AbstractTreeViewer viewer) {
        this.viewer = viewer;
    }

    @java.lang.Override
    public void run() {
        runWithEvent(null);
    }

    @java.lang.Override
    public void runWithEvent(org.eclipse.swt.widgets.Event event) {
        for (java.lang.Object element : getStructuredSelection().toList()) {
            if (((element instanceof org.eclipse.mylyn.tasks.core.ITask) && (event != null)) && ((event.keyCode & org.eclipse.swt.SWT.MOD1) != 0)) {
                org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.openTaskInBackground(((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (element)), true);
            } else if (element instanceof org.eclipse.mylyn.tasks.core.ITask) {
                org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.refreshAndOpenTaskListElement(((org.eclipse.mylyn.tasks.core.ITask) (element)));
            } else if (viewer != null) {
                if (viewer.getExpandedState(element)) {
                    viewer.collapseToLevel(element, 1);
                } else {
                    viewer.expandToLevel(element, 1);
                }
            }
        }
    }
}