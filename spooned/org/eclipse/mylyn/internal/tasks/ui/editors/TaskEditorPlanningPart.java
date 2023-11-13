/**
 * *****************************************************************************
 * Copyright (c) 2004, 2012 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.editors;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport;
import org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
/**
 *
 * @author Shawn Minto
 */
public class TaskEditorPlanningPart extends org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart {
    private class NotesAction extends org.eclipse.jface.action.Action {
        public NotesAction() {
            setImageDescriptor(org.eclipse.mylyn.commons.ui.CommonImages.NOTES_SMALL);
            setToolTipText(Messages.TaskEditorPlanningPart_Add_Private_Notes_Tooltip);
        }

        @java.lang.Override
        public void run() {
            org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil.setExpanded(part.getSection(), true);
            if ((part.getNoteEditor() != null) && (part.getNoteEditor().getControl() != null)) {
                part.getNoteEditor().getControl().setFocus();
            } else {
                part.getControl().setFocus();
            }
        }
    }

    private final org.eclipse.mylyn.internal.tasks.ui.editors.PlanningPart part;

    public TaskEditorPlanningPart() {
        part = new org.eclipse.mylyn.internal.tasks.ui.editors.PlanningPart(org.eclipse.ui.forms.widgets.ExpandableComposite.TWISTIE) {
            @java.lang.Override
            protected void fillToolBar(org.eclipse.jface.action.ToolBarManager toolBarManager) {
                org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorPlanningPart.NotesAction notesAction = new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorPlanningPart.NotesAction();
                notesAction.setEnabled(needsNotes());
                toolBarManager.add(notesAction);
                toolBarManager.add(getMaximizePartAction());
            }
        };
    }

    @java.lang.Override
    public void initialize(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage taskEditorPage) {
        super.initialize(taskEditorPage);
        boolean needsDueDate = !taskEditorPage.getConnector().hasRepositoryDueDate(taskEditorPage.getTaskRepository(), taskEditorPage.getTask(), getTaskData());
        org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport textSupport = ((org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport) (getTaskEditorPage().getAdapter(org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport.class)));
        // disable notes for new tasks to avoid confusion due to showing multiple input fields
        part.initialize(taskEditorPage.getManagedForm(), taskEditorPage.getTaskRepository(), ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (taskEditorPage.getTask())), needsDueDate, taskEditorPage, textSupport);
        part.setNeedsNotes(!getModel().getTaskData().isNew());
        part.setAlwaysExpand(getModel().getTaskData().isNew());
    }

    @java.lang.Override
    public void createControl(org.eclipse.swt.widgets.Composite parent, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        part.createControl(parent, toolkit);
        part.getSection().setToolTipText(Messages.TaskEditorPlanningPart_TaskEditorPlanningPart_tooltip);
        setSection(toolkit, part.getSection());
    }

    @java.lang.Override
    protected org.eclipse.swt.widgets.Control getLayoutControl() {
        return part.getLayoutControl();
    }

    @java.lang.Override
    public void commit(boolean onSave) {
        super.commit(onSave);
        part.commit(onSave);
    }

    @java.lang.Override
    public boolean isDirty() {
        return super.isDirty() || part.isDirty();
    }

    @java.lang.Override
    public void dispose() {
        part.dispose();
    }

    public org.eclipse.mylyn.internal.tasks.ui.editors.PlanningPart getPlanningPart() {
        return part;
    }
}