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
package org.eclipse.mylyn.internal.tasks.ui.views;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPluginContribution;
/**
 *
 * @author Mik Kersten
 * @author Rob Elves
 */
public abstract class AbstractTaskListPresentation implements org.eclipse.ui.IPluginContribution {
    private java.lang.String pluginId;

    private final java.lang.String id;

    private java.lang.String name;

    private org.eclipse.jface.resource.ImageDescriptor imageDescriptor;

    private boolean primary = false;

    private final java.util.Map<org.eclipse.mylyn.internal.tasks.ui.views.TaskListView, org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListContentProvider> contentProviders = new java.util.HashMap<org.eclipse.mylyn.internal.tasks.ui.views.TaskListView, org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListContentProvider>();

    public AbstractTaskListPresentation(java.lang.String id) {
        this.id = id;
    }

    public org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListContentProvider getContentProvider(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView taskListView) {
        org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListContentProvider contentProvider = contentProviders.get(taskListView);
        if (contentProvider == null) {
            contentProvider = createContentProvider(taskListView);
            contentProviders.put(taskListView, contentProvider);
        }
        return contentProvider;
    }

    /**
     * Creates a new instance of a content provider for a particular instance of the Task List TODO: change view
     * parameter to be the viewer
     */
    protected abstract org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListContentProvider createContentProvider(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView taskListView);

    public org.eclipse.jface.resource.ImageDescriptor getImageDescriptor() {
        return imageDescriptor;
    }

    public java.lang.String getName() {
        return name;
    }

    public void setName(java.lang.String name) {
        this.name = name;
    }

    public void setImageDescriptor(org.eclipse.jface.resource.ImageDescriptor imageDescriptor) {
        this.imageDescriptor = imageDescriptor;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public java.lang.String getId() {
        return id;
    }

    public final java.lang.String getLocalId() {
        return getId();
    }

    public final java.lang.String getPluginId() {
        return pluginId;
    }

    public final void setPluginId(java.lang.String pluginId) {
        this.pluginId = pluginId;
    }
}