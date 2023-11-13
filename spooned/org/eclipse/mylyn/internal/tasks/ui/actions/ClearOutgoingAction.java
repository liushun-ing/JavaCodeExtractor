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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITask.SynchronizationState;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
/**
 * Discard outgoing changes on selected task TODO: Enable multi task discard?
 *
 * @author Rob Elves
 */
public class ClearOutgoingAction extends org.eclipse.jface.action.Action {
    public static final java.lang.String ID = "org.eclipse.mylyn.tasklist.actions.mark.discard";// $NON-NLS-1$


    private final java.util.List<org.eclipse.mylyn.tasks.core.IRepositoryElement> selectedElements;

    private org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage taskEditorPage;

    public ClearOutgoingAction(java.util.List<org.eclipse.mylyn.tasks.core.IRepositoryElement> selectedElements) {
        this.selectedElements = selectedElements;
        setText(Messages.ClearOutgoingAction_Clear_outgoing);
        setToolTipText(Messages.ClearOutgoingAction_Clear_outgoing);
        setImageDescriptor(org.eclipse.mylyn.commons.ui.CommonImages.CLEAR);
        setId(org.eclipse.mylyn.internal.tasks.ui.actions.ClearOutgoingAction.ID);
        if ((selectedElements.size() == 1) && (selectedElements.get(0) instanceof org.eclipse.mylyn.tasks.core.ITask)) {
            org.eclipse.mylyn.tasks.core.ITask task = ((org.eclipse.mylyn.tasks.core.ITask) (selectedElements.get(0)));
            setEnabled(org.eclipse.mylyn.internal.tasks.ui.actions.ClearOutgoingAction.hasOutgoingChanges(task));
        } else {
            setEnabled(false);
        }
    }

    public org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage getTaskEditorPage() {
        return taskEditorPage;
    }

    public void setTaskEditorPage(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage taskEditorPage) {
        this.taskEditorPage = taskEditorPage;
    }

    public static boolean hasOutgoingChanges(org.eclipse.mylyn.tasks.core.ITask task) {
        return task.getSynchronizationState().equals(org.eclipse.mylyn.tasks.core.ITask.SynchronizationState.OUTGOING) || task.getSynchronizationState().equals(org.eclipse.mylyn.tasks.core.ITask.SynchronizationState.CONFLICT);
    }

    @java.lang.Override
    public void run() {
        java.util.ArrayList<org.eclipse.mylyn.internal.tasks.core.AbstractTask> toClear = new java.util.ArrayList<org.eclipse.mylyn.internal.tasks.core.AbstractTask>();
        for (java.lang.Object selectedObject : selectedElements) {
            if ((selectedObject instanceof org.eclipse.mylyn.tasks.core.ITask) && org.eclipse.mylyn.internal.tasks.ui.actions.ClearOutgoingAction.hasOutgoingChanges(((org.eclipse.mylyn.tasks.core.ITask) (selectedObject)))) {
                toClear.add(((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (selectedObject)));
            }
        }
        if (toClear.size() > 0) {
            org.eclipse.mylyn.internal.tasks.core.AbstractTask task = toClear.get(0);
            boolean confirm = org.eclipse.jface.dialogs.MessageDialog.openConfirm(null, Messages.ClearOutgoingAction_Confirm_discard, (Messages.ClearOutgoingAction_Discard_all_outgoing_changes_ + "\n\n")// $NON-NLS-1$
             + task.getSummary());
            if (confirm) {
                if (taskEditorPage != null) {
                    taskEditorPage.doSave(null);
                }
                try {
                    org.eclipse.mylyn.tasks.ui.TasksUi.getTaskDataManager().discardEdits(task);
                } catch (org.eclipse.core.runtime.CoreException e) {
                    org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayStatus(Messages.ClearOutgoingAction_Clear_outgoing_failed, e.getStatus());
                }
            }
        }
    }
}