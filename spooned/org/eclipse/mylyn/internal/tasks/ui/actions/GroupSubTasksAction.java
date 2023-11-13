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
package org.eclipse.mylyn.internal.tasks.ui.actions;
import org.eclipse.jface.action.Action;
import org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
/**
 *
 * @author Rob Elves
 * @author Mik Kersten
 */
public class GroupSubTasksAction extends org.eclipse.jface.action.Action {
    public static final java.lang.String ID = "org.eclipse.mylyn.tasklist.actions.filter.subtasks";// $NON-NLS-1$


    public GroupSubTasksAction() {
        setText(Messages.GroupSubTasksAction_Group_Subtasks);
        setToolTipText(Messages.GroupSubTasksAction_Group_Subtasks);
        setId(org.eclipse.mylyn.internal.tasks.ui.actions.GroupSubTasksAction.ID);
        setChecked(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.GROUP_SUBTASKS));
    }

    @java.lang.Override
    public void run() {
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().setValue(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.GROUP_SUBTASKS, isChecked());
    }
}