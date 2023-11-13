/**
 * *****************************************************************************
 * Copyright (c) 2004, 2016 David Green and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.editors;
import com.google.common.collect.Multimap;
import com.google.common.net.MediaType;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorExtension;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.ui.IPluginContribution;
/**
 *
 * @author David Green
 */
public class TaskEditorExtensions {
    public static final java.lang.String REPOSITORY_PROPERTY_EDITOR_EXTENSION = "editorExtension";// $NON-NLS-1$


    public static final java.lang.String REPOSITORY_PROPERTY_AVATAR_SUPPORT = "avatarSupport";// $NON-NLS-1$


    private static final java.lang.String MARKUP_KEY = "markup";// $NON-NLS-1$


    private static final java.lang.String BASE_MARKUP_KEY = "base-markup";// $NON-NLS-1$


    private static java.util.Map<java.lang.String, org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.RegisteredTaskEditorExtension> extensionsById = new java.util.HashMap<java.lang.String, org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.RegisteredTaskEditorExtension>();

    private static java.util.Map<java.lang.String, java.lang.String> associationByConnectorKind = new java.util.HashMap<java.lang.String, java.lang.String>();

    private static boolean initialized;

    public static java.util.SortedSet<org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.RegisteredTaskEditorExtension> getTaskEditorExtensions() {
        org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.init();
        return new java.util.TreeSet<org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.RegisteredTaskEditorExtension>(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.extensionsById.values());
    }

    /**
     * Contributes an extension to the {@link TaskEditor}.
     *
     * @param pluginId
     * 		the id of the contributing plug-in, may be <code>null</code>
     * @param id
     * 		the id of the extension, may not be <code>null</code>
     * @param name
     * 		the name of the extension that is displayed in the settings page
     * @param extension
     * 		the extension implementation
     */
    public static void addTaskEditorExtension(java.lang.String pluginId, java.lang.String id, java.lang.String name, org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorExtension extension) {
        org.eclipse.core.runtime.Assert.isNotNull(id);
        org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.RegisteredTaskEditorExtension descriptor = new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.RegisteredTaskEditorExtension(extension, id, name);
        descriptor.setPluginId(pluginId);
        org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.RegisteredTaskEditorExtension previous = org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.extensionsById.put(id, descriptor);
        if (previous != null) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Duplicate taskEditorExtension id="// $NON-NLS-1$
             + id, null));
        }
    }

    public static void addRepositoryAssociation(java.lang.String connectorKind, java.lang.String extensionId) {
        if ((connectorKind == null) || (extensionId == null)) {
            throw new java.lang.IllegalArgumentException();
        }
        java.lang.String previous = org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.associationByConnectorKind.put(connectorKind, extensionId);
        if (previous != null) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(// $NON-NLS-1$
            new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, java.lang.String.format("Duplicate association for repository %s: %s replaces %s", connectorKind, extensionId, previous), null));
        }
    }

    /**
     * get a task editor extension for a specific repository
     *
     * @param taskRepository
     * @return the extension, or null if there is none
     * @see #getDefaultTaskEditorExtension(TaskRepository)
     */
    public static org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorExtension getTaskEditorExtension(org.eclipse.mylyn.tasks.core.TaskRepository taskRepository) {
        org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.init();
        java.lang.String extensionId = org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.getTaskEditorExtensionId(taskRepository);
        if (extensionId != null) {
            org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.RegisteredTaskEditorExtension taskEditorExtension = org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.extensionsById.get(extensionId);
            return taskEditorExtension == null ? null : taskEditorExtension.getExtension();
        }
        return null;
    }

    /**
     * get a task editor extension for a specific task attribute
     *
     * @param taskRepository
     * @param taskAttribute
     * @return the extension, or null if there is none
     * @see #getTaskEditorExtension(TaskRepository);
     * @since 3.11
     */
    public static org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorExtension getTaskEditorExtension(org.eclipse.mylyn.tasks.core.TaskRepository taskRepository, org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute) {
        org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.init();
        java.lang.String input = taskAttribute.getMetaData().getMediaType();
        if (input != null) {
            try {
                com.google.common.net.MediaType media = com.google.common.net.MediaType.parse(input);
                com.google.common.collect.Multimap<java.lang.String, java.lang.String> parameters = media.parameters();
                if (parameters.containsKey(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.MARKUP_KEY)) {
                    java.util.Iterator<java.lang.String> iter = parameters.get(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.MARKUP_KEY).iterator();
                    java.lang.String markup = iter.next();
                    java.util.Iterator<java.lang.String> baseMarkupIterator = parameters.get(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.BASE_MARKUP_KEY).iterator();
                    java.lang.String baseMarkup = (baseMarkupIterator.hasNext()) ? baseMarkupIterator.next() : "";// $NON-NLS-1$

                    java.util.SortedSet<org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.RegisteredTaskEditorExtension> extensions = org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.getTaskEditorExtensions();
                    for (org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.RegisteredTaskEditorExtension extension : extensions) {
                        if (markup.equals(extension.getName()) || baseMarkup.equals(extension.getName())) {
                            return extension.getExtension();
                        }
                    }
                }
            } catch (java.lang.IllegalArgumentException e) {
                org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, java.lang.String.format("Unable to parse markup type for attribute %s", taskAttribute.toString()), e));// $NON-NLS-1$

            }
        }
        return org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.getTaskEditorExtension(taskRepository);
    }

    public static java.lang.String getTaskEditorExtensionId(org.eclipse.mylyn.tasks.core.TaskRepository taskRepository) {
        org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.init();
        java.lang.String id = taskRepository.getProperty(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.REPOSITORY_PROPERTY_EDITOR_EXTENSION);
        if (id == null) {
            id = org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.getDefaultTaskEditorExtensionId(taskRepository);
        }
        return id;
    }

    public static void setTaskEditorExtensionId(org.eclipse.mylyn.tasks.core.TaskRepository repository, java.lang.String editorExtensionId) {
        repository.setProperty(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.REPOSITORY_PROPERTY_EDITOR_EXTENSION, editorExtensionId);
    }

    /**
     * Get the default task editor extension id for the given task repository
     *
     * @param taskRepository
     * @return the default task editor extension id or null if there is no default
     */
    public static java.lang.String getDefaultTaskEditorExtensionId(org.eclipse.mylyn.tasks.core.TaskRepository taskRepository) {
        return org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.getDefaultTaskEditorExtensionId(taskRepository.getConnectorKind());
    }

    /**
     * Get the default task editor extension id for the given kind of connector
     *
     * @param connectorKind
     * 		the kind of connector
     * @return the default task editor extension id or null if there is no default
     */
    public static java.lang.String getDefaultTaskEditorExtensionId(java.lang.String connectorKind) {
        org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.init();
        return org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.associationByConnectorKind.get(connectorKind);
    }

    /**
     * get a default task editor extension for a specific repository
     *
     * @param taskRepository
     * @return the extension, or null if there is none
     * @see #getTaskEditorExtension(TaskRepository)
     */
    public static org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorExtension getDefaultTaskEditorExtension(org.eclipse.mylyn.tasks.core.TaskRepository taskRepository) {
        org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.init();
        java.lang.String extensionId = org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.getDefaultTaskEditorExtensionId(taskRepository);
        if (extensionId != null) {
            org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.RegisteredTaskEditorExtension taskEditorExtension = org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.extensionsById.get(extensionId);
            return taskEditorExtension == null ? null : taskEditorExtension.getExtension();
        }
        return null;
    }

    private static void init() {
        if (!org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.initialized) {
            org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.initialized = true;
            org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensionReader.initExtensions();
        }
    }

    public static class RegisteredTaskEditorExtension implements java.lang.Comparable<org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.RegisteredTaskEditorExtension> , org.eclipse.ui.IPluginContribution {
        private final java.lang.String id;

        private final java.lang.String name;

        private final org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorExtension extension;

        private java.lang.String pluginId;

        private RegisteredTaskEditorExtension(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorExtension extension, java.lang.String id, java.lang.String name) {
            this.extension = extension;
            this.id = id;
            this.name = name;
        }

        public java.lang.String getId() {
            return id;
        }

        public java.lang.String getName() {
            return name;
        }

        public org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorExtension getExtension() {
            return extension;
        }

        public int compareTo(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.RegisteredTaskEditorExtension o) {
            if (o == this) {
                return 0;
            }
            int i = name.compareTo(o.name);
            if (i == 0) {
                i = id.compareTo(o.id);
            }
            return i;
        }

        public java.lang.String getLocalId() {
            return id;
        }

        public java.lang.String getPluginId() {
            return pluginId;
        }

        public void setPluginId(java.lang.String pluginId) {
            this.pluginId = pluginId;
        }
    }
}