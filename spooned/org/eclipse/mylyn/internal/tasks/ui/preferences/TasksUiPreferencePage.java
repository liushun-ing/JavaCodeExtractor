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
 *     Abner Ballardo - fix for bug 276113
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.preferences;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.mylyn.commons.core.CommonMessages;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.ui.compatibility.CommonColors;
import org.eclipse.mylyn.internal.monitor.ui.ActivityContextManager;
import org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.actions.RestoreTaskListAction;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferenceLinkArea;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
/**
 *
 * @author Mik Kersten
 * @author Rob Elves
 * @author David Green
 */
public class TasksUiPreferencePage extends org.eclipse.jface.preference.PreferencePage implements org.eclipse.ui.IWorkbenchPreferencePage {
    public static final java.lang.String ID = "org.eclipse.mylyn.tasks.ui.preferences";// $NON-NLS-1$


    private static final java.lang.String FORWARDSLASH = "/";// $NON-NLS-1$


    private static final java.lang.String BACKSLASH_MULTI = "\\\\";// $NON-NLS-1$


    private static final int MS_MINUTES = 60 * 1000;

    private static com.google.common.collect.BiMap<java.lang.String, java.lang.Integer> SCHEDULE_TIME_MAP = com.google.common.collect.HashBiMap.create(com.google.common.collect.ImmutableMap.of(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.SCHEDULE_NEW_TASKS_FOR_THIS_WEEK, 0, org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.SCHEDULE_NEW_TASKS_FOR_TOMORROW, 1, org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.SCHEDULE_NEW_TASKS_FOR_TODAY, 2, org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.SCHEDULE_NEW_TASKS_FOR_NOT_SCHEDULED, 3));

    private org.eclipse.swt.widgets.Button useRichEditor;

    private org.eclipse.swt.widgets.Button editorHighlightsCurrentLine;

    private org.eclipse.swt.widgets.Button useWebBrowser;

    private org.eclipse.swt.widgets.Text fullSyncScheduleTime = null;

    private org.eclipse.swt.widgets.Text relevantTasksSyncScheduleTime = null;

    private org.eclipse.swt.widgets.Button enableBackgroundSynch;

    private org.eclipse.swt.widgets.Text taskDirectoryText = null;

    private org.eclipse.swt.widgets.Button browse = null;

    private org.eclipse.swt.widgets.Button notificationEnabledButton = null;

    private final org.eclipse.ui.forms.widgets.FormToolkit toolkit;

    private org.eclipse.swt.widgets.Spinner timeoutMinutes;

    private org.eclipse.swt.widgets.Button timeoutEnabledButton;

    private org.eclipse.ui.forms.widgets.ExpandableComposite advancedComposite;

    private org.eclipse.swt.widgets.Combo weekStartCombo;

    private org.eclipse.swt.widgets.Combo scheduleNewTasksCombo;

    private org.eclipse.swt.widgets.Button activityTrackingEnabledButton;

    private org.eclipse.swt.widgets.Label timeoutLabel1;

    private org.eclipse.swt.widgets.Label timeoutLabel2;

    private org.eclipse.swt.widgets.Button taskListTooltipEnabledButton;

    private org.eclipse.swt.widgets.Button showTaskTrimButton;

    private org.eclipse.swt.widgets.Button taskListServiceMessageEnabledButton;

    public TasksUiPreferencePage() {
        super();
        setPreferenceStore(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore());
        toolkit = new org.eclipse.ui.forms.widgets.FormToolkit(org.eclipse.swt.widgets.Display.getCurrent());
    }

    @java.lang.Override
    protected org.eclipse.swt.widgets.Control createContents(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.widgets.Composite container = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NULL);
        org.eclipse.swt.layout.GridLayout layout = new org.eclipse.swt.layout.GridLayout(1, false);
        container.setLayout(layout);
        createTaskRefreshScheduleGroup(container);
        createSchedulingGroup(container);
        createTaskNavigationGroup(container);
        createTaskListGroup(container);
        createTaskEditorGroup(container);
        org.eclipse.swt.widgets.Group taskActivityGroup = createTaskActivityGroup(container);
        if (!org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityMonitor().isEnabled()) {
            // hide controls but create them to avoid NPEs
            taskActivityGroup.setVisible(false);
            ((org.eclipse.swt.layout.GridData) (taskActivityGroup.getLayoutData())).exclude = true;
        }
        org.eclipse.swt.widgets.Composite advanced = createAdvancedSection(container);
        createTaskDataControl(advanced);
        if (getContainer() instanceof org.eclipse.ui.preferences.IWorkbenchPreferenceContainer) {
            java.lang.String message = Messages.TasksUiPreferencePage_See_X_for_configuring_Task_List_colors;
            // $NON-NLS-1$
            new org.eclipse.ui.dialogs.PreferenceLinkArea(advanced, org.eclipse.swt.SWT.NONE, "org.eclipse.ui.preferencePages.ColorsAndFonts", message, ((org.eclipse.ui.preferences.IWorkbenchPreferenceContainer) (getContainer())), null);
        }
        createLinks(advanced);
        updateRefreshGroupEnablements();
        applyDialogFont(container);
        return container;
    }

    private void createLinks(org.eclipse.swt.widgets.Composite container) {
        org.eclipse.ui.forms.widgets.Hyperlink link = new org.eclipse.ui.forms.widgets.Hyperlink(container, org.eclipse.swt.SWT.NULL);
        link.setForeground(org.eclipse.mylyn.commons.ui.compatibility.CommonColors.HYPERLINK_WIDGET);
        link.setUnderlined(true);
        link.setText(Messages.TasksUiPreferencePage_Use_the_Restore_dialog_to_recover_missing_tasks);
        link.addHyperlinkListener(new org.eclipse.ui.forms.events.IHyperlinkListener() {
            public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                getShell().close();
                new org.eclipse.mylyn.internal.tasks.ui.actions.RestoreTaskListAction().run();
            }

            public void linkEntered(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                // ignore
            }

            public void linkExited(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                // ignore
            }
        });
    }

    private org.eclipse.swt.widgets.Composite createAdvancedSection(org.eclipse.swt.widgets.Composite container) {
        advancedComposite = toolkit.createExpandableComposite(container, (org.eclipse.ui.forms.widgets.ExpandableComposite.COMPACT | org.eclipse.ui.forms.widgets.ExpandableComposite.TWISTIE) | org.eclipse.ui.forms.widgets.ExpandableComposite.TITLE_BAR);
        advancedComposite.setFont(container.getFont());
        advancedComposite.setBackground(container.getBackground());
        advancedComposite.setText(Messages.TasksUiPreferencePage_Advanced);
        advancedComposite.setLayout(new org.eclipse.swt.layout.GridLayout(1, false));
        advancedComposite.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_HORIZONTAL));
        advancedComposite.addExpansionListener(new org.eclipse.ui.forms.events.ExpansionAdapter() {
            @java.lang.Override
            public void expansionStateChanged(org.eclipse.ui.forms.events.ExpansionEvent e) {
                getControl().getShell().pack();
            }
        });
        org.eclipse.swt.widgets.Composite advanced = new org.eclipse.swt.widgets.Composite(advancedComposite, org.eclipse.swt.SWT.NONE);
        advanced.setLayout(new org.eclipse.swt.layout.GridLayout(1, false));
        advancedComposite.setClient(advanced);
        return advanced;
    }

    public void init(org.eclipse.ui.IWorkbench workbench) {
        // TODO Auto-generated method stub
    }

    @java.lang.Override
    public boolean performOk() {
        getPreferenceStore().setValue(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.NOTIFICATIONS_ENABLED, notificationEnabledButton.getSelection());
        getPreferenceStore().setValue(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.EDITOR_TASKS_RICH, useRichEditor.getSelection());
        getPreferenceStore().setValue(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.EDITOR_CURRENT_LINE_HIGHLIGHT, editorHighlightsCurrentLine.getSelection());
        getPreferenceStore().setValue(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.REPOSITORY_SYNCH_SCHEDULE_ENABLED, enableBackgroundSynch.getSelection());
        java.lang.String miliseconds = toMillisecondsString(fullSyncScheduleTime.getText());
        getPreferenceStore().setValue(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.REPOSITORY_SYNCH_SCHEDULE_MILISECONDS, miliseconds);
        miliseconds = toMillisecondsString(relevantTasksSyncScheduleTime.getText());
        getPreferenceStore().setValue(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.RELEVANT_TASKS_SCHEDULE_MILISECONDS, miliseconds);
        getPreferenceStore().setValue(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.TASK_LIST_TOOL_TIPS_ENABLED, taskListTooltipEnabledButton.getSelection());
        getPreferenceStore().setValue(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.SERVICE_MESSAGES_ENABLED, taskListServiceMessageEnabledButton.getSelection());
        getPreferenceStore().setValue(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.SHOW_TRIM, showTaskTrimButton.getSelection());
        getPreferenceStore().setValue(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.WEEK_START_DAY, getWeekStartValue());
        getPreferenceStore().setValue(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.SCHEDULE_NEW_TASKS_FOR, getScheduleNewTasksValue());
        org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin.getDefault().getPreferenceStore().setValue(org.eclipse.mylyn.internal.monitor.ui.ActivityContextManager.ACTIVITY_TIMEOUT_ENABLED, timeoutEnabledButton.getSelection());
        org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin.getDefault().getPreferenceStore().setValue(org.eclipse.mylyn.internal.monitor.ui.ActivityContextManager.ACTIVITY_TIMEOUT, timeoutMinutes.getSelection() * (60 * 1000));
        org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin.getDefault().getPreferenceStore().setValue(org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin.ACTIVITY_TRACKING_ENABLED, activityTrackingEnabledButton.getSelection());
        java.lang.String taskDirectory = taskDirectoryText.getText();
        taskDirectory = taskDirectory.replaceAll(org.eclipse.mylyn.internal.tasks.ui.preferences.TasksUiPreferencePage.BACKSLASH_MULTI, org.eclipse.mylyn.internal.tasks.ui.preferences.TasksUiPreferencePage.FORWARDSLASH);
        if (!taskDirectory.equals(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getDataDirectory())) {
            if (checkForExistingTasklist(taskDirectory)) {
                java.lang.Exception exception = null;
                try {
                    org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().setDataDirectory(taskDirectory);
                } catch (org.eclipse.core.runtime.CoreException e) {
                    exception = e;
                    org.eclipse.mylyn.commons.core.StatusHandler.log(e.getStatus());
                    org.eclipse.jface.dialogs.MessageDialog.openError(getShell(), Messages.TasksUiPreferencePage_Task_Data_Directory_Error, Messages.TasksUiPreferencePage_Error_applying_Task_List_data_directory_changes);
                } catch (org.eclipse.core.runtime.OperationCanceledException ce) {
                    exception = ce;
                }
                if ((exception != null) && (!taskDirectoryText.isDisposed())) {
                    java.lang.String originalDirectory = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getDefaultDataDirectory();
                    if (!taskDirectory.equals(originalDirectory)) {
                        taskDirectoryText.setText(originalDirectory);
                    }
                }
            } else {
                taskDirectoryText.setFocus();
                return false;
            }
        }
        return true;
    }

    private java.lang.String toMillisecondsString(java.lang.String minutesString) {
        return java.lang.Long.toString((60 * 1000) * java.lang.Long.parseLong(minutesString));
    }

    private int getWeekStartValue() {
        return weekStartCombo.getSelectionIndex() + 1;
    }

    private java.lang.String getScheduleNewTasksValue() {
        int index = scheduleNewTasksCombo.getSelectionIndex();
        return org.eclipse.mylyn.internal.tasks.ui.preferences.TasksUiPreferencePage.SCHEDULE_TIME_MAP.inverse().get(index);
    }

    @java.lang.Override
    public boolean performCancel() {
        taskDirectoryText.setText(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getDefaultDataDirectory());
        notificationEnabledButton.setSelection(getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.NOTIFICATIONS_ENABLED));
        useRichEditor.setSelection(getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.EDITOR_TASKS_RICH));
        editorHighlightsCurrentLine.setSelection(getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.EDITOR_CURRENT_LINE_HIGHLIGHT));
        java.lang.String repositorySyncMinutes = toMinutesString(getPreferenceStore().getLong(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.REPOSITORY_SYNCH_SCHEDULE_MILISECONDS));
        java.lang.String relevantSyncMinutes = toMinutesString(getPreferenceStore().getLong(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.RELEVANT_TASKS_SCHEDULE_MILISECONDS));
        boolean shouldSyncAutomatically = getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.REPOSITORY_SYNCH_SCHEDULE_ENABLED);
        enableBackgroundSynch.setSelection(shouldSyncAutomatically);
        fullSyncScheduleTime.setText(repositorySyncMinutes);
        relevantTasksSyncScheduleTime.setText(relevantSyncMinutes);
        taskListTooltipEnabledButton.setSelection(getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.TASK_LIST_TOOL_TIPS_ENABLED));
        taskListServiceMessageEnabledButton.setSelection(getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.SERVICE_MESSAGES_ENABLED));
        showTaskTrimButton.setSelection(getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.SHOW_TRIM));
        weekStartCombo.select(getPreferenceStore().getInt(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.WEEK_START_DAY) - 1);
        java.lang.String scheduleFor = getPreferenceStore().getString(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.SCHEDULE_NEW_TASKS_FOR);
        scheduleNewTasksCombo.select(org.eclipse.mylyn.internal.tasks.ui.preferences.TasksUiPreferencePage.SCHEDULE_TIME_MAP.getOrDefault(scheduleFor, 0));
        int minutes = org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin.getDefault().getPreferenceStore().getInt(org.eclipse.mylyn.internal.monitor.ui.ActivityContextManager.ACTIVITY_TIMEOUT) / org.eclipse.mylyn.internal.tasks.ui.preferences.TasksUiPreferencePage.MS_MINUTES;
        timeoutMinutes.setSelection(minutes);
        timeoutEnabledButton.setSelection(org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin.getDefault().getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.monitor.ui.ActivityContextManager.ACTIVITY_TIMEOUT_ENABLED));
        activityTrackingEnabledButton.setSelection(org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin.getDefault().getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin.ACTIVITY_TRACKING_ENABLED));
        return true;
    }

    @java.lang.Override
    public void performDefaults() {
        super.performDefaults();
        taskDirectoryText.setText(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getDefaultDataDirectory());
        notificationEnabledButton.setSelection(getPreferenceStore().getDefaultBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.NOTIFICATIONS_ENABLED));
        useRichEditor.setSelection(getPreferenceStore().getDefaultBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.EDITOR_TASKS_RICH));
        editorHighlightsCurrentLine.setSelection(getPreferenceStore().getDefaultBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.EDITOR_CURRENT_LINE_HIGHLIGHT));
        taskListTooltipEnabledButton.setSelection(getPreferenceStore().getDefaultBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.TASK_LIST_TOOL_TIPS_ENABLED));
        taskListServiceMessageEnabledButton.setSelection(getPreferenceStore().getDefaultBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.SERVICE_MESSAGES_ENABLED));
        showTaskTrimButton.setSelection(getPreferenceStore().getDefaultBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.SHOW_TRIM));
        enableBackgroundSynch.setSelection(getPreferenceStore().getDefaultBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.REPOSITORY_SYNCH_SCHEDULE_ENABLED));
        fullSyncScheduleTime.setText(toMinutesString(getPreferenceStore().getDefaultLong(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.REPOSITORY_SYNCH_SCHEDULE_MILISECONDS)));
        relevantTasksSyncScheduleTime.setText(toMinutesString(getPreferenceStore().getDefaultLong(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.RELEVANT_TASKS_SCHEDULE_MILISECONDS)));
        weekStartCombo.select(getPreferenceStore().getDefaultInt(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.WEEK_START_DAY) - 1);
        java.lang.String defaultScheduleFor = getPreferenceStore().getDefaultString(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.SCHEDULE_NEW_TASKS_FOR);
        scheduleNewTasksCombo.select(org.eclipse.mylyn.internal.tasks.ui.preferences.TasksUiPreferencePage.SCHEDULE_TIME_MAP.getOrDefault(defaultScheduleFor, 0));
        int activityTimeoutMinutes = org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin.getDefault().getPreferenceStore().getDefaultInt(org.eclipse.mylyn.internal.monitor.ui.ActivityContextManager.ACTIVITY_TIMEOUT) / org.eclipse.mylyn.internal.tasks.ui.preferences.TasksUiPreferencePage.MS_MINUTES;
        timeoutMinutes.setSelection(activityTimeoutMinutes);
        timeoutEnabledButton.setSelection(org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin.getDefault().getPreferenceStore().getDefaultBoolean(org.eclipse.mylyn.internal.monitor.ui.ActivityContextManager.ACTIVITY_TIMEOUT_ENABLED));
        activityTrackingEnabledButton.setSelection(org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin.getDefault().getPreferenceStore().getDefaultBoolean(org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin.ACTIVITY_TRACKING_ENABLED));
        updateRefreshGroupEnablements();
    }

    private void createTaskRefreshScheduleGroup(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.widgets.Group group = new org.eclipse.swt.widgets.Group(parent, org.eclipse.swt.SWT.SHADOW_ETCHED_IN);
        group.setText(Messages.TasksUiPreferencePage_Synchronization);
        group.setLayout(new org.eclipse.swt.layout.GridLayout(1, false));
        group.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_HORIZONTAL));
        org.eclipse.swt.widgets.Composite backgroundSync = new org.eclipse.swt.widgets.Composite(group, org.eclipse.swt.SWT.NULL);
        org.eclipse.jface.layout.GridLayoutFactory.swtDefaults().numColumns(3).equalWidth(false).margins(0, 0).applyTo(backgroundSync);
        // Enabled background synchronization
        enableBackgroundSynch = new org.eclipse.swt.widgets.Button(backgroundSync, org.eclipse.swt.SWT.CHECK);
        enableBackgroundSynch.setText(Messages.TasksUiPreferencePage_Synchronize_Task_List);
        enableBackgroundSynch.setSelection(getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.REPOSITORY_SYNCH_SCHEDULE_ENABLED));
        enableBackgroundSynch.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                updateRefreshGroupEnablements();
            }

            public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
            }
        });
        org.eclipse.jface.layout.GridDataFactory.defaultsFor(enableBackgroundSynch).span(3, 1).applyTo(enableBackgroundSynch);
        // Synchronize Task List Fully
        new org.eclipse.swt.widgets.Label(backgroundSync, org.eclipse.swt.SWT.NONE).setText(Messages.TasksUiPreferencePage_Synchronize_Fully);
        fullSyncScheduleTime = createSynchronizationScheduleTextBox(backgroundSync, org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.REPOSITORY_SYNCH_SCHEDULE_MILISECONDS);
        new org.eclipse.swt.widgets.Label(backgroundSync, org.eclipse.swt.SWT.NONE).setText(Messages.TasksUiPreferencePage_minutes);
        // Synchronize Relevant Tasks
        new org.eclipse.swt.widgets.Label(backgroundSync, org.eclipse.swt.SWT.NONE).setText(Messages.TasksUiPreferencePage_Synchronize_Relevant_Tasks);
        relevantTasksSyncScheduleTime = createSynchronizationScheduleTextBox(backgroundSync, org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.RELEVANT_TASKS_SCHEDULE_MILISECONDS);
        new org.eclipse.swt.widgets.Label(backgroundSync, org.eclipse.swt.SWT.NONE).setText(Messages.TasksUiPreferencePage_minutes);
        // notification
        notificationEnabledButton = new org.eclipse.swt.widgets.Button(group, org.eclipse.swt.SWT.CHECK);
        notificationEnabledButton.setText(Messages.TasksUiPreferencePage_Display_notifications_for_overdue_tasks_and_incoming_changes);
        notificationEnabledButton.setSelection(getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.NOTIFICATIONS_ENABLED));
    }

    private org.eclipse.swt.widgets.Text createSynchronizationScheduleTextBox(org.eclipse.swt.widgets.Composite backgroundSync, java.lang.String preferenceKey) {
        org.eclipse.swt.widgets.Text text = new org.eclipse.swt.widgets.Text(backgroundSync, org.eclipse.swt.SWT.BORDER | org.eclipse.swt.SWT.RIGHT);
        org.eclipse.swt.layout.GridData gridDataRepo = new org.eclipse.swt.layout.GridData();
        gridDataRepo.widthHint = 25;
        text.setLayoutData(gridDataRepo);
        long querySyncMilliseconds = getPreferenceStore().getLong(preferenceKey);
        text.setText(toMinutesString(querySyncMilliseconds));
        text.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
            public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
                updateRefreshGroupEnablements();
            }
        });
        return text;
    }

    private void createTaskEditorGroup(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.widgets.Group container = new org.eclipse.swt.widgets.Group(parent, org.eclipse.swt.SWT.SHADOW_ETCHED_IN);
        container.setLayout(new org.eclipse.swt.layout.GridLayout(2, false));
        container.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_HORIZONTAL));
        container.setText(Messages.TasksUiPreferencePage_Task_Editing);
        useRichEditor = new org.eclipse.swt.widgets.Button(container, org.eclipse.swt.SWT.RADIO);
        useRichEditor.setText(Messages.TasksUiPreferencePage_Rich_Editor__Recommended_);
        useRichEditor.setSelection(getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.EDITOR_TASKS_RICH));
        useWebBrowser = new org.eclipse.swt.widgets.Button(container, org.eclipse.swt.SWT.RADIO);
        useWebBrowser.setText(Messages.TasksUiPreferencePage_Web_Browser);
        useWebBrowser.setSelection(!getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.EDITOR_TASKS_RICH));
        editorHighlightsCurrentLine = new org.eclipse.swt.widgets.Button(container, org.eclipse.swt.SWT.CHECK);
        editorHighlightsCurrentLine.setText(Messages.TasksUiPreferencePage_highlight_current_line);
        editorHighlightsCurrentLine.setSelection(getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.EDITOR_CURRENT_LINE_HIGHLIGHT));
    }

    private void createTaskDataControl(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.widgets.Group taskDataGroup = new org.eclipse.swt.widgets.Group(parent, org.eclipse.swt.SWT.SHADOW_ETCHED_IN);
        taskDataGroup.setText(Messages.TasksUiPreferencePage_Task_Data);
        taskDataGroup.setLayout(new org.eclipse.swt.layout.GridLayout(1, false));
        taskDataGroup.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_HORIZONTAL));
        org.eclipse.swt.widgets.Composite dataDirComposite = new org.eclipse.swt.widgets.Composite(taskDataGroup, org.eclipse.swt.SWT.NULL);
        org.eclipse.swt.layout.GridLayout gridLayout = new org.eclipse.swt.layout.GridLayout(3, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        dataDirComposite.setLayout(gridLayout);
        dataDirComposite.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_HORIZONTAL));
        org.eclipse.swt.widgets.Label label = new org.eclipse.swt.widgets.Label(dataDirComposite, org.eclipse.swt.SWT.NULL);
        label.setText(Messages.TasksUiPreferencePage_Data_directory_);
        java.lang.String taskDirectory = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getDataDirectory();
        taskDirectory = taskDirectory.replaceAll(org.eclipse.mylyn.internal.tasks.ui.preferences.TasksUiPreferencePage.BACKSLASH_MULTI, org.eclipse.mylyn.internal.tasks.ui.preferences.TasksUiPreferencePage.FORWARDSLASH);
        taskDirectoryText = new org.eclipse.swt.widgets.Text(dataDirComposite, org.eclipse.swt.SWT.BORDER);
        taskDirectoryText.setText(taskDirectory);
        taskDirectoryText.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_HORIZONTAL));
        browse = new org.eclipse.swt.widgets.Button(dataDirComposite, org.eclipse.swt.SWT.TRAIL);
        browse.setText(Messages.TasksUiPreferencePage_Browse_);
        browse.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                org.eclipse.swt.widgets.DirectoryDialog dialog = new org.eclipse.swt.widgets.DirectoryDialog(getShell());
                dialog.setText(Messages.TasksUiPreferencePage_Folder_Selection);
                dialog.setMessage(Messages.TasksUiPreferencePage_Specify_the_folder_for_tasks);
                java.lang.String dir = taskDirectoryText.getText();
                dir = dir.replaceAll(org.eclipse.mylyn.internal.tasks.ui.preferences.TasksUiPreferencePage.BACKSLASH_MULTI, org.eclipse.mylyn.internal.tasks.ui.preferences.TasksUiPreferencePage.FORWARDSLASH);
                dialog.setFilterPath(dir);
                dir = dialog.open();
                if ((dir == null) || dir.equals("")) {
                    // $NON-NLS-1$
                    return;
                }
                dir = dir.replaceAll(org.eclipse.mylyn.internal.tasks.ui.preferences.TasksUiPreferencePage.BACKSLASH_MULTI, org.eclipse.mylyn.internal.tasks.ui.preferences.TasksUiPreferencePage.FORWARDSLASH);
                taskDirectoryText.setText(dir);
            }
        });
    }

    private void createSchedulingGroup(org.eclipse.swt.widgets.Composite container) {
        org.eclipse.swt.widgets.Group group = new org.eclipse.swt.widgets.Group(container, org.eclipse.swt.SWT.SHADOW_ETCHED_IN);
        group.setText(Messages.TasksUiPreferencePage_Scheduling);
        group.setLayout(new org.eclipse.swt.layout.GridLayout(2, false));
        group.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_HORIZONTAL));
        org.eclipse.swt.widgets.Label weekStartLabel = new org.eclipse.swt.widgets.Label(group, org.eclipse.swt.SWT.NONE);
        weekStartLabel.setText(Messages.TasksUiPreferencePage_Week_Start);
        weekStartCombo = new org.eclipse.swt.widgets.Combo(group, org.eclipse.swt.SWT.READ_ONLY);
        weekStartCombo.add(org.eclipse.mylyn.commons.core.CommonMessages.Sunday);
        weekStartCombo.add(org.eclipse.mylyn.commons.core.CommonMessages.Monday);
        weekStartCombo.add(org.eclipse.mylyn.commons.core.CommonMessages.Tuesday);
        weekStartCombo.add(org.eclipse.mylyn.commons.core.CommonMessages.Wednesday);
        weekStartCombo.add(org.eclipse.mylyn.commons.core.CommonMessages.Thursday);
        weekStartCombo.add(org.eclipse.mylyn.commons.core.CommonMessages.Friday);
        weekStartCombo.add(org.eclipse.mylyn.commons.core.CommonMessages.Saturday);
        weekStartCombo.select(getPreferenceStore().getInt(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.WEEK_START_DAY) - 1);
        org.eclipse.swt.widgets.Label scheduleNewTasksLabel = new org.eclipse.swt.widgets.Label(group, org.eclipse.swt.SWT.NONE);
        scheduleNewTasksLabel.setText(Messages.TasksUiPreferencePage_ScheduleNewTasks);
        scheduleNewTasksCombo = new org.eclipse.swt.widgets.Combo(group, org.eclipse.swt.SWT.READ_ONLY);
        scheduleNewTasksCombo.add(Messages.TasksUiPreferencePage_ThisWeek);
        scheduleNewTasksCombo.add(Messages.TasksUiPreferencePage_Tomorrow);
        scheduleNewTasksCombo.add(Messages.TasksUiPreferencePage_Today);
        scheduleNewTasksCombo.add(Messages.TasksUiPreferencePage_Unscheduled);
        java.lang.String scheduleFor = getPreferenceStore().getString(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.SCHEDULE_NEW_TASKS_FOR);
        scheduleNewTasksCombo.select(org.eclipse.mylyn.internal.tasks.ui.preferences.TasksUiPreferencePage.SCHEDULE_TIME_MAP.getOrDefault(scheduleFor, 0));
    }

    private void createTaskNavigationGroup(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.widgets.Group group = new org.eclipse.swt.widgets.Group(parent, org.eclipse.swt.SWT.NONE);
        group.setText(Messages.TasksUiPreferencePage_Task_Navigation_Group_Label);
        group.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_HORIZONTAL));
        group.setLayout(new org.eclipse.swt.layout.GridLayout());
        showTaskTrimButton = new org.eclipse.swt.widgets.Button(group, org.eclipse.swt.SWT.CHECK);
        showTaskTrimButton.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.LEFT, org.eclipse.swt.SWT.CENTER, true, false));
        showTaskTrimButton.setText(Messages.TasksUiPreferencePage_Show_active_task_trim_Button_Label);
        showTaskTrimButton.setSelection(getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.SHOW_TRIM));
    }

    private void createTaskListGroup(org.eclipse.swt.widgets.Composite container) {
        org.eclipse.swt.widgets.Group group = new org.eclipse.swt.widgets.Group(container, org.eclipse.swt.SWT.SHADOW_ETCHED_IN);
        group.setText(Messages.TasksUiPreferencePage_Task_List_Group);
        group.setLayout(new org.eclipse.swt.layout.GridLayout(1, false));
        group.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_HORIZONTAL));
        taskListTooltipEnabledButton = new org.eclipse.swt.widgets.Button(group, org.eclipse.swt.SWT.CHECK);
        taskListTooltipEnabledButton.setText(Messages.TasksUiPreferencePage_Show_tooltip_on_hover_Label);
        taskListTooltipEnabledButton.setSelection(getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.TASK_LIST_TOOL_TIPS_ENABLED));
        taskListServiceMessageEnabledButton = new org.eclipse.swt.widgets.Button(group, org.eclipse.swt.SWT.CHECK);
        taskListServiceMessageEnabledButton.setText(Messages.TasksUiPreferencePage_Notification_for_new_connectors_available_Label);
        taskListServiceMessageEnabledButton.setSelection(getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.SERVICE_MESSAGES_ENABLED));
    }

    private org.eclipse.swt.widgets.Group createTaskActivityGroup(org.eclipse.swt.widgets.Composite container) {
        org.eclipse.swt.widgets.Group group = new org.eclipse.swt.widgets.Group(container, org.eclipse.swt.SWT.SHADOW_ETCHED_IN);
        group.setText(Messages.TasksUiPreferencePage_Task_Timing);
        group.setLayout(new org.eclipse.swt.layout.GridLayout(3, false));
        group.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_HORIZONTAL));
        boolean activityTrackingEnabled = org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin.getDefault().getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin.ACTIVITY_TRACKING_ENABLED);
        boolean timeoutEnabled = org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin.getDefault().getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.monitor.ui.ActivityContextManager.ACTIVITY_TIMEOUT_ENABLED);
        activityTrackingEnabledButton = new org.eclipse.swt.widgets.Button(group, org.eclipse.swt.SWT.CHECK);
        activityTrackingEnabledButton.setText(Messages.TasksUiPreferencePage_Enable_Time_Tracking);
        activityTrackingEnabledButton.setSelection(activityTrackingEnabled);
        activityTrackingEnabledButton.setToolTipText(Messages.TasksUiPreferencePage_Track_Time_Spent);
        activityTrackingEnabledButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                updateRefreshGroupEnablements();
            }
        });
        org.eclipse.jface.layout.GridDataFactory.swtDefaults().span(3, 1).applyTo(activityTrackingEnabledButton);
        timeoutEnabledButton = new org.eclipse.swt.widgets.Button(group, org.eclipse.swt.SWT.CHECK);
        timeoutEnabledButton.setText(Messages.TasksUiPreferencePage_Enable_inactivity_timeouts);
        timeoutEnabledButton.setSelection(timeoutEnabled);
        timeoutEnabledButton.setToolTipText(Messages.TasksUiPreferencePage_If_disabled);
        timeoutEnabledButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                updateRefreshGroupEnablements();
            }
        });
        org.eclipse.jface.layout.GridDataFactory.swtDefaults().span(3, 1).applyTo(timeoutEnabledButton);
        timeoutLabel1 = new org.eclipse.swt.widgets.Label(group, org.eclipse.swt.SWT.NONE);
        timeoutLabel1.setText(Messages.TasksUiPreferencePage_Stop_time_accumulation_after);
        timeoutMinutes = new org.eclipse.swt.widgets.Spinner(group, org.eclipse.swt.SWT.BORDER);
        timeoutMinutes.setDigits(0);
        timeoutMinutes.setIncrement(5);
        timeoutMinutes.setMaximum(60);
        timeoutMinutes.setMinimum(1);
        long minutes = org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin.getDefault().getPreferenceStore().getLong(org.eclipse.mylyn.internal.monitor.ui.ActivityContextManager.ACTIVITY_TIMEOUT) / org.eclipse.mylyn.internal.tasks.ui.preferences.TasksUiPreferencePage.MS_MINUTES;
        timeoutMinutes.setSelection(((int) (minutes)));
        timeoutMinutes.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                updateRefreshGroupEnablements();
            }
        });
        timeoutLabel2 = new org.eclipse.swt.widgets.Label(group, org.eclipse.swt.SWT.NONE);
        timeoutLabel2.setText(Messages.TasksUiPreferencePage_minutes_of_inactivity);
        return group;
    }

    public void updateRefreshGroupEnablements() {
        java.lang.String errorMessage = null;
        if (enableBackgroundSynch.getSelection()) {
            errorMessage = validateSynchronizeSchedule(fullSyncScheduleTime);
            if (errorMessage == null) {
                errorMessage = validateSynchronizeSchedule(relevantTasksSyncScheduleTime);
            }
        }
        setErrorMessage(errorMessage);
        setValid(errorMessage == null);
        if (activityTrackingEnabledButton.getSelection()) {
            timeoutEnabledButton.setEnabled(true);
            timeoutMinutes.setEnabled(timeoutEnabledButton.getSelection());
            timeoutLabel1.setEnabled(timeoutEnabledButton.getSelection());
            timeoutLabel2.setEnabled(timeoutEnabledButton.getSelection());
        } else {
            timeoutEnabledButton.setEnabled(false);
            timeoutMinutes.setEnabled(false);
            timeoutLabel1.setEnabled(false);
            timeoutLabel2.setEnabled(false);
        }
        fullSyncScheduleTime.setEnabled(enableBackgroundSynch.getSelection());
        relevantTasksSyncScheduleTime.setEnabled(enableBackgroundSynch.getSelection());
    }

    private java.lang.String validateSynchronizeSchedule(org.eclipse.swt.widgets.Text synchronizeText) {
        java.lang.String errorMessage = null;
        try {
            long number = java.lang.Long.parseLong(synchronizeText.getText());
            if (number <= 0) {
                errorMessage = Messages.TasksUiPreferencePage_Synchronize_schedule_time_must_be_GT_0;
            }
        } catch (java.lang.NumberFormatException e) {
            errorMessage = Messages.TasksUiPreferencePage_Synchronize_schedule_time_must_be_valid_integer;
        }
        return errorMessage;
    }

    private java.lang.String toMinutesString(long miliseconds) {
        long minutes = miliseconds / 60000;
        return java.lang.Long.toString(minutes);
    }

    private boolean checkForExistingTasklist(java.lang.String dir) {
        java.io.File newDataFolder = new java.io.File(dir);
        if ((!newDataFolder.exists()) && (!newDataFolder.mkdirs())) {
            org.eclipse.jface.dialogs.MessageDialog.openWarning(getControl().getShell(), Messages.TasksUiPreferencePage_Change_data_directory, Messages.TasksUiPreferencePage_Destination_folder_cannot_be_created);
            return false;
        }
        org.eclipse.jface.dialogs.MessageDialog dialogConfirm = new org.eclipse.jface.dialogs.MessageDialog(null, Messages.TasksUiPreferencePage_Confirm_Task_List_data_directory_change, null, Messages.TasksUiPreferencePage_A_new_empty_Task_List_will_be_created_in_the_chosen_directory_if_one_does_not_already_exists, org.eclipse.jface.dialogs.MessageDialog.WARNING, new java.lang.String[]{ org.eclipse.jface.dialogs.IDialogConstants.OK_LABEL, org.eclipse.jface.dialogs.IDialogConstants.CANCEL_LABEL }, org.eclipse.jface.dialogs.IDialogConstants.CANCEL_ID);
        int taskDataDirectoryAction = dialogConfirm.open();
        if (taskDataDirectoryAction != org.eclipse.jface.dialogs.IDialogConstants.OK_ID) {
            return false;
        }
        for (org.eclipse.mylyn.tasks.ui.editors.TaskEditor taskEditor : org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getActiveRepositoryTaskEditors()) {
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.closeTaskEditorInAllPages(taskEditor.getTaskEditorInput().getTask(), true);
        }
        return true;
    }

    @java.lang.Override
    public void dispose() {
        if (toolkit != null) {
            if (toolkit.getColors() != null) {
                toolkit.dispose();
            }
        }
        super.dispose();
    }
}