/**
 * *****************************************************************************
 * Copyright (c) 2004, 2015 Tasktop Technologies and others.
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
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.internal.tasks.ui.actions.ClearOutgoingAction;
import org.eclipse.mylyn.tasks.core.ITask;
/**
 *
 * @author Steffen Pingel
 */
public class TaskPropertyTester extends org.eclipse.core.expressions.PropertyTester {
    private static final java.lang.String PROPERTY_CAN_GET_ATTACHMENT = "canGetAttachment";// $NON-NLS-1$


    private static final java.lang.String PROPERTY_CAN_GET_HISTORY = "canGetHistory";// $NON-NLS-1$


    private static final java.lang.String PROPERTY_CAN_POST_ATTACHMENT = "canPostAttachment";// $NON-NLS-1$


    private static final java.lang.String PROPERTY_CONNECTOR_KIND = "connectorKind";// $NON-NLS-1$


    private static final java.lang.String PROPERTY_HAS_ACTIVE_TIME = "hasActiveTime";// $NON-NLS-1$


    private static final java.lang.String PROPERTY_HAS_EDITS = "hasEdits";// $NON-NLS-1$


    private static final java.lang.String PROPERTY_HAS_LOCAL_CONTEXT = "hasLocalContext";// $NON-NLS-1$


    private static final java.lang.String PROPERTY_HAS_REPOSITORY_CONTEXT = "hasRepositoryContext";// $NON-NLS-1$


    private static final java.lang.String PROPERTY_IS_COMPLETED = "isCompleted";// $NON-NLS-1$


    private static final java.lang.String PROPERTY_IS_LOCAL = "isLocal";// $NON-NLS-1$


    private static final java.lang.String PROPERTY_LOCAL_COMPLETION_STATE = "hasLocalCompletionState";// $NON-NLS-1$


    private static final java.lang.String PROPERTY_IS_ARTIFACT = "isArtifact";// $NON-NLS-1$


    private boolean equals(boolean value, java.lang.Object expectedValue) {
        return new java.lang.Boolean(value).equals(expectedValue);
    }

    @java.lang.SuppressWarnings("deprecation")
    public boolean test(java.lang.Object receiver, java.lang.String property, java.lang.Object[] args, java.lang.Object expectedValue) {
        if (receiver instanceof org.eclipse.mylyn.tasks.core.ITask) {
            org.eclipse.mylyn.tasks.core.ITask task = ((org.eclipse.mylyn.tasks.core.ITask) (receiver));
            if (org.eclipse.mylyn.internal.tasks.ui.util.TaskPropertyTester.PROPERTY_CONNECTOR_KIND.equals(property)) {
                return task.getConnectorKind().equals(expectedValue);
            } else if (org.eclipse.mylyn.internal.tasks.ui.util.TaskPropertyTester.PROPERTY_CAN_POST_ATTACHMENT.equals(property)) {
                return equals(org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.canUploadAttachment(task), expectedValue);
            } else if (org.eclipse.mylyn.internal.tasks.ui.util.TaskPropertyTester.PROPERTY_CAN_GET_ATTACHMENT.equals(property)) {
                return equals(org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.canDownloadAttachment(task), expectedValue);
            } else if (org.eclipse.mylyn.internal.tasks.ui.util.TaskPropertyTester.PROPERTY_CAN_GET_HISTORY.equals(property)) {
                return org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.canGetTaskHistory(task);
            } else if (org.eclipse.mylyn.internal.tasks.ui.util.TaskPropertyTester.PROPERTY_HAS_ACTIVE_TIME.equals(property)) {
                return equals(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getActiveTime(task) > 0, expectedValue);
            } else if (org.eclipse.mylyn.internal.tasks.ui.util.TaskPropertyTester.PROPERTY_HAS_EDITS.equals(property)) {
                return equals(org.eclipse.mylyn.internal.tasks.ui.actions.ClearOutgoingAction.hasOutgoingChanges(task), expectedValue);
            } else if (org.eclipse.mylyn.internal.tasks.ui.util.TaskPropertyTester.PROPERTY_HAS_LOCAL_CONTEXT.equals(property)) {
                return equals(org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.hasLocalContext(task), expectedValue);
            } else if (org.eclipse.mylyn.internal.tasks.ui.util.TaskPropertyTester.PROPERTY_HAS_REPOSITORY_CONTEXT.equals(property)) {
                return equals(org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.hasContextAttachment(task), expectedValue);
            } else if (org.eclipse.mylyn.internal.tasks.ui.util.TaskPropertyTester.PROPERTY_IS_COMPLETED.equals(property)) {
                return equals(task.isCompleted(), expectedValue);
            } else if (org.eclipse.mylyn.internal.tasks.ui.util.TaskPropertyTester.PROPERTY_IS_LOCAL.equals(property)) {
                return (task instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask) && equals(((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (task)).isLocal(), expectedValue);
            } else if (org.eclipse.mylyn.internal.tasks.ui.util.TaskPropertyTester.PROPERTY_LOCAL_COMPLETION_STATE.equals(property)) {
                return equals(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.hasLocalCompletionState(task), expectedValue);
            } else if (org.eclipse.mylyn.internal.tasks.ui.util.TaskPropertyTester.PROPERTY_IS_ARTIFACT.equals(property)) {
                java.lang.String artifactFlag = task.getAttribute(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.ATTRIBUTE_ARTIFACT);
                return equals(java.lang.Boolean.valueOf(artifactFlag), expectedValue);
            }
        }
        return false;
    }
}