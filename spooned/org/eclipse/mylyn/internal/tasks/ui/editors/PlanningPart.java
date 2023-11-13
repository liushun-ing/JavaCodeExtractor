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
package org.eclipse.mylyn.internal.tasks.ui.editors;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.mylyn.commons.core.DateUtil;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.commons.ui.CommonUiUtil;
import org.eclipse.mylyn.commons.ui.PlatformUiUtil;
import org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport;
import org.eclipse.mylyn.commons.workbench.forms.DatePicker;
import org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.DateRange;
import org.eclipse.mylyn.internal.tasks.core.DayDateRange;
import org.eclipse.mylyn.internal.tasks.core.ITaskListChangeListener;
import org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil;
import org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta;
import org.eclipse.mylyn.internal.tasks.core.WeekDateRange;
import org.eclipse.mylyn.internal.tasks.ui.ScheduleDatePicker;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.monitor.ui.MonitorUi;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskActivationListener;
import org.eclipse.mylyn.tasks.core.ITaskActivityListener;
import org.eclipse.mylyn.tasks.core.TaskActivationAdapter;
import org.eclipse.mylyn.tasks.core.TaskActivityAdapter;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorExtension;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.TaskFormPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.Section;
/**
 *
 * @author Shawn Minto
 * @author Sam Davis
 */
public class PlanningPart extends org.eclipse.mylyn.internal.tasks.ui.editors.AbstractLocalEditorPart {
    private boolean needsDueDate;

    private java.lang.String notesString;

    private org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor noteEditor;

    private static final int CONTROL_WIDTH = 120;

    private org.eclipse.mylyn.commons.workbench.forms.DatePicker dueDatePicker;

    private org.eclipse.swt.widgets.Text activeTimeText;

    private org.eclipse.swt.widgets.Spinner estimatedTimeSpinner;

    private org.eclipse.mylyn.internal.tasks.ui.ScheduleDatePicker scheduleDatePicker;

    private static final java.lang.String PERSONAL_NOTES = Messages.PlanningPart_Personal_Notes;

    private final org.eclipse.mylyn.internal.tasks.core.ITaskListChangeListener TASK_LIST_LISTENER = new org.eclipse.mylyn.internal.tasks.ui.editors.TaskListChangeAdapter() {
        @java.lang.Override
        public void containersChanged(java.util.Set<org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta> containers) {
            for (org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta taskContainerDelta : containers) {
                if (taskContainerDelta.getElement() instanceof org.eclipse.mylyn.tasks.core.ITask) {
                    final org.eclipse.mylyn.internal.tasks.core.AbstractTask updateTask = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (taskContainerDelta.getElement()));
                    if (((updateTask != null) && (getTask() != null)) && updateTask.getHandleIdentifier().equals(getTask().getHandleIdentifier())) {
                        if ((org.eclipse.ui.PlatformUI.getWorkbench() != null) && (!org.eclipse.ui.PlatformUI.getWorkbench().isClosing())) {
                            org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                                public void run() {
                                    refresh(false);
                                }
                            });
                        }
                    }
                }
            }
        }
    };

    private final org.eclipse.mylyn.tasks.core.ITaskActivationListener activationListener = new org.eclipse.mylyn.tasks.core.TaskActivationAdapter() {
        @java.lang.Override
        public void taskActivated(org.eclipse.mylyn.tasks.core.ITask task) {
            if (task.equals(getTask())) {
                org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                    public void run() {
                        if ((activeTimeText != null) && (!activeTimeText.isDisposed())) {
                            updateActiveTime();
                        }
                    }
                });
            }
        }

        @java.lang.Override
        public void taskDeactivated(org.eclipse.mylyn.tasks.core.ITask task) {
            if (task.equals(getTask())) {
                org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                    public void run() {
                        if ((activeTimeText != null) && (!activeTimeText.isDisposed())) {
                            updateActiveTime();
                        }
                    }
                });
            }
        }
    };

    private final org.eclipse.mylyn.tasks.core.ITaskActivityListener timingListener = new org.eclipse.mylyn.tasks.core.TaskActivityAdapter() {
        @java.lang.Override
        public void elapsedTimeUpdated(org.eclipse.mylyn.tasks.core.ITask task, long newElapsedTime) {
            if (task.equals(PlanningPart.this.getTask())) {
                org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                    public void run() {
                        if ((activeTimeText != null) && (!activeTimeText.isDisposed())) {
                            updateActiveTime();
                        }
                    }
                });
            }
        }
    };

    private final org.eclipse.jface.util.IPropertyChangeListener ACTIVITY_PROPERTY_LISTENER = new org.eclipse.jface.util.IPropertyChangeListener() {
        public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
            if (event.getProperty().equals(org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin.ACTIVITY_TRACKING_ENABLED)) {
                org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                    public void run() {
                        if ((activeTimeText != null) && (!activeTimeText.isDisposed())) {
                            updateActiveTime();
                        }
                    }
                });
            }
        }
    };

    private org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport textSupport;

    private org.eclipse.mylyn.tasks.ui.editors.TaskFormPage page;

    private org.eclipse.swt.widgets.Composite activeTimeComposite;

    private org.eclipse.jface.action.ToolBarManager toolBarManager;

    private boolean needsNotes;

    private boolean alwaysExpand;

    private org.eclipse.swt.widgets.Composite sectionClient;

    private boolean activeTimeEnabled;

    private org.eclipse.swt.widgets.Label scheduledLabel;

    private org.eclipse.swt.widgets.Composite layoutControl;

    public PlanningPart(int sectionStyle) {
        super(sectionStyle, Messages.PersonalPart_Personal_Planning);
        this.activeTimeEnabled = true;
        this.needsNotes = true;
    }

    public void initialize(org.eclipse.ui.forms.IManagedForm managedForm, org.eclipse.mylyn.tasks.core.TaskRepository taskRepository, org.eclipse.mylyn.internal.tasks.core.AbstractTask task, boolean needsDueDate, org.eclipse.mylyn.tasks.ui.editors.TaskFormPage page, org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport textSupport) {
        super.initialize(managedForm, taskRepository, task);
        this.needsDueDate = needsDueDate;
        this.page = page;
        this.textSupport = textSupport;
    }

    private boolean notesEqual() {
        if ((getTask().getNotes() == null) && (notesString == null)) {
            return true;
        }
        if ((getTask().getNotes() != null) && (notesString != null)) {
            return getTask().getNotes().equals(notesString) || notesString.equals(org.eclipse.mylyn.internal.tasks.ui.editors.PlanningPart.PERSONAL_NOTES);
        }
        return false;
    }

    @java.lang.Override
    public void commit(boolean onSave) {
        org.eclipse.core.runtime.Assert.isNotNull(getTask());
        if (!notesEqual()) {
            getTask().setNotes(notesString);
            // XXX REFRESH THE TASLKIST
        }
        if ((scheduleDatePicker != null) && (scheduleDatePicker.getScheduledDate() != null)) {
            if (((getTask().getScheduledForDate() == null) || ((getTask().getScheduledForDate() != null) && (!scheduleDatePicker.getScheduledDate().equals(getTask().getScheduledForDate())))) || (getTask().getScheduledForDate() instanceof org.eclipse.mylyn.internal.tasks.core.DayDateRange)) {
                org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().setScheduledFor(getTask(), scheduleDatePicker.getScheduledDate());
                getTask().setReminded(false);
            }
        } else {
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().setScheduledFor(getTask(), null);
            getTask().setReminded(false);
        }
        if (estimatedTimeSpinner != null) {
            getTask().setEstimatedTimeHours(estimatedTimeSpinner.getSelection());
        }
        if ((dueDatePicker != null) && (dueDatePicker.getDate() != null)) {
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().setDueDate(getTask(), dueDatePicker.getDate().getTime());
        } else {
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().setDueDate(getTask(), null);
        }
        super.commit(onSave);
    }

    @java.lang.Override
    public org.eclipse.swt.widgets.Control createControl(org.eclipse.swt.widgets.Composite parent, final org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        this.notesString = getTask().getNotes();
        if (this.notesString == null) {
            this.notesString = "";// $NON-NLS-1$

        }
        boolean expand = isAlwaysExpand() || (notesString.length() > 0);
        final org.eclipse.ui.forms.widgets.Section section = createSection(parent, toolkit, expand);
        section.clientVerticalSpacing = 0;
        if (section.isExpanded()) {
            expandSection(toolkit, section);
        } else {
            section.addExpansionListener(new org.eclipse.ui.forms.events.ExpansionAdapter() {
                @java.lang.Override
                public void expansionStateChanged(org.eclipse.ui.forms.events.ExpansionEvent event) {
                    if (sectionClient == null) {
                        expandSection(toolkit, section);
                        if (page instanceof org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage) {
                            ((org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage) (page)).reflow();
                        }
                    }
                }
            });
        }
        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().addChangeListener(TASK_LIST_LISTENER);
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().addActivityListener(timingListener);
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().addActivationListener(activationListener);
        org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(ACTIVITY_PROPERTY_LISTENER);
        setSection(toolkit, section);
        return section;
    }

    private void expandSection(org.eclipse.ui.forms.widgets.FormToolkit toolkit, org.eclipse.ui.forms.widgets.Section section) {
        sectionClient = toolkit.createComposite(section);
        org.eclipse.jface.layout.GridLayoutFactory.fillDefaults().applyTo(sectionClient);
        // create nested composite with GridData to enable resizing behavior of maximize action
        layoutControl = toolkit.createComposite(sectionClient);
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().grab(true, true).applyTo(layoutControl);
        org.eclipse.swt.layout.GridLayout layout = org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.createSectionClientLayout();
        layout.numColumns = (needsDueDate) ? 6 : 4;
        layoutControl.setLayout(layout);
        createScheduledDatePicker(toolkit, layoutControl);
        // disable due date picker if it's a repository due date
        if (needsDueDate) {
            createDueDatePicker(toolkit, layoutControl);
        }
        createEstimatedTime(toolkit, layoutControl);
        if (needsNotes()) {
            createNotesArea(toolkit, layoutControl, layout.numColumns);
        }
        createActiveTime(toolkit, layoutControl, layout.numColumns);
        toolkit.paintBordersFor(sectionClient);
        section.setClient(sectionClient);
        org.eclipse.mylyn.commons.ui.CommonUiUtil.setMenu(sectionClient, section.getParent().getMenu());
    }

    private void createNotesArea(final org.eclipse.ui.forms.widgets.FormToolkit toolkit, org.eclipse.swt.widgets.Composite parent, int numColumns) {
        org.eclipse.swt.widgets.Composite composite = toolkit.createComposite(parent);
        org.eclipse.swt.layout.GridLayout layout = new org.eclipse.swt.layout.GridLayout();
        layout.numColumns = 1;
        layout.marginWidth = 1;
        composite.setLayout(layout);
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().span(numColumns, org.eclipse.swt.SWT.DEFAULT).grab(true, true).applyTo(composite);
        if (page != null) {
            org.eclipse.ui.contexts.IContextService contextService = ((org.eclipse.ui.contexts.IContextService) (page.getEditorSite().getService(org.eclipse.ui.contexts.IContextService.class)));
            if (contextService != null) {
                org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorExtension extension = org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.getTaskEditorExtension(getRepository());
                if (extension != null) {
                    noteEditor = new org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor(getRepository(), ((org.eclipse.swt.SWT.FLAT | org.eclipse.swt.SWT.MULTI) | org.eclipse.swt.SWT.WRAP) | org.eclipse.swt.SWT.V_SCROLL, contextService, extension, getTask());
                }
            }
        }
        if (noteEditor == null) {
            noteEditor = new org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor(getRepository(), ((org.eclipse.swt.SWT.FLAT | org.eclipse.swt.SWT.MULTI) | org.eclipse.swt.SWT.WRAP) | org.eclipse.swt.SWT.V_SCROLL, null, null, getTask());
        }
        noteEditor.setSpellCheckingEnabled(true);
        noteEditor.createControl(composite, toolkit);
        noteEditor.setText(notesString);
        noteEditor.getControl().setLayoutData(org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.getTextControlLayoutData(page, noteEditor.getViewer().getControl(), true));
        noteEditor.getControl().setData(org.eclipse.ui.forms.widgets.FormToolkit.KEY_DRAW_BORDER, org.eclipse.ui.forms.widgets.FormToolkit.TREE_BORDER);
        noteEditor.setReadOnly(false);
        if (textSupport != null) {
            textSupport.install(noteEditor.getViewer(), true);
        }
        noteEditor.getViewer().addTextListener(new org.eclipse.jface.text.ITextListener() {
            public void textChanged(org.eclipse.jface.text.TextEvent event) {
                notesString = (org.eclipse.mylyn.internal.tasks.ui.editors.PlanningPart.PERSONAL_NOTES.equals(noteEditor.getText())) ? "" : noteEditor.getText();// $NON-NLS-1$

                if (!notesEqual()) {
                    markDirty();
                }
            }
        });
        addNotesLabelText(toolkit, composite);
        toolkit.paintBordersFor(composite);
    }

    private void addNotesLabelText(final org.eclipse.ui.forms.widgets.FormToolkit toolkit, final org.eclipse.swt.widgets.Composite composite) {
        if (!noteEditor.getViewer().getTextWidget().isFocusControl()) {
            setNotesLabelText(composite);
        }
        noteEditor.getViewer().getTextWidget().addFocusListener(new org.eclipse.swt.events.FocusListener() {
            @java.lang.Override
            public void focusGained(org.eclipse.swt.events.FocusEvent e) {
                if (org.eclipse.mylyn.internal.tasks.ui.editors.PlanningPart.PERSONAL_NOTES.equals(noteEditor.getText())) {
                    noteEditor.setText("");// $NON-NLS-1$

                    if (noteEditor.getViewer() != null) {
                        noteEditor.getViewer().getTextWidget().setForeground(toolkit.getColors().getForeground());
                    }
                }
            }

            @java.lang.Override
            public void focusLost(org.eclipse.swt.events.FocusEvent e) {
                setNotesLabelText(composite);
            }
        });
    }

    private void setNotesLabelText(org.eclipse.swt.widgets.Composite composite) {
        if (notesString.length() == 0) {
            notesString = org.eclipse.mylyn.internal.tasks.ui.editors.PlanningPart.PERSONAL_NOTES;
            noteEditor.setText(notesString);
            if (noteEditor.getViewer() != null) {
                noteEditor.getViewer().getTextWidget().setForeground(composite.getShell().getDisplay().getSystemColor(org.eclipse.swt.SWT.COLOR_GRAY));
            }
        }
    }

    private void createActiveTime(org.eclipse.ui.forms.widgets.FormToolkit toolkit, org.eclipse.swt.widgets.Composite toolbarComposite, int numColumns) {
        activeTimeComposite = toolkit.createComposite(toolbarComposite);
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().span(numColumns, org.eclipse.swt.SWT.DEFAULT).grab(false, false).applyTo(activeTimeComposite);
        activeTimeComposite.setBackground(null);
        activeTimeComposite.setBackgroundMode(org.eclipse.swt.SWT.INHERIT_FORCE);
        org.eclipse.swt.layout.RowLayout rowLayout = new org.eclipse.swt.layout.RowLayout();
        rowLayout.center = true;
        rowLayout.marginTop = 0;
        rowLayout.marginBottom = 0;
        rowLayout.marginLeft = 0;
        rowLayout.marginRight = 0;
        activeTimeComposite.setLayout(rowLayout);
        java.lang.String labelString;
        if (org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin.getDefault().isTrackingOsTime()) {
            labelString = Messages.PlanningPart_Active_time_Label;
        } else {
            java.lang.String productName = org.eclipse.mylyn.commons.ui.CommonUiUtil.getProductName(Messages.PlanningPart_Default_Product);
            labelString = org.eclipse.osgi.util.NLS.bind(Messages.PlanningPart_Active_time_in_Product_Label, productName);
        }
        org.eclipse.swt.widgets.Label label = toolkit.createLabel(activeTimeComposite, labelString);
        label.setForeground(toolkit.getColors().getColor(org.eclipse.ui.forms.IFormColors.TITLE));
        label.setToolTipText(Messages.TaskEditorPlanningPart_Time_working_on_this_task);
        label.setBackground(null);
        activeTimeText = new org.eclipse.swt.widgets.Text(activeTimeComposite, org.eclipse.swt.SWT.FLAT | org.eclipse.swt.SWT.READ_ONLY);
        activeTimeText.setText("00:00");// $NON-NLS-1$

        activeTimeText.setFont(EditorUtil.TEXT_FONT);
        activeTimeText.setData(org.eclipse.ui.forms.widgets.FormToolkit.KEY_DRAW_BORDER, java.lang.Boolean.FALSE);
        toolkit.adapt(activeTimeText, true, false);
        activeTimeText.setForeground(toolkit.getColors().getColor(org.eclipse.ui.forms.IFormColors.TITLE));
        activeTimeText.setBackground(null);
        activeTimeText.setEditable(false);
        org.eclipse.ui.forms.widgets.ImageHyperlink resetActivityTimeButton = toolkit.createImageHyperlink(activeTimeComposite, org.eclipse.swt.SWT.NONE);
        resetActivityTimeButton.setBackground(null);
        resetActivityTimeButton.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.FIND_CLEAR_DISABLED));
        resetActivityTimeButton.setHoverImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.FIND_CLEAR));
        resetActivityTimeButton.setToolTipText(Messages.PlanningPart_Reset_Active_Time);
        resetActivityTimeButton.addHyperlinkListener(new org.eclipse.ui.forms.events.HyperlinkAdapter() {
            @java.lang.Override
            public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                if (org.eclipse.jface.dialogs.MessageDialog.openConfirm(getControl().getShell(), Messages.TaskEditorPlanningPart_Confirm_Activity_Time_Deletion, Messages.TaskEditorPlanningPart_Do_you_wish_to_reset_your_activity_time_on_this_task_)) {
                    org.eclipse.mylyn.monitor.ui.MonitorUi.getActivityContextManager().removeActivityTime(getTask().getHandleIdentifier(), 0L, java.lang.System.currentTimeMillis());
                }
            }
        });
        updateActiveTime();
    }

    private void updateActiveTime() {
        boolean show = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.isActivityTrackingEnabled() && isActiveTimeEnabled();
        long elapsedTime = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getActiveTime(getTask());
        boolean visible = (activeTimeComposite != null) && activeTimeComposite.isVisible();
        if (show && ((elapsedTime > 0) || getTask().isActive())) {
            if ((activeTimeComposite != null) && (!activeTimeComposite.isVisible())) {
                activeTimeComposite.setVisible(true);
                ((org.eclipse.swt.layout.GridData) (activeTimeComposite.getLayoutData())).exclude = false;
                activeTimeComposite.getParent().layout();
            }
            java.lang.String elapsedTimeString = org.eclipse.mylyn.commons.core.DateUtil.getFormattedDurationShort(elapsedTime);
            if (elapsedTimeString.equals("")) {
                // $NON-NLS-1$
                elapsedTimeString = Messages.TaskEditorPlanningPart_0_SECOUNDS;
            }
            activeTimeText.setText(elapsedTimeString);
        } else if (activeTimeComposite != null) {
            ((org.eclipse.swt.layout.GridData) (activeTimeComposite.getLayoutData())).exclude = true;
            activeTimeComposite.getParent().layout();
            activeTimeComposite.setVisible(false);
        }
        if ((!needsNotes()) && (visible != ((activeTimeComposite != null) && activeTimeComposite.isVisible()))) {
            if (page instanceof org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage) {
                ((org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage) (page)).reflow();
            }
        }
    }

    private org.eclipse.swt.widgets.Composite createComposite(org.eclipse.swt.widgets.Composite parent, int col, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        org.eclipse.swt.widgets.Composite nameValueComp = toolkit.createComposite(parent);
        org.eclipse.swt.layout.GridLayout layout = new org.eclipse.swt.layout.GridLayout(3, false);
        layout.marginHeight = 3;
        nameValueComp.setLayout(layout);
        return nameValueComp;
    }

    private void createDueDatePicker(org.eclipse.ui.forms.widgets.FormToolkit toolkit, org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.widgets.Label label = toolkit.createLabel(parent, Messages.TaskEditorPlanningPart_Due);
        label.setForeground(toolkit.getColors().getColor(org.eclipse.ui.forms.IFormColors.TITLE));
        org.eclipse.swt.widgets.Composite composite = createComposite(parent, 1, toolkit);
        dueDatePicker = new org.eclipse.mylyn.commons.workbench.forms.DatePicker(composite, org.eclipse.swt.SWT.FLAT, org.eclipse.mylyn.commons.workbench.forms.DatePicker.LABEL_CHOOSE, true, 0);
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().hint(org.eclipse.mylyn.internal.tasks.ui.editors.PlanningPart.CONTROL_WIDTH, org.eclipse.swt.SWT.DEFAULT).applyTo(dueDatePicker);
        dueDatePicker.setBackground(org.eclipse.swt.widgets.Display.getDefault().getSystemColor(org.eclipse.swt.SWT.COLOR_WHITE));
        dueDatePicker.setData(org.eclipse.ui.forms.widgets.FormToolkit.KEY_DRAW_BORDER, org.eclipse.ui.forms.widgets.FormToolkit.TREE_BORDER);
        if (getTask().getDueDate() != null) {
            java.util.Calendar calendar = org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil.getCalendar();
            calendar.setTime(getTask().getDueDate());
            dueDatePicker.setDate(calendar);
        }
        dueDatePicker.addPickerSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent arg0) {
                markDirty();
            }
        });
        toolkit.adapt(dueDatePicker, false, false);
        toolkit.paintBordersFor(composite);
    }

    private void createEstimatedTime(org.eclipse.ui.forms.widgets.FormToolkit toolkit, org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.widgets.Label label = toolkit.createLabel(parent, Messages.TaskEditorPlanningPart_Estimated);
        label.setForeground(toolkit.getColors().getColor(org.eclipse.ui.forms.IFormColors.TITLE));
        org.eclipse.swt.widgets.Composite composite = createComposite(parent, 2, toolkit);
        // Estimated time
        estimatedTimeSpinner = new org.eclipse.swt.widgets.Spinner(composite, org.eclipse.swt.SWT.FLAT);
        estimatedTimeSpinner.setDigits(0);
        estimatedTimeSpinner.setMaximum(10000);
        estimatedTimeSpinner.setMinimum(0);
        estimatedTimeSpinner.setIncrement(1);
        estimatedTimeSpinner.setSelection(getTask().getEstimatedTimeHours());
        if (!org.eclipse.mylyn.commons.ui.PlatformUiUtil.spinnerHasNativeBorder()) {
            estimatedTimeSpinner.setData(org.eclipse.ui.forms.widgets.FormToolkit.KEY_DRAW_BORDER, org.eclipse.ui.forms.widgets.FormToolkit.TREE_BORDER);
        }
        estimatedTimeSpinner.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
            public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
                if (getTask().getEstimatedTimeHours() != estimatedTimeSpinner.getSelection()) {
                    markDirty();
                }
            }
        });
        // ImageHyperlink clearEstimated = toolkit.createImageHyperlink(composite, SWT.NONE);
        // clearEstimated.setImage(CommonImages.getImage(CommonImages.FIND_CLEAR_DISABLED));
        // clearEstimated.setHoverImage(CommonImages.getImage(CommonImages.FIND_CLEAR));
        // clearEstimated.setToolTipText(Messages.TaskEditorPlanningPart_Clear);
        // clearEstimated.addHyperlinkListener(new HyperlinkAdapter() {
        // @Override
        // public void linkActivated(HyperlinkEvent e) {
        // estimatedTime.setSelection(0);
        // markDirty();
        // }
        // });
        toolkit.paintBordersFor(composite);
    }

    private void createScheduledDatePicker(org.eclipse.ui.forms.widgets.FormToolkit toolkit, org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.widgets.Label label = toolkit.createLabel(parent, Messages.TaskEditorPlanningPart_Scheduled);
        label.setForeground(toolkit.getColors().getColor(org.eclipse.ui.forms.IFormColors.TITLE));
        org.eclipse.swt.widgets.Composite composite = createComposite(parent, 2, toolkit);
        scheduleDatePicker = new org.eclipse.mylyn.internal.tasks.ui.ScheduleDatePicker(composite, getTask(), org.eclipse.swt.SWT.FLAT);
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().hint(org.eclipse.mylyn.internal.tasks.ui.editors.PlanningPart.CONTROL_WIDTH, org.eclipse.swt.SWT.DEFAULT).applyTo(scheduleDatePicker);
        scheduleDatePicker.setData(org.eclipse.ui.forms.widgets.FormToolkit.KEY_DRAW_BORDER, org.eclipse.ui.forms.widgets.FormToolkit.TREE_BORDER);
        toolkit.adapt(scheduleDatePicker, false, false);
        toolkit.paintBordersFor(composite);
        scheduleDatePicker.setBackground(org.eclipse.swt.widgets.Display.getDefault().getSystemColor(org.eclipse.swt.SWT.COLOR_WHITE));
        scheduleDatePicker.addPickerSelectionListener(new org.eclipse.swt.events.SelectionListener() {
            public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent arg0) {
                // ignore
            }

            public void widgetSelected(org.eclipse.swt.events.SelectionEvent arg0) {
                markDirty();
            }
        });
    }

    @java.lang.Override
    public void dispose() {
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().removeActivationListener(activationListener);
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().removeActivityListener(timingListener);
        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().removeChangeListener(TASK_LIST_LISTENER);
        org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(ACTIVITY_PROPERTY_LISTENER);
        if (toolBarManager != null) {
            toolBarManager.dispose();
        }
    }

    @java.lang.Override
    protected void setSection(org.eclipse.ui.forms.widgets.FormToolkit toolkit, org.eclipse.ui.forms.widgets.Section section) {
        super.setSection(toolkit, section);
        if (section.getTextClient() == null) {
            org.eclipse.swt.widgets.Composite toolbarComposite = toolkit.createComposite(section);
            toolbarComposite.setBackground(null);
            org.eclipse.swt.layout.RowLayout rowLayout = new org.eclipse.swt.layout.RowLayout();
            rowLayout.marginLeft = 0;
            rowLayout.marginRight = 0;
            rowLayout.marginTop = 0;
            rowLayout.marginBottom = 0;
            rowLayout.center = true;
            toolbarComposite.setLayout(rowLayout);
            createScheduledLabel(toolbarComposite, section, toolkit);
            org.eclipse.jface.action.ToolBarManager toolBarManager = new org.eclipse.jface.action.ToolBarManager(org.eclipse.swt.SWT.FLAT);
            fillToolBar(toolBarManager);
            // TODO toolBarManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
            if (toolBarManager.getSize() > 0) {
                toolBarManager.createControl(toolbarComposite);
                section.clientVerticalSpacing = 0;
                section.descriptionVerticalSpacing = 0;
            }
            section.setTextClient(toolbarComposite);
        }
    }

    private void createScheduledLabel(org.eclipse.swt.widgets.Composite composite, org.eclipse.ui.forms.widgets.Section section, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        scheduledLabel = toolkit.createLabel(composite, "");// $NON-NLS-1$

        scheduledLabel.setForeground(toolkit.getColors().getColor(org.eclipse.ui.forms.IFormColors.TITLE));
        scheduledLabel.setBackground(null);
        updateScheduledLabel(section.isExpanded());
        section.addExpansionListener(new org.eclipse.ui.forms.events.ExpansionAdapter() {
            @java.lang.Override
            public void expansionStateChanging(org.eclipse.ui.forms.events.ExpansionEvent event) {
                updateScheduledLabel(event.getState());
            }
        });
    }

    private void updateScheduledLabel(boolean sectionIsExpanded) {
        if ((scheduledLabel != null) && (!scheduledLabel.isDisposed())) {
            if ((!sectionIsExpanded) && (!getTask().isCompleted())) {
                org.eclipse.mylyn.internal.tasks.core.DateRange date = getTask().getScheduledForDate();
                if (date != null) {
                    scheduledLabel.setText(org.eclipse.mylyn.internal.tasks.ui.editors.PlanningPart.getLabel(date));
                    scheduledLabel.setToolTipText(org.eclipse.osgi.util.NLS.bind(Messages.PlanningPart_Scheduled_for_X_Tooltip, date.toString()));
                } else {
                    scheduledLabel.setText("");// $NON-NLS-1$

                    scheduledLabel.setToolTipText(null);
                }
                if (!scheduledLabel.isVisible()) {
                    scheduledLabel.setVisible(true);
                }
                scheduledLabel.getParent().getParent().layout(true);
            } else if (scheduledLabel.isVisible()) {
                scheduledLabel.setVisible(false);
                scheduledLabel.getParent().getParent().layout(true);
            }
        }
    }

    /**
     * Returns a short label that describes <code>dateRage</code>. Public for testing.
     */
    public static java.lang.String getLabel(org.eclipse.mylyn.internal.tasks.core.DateRange dateRange) {
        if (dateRange instanceof org.eclipse.mylyn.internal.tasks.core.WeekDateRange) {
            if (dateRange.isPast() || dateRange.isPresent()) {
                return Messages.PlanningPart_This_Week;
            } else if (org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil.getNextWeek().compareTo(dateRange) == 0) {
                return Messages.PlanningPart_Next_Week;
            }
        } else {
            if (dateRange.isPast() || dateRange.isPresent()) {
                return Messages.PlanningPart_Today;
            }
            if (org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil.getCurrentWeek().includes(dateRange)) {
                return Messages.PlanningPart_This_Week;
            }
            if (org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil.getNextWeek().includes(dateRange)) {
                return Messages.PlanningPart_Next_Week;
            }
        }
        return Messages.PlanningPart_Later;
    }

    protected void fillToolBar(org.eclipse.jface.action.ToolBarManager toolBarManager) {
    }

    public boolean needsNotes() {
        return needsNotes;
    }

    public void setNeedsNotes(boolean needsNotes) {
        this.needsNotes = needsNotes;
    }

    public boolean isAlwaysExpand() {
        return alwaysExpand;
    }

    public void setAlwaysExpand(boolean alwaysExpand) {
        this.alwaysExpand = alwaysExpand;
    }

    @java.lang.Override
    protected void refresh(boolean discardChanges) {
        if ((scheduleDatePicker != null) && (!scheduleDatePicker.isDisposed())) {
            if (getTask().getScheduledForDate() != null) {
                scheduleDatePicker.setScheduledDate(getTask().getScheduledForDate());
            } else {
                scheduleDatePicker.setScheduledDate(null);
            }
        }
        if ((scheduledLabel != null) && (!scheduledLabel.isDisposed())) {
            updateScheduledLabel(getSection().isExpanded());
        }
        if ((estimatedTimeSpinner != null) && (!estimatedTimeSpinner.isDisposed())) {
            estimatedTimeSpinner.setSelection(getTask().getEstimatedTimeHours());
        }
        // TODO refresh notes
    }

    public boolean isActiveTimeEnabled() {
        return activeTimeEnabled;
    }

    public void setActiveTimeEnabled(boolean activeTimeEnabled) {
        this.activeTimeEnabled = activeTimeEnabled;
        if ((activeTimeComposite != null) && (!activeTimeComposite.isDisposed())) {
            updateActiveTime();
        }
    }

    public org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor getNoteEditor() {
        return noteEditor;
    }

    public org.eclipse.swt.widgets.Control getLayoutControl() {
        return layoutControl;
    }
}