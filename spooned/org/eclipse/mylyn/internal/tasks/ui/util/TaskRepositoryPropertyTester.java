/**
 * *****************************************************************************
 * Copyright (c) 2004, 2012 Jingwen Ou and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jingwen Ou - initial API and implementation
 *     Tasktop Technologies - enhancements
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.util;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
/**
 *
 * @author Jingwen Ou
 * @author Steffen Pingel
 */
public class TaskRepositoryPropertyTester extends org.eclipse.core.expressions.PropertyTester {
    private static final java.lang.String PROPERTY_CONNECTOR_KIND = "connectorKind";// $NON-NLS-1$


    private static final java.lang.String PROPERTY_USER_MANAGED = "userManaged";// $NON-NLS-1$


    private static final java.lang.String PROPERTY_DISCONNECTED = "disconnected";// $NON-NLS-1$


    private boolean equals(boolean value, java.lang.Object expectedValue) {
        return new java.lang.Boolean(value).equals(expectedValue);
    }

    public boolean test(java.lang.Object receiver, java.lang.String property, java.lang.Object[] args, java.lang.Object expectedValue) {
        if (receiver instanceof org.eclipse.mylyn.tasks.core.TaskRepository) {
            org.eclipse.mylyn.tasks.core.TaskRepository repository = ((org.eclipse.mylyn.tasks.core.TaskRepository) (receiver));
            if (org.eclipse.mylyn.internal.tasks.ui.util.TaskRepositoryPropertyTester.PROPERTY_CONNECTOR_KIND.equals(property)) {
                return repository.getConnectorKind().equals(expectedValue);
            } else if (org.eclipse.mylyn.internal.tasks.ui.util.TaskRepositoryPropertyTester.PROPERTY_USER_MANAGED.equals(property)) {
                org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(repository.getConnectorKind());
                return equals((connector != null) && connector.isUserManaged(), expectedValue);
            } else if (org.eclipse.mylyn.internal.tasks.ui.util.TaskRepositoryPropertyTester.PROPERTY_DISCONNECTED.equals(property)) {
                return equals(!repository.isOffline(), expectedValue);
            }
        }
        return false;
    }
}