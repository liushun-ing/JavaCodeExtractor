/**
 * *****************************************************************************
 * Copyright (c) 2013, 2015 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.util;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.core.util.ContributorBlackList;
import org.eclipse.mylyn.internal.tasks.ui.BrandManager;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.spi.RepositoryConnectorBranding;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.ui.plugin.AbstractUIPlugin;
public class RepositoryConnectorUiExtensionReader {
    private static final java.lang.String EXTENSION_REPOSITORIES = "org.eclipse.mylyn.tasks.ui.repositories";// $NON-NLS-1$


    public static final java.lang.String ELMNT_REPOSITORY_UI = "connectorUi";// $NON-NLS-1$


    private static final java.lang.String ATTR_BRANDING_ICON = "brandingIcon";// $NON-NLS-1$


    private static final java.lang.String ATTR_OVERLAY_ICON = "overlayIcon";// $NON-NLS-1$


    private static final java.lang.String ATTR_CLASS = "class";// $NON-NLS-1$


    private final org.eclipse.core.runtime.IExtensionRegistry registry;

    /**
     * Plug-in ids of connector extensions that failed to load.
     */
    private final org.eclipse.mylyn.internal.tasks.core.util.ContributorBlackList blackList;

    public RepositoryConnectorUiExtensionReader(org.eclipse.core.runtime.IExtensionRegistry registry, org.eclipse.mylyn.internal.tasks.core.util.ContributorBlackList blackList) {
        org.eclipse.core.runtime.Assert.isNotNull(registry);
        org.eclipse.core.runtime.Assert.isNotNull(blackList);
        this.registry = registry;
        this.blackList = blackList;
    }

    public void registerConnectorUis() {
        registerFromExtensionPoint();
        registerFromAdaptable();
        registerConnetorToConnectorUi();
    }

    private void registerConnetorToConnectorUi() {
        for (org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector : org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnectors()) {
            org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnectorUi(connector.getConnectorKind());
            if (connectorUi != null) {
                org.eclipse.mylyn.internal.tasks.ui.util.RepositoryConnectorUiExtensionReader.setConnectorForConnectorUi(connector, connectorUi);
            }
        }
    }

    private void registerFromAdaptable() {
        for (org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector : org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnectors()) {
            if (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnectorUi(connector.getConnectorKind()) == null) {
                registerFromAdaptable(connector);
            }
        }
    }

    private void registerFromAdaptable(final org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector) {
        org.eclipse.core.runtime.SafeRunner.run(new org.eclipse.core.runtime.ISafeRunnable() {
            @java.lang.Override
            public void run() throws java.lang.Exception {
                org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi = loadAdapter(connector, org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi.class);
                if (connectorUi != null) {
                    org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().addRepositoryConnectorUi(connectorUi);
                }
                org.eclipse.mylyn.tasks.core.spi.RepositoryConnectorBranding branding = loadAdapter(connector, org.eclipse.mylyn.tasks.core.spi.RepositoryConnectorBranding.class);
                if (branding != null) {
                    addDefaultImageData(connector, branding);
                    addBranding(connector.getConnectorKind(), branding);
                }
            }

            protected void addDefaultImageData(final org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector, org.eclipse.mylyn.tasks.core.spi.RepositoryConnectorBranding branding) throws java.io.IOException {
                java.io.InputStream brandingImageData = branding.getBrandingImageData();
                if (brandingImageData != null) {
                    try {
                        ((org.eclipse.mylyn.internal.tasks.ui.BrandManager) (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getBrandManager())).addDefaultBrandingIcon(connector.getConnectorKind(), getImage(brandingImageData));
                    } finally {
                        closeQuietly(brandingImageData);
                    }
                }
                java.io.InputStream overlayImageData = branding.getOverlayImageData();
                if (overlayImageData != null) {
                    try {
                        ((org.eclipse.mylyn.internal.tasks.ui.BrandManager) (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getBrandManager())).addDefaultOverlayIcon(connector.getConnectorKind(), getImageDescriptor(overlayImageData));
                    } finally {
                        closeQuietly(brandingImageData);
                    }
                }
            }

            @java.lang.Override
            public void handleException(java.lang.Throwable e) {
                org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.osgi.util.NLS.bind("Loading of connector ui for kind ''{0}'' failed.", connector.getConnectorKind()), e));// $NON-NLS-1$

            }
        });
    }

    protected void addBranding(final java.lang.String connectorKind, org.eclipse.mylyn.tasks.core.spi.RepositoryConnectorBranding branding) throws java.io.IOException {
        org.eclipse.mylyn.internal.tasks.ui.BrandManager brandManager = ((org.eclipse.mylyn.internal.tasks.ui.BrandManager) (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getBrandManager()));
        for (java.lang.String brand : branding.getBrands()) {
            if (brand == null) {
                continue;
            }
            try {
                java.lang.String connectorLabel = branding.getConnectorLabel(brand);
                if (connectorLabel != null) {
                    brandManager.addConnectorLabel(connectorKind, brand, connectorLabel);
                }
                java.io.InputStream brandingImageData = branding.getBrandingImageData(brand);
                if (brandingImageData != null) {
                    try {
                        brandManager.addBrandingIcon(connectorKind, brand, getImage(brandingImageData));
                    } finally {
                        closeQuietly(brandingImageData);
                    }
                }
                java.io.InputStream overlayImageData = branding.getOverlayImageData(brand);
                if (overlayImageData != null) {
                    try {
                        brandManager.addOverlayIcon(connectorKind, brand, getImageDescriptor(overlayImageData));
                    } finally {
                        closeQuietly(brandingImageData);
                    }
                }
            } catch (java.lang.Exception e) {
                org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.osgi.util.NLS.bind("Loading of brand ''{0}'' for kind ''{1}'' failed.", brand, connectorKind), e));// $NON-NLS-1$

            }
        }
    }

    private void closeQuietly(java.io.InputStream in) {
        try {
            in.close();
        } catch (java.io.IOException e) {
            // ignore
        }
    }

    @java.lang.SuppressWarnings("unchecked")
    public <T> T loadAdapter(final org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector, java.lang.Class<T> klass) {
        T adapter = null;
        if (connector instanceof org.eclipse.core.runtime.IAdaptable) {
            adapter = ((T) (((org.eclipse.core.runtime.IAdaptable) (connector)).getAdapter(klass)));
        }
        if (adapter == null) {
            adapter = ((T) (org.eclipse.core.runtime.Platform.getAdapterManager().loadAdapter(connector, klass.getName())));
        }
        return adapter;
    }

    private org.eclipse.jface.resource.ImageDescriptor getImageDescriptor(java.io.InputStream in) {
        return org.eclipse.jface.resource.ImageDescriptor.createFromImageData(new org.eclipse.swt.graphics.ImageData(in));
    }

    private org.eclipse.swt.graphics.Image getImage(java.io.InputStream in) {
        return org.eclipse.mylyn.commons.ui.CommonImages.getImage(getImageDescriptor(in));
    }

    private void registerFromExtensionPoint() {
        org.eclipse.core.runtime.IExtensionPoint repositoriesExtensionPoint = registry.getExtensionPoint(org.eclipse.mylyn.internal.tasks.ui.util.RepositoryConnectorUiExtensionReader.EXTENSION_REPOSITORIES);
        org.eclipse.core.runtime.IExtension[] repositoryExtensions = repositoriesExtensionPoint.getExtensions();
        for (org.eclipse.core.runtime.IExtension repositoryExtension : repositoryExtensions) {
            org.eclipse.core.runtime.IConfigurationElement[] elements = repositoryExtension.getConfigurationElements();
            for (org.eclipse.core.runtime.IConfigurationElement element : elements) {
                if (!blackList.isDisabled(element)) {
                    if (element.getName().equals(org.eclipse.mylyn.internal.tasks.ui.util.RepositoryConnectorUiExtensionReader.ELMNT_REPOSITORY_UI)) {
                        registerRepositoryConnectorUi(element);
                    }
                }
            }
        }
    }

    private void registerRepositoryConnectorUi(org.eclipse.core.runtime.IConfigurationElement element) {
        try {
            java.lang.Object connectorUiObject = element.createExecutableExtension(org.eclipse.mylyn.internal.tasks.ui.util.RepositoryConnectorUiExtensionReader.ATTR_CLASS);
            if (connectorUiObject instanceof org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi) {
                org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi = ((org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi) (connectorUiObject));
                org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnector(connectorUi.getConnectorKind());
                if (connector != null) {
                    org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().addRepositoryConnectorUi(connectorUi);
                    java.lang.String iconPath = element.getAttribute(org.eclipse.mylyn.internal.tasks.ui.util.RepositoryConnectorUiExtensionReader.ATTR_BRANDING_ICON);
                    if (iconPath != null) {
                        org.eclipse.jface.resource.ImageDescriptor descriptor = org.eclipse.ui.plugin.AbstractUIPlugin.imageDescriptorFromPlugin(element.getContributor().getName(), iconPath);
                        if (descriptor != null) {
                            ((org.eclipse.mylyn.internal.tasks.ui.BrandManager) (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getBrandManager())).addDefaultBrandingIcon(connectorUi.getConnectorKind(), org.eclipse.mylyn.commons.ui.CommonImages.getImage(descriptor));
                        }
                    }
                    java.lang.String overlayIconPath = element.getAttribute(org.eclipse.mylyn.internal.tasks.ui.util.RepositoryConnectorUiExtensionReader.ATTR_OVERLAY_ICON);
                    if (overlayIconPath != null) {
                        org.eclipse.jface.resource.ImageDescriptor descriptor = org.eclipse.ui.plugin.AbstractUIPlugin.imageDescriptorFromPlugin(element.getContributor().getName(), overlayIconPath);
                        if (descriptor != null) {
                            ((org.eclipse.mylyn.internal.tasks.ui.BrandManager) (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getBrandManager())).addDefaultOverlayIcon(connectorUi.getConnectorKind(), descriptor);
                        }
                    }
                    org.eclipse.mylyn.tasks.core.spi.RepositoryConnectorBranding branding = loadAdapter(connector, org.eclipse.mylyn.tasks.core.spi.RepositoryConnectorBranding.class);
                    if (branding != null) {
                        addBranding(connector.getConnectorKind(), branding);
                    }
                } else {
                    org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, // $NON-NLS-1$
                    org.eclipse.osgi.util.NLS.bind("Ignoring connector ui for kind ''{0}'' without corresponding core contributed by ''{1}''.", connectorUi.getConnectorKind(), element.getContributor().getName())));
                }
            } else {
                org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Could not load connector ui "// $NON-NLS-1$
                 + connectorUiObject.getClass().getCanonicalName()));
            }
        } catch (java.lang.Throwable e) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Could not load connector ui", e));// $NON-NLS-1$

        }
    }

    private static void setConnectorForConnectorUi(org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector, org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi) {
        // need reflection since the field is private
        try {
            java.lang.reflect.Field field = org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi.class.getDeclaredField("connector");// $NON-NLS-1$

            field.setAccessible(true);
            field.set(connectorUi, connector);
        } catch (java.lang.Exception e) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Unable to call setConnector()", e));// $NON-NLS-1$

        }
    }
}