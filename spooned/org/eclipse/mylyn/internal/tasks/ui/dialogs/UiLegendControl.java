/**
 * *****************************************************************************
 * Copyright (c) 2004, 2016 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.dialogs;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.commons.ui.compatibility.CommonFonts;
import org.eclipse.mylyn.commons.ui.compatibility.CommonThemes;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.ITask.PriorityLevel;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.ITasksUiConstants;
import org.eclipse.mylyn.tasks.ui.LegendElement;
import org.eclipse.mylyn.tasks.ui.TaskElementLabelProvider;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.themes.IThemeManager;
/**
 *
 * @author Mik Kersten
 * @author Leo Dos Santos
 */
public class UiLegendControl extends org.eclipse.swt.widgets.Composite {
    private final org.eclipse.ui.forms.widgets.FormToolkit toolkit;

    private org.eclipse.jface.window.Window window = null;

    private final org.eclipse.mylyn.tasks.ui.TaskElementLabelProvider labelProvider = new org.eclipse.mylyn.tasks.ui.TaskElementLabelProvider(false);

    private final org.eclipse.ui.themes.IThemeManager themeManager = org.eclipse.ui.PlatformUI.getWorkbench().getThemeManager();

    private final java.util.ArrayList<org.eclipse.mylyn.tasks.ui.LegendElement> legendElements = new java.util.ArrayList<org.eclipse.mylyn.tasks.ui.LegendElement>();

    public UiLegendControl(org.eclipse.swt.widgets.Composite parent, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        this(parent, toolkit, true, org.eclipse.swt.SWT.VERTICAL);
    }

    public UiLegendControl(org.eclipse.swt.widgets.Composite parent, org.eclipse.ui.forms.widgets.FormToolkit toolkit, boolean showConnectors, int style) {
        super(parent, org.eclipse.swt.SWT.NONE);
        this.toolkit = toolkit;
        toolkit.adapt(this);
        addDisposeListener(new org.eclipse.swt.events.DisposeListener() {
            public void widgetDisposed(org.eclipse.swt.events.DisposeEvent e) {
                doDispose();
            }
        });
        org.eclipse.ui.forms.widgets.TableWrapLayout layout = new org.eclipse.ui.forms.widgets.TableWrapLayout();
        layout.leftMargin = 0;
        layout.rightMargin = 0;
        layout.topMargin = 0;
        layout.bottomMargin = 0;
        if (style == org.eclipse.swt.SWT.DEFAULT) {
            createContentsVertical(layout, showConnectors);
        } else if ((style & org.eclipse.swt.SWT.HORIZONTAL) != 0) {
            createContentsHorizontal(layout, showConnectors);
        } else {
            createContentsVertical(layout, showConnectors);
        }
        setLayout(layout);
        setLayoutData(new org.eclipse.ui.forms.widgets.TableWrapData(org.eclipse.ui.forms.widgets.TableWrapData.FILL_GRAB, org.eclipse.ui.forms.widgets.TableWrapData.FILL_GRAB));
    }

    private void doDispose() {
        for (org.eclipse.mylyn.tasks.ui.LegendElement element : legendElements) {
            element.dispose();
        }
        if (labelProvider != null) {
            labelProvider.dispose();
        }
    }

    public void setWindow(org.eclipse.jface.window.Window window) {
        this.window = window;
    }

    public boolean close() {
        if (window != null) {
            return window.close();
        } else {
            return false;
        }
    }

    private void createContentsHorizontal(org.eclipse.ui.forms.widgets.TableWrapLayout layout, boolean showConnectors) {
        layout.numColumns = 2;
        createTasksPrioritiesSection(this);
        createContextSection(this);
        createActivitySection(this);
        createSynchronizationSection(this);
        org.eclipse.swt.widgets.Composite subComp = toolkit.createComposite(this);
        org.eclipse.ui.forms.widgets.TableWrapLayout subLayout = new org.eclipse.ui.forms.widgets.TableWrapLayout();
        subLayout.topMargin = 0;
        subLayout.bottomMargin = 0;
        subLayout.leftMargin = 0;
        subLayout.rightMargin = 0;
        subComp.setLayout(subLayout);
        subComp.setLayoutData(new org.eclipse.ui.forms.widgets.TableWrapData(org.eclipse.ui.forms.widgets.TableWrapData.FILL_GRAB, org.eclipse.ui.forms.widgets.TableWrapData.FILL_GRAB, 1, 2));
        if (showConnectors) {
            createConnectorsSection(subComp);
        }
        createGettingStartedSection(subComp);
    }

    private void createContentsVertical(org.eclipse.ui.forms.widgets.TableWrapLayout layout, boolean showConnectors) {
        layout.numColumns = 1;
        createTasksPrioritiesSection(this);
        createActivitySection(this);
        createContextSection(this);
        createSynchronizationSection(this);
        if (showConnectors) {
            createConnectorsSection(this);
        }
        createGettingStartedSection(this);
    }

    private void createTasksPrioritiesSection(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.ui.forms.widgets.TableWrapLayout layout = new org.eclipse.ui.forms.widgets.TableWrapLayout();
        layout.numColumns = 2;
        layout.makeColumnsEqualWidth = true;
        layout.leftMargin = 0;
        layout.rightMargin = 0;
        layout.topMargin = 0;
        layout.bottomMargin = 0;
        org.eclipse.swt.widgets.Composite composite = toolkit.createComposite(parent);
        composite.setLayout(layout);
        composite.setLayoutData(new org.eclipse.ui.forms.widgets.TableWrapData(org.eclipse.ui.forms.widgets.TableWrapData.FILL_GRAB));
        createTasksSection(composite);
        createPrioritiesSection(composite);
    }

    private void createTasksSection(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.ui.forms.widgets.Section tasksSection = toolkit.createSection(parent, org.eclipse.ui.forms.widgets.ExpandableComposite.TITLE_BAR);
        tasksSection.setText(Messages.UiLegendControl_Tasks);
        tasksSection.setLayout(new org.eclipse.ui.forms.widgets.TableWrapLayout());
        tasksSection.setLayoutData(new org.eclipse.ui.forms.widgets.TableWrapData(org.eclipse.ui.forms.widgets.TableWrapData.FILL_GRAB));
        org.eclipse.ui.forms.widgets.TableWrapLayout layout = new org.eclipse.ui.forms.widgets.TableWrapLayout();
        layout.numColumns = 2;
        layout.makeColumnsEqualWidth = false;
        layout.verticalSpacing = 1;
        layout.topMargin = 1;
        layout.bottomMargin = 1;
        org.eclipse.swt.widgets.Composite tasksClient = toolkit.createComposite(tasksSection);
        tasksClient.setLayout(layout);
        tasksClient.setLayoutData(new org.eclipse.ui.forms.widgets.TableWrapData(org.eclipse.ui.forms.widgets.TableWrapData.FILL_GRAB));
        tasksSection.setClient(tasksClient);
        org.eclipse.swt.widgets.Label imageLabel;
        imageLabel = toolkit.createLabel(tasksClient, "");// $NON-NLS-1$

        imageLabel.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.TASK));
        toolkit.createLabel(tasksClient, Messages.UiLegendControl_Task);
        imageLabel = toolkit.createLabel(tasksClient, "");// $NON-NLS-1$

        imageLabel.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.TASK_OWNED));
        toolkit.createLabel(tasksClient, Messages.UiLegendControl_Task_Owned);
        imageLabel = toolkit.createLabel(tasksClient, "");// $NON-NLS-1$

        imageLabel.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.CATEGORY));
        toolkit.createLabel(tasksClient, Messages.UiLegendControl_Category);
        imageLabel = toolkit.createLabel(tasksClient, "");// $NON-NLS-1$

        imageLabel.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.QUERY));
        toolkit.createLabel(tasksClient, Messages.UiLegendControl_Query);
        imageLabel = toolkit.createLabel(tasksClient, "");// $NON-NLS-1$

        imageLabel.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.CALENDAR));
        toolkit.createLabel(tasksClient, Messages.UiLegendControl_Date_range);
        imageLabel = toolkit.createLabel(tasksClient, "");// $NON-NLS-1$

        imageLabel.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.BLANK));
        org.eclipse.ui.forms.widgets.Hyperlink openView = toolkit.createHyperlink(tasksClient, Messages.UiLegendControl_Open_Task_List_, org.eclipse.swt.SWT.WRAP);
        openView.addHyperlinkListener(new org.eclipse.ui.forms.events.IHyperlinkListener() {
            public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                close();
                org.eclipse.mylyn.tasks.ui.TasksUiUtil.openTasksViewInActivePerspective();
            }

            public void linkEntered(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                // ignore
            }

            public void linkExited(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                // ignore
            }
        });
    }

    private void createPrioritiesSection(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.ui.forms.widgets.Section prioritiesSection = toolkit.createSection(parent, org.eclipse.ui.forms.widgets.ExpandableComposite.TITLE_BAR);
        prioritiesSection.setText(Messages.UiLegendControl_Priorities);
        prioritiesSection.setLayout(new org.eclipse.ui.forms.widgets.TableWrapLayout());
        prioritiesSection.setLayoutData(new org.eclipse.ui.forms.widgets.TableWrapData(org.eclipse.ui.forms.widgets.TableWrapData.FILL_GRAB));
        org.eclipse.ui.forms.widgets.TableWrapLayout layout = new org.eclipse.ui.forms.widgets.TableWrapLayout();
        layout.numColumns = 2;
        layout.makeColumnsEqualWidth = false;
        layout.verticalSpacing = 1;
        layout.topMargin = 1;
        layout.bottomMargin = 1;
        org.eclipse.swt.widgets.Composite prioritiesClient = toolkit.createComposite(prioritiesSection);
        prioritiesClient.setLayout(layout);
        prioritiesClient.setLayoutData(new org.eclipse.ui.forms.widgets.TableWrapData(org.eclipse.ui.forms.widgets.TableWrapData.FILL_GRAB));
        prioritiesSection.setClient(prioritiesClient);
        org.eclipse.swt.widgets.Label imageLabel;
        imageLabel = toolkit.createLabel(prioritiesClient, "");// $NON-NLS-1$

        imageLabel.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.PRIORITY_1));
        toolkit.createLabel(prioritiesClient, org.eclipse.mylyn.tasks.core.ITask.PriorityLevel.P1.getDescription());
        imageLabel = toolkit.createLabel(prioritiesClient, "");// $NON-NLS-1$

        imageLabel.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.PRIORITY_2));
        toolkit.createLabel(prioritiesClient, org.eclipse.mylyn.tasks.core.ITask.PriorityLevel.P2.getDescription());
        imageLabel = toolkit.createLabel(prioritiesClient, "");// $NON-NLS-1$

        imageLabel.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.PRIORITY_3));
        toolkit.createLabel(prioritiesClient, org.eclipse.mylyn.tasks.core.ITask.PriorityLevel.P3.getDescription() + Messages.UiLegendControl__default_);
        imageLabel = toolkit.createLabel(prioritiesClient, "");// $NON-NLS-1$

        imageLabel.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.PRIORITY_4));
        toolkit.createLabel(prioritiesClient, org.eclipse.mylyn.tasks.core.ITask.PriorityLevel.P4.getDescription());
        imageLabel = toolkit.createLabel(prioritiesClient, "");// $NON-NLS-1$

        imageLabel.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.PRIORITY_5));
        toolkit.createLabel(prioritiesClient, org.eclipse.mylyn.tasks.core.ITask.PriorityLevel.P5.getDescription());
    }

    private void createActivitySection(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.ui.forms.widgets.Section activitySection = toolkit.createSection(parent, org.eclipse.ui.forms.widgets.ExpandableComposite.TITLE_BAR);
        activitySection.setText(Messages.UiLegendControl_Task_Activity);
        activitySection.setLayout(new org.eclipse.ui.forms.widgets.TableWrapLayout());
        activitySection.setLayoutData(new org.eclipse.ui.forms.widgets.TableWrapData(org.eclipse.ui.forms.widgets.TableWrapData.FILL_GRAB));
        org.eclipse.ui.forms.widgets.TableWrapLayout layout = new org.eclipse.ui.forms.widgets.TableWrapLayout();
        layout.numColumns = 2;
        layout.makeColumnsEqualWidth = false;
        layout.verticalSpacing = 1;
        layout.topMargin = 1;
        layout.bottomMargin = 1;
        org.eclipse.swt.widgets.Composite activityClient = toolkit.createComposite(activitySection);
        activityClient.setLayout(layout);
        activityClient.setLayoutData(new org.eclipse.ui.forms.widgets.TableWrapData(org.eclipse.ui.forms.widgets.TableWrapData.FILL_GRAB));
        activitySection.setClient(activityClient);
        org.eclipse.swt.widgets.Label imageLabel = toolkit.createLabel(activityClient, "");// $NON-NLS-1$

        imageLabel.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.TASK));
        org.eclipse.swt.widgets.Label labelToday = toolkit.createLabel(activityClient, Messages.UiLegendControl_Scheduled_for_today);
        labelToday.setForeground(themeManager.getCurrentTheme().getColorRegistry().get(org.eclipse.mylyn.commons.ui.compatibility.CommonThemes.COLOR_SCHEDULED_TODAY));
        imageLabel = toolkit.createLabel(activityClient, "");// $NON-NLS-1$

        imageLabel.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.TASK));
        org.eclipse.swt.widgets.Label labelOverdue = toolkit.createLabel(activityClient, Messages.UiLegendControl_Past_scheduled_date);
        labelOverdue.setForeground(themeManager.getCurrentTheme().getColorRegistry().get(org.eclipse.mylyn.commons.ui.compatibility.CommonThemes.COLOR_SCHEDULED_PAST));
        // imageLabel = toolkit.createLabel(activityClient, "");
        // imageLabel.setImage(TasksUiImages.getImage(TasksUiImages.TASK));
        // Label labelThisWeek = toolkit.createLabel(activityClient, "Scheduled for this
        // week");
        // labelThisWeek.setForeground(themeManager.getCurrentTheme().getColorRegistry().get(
        // TaskListColorsAndFonts.THEME_COLOR_TASK_THISWEEK_SCHEDULED));
        imageLabel = toolkit.createLabel(activityClient, "");// $NON-NLS-1$

        imageLabel.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.TASK));
        org.eclipse.swt.widgets.Label labelCompleted = toolkit.createLabel(activityClient, Messages.UiLegendControl_Completed);
        labelCompleted.setFont(org.eclipse.mylyn.commons.ui.compatibility.CommonFonts.STRIKETHROUGH);
        // labelCompleted.setForeground(TaskListColorsAndFonts.COLOR_TASK_COMPLETED);
        labelCompleted.setForeground(themeManager.getCurrentTheme().getColorRegistry().get(org.eclipse.mylyn.commons.ui.compatibility.CommonThemes.COLOR_COMPLETED));
        imageLabel = toolkit.createLabel(activityClient, "");// $NON-NLS-1$

        imageLabel.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.TASK));
        org.eclipse.swt.widgets.Label labelCompletedToday = toolkit.createLabel(activityClient, Messages.UiLegendControl_Completed_today);
        labelCompletedToday.setFont(org.eclipse.mylyn.commons.ui.compatibility.CommonFonts.STRIKETHROUGH);
        labelCompletedToday.setForeground(themeManager.getCurrentTheme().getColorRegistry().get(org.eclipse.mylyn.commons.ui.compatibility.CommonThemes.COLOR_COMPLETED_TODAY));
        imageLabel = toolkit.createLabel(activityClient, "");// $NON-NLS-1$

        imageLabel.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.OVERLAY_DATE_DUE));
        toolkit.createLabel(activityClient, Messages.UiLegendControl_Has_Due_date);
        imageLabel = toolkit.createLabel(activityClient, "");// $NON-NLS-1$

        imageLabel.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.OVERLAY_DATE_OVERDUE));
        org.eclipse.swt.widgets.Label textLabel = toolkit.createLabel(activityClient, Messages.UiLegendControl_Past_due_date);
        textLabel.setForeground(themeManager.getCurrentTheme().getColorRegistry().get(org.eclipse.mylyn.commons.ui.compatibility.CommonThemes.COLOR_OVERDUE));
        imageLabel = toolkit.createLabel(activityClient, "");// $NON-NLS-1$

        imageLabel.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.NOTES));
        toolkit.createLabel(activityClient, Messages.UiLegendControl_Notes);
        imageLabel = toolkit.createLabel(activityClient, "");// $NON-NLS-1$

        imageLabel.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.BLANK));
        org.eclipse.ui.forms.widgets.Hyperlink adjust = toolkit.createHyperlink(activityClient, Messages.UiLegendControl_Adjust_Colors_and_Fonts_, org.eclipse.swt.SWT.WRAP);
        adjust.addHyperlinkListener(new org.eclipse.ui.forms.events.IHyperlinkListener() {
            public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                org.eclipse.jface.preference.PreferenceDialog dlg = org.eclipse.ui.dialogs.PreferencesUtil.createPreferenceDialogOn(org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), org.eclipse.mylyn.tasks.ui.ITasksUiConstants.ID_PREFERENCES_COLORS_AND_FONTS, new java.lang.String[]{ org.eclipse.mylyn.tasks.ui.ITasksUiConstants.ID_PREFERENCES_COLORS_AND_FONTS }, null);
                dlg.open();
            }

            public void linkEntered(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                // ignore
            }

            public void linkExited(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                // ignore
            }
        });
    }

    private void createContextSection(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.ui.forms.widgets.Section contextSection = toolkit.createSection(parent, org.eclipse.ui.forms.widgets.ExpandableComposite.TITLE_BAR);
        contextSection.setText(Messages.UiLegendControl_Task_Context);
        contextSection.setLayout(new org.eclipse.ui.forms.widgets.TableWrapLayout());
        contextSection.setLayoutData(new org.eclipse.ui.forms.widgets.TableWrapData(org.eclipse.ui.forms.widgets.TableWrapData.FILL_GRAB));
        org.eclipse.ui.forms.widgets.TableWrapLayout layout = new org.eclipse.ui.forms.widgets.TableWrapLayout();
        layout.numColumns = 2;
        layout.makeColumnsEqualWidth = false;
        layout.verticalSpacing = 1;
        layout.topMargin = 1;
        layout.bottomMargin = 1;
        org.eclipse.swt.widgets.Composite contextClient = toolkit.createComposite(contextSection);
        contextClient.setLayout(layout);
        contextClient.setLayoutData(new org.eclipse.ui.forms.widgets.TableWrapData(org.eclipse.ui.forms.widgets.TableWrapData.FILL_GRAB));
        contextSection.setClient(contextClient);
        org.eclipse.swt.widgets.Label imageLabel;
        imageLabel = toolkit.createLabel(contextClient, "");// $NON-NLS-1$

        imageLabel.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.CONTEXT_FOCUS));
        toolkit.createLabel(contextClient, Messages.UiLegendControl_Focus_view_on_active_task);
        imageLabel = toolkit.createLabel(contextClient, "");// $NON-NLS-1$

        imageLabel.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.CONTEXT_INACTIVE_EMPTY));
        toolkit.createLabel(contextClient, Messages.UiLegendControl_Inactive_task_with_no_context);
        imageLabel = toolkit.createLabel(contextClient, "");// $NON-NLS-1$

        imageLabel.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.CONTEXT_INACTIVE));
        toolkit.createLabel(contextClient, Messages.UiLegendControl_Inactive_task_with_context);
        imageLabel = toolkit.createLabel(contextClient, "");// $NON-NLS-1$

        imageLabel.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.CONTEXT_ACTIVE));
        toolkit.createLabel(contextClient, Messages.UiLegendControl_Active_task);
    }

    private void createSynchronizationSection(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.ui.forms.widgets.Section synchroSection = toolkit.createSection(parent, org.eclipse.ui.forms.widgets.ExpandableComposite.TITLE_BAR);
        synchroSection.setText(Messages.UiLegendControl_Synchronization);
        synchroSection.setLayout(new org.eclipse.ui.forms.widgets.TableWrapLayout());
        synchroSection.setLayoutData(new org.eclipse.ui.forms.widgets.TableWrapData(org.eclipse.ui.forms.widgets.TableWrapData.FILL_GRAB));
        org.eclipse.ui.forms.widgets.TableWrapLayout layout = new org.eclipse.ui.forms.widgets.TableWrapLayout();
        layout.numColumns = 2;
        layout.makeColumnsEqualWidth = false;
        layout.verticalSpacing = 3;
        layout.topMargin = 1;
        layout.bottomMargin = 1;
        org.eclipse.swt.widgets.Composite synchroClient = toolkit.createComposite(synchroSection);
        synchroClient.setLayout(layout);
        synchroClient.setLayoutData(new org.eclipse.ui.forms.widgets.TableWrapData(org.eclipse.ui.forms.widgets.TableWrapData.FILL_GRAB));
        synchroSection.setClient(synchroClient);
        org.eclipse.swt.widgets.Label imageLabel;
        imageLabel = toolkit.createLabel(synchroClient, "");// $NON-NLS-1$

        imageLabel.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.OVERLAY_SYNC_INCOMMING_NEW));
        toolkit.createLabel(synchroClient, Messages.UiLegendControl_New_task);
        imageLabel = toolkit.createLabel(synchroClient, "");// $NON-NLS-1$

        imageLabel.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.OVERLAY_SYNC_INCOMMING));
        toolkit.createLabel(synchroClient, Messages.UiLegendControl_Incoming_changes);
        imageLabel = toolkit.createLabel(synchroClient, "");// $NON-NLS-1$

        imageLabel.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.OVERLAY_SYNC_OUTGOING));
        toolkit.createLabel(synchroClient, Messages.UiLegendControl_Outgoing_changes);
        imageLabel = toolkit.createLabel(synchroClient, "");// $NON-NLS-1$

        imageLabel.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.OVERLAY_SYNC_OUTGOING_NEW));
        toolkit.createLabel(synchroClient, Messages.UiLegendControl_Unsubmitted_outgoing_changes);
        imageLabel = toolkit.createLabel(synchroClient, "");// $NON-NLS-1$

        imageLabel.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.OVERLAY_SYNC_WARNING));
        toolkit.createLabel(synchroClient, Messages.UiLegendControl_Synchronization_failed);
        imageLabel = toolkit.createLabel(synchroClient, "");// $NON-NLS-1$

        imageLabel.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.OVERLAY_SYNC_ERROR));
        toolkit.createLabel(synchroClient, Messages.UiLegendControl_Synchronization_error);
        imageLabel = toolkit.createLabel(synchroClient, "");// $NON-NLS-1$

        imageLabel.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.OVERLAY_SYNC_CONFLICT));
        toolkit.createLabel(synchroClient, Messages.UiLegendControl_Conflicting_changes);
    }

    private void createConnectorsSection(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.ui.forms.widgets.TableWrapLayout layout = new org.eclipse.ui.forms.widgets.TableWrapLayout();
        layout.makeColumnsEqualWidth = true;
        layout.leftMargin = 0;
        layout.rightMargin = 0;
        layout.topMargin = 0;
        layout.bottomMargin = 0;
        org.eclipse.ui.forms.widgets.ScrolledForm composite = toolkit.createScrolledForm(parent);
        composite.getBody().setLayout(layout);
        org.eclipse.ui.forms.widgets.TableWrapData data = new org.eclipse.ui.forms.widgets.TableWrapData(org.eclipse.ui.forms.widgets.TableWrapData.FILL);
        composite.setLayoutData(data);
        java.util.List<org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector> connectors = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector>(org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnectors());
        java.util.Collections.sort(connectors, new java.util.Comparator<org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector>() {
            public int compare(org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector o1, org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector o2) {
                return o1.getLabel().compareToIgnoreCase(o2.getLabel());
            }
        });
        for (org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector : connectors) {
            if (org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositories(connector.getConnectorKind()).isEmpty()) {
                continue;
            }
            org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryConnectorUi(connector.getConnectorKind());
            if (connectorUi != null) {
                java.util.List<org.eclipse.mylyn.tasks.ui.LegendElement> elements = connectorUi.getLegendElements();
                if ((elements != null) && (elements.size() > 0)) {
                    legendElements.addAll(elements);
                    addLegendElements(composite.getBody(), connector, elements);
                }
            }
        }
        layout.numColumns = java.lang.Math.max(composite.getBody().getChildren().length, 1);
        // show 3 columns by default
        org.eclipse.swt.graphics.Point w = composite.computeSize(org.eclipse.swt.SWT.DEFAULT, org.eclipse.swt.SWT.DEFAULT);
        data.maxWidth = (w.x / layout.numColumns) * 3;
    }

    private void addLegendElements(org.eclipse.swt.widgets.Composite composite, org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector, java.util.List<org.eclipse.mylyn.tasks.ui.LegendElement> elements) {
        org.eclipse.ui.forms.widgets.Section connectorSection = toolkit.createSection(composite, org.eclipse.ui.forms.widgets.ExpandableComposite.TITLE_BAR);
        connectorSection.setLayout(new org.eclipse.ui.forms.widgets.TableWrapLayout());
        connectorSection.setLayoutData(new org.eclipse.ui.forms.widgets.TableWrapData(org.eclipse.ui.forms.widgets.TableWrapData.FILL_GRAB));
        java.lang.String label = connector.getLabel();
        int parenIndex = label.indexOf('(');
        if (parenIndex != (-1)) {
            label = label.substring(0, parenIndex);
        }
        connectorSection.setText(label);
        org.eclipse.ui.forms.widgets.TableWrapLayout clientLayout = new org.eclipse.ui.forms.widgets.TableWrapLayout();
        clientLayout.numColumns = 2;
        clientLayout.makeColumnsEqualWidth = false;
        clientLayout.verticalSpacing = 1;
        clientLayout.topMargin = 1;
        clientLayout.bottomMargin = 1;
        org.eclipse.swt.widgets.Composite connectorClient = toolkit.createComposite(connectorSection);
        connectorClient.setLayout(clientLayout);
        connectorClient.setLayoutData(new org.eclipse.ui.forms.widgets.TableWrapData(org.eclipse.ui.forms.widgets.TableWrapData.FILL_GRAB));
        connectorSection.setClient(connectorClient);
        org.eclipse.swt.widgets.Label imageLabel;
        for (org.eclipse.mylyn.tasks.ui.LegendElement element : elements) {
            imageLabel = toolkit.createLabel(connectorClient, "");// $NON-NLS-1$

            imageLabel.setImage(element.getImage());
            toolkit.createLabel(connectorClient, element.getLabel());
        }
        if (elements.size() < 4) {
            imageLabel = toolkit.createLabel(connectorClient, "");// $NON-NLS-1$

            toolkit.createLabel(connectorClient, "");// $NON-NLS-1$

        }
    }

    private void createGettingStartedSection(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.ui.forms.widgets.TableWrapLayout layout = new org.eclipse.ui.forms.widgets.TableWrapLayout();
        layout.verticalSpacing = 0;
        layout.leftMargin = 0;
        layout.rightMargin = 0;
        layout.topMargin = 0;
        layout.bottomMargin = 0;
        org.eclipse.swt.widgets.Composite hyperlinkClient = toolkit.createComposite(parent);
        hyperlinkClient.setLayout(layout);
        hyperlinkClient.setLayoutData(new org.eclipse.ui.forms.widgets.TableWrapData(org.eclipse.ui.forms.widgets.TableWrapData.FILL_GRAB));
        org.eclipse.ui.forms.widgets.Hyperlink gettingStartedLink = toolkit.createHyperlink(hyperlinkClient, Messages.UiLegendControl_Also_see_the_Getting_Started_documentation_online, org.eclipse.swt.SWT.WRAP);
        gettingStartedLink.addHyperlinkListener(new org.eclipse.ui.forms.events.IHyperlinkListener() {
            public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                close();
                org.eclipse.mylyn.tasks.ui.TasksUiUtil.openUrl(Messages.UiLegendControl_http_www_eclipse_org_mylyn_start);
            }

            public void linkEntered(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                // ignore
            }

            public void linkExited(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                // ignore
            }
        });
    }
}