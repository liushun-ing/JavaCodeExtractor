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
package org.eclipse.mylyn.internal.tasks.ui.views;
import org.eclipse.osgi.util.NLS;
public class Messages extends org.eclipse.osgi.util.NLS {
    private static final java.lang.String BUNDLE_NAME = "org.eclipse.mylyn.internal.tasks.ui.views.messages";// $NON-NLS-1$


    static {
        // load message values from bundle file
        reloadMessages();
    }

    public static void reloadMessages() {
        org.eclipse.osgi.util.NLS.initializeMessages(org.eclipse.mylyn.internal.tasks.ui.views.Messages.BUNDLE_NAME, org.eclipse.mylyn.internal.tasks.ui.views.Messages.class);
    }

    public static java.lang.String DisconnectRepositoryAction_Disconnected;

    public static java.lang.String PriorityDropDownAction_Filter_Priority_Lower_Than;

    public static java.lang.String RepositoryElementActionGroup_Copy_Detail_Menu_Label;

    public static java.lang.String RepositoryElementActionGroup_New;

    public static java.lang.String TaskListDropAdapter__retrieving_from_URL_;

    public static java.lang.String TaskListFilteredTree_Activate;

    public static java.lang.String TaskListFilteredTree_Edit_Task_Working_Sets_;

    public static java.lang.String TaskListFilteredTree_Estimated_hours;

    public static java.lang.String TaskListFilteredTree__multiple_;

    public static java.lang.String TaskListFilteredTree_Scheduled_tasks;

    public static java.lang.String TaskListFilteredTree_Search_repository_for_key_or_summary_;

    public static java.lang.String TaskListFilteredTree_Select_Active_Task;

    public static java.lang.String TaskListFilteredTree_Select_Working_Set;

    public static java.lang.String TaskListFilteredTree_Show_Tasks_UI_Legend;

    public static java.lang.String TaskListFilteredTree_Workweek_Progress;

    public static java.lang.String TaskListSorter_Catagory_and_Query;

    public static java.lang.String TaskListSorter_Catagory_and_Repository;

    public static java.lang.String TaskListSorter_No_Grouping;

    public static java.lang.String TaskListToolTip_Active_X;

    public static java.lang.String TaskListToolTip_Assigned_to_X;

    public static java.lang.String TaskListToolTip_Automatic_container_for_all_local_tasks;

    public static java.lang.String TaskListToolTip_Automatic_container_for_repository_tasks;

    public static java.lang.String TaskListToolTip_Due;

    public static java.lang.String TaskListToolTip_Estimate;

    public static java.lang.String TaskListToolTip_Please_synchronize_manually_for_full_error_message;

    public static java.lang.String TaskListToolTip_Scheduled;

    public static java.lang.String TaskListToolTip_Some_incoming_elements_may_be_filtered;

    public static java.lang.String TaskListToolTip_Synchronized;

    public static java.lang.String TaskListToolTip_Total_Complete_Incomplete;

    public static java.lang.String TaskListToolTip_Incoming_Outgoing;

    public static java.lang.String TaskListToolTip_Unassigned;

    public static java.lang.String TaskListView_Mylyn_context_capture_paused;

    public static java.lang.String TaskListView__paused_;

    public static java.lang.String TaskListView_Advanced_Filters_Label;

    public static java.lang.String TaskListView_Repository;

    public static java.lang.String TaskListView_Summary;

    public static java.lang.String TaskListView_Task_List;

    public static java.lang.String TaskListView_Welcome_Message;

    public static java.lang.String TaskListView_Welcome_Message_Title;

    public static java.lang.String TaskScheduleContentProvider_Completed;

    public static java.lang.String TaskScheduleContentProvider_Future;

    public static java.lang.String TaskScheduleContentProvider_Incoming;

    public static java.lang.String TaskScheduleContentProvider_Outgoing;

    public static java.lang.String TaskScheduleContentProvider_Two_Weeks;

    public static java.lang.String TaskScheduleContentProvider_Unscheduled;

    public static java.lang.String UpdateRepositoryConfigurationAction_Update_Repository_Configuration;
}