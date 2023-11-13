/**
 * *****************************************************************************
 * Copyright (c) 2004, 2015 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Eugene Kuleshov - improvements
 *     David Green - fix for bug 244442
 * *****************************************************************************
 */
package org.eclipse.mylyn.tasks.ui;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.ui.Messages;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskComment;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentModel;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskSearchPage;
import org.eclipse.mylyn.tasks.ui.wizards.TaskAttachmentPage;
import org.eclipse.ui.IEditorInput;
/**
 * Extend to provide connector-specific UI extensions. TODO: consider refactoring into extension points
 *
 * @author Mik Kersten
 * @since 2.0
 */
public abstract class AbstractRepositoryConnectorUi {
    private static final java.lang.String LABEL_TASK_DEFAULT = org.eclipse.mylyn.internal.tasks.ui.Messages.AbstractRepositoryConnectorUi_Task;

    private final boolean customNotificationHandling = false;

    private final org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector;

    /**
     * Connectors should provide a one-argument constructor that can be used to pass the connector into the connector UI
     * in tests. Connectors must provide a default constructor so that they can be created via extension point.
     *
     * @param connector
     * 		the task repository connector
     * @noreference This method is not intended to be referenced by clients.
     */
    public AbstractRepositoryConnectorUi(org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector) {
        this.connector = connector;
    }

    public AbstractRepositoryConnectorUi() {
        this(null);
    }

    /**
     *
     * @return the unique type of the repository, e.g. "bugzilla"
     */
    @org.eclipse.jdt.annotation.NonNull
    public abstract java.lang.String getConnectorKind();

    /**
     *
     * @param repository
     * 		the repository to edit or {@code null} if creating a new repository
     * @since 3.0
     */
    @org.eclipse.jdt.annotation.NonNull
    public abstract org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage getSettingsPage(@org.eclipse.jdt.annotation.Nullable
    org.eclipse.mylyn.tasks.core.TaskRepository repository);

    /**
     *
     * @param repository
     * @param queryToEdit
     * 		can be null
     * @since 3.0
     */
    @org.eclipse.jdt.annotation.NonNull
    public abstract org.eclipse.jface.wizard.IWizard getQueryWizard(@org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.core.TaskRepository repository, @org.eclipse.jdt.annotation.Nullable
    org.eclipse.mylyn.tasks.core.IRepositoryQuery query);

    /**
     *
     * @since 3.0
     */
    @org.eclipse.jdt.annotation.NonNull
    public abstract org.eclipse.jface.wizard.IWizard getNewTaskWizard(@org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.core.TaskRepository repository, @org.eclipse.jdt.annotation.Nullable
    org.eclipse.mylyn.tasks.core.ITaskMapping selection);

    /**
     * Connectors can implement this method if they need to open a wizard dialog before opening a new subtask editor.
     *
     * @since 3.18
     */
    @org.eclipse.jdt.annotation.Nullable
    public org.eclipse.jface.wizard.IWizard getNewSubTaskWizard(@org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.core.TaskRepository taskRepository, @org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.core.ITask parentTask) {
        return null;
    }

    /**
     * Override to return a custom task editor ID. If overriding this method the connector becomes responsible for
     * showing the additional pages handled by the default task editor. As of Mylyn 2.0M2 these are the Planning and
     * Context pages.
     *
     * @since 3.0
     */
    @org.eclipse.jdt.annotation.NonNull
    public java.lang.String getTaskEditorId(@org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.core.ITask repositoryTask) {
        return org.eclipse.mylyn.tasks.ui.editors.TaskEditor.ID_EDITOR;
    }

    /**
     * Default implementation returns the standard {@link org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput}. Override
     * this method to return a custom task editor input. The connector author must ensure the corresponding editor is
     * capable of opening this editor input and will likely need to override
     * AbstractRepositoryConnectorUi.getTaskEditorId() as well.
     *
     * @param repository
     * 		- task repository for which to construct an editor
     * @param task
     * 		- the task to edit
     * @since 3.4
     */
    @org.eclipse.jdt.annotation.NonNull
    public org.eclipse.ui.IEditorInput getTaskEditorInput(@org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.core.TaskRepository repository, @org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.core.ITask task) {
        return new org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput(repository, task);
    }

    public abstract boolean hasSearchPage();

    /**
     * Contributions to the UI legend.
     *
     * @deprecated use {@link #getLegendElements()} instead
     */
    @java.lang.Deprecated
    @org.eclipse.jdt.annotation.NonNull
    public java.util.List<org.eclipse.mylyn.tasks.core.ITask> getLegendItems() {
        return java.util.Collections.emptyList();
    }

    /**
     * Contributions to the UI legend.
     *
     * @since 3.0
     */
    @org.eclipse.jdt.annotation.NonNull
    public java.util.List<org.eclipse.mylyn.tasks.ui.LegendElement> getLegendElements() {
        return java.util.Collections.emptyList();
    }

    /**
     *
     * @param repositoryTask
     * 		can be null
     * @since 3.0
     */
    @org.eclipse.jdt.annotation.NonNull
    public java.lang.String getTaskKindLabel(@org.eclipse.jdt.annotation.Nullable
    org.eclipse.mylyn.tasks.core.ITask task) {
        return org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi.LABEL_TASK_DEFAULT;
    }

    /**
     * Connector-specific task icons. Not recommended to override unless providing custom icons and kind overlays. For
     * connectors that have a decorator that they want to reuse, the connector can maintain a reference to the label
     * provider and get the descriptor from the images it returns.
     *
     * @since 3.0
     */
    @org.eclipse.jdt.annotation.Nullable
    public org.eclipse.jface.resource.ImageDescriptor getImageDescriptor(@org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.core.IRepositoryElement element) {
        if (element instanceof org.eclipse.mylyn.internal.tasks.core.RepositoryQuery) {
            return ((org.eclipse.mylyn.internal.tasks.core.RepositoryQuery) (element)).getAutoUpdate() ? TasksUiImages.QUERY : TasksUiImages.QUERY_OFFLINE;
        } else if (element instanceof org.eclipse.mylyn.tasks.core.ITask) {
            org.eclipse.mylyn.tasks.core.ITask task = ((org.eclipse.mylyn.tasks.core.ITask) (element));
            org.eclipse.mylyn.tasks.core.TaskRepository repository = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryManager().getRepository(task.getRepositoryUrl());
            if (((repository != null) && (connector != null)) && connector.isOwnedByUser(repository, task)) {
                return TasksUiImages.TASK_OWNED;
            }
            return TasksUiImages.TASK;
        } else {
            return null;
        }
    }

    /**
     * Task kind overlay, recommended to override with connector-specific overlay.
     *
     * @since 3.0
     */
    @org.eclipse.jdt.annotation.Nullable
    public org.eclipse.jface.resource.ImageDescriptor getTaskKindOverlay(@org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.core.ITask task) {
        return null;
    }

    /**
     * Connector-specific priority icons. Not recommended to override since priority icons are used elsewhere in the
     * Task List UI (e.g. filter selection in view menu).
     *
     * @since 3.0
     */
    @org.eclipse.jdt.annotation.NonNull
    public org.eclipse.jface.resource.ImageDescriptor getTaskPriorityOverlay(@org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.core.ITask task) {
        return org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getPriorityImage(task);
    }

    /**
     * This method is not used anymore.
     *
     * @return returns null
     */
    @java.lang.Deprecated
    @org.eclipse.jdt.annotation.Nullable
    public org.eclipse.jface.wizard.IWizard getAddExistingTaskWizard(@org.eclipse.jdt.annotation.Nullable
    org.eclipse.mylyn.tasks.core.TaskRepository repository) {
        return null;
    }

    /**
     *
     * @since 3.0
     */
    @org.eclipse.jdt.annotation.Nullable
    public org.eclipse.mylyn.tasks.ui.wizards.ITaskSearchPage getSearchPage(@org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.core.TaskRepository repository, @org.eclipse.jdt.annotation.Nullable
    org.eclipse.jface.viewers.IStructuredSelection selection) {
        return null;
    }

    /**
     * Override to return a URL that provides the user with an account creation page for the repository
     *
     * @param taskRepository
     * 		TODO
     */
    @org.eclipse.jdt.annotation.Nullable
    public java.lang.String getAccountCreationUrl(@org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.core.TaskRepository taskRepository) {
        return null;
    }

    /**
     * Override to return a URL that provides the user with an account management page for the repository
     *
     * @param taskRepository
     * 		TODO
     */
    @org.eclipse.jdt.annotation.Nullable
    public java.lang.String getAccountManagementUrl(@org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.core.TaskRepository taskRepository) {
        return null;
    }

    /**
     * Override to return a URL that provides the user with a history page for the task.
     *
     * @return a url of a page for the history of the task; null, if no history url is available
     * @since 3.0
     */
    @org.eclipse.jdt.annotation.Nullable
    public java.lang.String getTaskHistoryUrl(@org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.core.TaskRepository taskRepository, @org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.core.ITask task) {
        return null;
    }

    /**
     * Override to return a specific textual reference to a comment, e.g. by default this method returns
     * <code>In reply to comment #12</code> for a reply to comment 12. This text is used when generating replies to
     * comments.
     *
     * @return the reply text with a reference to <code>taskComment</code>; null, if no reference is available
     * @since 3.0
     */
    @org.eclipse.jdt.annotation.Nullable
    public java.lang.String getReplyText(@org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.core.TaskRepository taskRepository, @org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.core.ITask task, @org.eclipse.jdt.annotation.Nullable
    org.eclipse.mylyn.tasks.core.ITaskComment taskComment, boolean includeTask) {
        if (taskComment == null) {
            return org.eclipse.mylyn.internal.tasks.ui.Messages.AbstractRepositoryConnectorUi_InReplyToDescription;
        } else if (includeTask) {
            return java.text.MessageFormat.format(org.eclipse.mylyn.internal.tasks.ui.Messages.AbstractRepositoryConnectorUi_InReplyToTaskAndComment, task.getTaskKey(), taskComment.getNumber());
        } else {
            return java.text.MessageFormat.format(org.eclipse.mylyn.internal.tasks.ui.Messages.AbstractRepositoryConnectorUi_InReplyToComment, taskComment.getNumber());
        }
    }

    /**
     * Returns an array of hyperlinks that link to tasks within <code>text</code>. If <code>index</code> is != -1
     * clients may limit the results to hyperlinks found at <code>index</code>. It is legal for clients to always return
     * all results.
     *
     * @param repository
     * 		the task repository, never <code>null</code>
     * @param text
     * 		the line of text
     * @param index
     * 		the index within <code>text</code>, if -1 return all hyperlinks found in text
     * @param textOffset
     * 		the offset of <code>text</code>
     * @return an array of hyperlinks, or null if no hyperlinks were found
     * @since 2.0
     * @deprecated use {@link #findHyperlinks(TaskRepository, ITask, String, int, int)} instead
     */
    @java.lang.Deprecated
    @org.eclipse.jdt.annotation.Nullable
    public org.eclipse.jface.text.hyperlink.IHyperlink[] findHyperlinks(@org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.core.TaskRepository repository, @org.eclipse.jdt.annotation.NonNull
    java.lang.String text, int index, int textOffset) {
        return null;
    }

    /**
     *
     * @since 3.0
     */
    public boolean hasCustomNotifications() {
        return customNotificationHandling;
    }

    /**
     *
     * @since 3.0
     * @return true if connector doesn't support non-grouping (flattening) of subtasks
     */
    public boolean hasStrictSubtaskHierarchy() {
        return false;
    }

    /**
     *
     * @since 3.0
     */
    @org.eclipse.jdt.annotation.NonNull
    public org.eclipse.jface.wizard.IWizardPage getTaskAttachmentPage(@org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.core.data.TaskAttachmentModel model) {
        return new org.eclipse.mylyn.tasks.ui.wizards.TaskAttachmentPage(model);
    }

    /**
     * Returns an array of hyperlinks that link to tasks within <code>text</code>. If <code>index</code> is != -1
     * clients may limit the results to hyperlinks found at <code>index</code>. It is legal for clients to always return
     * all results.
     *
     * @param repository
     * 		the task repository, never <code>null</code>
     * @param task
     * 		the task, can be <code>null</code>
     * @param text
     * 		the line of text
     * @param index
     * 		the index within <code>text</code>, if -1 return all hyperlinks found in text
     * @param textOffset
     * 		the offset of <code>text</code>
     * @return an array of hyperlinks, or null if no hyperlinks were found
     * @since 3.4
     */
    @org.eclipse.jdt.annotation.Nullable
    public org.eclipse.jface.text.hyperlink.IHyperlink[] findHyperlinks(@org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.core.TaskRepository repository, @org.eclipse.jdt.annotation.Nullable
    org.eclipse.mylyn.tasks.core.ITask task, @org.eclipse.jdt.annotation.NonNull
    java.lang.String text, int index, int textOffset) {
        return findHyperlinks(repository, text, index, textOffset);
    }

    /**
     *
     * @return the {@link AbstractRepositoryConnector} that is associated to this instance
     * @since 3.14
     */
    public org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector getConnector() {
        return connector;
    }
}