/**
 * *****************************************************************************
 * Copyright (c) 2010 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.editors.outline;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNodeLabelProvider;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMetaData;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.swt.graphics.Image;
/**
 *
 * @author Frank Becker
 */
public class QuickOutlineLabelProvider extends org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNodeLabelProvider {
    @java.lang.Override
    public java.lang.String getText(java.lang.Object element) {
        java.lang.String result = "";// $NON-NLS-1$

        if (element instanceof org.eclipse.mylyn.tasks.core.data.TaskData) {
            org.eclipse.mylyn.tasks.core.data.TaskData node = ((org.eclipse.mylyn.tasks.core.data.TaskData) (element));
            result = node.getTaskId();
        } else if (element instanceof org.eclipse.mylyn.tasks.core.data.TaskAttribute) {
            org.eclipse.mylyn.tasks.core.data.TaskAttribute node = ((org.eclipse.mylyn.tasks.core.data.TaskAttribute) (element));
            org.eclipse.mylyn.tasks.core.data.TaskAttributeMetaData meta = node.getMetaData();
            if (meta != null) {
                java.lang.String lable = meta.getLabel();
                if (lable != null) {
                    result = ((lable + " (") + node.getId()) + ")";// $NON-NLS-1$ //$NON-NLS-2$

                } else if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_ATTACHMENT.equals(meta.getType())) {
                    result = "Attachment: " + node.getValue();// $NON-NLS-1$

                } else if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_COMMENT.equals(meta.getType())) {
                    result = "Comment: " + node.getValue();// $NON-NLS-1$

                } else {
                    result = ("<" + node.getId()) + ">";// $NON-NLS-1$//$NON-NLS-2$

                }
            }
        } else if (element instanceof org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode) {
            result = super.getText(element);
        } else if (element instanceof java.lang.String) {
            result = ((java.lang.String) (element));
        }
        return result;
    }

    @java.lang.Override
    public org.eclipse.swt.graphics.Image getImage(java.lang.Object element) {
        if (element instanceof org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode) {
            return super.getImage(element);
        } else {
            return null;
        }
    }
}