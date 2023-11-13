/**
 * *****************************************************************************
 * Copyright (c) 2004, 2011 Tasktop Technologies and others.
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
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.RepositoryTemplate;
import org.eclipse.mylyn.tasks.ui.TasksUi;
/**
 *
 * @author Rob Elves
 */
public class TaskRepositoryUtil {
    /**
     * Is auto add of template repository disabled for repositoryUrl
     *
     * @since 2.1
     */
    public static boolean isAddAutomaticallyDisabled(java.lang.String repositoryUrl) {
        java.lang.String deletedTemplates = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().getString(ITasksUiPreferenceConstants.TEMPLATES_DELETED);
        java.lang.String[] templateUrls = deletedTemplates.split("\\" + ITasksUiPreferenceConstants.TEMPLATES_DELETED_DELIM);// $NON-NLS-1$

        for (java.lang.String deletedUrl : templateUrls) {
            if (deletedUrl.equalsIgnoreCase(repositoryUrl)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Disable template repository from being automatically added
     *
     * @since 2.1
     */
    public static void disableAddAutomatically(java.lang.String repositoryUrl) {
        if ((!org.eclipse.mylyn.internal.tasks.ui.TaskRepositoryUtil.isAddAutomaticallyDisabled(repositoryUrl)) && org.eclipse.mylyn.internal.tasks.ui.TaskRepositoryUtil.isAddAutomatically(repositoryUrl)) {
            java.lang.String deletedTemplates = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().getString(ITasksUiPreferenceConstants.TEMPLATES_DELETED);
            deletedTemplates += ITasksUiPreferenceConstants.TEMPLATES_DELETED_DELIM + repositoryUrl;
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().setValue(ITasksUiPreferenceConstants.TEMPLATES_DELETED, deletedTemplates);
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().savePluginPreferences();
        }
    }

    /**
     * Template exists and is auto add enabled
     */
    public static boolean isAddAutomatically(java.lang.String repositoryUrl) {
        for (org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector : org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnectors()) {
            for (org.eclipse.mylyn.tasks.core.RepositoryTemplate template : org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryTemplateManager().getTemplates(connector.getConnectorKind())) {
                if (((template.repositoryUrl != null) && template.repositoryUrl.equalsIgnoreCase(repositoryUrl)) && template.addAutomatically) {
                    return true;
                }
            }
        }
        return false;
    }
}