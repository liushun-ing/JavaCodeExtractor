/**
 * *****************************************************************************
 * Copyright (c) 2004, 2013 Tasktop Technologies and others.
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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.ITask.PriorityLevel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
/**
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @author Mik Kersten
 * @since 3.0
 */
public class TasksUiImages {
    private static final java.net.URL baseURL = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getBundle().getEntry("/icons/");// $NON-NLS-1$


    private static final java.lang.String VIEW = "eview16";// $NON-NLS-1$


    private static final java.lang.String TOOL = "etool16";// $NON-NLS-1$


    private static final java.lang.String TOOL_SMALL = "etool12";// $NON-NLS-1$


    private static final java.lang.String OBJ = "obj16";// $NON-NLS-1$


    private static final java.lang.String OVERLAY = "ovr16";// $NON-NLS-1$


    private static final java.lang.String WIZBAN = "wizban";// $NON-NLS-1$


    // Tasks and Task List elements
    public static final org.eclipse.jface.resource.ImageDescriptor TASK = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "task.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor TASK_COMPLETE = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "task-complete.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor TASK_INCOMPLETE = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "task-incomplete.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor TASK_COMPLETED = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "task-completed.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor TASK_NOTES = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "task-notes.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor TASK_NEW = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "task-new.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor TASK_NEW_SUB = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "sub-task-new.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor TASK_REPOSITORY_HISTORY = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "task-repository-history.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor TASK_REMOTE = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "task-remote.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor TASK_WORKING_SET = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "open-task.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor TASKS_VIEW = org.eclipse.mylyn.tasks.ui.TasksUiImages.create("eview16", "task-list.gif");// $NON-NLS-1$ //$NON-NLS-2$


    public static final org.eclipse.jface.resource.ImageDescriptor TASK_ATTACHMENT_PATCH = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.OBJ, "attachment-patch.gif");// $NON-NLS-1$


    /**
     *
     * @since 3.21
     */
    public static final org.eclipse.jface.resource.ImageDescriptor TASK_OWNED = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "task-owned.gif");// $NON-NLS-1$


    /**
     *
     * @since 3.7
     */
    public static final org.eclipse.jface.resource.ImageDescriptor FILTER_OBSOLETE_SMALL = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL_SMALL, "file-delete-line-12x12.png");// $NON-NLS-1$


    /**
     *
     * @since 3.7
     */
    public static final org.eclipse.jface.resource.ImageDescriptor FILE_NEW_SMALL = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL_SMALL, "file-new-12x12.png");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor TASK_RETRIEVE = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "task-retrieve.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor TASK_REPOSITORY = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "task-repository.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor TASK_REPOSITORY_NEW = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "task-repository-new.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor CATEGORY = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "category.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor CATEGORY_NEW = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "category-new.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor CATEGORY_UNCATEGORIZED = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "category-archive.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor QUERY = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "query.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor QUERY_NEW = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "query-new.gif");// $NON-NLS-1$


    /**
     *
     * @since 3.2
     */
    public static final org.eclipse.jface.resource.ImageDescriptor QUERY_OFFLINE = org.eclipse.jface.resource.ImageDescriptor.createWithFlags(org.eclipse.mylyn.tasks.ui.TasksUiImages.QUERY, org.eclipse.swt.SWT.IMAGE_GRAY);

    public static final org.eclipse.jface.resource.ImageDescriptor QUERY_UNMATCHED = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "query-unmatched.png");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor REPOSITORY = org.eclipse.mylyn.tasks.ui.TasksUiImages.create("eview16", "repository.gif");// $NON-NLS-1$ //$NON-NLS-2$


    public static final org.eclipse.jface.resource.ImageDescriptor REPOSITORY_OFFLINE = org.eclipse.jface.resource.ImageDescriptor.createWithFlags(org.eclipse.mylyn.tasks.ui.TasksUiImages.REPOSITORY, org.eclipse.swt.SWT.IMAGE_GRAY);

    public static final org.eclipse.jface.resource.ImageDescriptor REPOSITORY_SYNCHRONIZE_SMALL = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL_SMALL, "repository-synchronize-small.png");// $NON-NLS-1$


    /**
     *
     * @since 3.2
     */
    public static final org.eclipse.jface.resource.ImageDescriptor REPOSITORY_UPDATE_CONFIGURATION = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "repository-synchronize-attributes.png");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor REPOSITORY_SYNCHRONIZE = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "repository-synchronize.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor REPOSITORY_SUBMIT = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "repository-submit.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor REPOSITORY_SMALL = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.OBJ, "repository-small.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor REPOSITORY_NEW = org.eclipse.mylyn.tasks.ui.TasksUiImages.create("etool16", "repository-new.gif");// $NON-NLS-1$ //$NON-NLS-2$


    /**
     *
     * @since 3.1
     */
    public static final org.eclipse.jface.resource.ImageDescriptor REPOSITORY_VALIDATE = org.eclipse.mylyn.tasks.ui.TasksUiImages.create("obj16", "resource_obj.gif");// $NON-NLS-1$ //$NON-NLS-2$


    public static final org.eclipse.jface.resource.ImageDescriptor REPOSITORIES_VIEW = org.eclipse.mylyn.tasks.ui.TasksUiImages.create("eview16", "repositories.gif");// $NON-NLS-1$ //$NON-NLS-2$


    // Context and activation
    public static final org.eclipse.jface.resource.ImageDescriptor CONTEXT_ACTIVE = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "task-active.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor CONTEXT_ACTIVE_CENTERED = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "task-active-centered.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor CONTEXT_INACTIVE_EMPTY = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "task-inactive.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor CONTEXT_INACTIVE = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "task-context.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor CONTEXT_FOCUS = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.VIEW, "focus.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor CONTEXT_ATTACH = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "context-attach.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor CONTEXT_RETRIEVE = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "context-retrieve.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor CONTEXT_TRANSFER = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "context-transfer.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor CONTEXT_CLEAR = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "context-clear.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor CONTEXT_HISTORY_PREVIOUS = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "navigate-previous.gif");// $NON-NLS-1$


    @java.lang.Deprecated
    public static final org.eclipse.jface.resource.ImageDescriptor CONTEXT_HISTORY_PREVIOUS_PAUSE = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "navigate-previous-pause.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor CONTEXT_HISTORY_PREVIOUS_ACTIVE = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "navigate-previous-active.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor CONTEXT_HISTORY_NEXT = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "navigate-next.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor CONTEXT_CAPTURE_PAUSE = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "capture-pause.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor CONTEXT_ADD = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "context-add.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor CONTEXT_COPY = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "context-transfer.gif");// $NON-NLS-1$


    // Comments and collaboration
    public static final org.eclipse.jface.resource.ImageDescriptor COMMENT = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "comment.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor COMMENT_SORT_DOWN = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "sort-down.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor COMMENT_SORT_UP = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "sort-up.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor COMMENT_SORT_DOWN_GRAY = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "sort-down-gray.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor COMMENT_SORT_UP_GRAY = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "sort-up-gray.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor COMMENT_REPLY = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "reply.gif");// $NON-NLS-1$


    /**
     *
     * @since 3.1
     */
    public static final org.eclipse.jface.resource.ImageDescriptor COMMENT_REPLY_SMALL = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL_SMALL, "reply.gif");// $NON-NLS-1$


    // Wizard banners
    public static final org.eclipse.jface.resource.ImageDescriptor BANNER_REPOSITORY = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.WIZBAN, "banner-repository.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor BANNER_REPOSITORY_SETTINGS = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.WIZBAN, "banner-repository-settings.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor BANNER_REPOSITORY_CONTEXT = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.WIZBAN, "banner-repository-context.gif");// $NON-NLS-1$


    public static final org.eclipse.jface.resource.ImageDescriptor BANNER_WORKING_SET = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.WIZBAN, "workset_wiz.png");// $NON-NLS-1$


    /**
     *
     * @since 3.2
     */
    public static final org.eclipse.jface.resource.ImageDescriptor PRESENTATION_CATEGORIZED = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "presentation-categorized.png");// $NON-NLS-1$


    /**
     *
     * @since 3.2
     */
    public static final org.eclipse.jface.resource.ImageDescriptor PRESENTATION_SCHEDULED = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL, "presentation-scheduled.png");// $NON-NLS-1$


    /**
     *
     * @since 3.2
     */
    public static final org.eclipse.jface.resource.ImageDescriptor BANNER_REPORT_BUG = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.WIZBAN, "bug-wizard.gif");// $NON-NLS-1$


    /**
     *
     * @since 3.7
     */
    public static final org.eclipse.jface.resource.ImageDescriptor LOCK_CLOSE = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL_SMALL, "lock.gif");// $NON-NLS-1$


    /**
     *
     * @since 3.7
     */
    public static final org.eclipse.jface.resource.ImageDescriptor LOCK_OPEN = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL_SMALL, "unlock.gif");// $NON-NLS-1$


    /**
     *
     * @since 3.8
     */
    public static final org.eclipse.jface.resource.ImageDescriptor IMAGE_CAPTURE_SMALL = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.TOOL_SMALL, "image_capture.png");// $NON-NLS-1$


    /**
     *
     * @since 3.10
     */
    public static final org.eclipse.jface.resource.ImageDescriptor NOTES = org.eclipse.mylyn.tasks.ui.TasksUiImages.create(org.eclipse.mylyn.tasks.ui.TasksUiImages.OVERLAY, "overlay-notes.gif");// $NON-NLS-1$


    public static org.eclipse.swt.graphics.Image getImageForPriority(org.eclipse.mylyn.tasks.core.ITask.PriorityLevel priorityLevel) {
        if (priorityLevel == null) {
            return null;
        } else {
            org.eclipse.jface.resource.ImageDescriptor imageDescriptor = org.eclipse.mylyn.tasks.ui.TasksUiImages.getImageDescriptorForPriority(priorityLevel);
            if (imageDescriptor != null) {
                return org.eclipse.mylyn.commons.ui.CommonImages.getImage(imageDescriptor);
            }
        }
        return null;
    }

    public static org.eclipse.jface.resource.ImageDescriptor getImageDescriptorForPriority(org.eclipse.mylyn.tasks.core.ITask.PriorityLevel priorityLevel) {
        if (priorityLevel == null) {
            return null;
        }
        switch (priorityLevel) {
            case P1 :
                return org.eclipse.mylyn.commons.ui.CommonImages.PRIORITY_1;
            case P2 :
                return org.eclipse.mylyn.commons.ui.CommonImages.PRIORITY_2;
            case P3 :
                return org.eclipse.mylyn.commons.ui.CommonImages.PRIORITY_3;
            case P4 :
                return org.eclipse.mylyn.commons.ui.CommonImages.PRIORITY_4;
            case P5 :
                return org.eclipse.mylyn.commons.ui.CommonImages.PRIORITY_5;
            default :
                return null;
        }
    }

    public static org.eclipse.jface.resource.ImageDescriptor create(java.lang.String prefix, java.lang.String name) {
        try {
            return org.eclipse.jface.resource.ImageDescriptor.createFromURL(org.eclipse.mylyn.tasks.ui.TasksUiImages.makeIconFileURL(prefix, name));
        } catch (java.net.MalformedURLException e) {
            return org.eclipse.jface.resource.ImageDescriptor.getMissingImageDescriptor();
        }
    }

    private static java.net.URL makeIconFileURL(java.lang.String prefix, java.lang.String name) throws java.net.MalformedURLException {
        if (org.eclipse.mylyn.tasks.ui.TasksUiImages.baseURL == null) {
            throw new java.net.MalformedURLException();
        }
        java.lang.StringBuffer buffer = new java.lang.StringBuffer(prefix);
        buffer.append('/');
        buffer.append(name);
        return new java.net.URL(org.eclipse.mylyn.tasks.ui.TasksUiImages.baseURL, buffer.toString());
    }
}