/**
 * *****************************************************************************
 * Copyright (c) 2010, 2013 Tasktop Technologies and others.
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
import org.eclipse.mylyn.commons.repositories.core.RepositoryCategory;
import org.eclipse.mylyn.internal.tasks.core.IRepositoryChangeListener;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryAdapter;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryChangeEvent;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryDelta;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryDelta.Type;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.swt.widgets.Display;
/**
 *
 * @author Robert Elves
 * @author Steffen Pingel
 */
public class TaskRepositoriesNavigatorContentProvider implements org.eclipse.jface.viewers.ITreeContentProvider {
    private static final java.lang.Object[] EMPTY_ARRAY = new java.lang.Object[0];

    private final org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager manager;

    private org.eclipse.jface.viewers.Viewer viewer;

    private org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoriesNavigatorContentProvider.Listener listener;

    private class Listener extends org.eclipse.mylyn.internal.tasks.core.TaskRepositoryAdapter implements org.eclipse.mylyn.internal.tasks.core.IRepositoryChangeListener {
        @java.lang.Override
        public void repositoryAdded(org.eclipse.mylyn.tasks.core.TaskRepository repository) {
            refresh(repository);
        }

        protected void refresh(org.eclipse.mylyn.tasks.core.TaskRepository repository) {
            if (viewer != null) {
                org.eclipse.swt.widgets.Display.getDefault().asyncExec(new java.lang.Runnable() {
                    public void run() {
                        if (((viewer != null) && (viewer.getControl() != null)) && (!viewer.getControl().isDisposed())) {
                            viewer.refresh();
                        }
                    }
                });
            }
        }

        @java.lang.Override
        public void repositoryRemoved(org.eclipse.mylyn.tasks.core.TaskRepository repository) {
            refresh(repository);
        }

        @java.lang.Override
        public void repositoryUrlChanged(org.eclipse.mylyn.tasks.core.TaskRepository repository, java.lang.String oldUrl) {
            refresh(repository);
        }

        public void repositoryChanged(org.eclipse.mylyn.internal.tasks.core.TaskRepositoryChangeEvent event) {
            org.eclipse.mylyn.internal.tasks.core.TaskRepositoryDelta.Type type = event.getDelta().getType();
            if (((type == org.eclipse.mylyn.internal.tasks.core.TaskRepositoryDelta.Type.ALL) || (type == org.eclipse.mylyn.internal.tasks.core.TaskRepositoryDelta.Type.PROPERTY)) || (type == org.eclipse.mylyn.internal.tasks.core.TaskRepositoryDelta.Type.OFFLINE)) {
                refresh(event.getRepository());
            }
        }
    }

    public TaskRepositoriesNavigatorContentProvider() {
        manager = ((org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager) (org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager()));
    }

    public void dispose() {
        if (listener != null) {
            manager.removeListener(listener);
            listener = null;
        }
    }

    public java.lang.Object[] getChildren(java.lang.Object parentElement) {
        if (parentElement instanceof org.eclipse.mylyn.commons.repositories.core.RepositoryCategory) {
            java.util.List<org.eclipse.mylyn.tasks.core.TaskRepository> repositories = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getAllRepositories();
            org.eclipse.mylyn.commons.repositories.core.RepositoryCategory category = ((org.eclipse.mylyn.commons.repositories.core.RepositoryCategory) (parentElement));
            if (org.eclipse.mylyn.commons.repositories.core.RepositoryCategory.ID_CATEGORY_ALL.equals(category.getId())) {
                return repositories.toArray();
            } else if (org.eclipse.mylyn.commons.repositories.core.RepositoryCategory.ID_CATEGORY_OTHER.equals(category.getId())) {
                java.util.Set<java.lang.Object> items = new java.util.HashSet<java.lang.Object>();
                for (org.eclipse.mylyn.tasks.core.TaskRepository repository : repositories) {
                    if (repository.getCategory() == null) {
                        items.add(repository);
                    }
                }
                return items.toArray();
            } else {
                java.util.Set<java.lang.Object> items = new java.util.HashSet<java.lang.Object>();
                for (org.eclipse.mylyn.tasks.core.TaskRepository repository : repositories) {
                    if (category.getId().equals(repository.getCategory())) {
                        items.add(repository);
                    }
                }
                return items.toArray();
            }
        }
        return org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoriesNavigatorContentProvider.EMPTY_ARRAY;
    }

    public java.lang.Object[] getElements(java.lang.Object inputElement) {
        return getChildren(inputElement);
    }

    public java.lang.Object getParent(java.lang.Object element) {
        return null;
    }

    public boolean hasChildren(java.lang.Object element) {
        return getChildren(element).length > 0;
    }

    public void inputChanged(org.eclipse.jface.viewers.Viewer viewer, java.lang.Object oldInput, java.lang.Object newInput) {
        if (listener != null) {
            manager.removeListener(listener);
            listener = null;
        }
        this.viewer = viewer;
        if (newInput != null) {
            listener = new org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoriesNavigatorContentProvider.Listener();
            manager.addListener(listener);
        }
    }
}