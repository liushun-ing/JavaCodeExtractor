/**
 * *****************************************************************************
 * Copyright (c) 2004, 2015 Willian Mitsuda and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Willian Mitsuda - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.views;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.swt.graphics.Image;
/**
 * Displays task repository info from a {@link AbstractTask}
 *
 * @author Willian Mitsuda
 */
public class TaskDetailLabelProvider extends org.eclipse.jface.viewers.LabelProvider implements org.eclipse.jface.viewers.ILabelProvider {
    @java.lang.Override
    public org.eclipse.swt.graphics.Image getImage(java.lang.Object element) {
        if (!(element instanceof org.eclipse.mylyn.tasks.core.ITask)) {
            return super.getImage(element);
        }
        org.eclipse.jface.resource.ImageDescriptor overlay = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getBrandManager().getOverlayIcon(((org.eclipse.mylyn.tasks.core.ITask) (element)));
        if (overlay != null) {
            return org.eclipse.mylyn.commons.ui.CommonImages.getImageWithOverlay(org.eclipse.mylyn.tasks.ui.TasksUiImages.REPOSITORY, overlay, false, false);
        } else {
            return org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.REPOSITORY);
        }
    }

    @java.lang.Override
    public java.lang.String getText(java.lang.Object element) {
        if (!(element instanceof org.eclipse.mylyn.tasks.core.ITask)) {
            return super.getText(element);
        }
        org.eclipse.mylyn.tasks.core.ITask task = ((org.eclipse.mylyn.tasks.core.ITask) (element));
        org.eclipse.mylyn.tasks.core.TaskRepository repository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(task.getConnectorKind(), task.getRepositoryUrl());
        return repository.getRepositoryLabel();
    }
}