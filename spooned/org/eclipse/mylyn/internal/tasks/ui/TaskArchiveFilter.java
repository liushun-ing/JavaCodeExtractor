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
package org.eclipse.mylyn.internal.tasks.ui;
import org.eclipse.mylyn.internal.tasks.core.UncategorizedTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.UnmatchedTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.UnsubmittedTaskContainer;
/**
 *
 * @author Mik Kersten
 */
public class TaskArchiveFilter extends org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter {
    @java.lang.Override
    public boolean select(java.lang.Object parent, java.lang.Object element) {
        if (element instanceof org.eclipse.mylyn.internal.tasks.core.UnmatchedTaskContainer) {
            if (((org.eclipse.mylyn.internal.tasks.core.UnmatchedTaskContainer) (element)).isEmpty()) {
                return false;
            }
        } else if (element instanceof org.eclipse.mylyn.internal.tasks.core.UnsubmittedTaskContainer) {
            if (((org.eclipse.mylyn.internal.tasks.core.UnsubmittedTaskContainer) (element)).isEmpty()) {
                return false;
            }
        } else if (element instanceof org.eclipse.mylyn.internal.tasks.core.UncategorizedTaskContainer) {
            if (((org.eclipse.mylyn.internal.tasks.core.UncategorizedTaskContainer) (element)).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}