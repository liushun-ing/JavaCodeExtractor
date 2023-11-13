/**
 * *****************************************************************************
 * Copyright (c) 2004, 2010 Willian Mitsuda and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Willian Mitsuda - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.commands;
import org.eclipse.core.commands.IParameterValues;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.ui.TasksUi;
/**
 * Compute repository connectors type/label map
 *
 * @author Willian Mitsuda
 */
public class TaskRepositoryParameterValues implements org.eclipse.core.commands.IParameterValues {
    public java.util.Map<java.lang.String, java.lang.String> getParameterValues() {
        java.util.Collection<org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector> connectors = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnectors();
        java.util.Map<java.lang.String, java.lang.String> values = new java.util.HashMap<java.lang.String, java.lang.String>();
        for (org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector : connectors) {
            if (connector.canCreateRepository()) {
                values.put(connector.getLabel(), connector.getConnectorKind());
            }
        }
        return values;
    }
}