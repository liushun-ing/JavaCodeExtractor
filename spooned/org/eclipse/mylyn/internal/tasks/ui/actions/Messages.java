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
package org.eclipse.mylyn.internal.tasks.ui.actions;
import org.eclipse.osgi.util.NLS;
public class Messages extends org.eclipse.osgi.util.NLS {
    private static final java.lang.String BUNDLE_NAME = "org.eclipse.mylyn.internal.tasks.ui.actions.messages";// $NON-NLS-1$


    static {
        // load message values from bundle file
        reloadMessages();
    }

    public static void reloadMessages() {
        org.eclipse.osgi.util.NLS.initializeMessages(org.eclipse.mylyn.internal.tasks.ui.actions.Messages.BUNDLE_NAME, org.eclipse.mylyn.internal.tasks.ui.actions.Messages.class);
    }

    public static java.lang.String AbstractChangeCompletionAction_Mark_selected_local_tasks_X;

    public static java.lang.String ActivateTaskDialogAction_Activate_Task;

    public static java.lang.String ActivateTaskDialogAction_Select_a_task_to_activate__;

    public static java.lang.String ActivateTaskHistoryDropDownAction_Activate_Previous_Task;

    public static java.lang.String AddRepositoryAction_Add_new_query;

    public static java.lang.String AddRepositoryAction_Add_a_query_to_the_Task_List;

    public static java.lang.String AddRepositoryAction_Add_Task_Repository;

    public static java.lang.String AddRepositoryAction_Do_not_show_again;

    public static java.lang.String AutoUpdateQueryAction_Synchronize_Automatically_Label;

    public static java.lang.String ClearOutgoingAction_Clear_outgoing;

    public static java.lang.String ClearOutgoingAction_Clear_outgoing_failed;

    public static java.lang.String ClearOutgoingAction_Confirm_discard;

    public static java.lang.String ClearOutgoingAction_Discard_all_outgoing_changes_;

    public static java.lang.String CloneTaskAction_Clone_Label;

    public static java.lang.String CloneTaskAction_Clone_Task_Failed;

    public static java.lang.String CloneTaskAction_Cloned_from_;

    public static java.lang.String CollapseAllAction_Collapse_All;

    public static java.lang.String CompareAttachmentsAction_Compare__;

    public static java.lang.String CompareAttachmentsAction_Compare_Attachments;

    public static java.lang.String CompareAttachmentsAction_Failed_to_find_attachment;

    public static java.lang.String CopyCommentDetailsAction_Copy_User_ID;

    public static java.lang.String CopyCommentDetailsAction_Copy_User_ID_Tooltip;

    public static java.lang.String CopyCommentDetailsURL_Copy_Comment_URL;

    public static java.lang.String CopyCommentDetailsURL_Copy_Comment_URL_Tooltip;

    public static java.lang.String CopyCommenterNameAction_Copy_User_Name;

    public static java.lang.String CopyCommenterNameAction_Copy_User_Name_Tooltip;

    public static java.lang.String CopyTaskDetailsAction_ID_Menu_Label;

    public static java.lang.String CopyTaskDetailsAction_ID_Summary_and_Url_Menu_Label;

    public static java.lang.String CopyTaskDetailsAction_ID_Summary_Menu_Label;

    public static java.lang.String CopyTaskDetailsAction_Url_Menu_Label;

    public static java.lang.String DeleteAction_Also_delete_from_repository_X;

    public static java.lang.String DeleteAction_Delete_Tasks;

    public static java.lang.String DeleteAction_Delete;

    public static java.lang.String DeleteAction_Delete_all_of_the_unsubmitted_tasks;

    public static java.lang.String DeleteAction_Delete_elements_from_task_list_context_planning_deleted;

    public static java.lang.String DeleteAction_Delete_failed;

    public static java.lang.String DeleteAction_Delete_in_progress;

    public static java.lang.String DeleteAction_Delete_task_from_task_list_context_planning_deleted;

    public static java.lang.String DeleteAction_Delete_tasks_from_task_list_context_planning_deleted;

    public static java.lang.String DeleteAction_Delete_the_planning_information_and_context_of_all_unmatched_tasks;

    public static java.lang.String DeleteAction_Deleting_tasks_from_repositories;

    public static java.lang.String DeleteAction_Not_supported;

    public static java.lang.String DeleteAction_Nothing_selected;

    public static java.lang.String DeleteAction_Permanently_delete_from_task_list;

    public static java.lang.String DeleteAction_Permanently_delete_the_category;

    public static java.lang.String DeleteAction_Permanently_delete_the_element_listed_below;

    public static java.lang.String DeleteAction_Permanently_delete_the_query;

    public static java.lang.String DeleteTaskEditorAction_Delete_Task;

    public static java.lang.String DeleteTaskRepositoryAction_Confirm_Delete;

    public static java.lang.String DeleteTaskRepositoryAction_Delete_Specific_Task_Repository;

    public static java.lang.String DeleteTaskRepositoryAction_Delete_Repository;

    public static java.lang.String DeleteTaskRepositoryAction_Delete_the_selected_task_repositories;

    public static java.lang.String DeleteTaskRepositoryAction_Delete_Repository_In_Progress;

    public static java.lang.String DeleteTaskRepositoryAction_Delete_Task_Repository_Failed;

    public static java.lang.String EditRepositoryPropertiesAction_Properties;

    public static java.lang.String ExpandAllAction_Expand_All;

    public static java.lang.String ExportAction_Dialog_Title;

    public static java.lang.String ExportAction_Nothing_selected;

    public static java.lang.String ExportAction_Problems_encountered;

    public static java.lang.String ExportAction_X_exists_Do_you_wish_to_overwrite;

    public static java.lang.String FilterCompletedTasksAction_Filter_Completed_Tasks;

    public static java.lang.String FilterMyTasksAction_My_Tasks;

    public static java.lang.String GoIntoAction_Go_Into;

    public static java.lang.String GoUpAction_Go_Up_To_Root;

    public static java.lang.String GroupSubTasksAction_Group_Subtasks;

    public static java.lang.String HideQueryAction_Hidden_Label;

    public static java.lang.String ImportAction_Dialog_Title;

    public static java.lang.String ImportAction_Problems_encountered;

    public static java.lang.String LinkWithEditorAction_Link_with_Editor;

    public static java.lang.String NewCategoryAction_A_category_with_this_name_already_exists;

    public static java.lang.String NewCategoryAction_Enter_name;

    public static java.lang.String NewCategoryAction_Enter_a_name_for_the_Category;

    public static java.lang.String NewCategoryAction_New_Category;

    public static java.lang.String NewCategoryAction_New_Category_;

    public static java.lang.String NewCategoryAction_A_query_with_this_name_already_exists;

    public static java.lang.String NewQueryAction_new_query_;

    public static java.lang.String NewSubTaskAction_The_connector_does_not_support_creating_subtasks_for_this_task;

    public static java.lang.String NewSubTaskAction_Could_not_retrieve_task_data_for_task_;

    public static java.lang.String NewSubTaskAction_Create_a_new_subtask;

    public static java.lang.String NewSubTaskAction_Failed_to_create_new_sub_task_;

    public static java.lang.String NewSubTaskAction_Subtask;

    public static java.lang.String NewSubTaskAction_Unable_to_create_subtask;

    public static java.lang.String NewTaskAction_Add_Repository;

    public static java.lang.String NewTaskAction_new_task;

    public static java.lang.String NewTaskAction_Show_Wizard_Label;

    public static java.lang.String NewTaskFromSelectionAction_Comment_;

    public static java.lang.String NewTaskFromSelectionAction____Created_from_Comment___;

    public static java.lang.String NewTaskFromSelectionAction_New_Task_from_Selection;

    public static java.lang.String NewTaskFromSelectionAction_Nothing_selected_to_create_task_from;

    public static java.lang.String NewTaskFromSelectionAction_URL_;

    public static java.lang.String OpenRepositoryTask_Could_not_find_matching_repository_task;

    public static java.lang.String OpenRepositoryTask_Open_Repository_Task;

    public static java.lang.String OpenRepositoryTask_Open_Task;

    public static java.lang.String OpenTaskAction_Open_Task;

    public static java.lang.String OpenTaskAction_Select_a_task_to_open__;

    public static java.lang.String OpenTaskListElementAction_Open;

    public static java.lang.String OpenTaskListElementAction_Open_Task_List_Element;

    public static java.lang.String OpenTasksUiPreferencesAction_Preferences_;

    public static java.lang.String OpenWithBrowserAction_Open_with_Browser;

    public static java.lang.String PresentationDropDownSelectionAction_Task_Presentation;

    public static java.lang.String QueryCloneAction_Clone_Query;

    public static java.lang.String QueryCloneAction_Copy_of_X;

    public static java.lang.String QueryCloneAction_No_query_selected;

    public static java.lang.String RefreshRepositoryTasksAction_Refresh_All_Tasks;

    public static java.lang.String RemoveFromCategoryAction_Remove_From_Category;

    public static java.lang.String RenameAction_Rename;

    public static java.lang.String ShowAllQueriesAction_Show_All_Queries;

    public static java.lang.String ShowAllQueriesAction_Show_All_Queries_Including_Hidden_Queries;

    public static java.lang.String ShowInSearchViewAction_Open_in_Search_Label;

    public static java.lang.String ShowInTaskListAction_Show_In_Task_List;

    public static java.lang.String ShowNonMatchingSubtasksAction_Show_Non_Matching_Subtasks;

    public static java.lang.String SynchronizeAutomaticallyAction_Synchronize_Automatically;

    public static java.lang.String SynchronizeEditorAction_Synchronize;

    public static java.lang.String SynchronizeEditorAction_Synchronize_Incoming_Changes;

    public static java.lang.String TaskActivateAction_Activate;

    public static java.lang.String TaskDeactivateAction_Deactivate;

    public static java.lang.String TaskEditorScheduleAction_Private_Scheduling;

    public static java.lang.String TaskListSortAction_Sort_;

    public static java.lang.String TaskSelectionDialog__matches;

    public static java.lang.String TaskSelectionDialog_Deselect_Working_Set;

    public static java.lang.String TaskSelectionDialog_Edit_Active_Working_Set_;

    public static java.lang.String TaskSelectionDialog_New_Task_;

    public static java.lang.String TaskSelectionDialog_Open_with_Browser;

    public static java.lang.String TaskSelectionDialog_Random_Task;

    public static java.lang.String TaskSelectionDialog_Scanning_tasks;

    public static java.lang.String TaskSelectionDialog_Search_for_tasks;

    public static java.lang.String TaskSelectionDialog_Select_Working_Set_;

    public static java.lang.String TaskSelectionDialog_Selected_item_is_not_a_task;

    public static java.lang.String TaskSelectionDialog_Show_Completed_Tasks;

    public static java.lang.String TaskSelectionDialogWithRandom_Feeling_Lazy_Error;

    public static java.lang.String TaskSelectionDialogWithRandom_Feeling_Lazy_Error_Title;

    public static java.lang.String TaskSelectionDialogWithRandom_Feeling_Lazy_Tooltip;

    public static java.lang.String TaskWorkingSetAction_All;

    public static java.lang.String TaskWorkingSetAction_Deselect_All;

    public static java.lang.String TaskWorkingSetAction_Edit_Label;

    public static java.lang.String TaskWorkingSetAction_Select_and_Edit_Working_Sets;

    public static java.lang.String TaskWorkingSetAction_Sets;

    public static java.lang.String ToggleAllWorkingSetsAction_Show_All;

    public static java.lang.String ToggleTaskActivationAction_Activate_Task;

    public static java.lang.String ToggleTaskActivationAction_Deactivate_Task;
}