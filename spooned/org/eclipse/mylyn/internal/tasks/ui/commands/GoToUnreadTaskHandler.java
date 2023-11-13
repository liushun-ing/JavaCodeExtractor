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
package org.eclipse.mylyn.internal.tasks.ui.commands;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker;
import org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker.Direction;
import org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker.TreeVisitor;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
/**
 *
 * @author Steffen Pingel
 */
public abstract class GoToUnreadTaskHandler extends org.eclipse.mylyn.internal.tasks.ui.commands.AbstractTaskListViewHandler {
    public static final java.lang.String ID_NEXT = "org.eclipse.mylyn.tasklist.actions.goToNextUnread";// $NON-NLS-1$


    public static final java.lang.String ID_PREVIOUS = "org.eclipse.mylyn.tasklist.actions.goToPreviousUnread";// $NON-NLS-1$


    private org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker.Direction direction = org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker.Direction.DOWN;

    public org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker.Direction getDirection() {
        return direction;
    }

    private org.eclipse.jface.viewers.TreePath getUnreadItem(org.eclipse.jface.viewers.TreeViewer treeViewer, org.eclipse.swt.widgets.Tree tree) {
        org.eclipse.swt.widgets.TreeItem[] selection = tree.getSelection();
        org.eclipse.swt.widgets.TreeItem selectedItem = (selection.length > 0) ? selection[0] : null;
        org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker.TreeVisitor visitor = new org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker.TreeVisitor() {
            @java.lang.Override
            public boolean visit(java.lang.Object object) {
                if (object instanceof org.eclipse.mylyn.tasks.core.ITask) {
                    org.eclipse.mylyn.tasks.core.ITask task = ((org.eclipse.mylyn.tasks.core.ITask) (object));
                    if (org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.shouldShowIncoming(task)) {
                        // task.getSynchronizationState().isIncoming()
                        return true;
                    }
                }
                return false;
            }
        };
        org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker treeWalker = new org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker(treeViewer);
        treeWalker.setDirection(direction);
        treeWalker.setExpandNodes(true);
        return treeWalker.walk(visitor, selectedItem);
    }

    @java.lang.Override
    protected void execute(org.eclipse.core.commands.ExecutionEvent event, org.eclipse.mylyn.internal.tasks.ui.views.TaskListView taskListView, org.eclipse.mylyn.tasks.core.IRepositoryElement item) {
        org.eclipse.jface.viewers.TreeViewer treeViewer = taskListView.getViewer();
        org.eclipse.swt.widgets.Tree tree = treeViewer.getTree();
        // need to expand nodes to traverse the tree, disable redraw to avoid flickering
        org.eclipse.jface.viewers.TreePath treePath = null;
        try {
            tree.setRedraw(false);
            treePath = getUnreadItem(treeViewer, tree);
        } finally {
            tree.setRedraw(true);
        }
        if (treePath != null) {
            treeViewer.expandToLevel(treePath, 0);
            treeViewer.setSelection(new org.eclipse.jface.viewers.TreeSelection(treePath), true);
        }
    }

    public void setDirection(org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker.Direction direction) {
        this.direction = direction;
    }

    public static void execute(org.eclipse.core.commands.ExecutionEvent event, org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker.Direction direction) throws org.eclipse.core.commands.ExecutionException {
        org.eclipse.mylyn.internal.tasks.ui.commands.GoToUnreadTaskHandler handler = new org.eclipse.mylyn.internal.tasks.ui.commands.GoToUnreadTaskHandler() {};
        handler.setDirection(direction);
        handler.execute(event);
    }

    public static class GoToNextUnreadTaskHandler extends org.eclipse.mylyn.internal.tasks.ui.commands.GoToUnreadTaskHandler {
        public GoToNextUnreadTaskHandler() {
            setDirection(org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker.Direction.DOWN);
        }
    }

    public static class GoToPreviousUnreadTaskHandler extends org.eclipse.mylyn.internal.tasks.ui.commands.GoToUnreadTaskHandler {
        public GoToPreviousUnreadTaskHandler() {
            setDirection(org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker.Direction.UP);
        }
    }
}