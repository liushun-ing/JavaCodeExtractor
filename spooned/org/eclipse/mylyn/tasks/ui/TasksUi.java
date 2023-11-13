/**
 * *****************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.tasks.ui;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryManager;
import org.eclipse.mylyn.tasks.core.IRepositoryModel;
import org.eclipse.mylyn.tasks.core.ITaskActivityManager;
import org.eclipse.mylyn.tasks.core.data.ITaskDataManager;
/**
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @author Steffen Pingel
 * @author Mik Kersten
 * @since 3.0
 */
public class TasksUi {
    /**
     *
     * @since 3.0
     */
    public static org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector getRepositoryConnector(java.lang.String kind) {
        return org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(kind);
    }

    /**
     *
     * @since 3.0
     */
    public static org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi getRepositoryConnectorUi(java.lang.String kind) {
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnectorUi(kind);
    }

    /**
     *
     * @since 3.0
     */
    public static org.eclipse.mylyn.tasks.core.IRepositoryManager getRepositoryManager() {
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryManager();
    }

    /**
     *
     * @since 3.0
     */
    public static org.eclipse.mylyn.tasks.core.ITaskActivityManager getTaskActivityManager() {
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager();
    }

    /**
     *
     * @since 3.0
     */
    public static org.eclipse.mylyn.tasks.core.data.ITaskDataManager getTaskDataManager() {
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskDataManager();
    }

    /**
     *
     * @since 3.0
     */
    public static org.eclipse.mylyn.tasks.core.IRepositoryModel getRepositoryModel() {
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryModel();
    }

    /**
     *
     * @since 3.1
     */
    public static org.eclipse.mylyn.tasks.ui.ITasksUiFactory getUiFactory() {
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getUiFactory();
    }
}