/**
 * *****************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
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
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor;
/**
 *
 * @author Shawn Minto
 */
public class TaskEditorContributionExtensionReader {
    private static final java.lang.String ATTR_ID = "id";// $NON-NLS-1$


    public static final java.lang.String EXTENSION_TASK_EDITOR_PAGE_CONTRIBUTION = "org.eclipse.mylyn.tasks.ui.taskEditorPageContribution";// $NON-NLS-1$


    private static final java.lang.String REPOSITORY_TASK_EDITOR_CONTRIBUTION = "repositoryPart";// $NON-NLS-1$


    private static final java.lang.String LOCAL_TASK_EDITOR_CONTRIBUTION = "localPart";// $NON-NLS-1$


    private static java.util.Collection<org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor> repositoryEditorContributions;

    private static java.util.Collection<org.eclipse.mylyn.internal.tasks.ui.editors.LocalTaskEditorContributionDescriptor> localEditorContributions;

    public static java.util.Collection<org.eclipse.mylyn.internal.tasks.ui.editors.LocalTaskEditorContributionDescriptor> getLocalEditorContributions() {
        if (org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorContributionExtensionReader.localEditorContributions == null) {
            org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorContributionExtensionReader.initExtensions();
        }
        return java.util.Collections.unmodifiableCollection(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorContributionExtensionReader.localEditorContributions);
    }

    public static java.util.Collection<org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor> getRepositoryEditorContributions() {
        if (org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorContributionExtensionReader.repositoryEditorContributions == null) {
            org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorContributionExtensionReader.initExtensions();
        }
        return java.util.Collections.unmodifiableCollection(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorContributionExtensionReader.repositoryEditorContributions);
    }

    private static void initExtensions() {
        java.util.Collection<org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor> repositoryContributions = new java.util.ArrayList<org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor>();
        java.util.Collection<org.eclipse.mylyn.internal.tasks.ui.editors.LocalTaskEditorContributionDescriptor> localContributions = new java.util.ArrayList<org.eclipse.mylyn.internal.tasks.ui.editors.LocalTaskEditorContributionDescriptor>();
        org.eclipse.core.runtime.IExtensionRegistry registry = org.eclipse.core.runtime.Platform.getExtensionRegistry();
        org.eclipse.core.runtime.IExtensionPoint editorExtensionPoint = registry.getExtensionPoint(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorContributionExtensionReader.EXTENSION_TASK_EDITOR_PAGE_CONTRIBUTION);
        org.eclipse.core.runtime.IExtension[] editorExtensions = editorExtensionPoint.getExtensions();
        for (org.eclipse.core.runtime.IExtension extension : editorExtensions) {
            org.eclipse.core.runtime.IConfigurationElement[] elements = extension.getConfigurationElements();
            for (org.eclipse.core.runtime.IConfigurationElement element : elements) {
                if (element.getName().equals(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorContributionExtensionReader.REPOSITORY_TASK_EDITOR_CONTRIBUTION)) {
                    org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorContributionExtensionReader.readRepositoryEditorContributionExtension(element, repositoryContributions);
                } else if (element.getName().equals(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorContributionExtensionReader.LOCAL_TASK_EDITOR_CONTRIBUTION)) {
                    org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorContributionExtensionReader.readLocalEditorContributionExtension(element, localContributions);
                }
            }
        }
        org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorContributionExtensionReader.repositoryEditorContributions = repositoryContributions;
        org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorContributionExtensionReader.localEditorContributions = localContributions;
    }

    private static void readRepositoryEditorContributionExtension(org.eclipse.core.runtime.IConfigurationElement element, java.util.Collection<org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor> contributions) {
        try {
            java.lang.String id = element.getAttribute(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorContributionExtensionReader.ATTR_ID);
            org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensionPartDescriptor descriptor = new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensionPartDescriptor(id, element);
            contributions.add(descriptor);
        } catch (java.lang.Exception e) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Unable to read repository editor contribution", e));// $NON-NLS-1$

        }
    }

    private static void readLocalEditorContributionExtension(org.eclipse.core.runtime.IConfigurationElement element, java.util.Collection<org.eclipse.mylyn.internal.tasks.ui.editors.LocalTaskEditorContributionDescriptor> localContributions) {
        try {
            localContributions.add(new org.eclipse.mylyn.internal.tasks.ui.editors.LocalTaskEditorContributionDescriptor(element));
        } catch (java.lang.Exception e) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Unable to read local editor contribution", e));// $NON-NLS-1$

        }
    }
}