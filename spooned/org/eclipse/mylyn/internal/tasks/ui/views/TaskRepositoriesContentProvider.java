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
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylyn.internal.tasks.core.Category;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
/**
 *
 * @author Robert Elves
 */
public class TaskRepositoriesContentProvider implements org.eclipse.jface.viewers.ITreeContentProvider {
    private final org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager manager;

    public TaskRepositoriesContentProvider() {
        manager = ((org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager) (org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager()));
    }

    public void inputChanged(org.eclipse.jface.viewers.Viewer v, java.lang.Object oldInput, java.lang.Object newInput) {
    }

    public void dispose() {
    }

    public java.lang.Object[] getElements(java.lang.Object parent) {
        java.util.Set<java.lang.Object> objects = new java.util.HashSet<java.lang.Object>();
        objects.addAll(manager.getCategories());
        return objects.toArray();
    }

    public java.lang.Object[] getChildren(java.lang.Object parentElement) {
        if (parentElement instanceof org.eclipse.mylyn.internal.tasks.core.Category) {
            java.util.Set<java.lang.Object> objects = new java.util.HashSet<java.lang.Object>();
            for (org.eclipse.mylyn.tasks.core.TaskRepository repository : org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getAllRepositories()) {
                org.eclipse.mylyn.internal.tasks.core.Category cat = manager.getCategory(repository);
                if (cat.equals(parentElement)) {
                    objects.add(repository);
                }
                // String categoryId = repository.getProperty(IRepositoryConstants.PROPERTY_CATEGORY);
                // if (categoryId != null && ((Category) parentElement).getId().equals(categoryId)) {
                // objects.add(repository);
                // }
            }
            return objects.toArray();
        }
        return new java.lang.Object[0];
    }

    public java.lang.Object getParent(java.lang.Object element) {
        return null;
    }

    public boolean hasChildren(java.lang.Object element) {
        return element instanceof org.eclipse.mylyn.internal.tasks.core.Category;
    }
}