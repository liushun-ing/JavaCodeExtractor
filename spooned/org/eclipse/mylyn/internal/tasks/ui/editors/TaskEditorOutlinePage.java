/**
 * *****************************************************************************
 * Copyright (c) 2004, 2012 Tasktop Technologies and others.
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
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
/**
 * An outline page for a {@link TaskEditor}.
 *
 * @author Steffen Pingel
 */
public class TaskEditorOutlinePage extends org.eclipse.ui.views.contentoutline.ContentOutlinePage {
    private org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineModel model;

    private org.eclipse.jface.viewers.TreeViewer viewer;

    public TaskEditorOutlinePage() {
    }

    public void setInput(org.eclipse.mylyn.tasks.core.TaskRepository taskRepository, org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode rootNode) {
        if (rootNode != null) {
            this.model = new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineModel(rootNode);
        } else {
            this.model = null;
        }
        if (((viewer != null) && (viewer.getControl() != null)) && (!viewer.getControl().isDisposed())) {
            viewer.setInput(this.model);
            viewer.expandAll();
            viewer.refresh(true);
        }
    }

    @java.lang.Override
    public void createControl(org.eclipse.swt.widgets.Composite parent) {
        super.createControl(parent);
        viewer = getTreeViewer();
        viewer.setContentProvider(new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineContentProvider());
        viewer.setLabelProvider(new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNodeLabelProvider());
        viewer.setInput(model);
        viewer.expandAll();
    }
}