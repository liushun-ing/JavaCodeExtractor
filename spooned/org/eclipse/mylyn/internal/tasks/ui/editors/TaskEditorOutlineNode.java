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
package org.eclipse.mylyn.internal.tasks.ui.editors;
import org.eclipse.core.runtime.Assert;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskAttachment;
import org.eclipse.mylyn.tasks.core.ITaskComment;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskRelation;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.osgi.util.NLS;
/**
 * A node for the tree in {@link TaskEditorOutlinePage}.
 *
 * @author Steffen Pingel
 */
public class TaskEditorOutlineNode {
    public static final java.lang.String LABEL_COMMENTS = Messages.TaskEditorOutlineNode_Comments;

    public static final java.lang.String LABEL_DESCRIPTION = Messages.TaskEditorOutlineNode_Description;

    public static final java.lang.String LABEL_NEW_COMMENT = Messages.TaskEditorOutlineNode_New_Comment;

    public static final java.lang.String LABEL_ATTACHMENTS = Messages.TaskEditorOutlineNode_Attachments;

    public static final java.lang.String LABEL_ATTRIBUTES = Messages.TaskEditorOutlineNode_Attributes;

    public static final java.lang.String LABEL_RELATED_TASKS = Messages.TaskEditorOutlineNode_Related_Tasks;

    private static org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode createNode(org.eclipse.mylyn.tasks.core.data.TaskData taskData, java.lang.String attributeId, java.lang.String label) {
        org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute = taskData.getRoot().getMappedAttribute(attributeId);
        if (taskAttribute != null) {
            if (label == null) {
                label = taskAttribute.getValue();
            }
            return new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode(label, taskAttribute);
        }
        return null;
    }

    private static org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode createNode(org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute) {
        java.lang.String type = taskAttribute.getMetaData().getType();
        if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_COMMENT.equals(type)) {
            org.eclipse.mylyn.tasks.core.ITaskComment taskComment = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryModel().createTaskComment(taskAttribute);
            if (taskComment != null) {
                taskAttribute.getTaskData().getAttributeMapper().updateTaskComment(taskComment, taskAttribute);
                java.lang.StringBuilder sb = new java.lang.StringBuilder();
                sb.append(taskComment.getNumber());
                sb.append(": ");// $NON-NLS-1$

                org.eclipse.mylyn.tasks.core.IRepositoryPerson author = taskComment.getAuthor();
                if (author != null) {
                    sb.append(author.toString());
                }
                java.util.Date creationDate = taskComment.getCreationDate();
                if (creationDate != null) {
                    sb.append(" (");// $NON-NLS-1$

                    sb.append(org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.formatDateTime(creationDate));
                    sb.append(")");// $NON-NLS-1$

                }
                org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode node = new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode(sb.toString(), taskAttribute);
                node.setTaskComment(taskComment);
                return node;
            }
        } else if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_ATTACHMENT.equals(type)) {
            org.eclipse.mylyn.tasks.core.ITaskAttachment taskAttachment = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryModel().createTaskAttachment(taskAttribute);
            if (taskAttachment != null) {
                taskAttribute.getTaskData().getAttributeMapper().updateTaskAttachment(taskAttachment, taskAttribute);
                java.lang.StringBuilder sb = new java.lang.StringBuilder();
                sb.append(taskAttribute.getTaskData().getAttributeMapper().getValueLabel(taskAttribute));
                sb.append(": ");// $NON-NLS-1$

                if (org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.isContext(taskAttachment)) {
                    sb.append(Messages.AttachmentTableLabelProvider_Task_Context);
                } else if (taskAttachment.isPatch()) {
                    sb.append(Messages.AttachmentTableLabelProvider_Patch);
                } else {
                    sb.append(taskAttachment.getFileName());
                }
                org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode node = new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode(sb.toString(), taskAttribute);
                node.setTaskAttachment(taskAttachment);
                return node;
            }
        } else {
            java.lang.String label = taskAttribute.getTaskData().getAttributeMapper().getLabel(taskAttribute);
            if (label.endsWith(":")) {
                // $NON-NLS-1$
                label = label.substring(0, label.length() - 1);
            }
            return new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode(label, taskAttribute);
        }
        return null;
    }

    public static org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode parse(org.eclipse.mylyn.tasks.core.data.TaskData taskData, boolean includeAttributes) {
        org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode rootNode = org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode.createNode(taskData, org.eclipse.mylyn.tasks.core.data.TaskAttribute.SUMMARY, null);
        if (rootNode == null) {
            rootNode = new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode(Messages.TaskEditorOutlineNode_Task_ + taskData.getTaskId());
        }
        if (includeAttributes) {
            final org.eclipse.mylyn.internal.tasks.core.TaskList taskList = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList();
            org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode relatedTasksNode = new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode.LABEL_RELATED_TASKS);
            rootNode.addChild(relatedTasksNode);
            org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryConnector(taskData.getConnectorKind());
            java.util.Collection<org.eclipse.mylyn.tasks.core.data.TaskRelation> relations = connector.getTaskRelations(taskData);
            org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager manager = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryManager();
            org.eclipse.mylyn.tasks.core.TaskRepository taskRepository = manager.getRepository(taskData.getConnectorKind(), taskData.getRepositoryUrl());
            if (relations != null) {
                for (org.eclipse.mylyn.tasks.core.data.TaskRelation taskRelation : relations) {
                    org.eclipse.mylyn.tasks.core.ITask task = taskList.getTask(taskData.getRepositoryUrl(), taskRelation.getTaskId());
                    java.lang.String label;
                    if (task != null) {
                        if (task.getTaskKey() != null) {
                            label = org.eclipse.osgi.util.NLS.bind(Messages.TaskEditorOutlineNode_TaskRelation_Label, new java.lang.Object[]{ task.getTaskKey(), task.getSummary() });
                        } else {
                            label = task.getSummary();
                        }
                    } else {
                        label = org.eclipse.osgi.util.NLS.bind(Messages.TaskEditorOutlineNode_TaskRelation_Label, new java.lang.Object[]{ taskRelation.getTaskId(), Messages.TaskEditorOutlineNode_unknown_Label });
                    }
                    org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode childNode = new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode(label);
                    childNode.setTaskRelation(taskRelation);
                    childNode.setTaskRepository(taskRepository);
                    relatedTasksNode.addChild(childNode);
                }
            }
            org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode attributesNode = new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode.LABEL_ATTRIBUTES);
            rootNode.addChild(attributesNode);
            java.util.Map<java.lang.String, org.eclipse.mylyn.tasks.core.data.TaskAttribute> attributes = taskData.getRoot().getAttributes();
            for (org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute : attributes.values()) {
                if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.KIND_DEFAULT.equals(attribute.getMetaData().getKind())) {
                    org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode node = org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode.createNode(attribute);
                    if (node != null) {
                        attributesNode.addChild(node);
                    }
                }
            }
        }
        org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode.addNode(rootNode, taskData, org.eclipse.mylyn.tasks.core.data.TaskAttribute.DESCRIPTION, org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode.LABEL_DESCRIPTION);
        java.util.List<org.eclipse.mylyn.tasks.core.data.TaskAttribute> attachments = taskData.getAttributeMapper().getAttributesByType(taskData, org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_ATTACHMENT);
        if (attachments.size() > 0) {
            org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode attachmentNode = new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode.LABEL_ATTACHMENTS);
            rootNode.addChild(attachmentNode);
            for (org.eclipse.mylyn.tasks.core.data.TaskAttribute attachmentAttribute : attachments) {
                org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode node = org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode.createNode(attachmentAttribute);
                if (node != null) {
                    attachmentNode.addChild(node);
                }
            }
        }
        java.util.List<org.eclipse.mylyn.tasks.core.data.TaskAttribute> comments = taskData.getAttributeMapper().getAttributesByType(taskData, org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_COMMENT);
        if (comments.size() > 0) {
            org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode commentsNode = new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode.LABEL_COMMENTS);
            rootNode.addChild(commentsNode);
            for (org.eclipse.mylyn.tasks.core.data.TaskAttribute commentAttribute : comments) {
                org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode node = org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode.createNode(commentAttribute);
                if (node != null) {
                    commentsNode.addChild(node);
                }
            }
        }
        org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode.addNode(rootNode, taskData, org.eclipse.mylyn.tasks.core.data.TaskAttribute.COMMENT_NEW, org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode.LABEL_NEW_COMMENT);
        return rootNode;
    }

    private static org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode addNode(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode parentNode, org.eclipse.mylyn.tasks.core.data.TaskData taskData, java.lang.String attributeId, java.lang.String label) {
        org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode node = org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode.createNode(taskData, attributeId, label);
        if (node != null) {
            parentNode.addChild(node);
        }
        return node;
    }

    private java.util.List<org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode> children;

    private final java.lang.String label;

    /**
     * The parent of this node or null if it is the bug report
     */
    private org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode parent;

    private final org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute;

    private org.eclipse.mylyn.tasks.core.ITaskComment taskComment;

    private org.eclipse.mylyn.tasks.core.ITaskAttachment taskAttachment;

    private org.eclipse.mylyn.tasks.core.data.TaskRelation taskRelation;

    private org.eclipse.mylyn.tasks.core.TaskRepository taskRepository;

    public TaskEditorOutlineNode(java.lang.String label) {
        this(label, null);
    }

    public TaskEditorOutlineNode(java.lang.String label, org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute) {
        this.label = label;
        this.taskAttribute = taskAttribute;
    }

    public void addChild(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode node) {
        org.eclipse.core.runtime.Assert.isNotNull(node);
        if (children == null) {
            children = new java.util.ArrayList<org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode>();
        }
        node.parent = this;
        children.add(node);
    }

    public org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode getChild(java.lang.String label) {
        if (children != null) {
            for (org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode child : children) {
                if (child.getLabel().equals(label)) {
                    return child;
                }
            }
        }
        return null;
    }

    /**
     *
     * @return <code>true</code> if the given object is another node representing the same piece of data in the editor.
     */
    @java.lang.Override
    public boolean equals(java.lang.Object o) {
        if (o instanceof org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode) {
            org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode node = ((org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode) (o));
            return getLabel().equals(node.getLabel());
        }
        return false;
    }

    public org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode[] getChildren() {
        return children == null ? new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode[0] : children.toArray(new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode[children.size()]);
    }

    public org.eclipse.mylyn.tasks.core.ITaskComment getTaskComment() {
        return taskComment;
    }

    public void setTaskComment(org.eclipse.mylyn.tasks.core.ITaskComment taskComment) {
        this.taskComment = taskComment;
    }

    public org.eclipse.mylyn.tasks.core.data.TaskAttribute getData() {
        return taskAttribute;
    }

    public java.lang.String getLabel() {
        return label;
    }

    public org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode getParent() {
        return parent;
    }

    @java.lang.Override
    public int hashCode() {
        return getLabel().hashCode();
    }

    @java.lang.Override
    public java.lang.String toString() {
        return getLabel();
    }

    public org.eclipse.mylyn.tasks.core.ITaskAttachment getTaskAttachment() {
        return taskAttachment;
    }

    public void setTaskAttachment(org.eclipse.mylyn.tasks.core.ITaskAttachment taskAttachment) {
        this.taskAttachment = taskAttachment;
    }

    public org.eclipse.mylyn.tasks.core.data.TaskRelation getTaskRelation() {
        return taskRelation;
    }

    public void setTaskRelation(org.eclipse.mylyn.tasks.core.data.TaskRelation taskRelation) {
        this.taskRelation = taskRelation;
    }

    public org.eclipse.mylyn.tasks.core.TaskRepository getTaskRepository() {
        return taskRepository;
    }

    public void setTaskRepository(org.eclipse.mylyn.tasks.core.TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
}