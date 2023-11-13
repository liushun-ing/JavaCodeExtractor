/**
 * *****************************************************************************
 * Copyright (c) 2004, 2009 David Green and others.
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
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorExtension;
/**
 *
 * @author David Green
 */
public class TaskEditorExtensionReader {
    private static final java.lang.String CONNECTOR_KIND = "connectorKind";// $NON-NLS-1$


    public static final java.lang.String ATTR_ID = "id";// $NON-NLS-1$


    public static final java.lang.String ATTR_NAME = "name";// $NON-NLS-1$


    public static final java.lang.String EXTENSION_TASK_EDITOR_EXTENSIONS = "org.eclipse.mylyn.tasks.ui.taskEditorExtensions";// $NON-NLS-1$


    private static final java.lang.String REPOSITORY_ASSOCIATION = "repositoryAssociation";// $NON-NLS-1$


    private static final java.lang.String TASK_EDITOR_EXTENSION = "taskEditorExtension";// $NON-NLS-1$


    public static void initExtensions() {
        org.eclipse.core.runtime.IExtensionRegistry registry = org.eclipse.core.runtime.Platform.getExtensionRegistry();
        org.eclipse.core.runtime.IExtensionPoint editorExtensionPoint = registry.getExtensionPoint(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensionReader.EXTENSION_TASK_EDITOR_EXTENSIONS);
        org.eclipse.core.runtime.IExtension[] editorExtensions = editorExtensionPoint.getExtensions();
        for (org.eclipse.core.runtime.IExtension extension : editorExtensions) {
            org.eclipse.core.runtime.IConfigurationElement[] elements = extension.getConfigurationElements();
            for (org.eclipse.core.runtime.IConfigurationElement element : elements) {
                if (element.getName().equals(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensionReader.TASK_EDITOR_EXTENSION)) {
                    org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensionReader.readEditorExtension(element);
                } else if (element.getName().equals(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensionReader.REPOSITORY_ASSOCIATION)) {
                    org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensionReader.readEditorExtensionAssociation(element);
                }
            }
        }
    }

    private static void readEditorExtension(org.eclipse.core.runtime.IConfigurationElement element) {
        try {
            java.lang.String id = element.getAttribute(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensionReader.ATTR_ID);
            java.lang.String name = element.getAttribute(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensionReader.ATTR_NAME);
            org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorExtension extension = ((org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorExtension) (element.createExecutableExtension("class")));// $NON-NLS-1$

            org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.addTaskEditorExtension(element.getNamespaceIdentifier(), id, name, extension);
        } catch (java.lang.Throwable e) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(// $NON-NLS-1$
            new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Could not load taskEditorExtension", e));
        }
    }

    private static void readEditorExtensionAssociation(org.eclipse.core.runtime.IConfigurationElement element) {
        try {
            java.lang.String repository = element.getAttribute(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensionReader.CONNECTOR_KIND);
            java.lang.String taskEditorExtension = element.getAttribute(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensionReader.TASK_EDITOR_EXTENSION);
            org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.addRepositoryAssociation(repository, taskEditorExtension);
        } catch (java.lang.Exception e) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Could not load repositoryAssociation", e));// $NON-NLS-1$

        }
    }
}