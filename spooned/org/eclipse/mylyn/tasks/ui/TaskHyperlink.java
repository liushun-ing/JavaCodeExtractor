/**
 * *****************************************************************************
 * Copyright (c) 2004, 2010 Eugene Kuleshov and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugene Kuleshov - initial API and implementation
 *     Tasktop Technologies - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.tasks.ui;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.mylyn.internal.tasks.ui.Messages;
import org.eclipse.mylyn.internal.tasks.ui.util.TaskOpenEvent;
import org.eclipse.mylyn.internal.tasks.ui.util.TaskOpenListener;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
/**
 * Immutable. Encapsulates information for linking to tasks from text.
 *
 * @author Eugene Kuleshov
 * @author Steffen Pingel
 */
public final class TaskHyperlink implements org.eclipse.jface.text.hyperlink.IHyperlink {
    private final org.eclipse.jface.text.IRegion region;

    private final org.eclipse.mylyn.tasks.core.TaskRepository repository;

    private final java.lang.String taskId;

    private java.lang.Object selection;

    public TaskHyperlink(org.eclipse.jface.text.IRegion region, org.eclipse.mylyn.tasks.core.TaskRepository repository, java.lang.String taskId) {
        this.region = region;
        this.repository = repository;
        this.taskId = taskId;
    }

    public org.eclipse.jface.text.IRegion getHyperlinkRegion() {
        return region;
    }

    public java.lang.String getTaskId() {
        return taskId;
    }

    public java.lang.String getTypeLabel() {
        return null;
    }

    /**
     *
     * @Since 2.1
     * @return  */
    public org.eclipse.mylyn.tasks.core.TaskRepository getRepository() {
        return repository;
    }

    public java.lang.String getHyperlinkText() {
        return java.text.MessageFormat.format(org.eclipse.mylyn.internal.tasks.ui.Messages.TaskHyperlink_Open_Task_X_in_X, taskId, repository.getRepositoryLabel());
    }

    public void open() {
        if (repository != null) {
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.openTask(repository, taskId, new org.eclipse.mylyn.internal.tasks.ui.util.TaskOpenListener() {
                @java.lang.Override
                public void taskOpened(org.eclipse.mylyn.internal.tasks.ui.util.TaskOpenEvent event) {
                    if (selection == null) {
                        return;
                    }
                    if (event.getEditor() instanceof org.eclipse.mylyn.tasks.ui.editors.TaskEditor) {
                        org.eclipse.mylyn.tasks.ui.editors.TaskEditor editor = ((org.eclipse.mylyn.tasks.ui.editors.TaskEditor) (event.getEditor()));
                        editor.selectReveal(selection);
                    }
                }
            });
        } else {
            org.eclipse.jface.dialogs.MessageDialog.openError(null, "Mylyn", org.eclipse.mylyn.internal.tasks.ui.Messages.TaskHyperlink_Could_not_determine_repository_for_report);// $NON-NLS-1$

        }
    }

    /**
     * Returns the selection to select and reveal when opening the task.
     *
     * @return an object or null if not set
     * @see #setSelection(Object)
     * @see TaskEditor#selectReveal(Object)
     * @since 3.4
     */
    public java.lang.Object getSelection() {
        return selection;
    }

    /**
     * Sets the selection to select and reveal when opening the task.
     *
     * @param selection
     * 		the selection
     * @see #getSelection()
     * @see TaskEditor#selectReveal(Object)
     * @since 3.4
     */
    public void setSelection(java.lang.Object selection) {
        this.selection = selection;
    }

    @java.lang.Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (region == null ? 0 : region.hashCode());
        result = (prime * result) + (repository == null ? 0 : repository.hashCode());
        result = (prime * result) + (selection == null ? 0 : selection.hashCode());
        result = (prime * result) + (taskId == null ? 0 : taskId.hashCode());
        return result;
    }

    @java.lang.Override
    public boolean equals(java.lang.Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        org.eclipse.mylyn.tasks.ui.TaskHyperlink other = ((org.eclipse.mylyn.tasks.ui.TaskHyperlink) (obj));
        if (region == null) {
            if (other.region != null) {
                return false;
            }
        } else if (!region.equals(other.region)) {
            return false;
        }
        if (repository == null) {
            if (other.repository != null) {
                return false;
            }
        } else if (!repository.equals(other.repository)) {
            return false;
        }
        if (selection == null) {
            if (other.selection != null) {
                return false;
            }
        } else if (!selection.equals(other.selection)) {
            return false;
        }
        if (taskId == null) {
            if (other.taskId != null) {
                return false;
            }
        } else if (!taskId.equals(other.taskId)) {
            return false;
        }
        return true;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return ((((((("TaskHyperlink [region=" + region) + ", repository=") + repository) + ", selection=") + selection)// $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
         + ", taskId=") + taskId) + "]";// $NON-NLS-1$ //$NON-NLS-2$

    }
}