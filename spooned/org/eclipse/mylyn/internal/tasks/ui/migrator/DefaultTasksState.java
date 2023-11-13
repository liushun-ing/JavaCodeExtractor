/**
 * *****************************************************************************
 * Copyright (c) 2015 Tasktop Technologies.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.migrator;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
public class DefaultTasksState extends org.eclipse.mylyn.internal.tasks.ui.migrator.TasksState {
    public DefaultTasksState() {
        super(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList(), org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskDataManager(), org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryManager(), org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryModel(), org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getContextStore(), org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager(), org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskJobFactory());
    }
}