/**
 * *****************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.LocalTask;
import org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskContainer;
import org.eclipse.ui.IWorkingSet;
/**
 * AbstractTaskListFilter for task working sets
 *
 * @author Eugene Kuleshov
 * @author Rob Elves
 * @author Steffen Pingel
 */
public class TaskWorkingSetFilter extends org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter {
    private org.eclipse.core.runtime.IAdaptable[] elements;

    @java.lang.Override
    public boolean select(java.lang.Object parent, java.lang.Object element) {
        if (parent instanceof org.eclipse.mylyn.tasks.core.ITask) {
            return true;
        }
        if ((parent == null) && (element instanceof org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer)) {
            return true;
        }
        if ((parent == null) && (element instanceof org.eclipse.mylyn.tasks.core.IRepositoryElement)) {
            return isContainedInWorkingSet(((org.eclipse.mylyn.tasks.core.IRepositoryElement) (element)));
        }
        if ((parent instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) && (!(parent instanceof org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer))) {
            return isContainedInWorkingSet(((org.eclipse.mylyn.tasks.core.IRepositoryElement) (parent)));
        }
        if (element instanceof org.eclipse.mylyn.internal.tasks.core.LocalTask) {
            for (org.eclipse.mylyn.tasks.core.IRepositoryElement container : ((org.eclipse.mylyn.internal.tasks.core.LocalTask) (element)).getParentContainers()) {
                return isContainedInWorkingSet(container);
            }
        }
        if ((parent instanceof org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer) && (element instanceof org.eclipse.mylyn.tasks.core.ITask)) {
            for (org.eclipse.mylyn.tasks.core.IRepositoryElement query : ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (element)).getParentContainers()) {
                if (isContainedInWorkingSet(query)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public boolean select(org.eclipse.mylyn.tasks.core.ITask task) {
        for (org.eclipse.mylyn.tasks.core.IRepositoryElement query : ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (task)).getParentContainers()) {
            if (isContainedInWorkingSet(query)) {
                return true;
            }
        }
        return false;
    }

    @java.lang.Override
    public boolean applyToFilteredText() {
        return true;
    }

    private boolean isContainedInWorkingSet(org.eclipse.mylyn.tasks.core.IRepositoryElement element) {
        return isContainedInWorkingSet(element, new java.util.HashSet<org.eclipse.mylyn.tasks.core.IRepositoryElement>());
    }

    private boolean isContainedInWorkingSet(org.eclipse.mylyn.tasks.core.IRepositoryElement container, java.util.Set<org.eclipse.mylyn.tasks.core.IRepositoryElement> visited) {
        if (elements == null) {
            return true;
        }
        if (visited.contains(container)) {
            return false;
        }
        visited.add(container);
        boolean seenTaskWorkingSets = false;
        java.lang.String handleIdentifier = container.getHandleIdentifier();
        for (org.eclipse.core.runtime.IAdaptable adaptable : elements) {
            if (adaptable instanceof org.eclipse.mylyn.tasks.core.IRepositoryElement) {
                seenTaskWorkingSets = true;
                if (handleIdentifier.equals(((org.eclipse.mylyn.tasks.core.IRepositoryElement) (adaptable)).getHandleIdentifier())) {
                    return true;
                }
                // handle case of sub tasks (not directly under a category/query)
                if (container instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask) {
                    for (org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer parent : ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (container)).getParentContainers()) {
                        if (visited.contains(parent)) {
                            continue;
                        }
                        if (isContainedInWorkingSet(parent, visited)) {
                            return true;
                        }
                    }
                }
            }
        }
        return !seenTaskWorkingSets;
    }

    public boolean updateWorkingSet(org.eclipse.ui.IWorkingSet currentWorkingSet) {
        org.eclipse.core.runtime.IAdaptable[] newElements = currentWorkingSet.getElements();
        if (!java.util.Arrays.equals(this.elements, newElements)) {
            this.elements = newElements;
            return true;
        }
        return false;
    }

    public org.eclipse.core.runtime.IAdaptable[] getElements() {
        return elements;
    }
}