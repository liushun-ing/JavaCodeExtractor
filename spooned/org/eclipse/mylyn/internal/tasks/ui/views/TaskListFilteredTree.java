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
 *     Frank Becker - fix for bug 280172
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.views;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.commons.ui.SelectionProviderAdapter;
import org.eclipse.mylyn.commons.workbench.AbstractFilteredTree;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.commons.workbench.search.SearchHistoryPopupDialog;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.ITaskListChangeListener;
import org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta;
import org.eclipse.mylyn.internal.tasks.ui.TaskHistoryDropDown;
import org.eclipse.mylyn.internal.tasks.ui.TaskScalingHyperlink;
import org.eclipse.mylyn.internal.tasks.ui.TaskWorkingSetFilter;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.actions.ActivateTaskDialogAction;
import org.eclipse.mylyn.internal.tasks.ui.actions.RepositoryElementActionGroup;
import org.eclipse.mylyn.internal.tasks.ui.actions.TaskWorkingSetAction;
import org.eclipse.mylyn.internal.tasks.ui.dialogs.UiLegendDialog;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskListChangeAdapter;
import org.eclipse.mylyn.internal.tasks.ui.search.AbstractSearchHandler;
import org.eclipse.mylyn.internal.tasks.ui.search.AbstractSearchHandler.IFilterChangeListener;
import org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskActivityAdapter;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
/**
 *
 * @author Mik Kersten
 * @author Leo Dos Santos - Task Working Set UI
 * @author David Green
 */
public class TaskListFilteredTree extends org.eclipse.mylyn.commons.workbench.AbstractFilteredTree {
    public static final java.lang.String LABEL_SEARCH = Messages.TaskListFilteredTree_Search_repository_for_key_or_summary_;

    private org.eclipse.mylyn.internal.tasks.ui.TaskScalingHyperlink workingSetLink;

    private org.eclipse.mylyn.internal.tasks.ui.TaskScalingHyperlink activeTaskLink;

    private org.eclipse.mylyn.internal.tasks.ui.views.WorkweekProgressBar taskProgressBar;

    private int totalTasks;

    private int completeTime;

    private int completeTasks;

    private int incompleteTime;

    private org.eclipse.ui.IWorkingSet currentWorkingSet;

    private org.eclipse.jface.action.MenuManager activeTaskMenuManager;

    private org.eclipse.mylyn.internal.tasks.ui.views.TaskListToolTip taskListToolTip;

    private org.eclipse.mylyn.internal.tasks.core.ITaskListChangeListener changeListener;

    private org.eclipse.mylyn.internal.tasks.ui.editors.TaskListChangeAdapter taskProgressBarChangeListener;

    private org.eclipse.mylyn.tasks.core.TaskActivityAdapter taskProgressBarActivityListener;

    private org.eclipse.jface.util.IPropertyChangeListener taskProgressBarWorkingSetListener;

    private org.eclipse.mylyn.internal.tasks.ui.TaskWorkingSetFilter workingSetFilter;

    private final org.eclipse.ui.IWorkbenchWindow window;

    private org.eclipse.mylyn.commons.ui.SelectionProviderAdapter activeTaskSelectionProvider;

    private org.eclipse.mylyn.internal.tasks.ui.actions.RepositoryElementActionGroup actionGroup;

    private org.eclipse.jface.viewers.StructuredSelection activeSelection;

    private final org.eclipse.mylyn.internal.tasks.ui.search.AbstractSearchHandler searchHandler;

    private org.eclipse.swt.widgets.Composite searchComposite;

    /**
     *
     * @param window
     * 		can be null. Needed for the working sets to be displayed properly
     */
    public TaskListFilteredTree(org.eclipse.swt.widgets.Composite parent, int treeStyle, org.eclipse.mylyn.internal.tasks.ui.search.AbstractSearchHandler searchHandler, org.eclipse.ui.IWorkbenchWindow window) {
        super(parent, treeStyle, searchHandler.createFilter());
        this.searchHandler = searchHandler;
        initSearchComposite();
        hookContextMenu();
        this.window = window;
        indicateActiveTaskWorkingSet();
        addDisposeListener(new org.eclipse.swt.events.DisposeListener() {
            public void widgetDisposed(org.eclipse.swt.events.DisposeEvent e) {
                if (changeListener != null) {
                    org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().removeChangeListener(changeListener);
                }
                if (taskProgressBarChangeListener != null) {
                    org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().removeChangeListener(taskProgressBarChangeListener);
                }
                if (taskProgressBarActivityListener != null) {
                    org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().removeActivityListener(taskProgressBarActivityListener);
                }
                if (taskProgressBarWorkingSetListener != null) {
                    org.eclipse.ui.PlatformUI.getWorkbench().getWorkingSetManager().removePropertyChangeListener(taskProgressBarWorkingSetListener);
                }
                actionGroup.setSelectionProvider(null);
                activeTaskMenuManager.dispose();
                if (taskListToolTip != null) {
                    taskListToolTip.dispose();
                }
            }
        });
    }

    private void hookContextMenu() {
        actionGroup = new org.eclipse.mylyn.internal.tasks.ui.actions.RepositoryElementActionGroup();
        activeTaskMenuManager = new org.eclipse.jface.action.MenuManager("#PopupMenu");// $NON-NLS-1$

        activeTaskMenuManager.setRemoveAllWhenShown(true);
        activeTaskMenuManager.addMenuListener(new org.eclipse.jface.action.IMenuListener() {
            public void menuAboutToShow(org.eclipse.jface.action.IMenuManager manager) {
                actionGroup.fillContextMenu(manager);
            }
        });
    }

    @java.lang.Override
    protected org.eclipse.jface.viewers.TreeViewer doCreateTreeViewer(org.eclipse.swt.widgets.Composite parent, int style) {
        // Use a single Composite for the Tree to being able to use the
        // TreeColumnLayout. See Bug 177891 for more details.
        org.eclipse.swt.widgets.Composite container = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.None);
        org.eclipse.swt.layout.GridData gridData = new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.FILL, true, true);
        gridData.verticalIndent = 0;
        gridData.horizontalIndent = 0;
        container.setLayoutData(gridData);
        container.setLayout(new org.eclipse.jface.layout.TreeColumnLayout());
        return super.doCreateTreeViewer(container, style);
    }

    @java.lang.Override
    protected org.eclipse.swt.widgets.Composite createProgressComposite(org.eclipse.swt.widgets.Composite container) {
        org.eclipse.swt.widgets.Composite progressComposite = new org.eclipse.swt.widgets.Composite(container, org.eclipse.swt.SWT.NONE);
        org.eclipse.swt.layout.GridLayout progressLayout = new org.eclipse.swt.layout.GridLayout(1, false);
        progressLayout.marginWidth = 4;
        progressLayout.marginHeight = 0;
        progressLayout.marginBottom = 0;
        progressLayout.horizontalSpacing = 0;
        progressLayout.verticalSpacing = 0;
        progressComposite.setLayout(progressLayout);
        progressComposite.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.DEFAULT, true, false, 4, 1));
        taskProgressBar = new org.eclipse.mylyn.internal.tasks.ui.views.WorkweekProgressBar(progressComposite);
        taskProgressBar.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.FILL, true, true));
        updateTaskProgressBar();
        taskProgressBarChangeListener = new org.eclipse.mylyn.internal.tasks.ui.editors.TaskListChangeAdapter() {
            @java.lang.Override
            public void containersChanged(java.util.Set<org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta> containers) {
                for (org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta taskContainerDelta : containers) {
                    if (taskContainerDelta.getElement() instanceof org.eclipse.mylyn.tasks.core.ITask) {
                        updateTaskProgressBar();
                        break;
                    }
                }
            }
        };
        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().addChangeListener(taskProgressBarChangeListener);
        taskProgressBarActivityListener = new org.eclipse.mylyn.tasks.core.TaskActivityAdapter() {
            @java.lang.Override
            public void activityReset() {
                updateTaskProgressBar();
            }
        };
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().addActivityListener(taskProgressBarActivityListener);
        taskProgressBarWorkingSetListener = new org.eclipse.jface.util.IPropertyChangeListener() {
            public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
                java.lang.String property = event.getProperty();
                if (org.eclipse.ui.IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE.equals(property) || org.eclipse.ui.IWorkingSetManager.CHANGE_WORKING_SET_REMOVE.equals(property)) {
                    updateTaskProgressBar();
                }
            }
        };
        org.eclipse.ui.PlatformUI.getWorkbench().getWorkingSetManager().addPropertyChangeListener(taskProgressBarWorkingSetListener);
        return progressComposite;
    }

    @java.lang.Override
    protected org.eclipse.swt.widgets.Composite createSearchComposite(org.eclipse.swt.widgets.Composite container) {
        searchComposite = new org.eclipse.swt.widgets.Composite(container, org.eclipse.swt.SWT.NONE);
        org.eclipse.swt.layout.GridLayout searchLayout = new org.eclipse.swt.layout.GridLayout(2, false);
        searchLayout.marginHeight = 0;
        searchLayout.marginWidth = 0;
        searchComposite.setLayout(searchLayout);
        searchComposite.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.DEFAULT, true, false, 4, 1));
        return searchComposite;
    }

    private void initSearchComposite() {
        searchHandler.createSearchComposite(searchComposite);
        searchHandler.adaptTextSearchControl(getTextSearchControl().getTextControl());
        searchHandler.addFilterChangeListener(new org.eclipse.mylyn.internal.tasks.ui.search.AbstractSearchHandler.IFilterChangeListener() {
            public void filterChanged() {
                getRefreshPolicy().filterChanged();
            }
        });
        if (org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil.supportsTaskSearch()) {
            final org.eclipse.mylyn.internal.tasks.ui.TaskScalingHyperlink searchLink = new org.eclipse.mylyn.internal.tasks.ui.TaskScalingHyperlink(searchComposite, org.eclipse.swt.SWT.LEFT);
            searchLink.setText(org.eclipse.mylyn.internal.tasks.ui.views.TaskListFilteredTree.LABEL_SEARCH);
            searchLink.addHyperlinkListener(new org.eclipse.ui.forms.events.IHyperlinkListener() {
                public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                    org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil.openSearchDialog(org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow());
                }

                public void linkEntered(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                    searchLink.setUnderlined(true);
                }

                public void linkExited(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                    searchLink.setUnderlined(false);
                }
            });
        }
    }

    private void updateTaskProgressBar() {
        if (taskProgressBar.isDisposed()) {
            return;
        }
        java.util.Set<org.eclipse.mylyn.tasks.core.ITask> tasksThisWeek = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().getScheduledForADayThisWeek();
        if (workingSetFilter != null) {
            for (java.util.Iterator<org.eclipse.mylyn.tasks.core.ITask> it = tasksThisWeek.iterator(); it.hasNext();) {
                org.eclipse.mylyn.tasks.core.ITask task = it.next();
                if (!workingSetFilter.select(task)) {
                    it.remove();
                }
            }
        }
        totalTasks = tasksThisWeek.size();
        completeTime = 0;
        completeTasks = 0;
        incompleteTime = 0;
        for (org.eclipse.mylyn.tasks.core.ITask task : tasksThisWeek) {
            if (task instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask) {
                org.eclipse.mylyn.internal.tasks.core.AbstractTask abstractTask = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (task));
                if (task.isCompleted()) {
                    completeTasks++;
                    if (abstractTask.getEstimatedTimeHours() > 0) {
                        completeTime += abstractTask.getEstimatedTimeHours();
                    } else {
                        completeTime++;
                    }
                } else if (abstractTask.getEstimatedTimeHours() > 0) {
                    incompleteTime += abstractTask.getEstimatedTimeHours();
                } else {
                    incompleteTime++;
                }
            }
        }
        org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
            public void run() {
                if (org.eclipse.ui.PlatformUI.isWorkbenchRunning() && (!taskProgressBar.isDisposed())) {
                    taskProgressBar.reset(completeTime, completeTime + incompleteTime);
                    taskProgressBar.setToolTipText((((Messages.TaskListFilteredTree_Workweek_Progress + "\n")// $NON-NLS-1$
                     + java.text.MessageFormat.format(Messages.TaskListFilteredTree_Estimated_hours, completeTime, completeTime + incompleteTime)) + "\n")// $NON-NLS-1$
                     + java.text.MessageFormat.format(Messages.TaskListFilteredTree_Scheduled_tasks, completeTasks, totalTasks));
                }
            }
        });
    }

    @java.lang.Override
    protected org.eclipse.swt.widgets.Composite createActiveWorkingSetComposite(org.eclipse.swt.widgets.Composite container) {
        final org.eclipse.ui.forms.widgets.ImageHyperlink workingSetButton = new org.eclipse.ui.forms.widgets.ImageHyperlink(container, org.eclipse.swt.SWT.FLAT);
        workingSetButton.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.TOOLBAR_ARROW_RIGHT));
        workingSetButton.setToolTipText(Messages.TaskListFilteredTree_Select_Working_Set);
        workingSetLink = new org.eclipse.mylyn.internal.tasks.ui.TaskScalingHyperlink(container, org.eclipse.swt.SWT.LEFT);
        workingSetLink.setText(org.eclipse.mylyn.internal.tasks.ui.actions.TaskWorkingSetAction.LABEL_SETS_NONE);
        workingSetLink.setUnderlined(false);
        final org.eclipse.mylyn.internal.tasks.ui.actions.TaskWorkingSetAction workingSetAction = new org.eclipse.mylyn.internal.tasks.ui.actions.TaskWorkingSetAction();
        workingSetButton.addHyperlinkListener(new org.eclipse.ui.forms.events.IHyperlinkListener() {
            public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                workingSetAction.getMenu(workingSetButton).setVisible(true);
            }

            public void linkEntered(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                workingSetButton.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.TOOLBAR_ARROW_DOWN));
            }

            public void linkExited(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                workingSetButton.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.TOOLBAR_ARROW_RIGHT));
            }
        });
        workingSetLink.addMouseListener(new org.eclipse.swt.events.MouseAdapter() {
            @java.lang.Override
            public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
                if (currentWorkingSet != null) {
                    workingSetAction.run(currentWorkingSet);
                } else {
                    workingSetAction.run();
                }
            }
        });
        return workingSetLink;
    }

    @java.lang.Override
    protected org.eclipse.swt.widgets.Composite createActiveTaskComposite(final org.eclipse.swt.widgets.Composite container) {
        final org.eclipse.ui.forms.widgets.ImageHyperlink activeTaskButton = new org.eclipse.ui.forms.widgets.ImageHyperlink(container, org.eclipse.swt.SWT.LEFT);// SWT.ARROW | SWT.RIGHT);

        activeTaskButton.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.TOOLBAR_ARROW_RIGHT));
        activeTaskButton.setToolTipText(Messages.TaskListFilteredTree_Select_Active_Task);
        activeTaskLink = new org.eclipse.mylyn.internal.tasks.ui.TaskScalingHyperlink(container, org.eclipse.swt.SWT.LEFT);
        changeListener = new org.eclipse.mylyn.internal.tasks.ui.editors.TaskListChangeAdapter() {
            @java.lang.Override
            public void containersChanged(java.util.Set<org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta> containers) {
                for (org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta taskContainerDelta : containers) {
                    if (taskContainerDelta.getElement() instanceof org.eclipse.mylyn.tasks.core.ITask) {
                        final org.eclipse.mylyn.internal.tasks.core.AbstractTask changedTask = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (taskContainerDelta.getElement()));
                        if (changedTask.isActive()) {
                            if (org.eclipse.core.runtime.Platform.isRunning() && (org.eclipse.ui.PlatformUI.getWorkbench() != null)) {
                                if (org.eclipse.swt.widgets.Display.getCurrent() == null) {
                                    if ((org.eclipse.ui.PlatformUI.getWorkbench().getDisplay() != null) && (!org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().isDisposed())) {
                                        org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                                            public void run() {
                                                indicateActiveTask(changedTask);
                                            }
                                        });
                                    }
                                } else {
                                    indicateActiveTask(changedTask);
                                }
                            }
                        }
                    }
                }
            }
        };
        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().addChangeListener(changeListener);
        activeTaskLink.setText(Messages.TaskListFilteredTree_Activate);
        // avoid having the Hyperlink class show a native tooltip when it shortens the text which would overlap with the task list tooltip
        activeTaskLink.setToolTipText("");// $NON-NLS-1$

        taskListToolTip = new org.eclipse.mylyn.internal.tasks.ui.views.TaskListToolTip(activeTaskLink);
        org.eclipse.mylyn.tasks.core.ITask activeTask = org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().getActiveTask();
        if (activeTask != null) {
            indicateActiveTask(activeTask);
        }
        activeTaskButton.addHyperlinkListener(new org.eclipse.ui.forms.events.IHyperlinkListener() {
            private org.eclipse.swt.widgets.Menu dropDownMenu;

            public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent event) {
                if (dropDownMenu != null) {
                    dropDownMenu.dispose();
                }
                org.eclipse.mylyn.internal.tasks.ui.TaskHistoryDropDown taskHistory = new org.eclipse.mylyn.internal.tasks.ui.TaskHistoryDropDown();
                taskHistory.setScopedToWorkingSet(true);
                dropDownMenu = new org.eclipse.swt.widgets.Menu(activeTaskButton);
                taskHistory.fill(dropDownMenu, 0);
                dropDownMenu.setVisible(true);
            }

            public void linkEntered(org.eclipse.ui.forms.events.HyperlinkEvent event) {
                activeTaskButton.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.TOOLBAR_ARROW_DOWN));
            }

            public void linkExited(org.eclipse.ui.forms.events.HyperlinkEvent event) {
                activeTaskButton.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.TOOLBAR_ARROW_RIGHT));
            }
        });
        activeTaskLink.addMenuDetectListener(new org.eclipse.swt.events.MenuDetectListener() {
            public void menuDetected(org.eclipse.swt.events.MenuDetectEvent e) {
                // do not show menu for inactive task link
                if (((activeTaskSelectionProvider != null) && (activeSelection != null)) && (!activeSelection.isEmpty())) {
                    // grab focus since the active task will become the active selection: this causes the focus listener on the task list viewer to reset the active selection when focus is set back to the task list
                    activeTaskLink.setFocus();
                    // set site selection to active task
                    activeTaskSelectionProvider.setSelection(activeSelection);
                    // show menu that has been registered with view
                    org.eclipse.swt.widgets.Menu activeTaskMenu = activeTaskMenuManager.createContextMenu(container);
                    activeTaskMenu.setVisible(true);
                }
            }
        });
        activeTaskLink.addMouseListener(new org.eclipse.swt.events.MouseAdapter() {
            @java.lang.Override
            public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
                // only handle left clicks, context menu is handled by platform
                if (e.button == 1) {
                    org.eclipse.mylyn.tasks.core.ITask activeTask = org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().getActiveTask();
                    if (activeTask == null) {
                        org.eclipse.mylyn.internal.tasks.ui.actions.ActivateTaskDialogAction activateAction = new org.eclipse.mylyn.internal.tasks.ui.actions.ActivateTaskDialogAction();
                        activateAction.init(org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow());
                        activateAction.run(null);
                    } else {
                        // if (TaskListFilteredTree.super.filterText.getText().length() > 0) {
                        // TaskListFilteredTree.super.filterText.setText("");
                        // TaskListFilteredTree.this.textChanged();
                        // }
                        // if (TaskListView.getFromActivePerspective().getDrilledIntoCategory() != null) {
                        // TaskListView.getFromActivePerspective().goUpToRoot();
                        // }
                        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.refreshAndOpenTaskListElement(activeTask);
                    }
                }
            }
        });
        return activeTaskLink;
    }

    @java.lang.Override
    protected void textChanged() {
        super.textChanged();
        if ((getFilterString() != null) && (!getFilterString().trim().equals(""))) {
            // $NON-NLS-1$
            setShowSearch(true);
        } else {
            setShowSearch(false);
        }
    }

    public void indicateActiveTaskWorkingSet() {
        if ((((window == null) || (workingSetLink == null)) || (filterComposite == null)) || filterComposite.isDisposed()) {
            return;
        }
        java.util.Set<org.eclipse.ui.IWorkingSet> activeSets = org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater.getActiveWorkingSets(window);
        if (activeSets == null) {
            return;
        }
        if (activeSets.size() == 0) {
            workingSetLink.setText(org.eclipse.mylyn.internal.tasks.ui.actions.TaskWorkingSetAction.LABEL_SETS_NONE);
            workingSetLink.setToolTipText(Messages.TaskListFilteredTree_Edit_Task_Working_Sets_);
            currentWorkingSet = null;
        } else if (activeSets.size() > 1) {
            workingSetLink.setText(Messages.TaskListFilteredTree__multiple_);
            workingSetLink.setToolTipText(Messages.TaskListFilteredTree_Edit_Task_Working_Sets_);
            currentWorkingSet = null;
        } else {
            java.lang.Object[] array = activeSets.toArray();
            org.eclipse.ui.IWorkingSet workingSet = ((org.eclipse.ui.IWorkingSet) (array[0]));
            workingSetLink.setText(workingSet.getLabel());
            workingSetLink.setToolTipText(Messages.TaskListFilteredTree_Edit_Task_Working_Sets_);
            currentWorkingSet = workingSet;
        }
        relayoutFilterControls();
    }

    public void indicateActiveTask(org.eclipse.mylyn.tasks.core.ITask task) {
        if (org.eclipse.swt.widgets.Display.getCurrent() != null) {
            if (filterComposite.isDisposed()) {
                return;
            }
            activeTaskLink.setTask(task);
            activeSelection = new org.eclipse.jface.viewers.StructuredSelection(task);
            relayoutFilterControls();
        }
    }

    private void relayoutFilterControls() {
        filterComposite.layout();
    }

    public java.lang.String getActiveTaskLabelText() {
        return activeTaskLink.getText();
    }

    public void indicateNoActiveTask() {
        if (filterComposite.isDisposed()) {
            return;
        }
        activeTaskLink.setTask(null);
        activeTaskLink.setText(Messages.TaskListFilteredTree_Activate);
        activeTaskLink.setToolTipText("");// $NON-NLS-1$

        activeSelection = org.eclipse.jface.viewers.StructuredSelection.EMPTY;
        relayoutFilterControls();
    }

    @java.lang.Override
    public void setFilterText(java.lang.String string) {
        if (filterText != null) {
            filterText.setText(string);
            selectAll();
        }
    }

    @java.lang.Override
    protected java.lang.String getFilterString() {
        java.lang.String text = super.getFilterString();
        return text != null ? text.trim() : null;
    }

    public org.eclipse.mylyn.internal.tasks.ui.TaskWorkingSetFilter getWorkingSetFilter() {
        return workingSetFilter;
    }

    public void setWorkingSetFilter(org.eclipse.mylyn.internal.tasks.ui.TaskWorkingSetFilter workingSetFilter) {
        this.workingSetFilter = workingSetFilter;
    }

    public org.eclipse.jface.action.MenuManager getActiveTaskMenuManager() {
        return activeTaskMenuManager;
    }

    public org.eclipse.mylyn.commons.ui.SelectionProviderAdapter getActiveTaskSelectionProvider() {
        return activeTaskSelectionProvider;
    }

    public void setActiveTaskSelectionProvider(org.eclipse.mylyn.commons.ui.SelectionProviderAdapter activeTaskSelectionProvider) {
        this.activeTaskSelectionProvider = activeTaskSelectionProvider;
        this.actionGroup.setSelectionProvider(activeTaskSelectionProvider);
    }

    @java.lang.SuppressWarnings("deprecation")
    @java.lang.Override
    protected org.eclipse.mylyn.commons.workbench.search.SearchHistoryPopupDialog getHistoryPopupDialog() {
        return null;
    }

    @java.lang.Override
    protected org.eclipse.swt.widgets.Composite createAdditionalControls(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.widgets.Composite container = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NULL);
        org.eclipse.jface.layout.GridLayoutFactory.fillDefaults().applyTo(container);
        org.eclipse.ui.forms.widgets.ImageHyperlink showUILegend = new org.eclipse.ui.forms.widgets.ImageHyperlink(container, org.eclipse.swt.SWT.NONE);
        showUILegend.setImage(org.eclipse.mylyn.commons.ui.CommonImages.QUESTION.createImage());
        showUILegend.setToolTipText(Messages.TaskListFilteredTree_Show_Tasks_UI_Legend);
        showUILegend.addHyperlinkListener(new org.eclipse.ui.forms.events.HyperlinkAdapter() {
            @java.lang.Override
            public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                org.eclipse.mylyn.internal.tasks.ui.dialogs.UiLegendDialog uiLegendDialog = new org.eclipse.mylyn.internal.tasks.ui.dialogs.UiLegendDialog(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell());
                uiLegendDialog.open();
            }
        });
        return container;
    }
}