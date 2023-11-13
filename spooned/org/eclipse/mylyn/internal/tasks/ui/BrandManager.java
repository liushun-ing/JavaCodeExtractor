/**
 * *****************************************************************************
 * Copyright (c) 2015 Tasktop Technologies and others.
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
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryManager;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.swt.graphics.Image;
public class BrandManager implements org.eclipse.mylyn.internal.tasks.ui.IBrandManager {
    private final com.google.common.collect.Multimap<java.lang.String, java.lang.String> brands = com.google.common.collect.LinkedHashMultimap.create();

    private final com.google.common.collect.Table<java.lang.String, java.lang.String, org.eclipse.swt.graphics.Image> brandingIcons = com.google.common.collect.HashBasedTable.create();

    private final com.google.common.collect.Table<java.lang.String, java.lang.String, org.eclipse.jface.resource.ImageDescriptor> overlayIcons = com.google.common.collect.HashBasedTable.create();

    private final com.google.common.collect.Table<java.lang.String, java.lang.String, java.lang.String> connectorLabels = com.google.common.collect.HashBasedTable.create();

    private final java.util.Map<java.lang.String, org.eclipse.swt.graphics.Image> defaultBrandingIcons = new java.util.HashMap<>();

    private final java.util.Map<java.lang.String, org.eclipse.jface.resource.ImageDescriptor> defaultOverlayIcons = new java.util.HashMap<>();

    public void addBrandingIcon(java.lang.String repositoryType, java.lang.String brand, org.eclipse.swt.graphics.Image icon) {
        brands.put(repositoryType, brand);
        brandingIcons.put(repositoryType, brand, icon);
    }

    public void addOverlayIcon(java.lang.String repositoryType, java.lang.String brand, org.eclipse.jface.resource.ImageDescriptor icon) {
        brands.put(repositoryType, brand);
        overlayIcons.put(repositoryType, brand, icon);
    }

    public void addConnectorLabel(java.lang.String repositoryType, java.lang.String brand, java.lang.String label) {
        brands.put(repositoryType, brand);
        connectorLabels.put(repositoryType, brand, label);
    }

    public void addDefaultBrandingIcon(java.lang.String repositoryType, org.eclipse.swt.graphics.Image icon) {
        defaultBrandingIcons.put(repositoryType, icon);
    }

    public void addDefaultOverlayIcon(java.lang.String repositoryType, org.eclipse.jface.resource.ImageDescriptor icon) {
        defaultOverlayIcons.put(repositoryType, icon);
    }

    @java.lang.Override
    public org.eclipse.swt.graphics.Image getBrandingIcon(org.eclipse.mylyn.tasks.core.TaskRepository repository) {
        return getBrandingIcon(repository.getConnectorKind(), repository.getProperty(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.PROPERTY_BRAND_ID));
    }

    @java.lang.Override
    public org.eclipse.jface.resource.ImageDescriptor getOverlayIcon(org.eclipse.mylyn.tasks.core.TaskRepository repository) {
        return getOverlayIcon(repository.getConnectorKind(), repository.getProperty(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.PROPERTY_BRAND_ID));
    }

    @java.lang.Override
    public org.eclipse.jface.resource.ImageDescriptor getOverlayIcon(org.eclipse.mylyn.tasks.core.ITask task) {
        org.eclipse.mylyn.tasks.core.TaskRepository repository = getRepositoryManager().getRepository(task.getConnectorKind(), task.getRepositoryUrl());
        if (repository != null) {
            return getOverlayIcon(repository);
        }
        return getDefaultOverlayIcon(task.getConnectorKind());
    }

    @java.lang.Override
    public org.eclipse.swt.graphics.Image getBrandingIcon(java.lang.String repositoryType, @org.eclipse.jdt.annotation.Nullable
    java.lang.String brand) {
        org.eclipse.swt.graphics.Image icon = brandingIcons.get(repositoryType, brand);
        if (icon != null) {
            return icon;
        }
        return getDefaultBrandingIcon(repositoryType);
    }

    @java.lang.Override
    public org.eclipse.jface.resource.ImageDescriptor getOverlayIcon(java.lang.String repositoryType, @org.eclipse.jdt.annotation.Nullable
    java.lang.String brand) {
        org.eclipse.jface.resource.ImageDescriptor icon = overlayIcons.get(repositoryType, brand);
        if (icon != null) {
            return icon;
        }
        return getDefaultOverlayIcon(repositoryType);
    }

    @java.lang.Override
    public java.lang.String getConnectorLabel(org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector, @org.eclipse.jdt.annotation.Nullable
    java.lang.String brand) {
        java.lang.String brandLabel = connectorLabels.get(connector.getConnectorKind(), brand);
        if (brandLabel != null) {
            return brandLabel;
        }
        return connector.getLabel();
    }

    @java.lang.Override
    public java.util.Collection<java.lang.String> getBrands(java.lang.String connectorKind) {
        return brands.get(connectorKind);
    }

    @java.lang.Override
    public org.eclipse.swt.graphics.Image getDefaultBrandingIcon(java.lang.String repositoryType) {
        return defaultBrandingIcons.get(repositoryType);
    }

    @java.lang.Override
    public org.eclipse.jface.resource.ImageDescriptor getDefaultOverlayIcon(java.lang.String repositoryType) {
        return defaultOverlayIcons.get(repositoryType);
    }

    protected org.eclipse.mylyn.tasks.core.IRepositoryManager getRepositoryManager() {
        return org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager();
    }
}