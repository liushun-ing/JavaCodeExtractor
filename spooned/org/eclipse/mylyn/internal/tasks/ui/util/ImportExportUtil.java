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
package org.eclipse.mylyn.internal.tasks.ui.util;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.commons.ui.PlatformUiUtil;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.core.TaskCategory;
import org.eclipse.mylyn.internal.tasks.core.TransferList;
import org.eclipse.mylyn.internal.tasks.core.externalization.TaskListExternalizer;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.FileDialog;
/**
 *
 * @author Steffen Pingel
 */
public class ImportExportUtil {
    public static void configureFilter(org.eclipse.swt.widgets.FileDialog dialog) {
        dialog.setFilterExtensions(org.eclipse.mylyn.commons.ui.PlatformUiUtil.getFilterExtensions(new java.lang.String[]{ "*" + org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.FILE_EXTENSION }));// $NON-NLS-1$

        dialog.setFilterNames(new java.lang.String[]{ org.eclipse.osgi.util.NLS.bind(Messages.ImportExportUtil_Tasks_and_queries_Filter0, org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.FILE_EXTENSION) });
    }

    public static void export(java.io.File file, org.eclipse.jface.viewers.IStructuredSelection selection) throws org.eclipse.core.runtime.CoreException {
        // extract queries and tasks from selection
        org.eclipse.mylyn.internal.tasks.core.TransferList list = new org.eclipse.mylyn.internal.tasks.core.TransferList();
        for (java.util.Iterator<?> it = selection.iterator(); it.hasNext();) {
            java.lang.Object element = it.next();
            if (element instanceof org.eclipse.mylyn.internal.tasks.core.TaskCategory) {
                list.addCategory(((org.eclipse.mylyn.internal.tasks.core.TaskCategory) (element)));
            } else if (element instanceof org.eclipse.mylyn.internal.tasks.core.RepositoryQuery) {
                list.addQuery(((org.eclipse.mylyn.internal.tasks.core.RepositoryQuery) (element)));
            } else if (element instanceof org.eclipse.mylyn.tasks.core.ITask) {
                list.addTask(((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (element)));
            }
        }
        org.eclipse.mylyn.internal.tasks.core.externalization.TaskListExternalizer externalizer = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().createTaskListExternalizer();
        externalizer.writeTaskList(list, file);
    }
}