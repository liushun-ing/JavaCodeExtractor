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
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.core.TaskCategory;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
/**
 *
 * @author Mik Kersten
 */
public class NewCategoryAction extends org.eclipse.jface.action.Action implements org.eclipse.ui.IViewActionDelegate {
    public static final java.lang.String ID = "org.eclipse.mylyn.tasks.ui.actions.create.category";// $NON-NLS-1$


    public NewCategoryAction() {
        setText(Messages.NewCategoryAction_New_Category_);
        setToolTipText(Messages.NewCategoryAction_New_Category_);
        setId(org.eclipse.mylyn.internal.tasks.ui.actions.NewCategoryAction.ID);
        setImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.CATEGORY_NEW);
    }

    public void init(org.eclipse.ui.IViewPart view) {
    }

    public void run(org.eclipse.jface.action.IAction action) {
        run();
    }

    @java.lang.Override
    public void run() {
        createCategory();
    }

    public org.eclipse.mylyn.internal.tasks.core.TaskCategory createCategory() {
        org.eclipse.jface.dialogs.InputDialog dialog = new org.eclipse.jface.dialogs.InputDialog(org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages.NewCategoryAction_Enter_name, Messages.NewCategoryAction_Enter_a_name_for_the_Category, "", null);// $NON-NLS-1$

        int dialogResult = dialog.open();
        if (dialogResult == org.eclipse.jface.window.Window.OK) {
            java.lang.String name = dialog.getValue();
            java.util.Set<org.eclipse.mylyn.internal.tasks.core.RepositoryQuery> queries = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().getQueries();
            java.util.Set<org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory> categories = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().getCategories();
            for (org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory category : categories) {
                if ((name != null) && name.equals(category.getSummary())) {
                    org.eclipse.jface.dialogs.MessageDialog.openInformation(org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages.NewCategoryAction_New_Category, Messages.NewCategoryAction_A_category_with_this_name_already_exists);
                    return null;
                }
            }
            for (org.eclipse.mylyn.internal.tasks.core.RepositoryQuery query : queries) {
                if ((name != null) && name.equals(query.getSummary())) {
                    org.eclipse.jface.dialogs.MessageDialog.openInformation(org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages.NewCategoryAction_New_Category, Messages.NewCategoryAction_A_query_with_this_name_already_exists);
                    return null;
                }
            }
            org.eclipse.mylyn.internal.tasks.core.TaskCategory category = new org.eclipse.mylyn.internal.tasks.core.TaskCategory(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().getUniqueHandleIdentifier(), name);
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().addCategory(category);
            return category;
        }
        return null;
    }

    public void selectionChanged(org.eclipse.jface.action.IAction action, org.eclipse.jface.viewers.ISelection selection) {
    }
}