/**
 * *****************************************************************************
 * Copyright (c) 2004, 2008 Eugene Kuleshov and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugene Kuleshov - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.tasks.ui;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.mylyn.tasks.core.IRepositoryManager;
import org.eclipse.mylyn.tasks.core.TaskRepository;
/**
 * Extend to provide linking between repositories and tasks.
 *
 * @author Eugene Kuleshov
 * @since 2.0
 */
public abstract class AbstractTaskRepositoryLinkProvider implements org.eclipse.core.runtime.IExecutableExtension {
    private static final int DEFAULT_ORDER = 1000;

    private java.lang.String id;

    private java.lang.String name;

    private int order;

    public void setInitializationData(org.eclipse.core.runtime.IConfigurationElement config, java.lang.String propertyName, java.lang.Object data) {
        id = config.getAttribute("id");// $NON-NLS-1$

        name = config.getAttribute("name");// $NON-NLS-1$

        try {
            order = java.lang.Integer.parseInt(config.getAttribute("order"));// $NON-NLS-1$

        } catch (java.lang.NumberFormatException ex) {
            order = org.eclipse.mylyn.tasks.ui.AbstractTaskRepositoryLinkProvider.DEFAULT_ORDER;
        }
    }

    /**
     * This operation is invoked frequently by hyperlink detectors and needs to be fast (i.e. cannot do network access
     * or invoke long-running refreshes). Return null if the repository cannot be resolved without excessive file
     * I/O.@since 3.0
     *
     * @since 3.0
     */
    public abstract org.eclipse.mylyn.tasks.core.TaskRepository getTaskRepository(org.eclipse.core.resources.IResource resource, org.eclipse.mylyn.tasks.core.IRepositoryManager repositoryManager);

    public boolean canSetTaskRepository(org.eclipse.core.resources.IResource resource) {
        return false;
    }

    public boolean setTaskRepository(org.eclipse.core.resources.IResource resource, org.eclipse.mylyn.tasks.core.TaskRepository repository) {
        return false;
    }

    public java.lang.String getId() {
        return id;
    }

    public java.lang.String getName() {
        return name;
    }

    public int getOrder() {
        return order;
    }
}