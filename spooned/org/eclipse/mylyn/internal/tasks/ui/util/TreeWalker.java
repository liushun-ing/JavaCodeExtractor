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
package org.eclipse.mylyn.internal.tasks.ui.util;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
/**
 *
 * @author Steffen Pingel
 */
public class TreeWalker {
    public enum Direction {

        UP,
        DOWN;}

    public abstract static class TreeVisitor {
        public abstract boolean visit(java.lang.Object object);
    }

    private org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker.Direction direction = org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker.Direction.DOWN;

    private final org.eclipse.jface.viewers.TreeViewer treeViewer;

    private final org.eclipse.swt.widgets.Tree tree;

    private boolean expandNodes;

    public TreeWalker(org.eclipse.jface.viewers.TreeViewer treeViewer) {
        this.treeViewer = treeViewer;
        this.tree = treeViewer.getTree();
    }

    public org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker.Direction getDirection() {
        return direction;
    }

    public boolean getExpandNodes() {
        return expandNodes;
    }

    private org.eclipse.jface.viewers.TreePath getTreePath(org.eclipse.swt.widgets.TreeItem item) {
        java.util.List<java.lang.Object> path = new java.util.ArrayList<java.lang.Object>();
        do {
            // the tree is probably not fully refreshed at this point
            if (item.getData() == null) {
                return null;
            }
            path.add(0, item.getData());
            item = item.getParentItem();
        } while (item != null );
        return new org.eclipse.jface.viewers.TreePath(path.toArray());
    }

    public void setDirection(org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker.Direction direction) {
        this.direction = direction;
    }

    public void setExpandNodes(boolean expandNodes) {
        this.expandNodes = expandNodes;
    }

    private org.eclipse.jface.viewers.TreePath visitChildren(org.eclipse.jface.viewers.TreeViewer viewer, org.eclipse.jface.viewers.TreePath itemPath, org.eclipse.swt.widgets.TreeItem item, org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker.TreeVisitor visitor) {
        boolean restoreCollapsedState = false;
        // expand item
        try {
            if (getExpandNodes()) {
                boolean expandedState = item.getExpanded();
                if (!expandedState) {
                    restoreCollapsedState = true;
                    viewer.setExpandedState(itemPath, true);
                }
            }
            org.eclipse.swt.widgets.TreeItem[] children = item.getItems();
            if ((children.length > 0) && (children[0].getData() != null)) {
                org.eclipse.jface.viewers.TreePath childPath = visitItems(viewer, itemPath, children, null, visitor);
                if (childPath != null) {
                    return childPath;
                }
            }
        } finally {
            if (restoreCollapsedState) {
                // restore item state
                viewer.setExpandedState(itemPath, false);
            }
        }
        return null;
    }

    private org.eclipse.jface.viewers.TreePath visitItems(org.eclipse.jface.viewers.TreeViewer viewer, org.eclipse.jface.viewers.TreePath parentPath, org.eclipse.swt.widgets.TreeItem[] items, org.eclipse.swt.widgets.TreeItem visitedItem, org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker.TreeVisitor visitor) {
        if (direction == org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker.Direction.UP) {
            java.util.Collections.reverse(java.util.Arrays.asList(items));
        }
        boolean found = visitedItem == null;
        for (org.eclipse.swt.widgets.TreeItem item : items) {
            if (!found) {
                if (item == visitedItem) {
                    found = true;
                }
            } else {
                org.eclipse.jface.viewers.TreePath itemPath = parentPath.createChildPath(item.getData());
                if (direction == org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker.Direction.DOWN) {
                    if (visitor.visit(item.getData())) {
                        return itemPath;
                    }
                }
                org.eclipse.jface.viewers.TreePath childPath = visitChildren(viewer, itemPath, item, visitor);
                if (childPath != null) {
                    return childPath;
                }
                if (direction == org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker.Direction.UP) {
                    if (visitor.visit(item.getData())) {
                        return itemPath;
                    }
                }
            }
        }
        // visit parent siblings
        if (visitedItem != null) {
            org.eclipse.swt.widgets.TreeItem parent = visitedItem.getParentItem();
            if (parent != null) {
                if (direction == org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker.Direction.UP) {
                    if (visitor.visit(parent.getData())) {
                        return parentPath;
                    }
                }
                return visitSiblings(viewer, parent, visitor);
            }
        }
        return null;
    }

    private org.eclipse.jface.viewers.TreePath visitSiblings(org.eclipse.jface.viewers.TreeViewer viewer, org.eclipse.swt.widgets.TreeItem item, org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker.TreeVisitor visitor) {
        org.eclipse.swt.widgets.TreeItem[] siblings;
        org.eclipse.jface.viewers.TreePath path;
        org.eclipse.swt.widgets.TreeItem parent = item.getParentItem();
        if (parent != null) {
            path = getTreePath(parent);
            if (path == null) {
                return null;
            }
            siblings = parent.getItems();
        } else {
            path = org.eclipse.jface.viewers.TreePath.EMPTY;
            siblings = viewer.getTree().getItems();
        }
        return visitItems(viewer, path, siblings, item, visitor);
    }

    public org.eclipse.jface.viewers.TreePath walk(org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker.TreeVisitor visitor, org.eclipse.swt.widgets.TreeItem startItem) {
        org.eclipse.jface.viewers.TreePath path = null;
        if (startItem != null) {
            if (direction == org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker.Direction.DOWN) {
                path = getTreePath(startItem);
                if (path != null) {
                    path = visitChildren(treeViewer, path, startItem, visitor);
                }
            }
            if (path == null) {
                path = visitSiblings(treeViewer, startItem, visitor);
            }
        } else {
            path = visitItems(treeViewer, org.eclipse.jface.viewers.TreePath.EMPTY, tree.getItems(), null, visitor);
        }
        return path;
    }
}