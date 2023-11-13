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
 *     Ken Sueda - improvements
 *     Eugene Kuleshov - improvements
 *     Frank Becker - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.views;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.commons.ui.PlatformUiUtil;
import org.eclipse.mylyn.commons.ui.compatibility.CommonThemes;
import org.eclipse.mylyn.commons.workbench.GradientDrawer;
import org.eclipse.mylyn.internal.commons.notifications.feed.ServiceMessage;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter;
import org.eclipse.mylyn.internal.tasks.ui.CategorizedPresentation;
import org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants;
import org.eclipse.mylyn.internal.tasks.ui.MyTasksFilter;
import org.eclipse.mylyn.internal.tasks.ui.ScheduledPresentation;
import org.eclipse.mylyn.internal.tasks.ui.TaskArchiveFilter;
import org.eclipse.mylyn.internal.tasks.ui.TaskCompletionFilter;
import org.eclipse.mylyn.internal.tasks.ui.TaskPriorityFilter;
import org.eclipse.mylyn.internal.tasks.ui.TaskRepositoryUtil;
import org.eclipse.mylyn.internal.tasks.ui.TaskReviewArtifactFilter;
import org.eclipse.mylyn.internal.tasks.ui.TaskWorkingSetFilter;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.actions.CollapseAllAction;
import org.eclipse.mylyn.internal.tasks.ui.actions.ExpandAllAction;
import org.eclipse.mylyn.internal.tasks.ui.actions.FilterCompletedTasksAction;
import org.eclipse.mylyn.internal.tasks.ui.actions.FilterMyTasksAction;
import org.eclipse.mylyn.internal.tasks.ui.actions.GoUpAction;
import org.eclipse.mylyn.internal.tasks.ui.actions.GroupSubTasksAction;
import org.eclipse.mylyn.internal.tasks.ui.actions.LinkWithEditorAction;
import org.eclipse.mylyn.internal.tasks.ui.actions.NewTaskAction;
import org.eclipse.mylyn.internal.tasks.ui.actions.OpenTasksUiPreferencesAction;
import org.eclipse.mylyn.internal.tasks.ui.actions.PresentationDropDownSelectionAction;
import org.eclipse.mylyn.internal.tasks.ui.actions.ShowAllQueriesAction;
import org.eclipse.mylyn.internal.tasks.ui.actions.ShowNonMatchingSubtasksAction;
import org.eclipse.mylyn.internal.tasks.ui.actions.SynchronizeAutomaticallyAction;
import org.eclipse.mylyn.internal.tasks.ui.actions.TaskListSortAction;
import org.eclipse.mylyn.internal.tasks.ui.actions.TaskListViewActionGroup;
import org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListServiceMessageControl;
import org.eclipse.mylyn.internal.tasks.ui.search.AbstractSearchHandler;
import org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil;
import org.eclipse.mylyn.internal.tasks.ui.util.TaskDragSourceListener;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITask.PriorityLevel;
import org.eclipse.mylyn.tasks.core.ITaskActivationListener;
import org.eclipse.mylyn.tasks.core.ITaskActivityListener;
import org.eclipse.mylyn.tasks.core.ITaskContainer;
import org.eclipse.mylyn.tasks.core.TaskActivationAdapter;
import org.eclipse.mylyn.tasks.core.TaskActivityAdapter;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.ITasksUiConstants;
import org.eclipse.mylyn.tasks.ui.TaskElementLabelProvider;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.RTFTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.URLTransfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISizeProvider;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.CollapseAllHandler;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.themes.IThemeManager;
/**
 *
 * @author Mik Kersten
 * @author Ken Sueda
 * @author Eugene Kuleshov
 * @author David Green
 */
public class TaskListView extends org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListView implements org.eclipse.jface.util.IPropertyChangeListener , org.eclipse.ui.part.IShowInTarget {
    private static final java.lang.String ID_SEPARATOR_FILTERS = "filters";// $NON-NLS-1$


    private static final java.lang.String ID_SEPARATOR_SEARCH = "search";// $NON-NLS-1$


    private static final java.lang.String ID_SEPARATOR_TASKS = "tasks";// $NON-NLS-1$


    private static final java.lang.String ID_SEPARATOR_CONTEXT = "context";// $NON-NLS-1$


    /**
     *
     * @deprecated Use {@link ITasksUiConstants#ID_VIEW_TASKS} instead
     */
    @java.lang.Deprecated
    public static final java.lang.String ID = org.eclipse.mylyn.tasks.ui.ITasksUiConstants.ID_VIEW_TASKS;

    public static final java.lang.String LABEL_VIEW = Messages.TaskListView_Task_List;

    private static final java.lang.String MEMENTO_SORTER = "sorter";// $NON-NLS-1$


    private static final java.lang.String MEMENTO_LINK_WITH_EDITOR = "linkWithEditor";// $NON-NLS-1$


    private static final java.lang.String MEMENTO_PRESENTATION = "presentation";// $NON-NLS-1$


    private static final java.lang.String LABEL_NO_TASKS = "no task active";// $NON-NLS-1$


    private static final int SIZE_MAX_SELECTION_HISTORY = 10;

    static final java.lang.String[] PRIORITY_LEVELS = new java.lang.String[]{ org.eclipse.mylyn.tasks.core.ITask.PriorityLevel.P1.toString(), org.eclipse.mylyn.tasks.core.ITask.PriorityLevel.P2.toString(), org.eclipse.mylyn.tasks.core.ITask.PriorityLevel.P3.toString(), org.eclipse.mylyn.tasks.core.ITask.PriorityLevel.P4.toString(), org.eclipse.mylyn.tasks.core.ITask.PriorityLevel.P5.toString() };

    public static final java.lang.String[] PRIORITY_LEVEL_DESCRIPTIONS = new java.lang.String[]{ org.eclipse.mylyn.tasks.core.ITask.PriorityLevel.P1.getDescription(), org.eclipse.mylyn.tasks.core.ITask.PriorityLevel.P2.getDescription(), org.eclipse.mylyn.tasks.core.ITask.PriorityLevel.P3.getDescription(), org.eclipse.mylyn.tasks.core.ITask.PriorityLevel.P4.getDescription(), org.eclipse.mylyn.tasks.core.ITask.PriorityLevel.P5.getDescription() };

    private static java.util.List<org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListPresentation> presentationsPrimary = new java.util.ArrayList<org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListPresentation>();

    private static java.util.List<org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListPresentation> presentationsSecondary = new java.util.ArrayList<org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListPresentation>();

    private boolean focusedMode;

    private boolean linkWithEditor;

    private final org.eclipse.mylyn.internal.tasks.ui.views.TaskListCellModifier taskListCellModifier = new org.eclipse.mylyn.internal.tasks.ui.views.TaskListCellModifier(this);

    private org.eclipse.ui.themes.IThemeManager themeManager;

    private org.eclipse.mylyn.internal.tasks.ui.views.TaskListFilteredTree filteredTree;

    private org.eclipse.mylyn.commons.ui.SelectionProviderAdapter selectionProvider;

    private org.eclipse.ui.part.DrillDownAdapter drillDownAdapter;

    private org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer drilledIntoCategory;

    private org.eclipse.mylyn.internal.tasks.ui.actions.CollapseAllAction collapseAll;

    private org.eclipse.mylyn.internal.tasks.ui.actions.ExpandAllAction expandAll;

    private org.eclipse.mylyn.internal.tasks.ui.actions.FilterCompletedTasksAction filterCompleteTask;

    private org.eclipse.mylyn.internal.tasks.ui.actions.FilterMyTasksAction filterMyTasksAction;

    private org.eclipse.mylyn.internal.tasks.ui.actions.GroupSubTasksAction groupSubTasksAction;

    private org.eclipse.mylyn.internal.tasks.ui.actions.SynchronizeAutomaticallyAction synchronizeAutomatically;

    private org.eclipse.mylyn.internal.tasks.ui.actions.OpenTasksUiPreferencesAction openPreferencesAction;

    private org.eclipse.mylyn.internal.tasks.ui.views.PriorityDropDownAction filterOnPriorityAction;

    private org.eclipse.mylyn.internal.tasks.ui.actions.TaskListSortAction sortDialogAction;

    private org.eclipse.mylyn.internal.tasks.ui.actions.NewTaskAction newTaskAction;

    private org.eclipse.mylyn.internal.tasks.ui.actions.LinkWithEditorAction linkWithEditorAction;

    private final org.eclipse.mylyn.internal.tasks.ui.actions.PresentationDropDownSelectionAction presentationDropDownSelectionAction = new org.eclipse.mylyn.internal.tasks.ui.actions.PresentationDropDownSelectionAction(this);

    private org.eclipse.mylyn.internal.tasks.ui.actions.ShowNonMatchingSubtasksAction showNonMatchingSubtasksAction;

    private final org.eclipse.mylyn.internal.tasks.ui.TaskPriorityFilter filterPriority = new org.eclipse.mylyn.internal.tasks.ui.TaskPriorityFilter();

    private final org.eclipse.mylyn.internal.tasks.ui.TaskCompletionFilter filterComplete = new org.eclipse.mylyn.internal.tasks.ui.TaskCompletionFilter();

    private final org.eclipse.mylyn.internal.tasks.ui.TaskArchiveFilter filterArchive = new org.eclipse.mylyn.internal.tasks.ui.TaskArchiveFilter();

    private final org.eclipse.mylyn.internal.tasks.ui.TaskReviewArtifactFilter filterArtifact = new org.eclipse.mylyn.internal.tasks.ui.TaskReviewArtifactFilter();

    private final org.eclipse.mylyn.internal.tasks.ui.views.PresentationFilter filterPresentation = org.eclipse.mylyn.internal.tasks.ui.views.PresentationFilter.getInstance();

    private final org.eclipse.mylyn.internal.tasks.ui.MyTasksFilter filterMyTasks = new org.eclipse.mylyn.internal.tasks.ui.MyTasksFilter();

    private org.eclipse.mylyn.internal.tasks.ui.TaskWorkingSetFilter filterWorkingSet;

    private final java.util.Set<org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter> filters = new java.util.HashSet<org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter>();

    protected java.lang.String[] columnNames = new java.lang.String[]{ Messages.TaskListView_Summary };

    protected int[] columnWidths = new int[]{ 200 };

    private org.eclipse.swt.widgets.TreeColumn[] columns;

    private org.eclipse.ui.IMemento taskListMemento;

    private org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListPresentation currentPresentation;

    private org.eclipse.mylyn.internal.tasks.ui.views.TaskTableLabelProvider taskListTableLabelProvider;

    private org.eclipse.mylyn.internal.tasks.ui.views.TaskListSorter tableSorter;

    private org.eclipse.mylyn.internal.tasks.ui.actions.TaskListViewActionGroup actionGroup;

    private org.eclipse.mylyn.internal.tasks.ui.views.CustomTaskListDecorationDrawer customDrawer;

    private org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListServiceMessageControl serviceMessageControl;

    private long lastExpansionTime;

    private final org.eclipse.ui.IPageListener PAGE_LISTENER = new org.eclipse.ui.IPageListener() {
        public void pageActivated(org.eclipse.ui.IWorkbenchPage page) {
            filteredTree.indicateActiveTaskWorkingSet();
        }

        public void pageClosed(org.eclipse.ui.IWorkbenchPage page) {
            // ignore
        }

        public void pageOpened(org.eclipse.ui.IWorkbenchPage page) {
            // ignore
        }
    };

    private final java.util.LinkedHashMap<java.lang.String, org.eclipse.jface.viewers.IStructuredSelection> lastSelectionByTaskHandle = new java.util.LinkedHashMap<java.lang.String, org.eclipse.jface.viewers.IStructuredSelection>(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.SIZE_MAX_SELECTION_HISTORY);

    /**
     * True if the view should indicate that interaction monitoring is paused
     */
    protected boolean isPaused = false;

    private final org.eclipse.mylyn.tasks.core.ITaskActivityListener TASK_ACTIVITY_LISTENER = new org.eclipse.mylyn.tasks.core.TaskActivityAdapter() {
        @java.lang.Override
        public void activityReset() {
            org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                public void run() {
                    refreshJob.refresh();
                }
            });
        }
    };

    private final org.eclipse.mylyn.tasks.core.ITaskActivationListener TASK_ACTIVATION_LISTENER = new org.eclipse.mylyn.tasks.core.TaskActivationAdapter() {
        @java.lang.Override
        public void taskActivated(final org.eclipse.mylyn.tasks.core.ITask task) {
            if (task != null) {
                org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                    public void run() {
                        updateDescription();
                        refresh(task);
                        selectedAndFocusTask(task);
                        filteredTree.indicateActiveTask(task);
                    }
                });
            }
        }

        @java.lang.Override
        public void taskDeactivated(final org.eclipse.mylyn.tasks.core.ITask task) {
            org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                public void run() {
                    refresh(task);
                    updateDescription();
                    filteredTree.indicateNoActiveTask();
                }
            });
        }

        private void refresh(final org.eclipse.mylyn.tasks.core.ITask task) {
            if (TaskListView.this.isScheduledPresentation()) {
                refreshJob.refresh();
            } else {
                refreshJob.refreshElement(task);
            }
        }
    };

    private final org.eclipse.jface.util.IPropertyChangeListener THEME_CHANGE_LISTENER = new org.eclipse.jface.util.IPropertyChangeListener() {
        public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
            if (event.getProperty().equals(org.eclipse.ui.themes.IThemeManager.CHANGE_CURRENT_THEME) || org.eclipse.mylyn.commons.ui.compatibility.CommonThemes.isCommonTheme(event.getProperty())) {
                taskListTableLabelProvider.setCategoryBackgroundColor(themeManager.getCurrentTheme().getColorRegistry().get(org.eclipse.mylyn.commons.ui.compatibility.CommonThemes.COLOR_CATEGORY));
                getViewer().refresh();
            }
        }
    };

    private final org.eclipse.jface.util.IPropertyChangeListener tasksUiPreferenceListener = new org.eclipse.jface.util.IPropertyChangeListener() {
        public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
            if (org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.TASK_LIST_TOOL_TIPS_ENABLED.equals(event.getProperty())) {
                updateTooltipEnablement();
            }
            if (event.getProperty().equals(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.USE_STRIKETHROUGH_FOR_COMPLETED) || event.getProperty().equals(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.OVERLAYS_INCOMING_TIGHT)) {
                refreshJob.refresh();
            }
            if ((event.getProperty().equals(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.FILTER_HIDDEN) || event.getProperty().equals(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.FILTER_NON_MATCHING)) || event.getProperty().equals(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.GROUP_SUBTASKS)) {
                filterPresentation.updateSettings();
                refresh(true);
                if (showNonMatchingSubtasksAction != null) {
                    showNonMatchingSubtasksAction.update();
                }
            }
        }
    };

    private org.eclipse.mylyn.internal.tasks.ui.views.TaskListToolTip taskListToolTip;

    public static org.eclipse.mylyn.internal.tasks.ui.views.TaskListView getFromActivePerspective() {
        if (org.eclipse.ui.PlatformUI.isWorkbenchRunning()) {
            org.eclipse.ui.IWorkbenchWindow activeWorkbenchWindow = org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if (activeWorkbenchWindow != null) {
                org.eclipse.ui.IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
                if (activePage != null) {
                    org.eclipse.ui.IViewPart view = activePage.findView(org.eclipse.mylyn.tasks.ui.ITasksUiConstants.ID_VIEW_TASKS);
                    if (view instanceof org.eclipse.mylyn.internal.tasks.ui.views.TaskListView) {
                        return ((org.eclipse.mylyn.internal.tasks.ui.views.TaskListView) (view));
                    }
                }
            }
        }
        return null;
    }

    private static boolean initializedSynchronization;

    public TaskListView() {
        if (!org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.initializedSynchronization) {
            org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.initializedSynchronization = true;
            // trigger additional initialization when task list is first made visible.
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().initializeNotificationsAndSynchronization();
        }
    }

    @java.lang.Override
    public void dispose() {
        super.dispose();
        if (actionGroup != null) {
            actionGroup.dispose();
        }
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getServiceMessageManager().removeServiceMessageListener(serviceMessageControl);
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(tasksUiPreferenceListener);
        if (refreshJob != null) {
            refreshJob.dispose();
        }
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().removeActivityListener(TASK_ACTIVITY_LISTENER);
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().removeActivationListener(TASK_ACTIVATION_LISTENER);
        org.eclipse.ui.PlatformUI.getWorkbench().getWorkingSetManager().removePropertyChangeListener(this);
        if (org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null) {
            org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow().removePageListener(PAGE_LISTENER);
        }
        final org.eclipse.ui.themes.IThemeManager themeManager = getSite().getWorkbenchWindow().getWorkbench().getThemeManager();
        if (themeManager != null) {
            themeManager.removePropertyChangeListener(THEME_CHANGE_LISTENER);
        }
        if (editorListener != null) {
            getSite().getPage().removePartListener(editorListener);
        }
        if (searchHandler != null) {
            searchHandler.dispose();
        }
    }

    private void updateDescription() {
        org.eclipse.mylyn.tasks.core.ITask task = org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().getActiveTask();
        if ((getSite() == null) || (getSite().getPage() == null)) {
            return;
        }
        org.eclipse.ui.IViewReference reference = getSite().getPage().findViewReference(org.eclipse.mylyn.tasks.ui.ITasksUiConstants.ID_VIEW_TASKS);
        boolean shouldSetDescription = false;
        if (((reference != null) && reference.isFastView()) && (!getSite().getPage().isPartVisible(this))) {
            shouldSetDescription = true;
        }
        if (task != null) {
            setTitleToolTip(((org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.LABEL_VIEW + " (") + task.getSummary()) + ")");// $NON-NLS-1$ //$NON-NLS-2$

            if (shouldSetDescription) {
                setContentDescription(task.getSummary());
            } else {
                setContentDescription("");// $NON-NLS-1$

            }
        } else {
            setTitleToolTip(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.LABEL_VIEW);
            if (shouldSetDescription) {
                setContentDescription(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.LABEL_NO_TASKS);
            } else {
                setContentDescription("");// $NON-NLS-1$

            }
        }
    }

    @java.lang.Override
    public void init(org.eclipse.ui.IViewSite site, org.eclipse.ui.IMemento memento) throws org.eclipse.ui.PartInitException {
        init(site);
        this.taskListMemento = memento;
    }

    @java.lang.Override
    public void saveState(org.eclipse.ui.IMemento memento) {
        if (tableSorter != null) {
            org.eclipse.ui.IMemento child = memento.createChild(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.MEMENTO_SORTER);
            tableSorter.saveState(child);
        }
        memento.putString(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.MEMENTO_LINK_WITH_EDITOR, java.lang.Boolean.toString(linkWithEditor));
        memento.putString(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.MEMENTO_PRESENTATION, currentPresentation != null ? currentPresentation.getId() : org.eclipse.mylyn.internal.tasks.ui.CategorizedPresentation.ID);
        if ((filteredTree != null) && (filteredTree.getTextSearchControl() != null)) {
            filteredTree.getTextSearchControl().saveState(memento);
        }
    }

    private void restoreState() {
        if (taskListMemento != null) {
            if (tableSorter != null) {
                org.eclipse.ui.IMemento sorterMemento = taskListMemento.getChild(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.MEMENTO_SORTER);
                if (sorterMemento != null) {
                    tableSorter.restoreState(sorterMemento);
                }
            }
            applyPresentation(taskListMemento.getString(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.MEMENTO_PRESENTATION));
        }
        filterWorkingSet = new org.eclipse.mylyn.internal.tasks.ui.TaskWorkingSetFilter();
        filterWorkingSet.updateWorkingSet(getSite().getPage().getAggregateWorkingSet());
        filteredTree.setWorkingSetFilter(filterWorkingSet);
        addFilter(filterWorkingSet);
        addFilter(filterPriority);
        if (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().contains(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.FILTER_COMPLETE_MODE)) {
            addFilter(filterComplete);
        }
        if (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().contains(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.FILTER_MY_TASKS_MODE)) {
            addFilter(filterMyTasks);
        }
        addFilter(filterPresentation);
        addFilter(filterArchive);
        addFilter(filterArtifact);
        // Restore "link with editor" value; by default true
        boolean linkValue = true;
        if ((taskListMemento != null) && (taskListMemento.getString(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.MEMENTO_LINK_WITH_EDITOR) != null)) {
            linkValue = java.lang.Boolean.parseBoolean(taskListMemento.getString(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.MEMENTO_LINK_WITH_EDITOR));
        }
        setLinkWithEditor(linkValue);
        if ((taskListMemento != null) && (filteredTree.getTextSearchControl() != null)) {
            filteredTree.getTextSearchControl().restoreState(taskListMemento);
        }
        getViewer().refresh();
    }

    @java.lang.Override
    public void createPartControl(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.widgets.Composite body = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NONE);
        org.eclipse.swt.layout.GridLayout layout = new org.eclipse.swt.layout.GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        layout.numColumns = 1;
        body.setLayout(layout);
        org.eclipse.ui.progress.IWorkbenchSiteProgressService progress = ((org.eclipse.ui.progress.IWorkbenchSiteProgressService) (getSite().getAdapter(org.eclipse.ui.progress.IWorkbenchSiteProgressService.class)));
        if (progress != null) {
            // show indicator for all running query synchronizations
            progress.showBusyForFamily(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.JOB_FAMILY_SYNCHRONIZATION);
        }
        this.selectionProvider = new org.eclipse.mylyn.commons.ui.SelectionProviderAdapter();
        getSite().setSelectionProvider(selectionProvider);
        themeManager = getSite().getWorkbenchWindow().getWorkbench().getThemeManager();
        themeManager.addPropertyChangeListener(THEME_CHANGE_LISTENER);
        searchHandler = org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil.createSearchHandler();
        filteredTree = new org.eclipse.mylyn.internal.tasks.ui.views.TaskListFilteredTree(body, (((org.eclipse.swt.SWT.MULTI | org.eclipse.swt.SWT.VERTICAL) | org.eclipse.swt.SWT.V_SCROLL) | org.eclipse.swt.SWT.NO_SCROLL) | org.eclipse.swt.SWT.FULL_SELECTION, searchHandler, getViewSite().getWorkbenchWindow());
        // need to do initialize tooltip early for native tooltip disablement to take effect
        taskListToolTip = new org.eclipse.mylyn.internal.tasks.ui.views.TaskListToolTip(getViewer().getControl());
        updateTooltipEnablement();
        getSite().registerContextMenu(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.ID_MENU_ACTIVE_TASK, filteredTree.getActiveTaskMenuManager(), selectionProvider);
        filteredTree.setActiveTaskSelectionProvider(selectionProvider);
        getViewer().addSelectionChangedListener(this.selectionProvider);
        getViewer().getControl().addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @java.lang.Override
            public void focusGained(org.eclipse.swt.events.FocusEvent e) {
                selectionProvider.setSelection(getViewer().getSelection());
            }
        });
        filteredTree.getFilterControl().addModifyListener(new org.eclipse.swt.events.ModifyListener() {
            public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
                updateFilterEnablement();
            }
        });
        getViewer().getTree().setHeaderVisible(false);
        getViewer().setUseHashlookup(true);
        refreshJob = new org.eclipse.mylyn.internal.tasks.ui.views.TaskListRefreshJob(this, getViewer(), "Task List Refresh");// $NON-NLS-1$

        configureColumns(columnNames, columnWidths);
        final org.eclipse.ui.themes.IThemeManager themeManager = getSite().getWorkbenchWindow().getWorkbench().getThemeManager();
        org.eclipse.swt.graphics.Color categoryBackground = themeManager.getCurrentTheme().getColorRegistry().get(org.eclipse.mylyn.commons.ui.compatibility.CommonThemes.COLOR_CATEGORY);
        taskListTableLabelProvider = new org.eclipse.mylyn.internal.tasks.ui.views.TaskTableLabelProvider(new org.eclipse.mylyn.tasks.ui.TaskElementLabelProvider(true), org.eclipse.ui.PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator(), categoryBackground);
        getViewer().setLabelProvider(taskListTableLabelProvider);
        org.eclipse.jface.viewers.CellEditor[] editors = new org.eclipse.jface.viewers.CellEditor[columnNames.length];
        org.eclipse.jface.viewers.TextCellEditor textEditor = new org.eclipse.jface.viewers.TextCellEditor(getViewer().getTree());
        ((org.eclipse.swt.widgets.Text) (textEditor.getControl())).setOrientation(org.eclipse.swt.SWT.LEFT_TO_RIGHT);
        editors[0] = textEditor;
        // editors[1] = new ComboBoxCellEditor(getViewer().getTree(),
        // editors[2] = new CheckboxCellEditor();
        getViewer().setCellEditors(editors);
        getViewer().setCellModifier(taskListCellModifier);
        tableSorter = new org.eclipse.mylyn.internal.tasks.ui.views.TaskListSorter();
        getViewer().setSorter(tableSorter);
        applyPresentation(org.eclipse.mylyn.internal.tasks.ui.CategorizedPresentation.ID);
        drillDownAdapter = new org.eclipse.ui.part.DrillDownAdapter(getViewer());
        getViewer().setInput(getViewSite());
        final int activationImageOffset = org.eclipse.mylyn.commons.ui.PlatformUiUtil.getTreeImageOffset();
        customDrawer = new org.eclipse.mylyn.internal.tasks.ui.views.CustomTaskListDecorationDrawer(activationImageOffset, false);
        getViewer().getTree().addListener(org.eclipse.swt.SWT.EraseItem, customDrawer);
        getViewer().getTree().addListener(org.eclipse.swt.SWT.PaintItem, customDrawer);
        org.eclipse.swt.widgets.Listener expandListener = new org.eclipse.swt.widgets.Listener() {
            public void handleEvent(org.eclipse.swt.widgets.Event event) {
                lastExpansionTime = java.lang.System.currentTimeMillis();
            }
        };
        getViewer().getTree().addListener(org.eclipse.swt.SWT.Expand, expandListener);
        getViewer().getTree().addListener(org.eclipse.swt.SWT.Collapse, expandListener);
        getViewer().getTree().addMouseListener(new org.eclipse.swt.events.MouseAdapter() {
            @java.lang.Override
            public void mouseUp(org.eclipse.swt.events.MouseEvent event) {
                // avoid activation in case the event was actually triggered as a side-effect of a tree expansion
                long currentTime = java.lang.System.currentTimeMillis();
                if (((currentTime - lastExpansionTime) < 150) && (currentTime >= lastExpansionTime)) {
                    return;
                }
                if (event.button == 3) {
                    // button3 is used for the context menu so we ignore the mouseDown Event
                    return;
                }
                // NOTE: need e.x offset for Linux/GTK, which does not see
                // left-aligned items in tree
                java.lang.Object selectedNode = ((org.eclipse.swt.widgets.Tree) (event.widget)).getItem(new org.eclipse.swt.graphics.Point(event.x + 70, event.y));
                if (selectedNode instanceof org.eclipse.swt.widgets.TreeItem) {
                    java.lang.Object selectedObject = ((org.eclipse.swt.widgets.TreeItem) (selectedNode)).getData();
                    if (selectedObject instanceof org.eclipse.mylyn.tasks.core.ITask) {
                        if ((event.x > activationImageOffset) && (event.x < (activationImageOffset + 13))) {
                            taskListCellModifier.toggleTaskActivation(((org.eclipse.swt.widgets.TreeItem) (selectedNode)), event);
                        }
                    }
                }
            }
        });
        // TODO make these proper commands and move code into TaskListViewCommands
        getViewer().getTree().addKeyListener(new org.eclipse.swt.events.KeyListener() {
            public void keyPressed(org.eclipse.swt.events.KeyEvent e) {
                if ((e.keyCode == org.eclipse.swt.SWT.F2) && (e.stateMask == 0)) {
                    if (actionGroup.getRenameAction().isEnabled()) {
                        actionGroup.getRenameAction().run();
                    }
                } else if ((e.keyCode & org.eclipse.swt.SWT.KEYCODE_BIT) != 0) {
                    // Do nothing here since it is key code
                } else if (e.keyCode == org.eclipse.swt.SWT.ESC) {
                    taskListToolTip.hide();
                } else if ((e.keyCode == 'f') && (e.stateMask == org.eclipse.swt.SWT.MOD1)) {
                    filteredTree.getFilterControl().setFocus();
                } else if (e.stateMask == 0) {
                    if (java.lang.Character.isLetter(((char) (e.keyCode))) || java.lang.Character.isDigit(((char) (e.keyCode)))) {
                        java.lang.String string = new java.lang.Character(((char) (e.keyCode))).toString();
                        filteredTree.getFilterControl().setFocus();
                        filteredTree.getFilterControl().setText(string);
                        filteredTree.getFilterControl().setSelection(1, 1);
                    }
                }
            }

            public void keyReleased(org.eclipse.swt.events.KeyEvent e) {
            }
        });
        // update tooltip contents
        getViewer().addSelectionChangedListener(new org.eclipse.jface.viewers.ISelectionChangedListener() {
            public void selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent event) {
                updateToolTip(true);
            }
        });
        getViewer().getTree().addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @java.lang.Override
            public void focusLost(org.eclipse.swt.events.FocusEvent e) {
                taskListToolTip.hide();
            }
        });
        makeActions();
        hookGlobalActions();
        hookContextMenu();
        hookOpenAction();
        contributeToActionBars();
        initHandlers();
        new org.eclipse.mylyn.commons.workbench.GradientDrawer(themeManager, getViewer()) {
            @java.lang.Override
            protected boolean shouldApplyGradient(org.eclipse.swt.widgets.Event event) {
                return (event.item.getData() instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) && (!(event.item.getData() instanceof org.eclipse.mylyn.tasks.core.ITask));
            }
        };
        initDragAndDrop(parent);
        expandToActiveTasks();
        restoreState();
        updateDescription();
        org.eclipse.ui.contexts.IContextService contextSupport = ((org.eclipse.ui.contexts.IContextService) (getSite().getService(org.eclipse.ui.contexts.IContextService.class)));
        if (contextSupport != null) {
            contextSupport.activateContext(org.eclipse.mylyn.tasks.ui.ITasksUiConstants.ID_VIEW_TASKS);
        }
        getSite().getPage().addPartListener(editorListener);
        // we need to update the icon here as the action was not created when the presentation was applied
        updatePresentationSelectorImage();
        org.eclipse.ui.PlatformUI.getWorkbench().getWorkingSetManager().addPropertyChangeListener(this);
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().addActivityListener(TASK_ACTIVITY_LISTENER);
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().addActivationListener(TASK_ACTIVATION_LISTENER);
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(tasksUiPreferenceListener);
        serviceMessageControl = new org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListServiceMessageControl(body);
        java.util.List<org.eclipse.mylyn.tasks.core.TaskRepository> repos = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getAllRepositories();
        boolean showMessage = true;
        for (org.eclipse.mylyn.tasks.core.TaskRepository repository : repos) {
            if ((!repository.getConnectorKind().equals("local"))// $NON-NLS-1$
             && (!org.eclipse.mylyn.internal.tasks.ui.TaskRepositoryUtil.isAddAutomatically(repository.getRepositoryUrl()))) {
                showMessage = false;
                break;
            }
        }
        boolean showedWelcomeMessage = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.WELCOME_MESSAGE);
        if (showMessage && (!showedWelcomeMessage)) {
            org.eclipse.mylyn.internal.commons.notifications.feed.ServiceMessage message = new org.eclipse.mylyn.internal.commons.notifications.feed.ServiceMessage("welcome");// $NON-NLS-1$

            message.setDescription(Messages.TaskListView_Welcome_Message);
            message.setTitle(Messages.TaskListView_Welcome_Message_Title);
            message.setImage(org.eclipse.jface.dialogs.Dialog.DLG_IMG_MESSAGE_INFO);
            serviceMessageControl.setMessage(message);
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().setValue(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.WELCOME_MESSAGE, java.lang.Boolean.toString(true));
        }
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getServiceMessageManager().addServiceMessageListener(serviceMessageControl);
        // Need to do this because the page, which holds the active working set is not around on creation, see bug 203179
        org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPageListener(PAGE_LISTENER);
    }

    private void initHandlers() {
        org.eclipse.ui.handlers.IHandlerService handlerService = ((org.eclipse.ui.handlers.IHandlerService) (getSite().getService(org.eclipse.ui.handlers.IHandlerService.class)));
        handlerService.activateHandler(org.eclipse.ui.handlers.CollapseAllHandler.COMMAND_ID, new org.eclipse.ui.handlers.CollapseAllHandler(getViewer()));
    }

    private void hookGlobalActions() {
        org.eclipse.ui.IActionBars bars = getViewSite().getActionBars();
        bars.setGlobalActionHandler(org.eclipse.ui.actions.ActionFactory.DELETE.getId(), actionGroup.getDeleteAction());
        bars.setGlobalActionHandler(org.eclipse.ui.actions.ActionFactory.COPY.getId(), actionGroup.getCopyDetailsAction());
        bars.setGlobalActionHandler(org.eclipse.ui.actions.ActionFactory.UNDO.getId(), actionGroup.getUndoAction());
        bars.setGlobalActionHandler(org.eclipse.ui.actions.ActionFactory.REDO.getId(), actionGroup.getRedoAction());
    }

    private void applyPresentation(java.lang.String id) {
        if (id != null) {
            for (org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListPresentation presentation : org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.presentationsPrimary) {
                if (id.equals(presentation.getId())) {
                    applyPresentation(presentation);
                    return;
                }
            }
            for (org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListPresentation presentation : org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.presentationsSecondary) {
                if (id.equals(presentation.getId())) {
                    applyPresentation(presentation);
                    return;
                }
            }
        }
    }

    public void applyPresentation(org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListPresentation presentation) {
        try {
            getViewer().getControl().setRedraw(false);
            if (!filteredTree.getFilterString().equals("")) {
                // $NON-NLS-1$
                filteredTree.getFilterControl().setText("");// $NON-NLS-1$

            }
            org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListContentProvider contentProvider = presentation.getContentProvider(this);
            getViewer().setContentProvider(contentProvider);
            refresh(true);
            currentPresentation = presentation;
            updatePresentationSelectorImage();
            tableSorter.getTaskComparator().presentationChanged(presentation);
        } finally {
            getViewer().getControl().setRedraw(true);
        }
    }

    private void updatePresentationSelectorImage() {
        if ((presentationDropDownSelectionAction != null) && (currentPresentation != null)) {
            presentationDropDownSelectionAction.setImageDescriptor(currentPresentation.getImageDescriptor());
        }
        for (org.eclipse.jface.action.IContributionItem item : getViewSite().getActionBars().getToolBarManager().getItems()) {
            if (item instanceof org.eclipse.jface.action.ActionContributionItem) {
                org.eclipse.jface.action.IAction action = ((org.eclipse.jface.action.ActionContributionItem) (item)).getAction();
                if (action instanceof org.eclipse.mylyn.internal.tasks.ui.actions.PresentationDropDownSelectionAction.PresentationSelectionAction) {
                    ((org.eclipse.mylyn.internal.tasks.ui.actions.PresentationDropDownSelectionAction.PresentationSelectionAction) (action)).update();
                }
            }
        }
    }

    public org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListPresentation getCurrentPresentation() {
        return currentPresentation;
    }

    private void configureColumns(final java.lang.String[] columnNames, final int[] columnWidths) {
        org.eclipse.jface.layout.TreeColumnLayout layout = ((org.eclipse.jface.layout.TreeColumnLayout) (getViewer().getTree().getParent().getLayout()));
        getViewer().setColumnProperties(columnNames);
        columns = new org.eclipse.swt.widgets.TreeColumn[columnNames.length];
        for (int i = 0; i < columnNames.length; i++) {
            columns[i] = new org.eclipse.swt.widgets.TreeColumn(getViewer().getTree(), 0);
            columns[i].setText(columnNames[i]);
            if (i == 0) {
                layout.setColumnData(columns[i], new org.eclipse.jface.viewers.ColumnWeightData(100));
            } else {
                layout.setColumnData(columns[i], new org.eclipse.jface.viewers.ColumnPixelData(columnWidths[i]));
            }
            columns[i].addControlListener(new org.eclipse.swt.events.ControlListener() {
                public void controlResized(org.eclipse.swt.events.ControlEvent e) {
                    for (int j = 0; j < columnWidths.length; j++) {
                        if (columns[j].equals(e.getSource())) {
                            columnWidths[j] = columns[j].getWidth();
                        }
                    }
                }

                public void controlMoved(org.eclipse.swt.events.ControlEvent e) {
                    // don't care if the control is moved
                }
            });
        }
    }

    /**
     * Tracks editor activation and jump to corresponding task, if applicable
     */
    private final org.eclipse.ui.IPartListener editorListener = new org.eclipse.ui.IPartListener() {
        private void jumpToEditor(org.eclipse.ui.IWorkbenchPart part) {
            if ((!linkWithEditor) || (!(part instanceof org.eclipse.ui.IEditorPart))) {
                return;
            }
            jumpToEditorTask(((org.eclipse.ui.IEditorPart) (part)));
        }

        public void partActivated(org.eclipse.ui.IWorkbenchPart part) {
            if (part == TaskListView.this) {
                updateDescription();
            } else {
                jumpToEditor(part);
            }
        }

        public void partBroughtToTop(org.eclipse.ui.IWorkbenchPart part) {
        }

        public void partClosed(org.eclipse.ui.IWorkbenchPart part) {
        }

        public void partDeactivated(org.eclipse.ui.IWorkbenchPart part) {
            if (part == TaskListView.this) {
                org.eclipse.ui.IViewReference reference = getSite().getPage().findViewReference(org.eclipse.mylyn.tasks.ui.ITasksUiConstants.ID_VIEW_TASKS);
                if ((reference != null) && reference.isFastView()) {
                    updateDescription();
                }
                taskListToolTip.hide();
            }
        }

        public void partOpened(org.eclipse.ui.IWorkbenchPart part) {
        }
    };

    private void initDragAndDrop(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.dnd.Transfer[] dragTypes = new org.eclipse.swt.dnd.Transfer[]{ org.eclipse.jface.util.LocalSelectionTransfer.getTransfer(), org.eclipse.swt.dnd.FileTransfer.getInstance() };
        org.eclipse.swt.dnd.Transfer[] dropTypes = new org.eclipse.swt.dnd.Transfer[]{ org.eclipse.jface.util.LocalSelectionTransfer.getTransfer(), org.eclipse.swt.dnd.FileTransfer.getInstance(), org.eclipse.swt.dnd.TextTransfer.getInstance(), org.eclipse.swt.dnd.RTFTransfer.getInstance(), org.eclipse.swt.dnd.URLTransfer.getInstance() };
        getViewer().addDragSupport((org.eclipse.swt.dnd.DND.DROP_COPY | org.eclipse.swt.dnd.DND.DROP_MOVE) | org.eclipse.swt.dnd.DND.DROP_LINK, dragTypes, new org.eclipse.mylyn.internal.tasks.ui.util.TaskDragSourceListener(getViewer()));
        getViewer().addDropSupport(((org.eclipse.swt.dnd.DND.DROP_COPY | org.eclipse.swt.dnd.DND.DROP_MOVE) | org.eclipse.swt.dnd.DND.DROP_LINK) | org.eclipse.swt.dnd.DND.DROP_DEFAULT, dropTypes, new org.eclipse.mylyn.internal.tasks.ui.views.TaskListDropAdapter(getViewer()));
    }

    @java.lang.Override
    protected void expandToActiveTasks() {
        final org.eclipse.ui.IWorkbench workbench = org.eclipse.ui.PlatformUI.getWorkbench();
        workbench.getDisplay().asyncExec(new java.lang.Runnable() {
            public void run() {
                org.eclipse.mylyn.tasks.core.ITask task = org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().getActiveTask();
                if (task != null) {
                    getViewer().expandToLevel(task, 0);
                }
            }
        });
    }

    private void hookContextMenu() {
        org.eclipse.jface.action.MenuManager menuManager = new org.eclipse.jface.action.MenuManager("#PopupMenu");// $NON-NLS-1$

        menuManager.setRemoveAllWhenShown(true);
        menuManager.addMenuListener(new org.eclipse.jface.action.IMenuListener() {
            public void menuAboutToShow(org.eclipse.jface.action.IMenuManager manager) {
                actionGroup.fillContextMenu(manager);
            }
        });
        org.eclipse.swt.widgets.Menu menu = menuManager.createContextMenu(getViewer().getControl());
        getViewer().getControl().setMenu(menu);
        getSite().registerContextMenu(menuManager, getViewer());
    }

    private void contributeToActionBars() {
        org.eclipse.ui.IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown(bars.getMenuManager());
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalPullDown(org.eclipse.jface.action.IMenuManager manager) {
        updateDrillDownActions();
        manager.add(actionGroup.getGoUpAction());
        manager.add(collapseAll);
        manager.add(expandAll);
        manager.add(new org.eclipse.jface.action.Separator(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.ID_SEPARATOR_FILTERS));
        manager.add(sortDialogAction);
        manager.add(filterOnPriorityAction);
        manager.add(filterCompleteTask);
        manager.add(filterMyTasksAction);
        org.eclipse.jface.action.IMenuManager advancedMenu = new org.eclipse.jface.action.MenuManager(Messages.TaskListView_Advanced_Filters_Label);
        advancedMenu.add(new org.eclipse.mylyn.internal.tasks.ui.actions.ShowAllQueriesAction());
        showNonMatchingSubtasksAction = new org.eclipse.mylyn.internal.tasks.ui.actions.ShowNonMatchingSubtasksAction();
        advancedMenu.add(showNonMatchingSubtasksAction);
        advancedMenu.add(new org.eclipse.jface.action.Separator());
        advancedMenu.add(groupSubTasksAction);
        manager.add(advancedMenu);
        manager.add(new org.eclipse.jface.action.Separator(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.ID_SEPARATOR_SEARCH));
        manager.add(new org.eclipse.jface.action.GroupMarker(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.ID_SEPARATOR_TASKS));
        manager.add(synchronizeAutomatically);
        manager.add(new org.eclipse.jface.action.Separator(org.eclipse.ui.IWorkbenchActionConstants.MB_ADDITIONS));
        manager.addMenuListener(new org.eclipse.jface.action.IMenuListener() {
            public void menuAboutToShow(org.eclipse.jface.action.IMenuManager manager) {
                filterOnPriorityAction.updateCheckedState();
            }
        });
        manager.add(linkWithEditorAction);
        manager.add(new org.eclipse.jface.action.Separator());
        manager.add(openPreferencesAction);
    }

    private void fillLocalToolBar(org.eclipse.jface.action.IToolBarManager manager) {
        manager.add(newTaskAction);
        manager.add(new org.eclipse.jface.action.Separator());
        addPresentations(manager);
        manager.add(new org.eclipse.jface.action.Separator());
        manager.add(new org.eclipse.jface.action.GroupMarker(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.ID_SEPARATOR_CONTEXT));
        manager.add(new org.eclipse.jface.action.Separator());
        manager.add(filterCompleteTask);
        manager.add(filterMyTasksAction);
        manager.add(collapseAll);
        manager.add(new org.eclipse.jface.action.Separator(org.eclipse.ui.IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void addPresentations(org.eclipse.jface.action.IToolBarManager manager) {
        for (org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListPresentation presentation : org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.getPresentations()) {
            if (!presentation.isPrimary()) {
                // at least one non primary presentation present
                manager.add(presentationDropDownSelectionAction);
                return;
            }
        }
        // add toggle buttons for primary presentations
        for (org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListPresentation presentation : org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.getPresentations()) {
            manager.add(new org.eclipse.mylyn.internal.tasks.ui.actions.PresentationDropDownSelectionAction.PresentationSelectionAction(this, presentation));
        }
    }

    public java.util.List<org.eclipse.mylyn.tasks.core.IRepositoryElement> getSelectedTaskContainers() {
        java.util.List<org.eclipse.mylyn.tasks.core.IRepositoryElement> selectedElements = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.IRepositoryElement>();
        for (java.util.Iterator<?> i = ((org.eclipse.jface.viewers.IStructuredSelection) (getViewer().getSelection())).iterator(); i.hasNext();) {
            java.lang.Object object = i.next();
            if (object instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) {
                selectedElements.add(((org.eclipse.mylyn.tasks.core.IRepositoryElement) (object)));
            }
        }
        return selectedElements;
    }

    private void makeActions() {
        actionGroup = new org.eclipse.mylyn.internal.tasks.ui.actions.TaskListViewActionGroup(this, drillDownAdapter);
        actionGroup.getOpenAction().setViewer(getViewer());
        collapseAll = new org.eclipse.mylyn.internal.tasks.ui.actions.CollapseAllAction(this);
        expandAll = new org.eclipse.mylyn.internal.tasks.ui.actions.ExpandAllAction(this);
        filterCompleteTask = new org.eclipse.mylyn.internal.tasks.ui.actions.FilterCompletedTasksAction(this);
        groupSubTasksAction = new org.eclipse.mylyn.internal.tasks.ui.actions.GroupSubTasksAction();
        synchronizeAutomatically = new org.eclipse.mylyn.internal.tasks.ui.actions.SynchronizeAutomaticallyAction();
        openPreferencesAction = new org.eclipse.mylyn.internal.tasks.ui.actions.OpenTasksUiPreferencesAction();
        // filterArchiveCategory = new FilterArchiveContainerAction(this);
        filterMyTasksAction = new org.eclipse.mylyn.internal.tasks.ui.actions.FilterMyTasksAction(this);
        sortDialogAction = new org.eclipse.mylyn.internal.tasks.ui.actions.TaskListSortAction(getSite(), this);
        filterOnPriorityAction = new org.eclipse.mylyn.internal.tasks.ui.views.PriorityDropDownAction(this);
        linkWithEditorAction = new org.eclipse.mylyn.internal.tasks.ui.actions.LinkWithEditorAction(this);
        newTaskAction = new org.eclipse.mylyn.internal.tasks.ui.actions.NewTaskAction();
        filteredTree.getViewer().addSelectionChangedListener(newTaskAction);
    }

    private void hookOpenAction() {
        getViewer().addOpenListener(new org.eclipse.jface.viewers.IOpenListener() {
            public void open(org.eclipse.jface.viewers.OpenEvent event) {
                actionGroup.getOpenAction().run();
            }
        });
        getViewer().addDoubleClickListener(new org.eclipse.jface.viewers.IDoubleClickListener() {
            public void doubleClick(org.eclipse.jface.viewers.DoubleClickEvent event) {
                if (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.ACTIVATE_WHEN_OPENED)) {
                    org.eclipse.mylyn.internal.tasks.core.AbstractTask selectedTask = getSelectedTask();
                    if ((selectedTask != null) && (!selectedTask.isActive())) {
                        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.activateTaskThroughCommand(selectedTask);
                    }
                }
            }
        });
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    @java.lang.Override
    public void setFocus() {
        filteredTree.getViewer().getControl().setFocus();
    }

    public void refresh(boolean expandIfFocused) {
        if ((expandIfFocused && isFocusedMode()) && isAutoExpandMode()) {
            try {
                getViewer().getControl().setRedraw(false);
                refreshJob.refreshNow();
                getViewer().expandAll();
            } finally {
                getViewer().getControl().setRedraw(true);
            }
        } else {
            refreshJob.refreshNow();
        }
    }

    @java.lang.Override
    public void refresh() {
        refreshJob.refreshNow();
    }

    public org.eclipse.mylyn.internal.tasks.ui.views.TaskListToolTip getToolTip() {
        return taskListToolTip;
    }

    @java.lang.Override
    public org.eclipse.jface.viewers.TreeViewer getViewer() {
        return filteredTree.getViewer();
    }

    public org.eclipse.mylyn.internal.tasks.ui.TaskCompletionFilter getCompleteFilter() {
        return filterComplete;
    }

    public org.eclipse.mylyn.internal.tasks.ui.TaskPriorityFilter getPriorityFilter() {
        return filterPriority;
    }

    public void addFilter(org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter filter) {
        if (!filters.contains(filter)) {
            filters.add(filter);
        }
    }

    public java.util.Set<org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter> clearFilters() {
        java.util.HashSet<org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter> previousFilters = new java.util.HashSet<org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter>(getFilters());
        previousFilters.remove(filterMyTasks);// this filter is always available for users to toggle

        filters.clear();
        filters.add(filterArchive);
        filters.add(filterWorkingSet);
        filters.add(filterPresentation);
        boolean enableMyTasksFilter = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.FILTER_MY_TASKS_MODE);
        if (enableMyTasksFilter) {
            filters.add(getMyTasksFilter());
        }
        return previousFilters;
    }

    public void removeFilter(org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter filter) {
        filters.remove(filter);
    }

    public void updateDrillDownActions() {
        actionGroup.updateDrillDownActions();
    }

    boolean isInRenameAction = false;

    private org.eclipse.mylyn.internal.tasks.ui.views.TaskListRefreshJob refreshJob;

    private boolean itemNotFoundExceptionLogged;

    private org.eclipse.mylyn.internal.tasks.ui.search.AbstractSearchHandler searchHandler;

    public void setInRenameAction(boolean b) {
        isInRenameAction = b;
    }

    public void goIntoCategory() {
        org.eclipse.jface.viewers.ISelection selection = getViewer().getSelection();
        if (selection instanceof org.eclipse.jface.viewers.StructuredSelection) {
            org.eclipse.jface.viewers.StructuredSelection structuredSelection = ((org.eclipse.jface.viewers.StructuredSelection) (selection));
            java.lang.Object element = structuredSelection.getFirstElement();
            if (element instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) {
                drilledIntoCategory = ((org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer) (element));
                drillDownAdapter.goInto();
                org.eclipse.ui.IActionBars bars = getViewSite().getActionBars();
                bars.getToolBarManager().remove(actionGroup.getGoUpAction().getId());
                bars.getToolBarManager().add(actionGroup.getGoUpAction());
                bars.updateActionBars();
                updateDrillDownActions();
            }
        }
    }

    public void goUpToRoot() {
        drilledIntoCategory = null;
        drillDownAdapter.goBack();
        org.eclipse.ui.IActionBars bars = getViewSite().getActionBars();
        bars.getToolBarManager().remove(org.eclipse.mylyn.internal.tasks.ui.actions.GoUpAction.ID);
        bars.updateActionBars();
        updateDrillDownActions();
    }

    public org.eclipse.mylyn.internal.tasks.core.AbstractTask getSelectedTask() {
        org.eclipse.jface.viewers.ISelection selection = getViewer().getSelection();
        if (selection.isEmpty()) {
            return null;
        }
        if (selection instanceof org.eclipse.jface.viewers.StructuredSelection) {
            org.eclipse.jface.viewers.StructuredSelection structuredSelection = ((org.eclipse.jface.viewers.StructuredSelection) (selection));
            java.lang.Object element = structuredSelection.getFirstElement();
            if (element instanceof org.eclipse.mylyn.tasks.core.ITask) {
                return ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (structuredSelection.getFirstElement()));
            }
        }
        return null;
    }

    public static org.eclipse.mylyn.internal.tasks.core.AbstractTask getSelectedTask(org.eclipse.jface.viewers.ISelection selection) {
        if (selection instanceof org.eclipse.jface.viewers.StructuredSelection) {
            org.eclipse.jface.viewers.StructuredSelection structuredSelection = ((org.eclipse.jface.viewers.StructuredSelection) (selection));
            if (structuredSelection.size() != 1) {
                return null;
            }
            java.lang.Object element = structuredSelection.getFirstElement();
            if (element instanceof org.eclipse.mylyn.tasks.core.ITask) {
                return ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (structuredSelection.getFirstElement()));
            }
        }
        return null;
    }

    public void indicatePaused(boolean paused) {
        isPaused = paused;
        org.eclipse.jface.action.IStatusLineManager statusLineManager = getViewSite().getActionBars().getStatusLineManager();
        if (isPaused) {
            statusLineManager.setMessage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.TASKS_VIEW), Messages.TaskListView_Mylyn_context_capture_paused);
            setPartName(Messages.TaskListView__paused_ + org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.LABEL_VIEW);
        } else {
            statusLineManager.setMessage("");// $NON-NLS-1$

            setPartName(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.LABEL_VIEW);
        }
    }

    public org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer getDrilledIntoCategory() {
        return drilledIntoCategory;
    }

    @java.lang.Override
    public org.eclipse.mylyn.internal.tasks.ui.views.TaskListFilteredTree getFilteredTree() {
        return filteredTree;
    }

    public void selectedAndFocusTask(org.eclipse.mylyn.tasks.core.ITask task) {
        if ((task == null) || getViewer().getControl().isDisposed()) {
            return;
        }
        saveSelection();
        org.eclipse.jface.viewers.IStructuredSelection selection = restoreSelection(task);
        try {
            getViewer().setSelection(selection, true);
        } catch (org.eclipse.swt.SWTError e) {
            if (!itemNotFoundExceptionLogged) {
                itemNotFoundExceptionLogged = true;
                // It's probably not worth displaying this to the user since the item
                // is not there in this case, so consider removing.
                org.eclipse.mylyn.commons.core.StatusHandler.log(// $NON-NLS-1$
                new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.WARNING, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Could not link Task List with editor", e));
            }
        }
    }

    /**
     * Persists the path of a selection.
     */
    private void saveSelection() {
        org.eclipse.jface.viewers.IStructuredSelection selection = ((org.eclipse.jface.viewers.IStructuredSelection) (getViewer().getSelection()));
        if ((selection.size() == 1) && (selection.getFirstElement() instanceof org.eclipse.mylyn.tasks.core.ITask)) {
            // make sure the new selection is inserted at the end of the
            // list
            java.lang.String handle = ((org.eclipse.mylyn.tasks.core.IRepositoryElement) (selection.getFirstElement())).getHandleIdentifier();
            lastSelectionByTaskHandle.remove(handle);
            lastSelectionByTaskHandle.put(handle, selection);
            if (lastSelectionByTaskHandle.size() > org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.SIZE_MAX_SELECTION_HISTORY) {
                java.util.Iterator<java.lang.String> it = lastSelectionByTaskHandle.keySet().iterator();
                it.next();
                it.remove();
            }
        }
    }

    /**
     * If <code>task</code> was previously selected, a tree selection is returned that references the same path that was
     * previously selected.
     */
    private org.eclipse.jface.viewers.IStructuredSelection restoreSelection(org.eclipse.mylyn.tasks.core.ITask task) {
        org.eclipse.jface.viewers.IStructuredSelection selection = lastSelectionByTaskHandle.get(task.getHandleIdentifier());
        if (selection != null) {
            return selection;
        } else {
            return new org.eclipse.jface.viewers.StructuredSelection(task);
        }
    }

    public org.eclipse.swt.graphics.Image[] getPirorityImages() {
        org.eclipse.swt.graphics.Image[] images = new org.eclipse.swt.graphics.Image[org.eclipse.mylyn.tasks.core.ITask.PriorityLevel.values().length];
        for (int i = 0; i < org.eclipse.mylyn.tasks.core.ITask.PriorityLevel.values().length; i++) {
            images[i] = org.eclipse.mylyn.tasks.ui.TasksUiImages.getImageForPriority(org.eclipse.mylyn.tasks.core.ITask.PriorityLevel.values()[i]);
        }
        return images;
    }

    @java.lang.Override
    public java.util.Set<org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter> getFilters() {
        return filters;
    }

    public static java.lang.String getCurrentPriorityLevel() {
        if (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().contains(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.FILTER_PRIORITY)) {
            return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().getString(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.FILTER_PRIORITY);
        } else {
            return org.eclipse.mylyn.tasks.core.ITask.PriorityLevel.P5.toString();
        }
    }

    public org.eclipse.mylyn.internal.tasks.ui.TaskArchiveFilter getArchiveFilter() {
        return filterArchive;
    }

    private void updateFilterEnablement() {
        boolean enabled = !isFocusedMode();
        if (enabled) {
            java.lang.String filterText = filteredTree.getFilterString();
            if ((filterText != null) && (filterText.length() > 0)) {
                enabled = false;
            }
        }
        sortDialogAction.setEnabled(enabled);
        filterOnPriorityAction.setEnabled(enabled);
        filterCompleteTask.setEnabled(enabled);
        // filterArchiveCategory.setEnabled(enabled);
    }

    @java.lang.Override
    public boolean isScheduledPresentation() {
        return (currentPresentation != null) && org.eclipse.mylyn.internal.tasks.ui.ScheduledPresentation.ID.equals(currentPresentation.getId());
    }

    @java.lang.Override
    public boolean isFocusedMode() {
        return focusedMode;
    }

    @java.lang.Override
    protected boolean isAutoExpandMode() {
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.AUTO_EXPAND_TASK_LIST);
    }

    public void setFocusedMode(boolean focusedMode) {
        if (this.focusedMode == focusedMode) {
            return;
        }
        this.focusedMode = focusedMode;
        customDrawer.setFocusedMode(focusedMode);
        org.eclipse.jface.action.IToolBarManager manager = getViewSite().getActionBars().getToolBarManager();
        org.eclipse.jface.action.ToolBarManager toolBarManager = getToolBarManager(manager);
        try {
            if (toolBarManager != null) {
                toolBarManager.getControl().setRedraw(false);
            }
            if (focusedMode && isAutoExpandMode()) {
                manager.remove(org.eclipse.mylyn.internal.tasks.ui.actions.FilterCompletedTasksAction.ID);
                manager.remove(org.eclipse.mylyn.internal.tasks.ui.actions.CollapseAllAction.ID);
            } else if (manager.find(org.eclipse.mylyn.internal.tasks.ui.actions.CollapseAllAction.ID) == null) {
                manager.prependToGroup(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.ID_SEPARATOR_CONTEXT, collapseAll);
                manager.prependToGroup(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.ID_SEPARATOR_CONTEXT, filterCompleteTask);
            }
            updateFilterEnablement();
            getViewSite().getActionBars().updateActionBars();
        } finally {
            if (toolBarManager != null) {
                toolBarManager.getControl().setRedraw(true);
            }
        }
    }

    private org.eclipse.jface.action.ToolBarManager getToolBarManager(org.eclipse.jface.action.IToolBarManager manager) {
        if (((manager instanceof org.eclipse.jface.action.ToolBarManager) && (((org.eclipse.jface.action.ToolBarManager) (manager)).getControl() != null)) && (!((org.eclipse.jface.action.ToolBarManager) (manager)).getControl().isDisposed())) {
            return ((org.eclipse.jface.action.ToolBarManager) (manager));
        }
        return null;
    }

    public void displayPrioritiesAbove(java.lang.String priority) {
        filterPriority.displayPrioritiesAbove(priority);
        getViewer().refresh();
    }

    public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
        java.lang.String property = event.getProperty();
        if (org.eclipse.ui.IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE.equals(property) || org.eclipse.ui.IWorkingSetManager.CHANGE_WORKING_SET_REMOVE.equals(property)) {
            if ((getSite() != null) && (getSite().getPage() != null)) {
                if (filterWorkingSet.updateWorkingSet(getSite().getPage().getAggregateWorkingSet())) {
                    try {
                        getViewer().getControl().setRedraw(false);
                        if (drilledIntoCategory != null) {
                            goUpToRoot();
                        }
                        getViewer().refresh();
                        if (isFocusedMode() && isAutoExpandMode()) {
                            getViewer().expandAll();
                        }
                    } finally {
                        getViewer().getControl().setRedraw(true);
                    }
                }
            }
            org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                public void run() {
                    filteredTree.indicateActiveTaskWorkingSet();
                }
            });
        }
    }

    public void setLinkWithEditor(boolean linkWithEditor) {
        this.linkWithEditor = linkWithEditor;
        linkWithEditorAction.setChecked(linkWithEditor);
        if (linkWithEditor) {
            org.eclipse.ui.IEditorPart activeEditor = getSite().getPage().getActiveEditor();
            if (activeEditor != null) {
                jumpToEditorTask(activeEditor);
            }
        }
    }

    private void jumpToEditorTask(org.eclipse.ui.IEditorPart editor) {
        org.eclipse.ui.IEditorInput input = editor.getEditorInput();
        if (input instanceof org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput) {
            org.eclipse.mylyn.tasks.core.ITask task = ((org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput) (input)).getTask();
            org.eclipse.mylyn.tasks.core.ITask selected = getSelectedTask();
            if ((selected == null) || (!selected.equals(task))) {
                selectedAndFocusTask(task);
            }
        }
    }

    @java.lang.Override
    protected void updateToolTip(boolean force) {
        if ((taskListToolTip != null) && taskListToolTip.isVisible()) {
            if ((!force) && taskListToolTip.isTriggeredByMouse()) {
                return;
            }
            org.eclipse.swt.widgets.TreeItem[] selection = getViewer().getTree().getSelection();
            if ((selection != null) && (selection.length > 0)) {
                org.eclipse.swt.graphics.Rectangle bounds = selection[0].getBounds();
                taskListToolTip.show(new org.eclipse.swt.graphics.Point(bounds.x + 1, bounds.y + 1));
            }
        }
    }

    /**
     * This can be used for experimentally adding additional presentations, but note that this convention is extremely
     * likely to change in the Mylyn 3.0 cycle.
     */
    public static java.util.List<org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListPresentation> getPresentations() {
        java.util.List<org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListPresentation> presentations = new java.util.ArrayList<org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListPresentation>();
        presentations.addAll(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.presentationsPrimary);
        presentations.addAll(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.presentationsSecondary);
        return presentations;
    }

    public static void addPresentation(org.eclipse.mylyn.internal.tasks.ui.views.AbstractTaskListPresentation presentation) {
        if (presentation.isPrimary()) {
            org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.presentationsPrimary.add(presentation);
        } else {
            org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.presentationsSecondary.add(presentation);
        }
    }

    public org.eclipse.mylyn.internal.tasks.ui.views.TaskListSorter getSorter() {
        return tableSorter;
    }

    public boolean show(org.eclipse.ui.part.ShowInContext context) {
        org.eclipse.jface.viewers.ISelection selection = context.getSelection();
        if (selection instanceof org.eclipse.jface.viewers.IStructuredSelection) {
            getViewer().setSelection(selection, true);
            return true;
        }
        return false;
    }

    private void updateTooltipEnablement() {
        // Set to empty string to disable native tooltips (windows only?)
        // bug#160897
        // http://dev.eclipse.org/newslists/news.eclipse.platform.swt/msg29614.html
        if (taskListToolTip != null) {
            boolean enabled = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.TASK_LIST_TOOL_TIPS_ENABLED);
            taskListToolTip.setEnabled(enabled);
            if ((getViewer().getTree() != null) && (!getViewer().getTree().isDisposed())) {
                getViewer().getTree().setToolTipText(enabled ? "" : null);// $NON-NLS-1$

            }
        }
    }

    @java.lang.SuppressWarnings("rawtypes")
    @java.lang.Override
    public java.lang.Object getAdapter(java.lang.Class adapter) {
        if (adapter == org.eclipse.ui.ISizeProvider.class) {
            return new org.eclipse.ui.ISizeProvider() {
                public int getSizeFlags(boolean width) {
                    if (width) {
                        return org.eclipse.swt.SWT.MIN;
                    }
                    return 0;
                }

                public int computePreferredSize(boolean width, int availableParallel, int availablePerpendicular, int preferredResult) {
                    if (width) {
                        if (getViewSite().getActionBars().getToolBarManager() instanceof org.eclipse.jface.action.ToolBarManager) {
                            org.eclipse.swt.graphics.Point size = ((org.eclipse.jface.action.ToolBarManager) (getViewSite().getActionBars().getToolBarManager())).getControl().computeSize(org.eclipse.swt.SWT.DEFAULT, org.eclipse.swt.SWT.DEFAULT);
                            // leave some room for the view menu drop-down
                            return size.x + org.eclipse.mylyn.commons.ui.PlatformUiUtil.getViewMenuWidth();
                        }
                    }
                    return preferredResult;
                }
            };
        } else if (adapter == org.eclipse.ui.part.IShowInTargetList.class) {
            return new org.eclipse.ui.part.IShowInTargetList() {
                public java.lang.String[] getShowInTargetIds() {
                    return new java.lang.String[]{ "org.eclipse.team.ui.GenericHistoryView" };// $NON-NLS-1$

                }
            };
        } else if (adapter == org.eclipse.ui.part.IShowInSource.class) {
            return new org.eclipse.ui.part.IShowInSource() {
                public org.eclipse.ui.part.ShowInContext getShowInContext() {
                    return new org.eclipse.ui.part.ShowInContext(getViewer().getInput(), getViewer().getSelection());
                }
            };
        }
        return super.getAdapter(adapter);
    }

    public org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListServiceMessageControl getServiceMessageControl() {
        return serviceMessageControl;
    }

    public org.eclipse.mylyn.internal.tasks.ui.MyTasksFilter getMyTasksFilter() {
        return filterMyTasks;
    }
}