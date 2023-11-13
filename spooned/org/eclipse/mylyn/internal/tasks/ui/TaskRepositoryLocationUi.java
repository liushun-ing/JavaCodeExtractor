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
 *     BREDEX GmbH - fix for bug 295050
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.commons.core.CoreUtil;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.commons.net.UnsupportedRequestException;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryDelta;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryDelta.Type;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryLocation;
import org.eclipse.mylyn.internal.tasks.ui.dialogs.TaskRepositoryCredentialsDialog;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
/**
 *
 * @author Steffen Pingel
 */
public class TaskRepositoryLocationUi extends org.eclipse.mylyn.internal.tasks.core.TaskRepositoryLocation {
    private static java.lang.Object lock = new java.lang.Object();

    public TaskRepositoryLocationUi(org.eclipse.mylyn.tasks.core.TaskRepository taskRepository) {
        super(taskRepository);
    }

    @java.lang.Override
    public void requestCredentials(org.eclipse.mylyn.commons.net.AuthenticationType authType, java.lang.String message, org.eclipse.core.runtime.IProgressMonitor monitor) throws org.eclipse.mylyn.commons.net.UnsupportedRequestException {
        if (org.eclipse.mylyn.commons.core.CoreUtil.TEST_MODE) {
            throw new org.eclipse.mylyn.commons.net.UnsupportedRequestException();
        }
        org.eclipse.mylyn.commons.net.AuthenticationCredentials oldCredentials = taskRepository.getCredentials(authType);
        // synchronize on a static lock to ensure that only one password dialog is displayed at a time
        synchronized(org.eclipse.mylyn.internal.tasks.ui.TaskRepositoryLocationUi.lock) {
            // check if the credentials changed while the thread was waiting for the lock
            if (!areEqual(oldCredentials, taskRepository.getCredentials(authType))) {
                return;
            }
            if (org.eclipse.mylyn.commons.net.Policy.isBackgroundMonitor(monitor)) {
                throw new org.eclipse.mylyn.commons.net.UnsupportedRequestException();
            }
            org.eclipse.mylyn.internal.tasks.ui.TaskRepositoryLocationUi.PasswordRunner runner = new org.eclipse.mylyn.internal.tasks.ui.TaskRepositoryLocationUi.PasswordRunner(authType, message);
            if (!org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().isDisposed()) {
                org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().syncExec(runner);
                if (runner.isCanceled()) {
                    throw new org.eclipse.core.runtime.OperationCanceledException();
                }
                if (!runner.isChanged()) {
                    throw new org.eclipse.mylyn.commons.net.UnsupportedRequestException();
                }
            } else {
                throw new org.eclipse.mylyn.commons.net.UnsupportedRequestException();
            }
        }
    }

    private boolean areEqual(org.eclipse.mylyn.commons.net.AuthenticationCredentials oldCredentials, org.eclipse.mylyn.commons.net.AuthenticationCredentials credentials) {
        if (oldCredentials == null) {
            return credentials == null;
        } else {
            return oldCredentials.equals(credentials);
        }
    }

    private class PasswordRunner implements java.lang.Runnable {
        private final org.eclipse.mylyn.commons.net.AuthenticationType authType;

        private boolean changed;

        private final java.lang.String message;

        private boolean canceled;

        public PasswordRunner(org.eclipse.mylyn.commons.net.AuthenticationType credentialType, java.lang.String message) {
            this.authType = credentialType;
            this.message = message;
        }

        public boolean isChanged() {
            return changed;
        }

        public boolean isCanceled() {
            return canceled;
        }

        public void run() {
            // Shell shell = Display.getCurrent().getActiveShell();
            org.eclipse.swt.widgets.Shell shell = org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell();
            if ((shell != null) && (!shell.isDisposed())) {
                org.eclipse.mylyn.internal.tasks.ui.dialogs.TaskRepositoryCredentialsDialog dialog = org.eclipse.mylyn.internal.tasks.ui.dialogs.TaskRepositoryCredentialsDialog.createDialog(shell);
                initializeDialog(dialog);
                int resultCode = dialog.open();
                if (resultCode == org.eclipse.jface.window.Window.OK) {
                    apply(dialog);
                    changed = true;
                } else if (resultCode == org.eclipse.mylyn.internal.tasks.ui.dialogs.TaskRepositoryCredentialsDialog.TASK_REPOSITORY_CHANGED) {
                    changed = true;
                } else {
                    canceled = true;
                }
            }
        }

        private void initializeDialog(org.eclipse.mylyn.internal.tasks.ui.dialogs.TaskRepositoryCredentialsDialog dialog) {
            dialog.setTaskRepository(taskRepository);
            dialog.setFileDialog(org.eclipse.mylyn.commons.net.AuthenticationType.CERTIFICATE.equals(authType));
            org.eclipse.mylyn.commons.net.AuthenticationCredentials credentials = taskRepository.getCredentials(authType);
            if (credentials != null) {
                dialog.setUsername(credentials.getUserName());
                dialog.setPassword(credentials.getPassword());
            }
            // caller provided message takes precedence
            if (message != null) {
                dialog.setMessage(message);
            } else {
                dialog.setMessage(getDefaultMessage());
            }
        }

        private java.lang.String getDefaultMessage() {
            if (org.eclipse.mylyn.commons.net.AuthenticationType.REPOSITORY.equals(authType)) {
                return Messages.TaskRepositoryLocationUi_Enter_repository_password;
            } else if (org.eclipse.mylyn.commons.net.AuthenticationType.CERTIFICATE.equals(authType)) {
                return Messages.TaskRepositoryLocationUi_Enter_CLIENTCERTIFICATE_password;
            } else if (org.eclipse.mylyn.commons.net.AuthenticationType.HTTP.equals(authType)) {
                return Messages.TaskRepositoryLocationUi_Enter_HTTP_password;
            } else if (org.eclipse.mylyn.commons.net.AuthenticationType.PROXY.equals(authType)) {
                return Messages.TaskRepositoryLocationUi_Enter_proxy_password;
            }
            return null;
        }

        private void apply(org.eclipse.mylyn.internal.tasks.ui.dialogs.TaskRepositoryCredentialsDialog dialog) {
            org.eclipse.mylyn.commons.net.AuthenticationCredentials credentials = new org.eclipse.mylyn.commons.net.AuthenticationCredentials(dialog.getUserName(), dialog.getPassword());
            taskRepository.setCredentials(authType, credentials, dialog.getSavePassword());
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryManager().notifyRepositorySettingsChanged(taskRepository, new org.eclipse.mylyn.internal.tasks.core.TaskRepositoryDelta(org.eclipse.mylyn.internal.tasks.core.TaskRepositoryDelta.Type.CREDENTIALS, authType));
        }
    }
}