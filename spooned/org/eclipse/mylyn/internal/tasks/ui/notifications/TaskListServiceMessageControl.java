/**
 * *****************************************************************************
 * Copyright (c) 2010, 2015 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Itema AS - bug 326761: switched to use common service message control
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.notifications;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.mylyn.commons.notifications.feed.IServiceMessageListener;
import org.eclipse.mylyn.commons.notifications.feed.ServiceMessageEvent;
import org.eclipse.mylyn.commons.notifications.ui.NotificationControl;
import org.eclipse.mylyn.internal.commons.notifications.feed.ServiceMessage;
import org.eclipse.mylyn.internal.tasks.core.LocalTask;
import org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.actions.AddRepositoryAction;
import org.eclipse.mylyn.internal.tasks.ui.preferences.TasksUiPreferencePage;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.wizards.Messages;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
/**
 *
 * @author Robert Elves
 * @author Steffen Pingel
 * @author Torkild U. Resheim
 */
@java.lang.SuppressWarnings("restriction")
public class TaskListServiceMessageControl extends org.eclipse.mylyn.commons.notifications.ui.NotificationControl implements org.eclipse.mylyn.commons.notifications.feed.IServiceMessageListener {
    private org.eclipse.mylyn.internal.commons.notifications.feed.ServiceMessage currentMessage;

    public TaskListServiceMessageControl(org.eclipse.swt.widgets.Composite parent) {
        super(parent);
        setPreferencesPageId(org.eclipse.mylyn.internal.tasks.ui.preferences.TasksUiPreferencePage.ID);
    }

    static org.eclipse.core.commands.ExecutionEvent createExecutionEvent(org.eclipse.core.commands.Command command, org.eclipse.ui.handlers.IHandlerService handlerService) {
        return new org.eclipse.core.commands.ExecutionEvent(command, java.util.Collections.emptyMap(), null, org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.createDiscoveryWizardEvaluationContext(handlerService));
    }

    @java.lang.Override
    protected void closeMessage() {
        if (((currentMessage != null) && (currentMessage.getId() != null)) && (!currentMessage.getId().equals("0"))) {
            // $NON-NLS-1$
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().setValue(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.LAST_SERVICE_MESSAGE_ID, currentMessage.getId());
        }
        close();
    }

    public void handleEvent(final org.eclipse.mylyn.commons.notifications.feed.ServiceMessageEvent event) {
        switch (event.getEventKind()) {
            case MESSAGE_UPDATE :
                org.eclipse.jface.preference.IPreferenceStore preferenceStore = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore();
                preferenceStore.setValue(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.LAST_SERVICE_MESSAGE_CHECKTIME, new java.util.Date().getTime());
                java.lang.String lastMessageId = preferenceStore.getString(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.LAST_SERVICE_MESSAGE_ID);
                if ((lastMessageId != null) && lastMessageId.startsWith("org.eclipse.mylyn.reset.")) {
                    // $NON-NLS-1$
                    lastMessageId = "";// $NON-NLS-1$

                    preferenceStore.setValue(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.LAST_SERVICE_MESSAGE_ID, lastMessageId);
                }
                for (final org.eclipse.mylyn.internal.commons.notifications.feed.ServiceMessage message : event.getMessages()) {
                    if ((!message.isValid()) || message.getId().equals("-1")) {
                        // $NON-NLS-1$
                        continue;
                    }
                    if (message.getId().compareTo(lastMessageId) > 0) {
                        org.eclipse.swt.widgets.Display.getDefault().asyncExec(new java.lang.Runnable() {
                            public void run() {
                                setMessage(message);
                                // event id is not used but must be set to make configure link visible
                                setEventId("");// $NON-NLS-1$

                            }
                        });
                        break;
                    }
                }
                break;
            case STOP :
                close();
                break;
        }
    }

    public void setMessage(org.eclipse.mylyn.internal.commons.notifications.feed.ServiceMessage message) {
        if (message != null) {
            ensureControl();
            if ((message.getETag() != null) && (message.getLastModified() != null)) {
                org.eclipse.jface.preference.IPreferenceStore preferenceStore = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore();
                preferenceStore.setValue(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.LAST_SERVICE_MESSAGE_ETAG, message.getETag());
                preferenceStore.setValue(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.LAST_SERVICE_MESSAGE_LAST_MODIFIED, message.getLastModified());
            }
            this.currentMessage = message;
            setTitle(message.getTitle());
            setTitleImage(org.eclipse.jface.dialogs.Dialog.getImage(message.getImage()));
            setDescription(message.getDescription());
        }
    }

    @java.lang.Override
    public org.eclipse.swt.events.SelectionListener getLinkListener() {
        return new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                if (e.text == null) {
                    return;
                }
                final java.lang.String action = org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListServiceMessageControl.getAction(e.text);
                if ("create-local-task".equals(action)) {
                    // $NON-NLS-1$
                    closeMessage();
                    org.eclipse.mylyn.internal.tasks.core.LocalTask task = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.createNewLocalTask(null);
                    org.eclipse.mylyn.tasks.ui.TasksUiUtil.openTask(task);
                } else if ("connect".equals(action)) {
                    // $NON-NLS-1$
                    closeMessage();
                    new org.eclipse.mylyn.internal.tasks.ui.actions.AddRepositoryAction().run();
                } else if ("discovery".equals(action)) {
                    // $NON-NLS-1$
                    closeMessage();
                    final org.eclipse.core.commands.Command discoveryWizardCommand = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getConfiguredDiscoveryWizardCommand();
                    if ((discoveryWizardCommand != null) && discoveryWizardCommand.isEnabled()) {
                        org.eclipse.ui.handlers.IHandlerService handlerService = ((org.eclipse.ui.handlers.IHandlerService) (org.eclipse.ui.PlatformUI.getWorkbench().getService(org.eclipse.ui.handlers.IHandlerService.class)));
                        try {
                            handlerService.executeCommand(discoveryWizardCommand.getId(), null);
                        } catch (java.lang.Exception e1) {
                            org.eclipse.core.runtime.IStatus status = new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.osgi.util.NLS.bind(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.SelectRepositoryConnectorPage_discoveryProblemMessage, new java.lang.Object[]{ e1.getMessage() }), e1);
                            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.logAndDisplayStatus(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.SelectRepositoryConnectorPage_discoveryProblemTitle, status);
                        }
                    }
                } else if (org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.isValidUrl(e.text)) {
                    org.eclipse.mylyn.tasks.ui.TasksUiUtil.openUrl(e.text);
                } else if (currentMessage != null) {
                    org.eclipse.core.runtime.SafeRunner.run(new org.eclipse.jface.util.SafeRunnable() {
                        public void run() throws java.lang.Exception {
                            if (currentMessage.openLink(action)) {
                                closeMessage();
                            }
                        }
                    });
                }
            }
        };
    }

    /**
     * Extracts action from query part or a URL if applicable.
     */
    public static java.lang.String getAction(java.lang.String action) {
        if (action.startsWith("http")) {
            // $NON-NLS-1$
            java.net.URL url;
            try {
                url = new java.net.URL(action);
                java.lang.String query = url.getQuery();
                if ((query != null) && query.startsWith("action=")) {
                    // $NON-NLS-1$
                    int i = query.indexOf("&");// $NON-NLS-1$

                    if (i != (-1)) {
                        action = query.substring(7, i);
                    } else {
                        action = query.substring(7);
                    }
                } else {
                    return null;
                }
            } catch (java.net.MalformedURLException e1) {
                // ignore
                return null;
            }
        }
        return action.toLowerCase();
    }
}