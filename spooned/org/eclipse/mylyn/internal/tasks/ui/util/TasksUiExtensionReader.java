/**
 * *****************************************************************************
 * Copyright (c) 2004, 2014 Tasktop Technologies and others.
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
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.core.util.ContributorBlackList;
import org.eclipse.mylyn.internal.tasks.ui.IDynamicSubMenuContributor;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListPresentation;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylyn.tasks.core.AbstractDuplicateDetector;
import org.eclipse.mylyn.tasks.ui.AbstractTaskRepositoryLinkProvider;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;
/**
 *
 * @author Mik Kersten
 * @author Shawn Minto
 * @author Rob Elves
 * @author Steffen Pingel
 */
public class TasksUiExtensionReader {
    public static final java.lang.String EXTENSION_REPOSITORY_LINKS_PROVIDERS = "org.eclipse.mylyn.tasks.ui.projectLinkProviders";// $NON-NLS-1$


    public static final java.lang.String ELMNT_REPOSITORY_LINK_PROVIDER = "linkProvider";// $NON-NLS-1$


    public static final java.lang.String EXTENSION_TASK_CONTRIBUTOR = "org.eclipse.mylyn.tasks.ui.actions";// $NON-NLS-1$


    public static final java.lang.String DYNAMIC_POPUP_ELEMENT = "dynamicPopupMenu";// $NON-NLS-1$


    public static final java.lang.String ATTR_CLASS = "class";// $NON-NLS-1$


    public static final java.lang.String ATTR_MENU_PATH = "menuPath";// $NON-NLS-1$


    public static final java.lang.String EXTENSION_EDITORS = "org.eclipse.mylyn.tasks.ui.editors";// $NON-NLS-1$


    public static final java.lang.String ELMNT_TASK_EDITOR_PAGE_FACTORY = "pageFactory";// $NON-NLS-1$


    public static final java.lang.String EXTENSION_DUPLICATE_DETECTORS = "org.eclipse.mylyn.tasks.ui.duplicateDetectors";// $NON-NLS-1$


    public static final java.lang.String ELMNT_DUPLICATE_DETECTOR = "detector";// $NON-NLS-1$


    public static final java.lang.String ATTR_NAME = "name";// $NON-NLS-1$


    public static final java.lang.String ATTR_KIND = "kind";// $NON-NLS-1$


    private static final java.lang.String EXTENSION_PRESENTATIONS = "org.eclipse.mylyn.tasks.ui.presentations";// $NON-NLS-1$


    public static final java.lang.String ATTR_ICON = "icon";// $NON-NLS-1$


    public static final java.lang.String ATTR_PRIMARY = "primary";// $NON-NLS-1$


    public static final java.lang.String ATTR_ID = "id";// $NON-NLS-1$


    public static void initStartupExtensions(org.eclipse.mylyn.internal.tasks.core.util.ContributorBlackList blackList) {
        org.eclipse.core.runtime.IExtensionRegistry registry = org.eclipse.core.runtime.Platform.getExtensionRegistry();
        org.eclipse.core.runtime.IExtensionPoint presentationsExtensionPoint = registry.getExtensionPoint(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiExtensionReader.EXTENSION_PRESENTATIONS);
        org.eclipse.core.runtime.IExtension[] presentations = presentationsExtensionPoint.getExtensions();
        for (org.eclipse.core.runtime.IExtension presentation : presentations) {
            org.eclipse.core.runtime.IConfigurationElement[] elements = presentation.getConfigurationElements();
            for (org.eclipse.core.runtime.IConfigurationElement element : elements) {
                if (!blackList.isDisabled(element)) {
                    org.eclipse.mylyn.internal.tasks.ui.util.TasksUiExtensionReader.readPresentation(element);
                }
            }
        }
        // NOTE: causes ..mylyn.context.ui to load
        org.eclipse.core.runtime.IExtensionPoint editorsExtensionPoint = registry.getExtensionPoint(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiExtensionReader.EXTENSION_EDITORS);
        org.eclipse.core.runtime.IExtension[] editors = editorsExtensionPoint.getExtensions();
        for (org.eclipse.core.runtime.IExtension editor : editors) {
            org.eclipse.core.runtime.IConfigurationElement[] elements = editor.getConfigurationElements();
            for (org.eclipse.core.runtime.IConfigurationElement element : elements) {
                if (!blackList.isDisabled(element)) {
                    if (element.getName().equals(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiExtensionReader.ELMNT_TASK_EDITOR_PAGE_FACTORY)) {
                        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiExtensionReader.readTaskEditorPageFactory(element);
                    }
                }
            }
        }
    }

    public static void initWorkbenchUiExtensions(org.eclipse.mylyn.internal.tasks.core.util.ContributorBlackList blackList) {
        org.eclipse.core.runtime.IExtensionRegistry registry = org.eclipse.core.runtime.Platform.getExtensionRegistry();
        org.eclipse.mylyn.internal.tasks.ui.util.RepositoryConnectorUiExtensionReader reader = new org.eclipse.mylyn.internal.tasks.ui.util.RepositoryConnectorUiExtensionReader(registry, blackList);
        reader.registerConnectorUis();
        org.eclipse.core.runtime.IExtensionPoint linkProvidersExtensionPoint = registry.getExtensionPoint(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiExtensionReader.EXTENSION_REPOSITORY_LINKS_PROVIDERS);
        org.eclipse.core.runtime.IExtension[] linkProvidersExtensions = linkProvidersExtensionPoint.getExtensions();
        for (org.eclipse.core.runtime.IExtension linkProvidersExtension : linkProvidersExtensions) {
            org.eclipse.core.runtime.IConfigurationElement[] elements = linkProvidersExtension.getConfigurationElements();
            for (org.eclipse.core.runtime.IConfigurationElement element : elements) {
                if (!blackList.isDisabled(element)) {
                    if (element.getName().equals(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiExtensionReader.ELMNT_REPOSITORY_LINK_PROVIDER)) {
                        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiExtensionReader.readLinkProvider(element);
                    }
                }
            }
        }
        org.eclipse.core.runtime.IExtensionPoint duplicateDetectorsExtensionPoint = registry.getExtensionPoint(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiExtensionReader.EXTENSION_DUPLICATE_DETECTORS);
        org.eclipse.core.runtime.IExtension[] dulicateDetectorsExtensions = duplicateDetectorsExtensionPoint.getExtensions();
        for (org.eclipse.core.runtime.IExtension dulicateDetectorsExtension : dulicateDetectorsExtensions) {
            org.eclipse.core.runtime.IConfigurationElement[] elements = dulicateDetectorsExtension.getConfigurationElements();
            for (org.eclipse.core.runtime.IConfigurationElement element : elements) {
                if (!blackList.isDisabled(element)) {
                    if (element.getName().equals(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiExtensionReader.ELMNT_DUPLICATE_DETECTOR)) {
                        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiExtensionReader.readDuplicateDetector(element);
                    }
                }
            }
        }
        org.eclipse.core.runtime.IExtensionPoint extensionPoint = registry.getExtensionPoint(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiExtensionReader.EXTENSION_TASK_CONTRIBUTOR);
        org.eclipse.core.runtime.IExtension[] extensions = extensionPoint.getExtensions();
        for (org.eclipse.core.runtime.IExtension extension : extensions) {
            org.eclipse.core.runtime.IConfigurationElement[] elements = extension.getConfigurationElements();
            for (org.eclipse.core.runtime.IConfigurationElement element : elements) {
                if (!blackList.isDisabled(element)) {
                    if (element.getName().equals(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiExtensionReader.DYNAMIC_POPUP_ELEMENT)) {
                        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiExtensionReader.readDynamicPopupContributor(element);
                    }
                }
            }
        }
    }

    private static void readPresentation(org.eclipse.core.runtime.IConfigurationElement element) {
        try {
            java.lang.String name = element.getAttribute(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiExtensionReader.ATTR_NAME);
            java.lang.String iconPath = element.getAttribute(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiExtensionReader.ATTR_ICON);
            org.eclipse.jface.resource.ImageDescriptor imageDescriptor = // 
            org.eclipse.ui.plugin.AbstractUIPlugin.imageDescriptorFromPlugin(element.getContributor().getName(), iconPath);
            org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListPresentation presentation = ((org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListPresentation) (element.createExecutableExtension(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiExtensionReader.ATTR_CLASS)));
            presentation.setPluginId(element.getNamespaceIdentifier());
            presentation.setImageDescriptor(imageDescriptor);
            presentation.setName(name);
            java.lang.String primary = element.getAttribute(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiExtensionReader.ATTR_PRIMARY);
            if ((primary != null) && primary.equals("true")) {
                // $NON-NLS-1$
                presentation.setPrimary(true);
            }
            org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.addPresentation(presentation);
        } catch (java.lang.Throwable e) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Could not load presentation extension", e));// $NON-NLS-1$

        }
    }

    private static void readDuplicateDetector(org.eclipse.core.runtime.IConfigurationElement element) {
        try {
            java.lang.Object obj = element.createExecutableExtension(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiExtensionReader.ATTR_CLASS);
            if (obj instanceof org.eclipse.mylyn.tasks.core.AbstractDuplicateDetector) {
                org.eclipse.mylyn.tasks.core.AbstractDuplicateDetector duplicateDetector = ((org.eclipse.mylyn.tasks.core.AbstractDuplicateDetector) (obj));
                duplicateDetector.setName(element.getAttribute(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiExtensionReader.ATTR_NAME));
                duplicateDetector.setConnectorKind(element.getAttribute(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiExtensionReader.ATTR_KIND));
                org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().addDuplicateDetector(duplicateDetector);
            } else {
                org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Could not load duplicate detector " + obj.getClass().getCanonicalName()));// $NON-NLS-1$

            }
        } catch (java.lang.Throwable e) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Could not load duplicate detector", e));// $NON-NLS-1$

        }
    }

    private static void readLinkProvider(org.eclipse.core.runtime.IConfigurationElement element) {
        try {
            java.lang.Object repositoryLinkProvider = element.createExecutableExtension(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiExtensionReader.ATTR_CLASS);
            if (repositoryLinkProvider instanceof org.eclipse.mylyn.tasks.ui.AbstractTaskRepositoryLinkProvider) {
                org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().addRepositoryLinkProvider(((org.eclipse.mylyn.tasks.ui.AbstractTaskRepositoryLinkProvider) (repositoryLinkProvider)));
            } else {
                org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Could not load repository link provider "// $NON-NLS-1$
                 + repositoryLinkProvider.getClass().getCanonicalName()));
            }
        } catch (java.lang.Throwable e) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Could not load repository link provider", e));// $NON-NLS-1$

        }
    }

    private static void readTaskEditorPageFactory(org.eclipse.core.runtime.IConfigurationElement element) {
        java.lang.String id = element.getAttribute(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiExtensionReader.ATTR_ID);
        if (id == null) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Editor page factory must specify id"));// $NON-NLS-1$

            return;
        }
        try {
            java.lang.Object item = element.createExecutableExtension(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiExtensionReader.ATTR_CLASS);
            if (item instanceof org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory) {
                org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory editorPageFactory = ((org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory) (item));
                editorPageFactory.setId(id);
                editorPageFactory.setPluginId(element.getNamespaceIdentifier());
                org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().addTaskEditorPageFactory(editorPageFactory);
            } else {
                org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, (("Could not load editor page factory " + item.getClass().getCanonicalName()) + " must implement ")// $NON-NLS-1$ //$NON-NLS-2$
                 + org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory.class.getCanonicalName()));
            }
        } catch (java.lang.Throwable e) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(// $NON-NLS-1$
            new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Could not load page editor factory", e));
        }
    }

    private static void readDynamicPopupContributor(org.eclipse.core.runtime.IConfigurationElement element) {
        try {
            java.lang.Object dynamicPopupContributor = element.createExecutableExtension(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiExtensionReader.ATTR_CLASS);
            java.lang.String menuPath = element.getAttribute(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiExtensionReader.ATTR_MENU_PATH);
            if (dynamicPopupContributor instanceof org.eclipse.mylyn.internal.tasks.ui.IDynamicSubMenuContributor) {
                org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().addDynamicPopupContributor(menuPath, ((org.eclipse.mylyn.internal.tasks.ui.IDynamicSubMenuContributor) (dynamicPopupContributor)));
            } else {
                org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, (("Could not load dynamic popup menu: " + dynamicPopupContributor.getClass().getCanonicalName())// $NON-NLS-1$
                 + " must implement ") + org.eclipse.mylyn.internal.tasks.ui.IDynamicSubMenuContributor.class.getCanonicalName()));// $NON-NLS-1$

            }
        } catch (java.lang.Throwable e) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Could not load dynamic popup menu extension", e));// $NON-NLS-1$

        }
    }
}