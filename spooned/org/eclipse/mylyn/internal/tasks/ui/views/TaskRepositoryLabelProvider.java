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
package org.eclipse.mylyn.internal.tasks.ui.views;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.core.Category;
import org.eclipse.mylyn.internal.tasks.ui.ConnectorBrand;
import org.eclipse.mylyn.internal.tasks.ui.IBrandManager;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.actions.DisconnectRepositoryAction;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.swt.graphics.Image;
/**
 *
 * @author Mik Kersten
 */
public class TaskRepositoryLabelProvider implements org.eclipse.jface.viewers.ILabelProvider {
    // extends LabelProvider implements ITableLabelProvider {
    public org.eclipse.swt.graphics.Image getColumnImage(java.lang.Object obj, int index) {
        if (index == 0) {
            return getImage(obj);
        } else {
            return null;
        }
    }

    public org.eclipse.swt.graphics.Image getImage(java.lang.Object object) {
        if (object instanceof org.eclipse.mylyn.internal.tasks.core.Category) {
            return org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.CATEGORY);
        } else if (object instanceof org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector) {
            org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector repositoryConnector = ((org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector) (object));
            org.eclipse.swt.graphics.Image image = getBrandManager().getDefaultBrandingIcon(repositoryConnector.getConnectorKind());
            if (image != null) {
                return image;
            } else {
                return org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.REPOSITORY);
            }
        } else if (object instanceof org.eclipse.mylyn.internal.tasks.ui.ConnectorBrand) {
            org.eclipse.mylyn.internal.tasks.ui.ConnectorBrand connectorBrand = ((org.eclipse.mylyn.internal.tasks.ui.ConnectorBrand) (object));
            org.eclipse.swt.graphics.Image image = getBrandManager().getBrandingIcon(connectorBrand.getConnector().getConnectorKind(), connectorBrand.getBrandId());
            if (image != null) {
                return image;
            } else {
                return org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.REPOSITORY);
            }
        } else if (object instanceof org.eclipse.mylyn.tasks.core.TaskRepository) {
            if (((org.eclipse.mylyn.tasks.core.TaskRepository) (object)).isOffline()) {
                return org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.REPOSITORY_OFFLINE);
            } else {
                return org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.REPOSITORY);
            }
        }
        return null;
    }

    public java.lang.String getText(java.lang.Object object) {
        if (object instanceof org.eclipse.mylyn.tasks.core.TaskRepository) {
            org.eclipse.mylyn.tasks.core.TaskRepository repository = ((org.eclipse.mylyn.tasks.core.TaskRepository) (object));
            java.lang.StringBuilder label = new java.lang.StringBuilder();
            label.append(repository.getRepositoryLabel());
            if (repository.isOffline()) {
                label.append((" [" + org.eclipse.mylyn.internal.tasks.ui.actions.DisconnectRepositoryAction.LABEL) + "]");// $NON-NLS-1$ //$NON-NLS-2$

            }
            return label.toString();
        } else if (object instanceof org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector) {
            return ((org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector) (object)).getLabel();
        } else if (object instanceof org.eclipse.mylyn.internal.tasks.ui.ConnectorBrand) {
            org.eclipse.mylyn.internal.tasks.ui.ConnectorBrand connectorBrand = ((org.eclipse.mylyn.internal.tasks.ui.ConnectorBrand) (object));
            return getBrandManager().getConnectorLabel(connectorBrand.getConnector(), connectorBrand.getBrandId());
        } else if (object instanceof org.eclipse.mylyn.internal.tasks.core.Category) {
            return ((org.eclipse.mylyn.internal.tasks.core.Category) (object)).getLabel();
        } else {
            return null;
        }
    }

    protected org.eclipse.mylyn.internal.tasks.ui.IBrandManager getBrandManager() {
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getBrandManager();
    }

    public void addListener(org.eclipse.jface.viewers.ILabelProviderListener listener) {
        // ignore
    }

    public void dispose() {
        // ignore
    }

    public boolean isLabelProperty(java.lang.Object element, java.lang.String property) {
        // ignore
        return false;
    }

    public void removeListener(org.eclipse.jface.viewers.ILabelProviderListener listener) {
        // ignore
    }
}