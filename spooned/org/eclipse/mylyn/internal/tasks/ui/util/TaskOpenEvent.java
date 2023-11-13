/**
 * *****************************************************************************
 * Copyright (c) 2009, 2012 Tasktop Technologies and others.
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
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.ui.IEditorPart;
/**
 *
 * @author Steffen Pingel
 */
public class TaskOpenEvent {
    private final org.eclipse.mylyn.tasks.core.TaskRepository repository;

    private final org.eclipse.mylyn.tasks.core.ITask task;

    private final java.lang.String taskId;

    private final boolean inBrowser;

    private final org.eclipse.ui.IEditorPart editor;

    public TaskOpenEvent(org.eclipse.mylyn.tasks.core.TaskRepository repository, org.eclipse.mylyn.tasks.core.ITask task, java.lang.String taskId, org.eclipse.ui.IEditorPart editor, boolean inBrowser) {
        this.repository = repository;
        this.task = task;
        this.taskId = taskId;
        this.editor = editor;
        this.inBrowser = inBrowser;
    }

    public boolean isInBrowser() {
        return inBrowser;
    }

    public org.eclipse.mylyn.tasks.core.TaskRepository getRepository() {
        return repository;
    }

    public org.eclipse.mylyn.tasks.core.ITask getTask() {
        return task;
    }

    public java.lang.String getTaskId() {
        return taskId;
    }

    public org.eclipse.ui.IEditorPart getEditor() {
        return editor;
    }
}