/**
 * *****************************************************************************
 * Copyright (c) 2012 Tasktop Technologies and others.
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
import org.eclipse.core.runtime.Assert;
import org.eclipse.mylyn.commons.workbench.EditorHandle;
import org.eclipse.mylyn.commons.workbench.browser.AbstractUrlHandler;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.LocalTask;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.ui.IWorkbenchPage;
/**
 *
 * @author Steffen Pingel
 */
public class TaskUrlHandler extends org.eclipse.mylyn.commons.workbench.browser.AbstractUrlHandler {
    public TaskUrlHandler() {
        // ignore
    }

    @java.lang.Override
    public org.eclipse.mylyn.commons.workbench.EditorHandle openUrl(org.eclipse.ui.IWorkbenchPage page, java.lang.String url, int customFlags) {
        org.eclipse.core.runtime.Assert.isNotNull(url);
        org.eclipse.mylyn.internal.tasks.core.AbstractTask task = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskByUrl(url);
        if ((task != null) && (!(task instanceof org.eclipse.mylyn.internal.tasks.core.LocalTask))) {
            org.eclipse.mylyn.tasks.core.TaskRepository repository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(task.getConnectorKind(), task.getRepositoryUrl());
            return org.eclipse.mylyn.tasks.ui.TasksUiUtil.openTaskWithResult(repository, task.getTaskId());
        } else {
            org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryManager().getConnectorForRepositoryTaskUrl(url);
            if (connector != null) {
                java.lang.String repositoryUrl = connector.getRepositoryUrlFromTaskUrl(url);
                if (repositoryUrl != null) {
                    java.lang.String id = connector.getTaskIdFromTaskUrl(url);
                    if (id != null) {
                        org.eclipse.mylyn.tasks.core.TaskRepository repository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(connector.getConnectorKind(), repositoryUrl);
                        if (repository != null) {
                            return org.eclipse.mylyn.tasks.ui.TasksUiUtil.openTaskWithResult(repository, id);
                        }
                    }
                }
            }
        }
        return null;
    }
}