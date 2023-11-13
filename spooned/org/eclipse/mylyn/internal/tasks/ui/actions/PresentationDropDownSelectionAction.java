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
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.Separator;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListPresentation;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
/**
 *
 * @author Rob Elves
 * @author Steffen Pingel
 */
public class PresentationDropDownSelectionAction extends org.eclipse.jface.action.Action implements org.eclipse.jface.action.IMenuCreator {
    public static final java.lang.String ID = "org.eclipse.mylyn.tasklist.actions.presentationselection";// $NON-NLS-1$


    private final org.eclipse.mylyn.internal.tasks.ui.views.TaskListView view;

    private org.eclipse.swt.widgets.Menu dropDownMenu;

    public PresentationDropDownSelectionAction(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView view) {
        this.view = view;
        setMenuCreator(this);
        setText(Messages.PresentationDropDownSelectionAction_Task_Presentation);
        setToolTipText(Messages.PresentationDropDownSelectionAction_Task_Presentation);
        setId(org.eclipse.mylyn.internal.tasks.ui.actions.PresentationDropDownSelectionAction.ID);
        setEnabled(true);
        setImageDescriptor(org.eclipse.mylyn.commons.ui.CommonImages.PRESENTATION);
    }

    private void addActionsToMenu() {
        for (org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListPresentation presentation : org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.getPresentations()) {
            if (presentation.isPrimary()) {
                org.eclipse.mylyn.internal.tasks.ui.actions.PresentationDropDownSelectionAction.PresentationSelectionAction action = new org.eclipse.mylyn.internal.tasks.ui.actions.PresentationDropDownSelectionAction.PresentationSelectionAction(view, presentation);
                org.eclipse.jface.action.ActionContributionItem item = new org.eclipse.jface.action.ActionContributionItem(action);
                item.fill(dropDownMenu, -1);
            }
        }
        boolean separatorAdded = false;
        for (org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListPresentation presentation : org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.getPresentations()) {
            if (org.eclipse.mylyn.commons.workbench.WorkbenchUtil.allowUseOf(presentation)) {
                if (!presentation.isPrimary()) {
                    if (!separatorAdded) {
                        new org.eclipse.jface.action.Separator().fill(dropDownMenu, -1);
                        separatorAdded = true;
                    }
                    org.eclipse.mylyn.internal.tasks.ui.actions.PresentationDropDownSelectionAction.PresentationSelectionAction action = new org.eclipse.mylyn.internal.tasks.ui.actions.PresentationDropDownSelectionAction.PresentationSelectionAction(view, presentation);
                    org.eclipse.jface.action.ActionContributionItem item = new org.eclipse.jface.action.ActionContributionItem(action);
                    item.fill(dropDownMenu, -1);
                }
            }
        }
    }

    @java.lang.Override
    public void run() {
        org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListPresentation current = view.getCurrentPresentation();
        java.util.List<org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListPresentation> all = org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.getPresentations();
        int size = all.size();
        if (size == 0) {
            return;
        }
        // cycle between primary presentations
        int index = all.indexOf(current) + 1;
        for (int i = 0; i < size; i++) {
            org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListPresentation presentation = all.get(index % size);
            if (presentation.isPrimary()) {
                view.applyPresentation(presentation);
                return;
            }
            index++;
        }
        // fall back to next presentation in list
        index = all.indexOf(current) + 1;
        if (index < size) {
            view.applyPresentation(all.get(index));
        } else {
            view.applyPresentation(all.get(0));
        }
    }

    public void dispose() {
        if (dropDownMenu != null) {
            dropDownMenu.dispose();
            dropDownMenu = null;
        }
    }

    public org.eclipse.swt.widgets.Menu getMenu(org.eclipse.swt.widgets.Control parent) {
        if (dropDownMenu != null) {
            dropDownMenu.dispose();
        }
        dropDownMenu = new org.eclipse.swt.widgets.Menu(parent);
        addActionsToMenu();
        return dropDownMenu;
    }

    public org.eclipse.swt.widgets.Menu getMenu(org.eclipse.swt.widgets.Menu parent) {
        if (dropDownMenu != null) {
            dropDownMenu.dispose();
        }
        dropDownMenu = new org.eclipse.swt.widgets.Menu(parent);
        addActionsToMenu();
        return dropDownMenu;
    }

    public static class PresentationSelectionAction extends org.eclipse.jface.action.Action {
        private final org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListPresentation presentation;

        private final org.eclipse.mylyn.internal.tasks.ui.views.TaskListView view;

        public PresentationSelectionAction(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView view, org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListPresentation presentation) {
            this.view = view;
            this.presentation = presentation;
            setText(presentation.getName());
            setImageDescriptor(presentation.getImageDescriptor());
            update();
        }

        public void update() {
            setChecked(view.getCurrentPresentation().getId().equals(presentation.getId()));
        }

        @java.lang.Override
        public void run() {
            view.applyPresentation(presentation);
        }
    }
}