/**
 * *****************************************************************************
 * Copyright (c) 2004, 2009 Eugene Kuleshov and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugene Kuleshov - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.ui.IActionFilter;
/**
 * Adapter factory for adapting TaskRepository to org.eclipse.ui.IActionFilter
 *
 * @author Eugene Kuleshov
 */
public class TaskRepositoryAdapterFactory implements org.eclipse.core.runtime.IAdapterFactory {
    @java.lang.SuppressWarnings("rawtypes")
    private static final java.lang.Class[] ADAPTER_TYPES = new java.lang.Class[]{ org.eclipse.ui.IActionFilter.class };

    @java.lang.SuppressWarnings("rawtypes")
    public java.lang.Class[] getAdapterList() {
        return org.eclipse.mylyn.internal.tasks.ui.TaskRepositoryAdapterFactory.ADAPTER_TYPES;
    }

    @java.lang.SuppressWarnings("rawtypes")
    public java.lang.Object getAdapter(final java.lang.Object adaptable, java.lang.Class adapterType) {
        if (adaptable instanceof org.eclipse.mylyn.tasks.core.TaskRepository) {
            return new org.eclipse.ui.IActionFilter() {
                public boolean testAttribute(java.lang.Object target, java.lang.String name, java.lang.String value) {
                    org.eclipse.mylyn.tasks.core.TaskRepository repository = ((org.eclipse.mylyn.tasks.core.TaskRepository) (target));
                    if ("offline".equals(name)) {
                        // $NON-NLS-1$
                        return java.lang.Boolean.valueOf(value).booleanValue() == repository.isOffline();
                    } else if ("supportQuery".equals(name)) {
                        // $NON-NLS-1$
                        org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnectorUi(repository.getConnectorKind());
                        org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryManager().getRepositoryConnector(repository.getConnectorKind());
                        return (null != connectorUi.getQueryWizard(repository, null)) && connector.canQuery(repository);
                    } else if ("supportNewTask".equals(name)) {
                        // $NON-NLS-1$
                        org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(repository.getConnectorKind());
                        return connector.canCreateNewTask(repository);
                    } else if ("hasRepository".equals(name)) {
                        // $NON-NLS-1$
                        return !repository.getConnectorKind().equals(org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector.CONNECTOR_KIND);
                    }
                    return false;
                }
            };
        }
        return null;
    }
}