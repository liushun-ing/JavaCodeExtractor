/**
 * *****************************************************************************
 * Copyright (c) 2013 Tasktop Technologies and others.
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
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter;
import org.eclipse.ui.part.ViewPart;
public abstract class AbstractTaskListView extends org.eclipse.ui.part.ViewPart {
    public abstract java.util.Set<org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter> getFilters();

    public abstract org.eclipse.mylyn.internal.tasks.ui.views.TaskListFilteredTree getFilteredTree();

    public abstract void refresh();

    public abstract boolean isFocusedMode();

    public abstract org.eclipse.jface.viewers.TreeViewer getViewer();

    public abstract boolean isScheduledPresentation();

    protected abstract void expandToActiveTasks();

    protected abstract void updateToolTip(boolean force);

    protected abstract boolean isAutoExpandMode();
}