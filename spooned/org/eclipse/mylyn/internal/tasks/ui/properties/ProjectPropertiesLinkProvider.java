/**
 * *****************************************************************************
 * Copyright (c) 2004, 2008 Eugene Kuleshov and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugene Kuleshov - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.properties;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.IRepositoryManager;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractTaskRepositoryLinkProvider;
import org.osgi.service.prefs.BackingStoreException;
/**
 * Default Task Repository link provider
 *
 * @author Eugene Kuleshov
 */
public class ProjectPropertiesLinkProvider extends org.eclipse.mylyn.tasks.ui.AbstractTaskRepositoryLinkProvider {
    private static final java.lang.String PROPERTY_PREFIX = "project.repository";// $NON-NLS-1$


    private static final java.lang.String PROJECT_REPOSITORY_KIND = org.eclipse.mylyn.internal.tasks.ui.properties.ProjectPropertiesLinkProvider.PROPERTY_PREFIX + ".kind";// $NON-NLS-1$


    private static final java.lang.String PROJECT_REPOSITORY_URL = org.eclipse.mylyn.internal.tasks.ui.properties.ProjectPropertiesLinkProvider.PROPERTY_PREFIX + ".url";// $NON-NLS-1$


    @java.lang.Override
    public org.eclipse.mylyn.tasks.core.TaskRepository getTaskRepository(org.eclipse.core.resources.IResource resource, org.eclipse.mylyn.tasks.core.IRepositoryManager repositoryManager) {
        org.eclipse.core.resources.IProject project = resource.getProject();
        if ((project == null) || (!project.isAccessible())) {
            return null;
        }
        org.eclipse.core.runtime.preferences.IScopeContext projectScope = new org.eclipse.core.resources.ProjectScope(project);
        org.eclipse.core.runtime.preferences.IEclipsePreferences projectNode = projectScope.getNode(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN);
        if (projectNode != null) {
            java.lang.String kind = projectNode.get(org.eclipse.mylyn.internal.tasks.ui.properties.ProjectPropertiesLinkProvider.PROJECT_REPOSITORY_KIND, "");// $NON-NLS-1$

            java.lang.String urlString = projectNode.get(org.eclipse.mylyn.internal.tasks.ui.properties.ProjectPropertiesLinkProvider.PROJECT_REPOSITORY_URL, "");// $NON-NLS-1$

            return repositoryManager.getRepository(kind, urlString);
        }
        return null;
    }

    @java.lang.Override
    public boolean canSetTaskRepository(org.eclipse.core.resources.IResource resource) {
        org.eclipse.core.resources.IProject project = resource.getProject();
        return (project != null) && project.isAccessible();
    }

    @java.lang.Override
    public boolean setTaskRepository(org.eclipse.core.resources.IResource resource, org.eclipse.mylyn.tasks.core.TaskRepository repository) {
        org.eclipse.core.resources.IProject project = resource.getProject();
        if ((project == null) || (!project.isAccessible())) {
            return false;
        }
        org.eclipse.core.runtime.preferences.IScopeContext projectScope = new org.eclipse.core.resources.ProjectScope(project);
        org.eclipse.core.runtime.preferences.IEclipsePreferences projectNode = projectScope.getNode(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN);
        if (projectNode != null) {
            if (repository != null) {
                projectNode.put(org.eclipse.mylyn.internal.tasks.ui.properties.ProjectPropertiesLinkProvider.PROJECT_REPOSITORY_KIND, repository.getConnectorKind());
                projectNode.put(org.eclipse.mylyn.internal.tasks.ui.properties.ProjectPropertiesLinkProvider.PROJECT_REPOSITORY_URL, repository.getRepositoryUrl());
            } else {
                projectNode.remove(org.eclipse.mylyn.internal.tasks.ui.properties.ProjectPropertiesLinkProvider.PROJECT_REPOSITORY_KIND);
                projectNode.remove(org.eclipse.mylyn.internal.tasks.ui.properties.ProjectPropertiesLinkProvider.PROJECT_REPOSITORY_URL);
            }
            try {
                projectNode.flush();
                return true;
            } catch (org.osgi.service.prefs.BackingStoreException e) {
                org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Failed to save task repository to project association preference", e));// $NON-NLS-1$

            }
        }
        return false;
    }
}