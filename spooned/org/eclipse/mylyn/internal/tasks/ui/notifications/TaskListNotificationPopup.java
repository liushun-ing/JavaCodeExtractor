/**
 * *****************************************************************************
 * Copyright (c) 2004, 2013 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.notifications;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.commons.notifications.ui.AbstractUiNotification;
import org.eclipse.mylyn.commons.workbench.AbstractWorkbenchNotificationPopup;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.ui.TaskScalingHyperlink;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
/**
 *
 * @author Rob Elves
 * @author Mik Kersten
 */
public class TaskListNotificationPopup extends org.eclipse.mylyn.commons.workbench.AbstractWorkbenchNotificationPopup {
    private static final java.lang.String NOTIFICATIONS_HIDDEN = Messages.TaskListNotificationPopup_more;

    private static final int NUM_NOTIFICATIONS_TO_DISPLAY = 4;

    private java.util.List<org.eclipse.mylyn.commons.notifications.ui.AbstractUiNotification> notifications;

    public TaskListNotificationPopup(org.eclipse.swt.widgets.Shell parent) {
        super(parent.getDisplay());
    }

    public void setContents(java.util.List<org.eclipse.mylyn.commons.notifications.ui.AbstractUiNotification> notifications) {
        this.notifications = notifications;
    }

    public java.util.List<org.eclipse.mylyn.commons.notifications.ui.AbstractUiNotification> getNotifications() {
        return new java.util.ArrayList<org.eclipse.mylyn.commons.notifications.ui.AbstractUiNotification>(notifications);
    }

    @java.lang.Override
    protected void createTitleArea(org.eclipse.swt.widgets.Composite parent) {
        super.createTitleArea(parent);
    }

    @java.lang.Override
    protected org.eclipse.swt.graphics.Color getTitleForeground() {
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getFormColors(org.eclipse.swt.widgets.Display.getDefault()).getColor(org.eclipse.ui.forms.IFormColors.TITLE);
    }

    @java.lang.Override
    protected void createContentArea(org.eclipse.swt.widgets.Composite parent) {
        int count = 0;
        for (final org.eclipse.mylyn.commons.notifications.ui.AbstractUiNotification notification : notifications) {
            org.eclipse.swt.widgets.Composite notificationComposite = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NO_FOCUS);
            org.eclipse.swt.layout.GridLayout gridLayout = new org.eclipse.swt.layout.GridLayout(2, false);
            org.eclipse.jface.layout.GridDataFactory.fillDefaults().grab(true, false).align(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.TOP).applyTo(notificationComposite);
            notificationComposite.setLayout(gridLayout);
            notificationComposite.setBackground(parent.getBackground());
            if (count < org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotificationPopup.NUM_NOTIFICATIONS_TO_DISPLAY) {
                final org.eclipse.swt.widgets.Label notificationLabelIcon = new org.eclipse.swt.widgets.Label(notificationComposite, org.eclipse.swt.SWT.NO_FOCUS);
                notificationLabelIcon.setBackground(parent.getBackground());
                notificationLabelIcon.setImage(notification.getNotificationKindImage());
                if (!(notification instanceof org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotificationReminder)) {
                    final org.eclipse.mylyn.internal.tasks.core.AbstractTask task = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (notification.getAdapter(org.eclipse.mylyn.internal.tasks.core.AbstractTask.class)));
                    if (task != null) {
                        notificationLabelIcon.addMouseListener(new org.eclipse.swt.events.MouseAdapter() {
                            @java.lang.Override
                            public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
                                org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskDataManager().setTaskRead(task, true);
                                notificationLabelIcon.setImage(null);
                                notificationLabelIcon.setToolTipText(null);
                            }
                        });
                        notificationLabelIcon.setToolTipText(Messages.TaskListNotificationPopup_Mark_Task_Read);
                    }
                }
                final org.eclipse.mylyn.internal.tasks.ui.TaskScalingHyperlink itemLink = new org.eclipse.mylyn.internal.tasks.ui.TaskScalingHyperlink(notificationComposite, org.eclipse.swt.SWT.BEGINNING | org.eclipse.swt.SWT.NO_FOCUS);
                org.eclipse.jface.layout.GridDataFactory.fillDefaults().grab(true, false).align(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.TOP).applyTo(itemLink);
                itemLink.setText(org.eclipse.jface.action.LegacyActionTools.escapeMnemonics(notification.getLabel()));
                itemLink.setImage(notification.getNotificationImage());
                itemLink.setBackground(parent.getBackground());
                itemLink.addHyperlinkListener(new org.eclipse.ui.forms.events.HyperlinkAdapter() {
                    @java.lang.Override
                    public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                        notification.open();
                        org.eclipse.ui.IWorkbenchWindow window = org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                        if (window != null) {
                            org.eclipse.swt.widgets.Shell windowShell = window.getShell();
                            if (windowShell != null) {
                                if (windowShell.getMinimized()) {
                                    windowShell.setMinimized(false);
                                }
                                windowShell.open();
                                windowShell.forceActive();
                            }
                        }
                    }
                });
                java.lang.String descriptionText = null;
                if (notification.getDescription() != null) {
                    descriptionText = notification.getDescription();
                }
                if ((descriptionText != null) && (!descriptionText.trim().equals(""))) {
                    // $NON-NLS-1$
                    org.eclipse.swt.widgets.Label descriptionLabel = new org.eclipse.swt.widgets.Label(notificationComposite, org.eclipse.swt.SWT.NO_FOCUS);
                    descriptionLabel.setText(org.eclipse.jface.action.LegacyActionTools.escapeMnemonics(descriptionText));
                    descriptionLabel.setBackground(parent.getBackground());
                    org.eclipse.jface.layout.GridDataFactory.fillDefaults().span(2, org.eclipse.swt.SWT.DEFAULT).grab(true, false).align(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.TOP).applyTo(descriptionLabel);
                }
            } else {
                int numNotificationsRemain = notifications.size() - count;
                org.eclipse.mylyn.internal.tasks.ui.TaskScalingHyperlink remainingHyperlink = new org.eclipse.mylyn.internal.tasks.ui.TaskScalingHyperlink(notificationComposite, org.eclipse.swt.SWT.NO_FOCUS);
                remainingHyperlink.setBackground(parent.getBackground());
                remainingHyperlink.setText((numNotificationsRemain + " ") + org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotificationPopup.NOTIFICATIONS_HIDDEN);// $NON-NLS-1$

                org.eclipse.jface.layout.GridDataFactory.fillDefaults().span(2, org.eclipse.swt.SWT.DEFAULT).applyTo(remainingHyperlink);
                remainingHyperlink.addHyperlinkListener(new org.eclipse.ui.forms.events.HyperlinkAdapter() {
                    @java.lang.Override
                    public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                        org.eclipse.mylyn.tasks.ui.TasksUiUtil.openTasksViewInActivePerspective().setFocus();
                        org.eclipse.ui.IWorkbenchWindow window = org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                        if (window != null) {
                            org.eclipse.swt.widgets.Shell windowShell = window.getShell();
                            if (windowShell != null) {
                                windowShell.setMaximized(true);
                                windowShell.open();
                            }
                        }
                    }
                });
                break;
            }
            count++;
        }
    }
}