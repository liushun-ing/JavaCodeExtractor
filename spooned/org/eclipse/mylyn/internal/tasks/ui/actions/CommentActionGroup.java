/**
 * *****************************************************************************
 * Copyright (c) 2004, 2013 Tasktop Technologies and others.
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
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.tasks.core.ITaskComment;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;
/**
 *
 * @author Steffen Pingel
 */
public class CommentActionGroup extends org.eclipse.ui.actions.ActionGroup {
    private org.eclipse.mylyn.internal.tasks.ui.actions.CopyCommentDetailsAction copyDetailsAction;

    private org.eclipse.mylyn.internal.tasks.ui.actions.CopyCommenterNameAction copyCommenterNameAction;

    private org.eclipse.mylyn.internal.tasks.ui.actions.CopyCommentDetailsUrlAction copyCommentDetailsURL;

    private boolean initialized;

    private void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        copyDetailsAction = new org.eclipse.mylyn.internal.tasks.ui.actions.CopyCommentDetailsAction();
        copyCommenterNameAction = new org.eclipse.mylyn.internal.tasks.ui.actions.CopyCommenterNameAction();
        copyCommentDetailsURL = new org.eclipse.mylyn.internal.tasks.ui.actions.CopyCommentDetailsUrlAction();
    }

    @java.lang.Override
    public void fillContextMenu(org.eclipse.jface.action.IMenuManager manager) {
        updateActions();
        manager.add(copyDetailsAction);
        manager.add(copyCommenterNameAction);
        manager.add(copyCommentDetailsURL);
    }

    private void updateActions() {
        initialize();
        org.eclipse.jface.viewers.IStructuredSelection selection = getStructuredSelection();
        copyDetailsAction.selectionChanged(selection);
        copyCommenterNameAction.selectionChanged(selection);
        copyCommentDetailsURL.selectionChanged(selection);
        java.lang.Object firstElement = selection.getFirstElement();
        if (firstElement instanceof org.eclipse.mylyn.tasks.core.ITaskComment) {
            copyCommentDetailsURL.setEnabled(((org.eclipse.mylyn.tasks.core.ITaskComment) (firstElement)).getUrl() != null);
        }
    }

    public org.eclipse.jface.viewers.IStructuredSelection getStructuredSelection() {
        org.eclipse.ui.actions.ActionContext context = getContext();
        if (context != null) {
            org.eclipse.jface.viewers.ISelection selection = context.getSelection();
            if (selection instanceof org.eclipse.jface.viewers.IStructuredSelection) {
                return ((org.eclipse.jface.viewers.IStructuredSelection) (selection));
            }
        }
        return org.eclipse.jface.viewers.StructuredSelection.EMPTY;
    }
}