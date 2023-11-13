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
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui;
/**
 *
 * @author Mik Kersten
 */
public interface ITasksUiPreferenceConstants {
    public static final java.lang.String ACTIVATE_MULTIPLE = "org.eclipse.mylyn.tasks.ui.activation.multipe";// $NON-NLS-1$


    public static final java.lang.String ACTIVATE_WHEN_OPENED = "org.eclipse.mylyn.tasks.ui.activate.when.opened";// $NON-NLS-1$


    public static final java.lang.String SHOW_TRIM = "org.eclipse.mylyn.tasks.ui.show.trim";// $NON-NLS-1$


    /**
     *
     * @deprecated not used anymore, see {@link TaskListBackupManager}
     */
    @java.lang.Deprecated
    public static final java.lang.String BACKUP_LAST = "org.eclipse.mylyn.tasks.ui.backup.last";// $NON-NLS-1$


    /**
     *
     * @deprecated not used anymore, see {@link TaskListBackupManager}
     */
    @java.lang.Deprecated
    public static final java.lang.String BACKUP_MAXFILES = "org.eclipse.mylyn.tasks.ui.backup.maxfiles";// $NON-NLS-1$


    /**
     *
     * @deprecated not used anymore, see {@link TaskListBackupManager}
     */
    @java.lang.Deprecated
    public static final java.lang.String BACKUP_SCHEDULE = "org.eclipse.mylyn.tasks.ui.backup.schedule";// $NON-NLS-1$


    public static final java.lang.String FILTER_ARCHIVE_MODE = "org.eclipse.mylyn.tasks.ui.filters.archive";// $NON-NLS-1$


    public static final java.lang.String FILTER_COMPLETE_MODE = "org.eclipse.mylyn.tasks.ui.filters.complete";// $NON-NLS-1$


    public static final java.lang.String FILTER_HIDDEN = "org.eclipse.mylyn.tasks.ui.filters.hidden";// $NON-NLS-1$


    public static final java.lang.String FILTER_NON_MATCHING = "org.eclipse.mylyn.tasks.ui.filters.nonmatching";// $NON-NLS-1$


    public static final java.lang.String ENCOURAGED_FILTER_NON_MATCHING = "org.eclipse.mylyn.tasks.ui.filters.nonmatching.encouraged";// $NON-NLS-1$


    public static final java.lang.String FILTER_PRIORITY = "org.eclipse.mylyn.tasks.ui.filters.priority";// $NON-NLS-1$


    public static final java.lang.String FILTER_MY_TASKS_MODE = "org.eclipse.mylyn.tasks.ui.filters.my.tasks";// $NON-NLS-1$


    public static final java.lang.String GROUP_SUBTASKS = "org.eclipse.mylyn.tasks.ui.group.subtasks";// $NON-NLS-1$


    public static final java.lang.String OVERLAYS_INCOMING_TIGHT = "org.eclipse.mylyn.tasks.ui.overlays.incoming.tight";// $NON-NLS-1$


    public static final java.lang.String NOTIFICATIONS_ENABLED = "org.eclipse.mylyn.tasks.ui.notifications.enabled";// $NON-NLS-1$


    public static final java.lang.String SERVICE_MESSAGES_ENABLED = "org.eclipse.mylyn.tasks.ui.messages.enabled";// $NON-NLS-1$


    public static final java.lang.String WEEK_START_DAY = "org.eclipse.mylyn.tasks.ui.planning.week.start.day";// $NON-NLS-1$


    public static final java.lang.String SCHEDULE_NEW_TASKS_FOR = "org.eclipse.mylyn.tasks.ui.planning.schedule.new.tasks.for";// $NON-NLS-1$


    public static final java.lang.String SCHEDULE_NEW_TASKS_FOR_THIS_WEEK = "org.eclipse.mylyn.tasks.ui.planning.schedule.new.tasks.for.this.week";// $NON-NLS-1$


    public static final java.lang.String SCHEDULE_NEW_TASKS_FOR_TOMORROW = "org.eclipse.mylyn.tasks.ui.planning.schedule.new.tasks.for.tomorrow";// $NON-NLS-1$


    public static final java.lang.String SCHEDULE_NEW_TASKS_FOR_TODAY = "org.eclipse.mylyn.tasks.ui.planning.schedule.new.tasks.for.today";// $NON-NLS-1$


    public static final java.lang.String SCHEDULE_NEW_TASKS_FOR_NOT_SCHEDULED = "org.eclipse.mylyn.tasks.ui.planning.schedule.new.tasks.for.unscheduled";// $NON-NLS-1$


    public static final java.lang.String PLANNING_ENDHOUR = "org.eclipse.mylyn.tasks.ui.planning.end.hour";// $NON-NLS-1$


    public static final java.lang.String EDITOR_TASKS_RICH = "org.eclipse.mylyn.tasks.ui.reporting.open.editor";// $NON-NLS-1$


    public static final java.lang.String EDITOR_CURRENT_LINE_HIGHLIGHT = "org.eclipse.mylyn.tasks.ui.editor.currentLineHighlight";// $NON-NLS-1$


    public static final java.lang.String USE_STRIKETHROUGH_FOR_COMPLETED = "org.eclipse.mylyn.tasks.ui.strikethrough.for.completed";// $NON-NLS-1$


    public static final java.lang.String REPOSITORY_SYNCH_SCHEDULE_ENABLED = "org.eclipse.mylyn.tasks.ui.repositories.synch.schedule";// $NON-NLS-1$


    public static final java.lang.String REPOSITORY_SYNCH_SCHEDULE_MILISECONDS = "org.eclipse.mylyn.tasks.ui.repositories.synch.schedule.miliseconds";// $NON-NLS-1$


    public static final java.lang.String RELEVANT_TASKS_SCHEDULE_MILISECONDS = "org.eclipse.mylyn.tasks.ui.relevant.tasks.synch.schedule.miliseconds";// $NON-NLS-1$


    public static final java.lang.String TEMPLATES_DELETED = "org.eclipse.mylyn.tasks.ui.templates.deleted";// $NON-NLS-1$


    public static final java.lang.String TEMPLATES_DELETED_DELIM = "|";// $NON-NLS-1$


    public static final java.lang.String AUTO_EXPAND_TASK_LIST = "org.eclipse.mylyn.tasks.ui.auto.expand";// $NON-NLS-1$


    public static final java.lang.String TASK_LIST_FOCUSED = "org.eclipse.mylyn.tasks.ui.task.list.focused";// $NON-NLS-1$


    /**
     * Local subtasks are now enabled by default.
     *
     * @deprecated not used anymore
     */
    // TODO 4.0 remove
    @java.lang.Deprecated
    public static final java.lang.String LOCAL_SUB_TASKS_ENABLED = "org.eclipse.mylyn.tasks.ui.subtasks.local";// $NON-NLS-1$


    public static final java.lang.String TASK_LIST_TOOL_TIPS_ENABLED = "org.eclipse.mylyn.tasks.ui.task.list.tool.tip";// $NON-NLS-1$


    // NOTE: legacy name, do not change
    public static final java.lang.String PREF_DATA_DIR = "org.eclipse.mylyn.data.dir";// $NON-NLS-1$


    public static final java.lang.String DEFAULT_ATTACHMENTS_DIRECTORY = "org.eclipse.mylyn.tasks.ui.attachments.defaultDirectory";// $NON-NLS-1$


    public static final java.lang.String PREFERRED_TASK_ATTACHMENT_VIEWER_ID = "org.eclipse.mylyn.tasks.ui.attachments.preferredViewerID";// $NON-NLS-1$


    public static final java.lang.String SERVICE_MESSAGE_URL = "org.eclipse.mylyn.tasks.ui.servicemessage.url";// $NON-NLS-1$;


    public static final java.lang.String LAST_SERVICE_MESSAGE_ID = "org.eclipse.mylyn.tasks.ui.servicemessage.id";// $NON-NLS-1$


    public static final java.lang.String LAST_SERVICE_MESSAGE_ETAG = "org.eclipse.mylyn.tasks.ui.servicemessage.etag";// $NON-NLS-1$


    public static final java.lang.String LAST_SERVICE_MESSAGE_LAST_MODIFIED = "org.eclipse.mylyn.tasks.ui.servicemessage.lastmodified";// $NON-NLS-1$


    public static final java.lang.String LAST_SERVICE_MESSAGE_CHECKTIME = "org.eclipse.mylyn.tasks.ui.servicemessage.checktime";// $NON-NLS-1$


    public static final java.lang.String WELCOME_MESSAGE = "org.eclipse.mylyn.tasks.ui.welcome.message";// $NON-NLS-1$

}