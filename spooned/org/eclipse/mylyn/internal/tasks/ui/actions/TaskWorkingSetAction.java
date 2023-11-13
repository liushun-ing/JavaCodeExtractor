/**
 * *****************************************************************************
 * Copyright (c) 2004, 2009 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.actions;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.internal.tasks.ui.dialogs.AbstractWorkingSetDialogCOPY;
import org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetEditWizard;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkingSetComparator;
import org.eclipse.ui.internal.dialogs.WorkingSetFilter;
import org.eclipse.ui.internal.dialogs.WorkingSetLabelProvider;
/**
 * Derived from SelectWorkingSetsAction
 *
 * @author Leo Dos Santos
 * @author Mik Kersten
 */
public class TaskWorkingSetAction extends org.eclipse.jface.action.Action implements org.eclipse.jface.action.IMenuCreator {
    public static final java.lang.String LABEL_SETS_NONE = Messages.TaskWorkingSetAction_All;

    private org.eclipse.swt.widgets.Menu dropDownMenu;

    public TaskWorkingSetAction() {
        setText(Messages.TaskWorkingSetAction_Sets);
        setToolTipText(Messages.TaskWorkingSetAction_Select_and_Edit_Working_Sets);
        setImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.TASK_WORKING_SET);
        setEnabled(true);
        setMenuCreator(this);
    }

    public void dispose() {
        if (dropDownMenu != null) {
            dropDownMenu.dispose();
            dropDownMenu = null;
        }
    }

    public org.eclipse.swt.widgets.Menu getMenu(org.eclipse.swt.widgets.Control parent) {
        if (dropDownMenu != null) {
            dropDownMenu.dispose();
        }
        dropDownMenu = new org.eclipse.swt.widgets.Menu(parent);
        addActionsToMenu();
        return dropDownMenu;
    }

    public org.eclipse.swt.widgets.Menu getMenu(org.eclipse.swt.widgets.Menu parent) {
        if (dropDownMenu != null) {
            dropDownMenu.dispose();
        }
        dropDownMenu = new org.eclipse.swt.widgets.Menu(parent);
        addActionsToMenu();
        return dropDownMenu;
    }

    @java.lang.SuppressWarnings("unchecked")
    private void addActionsToMenu() {
        org.eclipse.ui.IWorkingSet[] workingSets = org.eclipse.ui.PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSets();
        if (doTaskWorkingSetsExist()) {
            org.eclipse.jface.action.ActionContributionItem itemAll = new org.eclipse.jface.action.ActionContributionItem(new org.eclipse.mylyn.internal.tasks.ui.actions.ToggleAllWorkingSetsAction());
            // ActionContributionItem itemNone = new ActionContributionItem(new ToggleNoWorkingSetsAction());
            java.util.List<org.eclipse.ui.IWorkingSet> sortedWorkingSets = java.util.Arrays.asList(workingSets);
            java.util.Collections.sort(sortedWorkingSets, new org.eclipse.ui.internal.WorkingSetComparator());
            java.util.Iterator<org.eclipse.ui.IWorkingSet> iter = sortedWorkingSets.iterator();
            while (iter.hasNext()) {
                org.eclipse.ui.IWorkingSet workingSet = iter.next();
                if ((workingSet != null) && workingSet.getId().equalsIgnoreCase(org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater.ID_TASK_WORKING_SET)) {
                    org.eclipse.jface.action.ActionContributionItem itemSet = new org.eclipse.jface.action.ActionContributionItem(new org.eclipse.mylyn.internal.tasks.ui.actions.ToggleWorkingSetAction(workingSet));
                    itemSet.fill(dropDownMenu, -1);
                }
            } 
            org.eclipse.jface.action.Separator separator = new org.eclipse.jface.action.Separator();
            separator.fill(dropDownMenu, -1);
            itemAll.fill(dropDownMenu, -1);
        }
        org.eclipse.jface.action.ActionContributionItem editItem = new org.eclipse.jface.action.ActionContributionItem(new org.eclipse.mylyn.internal.tasks.ui.actions.TaskWorkingSetAction.ManageWorkingSetsAction());
        editItem.fill(dropDownMenu, -1);
    }

    private boolean doTaskWorkingSetsExist() {
        org.eclipse.ui.IWorkingSet[] workingSets = org.eclipse.ui.PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSets();
        for (org.eclipse.ui.IWorkingSet workingSet : workingSets) {
            if ((workingSet != null) && workingSet.getId().equalsIgnoreCase(org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater.ID_TASK_WORKING_SET)) {
                return true;
            }
        }
        return false;
    }

    @java.lang.Override
    public void run() {
        java.lang.String[] ids = new java.lang.String[1];
        ids[0] = org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater.ID_TASK_WORKING_SET;
        org.eclipse.mylyn.internal.tasks.ui.actions.TaskWorkingSetAction.ConfigureWindowWorkingSetsDialog dialog = new org.eclipse.mylyn.internal.tasks.ui.actions.TaskWorkingSetAction.ConfigureWindowWorkingSetsDialog(org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow(), ids);
        dialog.open();
    }

    public void run(org.eclipse.jface.action.IAction action) {
        this.run();
    }

    public void run(org.eclipse.ui.IWorkingSet editWorkingSet) {
        org.eclipse.ui.IWorkingSetManager manager = org.eclipse.ui.internal.WorkbenchPlugin.getDefault().getWorkingSetManager();
        org.eclipse.ui.dialogs.IWorkingSetEditWizard wizard = manager.createWorkingSetEditWizard(editWorkingSet);
        org.eclipse.jface.wizard.WizardDialog dialog = new org.eclipse.jface.wizard.WizardDialog(org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
        // dialog.create();
        dialog.open();
        // PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(),
        // IWorkbenchHelpContextIds.WORKING_SET_EDIT_WIZARD);
        // if (dialog.open() == Window.OK) {
        // editWorkingSet = wizard.getSelection();
        // availableWorkingSetsChanged();
        // // make sure ok button is enabled when the selected working set
        // // is edited. Fixes bug 33386.
        // updateButtonAvailability();
        // }
        // editedWorkingSets.put(editWorkingSet, originalWorkingSet);
    }

    private class ManageWorkingSetsAction extends org.eclipse.jface.action.Action {
        ManageWorkingSetsAction() {
            super(Messages.TaskWorkingSetAction_Edit_Label);
        }

        @java.lang.Override
        public void run() {
            TaskWorkingSetAction.this.run(this);
        }
    }

    // TODO: remove?
    protected class ToggleEnableAllSetsAction extends org.eclipse.jface.action.Action {
        ToggleEnableAllSetsAction() {
            super(Messages.TaskWorkingSetAction_Deselect_All, org.eclipse.jface.action.IAction.AS_CHECK_BOX);
            // setImageDescriptor(TasksUiImages.TASK_WORKING_SET);
            // setChecked(!areAllTaskWorkingSetsEnabled());
        }

        @java.lang.Override
        public void runWithEvent(org.eclipse.swt.widgets.Event event) {
            java.util.Set<org.eclipse.ui.IWorkingSet> newList = new java.util.HashSet<org.eclipse.ui.IWorkingSet>(java.util.Arrays.asList(org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater.getEnabledSets()));
            java.util.Set<org.eclipse.ui.IWorkingSet> tempList = new java.util.HashSet<org.eclipse.ui.IWorkingSet>();
            java.util.Iterator<org.eclipse.ui.IWorkingSet> iter = newList.iterator();
            while (iter.hasNext()) {
                org.eclipse.ui.IWorkingSet workingSet = iter.next();
                if ((workingSet != null) && workingSet.getId().equalsIgnoreCase(org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater.ID_TASK_WORKING_SET)) {
                    tempList.add(workingSet);
                }
            } 
            newList.removeAll(tempList);
            if (isChecked()) {
                org.eclipse.ui.IWorkingSet[] allWorkingSets = org.eclipse.ui.PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSets();
                for (org.eclipse.ui.IWorkingSet workingSet : allWorkingSets) {
                    if ((workingSet != null) && workingSet.getId().equalsIgnoreCase(org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater.ID_TASK_WORKING_SET)) {
                        newList.add(workingSet);
                    }
                }
            }
            org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater.applyWorkingSetsToAllWindows(newList);
        }
    }

    class ConfigureWindowWorkingSetsDialog extends org.eclipse.mylyn.internal.tasks.ui.dialogs.AbstractWorkingSetDialogCOPY {
        private static final int SIZING_SELECTION_WIDGET_HEIGHT = 200;

        private static final int SIZING_SELECTION_WIDGET_WIDTH = 50;

        private final org.eclipse.ui.IWorkbenchWindow window;

        private org.eclipse.jface.viewers.CheckboxTableViewer viewer;

        private java.util.Set<java.lang.String> taskWorkingSetIds;

        protected ConfigureWindowWorkingSetsDialog(org.eclipse.ui.IWorkbenchWindow window, java.lang.String[] workingSetIds) {
            super(window.getShell(), workingSetIds, true);
            setShellStyle(getShellStyle() | org.eclipse.swt.SWT.RESIZE);
            this.window = window;
            // setTitle(WorkbenchMessages.WorkingSetSelectionDialog_title_multiSelect);
            setTitle(Messages.TaskWorkingSetAction_Select_and_Edit_Working_Sets);
            setMessage("");// $NON-NLS-1$

            if ((workingSetIds == null) || (workingSetIds.length == 0)) {
                taskWorkingSetIds = null;
            } else {
                taskWorkingSetIds = new java.util.HashSet<java.lang.String>();
                for (java.lang.String id : workingSetIds) {
                    taskWorkingSetIds.add(id);
                }
            }
        }

        @java.lang.Override
        protected org.eclipse.swt.widgets.Control createDialogArea(org.eclipse.swt.widgets.Composite parent) {
            initializeDialogUnits(parent);
            org.eclipse.swt.widgets.Composite composite = ((org.eclipse.swt.widgets.Composite) (super.createDialogArea(parent)));
            org.eclipse.swt.widgets.Composite viewerComposite = new org.eclipse.swt.widgets.Composite(composite, org.eclipse.swt.SWT.NONE);
            org.eclipse.swt.layout.GridLayout layout = new org.eclipse.swt.layout.GridLayout(2, false);
            layout.marginHeight = layout.marginWidth = 0;
            layout.horizontalSpacing = convertHorizontalDLUsToPixels(org.eclipse.jface.dialogs.IDialogConstants.HORIZONTAL_SPACING);
            layout.verticalSpacing = convertVerticalDLUsToPixels(org.eclipse.jface.dialogs.IDialogConstants.VERTICAL_SPACING);
            viewerComposite.setLayout(layout);
            org.eclipse.swt.layout.GridData data = new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_BOTH);
            data.heightHint = org.eclipse.mylyn.internal.tasks.ui.actions.TaskWorkingSetAction.ConfigureWindowWorkingSetsDialog.SIZING_SELECTION_WIDGET_HEIGHT;
            data.widthHint = org.eclipse.mylyn.internal.tasks.ui.actions.TaskWorkingSetAction.ConfigureWindowWorkingSetsDialog.SIZING_SELECTION_WIDGET_WIDTH + 300;// fudge?  I like fudge.

            viewerComposite.setLayoutData(data);
            viewer = org.eclipse.jface.viewers.CheckboxTableViewer.newCheckList(viewerComposite, org.eclipse.swt.SWT.BORDER);
            viewer.getControl().setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_BOTH));
            viewer.setLabelProvider(new org.eclipse.ui.internal.dialogs.WorkingSetLabelProvider());
            viewer.setContentProvider(new org.eclipse.jface.viewers.ArrayContentProvider());
            viewer.addFilter(new org.eclipse.ui.internal.dialogs.WorkingSetFilter(taskWorkingSetIds));
            viewer.setInput(window.getWorkbench().getWorkingSetManager().getWorkingSets());
            viewer.setCheckedElements(org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater.getActiveWorkingSets(window).toArray());
            viewer.addSelectionChangedListener(new org.eclipse.jface.viewers.ISelectionChangedListener() {
                public void selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent event) {
                    handleSelectionChanged();
                }
            });
            data = new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_BOTH);
            data.heightHint = org.eclipse.mylyn.internal.tasks.ui.actions.TaskWorkingSetAction.ConfigureWindowWorkingSetsDialog.SIZING_SELECTION_WIDGET_HEIGHT;
            data.widthHint = org.eclipse.mylyn.internal.tasks.ui.actions.TaskWorkingSetAction.ConfigureWindowWorkingSetsDialog.SIZING_SELECTION_WIDGET_WIDTH;
            viewer.getControl().setLayoutData(data);
            addModifyButtons(viewerComposite);
            addSelectionButtons(composite);
            availableWorkingSetsChanged();
            org.eclipse.jface.dialogs.Dialog.applyDialogFont(composite);
            return composite;
        }

        @java.lang.Override
        protected void okPressed() {
            java.util.Set<org.eclipse.ui.IWorkingSet> newList = new java.util.HashSet<org.eclipse.ui.IWorkingSet>(java.util.Arrays.asList(org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater.getEnabledSets()));
            java.util.Set<org.eclipse.ui.IWorkingSet> tempList = new java.util.HashSet<org.eclipse.ui.IWorkingSet>();
            for (org.eclipse.ui.IWorkingSet workingSet : newList) {
                for (java.lang.String id : taskWorkingSetIds) {
                    if (workingSet.getId().equalsIgnoreCase(id)) {
                        tempList.add(workingSet);
                    }
                }
            }
            newList.removeAll(tempList);
            java.lang.Object[] selection = viewer.getCheckedElements();
            org.eclipse.ui.IWorkingSet[] setsToEnable = new org.eclipse.ui.IWorkingSet[selection.length];
            java.lang.System.arraycopy(selection, 0, setsToEnable, 0, selection.length);
            newList.addAll(new java.util.HashSet<org.eclipse.ui.IWorkingSet>(java.util.Arrays.asList(setsToEnable)));
            org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater.applyWorkingSetsToAllWindows(newList);
            super.okPressed();
        }

        @java.lang.Override
        protected java.util.List<?> getSelectedWorkingSets() {
            org.eclipse.jface.viewers.ISelection selection = viewer.getSelection();
            if (selection instanceof org.eclipse.jface.viewers.IStructuredSelection) {
                return ((org.eclipse.jface.viewers.IStructuredSelection) (selection)).toList();
            }
            return null;
        }

        @java.lang.Override
        protected void availableWorkingSetsChanged() {
            viewer.setInput(window.getWorkbench().getWorkingSetManager().getWorkingSets());
            super.availableWorkingSetsChanged();
        }

        /**
         * Called when the selection has changed.
         */
        void handleSelectionChanged() {
            updateButtonAvailability();
        }

        @java.lang.Override
        protected void configureShell(org.eclipse.swt.widgets.Shell shell) {
            super.configureShell(shell);
        }

        @java.lang.Override
        protected void selectAllSets() {
            viewer.setCheckedElements(window.getWorkbench().getWorkingSetManager().getWorkingSets());
            updateButtonAvailability();
        }

        @java.lang.Override
        protected void deselectAllSets() {
            viewer.setCheckedElements(new java.lang.Object[0]);
            updateButtonAvailability();
        }
    }
}