/**
 * *****************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Maarten Meijer - improvements for bug 252699
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.util;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.mylyn.tasks.core.ITaskAttachment;
/**
 *
 * @author Steffen Pingel
 */
public class TaskAttachmentPropertyTester extends org.eclipse.core.expressions.PropertyTester {
    private static final java.lang.String PROPERTY_IS_CONTEXT = "isContext";// $NON-NLS-1$


    private static final java.lang.String PROPERTY_HAS_URL = "hasUrl";// $NON-NLS-1$


    private boolean equals(boolean value, java.lang.Object expectedValue) {
        return new java.lang.Boolean(value).equals(expectedValue);
    }

    public boolean test(java.lang.Object receiver, java.lang.String property, java.lang.Object[] args, java.lang.Object expectedValue) {
        if (receiver instanceof org.eclipse.mylyn.tasks.core.ITaskAttachment) {
            org.eclipse.mylyn.tasks.core.ITaskAttachment taskAttachment = ((org.eclipse.mylyn.tasks.core.ITaskAttachment) (receiver));
            if (org.eclipse.mylyn.internal.tasks.ui.util.TaskAttachmentPropertyTester.PROPERTY_IS_CONTEXT.equals(property)) {
                return equals(org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.isContext(taskAttachment), expectedValue);
            }
            if (org.eclipse.mylyn.internal.tasks.ui.util.TaskAttachmentPropertyTester.PROPERTY_HAS_URL.equals(property)) {
                return equals((taskAttachment.getUrl() != null) && (taskAttachment.getUrl().length() > 0), expectedValue);
            }
        }
        return false;
    }
}