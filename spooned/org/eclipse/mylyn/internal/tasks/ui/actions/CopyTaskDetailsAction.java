/**
 * *****************************************************************************
 * Copyright (c) 2004, 2014 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.actions;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.mylyn.commons.ui.ClipboardCopier;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
/**
 *
 * @author Mik Kersten
 * @author Steffen Pingel
 * @author Thomas Ehrnhoefer
 */
public class CopyTaskDetailsAction extends org.eclipse.ui.actions.BaseSelectionListenerAction {
    public static final java.lang.String ID = "org.eclipse.mylyn.tasklist.actions.copy";// $NON-NLS-1$


    public enum Mode {

        KEY(Messages.CopyTaskDetailsAction_ID_Menu_Label),
        URL(Messages.CopyTaskDetailsAction_Url_Menu_Label),
        ID_SUMMARY(Messages.CopyTaskDetailsAction_ID_Summary_Menu_Label),
        ID_SUMMARY_URL(Messages.CopyTaskDetailsAction_ID_Summary_and_Url_Menu_Label);
        private java.lang.String message;

        private Mode(java.lang.String message) {
            this.message = message;
        }
    }

    private org.eclipse.mylyn.internal.tasks.ui.actions.CopyTaskDetailsAction.Mode mode;

    public CopyTaskDetailsAction(org.eclipse.mylyn.internal.tasks.ui.actions.CopyTaskDetailsAction.Mode mode) {
        super("");// $NON-NLS-1$

        setMode(mode);
    }

    public org.eclipse.mylyn.internal.tasks.ui.actions.CopyTaskDetailsAction.Mode getMode() {
        return mode;
    }

    public void setMode(org.eclipse.mylyn.internal.tasks.ui.actions.CopyTaskDetailsAction.Mode mode) {
        org.eclipse.core.runtime.Assert.isNotNull(mode);
        this.mode = mode;
        setText(mode.message);
    }

    @java.lang.Override
    public void run() {
        org.eclipse.mylyn.commons.ui.ClipboardCopier.getDefault().copy(getStructuredSelection(), new org.eclipse.mylyn.commons.ui.ClipboardCopier.TextProvider() {
            public java.lang.String getTextForElement(java.lang.Object element) {
                return org.eclipse.mylyn.internal.tasks.ui.actions.CopyTaskDetailsAction.getTextForTask(element, getMode());
            }
        });
    }

    public static java.lang.String getTextForTask(java.lang.Object object) {
        return org.eclipse.mylyn.internal.tasks.ui.actions.CopyTaskDetailsAction.getTextForTask(object, org.eclipse.mylyn.internal.tasks.ui.actions.CopyTaskDetailsAction.Mode.ID_SUMMARY_URL);
    }

    // TODO move to TasksUiUtil / into core
    public static java.lang.String getTextForTask(java.lang.Object object, org.eclipse.mylyn.internal.tasks.ui.actions.CopyTaskDetailsAction.Mode mode) {
        java.lang.StringBuffer sb = new java.lang.StringBuffer();
        switch (mode) {
            case KEY :
                if (object instanceof org.eclipse.mylyn.tasks.core.ITask) {
                    org.eclipse.mylyn.tasks.core.ITask task = ((org.eclipse.mylyn.tasks.core.ITask) (object));
                    if (task.getTaskKey() != null) {
                        sb.append(task.getTaskKey());
                    }
                }
                break;
            case URL :
                if (object instanceof org.eclipse.mylyn.tasks.core.IRepositoryElement) {
                    java.lang.String taskUrl = org.eclipse.mylyn.internal.tasks.ui.actions.CopyTaskDetailsAction.getUrl(((org.eclipse.mylyn.tasks.core.IRepositoryElement) (object)));
                    if (org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.isValidUrl(taskUrl)) {
                        sb.append(taskUrl);
                    }
                }
                break;
            case ID_SUMMARY :
                if (object instanceof org.eclipse.mylyn.tasks.core.ITask) {
                    org.eclipse.mylyn.tasks.core.ITask task = ((org.eclipse.mylyn.tasks.core.ITask) (object));
                    if (task.getTaskKey() != null) {
                        sb.append(org.apache.commons.lang.StringUtils.capitalize(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskPrefix(task.getConnectorKind())));
                        sb.append(task.getTaskKey());
                        sb.append(": ");// $NON-NLS-1$

                    }
                    sb.append(task.getSummary());
                } else if (object instanceof org.eclipse.mylyn.tasks.core.IRepositoryElement) {
                    org.eclipse.mylyn.tasks.core.IRepositoryElement element = ((org.eclipse.mylyn.tasks.core.IRepositoryElement) (object));
                    sb.append(element.getSummary());
                }
                break;
            case ID_SUMMARY_URL :
                if (object instanceof org.eclipse.mylyn.tasks.core.ITask) {
                    org.eclipse.mylyn.tasks.core.ITask task = ((org.eclipse.mylyn.tasks.core.ITask) (object));
                    if (task.getTaskKey() != null) {
                        sb.append(task.getTaskKey());
                        sb.append(": ");// $NON-NLS-1$

                    }
                    sb.append(task.getSummary());
                    java.lang.String taskUrl = org.eclipse.mylyn.internal.tasks.ui.actions.CopyTaskDetailsAction.getUrl(((org.eclipse.mylyn.tasks.core.IRepositoryElement) (object)));
                    if (org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.isValidUrl(taskUrl)) {
                        sb.append(org.eclipse.mylyn.commons.ui.ClipboardCopier.LINE_SEPARATOR);
                        sb.append(taskUrl);
                    }
                } else if (object instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery) {
                    org.eclipse.mylyn.internal.tasks.core.RepositoryQuery query = ((org.eclipse.mylyn.internal.tasks.core.RepositoryQuery) (object));
                    sb.append(query.getSummary());
                    if (org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.isValidUrl(query.getUrl())) {
                        sb.append(org.eclipse.mylyn.commons.ui.ClipboardCopier.LINE_SEPARATOR);
                        sb.append(query.getUrl());
                    }
                } else if (object instanceof org.eclipse.mylyn.tasks.core.IRepositoryElement) {
                    org.eclipse.mylyn.tasks.core.IRepositoryElement element = ((org.eclipse.mylyn.tasks.core.IRepositoryElement) (object));
                    sb.append(element.getSummary());
                }
                break;
        }
        return sb.toString();
    }

    private static java.lang.String getUrl(org.eclipse.mylyn.tasks.core.IRepositoryElement element) {
        if (element instanceof org.eclipse.mylyn.tasks.core.ITask) {
            org.eclipse.mylyn.tasks.core.ITask task = ((org.eclipse.mylyn.tasks.core.ITask) (element));
            org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryConnector(task.getConnectorKind());
            org.eclipse.mylyn.tasks.core.TaskRepository repository = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getRepository(task);
            java.net.URL location = connector.getBrowserUrl(repository, element);
            if (location != null) {
                return location.toString();
            } else if (task.getUrl() != null) {
                return task.getUrl();
            } else {
                return connector.getTaskUrl(task.getRepositoryUrl(), task.getTaskId());
            }
        } else if (element.getUrl() != null) {
            return element.getUrl();
        }
        return null;
    }
}