/**
 * *****************************************************************************
 * Copyright (c) 2011, 2013 Tasktop Technologies and others.
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
 * @author Steffen Pingel
 */
public class ShowNonMatchingSubtasksAction extends org.eclipse.jface.action.Action {
    public ShowNonMatchingSubtasksAction() {
        setText(Messages.ShowNonMatchingSubtasksAction_Show_Non_Matching_Subtasks);
        setToolTipText(Messages.ShowNonMatchingSubtasksAction_Show_Non_Matching_Subtasks);
        setChecked(!org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.FILTER_NON_MATCHING));
    }

    @java.lang.Override
    public void run() {
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().setValue(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.FILTER_NON_MATCHING, !isChecked());
    }

    public void update() {
        setChecked(!org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.FILTER_NON_MATCHING));
    }
}