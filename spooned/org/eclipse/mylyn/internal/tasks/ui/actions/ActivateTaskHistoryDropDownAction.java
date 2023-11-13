/**
 * *****************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
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
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.TaskActivationHistory;
import org.eclipse.mylyn.internal.tasks.ui.TaskHistoryDropDown;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;
/**
 *
 * @author Wesley Coelho
 * @author Mik Kersten
 * @author Leo Dos Santos
 * @author Steffen Pingel
 */
public class ActivateTaskHistoryDropDownAction extends org.eclipse.jface.action.Action implements org.eclipse.ui.IWorkbenchWindowPulldownDelegate , org.eclipse.jface.action.IMenuCreator {
    public static final java.lang.String ID = "org.eclipse.mylyn.tasklist.actions.navigate.previous";// $NON-NLS-1$


    private org.eclipse.swt.widgets.Menu dropDownMenu;

    private final org.eclipse.mylyn.internal.tasks.core.TaskActivationHistory taskHistory;

    private final org.eclipse.mylyn.internal.tasks.ui.TaskHistoryDropDown taskHistoryDropDown;

    public ActivateTaskHistoryDropDownAction() {
        this.taskHistory = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().getTaskActivationHistory();
        this.taskHistoryDropDown = new org.eclipse.mylyn.internal.tasks.ui.TaskHistoryDropDown(null, taskHistory);
        setText(Messages.ActivateTaskHistoryDropDownAction_Activate_Previous_Task);
        setToolTipText(Messages.ActivateTaskHistoryDropDownAction_Activate_Previous_Task);
        setId(org.eclipse.mylyn.internal.tasks.ui.actions.ActivateTaskHistoryDropDownAction.ID);
        setEnabled(true);
        setImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.CONTEXT_HISTORY_PREVIOUS);
    }

    public void dispose() {
        // ignore
    }

    public org.eclipse.swt.widgets.Menu getMenu(org.eclipse.swt.widgets.Control parent) {
        if (dropDownMenu != null) {
            dropDownMenu.dispose();
        }
        dropDownMenu = new org.eclipse.swt.widgets.Menu(parent);
        taskHistoryDropDown.fill(dropDownMenu, -1);
        return dropDownMenu;
    }

    public org.eclipse.swt.widgets.Menu getMenu(org.eclipse.swt.widgets.Menu parent) {
        if (dropDownMenu != null) {
            dropDownMenu.dispose();
        }
        dropDownMenu = new org.eclipse.swt.widgets.Menu(parent);
        taskHistoryDropDown.fill(dropDownMenu, -1);
        return dropDownMenu;
    }

    public void init(org.eclipse.ui.IWorkbenchWindow window) {
        // ignore
    }

    @java.lang.Override
    public void run() {
        if (taskHistory.hasPrevious()) {
            org.eclipse.mylyn.internal.tasks.core.AbstractTask previousTask = taskHistory.getPreviousTask();
            if ((previousTask != null) && (!previousTask.isActive())) {
                org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.activateTaskThroughCommand(previousTask);
                if (org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.getFromActivePerspective() != null) {
                    org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.getFromActivePerspective().refresh();
                }
            }
            setEnabled((taskHistory.getPreviousTasks() != null) && (taskHistory.getPreviousTasks().size() > 0));
        }
    }

    public void run(org.eclipse.jface.action.IAction action) {
        run();
    }

    public void selectionChanged(org.eclipse.jface.action.IAction action, org.eclipse.jface.viewers.ISelection selection) {
        // ignore
    }
}