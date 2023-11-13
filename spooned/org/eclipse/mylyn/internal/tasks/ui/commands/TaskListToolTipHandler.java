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
package org.eclipse.mylyn.internal.tasks.ui.commands;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListToolTip;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
/**
 *
 * @author Steffen Pingel
 */
public class TaskListToolTipHandler extends org.eclipse.mylyn.internal.tasks.ui.commands.AbstractTaskListViewHandler {
    @java.lang.Override
    protected void execute(org.eclipse.core.commands.ExecutionEvent event, org.eclipse.mylyn.internal.tasks.ui.views.TaskListView taskListView) throws org.eclipse.core.commands.ExecutionException {
        org.eclipse.mylyn.internal.tasks.ui.views.TaskListToolTip toolTip = taskListView.getToolTip();
        if (toolTip.isVisible()) {
            toolTip.hide();
        } else {
            taskListView.getViewer().getControl().getBounds();
            org.eclipse.swt.widgets.Tree tree = taskListView.getViewer().getTree();
            org.eclipse.swt.widgets.TreeItem[] selection = tree.getSelection();
            if (selection.length > 0) {
                toolTip.show(new org.eclipse.swt.graphics.Point(selection[0].getBounds().x + 1, selection[0].getBounds().y + 1));
            }
        }
    }
}