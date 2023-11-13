/**
 * *****************************************************************************
 * Copyright (c) 2004, 2013 Tasktop Technologies and others.
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
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskRepository;
/**
 *
 * @author Mik Kersten
 */
public class TaskRepositoriesSorter extends org.eclipse.jface.viewers.ViewerSorter {
    @java.lang.Override
    public int compare(org.eclipse.jface.viewers.Viewer viewer, java.lang.Object e1, java.lang.Object e2) {
        if ((e1 instanceof org.eclipse.mylyn.tasks.core.TaskRepository) && (e2 instanceof org.eclipse.mylyn.tasks.core.TaskRepository)) {
            org.eclipse.mylyn.tasks.core.TaskRepository t1 = ((org.eclipse.mylyn.tasks.core.TaskRepository) (e1));
            org.eclipse.mylyn.tasks.core.TaskRepository t2 = ((org.eclipse.mylyn.tasks.core.TaskRepository) (e2));
            java.lang.String label1 = t1.getRepositoryLabel();
            java.lang.String label2 = t2.getRepositoryLabel();
            if (label1 == null) {
                return label2 != null ? 1 : 0;
            } else if (label2 == null) {
                return -1;
            }
            if (org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector.REPOSITORY_LABEL.equals(label1)) {
                return -1;
            } else if (org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector.REPOSITORY_LABEL.equals(label2)) {
                return 1;
            }
            return label1.compareToIgnoreCase(label2);
        }
        return super.compare(viewer, e1, e2);
    }
}