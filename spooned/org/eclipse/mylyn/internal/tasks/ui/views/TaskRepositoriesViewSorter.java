/**
 * *****************************************************************************
 * Copyright (c) 2010 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.views;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylyn.internal.tasks.core.Category;
import org.eclipse.mylyn.internal.tasks.core.IRepositoryConstants;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
/**
 *
 * @author Robert Elves
 */
public class TaskRepositoriesViewSorter extends org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoriesSorter {
    @java.lang.Override
    public int compare(org.eclipse.jface.viewers.Viewer viewer, java.lang.Object e1, java.lang.Object e2) {
        if ((e1 instanceof org.eclipse.mylyn.internal.tasks.core.Category) && (e2 instanceof org.eclipse.mylyn.internal.tasks.core.Category)) {
            return ((org.eclipse.mylyn.internal.tasks.core.Category) (e1)).compareTo(e2);
        }
        if ((e1 instanceof org.eclipse.mylyn.internal.tasks.core.Category) && (e2 instanceof org.eclipse.mylyn.tasks.core.TaskRepository)) {
            org.eclipse.mylyn.internal.tasks.core.Category cat1 = ((org.eclipse.mylyn.internal.tasks.core.Category) (e1));
            java.lang.String categoryId = ((org.eclipse.mylyn.tasks.core.TaskRepository) (e2)).getProperty(org.eclipse.mylyn.internal.tasks.core.IRepositoryConstants.PROPERTY_CATEGORY);
            org.eclipse.mylyn.internal.tasks.core.Category cat2 = ((org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager) (org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager())).getCategory(categoryId);
            return cat1.compareTo(cat2);
        } else if ((e1 instanceof org.eclipse.mylyn.tasks.core.TaskRepository) && (e2 instanceof org.eclipse.mylyn.internal.tasks.core.Category)) {
            org.eclipse.mylyn.internal.tasks.core.Category cat1 = ((org.eclipse.mylyn.internal.tasks.core.Category) (e2));
            java.lang.String categoryId = ((org.eclipse.mylyn.tasks.core.TaskRepository) (e1)).getProperty(org.eclipse.mylyn.internal.tasks.core.IRepositoryConstants.PROPERTY_CATEGORY);
            org.eclipse.mylyn.internal.tasks.core.Category cat2 = ((org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager) (org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager())).getCategory(categoryId);
            int result = cat2.compareTo(cat1);
            if (result == 0) {
                result = 1;
            }
            return result;
        } else if ((e1 instanceof org.eclipse.mylyn.tasks.core.TaskRepository) && (e2 instanceof org.eclipse.mylyn.tasks.core.TaskRepository)) {
            java.lang.String categoryId = ((org.eclipse.mylyn.tasks.core.TaskRepository) (e1)).getProperty(org.eclipse.mylyn.internal.tasks.core.IRepositoryConstants.PROPERTY_CATEGORY);
            org.eclipse.mylyn.internal.tasks.core.Category cat1 = ((org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager) (org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager())).getCategory(categoryId);
            java.lang.String categoryId2 = ((org.eclipse.mylyn.tasks.core.TaskRepository) (e2)).getProperty(org.eclipse.mylyn.internal.tasks.core.IRepositoryConstants.PROPERTY_CATEGORY);
            org.eclipse.mylyn.internal.tasks.core.Category cat2 = ((org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager) (org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager())).getCategory(categoryId2);
            int result = cat1.compareTo(cat2);
            if (result == 0) {
                return super.compare(viewer, e1, e2);
            } else {
                return result;
            }
        }
        return super.compare(viewer, e1, e2);
    }
}