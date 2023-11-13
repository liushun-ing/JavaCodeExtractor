/**
 * *****************************************************************************
 * Copyright (c) 2004, 2015 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.ITaskRepositoryElement;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
/**
 *
 * @author Mik Kersten
 */
public class TaskLabelDecorator implements org.eclipse.jface.viewers.ILightweightLabelDecorator {
    public void decorate(java.lang.Object element, org.eclipse.jface.viewers.IDecoration decoration) {
        org.eclipse.jface.resource.ImageDescriptor priorityOverlay = getPriorityImageDescriptor(element);
        if (priorityOverlay != null) {
            decoration.addOverlay(priorityOverlay, org.eclipse.jface.viewers.IDecoration.BOTTOM_LEFT);
        }
        if (element instanceof org.eclipse.mylyn.tasks.core.ITask) {
            org.eclipse.mylyn.tasks.core.ITask task = ((org.eclipse.mylyn.tasks.core.ITask) (element));
            if ((!task.isCompleted()) && (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().isDueToday(task) || org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().isOverdue(task))) {
                decoration.addOverlay(org.eclipse.mylyn.commons.ui.CommonImages.OVERLAY_DATE_OVERDUE, org.eclipse.jface.viewers.IDecoration.TOP_RIGHT);
            } else if ((!task.isCompleted()) && (task.getDueDate() != null)) {
                decoration.addOverlay(org.eclipse.mylyn.commons.ui.CommonImages.OVERLAY_DATE_DUE, org.eclipse.jface.viewers.IDecoration.TOP_RIGHT);
            }
            if (hasNotes(task)) {
                decoration.addOverlay(org.eclipse.mylyn.tasks.ui.TasksUiImages.NOTES, org.eclipse.jface.viewers.IDecoration.BOTTOM_RIGHT);
            }
        } else if (element instanceof org.eclipse.mylyn.internal.tasks.core.ITaskRepositoryElement) {
            org.eclipse.mylyn.internal.tasks.core.ITaskRepositoryElement repositoryElement = ((org.eclipse.mylyn.internal.tasks.core.ITaskRepositoryElement) (element));
            java.lang.String repositoryUrl = repositoryElement.getRepositoryUrl();
            org.eclipse.mylyn.tasks.core.TaskRepository taskRepository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(repositoryElement.getConnectorKind(), repositoryUrl);
            if (taskRepository != null) {
                decoration.addSuffix(("   [" + taskRepository.getRepositoryLabel()) + "]");// $NON-NLS-1$ //$NON-NLS-2$

            }
        } else if (element instanceof org.eclipse.mylyn.tasks.core.TaskRepository) {
            org.eclipse.jface.resource.ImageDescriptor overlay = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getBrandManager().getOverlayIcon(((org.eclipse.mylyn.tasks.core.TaskRepository) (element)));
            if (overlay != null) {
                decoration.addOverlay(overlay, org.eclipse.jface.viewers.IDecoration.BOTTOM_RIGHT);
            }
        }
    }

    public void addListener(org.eclipse.jface.viewers.ILabelProviderListener listener) {
        // ignore
    }

    public void dispose() {
        // ignore
    }

    public boolean isLabelProperty(java.lang.Object element, java.lang.String property) {
        return false;
    }

    public void removeListener(org.eclipse.jface.viewers.ILabelProviderListener listener) {
        // ignore
    }

    private org.eclipse.jface.resource.ImageDescriptor getPriorityImageDescriptor(java.lang.Object element) {
        org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi;
        if (element instanceof org.eclipse.mylyn.tasks.core.ITask) {
            org.eclipse.mylyn.tasks.core.ITask repositoryTask = ((org.eclipse.mylyn.tasks.core.ITask) (element));
            connectorUi = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnectorUi(((org.eclipse.mylyn.tasks.core.ITask) (element)).getConnectorKind());
            if (connectorUi != null) {
                return connectorUi.getTaskPriorityOverlay(repositoryTask);
            }
        }
        if (element instanceof org.eclipse.mylyn.tasks.core.ITask) {
            return org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getPriorityImage(((org.eclipse.mylyn.tasks.core.ITask) (element)));
        }
        return null;
    }

    private boolean hasNotes(org.eclipse.mylyn.tasks.core.ITask task) {
        if (task instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask) {
            return !((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (task)).getNotes().isEmpty();
        }
        return false;
    }
}