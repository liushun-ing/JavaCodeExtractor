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
 *     Raphael Ackermann - bug 160315
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory;
import org.eclipse.mylyn.internal.tasks.core.TaskCategory;
import org.eclipse.mylyn.internal.tasks.core.UnmatchedTaskContainer;
import org.eclipse.mylyn.internal.tasks.ui.actions.NewCategoryAction;
import org.eclipse.mylyn.internal.tasks.ui.util.TaskContainerComparator;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
/**
 *
 * @author Mik Kersten
 */
public class MoveToCategoryMenuContributor implements org.eclipse.mylyn.internal.tasks.ui.IDynamicSubMenuContributor {
    public org.eclipse.jface.action.MenuManager getSubMenuManager(final java.util.List<org.eclipse.mylyn.tasks.core.IRepositoryElement> selectedElements) {
        final org.eclipse.jface.action.MenuManager subMenuManager = new org.eclipse.jface.action.MenuManager(Messages.MoveToCategoryMenuContributor_Set_Category_Menu_Item);
        // Compute selected tasks
        java.util.List<org.eclipse.mylyn.internal.tasks.core.AbstractTask> selectedTasks = new java.util.ArrayList<org.eclipse.mylyn.internal.tasks.core.AbstractTask>(selectedElements.size());
        for (org.eclipse.mylyn.tasks.core.IRepositoryElement element : selectedElements) {
            if (element instanceof org.eclipse.mylyn.tasks.core.ITask) {
                selectedTasks.add(((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (element)));
            }
        }
        subMenuManager.setVisible(!selectedTasks.isEmpty());
        java.util.List<org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory> categories = new java.util.ArrayList<org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory>(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().getCategories());
        java.util.Collections.sort(categories, new org.eclipse.mylyn.internal.tasks.ui.util.TaskContainerComparator());
        for (final org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory category : categories) {
            if (!(category instanceof org.eclipse.mylyn.internal.tasks.core.UnmatchedTaskContainer)) {
                java.lang.String text = handleAcceleratorKeys(category.getSummary());
                org.eclipse.jface.action.Action action = new org.eclipse.jface.action.Action(text, org.eclipse.jface.action.IAction.AS_RADIO_BUTTON) {
                    @java.lang.Override
                    public void run() {
                        moveToCategory(selectedElements, category);
                    }
                };
                action.setImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.CATEGORY);
                if (selectedTasks.size() == 1) {
                    if (category.contains(selectedTasks.get(0).getHandleIdentifier())) {
                        action.setChecked(true);
                    }
                }
                subMenuManager.add(action);
            }
        }
        // add New Category action at the end of the Move to Category Submenu
        // and move selected actions to this newly created category
        org.eclipse.jface.action.Action action = new org.eclipse.mylyn.internal.tasks.ui.actions.NewCategoryAction() {
            @java.lang.Override
            public void run() {
                org.eclipse.mylyn.internal.tasks.core.TaskCategory category = createCategory();
                if (category != null) {
                    moveToCategory(selectedElements, category);
                }
            }
        };
        subMenuManager.add(new org.eclipse.jface.action.Separator());
        subMenuManager.add(action);
        return subMenuManager;
    }

    /**
     * public for testing Deals with text where user has entered a '@' or tab character but which are not meant to be
     * accelerators. from: Action#setText: Note that if you want to insert a '@' character into the text (but no
     * accelerator, you can simply insert a '@' or a tab at the end of the text. see Action#setText
     */
    public java.lang.String handleAcceleratorKeys(java.lang.String text) {
        if (text == null) {
            return null;
        }
        int index = text.lastIndexOf('\t');
        if (index == (-1)) {
            index = text.lastIndexOf('@');
        }
        if (index >= 0) {
            return text.concat("@");// $NON-NLS-1$

        }
        return text;
    }

    /**
     *
     * @param selectedElements
     * @param category
     */
    private void moveToCategory(final java.util.List<org.eclipse.mylyn.tasks.core.IRepositoryElement> selectedElements, org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory category) {
        for (org.eclipse.mylyn.tasks.core.IRepositoryElement element : selectedElements) {
            if (element instanceof org.eclipse.mylyn.tasks.core.ITask) {
                org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().addTask(((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (element)), category);
            }
        }
    }
}