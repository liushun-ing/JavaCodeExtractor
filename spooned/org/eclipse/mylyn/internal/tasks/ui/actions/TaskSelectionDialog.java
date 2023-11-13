/**
 * *****************************************************************************
 * Copyright (c) 2004, 2013 Willian Mitsuda and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Willian Mitsuda - initial API and implementation
 *     Tasktop Technologies - improvements
 *     Eugene Kuleshov - improvements
 *     Benjamin Muskalla - fix for bug 291992
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.actions;
import com.google.common.base.Strings;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.commons.ui.compatibility.CommonColors;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.TaskActivationHistory;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskDetailLabelProvider;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListFilteredTree;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater;
import org.eclipse.mylyn.internal.tasks.ui.workingsets.WorkingSetLabelComparator;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskContainer;
import org.eclipse.mylyn.tasks.ui.TaskElementLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.eclipse.ui.dialogs.IWorkingSetEditWizard;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;
import org.eclipse.ui.dialogs.SearchPattern;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
/**
 *
 * @author Willian Mitsuda
 * @author Mik Kersten
 * @author Eugene Kuleshov
 * @author Shawn Minto
 */
public class TaskSelectionDialog extends org.eclipse.ui.dialogs.FilteredItemsSelectionDialog {
    private class DeselectWorkingSetAction extends org.eclipse.jface.action.Action {
        public DeselectWorkingSetAction() {
            super(Messages.TaskSelectionDialog_Deselect_Working_Set, org.eclipse.jface.action.IAction.AS_PUSH_BUTTON);
        }

        @java.lang.Override
        public void run() {
            setSelectedWorkingSet(null);
        }
    }

    private class EditWorkingSetAction extends org.eclipse.jface.action.Action {
        public EditWorkingSetAction() {
            super(Messages.TaskSelectionDialog_Edit_Active_Working_Set_, org.eclipse.jface.action.IAction.AS_PUSH_BUTTON);
        }

        @java.lang.Override
        public void run() {
            org.eclipse.ui.dialogs.IWorkingSetEditWizard wizard = org.eclipse.ui.PlatformUI.getWorkbench().getWorkingSetManager().createWorkingSetEditWizard(selectedWorkingSet);
            if (wizard != null) {
                org.eclipse.jface.wizard.WizardDialog dlg = new org.eclipse.jface.wizard.WizardDialog(getShell(), wizard);
                dlg.open();
            }
        }
    }

    private class FilterWorkingSetAction extends org.eclipse.jface.action.Action {
        private final org.eclipse.ui.IWorkingSet workingSet;

        public FilterWorkingSetAction(org.eclipse.ui.IWorkingSet workingSet, int shortcutKeyNumber) {
            super("", org.eclipse.jface.action.IAction.AS_RADIO_BUTTON);// $NON-NLS-1$

            this.workingSet = workingSet;
            if ((shortcutKeyNumber >= 1) && (shortcutKeyNumber <= 9)) {
                setText((("&" + java.lang.String.valueOf(shortcutKeyNumber)) + " ") + workingSet.getLabel());// $NON-NLS-1$ //$NON-NLS-2$

            } else {
                setText(workingSet.getLabel());
            }
            setImageDescriptor(workingSet.getImageDescriptor());
        }

        @java.lang.Override
        public void run() {
            setSelectedWorkingSet(workingSet);
        }
    }

    private class SelectWorkingSetAction extends org.eclipse.jface.action.Action {
        public SelectWorkingSetAction() {
            super(Messages.TaskSelectionDialog_Select_Working_Set_, org.eclipse.jface.action.IAction.AS_PUSH_BUTTON);
        }

        @java.lang.Override
        public void run() {
            org.eclipse.ui.dialogs.IWorkingSetSelectionDialog dlg = org.eclipse.ui.PlatformUI.getWorkbench().getWorkingSetManager().createWorkingSetSelectionDialog(getShell(), false, new java.lang.String[]{ org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater.ID_TASK_WORKING_SET });
            if (selectedWorkingSet != null) {
                dlg.setSelection(new org.eclipse.ui.IWorkingSet[]{ selectedWorkingSet });
            }
            if (dlg.open() == org.eclipse.jface.window.Window.OK) {
                org.eclipse.ui.IWorkingSet[] selection = dlg.getSelection();
                if (selection.length == 0) {
                    setSelectedWorkingSet(null);
                } else {
                    setSelectedWorkingSet(selection[0]);
                }
            }
        }
    }

    private class ShowCompletedTasksAction extends org.eclipse.jface.action.Action {
        public ShowCompletedTasksAction() {
            super(Messages.TaskSelectionDialog_Show_Completed_Tasks, org.eclipse.jface.action.IAction.AS_CHECK_BOX);
        }

        @java.lang.Override
        public void run() {
            showCompletedTasks = isChecked();
            applyFilter();
        }
    }

    private class TaskHistoryItemsComparator implements java.util.Comparator<java.lang.Object> {
        java.util.Map<org.eclipse.mylyn.internal.tasks.core.AbstractTask, java.lang.Integer> positionByTask = new java.util.HashMap<org.eclipse.mylyn.internal.tasks.core.AbstractTask, java.lang.Integer>();

        public TaskHistoryItemsComparator(java.util.List<org.eclipse.mylyn.internal.tasks.core.AbstractTask> history) {
            for (int i = 0; i < history.size(); i++) {
                positionByTask.put(history.get(i), i);
            }
        }

        public int compare(java.lang.Object o1, java.lang.Object o2) {
            java.lang.Integer p1 = positionByTask.get(o1);
            java.lang.Integer p2 = positionByTask.get(o2);
            if ((p1 != null) && (p2 != null)) {
                return p2.compareTo(p1);
            }
            return labelProvider.getText(o1).compareTo(labelProvider.getText(o2));
        }
    }

    /**
     * Integrates {@link FilteredItemsSelectionDialog} history management with Mylyn's task list activation history
     * <p>
     * Due to {@link SelectionHistory} use of memento-based history storage, many methods are overridden
     */
    private class TaskSelectionHistory extends org.eclipse.mylyn.internal.tasks.ui.actions.SelectionHistory {
        @java.lang.Override
        public synchronized void accessed(java.lang.Object object) {
            // ignore, handled by TaskActivationHistory
        }

        @java.lang.Override
        public synchronized boolean contains(java.lang.Object object) {
            return history.contains(object);
        }

        @java.lang.Override
        public synchronized java.lang.Object[] getHistoryItems() {
            return history.toArray();
        }

        @java.lang.Override
        public synchronized boolean isEmpty() {
            return history.isEmpty();
        }

        @java.lang.Override
        public void load(org.eclipse.ui.IMemento memento) {
            // do nothing because tasklist history handles this
        }

        @java.lang.Override
        public synchronized boolean remove(java.lang.Object object) {
            taskActivationHistory.removeTask(((org.eclipse.mylyn.tasks.core.ITask) (object)));
            return history.remove(object);
        }

        @java.lang.Override
        protected java.lang.Object restoreItemFromMemento(org.eclipse.ui.IMemento memento) {
            // do nothing because tasklist history handles this
            return null;
        }

        @java.lang.Override
        public void save(org.eclipse.ui.IMemento memento) {
            // do nothing because tasklist history handles this
        }

        @java.lang.Override
        protected void storeItemToMemento(java.lang.Object item, org.eclipse.ui.IMemento memento) {
            // do nothing because tasklist history handles this
        }
    }

    /**
     * Supports filtering of completed tasks.
     */
    private class TasksFilter extends org.eclipse.mylyn.internal.tasks.ui.actions.ItemsFilter {
        private java.util.Set<org.eclipse.mylyn.tasks.core.ITask> allTasksFromWorkingSets;

        /**
         * Stores the task containers from selected working set; empty, which can come from no working set selection or
         * working set with no task containers selected, means no filtering
         */
        private final java.util.Set<org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer> elements;

        private final boolean showCompletedTasks;

        public TasksFilter(boolean showCompletedTasks, org.eclipse.ui.IWorkingSet selectedWorkingSet) {
            super(new org.eclipse.ui.dialogs.SearchPattern());
            // Little hack to force always a match inside any part of task text
            patternMatcher.setPattern("*" + patternMatcher.getPattern());// $NON-NLS-1$

            this.showCompletedTasks = showCompletedTasks;
            elements = new java.util.HashSet<org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer>();
            if (selectedWorkingSet != null) {
                for (org.eclipse.core.runtime.IAdaptable adaptable : selectedWorkingSet.getElements()) {
                    org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer container = ((org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer) (adaptable.getAdapter(org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer.class)));
                    if (container != null) {
                        elements.add(container);
                    }
                }
            }
        }

        @java.lang.Override
        public boolean equalsFilter(org.eclipse.mylyn.internal.tasks.ui.actions.ItemsFilter filter) {
            if (!super.equalsFilter(filter)) {
                return false;
            }
            if (filter instanceof org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.TasksFilter) {
                org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.TasksFilter tasksFilter = ((org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.TasksFilter) (filter));
                if (showCompletedTasks != tasksFilter.showCompletedTasks) {
                    return false;
                }
                return elements.equals(tasksFilter.elements);
            }
            return true;
        }

        @java.lang.Override
        public boolean isConsistentItem(java.lang.Object item) {
            return item instanceof org.eclipse.mylyn.tasks.core.ITask;
        }

        @java.lang.Override
        public boolean isSubFilter(org.eclipse.mylyn.internal.tasks.ui.actions.ItemsFilter filter) {
            if (!super.isSubFilter(filter)) {
                return false;
            }
            if (filter instanceof org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.TasksFilter) {
                org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.TasksFilter tasksFilter = ((org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.TasksFilter) (filter));
                if ((!showCompletedTasks) && tasksFilter.showCompletedTasks) {
                    return false;
                }
                if (elements.isEmpty()) {
                    return true;
                }
                if (tasksFilter.elements.isEmpty()) {
                    return false;
                }
                return elements.containsAll(tasksFilter.elements);
            }
            return true;
        }

        @java.lang.Override
        public boolean matchItem(java.lang.Object item) {
            if (!(item instanceof org.eclipse.mylyn.tasks.core.ITask)) {
                return false;
            }
            if ((!showCompletedTasks) && ((org.eclipse.mylyn.tasks.core.ITask) (item)).isCompleted()) {
                return false;
            }
            if (!elements.isEmpty()) {
                if (allTasksFromWorkingSets == null) {
                    populateTasksFromWorkingSets();
                }
                if (!allTasksFromWorkingSets.contains(item)) {
                    return false;
                }
            }
            return matches(labelProvider.getText(item));
        }

        private void populateTasksFromWorkingSets() {
            allTasksFromWorkingSets = new java.util.HashSet<org.eclipse.mylyn.tasks.core.ITask>(1000);
            for (org.eclipse.mylyn.tasks.core.ITaskContainer container : elements) {
                allTasksFromWorkingSets.addAll(container.getChildren());
            }
        }
    }

    private static final int SEARCH_ID = org.eclipse.jface.dialogs.IDialogConstants.CLIENT_ID + 1;

    private static final int CREATE_ID = org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.SEARCH_ID + 1;

    private static final java.lang.String IS_USING_WINDOW_WORKING_SET_SETTING = "IsUsingWindowWorkingSet";// $NON-NLS-1$


    private static final java.lang.String OPEN_IN_BROWSER_SETTING = "OpenInBrowser";// $NON-NLS-1$


    private static final java.lang.String SHOW_COMPLETED_TASKS_SETTING = "ShowCompletedTasks";// $NON-NLS-1$


    private static final java.lang.String TASK_SELECTION_DIALOG_SECTION = "TaskSelectionDialogSection";// $NON-NLS-1$


    private static final java.lang.String WORKING_SET_NAME_SETTING = "WorkingSetName";// $NON-NLS-1$


    /**
     * Caches all tasks; populated at first access
     */
    private java.util.Set<org.eclipse.mylyn.internal.tasks.core.AbstractTask> allTasks;

    private org.eclipse.swt.widgets.Button createTaskButton;

    /**
     * Mylyn's task activation history
     */
    private final java.util.LinkedHashSet<org.eclipse.mylyn.internal.tasks.core.AbstractTask> history;

    private final org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.TaskHistoryItemsComparator itemsComparator;

    private final org.eclipse.mylyn.tasks.ui.TaskElementLabelProvider labelProvider;

    private boolean needsCreateTask;

    private boolean openInBrowser;

    private org.eclipse.swt.widgets.Button openInBrowserCheck;

    /**
     * Set of filtered working sets
     */
    private org.eclipse.ui.IWorkingSet selectedWorkingSet;

    private boolean showCompletedTasks;

    private final org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.ShowCompletedTasksAction showCompletedTasksAction;

    private boolean showExtendedOpeningOptions;

    /**
     * Caches the window working set
     */
    private final org.eclipse.ui.IWorkingSet windowWorkingSet;

    /**
     * Refilters if the current working set content has changed
     */
    private final org.eclipse.jface.util.IPropertyChangeListener workingSetListener = new org.eclipse.jface.util.IPropertyChangeListener() {
        public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
            if (event.getProperty().equals(org.eclipse.ui.IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE)) {
                if (event.getNewValue().equals(selectedWorkingSet)) {
                    applyFilter();
                }
            }
        }
    };

    private final org.eclipse.mylyn.internal.tasks.core.TaskActivationHistory taskActivationHistory;

    public TaskSelectionDialog(org.eclipse.swt.widgets.Shell parent) {
        this(parent, false);
    }

    public TaskSelectionDialog(org.eclipse.swt.widgets.Shell parent, boolean multi) {
        super(parent, multi);
        this.taskActivationHistory = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().getTaskActivationHistory();
        this.history = new java.util.LinkedHashSet<org.eclipse.mylyn.internal.tasks.core.AbstractTask>(taskActivationHistory.getPreviousTasks());
        this.itemsComparator = new org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.TaskHistoryItemsComparator(new java.util.ArrayList<org.eclipse.mylyn.internal.tasks.core.AbstractTask>(history));
        this.needsCreateTask = true;
        this.labelProvider = new org.eclipse.mylyn.tasks.ui.TaskElementLabelProvider(false);
        this.showCompletedTasksAction = new org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.ShowCompletedTasksAction();
        setSelectionHistory(new org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.TaskSelectionHistory());
        setListLabelProvider(labelProvider);
        setDetailsLabelProvider(new org.eclipse.mylyn.internal.tasks.ui.views.TaskDetailLabelProvider());
        setSeparatorLabel(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.LABEL_VIEW + Messages.TaskSelectionDialog__matches);
        // If there is a text selection, use it as the initial filter
        org.eclipse.ui.IWorkbenchWindow window = org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        org.eclipse.jface.viewers.ISelection selection = window.getSelectionService().getSelection();
        if (selection instanceof org.eclipse.jface.text.ITextSelection) {
            // Get only get first line
            java.lang.String text = ((org.eclipse.jface.text.ITextSelection) (selection)).getText();
            text = com.google.common.base.Strings.nullToEmpty(text);
            int n = text.indexOf('\n');
            if (n > (-1)) {
                text = text.substring(0, n);
            }
            setInitialPattern(text);
        }
        windowWorkingSet = window.getActivePage().getAggregateWorkingSet();
        selectedWorkingSet = windowWorkingSet;
        org.eclipse.ui.PlatformUI.getWorkbench().getWorkingSetManager().addPropertyChangeListener(workingSetListener);
    }

    @java.lang.Override
    public boolean close() {
        org.eclipse.ui.PlatformUI.getWorkbench().getWorkingSetManager().removePropertyChangeListener(workingSetListener);
        if (openInBrowserCheck != null) {
            openInBrowser = openInBrowserCheck.getSelection();
        }
        return super.close();
    }

    @java.lang.Override
    protected org.eclipse.swt.widgets.Control createButtonBar(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.widgets.Composite composite = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NONE);
        org.eclipse.swt.layout.GridLayout layout = new org.eclipse.swt.layout.GridLayout();
        layout.numColumns = 0;// create

        layout.marginHeight = convertVerticalDLUsToPixels(org.eclipse.jface.dialogs.IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(org.eclipse.jface.dialogs.IDialogConstants.HORIZONTAL_MARGIN);
        layout.verticalSpacing = convertVerticalDLUsToPixels(org.eclipse.jface.dialogs.IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(org.eclipse.jface.dialogs.IDialogConstants.HORIZONTAL_SPACING);
        composite.setLayout(layout);
        composite.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_HORIZONTAL));
        // create help control if needed
        if (isHelpAvailable()) {
            createHelpControl(composite);
        }
        if (needsCreateTask) {
            createTaskButton = createButton(composite, org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.CREATE_ID, Messages.TaskSelectionDialog_New_Task_, true);
            createTaskButton.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
                public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
                    // ignore
                }

                public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                    close();
                    new org.eclipse.mylyn.internal.tasks.ui.actions.NewTaskAction().run();
                }
            });
        }
        createAdditionalButtons(composite);
        org.eclipse.swt.widgets.Label filler = new org.eclipse.swt.widgets.Label(composite, org.eclipse.swt.SWT.NONE);
        filler.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_HORIZONTAL | org.eclipse.swt.layout.GridData.GRAB_HORIZONTAL));
        layout.numColumns++;
        super.createButtonsForButtonBar(composite);// cancel button

        return composite;
    }

    /**
     * Allows to add new buttons at the bottom of this dialog next to New Task button
     *
     * @param parent
     * 		the parent composite to contain the button bar
     */
    protected void createAdditionalButtons(org.eclipse.swt.widgets.Composite parent) {
        // we don't want to add any new button
    }

    @java.lang.Override
    protected org.eclipse.swt.widgets.Control createExtendedContentArea(org.eclipse.swt.widgets.Composite parent) {
        if (!showExtendedOpeningOptions) {
            return null;
        }
        org.eclipse.swt.widgets.Composite composite = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NONE);
        composite.setLayout(org.eclipse.jface.layout.GridLayoutFactory.swtDefaults().margins(0, 5).create());
        composite.setLayoutData(org.eclipse.jface.layout.GridDataFactory.fillDefaults().create());
        openInBrowserCheck = new org.eclipse.swt.widgets.Button(composite, org.eclipse.swt.SWT.CHECK);
        openInBrowserCheck.setText(Messages.TaskSelectionDialog_Open_with_Browser);
        openInBrowserCheck.setSelection(openInBrowser);
        if (org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil.supportsTaskSearch()) {
            org.eclipse.ui.forms.widgets.ImageHyperlink openHyperlink = new org.eclipse.ui.forms.widgets.ImageHyperlink(composite, org.eclipse.swt.SWT.NONE);
            openHyperlink.setText(org.eclipse.mylyn.internal.tasks.ui.views.TaskListFilteredTree.LABEL_SEARCH);
            openHyperlink.setForeground(org.eclipse.mylyn.commons.ui.compatibility.CommonColors.HYPERLINK_WIDGET);
            openHyperlink.setUnderlined(true);
            openHyperlink.addHyperlinkListener(new org.eclipse.ui.forms.events.HyperlinkAdapter() {
                @java.lang.Override
                public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                    getShell().close();
                    org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil.openSearchDialog(org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow());
                }
            });
        }
        return composite;
    }

    @java.lang.Override
    protected org.eclipse.mylyn.internal.tasks.ui.actions.ItemsFilter createFilter() {
        return new org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.TasksFilter(showCompletedTasks, selectedWorkingSet);
    }

    @java.lang.Override
    protected void fillContentProvider(org.eclipse.mylyn.internal.tasks.ui.actions.AbstractContentProvider contentProvider, org.eclipse.mylyn.internal.tasks.ui.actions.ItemsFilter itemsFilter, org.eclipse.core.runtime.IProgressMonitor progressMonitor) throws org.eclipse.core.runtime.CoreException {
        progressMonitor.beginTask(Messages.TaskSelectionDialog_Search_for_tasks, 100);
        if (allTasks == null) {
            allTasks = new java.util.HashSet<org.eclipse.mylyn.internal.tasks.core.AbstractTask>();
            org.eclipse.mylyn.internal.tasks.core.TaskList taskList = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList();
            allTasks.addAll(taskList.getAllTasks());
        }
        progressMonitor.worked(10);
        org.eclipse.core.runtime.SubProgressMonitor subMonitor = new org.eclipse.core.runtime.SubProgressMonitor(progressMonitor, 90);
        subMonitor.beginTask(Messages.TaskSelectionDialog_Scanning_tasks, allTasks.size());
        for (org.eclipse.mylyn.tasks.core.ITask task : allTasks) {
            contentProvider.add(task, itemsFilter);
            subMonitor.worked(1);
        }
        subMonitor.done();
        progressMonitor.done();
    }

    @java.lang.Override
    protected void fillViewMenu(org.eclipse.jface.action.IMenuManager menuManager) {
        super.fillViewMenu(menuManager);
        menuManager.add(showCompletedTasksAction);
        menuManager.add(new org.eclipse.jface.action.Separator());
        // Fill existing tasks working sets
        menuManager.add(new org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.SelectWorkingSetAction());
        final org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.DeselectWorkingSetAction deselectAction = new org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.DeselectWorkingSetAction();
        menuManager.add(deselectAction);
        final org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.EditWorkingSetAction editAction = new org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.EditWorkingSetAction();
        menuManager.add(editAction);
        menuManager.add(new org.eclipse.jface.action.Separator("lruActions"));// $NON-NLS-1$

        final org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.FilterWorkingSetAction windowWorkingSetAction = new org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.FilterWorkingSetAction(windowWorkingSet, 1);
        menuManager.add(windowWorkingSetAction);
        menuManager.addMenuListener(new org.eclipse.jface.action.IMenuListener() {
            private final java.util.List<org.eclipse.jface.action.ActionContributionItem> lruActions = new java.util.ArrayList<org.eclipse.jface.action.ActionContributionItem>();

            public void menuAboutToShow(org.eclipse.jface.action.IMenuManager manager) {
                deselectAction.setEnabled(selectedWorkingSet != null);
                editAction.setEnabled((selectedWorkingSet != null) && selectedWorkingSet.isEditable());
                // Remove previous LRU actions
                for (org.eclipse.jface.action.ActionContributionItem action : lruActions) {
                    manager.remove(action);
                }
                lruActions.clear();
                // Adds actual LRU actions
                org.eclipse.ui.IWorkingSet[] workingSets = org.eclipse.ui.PlatformUI.getWorkbench().getWorkingSetManager().getRecentWorkingSets();
                java.util.Arrays.sort(workingSets, new org.eclipse.mylyn.internal.tasks.ui.workingsets.WorkingSetLabelComparator());
                int count = 2;
                for (org.eclipse.ui.IWorkingSet workingSet : workingSets) {
                    if (workingSet.getId().equalsIgnoreCase(org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater.ID_TASK_WORKING_SET)) {
                        org.eclipse.jface.action.IAction action = new org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.FilterWorkingSetAction(workingSet, count++);
                        if (workingSet.equals(selectedWorkingSet)) {
                            action.setChecked(true);
                        }
                        org.eclipse.jface.action.ActionContributionItem ci = new org.eclipse.jface.action.ActionContributionItem(action);
                        lruActions.add(ci);
                        manager.appendToGroup("lruActions", ci);// $NON-NLS-1$

                    }
                }
                windowWorkingSetAction.setChecked(windowWorkingSet.equals(selectedWorkingSet));
            }
        });
    }

    @java.lang.Override
    protected org.eclipse.jface.dialogs.IDialogSettings getDialogSettings() {
        org.eclipse.jface.dialogs.IDialogSettings settings = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getDialogSettings();
        org.eclipse.jface.dialogs.IDialogSettings section = settings.getSection(org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.TASK_SELECTION_DIALOG_SECTION);
        if (section == null) {
            section = settings.addNewSection(org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.TASK_SELECTION_DIALOG_SECTION);
            section.put(org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.OPEN_IN_BROWSER_SETTING, false);
            section.put(org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.SHOW_COMPLETED_TASKS_SETTING, true);
            section.put(org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.IS_USING_WINDOW_WORKING_SET_SETTING, true);
            section.put(org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.WORKING_SET_NAME_SETTING, "");// $NON-NLS-1$

        }
        return section;
    }

    @java.lang.Override
    public java.lang.String getElementName(java.lang.Object item) {
        return labelProvider.getText(item);
    }

    /**
     * Sort tasks by summary
     */
    @java.lang.SuppressWarnings("rawtypes")
    @java.lang.Override
    protected java.util.Comparator getItemsComparator() {
        return itemsComparator;
    }

    public boolean getOpenInBrowser() {
        return openInBrowser;
    }

    public boolean getShowExtendedOpeningOptions() {
        return showExtendedOpeningOptions;
    }

    public boolean needsCreateTask() {
        return needsCreateTask;
    }

    @java.lang.Override
    protected void restoreDialog(org.eclipse.jface.dialogs.IDialogSettings settings) {
        openInBrowser = settings.getBoolean(org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.OPEN_IN_BROWSER_SETTING);
        showCompletedTasks = settings.getBoolean(org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.SHOW_COMPLETED_TASKS_SETTING);
        showCompletedTasksAction.setChecked(showCompletedTasks);
        boolean isUsingWindowWorkingSet = settings.getBoolean(org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.IS_USING_WINDOW_WORKING_SET_SETTING);
        if (isUsingWindowWorkingSet) {
            selectedWorkingSet = windowWorkingSet;
        } else {
            java.lang.String workingSetName = settings.get(org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.WORKING_SET_NAME_SETTING);
            if (workingSetName != null) {
                selectedWorkingSet = org.eclipse.ui.PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(workingSetName);
            }
        }
        super.restoreDialog(settings);
    }

    public void setNeedsCreateTask(boolean value) {
        needsCreateTask = value;
    }

    public void setOpenInBrowser(boolean openInBrowser) {
        this.openInBrowser = openInBrowser;
    }

    /**
     * All working set filter changes should be made through this method; ensures proper history handling and triggers
     * refiltering
     */
    private void setSelectedWorkingSet(org.eclipse.ui.IWorkingSet workingSet) {
        selectedWorkingSet = workingSet;
        if (workingSet != null) {
            org.eclipse.ui.PlatformUI.getWorkbench().getWorkingSetManager().addRecentWorkingSet(workingSet);
        }
        applyFilter();
    }

    public void setShowExtendedOpeningOptions(boolean showExtendedOpeningOptions) {
        this.showExtendedOpeningOptions = showExtendedOpeningOptions;
    }

    @java.lang.Override
    protected void storeDialog(org.eclipse.jface.dialogs.IDialogSettings settings) {
        settings.put(org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.OPEN_IN_BROWSER_SETTING, openInBrowser);
        settings.put(org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.SHOW_COMPLETED_TASKS_SETTING, showCompletedTasks);
        settings.put(org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.IS_USING_WINDOW_WORKING_SET_SETTING, selectedWorkingSet == windowWorkingSet);
        if (selectedWorkingSet == null) {
            settings.put(org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.WORKING_SET_NAME_SETTING, "");// $NON-NLS-1$

        } else {
            settings.put(org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialog.WORKING_SET_NAME_SETTING, selectedWorkingSet.getName());
        }
        super.storeDialog(settings);
    }

    @java.lang.Override
    protected org.eclipse.core.runtime.IStatus validateItem(java.lang.Object item) {
        if (item instanceof org.eclipse.mylyn.tasks.core.ITask) {
            return org.eclipse.core.runtime.Status.OK_STATUS;
        }
        return new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, Messages.TaskSelectionDialog_Selected_item_is_not_a_task);
    }
}