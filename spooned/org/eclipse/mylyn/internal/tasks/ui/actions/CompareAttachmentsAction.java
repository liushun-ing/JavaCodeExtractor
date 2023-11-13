/**
 * *****************************************************************************
 * Copyright (c) 2004, 2011 Eugene Kuleshov and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugene Kuleshov - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.actions;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskAttachment;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.internal.WorkbenchImages;
/**
 *
 * @author Eugene Kuleshov
 */
public class CompareAttachmentsAction extends org.eclipse.ui.actions.BaseSelectionListenerAction implements org.eclipse.ui.IViewActionDelegate {
    private org.eclipse.jface.viewers.ISelection currentSelection;

    public CompareAttachmentsAction() {
        super(Messages.CompareAttachmentsAction_Compare_Attachments);
    }

    protected CompareAttachmentsAction(java.lang.String text) {
        super(text);
    }

    public void init(org.eclipse.ui.IViewPart view) {
        // ignore
    }

    public void run(org.eclipse.jface.action.IAction action) {
        if (currentSelection instanceof org.eclipse.jface.viewers.IStructuredSelection) {
            org.eclipse.jface.viewers.IStructuredSelection selection = ((org.eclipse.jface.viewers.IStructuredSelection) (currentSelection));
            java.lang.Object[] elements = selection.toArray();
            if (elements.length >= 2) {
                final org.eclipse.mylyn.tasks.core.ITaskAttachment attachment1 = ((org.eclipse.mylyn.tasks.core.ITaskAttachment) (elements[0]));
                final org.eclipse.mylyn.tasks.core.ITaskAttachment attachment2 = ((org.eclipse.mylyn.tasks.core.ITaskAttachment) (elements[1]));
                org.eclipse.compare.CompareConfiguration cc = new org.eclipse.compare.CompareConfiguration();
                cc.setLeftEditable(false);
                cc.setLeftLabel(attachment1.getFileName());
                cc.setLeftImage(getImage(attachment1));
                cc.setRightEditable(false);
                cc.setRightLabel(attachment2.getFileName());
                cc.setRightImage(getImage(attachment2));
                org.eclipse.compare.CompareEditorInput editorInput = new org.eclipse.compare.CompareEditorInput(cc) {
                    @java.lang.Override
                    public java.lang.String getTitle() {
                        return (((Messages.CompareAttachmentsAction_Compare__ + attachment1.getFileName()) + " - ") + attachment2.getFileName()) + ")";// $NON-NLS-1$ //$NON-NLS-2$

                    }

                    @java.lang.Override
                    protected java.lang.Object prepareInput(org.eclipse.core.runtime.IProgressMonitor pm) throws java.lang.reflect.InvocationTargetException {
                        org.eclipse.mylyn.internal.tasks.ui.actions.CompareAttachmentsAction.CompareItem left = new org.eclipse.mylyn.internal.tasks.ui.actions.CompareAttachmentsAction.CompareItem(attachment1);
                        org.eclipse.mylyn.internal.tasks.ui.actions.CompareAttachmentsAction.CompareItem right = new org.eclipse.mylyn.internal.tasks.ui.actions.CompareAttachmentsAction.CompareItem(attachment2);
                        return new org.eclipse.compare.structuremergeviewer.DiffNode(left, right);
                    }
                };
                org.eclipse.compare.CompareUI.openCompareEditor(editorInput);
            }
        }
    }

    private static final java.lang.String[] IMAGE_EXTENSIONS = new java.lang.String[]{ ".jpg", ".gif", ".png", ".tiff", ".tif", ".bmp" };// $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$


    private org.eclipse.swt.graphics.Image getImage(org.eclipse.mylyn.tasks.core.ITaskAttachment attachment) {
        if (org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.isContext(attachment)) {
            return org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.CONTEXT_TRANSFER);
        } else if (attachment.isPatch()) {
            return org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.TASK_ATTACHMENT_PATCH);
        } else {
            java.lang.String filename = attachment.getFileName();
            if (filename != null) {
                filename = filename.toLowerCase();
                for (java.lang.String extension : org.eclipse.mylyn.internal.tasks.ui.actions.CompareAttachmentsAction.IMAGE_EXTENSIONS) {
                    if (filename.endsWith(extension)) {
                        return org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.IMAGE_FILE);
                    }
                }
            }
            return org.eclipse.ui.internal.WorkbenchImages.getImage(org.eclipse.ui.ISharedImages.IMG_OBJ_FILE);
        }
    }

    public void selectionChanged(org.eclipse.jface.action.IAction action, org.eclipse.jface.viewers.ISelection selection) {
        this.currentSelection = selection;
    }

    private class CompareItem implements org.eclipse.compare.IStreamContentAccessor , org.eclipse.compare.ITypedElement {
        private final org.eclipse.mylyn.tasks.core.ITaskAttachment attachment;

        public CompareItem(org.eclipse.mylyn.tasks.core.ITaskAttachment attachment) {
            this.attachment = attachment;
        }

        public java.io.InputStream getContents() throws org.eclipse.core.runtime.CoreException {
            org.eclipse.mylyn.tasks.core.data.TaskAttribute attachmentAttribute = attachment.getTaskAttribute();
            if (attachmentAttribute == null) {
                throw new org.eclipse.core.runtime.CoreException(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, Messages.CompareAttachmentsAction_Failed_to_find_attachment + attachment.getUrl()));
            }
            org.eclipse.mylyn.tasks.core.TaskRepository taskRepository = attachment.getTaskRepository();
            org.eclipse.mylyn.tasks.core.ITask task = attachment.getTask();
            org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(taskRepository.getConnectorKind());
            org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler handler = connector.getTaskAttachmentHandler();
            return handler.getContent(taskRepository, task, attachmentAttribute, new org.eclipse.core.runtime.NullProgressMonitor());
        }

        public org.eclipse.swt.graphics.Image getImage() {
            return null;
        }

        public java.lang.String getName() {
            return attachment.getFileName();
        }

        public java.lang.String getType() {
            // ImageMergeViewerCreator - gif,jpg,jpeg,png,bmp,ico,tif,tiff
            // BinaryCompareViewerCreator - class,exe,dll,binary,zip,jar
            // TextMergeViewerCreator - txt
            // PropertiesFileMergeViewerCreator - properties,properties2
            // JavaContentViewerCreator - java,java2"
            // RefactoringDescriptorCompareViewerCreator - refactoring_descriptor
            // 
            java.lang.String filename = attachment.getFileName();
            int n = filename.lastIndexOf('.');
            if (n > (-1)) {
                return filename.substring(n + 1);
            }
            return org.eclipse.compare.ITypedElement.TEXT_TYPE;
        }
    }
}