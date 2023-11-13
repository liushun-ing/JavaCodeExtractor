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
package org.eclipse.mylyn.internal.tasks.ui.util;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
/**
 *
 * @author Steffen Pingel
 */
public class TaskContainerComparator implements java.util.Comparator<org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer> {
    public int compare(org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer c1, org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer c2) {
        if (c1.equals(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().getDefaultCategory())) {
            return -1;
        } else if (c2.equals(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().getDefaultCategory())) {
            return 1;
        } else {
            return c1.getSummary().compareToIgnoreCase(c2.getSummary());
        }
    }
}