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
package org.eclipse.mylyn.internal.tasks.ui.commands;
import org.eclipse.osgi.util.NLS;
public class Messages extends org.eclipse.osgi.util.NLS {
    private static final java.lang.String BUNDLE_NAME = "org.eclipse.mylyn.internal.tasks.ui.commands.messages";// $NON-NLS-1$


    static {
        // load message values from bundle file
        reloadMessages();
    }

    public static void reloadMessages() {
        org.eclipse.osgi.util.NLS.initializeMessages(org.eclipse.mylyn.internal.tasks.ui.commands.Messages.BUNDLE_NAME, org.eclipse.mylyn.internal.tasks.ui.commands.Messages.class);
    }

    public static java.lang.String NewLocalTaskHandler_Could_not_create_local_task;

    public static java.lang.String OpenTaskAttachmentHandler_failedToOpenViewer;

    public static java.lang.String OpenTaskAttachmentHandler_noAttachmentViewerFound;

    public static java.lang.String RemoteTaskSelectionDialog_Add_;

    public static java.lang.String RemoteTaskSelectionDialog_Add_to_Task_List_category;

    public static java.lang.String RemoteTaskSelectionDialog_Enter_Key_ID__use_comma_for_multiple_;

    public static java.lang.String RemoteTaskSelectionDialog_Enter_a_valid_task_ID;

    public static java.lang.String RemoteTaskSelectionDialog_Matching_tasks;

    public static java.lang.String RemoteTaskSelectionDialog_Select_a_task_or_repository;

    public static java.lang.String RemoteTaskSelectionDialog_Select_a_task_repository;

    public static java.lang.String ShowTasksConnectorDiscoveryWizardCommandHandler_Install_Connectors;

    public static java.lang.String ShowTasksConnectorDiscoveryWizardCommandHandler_Notify_when_updates_are_available_Text;

    public static java.lang.String ShowTasksConnectorDiscoveryWizardCommandHandler_Unable_to_launch_connector_install;

    public static java.lang.String MarkTaskHandler_MarkTasksReadOperation;

    public static java.lang.String MarkTaskHandler_MarkTasksUnreadOperation;
}