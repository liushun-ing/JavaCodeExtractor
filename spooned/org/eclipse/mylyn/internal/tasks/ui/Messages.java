/**
 * *****************************************************************************
 * Copyright (c) 2004, 2016 Tasktop Technologies and others.
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
import org.eclipse.osgi.util.NLS;
public class Messages extends org.eclipse.osgi.util.NLS {
    private static final java.lang.String BUNDLE_NAME = "org.eclipse.mylyn.internal.tasks.ui.messages";// $NON-NLS-1$


    static {
        // load message values from bundle file
        reloadMessages();
    }

    public static void reloadMessages() {
        org.eclipse.osgi.util.NLS.initializeMessages(org.eclipse.mylyn.internal.tasks.ui.Messages.BUNDLE_NAME, org.eclipse.mylyn.internal.tasks.ui.Messages.class);
    }

    public static java.lang.String ActiveContextExternalizationParticipant_Active_Task_Context;

    public static java.lang.String ActivityExternalizationParticipant_Activity_Context;

    public static java.lang.String ChangeActivityHandleOperation_Activity_migration;

    public static java.lang.String DialogErrorReporter_Mylyn_Error;

    public static java.lang.String DialogErrorReporter_Please_report_the_following_error_at;

    public static java.lang.String DownloadAndOpenTaskAttachmentJob_cannotOpenEditor;

    public static java.lang.String DownloadAndOpenTaskAttachmentJob_editorTooltip;

    public static java.lang.String DownloadAndOpenTaskAttachmentJob_failedToDownloadAttachment;

    public static java.lang.String MoveToCategoryMenuContributor_Set_Category_Menu_Item;

    public static java.lang.String OpenRepositoryTaskJob_Could_not_find_repository_configuration_for_X;

    public static java.lang.String OpenRepositoryTaskJob_Opening_Remote_Task;

    public static java.lang.String OpenRepositoryTaskJob_Opening_repository_task_X;

    public static java.lang.String OpenRepositoryTaskJob_Please_set_up_repository_via_X;

    public static java.lang.String OpenRepositoryTaskJob_Repository_Not_Found;

    public static java.lang.String OpenRepositoryTaskJob_Unable_to_open_task;

    public static java.lang.String RefactorRepositoryUrlOperation_Repository_URL_update;

    public static java.lang.String ScheduleDatePicker_Clear;

    public static java.lang.String ScheduleTaskMenuContributor_Cannot_schedule_completed_tasks;

    public static java.lang.String ScheduleTaskMenuContributor_Choose_Date_;

    public static java.lang.String ScheduleTaskMenuContributor_Future;

    public static java.lang.String ScheduleTaskMenuContributor_Not_Scheduled;

    public static java.lang.String ScheduleTaskMenuContributor_Schedule_for;

    public static java.lang.String TaskAttachmentEditorViewer_Do_not_warn_me_again;

    public static java.lang.String TaskAttachmentEditorViewer_Open_Attachment;

    public static java.lang.String TaskAttachmentEditorViewer_openingAttachment;

    public static java.lang.String TaskAttachmentEditorViewer_Some_files_can_harm_your_computer;

    public static java.lang.String TaskAttachmentViewerBrowser_browser;

    public static java.lang.String TaskHistoryDropDown_Activate_Task_;

    public static java.lang.String TaskHistoryDropDown_Deactivate_Task;

    public static java.lang.String TaskHistoryDropDown_Open_Active_Task;

    public static java.lang.String TaskListBackupManager_Error_occured_during_scheduled_tasklist_backup;

    public static java.lang.String TaskListBackupManager_Scheduled_task_data_backup;

    public static java.lang.String TaskListNotificationManager_Open_Notification_Job;

    public static java.lang.String TaskRepositoryLocationUi_Enter_CLIENTCERTIFICATE_password;

    public static java.lang.String TaskRepositoryLocationUi_Enter_HTTP_password;

    public static java.lang.String TaskRepositoryLocationUi_Enter_proxy_password;

    public static java.lang.String TaskRepositoryLocationUi_Enter_repository_password;

    public static java.lang.String TaskSearchPage_ERROR_Unable_to_present_query_page;

    public static java.lang.String TaskSearchPage_no_available_repositories;

    public static java.lang.String TaskSearchPage_no_searchable_repositories;

    public static java.lang.String TaskSearchPage_No_task_found_matching_key_;

    public static java.lang.String TaskSearchPage_Repository_Search;

    public static java.lang.String TaskSearchPage_Repository;

    public static java.lang.String TaskSearchPage_Task_Key_ID;

    public static java.lang.String TaskSearchPage_Task_Search;

    public static java.lang.String TasksReminderDialog_Description;

    public static java.lang.String TasksReminderDialog_Dismiss_All;

    public static java.lang.String TasksReminderDialog_Dismiss_Selected;

    public static java.lang.String TasksReminderDialog_Priority;

    public static java.lang.String TasksReminderDialog_Remind_tommorrow;

    public static java.lang.String TasksReminderDialog_Reminder_Day;

    public static java.lang.String TasksReminderDialog_Reminders;

    public static java.lang.String TasksUiPlugin_Activate_Task;

    public static java.lang.String TasksUiPlugin_Hide_Irrelevant_Subtasks;

    public static java.lang.String TasksUiPlugin_Hide_Irrelevant_Subtasks_Message;

    public static java.lang.String TasksUiPlugin_Initializing_Task_List;

    public static java.lang.String TasksUiPlugin_Task_Repositories;

    public static java.lang.String TasksUiPlugin_Load_Data_Directory;

    public static java.lang.String TasksUiPlugin_New_Task;

    public static java.lang.String TaskTrimWidget__no_active_task_;

    public static java.lang.String TaskTrimWidget__no_task_active_;

    public static java.lang.String AbstractRepositoryConnectorUi_InReplyToComment;

    public static java.lang.String AbstractRepositoryConnectorUi_InReplyToDescription;

    public static java.lang.String AbstractRepositoryConnectorUi_InReplyToTaskAndComment;

    public static java.lang.String AbstractRepositoryConnectorUi_Task;

    public static java.lang.String TaskElementLabelProvider__no_summary_available_;

    public static java.lang.String TaskHyperlink_Could_not_determine_repository_for_report;

    public static java.lang.String TaskHyperlink_Open_Task_X_in_X;

    public static java.lang.String AbstractRetrieveTitleFromUrlJob_Retrieving_summary_from_URL;

    public static java.lang.String FileStorage_unableToReadAttachmentFile;
}