/**
 * *****************************************************************************
 * Copyright (c) 2009, 2011 Tasktop Technologies and others.
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
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.commons.ui.SelectionProviderAdapter;
import org.eclipse.mylyn.commons.workbench.WorkbenchActionSupport;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
/**
 *
 * @author Steffen Pingel
 */
public class TaskEditorActionGroup extends org.eclipse.mylyn.internal.tasks.ui.actions.RepositoryElementActionGroup {
    private final org.eclipse.mylyn.commons.workbench.WorkbenchActionSupport actionSupport;

    private final org.eclipse.mylyn.internal.tasks.ui.actions.SynchronizeEditorAction synchronizeEditorAction = new org.eclipse.mylyn.internal.tasks.ui.actions.SynchronizeEditorAction();

    private final org.eclipse.mylyn.internal.tasks.ui.actions.NewTaskFromSelectionAction newTaskFromSelectionAction = new org.eclipse.mylyn.internal.tasks.ui.actions.NewTaskFromSelectionAction();

    public TaskEditorActionGroup(org.eclipse.mylyn.commons.workbench.WorkbenchActionSupport actionSupport) {
        this.actionSupport = actionSupport;
        synchronizeEditorAction.setActionDefinitionId("org.eclipse.ui.file.refresh");// $NON-NLS-1$

        synchronizeEditorAction.setEnabled(false);
    }

    public void fillContextMenu(org.eclipse.jface.action.IMenuManager manager, org.eclipse.mylyn.tasks.ui.editors.TaskEditor editor, boolean addClipboard) {
        org.eclipse.mylyn.tasks.core.ITask task = editor.getTaskEditorInput().getTask();
        org.eclipse.mylyn.commons.ui.SelectionProviderAdapter selectionProvider = new org.eclipse.mylyn.commons.ui.SelectionProviderAdapter();
        setSelectionProvider(selectionProvider);
        selectionProvider.setSelection(new org.eclipse.jface.viewers.StructuredSelection(task));
        super.fillContextMenu(manager);
        if (addClipboard) {
            addClipboardActions(manager);
        }
        synchronizeEditorAction.selectionChanged(new org.eclipse.jface.viewers.StructuredSelection(editor));
        org.eclipse.jface.viewers.IStructuredSelection selection = new org.eclipse.jface.viewers.StructuredSelection(task);
        actionSupport.updateActions(selection);
        newTaskFromSelectionAction.selectionChanged(selection);
        manager.add(new org.eclipse.jface.action.Separator());
        if (synchronizeEditorAction.isEnabled()) {
            manager.appendToGroup(org.eclipse.mylyn.internal.tasks.ui.actions.ID_SEPARATOR_REPOSITORY, synchronizeEditorAction);
        }
    }

    public void addClipboardActions(org.eclipse.jface.action.IMenuManager manager) {
        // manager.add(actionSupport.getUndoAction());
        // manager.add(actionSupport.getRedoAction());
        // manager.add(new Separator());
        manager.prependToGroup(org.eclipse.mylyn.internal.tasks.ui.actions.ID_SEPARATOR_EDIT, actionSupport.getCopyAction());
        manager.prependToGroup(org.eclipse.mylyn.internal.tasks.ui.actions.ID_SEPARATOR_EDIT, actionSupport.getCutAction());
        manager.appendToGroup(org.eclipse.mylyn.internal.tasks.ui.actions.ID_SEPARATOR_EDIT, actionSupport.getPasteAction());
        manager.appendToGroup(org.eclipse.mylyn.internal.tasks.ui.actions.ID_SEPARATOR_EDIT, actionSupport.getSelectAllAction());
        manager.appendToGroup(org.eclipse.mylyn.internal.tasks.ui.actions.ID_SEPARATOR_EDIT, newTaskFromSelectionAction);
    }

    public org.eclipse.mylyn.internal.tasks.ui.actions.SynchronizeEditorAction getSynchronizeEditorAction() {
        return synchronizeEditorAction;
    }

    public org.eclipse.mylyn.internal.tasks.ui.actions.NewTaskFromSelectionAction getNewTaskFromSelectionAction() {
        return newTaskFromSelectionAction;
    }
}