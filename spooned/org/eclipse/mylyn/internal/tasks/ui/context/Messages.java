/**
 * *****************************************************************************
 * Copyright (c) 2004, 2011 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.context;
import org.eclipse.osgi.util.NLS;
public class Messages extends org.eclipse.osgi.util.NLS {
    private static final java.lang.String BUNDLE_NAME = "org.eclipse.mylyn.internal.tasks.ui.context.messages";// $NON-NLS-1$


    static {
        // load message values from bundle file
        reloadMessages();
    }

    public static void reloadMessages() {
        org.eclipse.osgi.util.NLS.initializeMessages(org.eclipse.mylyn.internal.tasks.ui.context.Messages.BUNDLE_NAME, org.eclipse.mylyn.internal.tasks.ui.context.Messages.class);
    }

    public static java.lang.String ContextAttachWizard_Attach_Context;

    public static java.lang.String ContextAttachWizardPage_Enter_comment;

    public static java.lang.String ContextAttachWizardPage_Attaches_local_context_to_repository_task;

    public static java.lang.String ContextAttachWizardPage_Comment_;

    public static java.lang.String ContextAttachWizardPage_Repository_;

    public static java.lang.String ContextAttachWizardPage_Task;

    public static java.lang.String ContextRetrieveWizard_Retrieve_Context;

    public static java.lang.String ContextRetrieveWizardPage_Author;

    public static java.lang.String ContextRetrieveWizardPage_Date;

    public static java.lang.String ContextRetrieveWizardPage_Description;

    public static java.lang.String ContextRetrieveWizardPage_Select_context;

    public static java.lang.String ContextRetrieveWizardPage_SELECT_A_CONTEXT_TO_RETTRIEVE_FROM_TABLE_BELOW;

    public static java.lang.String ContextRetrieveWizardPage_Task;

    public static java.lang.String RetrieveLatestContextDialog_Dialog_Title;

    public static java.lang.String RetrieveLatestContextDialog_No_local_context_exists;

    public static java.lang.String RetrieveLatestContextDialog_Show_All_Contexts_Label;

    public static java.lang.String RetrieveLatestContextDialog_Unknown;

    public static java.lang.String ClearContextHandler_CLEAR_THE_CONTEXT_THE_FOR_SELECTED_TASK;

    public static java.lang.String ClearContextHandler_Confirm_clear_context;

    public static java.lang.String CopyContextHandler_Merge;

    public static java.lang.String CopyContextHandler_Copy_Context;

    public static java.lang.String CopyContextHandler_No_source_task_selected;

    public static java.lang.String CopyContextHandler_No_target_task_selected;

    public static java.lang.String CopyContextHandler_Replace;

    public static java.lang.String CopyContextHandler_Select_Target_Task;

    public static java.lang.String CopyContextHandler_Select_the_target_task__;

    public static java.lang.String CopyContextHandler_SELECTED_TASK_ALREADY_HAS_CONTEXT;

    public static java.lang.String CopyContextHandler_SOURCE_TASK_DOES_HAVE_A_CONTEXT;

    public static java.lang.String CopyContextHandler_TARGET_TASK_CON_NOT_BE_THE_SAME_AS_SOURCE_TASK;
}