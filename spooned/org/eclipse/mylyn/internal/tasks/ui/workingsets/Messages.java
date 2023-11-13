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
package org.eclipse.mylyn.internal.tasks.ui.workingsets;
import org.eclipse.osgi.util.NLS;
public class Messages extends org.eclipse.osgi.util.NLS {
    private static final java.lang.String BUNDLE_NAME = "org.eclipse.mylyn.internal.tasks.ui.workingsets.messages";// $NON-NLS-1$


    static {
        // load message values from bundle file
        reloadMessages();
    }

    public static void reloadMessages() {
        org.eclipse.osgi.util.NLS.initializeMessages(org.eclipse.mylyn.internal.tasks.ui.workingsets.Messages.BUNDLE_NAME, org.eclipse.mylyn.internal.tasks.ui.workingsets.Messages.class);
    }

    public static java.lang.String TaskWorkingSetPage_The_name_must_not_be_empty;

    public static java.lang.String TaskWorkingSetPage_The_name_must_not_have_a_leading_or_trailing_whitespace;

    public static java.lang.String TaskWorkingSetPage_No_categories_queries_selected;

    public static java.lang.String TaskWorkingSetPage_Resources;

    public static java.lang.String TaskWorkingSetPage_Select_Working_Set_Elements;

    public static java.lang.String TaskWorkingSetPage_Tasks;

    public static java.lang.String TaskWorkingSetPage_Page_Description;

    public static java.lang.String TaskWorkingSetPage_A_working_set_with_the_same_name_already_exists;

    public static java.lang.String TaskWorkingSetPage_Select_All;

    public static java.lang.String TaskWorkingSetPage_Deselect_All;

    public static java.lang.String TaskWorkingSetPage_Working_set_name;

    public static java.lang.String TaskWorkingSetPage_Working_set_contents;
}