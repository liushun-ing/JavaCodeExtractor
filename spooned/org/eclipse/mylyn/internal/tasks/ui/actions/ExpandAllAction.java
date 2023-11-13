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
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
/**
 *
 * @author Mik Kersten
 */
public class ExpandAllAction extends org.eclipse.jface.action.Action {
    public static final java.lang.String ID = "org.eclipse.mylyn.tasklist.actions.expand.all";// $NON-NLS-1$


    private final org.eclipse.mylyn.internal.tasks.ui.views.TaskListView taskListView;

    public ExpandAllAction(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView taskListView) {
        this.taskListView = taskListView;
        setId(org.eclipse.mylyn.internal.tasks.ui.actions.ExpandAllAction.ID);
        setText(Messages.ExpandAllAction_Expand_All);
        setToolTipText(Messages.ExpandAllAction_Expand_All);
        setImageDescriptor(org.eclipse.mylyn.commons.ui.CommonImages.EXPAND_ALL);
    }

    @java.lang.Override
    public void run() {
        if (taskListView.getViewer() != null) {
            taskListView.getViewer().expandAll();
        }
    }
}