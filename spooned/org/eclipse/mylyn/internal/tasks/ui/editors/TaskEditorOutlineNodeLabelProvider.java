/**
 * *****************************************************************************
 * Copyright (c) 2010, 2015 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.editors;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.ITaskAttachment;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchImages;
/**
 *
 * @author Frank Becker
 */
public class TaskEditorOutlineNodeLabelProvider extends org.eclipse.jface.viewers.LabelProvider {
    private static final java.lang.String[] IMAGE_EXTENSIONS = new java.lang.String[]{ "jpg", "gif", "png", "tiff", "tif", "bmp" };// $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$	@Override


    @java.lang.Override
    public org.eclipse.swt.graphics.Image getImage(java.lang.Object element) {
        if (element instanceof org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode) {
            org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode node = ((org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode) (element));
            if (TaskEditorOutlineNode.LABEL_COMMENTS.equals(node.getLabel()) || TaskEditorOutlineNode.LABEL_NEW_COMMENT.equals(node.getLabel())) {
                return org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.COMMENT);
            }
            if (TaskEditorOutlineNode.LABEL_DESCRIPTION.equals(node.getLabel())) {
                return org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.TASK_NOTES);
            } else if (node.getTaskComment() != null) {
                org.eclipse.mylyn.tasks.core.IRepositoryPerson author = node.getTaskComment().getAuthor();
                org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute = node.getData();
                java.lang.String repositoryUrl = taskAttribute.getTaskData().getRepositoryUrl();
                java.lang.String connectorKind = null;
                org.eclipse.mylyn.tasks.core.TaskRepository taskRepository = null;
                if (repositoryUrl != null) {
                    connectorKind = taskAttribute.getTaskData().getConnectorKind();
                    if (connectorKind != null) {
                        taskRepository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(connectorKind, repositoryUrl);
                    }
                }
                if (((taskRepository != null) && (author != null)) && author.matchesUsername(taskRepository.getUserName())) {
                    return org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.PERSON_ME);
                } else {
                    return org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.PERSON);
                }
            } else if (node.getTaskAttachment() != null) {
                org.eclipse.mylyn.tasks.core.ITaskAttachment attachment = node.getTaskAttachment();
                if (org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.isContext(attachment)) {
                    return org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.CONTEXT_TRANSFER);
                } else if (attachment.isPatch()) {
                    return org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.TASK_ATTACHMENT_PATCH);
                } else {
                    java.lang.String filename = attachment.getFileName();
                    if (filename != null) {
                        int dotIndex = filename.lastIndexOf('.');
                        if (dotIndex != (-1)) {
                            java.lang.String fileType = filename.substring(dotIndex + 1);
                            for (java.lang.String element2 : org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNodeLabelProvider.IMAGE_EXTENSIONS) {
                                if (element2.equalsIgnoreCase(fileType)) {
                                    return org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.IMAGE_FILE);
                                }
                            }
                        }
                    }
                    return org.eclipse.ui.internal.WorkbenchImages.getImage(org.eclipse.ui.ISharedImages.IMG_OBJ_FILE);
                }
            } else if (node.getParent() == null) {
                return org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.TASK);
            } else if (TaskEditorOutlineNode.LABEL_RELATED_TASKS.equals(node.getLabel()) || TaskEditorOutlineNode.LABEL_ATTRIBUTES.equals(node.getLabel())) {
                return org.eclipse.ui.PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ISharedImages.IMG_OBJ_FOLDER);
            }
            return org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.TASK);
        }
        return null;
    }

    @java.lang.Override
    public java.lang.String getText(java.lang.Object element) {
        if (element instanceof org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode) {
            org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode node = ((org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode) (element));
            if ((TaskEditorOutlineNode.LABEL_COMMENTS.equals(node.getLabel()) || TaskEditorOutlineNode.LABEL_NEW_COMMENT.equals(node.getLabel())) || TaskEditorOutlineNode.LABEL_DESCRIPTION.equals(node.getLabel())) {
                return node.getLabel();
            }
            if (((node.getData() != null) && (node.getTaskAttachment() == null)) && (node.getTaskComment() == null)) {
                // regular attribute
                org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute = node.getData();
                java.lang.String label = taskAttribute.getTaskData().getAttributeMapper().getLabel(taskAttribute);
                if (!label.endsWith(":")) {
                    // $NON-NLS-1$
                    label += ":";// $NON-NLS-1$

                }
                return (label + " ") + taskAttribute.getTaskData().getAttributeMapper().getValueLabel(taskAttribute);// $NON-NLS-1$

            }
            return node.getLabel();
        }
        return super.getText(element);
    }
}