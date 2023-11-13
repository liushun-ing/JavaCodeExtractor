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
package org.eclipse.mylyn.internal.tasks.ui.editors;
import org.eclipse.jface.action.Action;
import org.eclipse.mylyn.internal.tasks.core.CommentQuoter;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.ITaskComment;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
/**
 *
 * @author Steffen Pingel
 */
public abstract class AbstractReplyToCommentAction extends org.eclipse.jface.action.Action {
    private final org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage editor;

    private final org.eclipse.mylyn.tasks.core.ITaskComment taskComment;

    public AbstractReplyToCommentAction(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage editor, org.eclipse.mylyn.tasks.core.ITaskComment taskComment) {
        this.editor = editor;
        this.taskComment = taskComment;
        setImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.COMMENT_REPLY);
        setText(Messages.AbstractReplyToCommentAction_Reply);
        setToolTipText(Messages.AbstractReplyToCommentAction_Reply);
    }

    protected abstract java.lang.String getReplyText();

    @java.lang.Override
    public void run() {
        org.eclipse.mylyn.internal.tasks.ui.editors.AbstractReplyToCommentAction.reply(editor, taskComment, getReplyText());
    }

    public static void reply(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage editor, org.eclipse.mylyn.tasks.core.ITaskComment taskComment, java.lang.String text) {
        org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnectorUi(editor.getConnectorKind());
        java.lang.String reference = connectorUi.getReplyText(editor.getTaskRepository(), editor.getTask(), taskComment, false);
        java.lang.StringBuilder sb = new java.lang.StringBuilder();
        sb.append(reference);
        sb.append("\n");// $NON-NLS-1$

        if (text != null) {
            org.eclipse.mylyn.internal.tasks.core.CommentQuoter quoter = new org.eclipse.mylyn.internal.tasks.core.CommentQuoter();
            sb.append(quoter.quote(text));
        }
        editor.appendTextToNewComment(sb.toString());
    }
}