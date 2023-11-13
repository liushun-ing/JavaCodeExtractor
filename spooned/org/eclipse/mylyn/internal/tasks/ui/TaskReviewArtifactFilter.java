/**
 * *****************************************************************************
 * Copyright (c) 2015 Vaughan Hilts and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vaughan Hilts - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.tasks.core.ITask;
/**
 * This class provides a way to filter out review artifacts, since we do not want to see them in the list. As an
 * important note, this filter is not intended to have an action button on the UI, as the way it filters is ultimately
 * handled on a per repository basis.
 *
 * @author Vaughan Hilts
 */
public class TaskReviewArtifactFilter extends org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter {
    @java.lang.Override
    public boolean select(java.lang.Object parent, java.lang.Object element) {
        if (element instanceof org.eclipse.mylyn.tasks.core.ITask) {
            org.eclipse.mylyn.tasks.core.ITask task = ((org.eclipse.mylyn.tasks.core.ITask) (element));
            java.lang.String artifactStateAttribute = task.getAttribute(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.ATTRIBUTE_ARTIFACT);
            return !java.lang.Boolean.parseBoolean(artifactStateAttribute);
        } else {
            return true;
        }
    }
}