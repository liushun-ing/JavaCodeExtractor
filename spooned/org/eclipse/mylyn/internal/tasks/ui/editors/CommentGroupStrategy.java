/**
 * *****************************************************************************
 * Copyright (c) 2004, 2015 Jingwen Ou and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jingwen Ou - initial API and implementation
 *     Tasktop Technologies - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.editors;
import org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.ITaskComment;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
/**
 *
 * @author Jingwen Ou
 * @author Steffen Pingel
 */
public class CommentGroupStrategy {
    public static class CommentGroup {
        public static final java.lang.String CURRENT = Messages.CommentGroupStrategy_Current;

        public static final java.lang.String OLDER = Messages.CommentGroupStrategy_Older;

        public static final java.lang.String RECENT = Messages.CommentGroupStrategy_Recent;

        private final java.util.List<org.eclipse.mylyn.tasks.core.ITaskComment> comments;

        private final java.lang.String groupName;

        private final boolean incoming;

        public CommentGroup(java.lang.String groupName, java.util.List<org.eclipse.mylyn.tasks.core.ITaskComment> comments, boolean incoming) {
            this.groupName = groupName;
            this.comments = comments;
            this.incoming = incoming;
        }

        public java.util.List<org.eclipse.mylyn.tasks.core.data.TaskAttribute> getCommentAttributes() {
            java.util.List<org.eclipse.mylyn.tasks.core.data.TaskAttribute> commentAttributes = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.data.TaskAttribute>(comments.size());
            for (org.eclipse.mylyn.tasks.core.ITaskComment comment : comments) {
                commentAttributes.add(comment.getTaskAttribute());
            }
            return java.util.Collections.unmodifiableList(commentAttributes);
        }

        public java.util.List<org.eclipse.mylyn.tasks.core.ITaskComment> getComments() {
            return java.util.Collections.unmodifiableList(comments);
        }

        public java.lang.String getGroupName() {
            return groupName;
        }

        public boolean hasIncoming() {
            return incoming;
        }
    }

    // public for testing
    public static final int MAX_CURRENT = 12;

    // public for testing
    public static final int MAX_RECENT = 20;

    /**
     * Groups comments according to "Older", "Recent" and "Current".
     *
     * @param comments
     * 		a list of comments to be grouped
     * @param currentPersonId
     * 		current login user id
     * @return a list of groups
     */
    public java.util.List<org.eclipse.mylyn.internal.tasks.ui.editors.CommentGroupStrategy.CommentGroup> groupComments(java.util.List<org.eclipse.mylyn.tasks.core.ITaskComment> comments, java.lang.String currentPersonId) {
        if (comments.size() == 0) {
            return java.util.Collections.emptyList();
        }
        java.util.List<org.eclipse.mylyn.internal.tasks.ui.editors.CommentGroupStrategy.CommentGroup> commentGroups = new java.util.ArrayList<org.eclipse.mylyn.internal.tasks.ui.editors.CommentGroupStrategy.CommentGroup>(3);
        // current
        java.util.List<org.eclipse.mylyn.tasks.core.ITaskComment> current = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.ITaskComment>(org.eclipse.mylyn.internal.tasks.ui.editors.CommentGroupStrategy.MAX_CURRENT);
        if (comments.size() > org.eclipse.mylyn.internal.tasks.ui.editors.CommentGroupStrategy.MAX_CURRENT) {
            for (int i = comments.size() - 1; i >= 0; i--) {
                org.eclipse.mylyn.tasks.core.ITaskComment comment = comments.get(i);
                if (isCurrent(current, comment, currentPersonId)) {
                    current.add(comment);
                }
            }
            java.util.Collections.reverse(current);
        } else {
            current = comments;
        }
        commentGroups.add(new org.eclipse.mylyn.internal.tasks.ui.editors.CommentGroupStrategy.CommentGroup(org.eclipse.mylyn.internal.tasks.ui.editors.CommentGroupStrategy.CommentGroup.CURRENT, current, hasIncomingChanges(current)));
        // recent
        if (comments.size() > current.size()) {
            int recentToIndex = comments.size() - current.size();
            int recentFromIndex = java.lang.Math.max(recentToIndex - org.eclipse.mylyn.internal.tasks.ui.editors.CommentGroupStrategy.MAX_RECENT, 0);
            java.util.List<org.eclipse.mylyn.tasks.core.ITaskComment> recent = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.ITaskComment>(comments.subList(recentFromIndex, recentToIndex));
            if (recent.size() > 0) {
                commentGroups.add(new org.eclipse.mylyn.internal.tasks.ui.editors.CommentGroupStrategy.CommentGroup(org.eclipse.mylyn.internal.tasks.ui.editors.CommentGroupStrategy.CommentGroup.RECENT, recent, hasIncomingChanges(recent)));
                // the rest goes to older
                if (comments.size() > (current.size() + recent.size())) {
                    int olderToIndex = (comments.size() - current.size()) - recent.size();
                    java.util.List<org.eclipse.mylyn.tasks.core.ITaskComment> older = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.ITaskComment>(comments.subList(0, olderToIndex));
                    commentGroups.add(new org.eclipse.mylyn.internal.tasks.ui.editors.CommentGroupStrategy.CommentGroup(org.eclipse.mylyn.internal.tasks.ui.editors.CommentGroupStrategy.CommentGroup.OLDER, older, hasIncomingChanges(older)));
                }
            }
        }
        java.util.Collections.reverse(commentGroups);
        return commentGroups;
    }

    protected boolean hasIncomingChanges(org.eclipse.mylyn.tasks.core.ITaskComment taskComment) {
        return false;
    }

    private boolean hasIncomingChanges(java.util.List<org.eclipse.mylyn.tasks.core.ITaskComment> comments) {
        for (org.eclipse.mylyn.tasks.core.ITaskComment comment : comments) {
            if (hasIncomingChanges(comment)) {
                return true;
            }
        }
        return false;
    }

    // public for testing
    public boolean isCurrent(java.util.List<org.eclipse.mylyn.tasks.core.ITaskComment> current, org.eclipse.mylyn.tasks.core.ITaskComment comment, java.lang.String currentPersonId) {
        if (current.size() >= org.eclipse.mylyn.internal.tasks.ui.editors.CommentGroupStrategy.MAX_CURRENT) {
            return false;
        }
        // add all incoming changes
        if (hasIncomingChanges(comment)) {
            return true;
        }
        if (!current.isEmpty()) {
            // check if last comment was by current user
            org.eclipse.mylyn.tasks.core.ITaskComment lastComment = current.get(current.size() - 1);
            org.eclipse.mylyn.tasks.core.IRepositoryPerson lastPerson = lastComment.getAuthor();
            if ((lastPerson != null) && lastPerson.matchesUsername(currentPersonId)) {
                // bug 238038 comment #58, if the latest comment is generated automatically, look back one comment
                org.eclipse.mylyn.tasks.core.IRepositoryPerson person = comment.getAuthor();
                if ((((person != null) && person.getPersonId().equals(currentPersonId)) && (lastComment.getText() != null)) && lastComment.getText().contains(org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.CONTEXT_DESCRIPTION)) {
                    return true;
                }
                return false;
            }
        }
        return true;
    }
}