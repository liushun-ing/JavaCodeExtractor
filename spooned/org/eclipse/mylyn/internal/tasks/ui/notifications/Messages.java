/**
 * *****************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.notifications;
import org.eclipse.osgi.util.NLS;
public class Messages extends org.eclipse.osgi.util.NLS {
    private static final java.lang.String BUNDLE_NAME = "org.eclipse.mylyn.internal.tasks.ui.notifications.messages";// $NON-NLS-1$


    static {
        // load message values from bundle file
        reloadMessages();
    }

    public static void reloadMessages() {
        org.eclipse.osgi.util.NLS.initializeMessages(org.eclipse.mylyn.internal.tasks.ui.notifications.Messages.BUNDLE_NAME, org.eclipse.mylyn.internal.tasks.ui.notifications.Messages.class);
    }

    public static java.lang.String TaskDataDiff_more_;

    public static java.lang.String TaskDiffUtil_attachment;

    public static java.lang.String TaskDiffUtil_Comment_by_X;

    public static java.lang.String TaskDiffUtil_Unknown;

    public static java.lang.String TaskListNotificationPopup_Mark_Task_Read;

    public static java.lang.String TaskListNotificationPopup_more;

    public static java.lang.String TaskListNotifier_New_unread_task;
}