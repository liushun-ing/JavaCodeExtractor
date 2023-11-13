/**
 * *****************************************************************************
 * Copyright (c) 2011 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.compare;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.internal.tasks.core.data.TaskAttributeDiff;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataDiff;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.ITaskComment;
import org.eclipse.mylyn.tasks.core.data.ITaskAttributeDiff;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.swt.graphics.Image;
/**
 * A model element for comparing two {@link TaskData} objects.
 *
 * @author Steffen Pingel
 */
public class TaskDataDiffNode extends org.eclipse.compare.structuremergeviewer.DiffNode {
    static class ByteArrayInput implements org.eclipse.compare.ITypedElement , org.eclipse.compare.IStreamContentAccessor {
        java.lang.String content;

        private final java.lang.String name;

        public ByteArrayInput(java.lang.String content, java.lang.String name) {
            this.content = content;
            this.name = name;
        }

        public java.lang.String getName() {
            return name;
        }

        public org.eclipse.swt.graphics.Image getImage() {
            return null;
        }

        public java.lang.String getType() {
            return org.eclipse.compare.ITypedElement.TEXT_TYPE;
        }

        public java.io.InputStream getContents() throws org.eclipse.core.runtime.CoreException {
            return new java.io.ByteArrayInputStream(content.getBytes());
        }
    }

    public TaskDataDiffNode(int change, org.eclipse.mylyn.tasks.core.data.TaskData oldData, org.eclipse.mylyn.tasks.core.data.TaskData newData) {
        super(change);
        org.eclipse.mylyn.internal.tasks.core.data.TaskDataDiff diff = new org.eclipse.mylyn.internal.tasks.core.data.TaskDataDiff(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryModel(), newData, oldData);
        for (org.eclipse.mylyn.tasks.core.data.ITaskAttributeDiff attribute : diff.getChangedAttributes()) {
            org.eclipse.mylyn.internal.tasks.core.data.TaskAttributeDiff attr = ((org.eclipse.mylyn.internal.tasks.core.data.TaskAttributeDiff) (attribute));
            java.lang.String label = attr.getLabel();
            if (label.endsWith(":")) {
                // $NON-NLS-1$
                label = label.substring(0, label.length() - 1);
            }
            org.eclipse.compare.structuremergeviewer.DiffNode node = new org.eclipse.compare.structuremergeviewer.DiffNode(org.eclipse.compare.structuremergeviewer.Differencer.CHANGE, this, new org.eclipse.mylyn.internal.tasks.ui.compare.TaskDataDiffNode.ByteArrayInput(attr.getOldValues().toString(), null), new org.eclipse.mylyn.internal.tasks.ui.compare.TaskDataDiffNode.ByteArrayInput(attr.getNewValues().toString(), label));
            add(node);
        }
        for (org.eclipse.mylyn.tasks.core.ITaskComment attribute : diff.getNewComments()) {
            org.eclipse.compare.structuremergeviewer.DiffNode node = new org.eclipse.compare.structuremergeviewer.DiffNode(org.eclipse.compare.structuremergeviewer.Differencer.CHANGE, this, new org.eclipse.mylyn.internal.tasks.ui.compare.TaskDataDiffNode.ByteArrayInput("", null), // $NON-NLS-1$
            new org.eclipse.mylyn.internal.tasks.ui.compare.TaskDataDiffNode.ByteArrayInput(attribute.getText(), Messages.TaskDataDiffNode_New_Comment_Label));
            add(node);
        }
    }
}