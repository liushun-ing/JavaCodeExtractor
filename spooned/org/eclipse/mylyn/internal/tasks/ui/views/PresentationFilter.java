/**
 * *****************************************************************************
 * Copyright (c) 2011 Tasktop Technologies and others.
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
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.TaskTask;
import org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter;
import org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
/**
 *
 * @author Steffen Pingel
 */
public class PresentationFilter extends org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter {
    private static org.eclipse.mylyn.internal.tasks.ui.views.PresentationFilter instance = new org.eclipse.mylyn.internal.tasks.ui.views.PresentationFilter();

    public static org.eclipse.mylyn.internal.tasks.ui.views.PresentationFilter getInstance() {
        return org.eclipse.mylyn.internal.tasks.ui.views.PresentationFilter.instance;
    }

    private boolean filterHiddenQueries;

    private boolean filterNonMatching;

    /**
     *
     * @noreference This method is not intended to be referenced by clients.
     */
    PresentationFilter() {
        updateSettings();
    }

    public boolean isFilterHiddenQueries() {
        return filterHiddenQueries;
    }

    public boolean isFilterNonMatching() {
        return filterNonMatching;
    }

    @java.lang.Override
    public boolean select(java.lang.Object parent, java.lang.Object element) {
        if (element instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery) {
            return selectQuery(((org.eclipse.mylyn.tasks.core.IRepositoryQuery) (element)));
        }
        // only filter repository tasks
        if (element instanceof org.eclipse.mylyn.internal.tasks.core.TaskTask) {
            return selectTask(parent, ((org.eclipse.mylyn.internal.tasks.core.TaskTask) (element)));
        }
        return true;
    }

    private boolean selectQuery(org.eclipse.mylyn.tasks.core.IRepositoryQuery query) {
        if (!filterHiddenQueries) {
            return true;
        }
        return !java.lang.Boolean.parseBoolean(query.getAttribute(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.ATTRIBUTE_HIDDEN));
    }

    private boolean selectTask(java.lang.Object parent, org.eclipse.mylyn.internal.tasks.core.TaskTask task) {
        if (!filterNonMatching) {
            return true;
        }
        // tasks matching a query or category should be included
        if (isInVisibleQuery(task)) {
            return true;
        }
        // explicitly scheduled subtasks should be shown in those containers
        if ((parent != null) && parent.getClass().equals(org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer.class)) {
            return true;
        }
        return false;
    }

    public void setFilterHiddenQueries(boolean enabled) {
        this.filterHiddenQueries = enabled;
    }

    public void setFilterNonMatching(boolean filterSubtasks) {
        this.filterNonMatching = filterSubtasks;
    }

    public void updateSettings() {
        setFilterHiddenQueries(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.FILTER_HIDDEN));
        setFilterNonMatching(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.FILTER_NON_MATCHING));
    }

    public boolean isInVisibleQuery(org.eclipse.mylyn.tasks.core.ITask task) {
        for (org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer container : ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (task)).getParentContainers()) {
            // categories and local subtasks are always visible
            if (container instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory) {
                return true;
            }
            // show task if is contained in a query
            if ((container instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery) && selectQuery(((org.eclipse.mylyn.tasks.core.IRepositoryQuery) (container)))) {
                return true;
            }
        }
        return false;
    }
}