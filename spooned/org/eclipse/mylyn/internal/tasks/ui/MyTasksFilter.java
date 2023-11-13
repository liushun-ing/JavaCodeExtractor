/**
 * *****************************************************************************
 * Copyright (c) 2013, 2015 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.LocalTask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
public class MyTasksFilter extends org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter {
    public MyTasksFilter() {
        // ignore
    }

    @java.lang.Override
    public boolean select(java.lang.Object parent, java.lang.Object element) {
        if (element instanceof org.eclipse.mylyn.internal.tasks.core.LocalTask) {
            return true;
        }
        if (element instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask) {
            org.eclipse.mylyn.internal.tasks.core.AbstractTask task = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (element));
            org.eclipse.mylyn.tasks.core.TaskRepository repository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(task.getConnectorKind(), task.getRepositoryUrl());
            if (repository != null) {
                return org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryConnector(task.getConnectorKind()).isOwnedByUser(repository, task);
            }
            return false;
        }
        return true;
    }
}