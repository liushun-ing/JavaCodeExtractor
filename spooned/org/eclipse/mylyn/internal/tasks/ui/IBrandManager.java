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
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.graphics.Image;
public interface IBrandManager {
    public org.eclipse.swt.graphics.Image getBrandingIcon(org.eclipse.mylyn.tasks.core.TaskRepository repository);

    public org.eclipse.jface.resource.ImageDescriptor getOverlayIcon(org.eclipse.mylyn.tasks.core.TaskRepository repository);

    public org.eclipse.jface.resource.ImageDescriptor getOverlayIcon(org.eclipse.mylyn.tasks.core.ITask task);

    /**
     * Returns the branding icon for the given connector kind and brand. Returns the default icon for the repository
     * type if the brand is <code>null</code> or is unknown to the connector.
     */
    public org.eclipse.swt.graphics.Image getBrandingIcon(java.lang.String connectorKind, @org.eclipse.jdt.annotation.Nullable
    java.lang.String brand);

    /**
     * Returns the overlay icon for the given connector kind and brand. Returns the default icon for the repository type
     * if the brand is <code>null</code> or is unknown to the connector.
     */
    public org.eclipse.jface.resource.ImageDescriptor getOverlayIcon(java.lang.String connectorKind, @org.eclipse.jdt.annotation.Nullable
    java.lang.String brand);

    /**
     * Returns the connector label for the given connector and brand. Returns the default label for the connector if the
     * brand is <code>null</code> or is unknown to the connector.
     */
    public java.lang.String getConnectorLabel(org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector, @org.eclipse.jdt.annotation.Nullable
    java.lang.String brand);

    /**
     * Returns the brands known to the given connector.
     */
    public java.util.Collection<java.lang.String> getBrands(java.lang.String connectorKind);

    public org.eclipse.swt.graphics.Image getDefaultBrandingIcon(java.lang.String connectorKind);

    public org.eclipse.jface.resource.ImageDescriptor getDefaultOverlayIcon(java.lang.String connectorKind);
}