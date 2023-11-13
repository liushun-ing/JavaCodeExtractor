/**
 * *****************************************************************************
 * Copyright (c) 2004, 2014 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui;
import org.apache.commons.lang.reflect.MethodUtils;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.commons.ui.SelectionProviderAdapter;
import org.eclipse.mylyn.internal.tasks.core.ITaskListChangeListener;
import org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta;
import org.eclipse.mylyn.internal.tasks.ui.actions.RepositoryElementActionGroup;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskActivationListener;
import org.eclipse.mylyn.tasks.core.TaskActivationAdapter;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.internal.ObjectActionContributorManager;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;
/**
 *
 * @author Mik Kersten
 * @author Leo Dos Santos
 */
// // From PerspectiveBarContributionItem
// private String shortenText(String taskLabel) {
// if (taskLabel == null || composite == null || composite.isDisposed()) {
// return null;
// }
// 
// String returnText = taskLabel;
// GC gc = new GC(composite);
// int maxWidth = p.x;
// 
// if (gc.textExtent(taskLabel).x > maxWidth) {
// for (int i = taskLabel.length(); i > 0; i--) {
// String test = taskLabel.substring(0, i);
// test = test + "...";
// if (gc.textExtent(test).x < maxWidth) {
// returnText = test;
// break;
// }
// }
// }
// 
// gc.dispose();
// return returnText;
// }
public class TaskTrimWidget extends org.eclipse.ui.menus.WorkbenchWindowControlContribution {
    public static java.lang.String ID_CONTAINER = "org.eclipse.mylyn.tasks.ui.trim.container";// $NON-NLS-1$


    public static java.lang.String ID_CONTROL = "org.eclipse.mylyn.tasks.ui.trim.control";// $NON-NLS-1$


    private org.eclipse.swt.widgets.Composite composite;

    private org.eclipse.mylyn.tasks.core.ITask activeTask;

    private org.eclipse.jface.action.MenuManager menuManager;

    private org.eclipse.swt.widgets.Menu menu;

    private org.eclipse.mylyn.internal.tasks.ui.TaskScalingHyperlink activeTaskLabel;

    private final org.eclipse.mylyn.tasks.core.ITaskActivationListener taskActivationListener = new org.eclipse.mylyn.tasks.core.TaskActivationAdapter() {
        @java.lang.Override
        public void taskActivated(org.eclipse.mylyn.tasks.core.ITask task) {
            activeTask = task;
            indicateActiveTask();
        }

        @java.lang.Override
        public void taskDeactivated(org.eclipse.mylyn.tasks.core.ITask task) {
            activeTask = null;
            indicateNoActiveTask();
        }
    };

    private final org.eclipse.mylyn.internal.tasks.core.ITaskListChangeListener taskListListener = new org.eclipse.mylyn.internal.tasks.core.ITaskListChangeListener() {
        public void containersChanged(java.util.Set<org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta> containers) {
            // update label in case task changes
            if (activeTask != null) {
                for (org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta taskContainerDelta : containers) {
                    if (activeTask.equals(taskContainerDelta.getElement())) {
                        if (taskContainerDelta.getKind().equals(TaskContainerDelta.Kind.CONTENT)) {
                            org.eclipse.swt.widgets.Display.getDefault().asyncExec(new java.lang.Runnable() {
                                public void run() {
                                    if ((activeTask != null) && activeTask.isActive()) {
                                        indicateActiveTask();
                                    }
                                }
                            });
                            return;
                        }
                    }
                }
            }
        }
    };

    private org.eclipse.mylyn.commons.ui.SelectionProviderAdapter activeTaskSelectionProvider;

    private org.eclipse.mylyn.internal.tasks.ui.actions.RepositoryElementActionGroup actionGroup;

    public TaskTrimWidget() {
        org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().addActivationListener(taskActivationListener);
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().addChangeListener(taskListListener);
        hookContextMenu();
    }

    @java.lang.Override
    public void dispose() {
        if ((composite != null) && (!composite.isDisposed())) {
            composite.dispose();
        }
        composite = null;
        if (menuManager != null) {
            menuManager.removeAll();
            menuManager.dispose();
        }
        menuManager = null;
        if ((menu != null) && (!menu.isDisposed())) {
            menu.dispose();
        }
        menu = null;
        actionGroup.setSelectionProvider(null);
        org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().removeActivationListener(taskActivationListener);
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().removeChangeListener(taskListListener);
    }

    @java.lang.Override
    protected org.eclipse.swt.widgets.Control createControl(org.eclipse.swt.widgets.Composite parent) {
        composite = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NONE);
        org.eclipse.swt.layout.GridLayout layout = new org.eclipse.swt.layout.GridLayout();
        layout.numColumns = 1;
        layout.horizontalSpacing = 0;
        layout.marginHeight = 0;
        layout.marginLeft = 0;
        layout.marginRight = 0;
        composite.setLayout(layout);
        // composite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true));
        createStatusComposite(composite);
        return composite;
    }

    private org.eclipse.swt.widgets.Composite createStatusComposite(final org.eclipse.swt.widgets.Composite container) {
        org.eclipse.swt.graphics.GC gc = new org.eclipse.swt.graphics.GC(container);
        org.eclipse.swt.graphics.Point p = gc.textExtent("WWWWWWWWWWWWWWW");// $NON-NLS-1$

        gc.dispose();
        activeTaskLabel = new org.eclipse.mylyn.internal.tasks.ui.TaskScalingHyperlink(container, org.eclipse.swt.SWT.RIGHT);
        // activeTaskLabel.setLayoutData(new GridData(p.x, SWT.DEFAULT));
        org.eclipse.swt.layout.GridData gridData = new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.LEFT, org.eclipse.swt.SWT.CENTER, false, true);
        gridData.widthHint = p.x;
        gridData.minimumWidth = p.x;
        gridData.horizontalIndent = 0;
        activeTaskLabel.setLayoutData(gridData);
        activeTaskLabel.setText(Messages.TaskTrimWidget__no_task_active_);
        activeTask = org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().getActiveTask();
        if (activeTask != null) {
            indicateActiveTask();
        }
        activeTaskLabel.addMenuDetectListener(new org.eclipse.swt.events.MenuDetectListener() {
            public void menuDetected(org.eclipse.swt.events.MenuDetectEvent e) {
                if (menu != null) {
                    menu.dispose();
                }
                menu = menuManager.createContextMenu(container);
                menu.setVisible(true);
            }
        });
        activeTaskLabel.addHyperlinkListener(new org.eclipse.ui.forms.events.HyperlinkAdapter() {
            @java.lang.Override
            public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                org.eclipse.mylyn.internal.tasks.ui.views.TaskListView taskListView = org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.getFromActivePerspective();
                if ((taskListView != null) && (taskListView.getDrilledIntoCategory() != null)) {
                    taskListView.goUpToRoot();
                }
                org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.refreshAndOpenTaskListElement(org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().getActiveTask());
            }
        });
        activeTaskLabel.addMouseListener(new org.eclipse.swt.events.MouseAdapter() {
            @java.lang.Override
            public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
                // only handle left clicks, context menu is handled by platform
                if (e.button == 1) {
                    if (activeTask == null) {
                        return;
                    }
                    org.eclipse.mylyn.internal.tasks.ui.views.TaskListView taskListView = org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.getFromActivePerspective();
                    if ((taskListView != null) && (taskListView.getDrilledIntoCategory() != null)) {
                        taskListView.goUpToRoot();
                    }
                    org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.refreshAndOpenTaskListElement(activeTask);
                }
            }
        });
        return activeTaskLabel;
    }

    private void hookContextMenu() {
        activeTaskSelectionProvider = new org.eclipse.mylyn.commons.ui.SelectionProviderAdapter();
        actionGroup = new org.eclipse.mylyn.internal.tasks.ui.actions.RepositoryElementActionGroup();
        actionGroup.setSelectionProvider(activeTaskSelectionProvider);
        menuManager = new org.eclipse.jface.action.MenuManager("#PopupMenu");// $NON-NLS-1$

        menuManager.setRemoveAllWhenShown(true);
        menuManager.addMenuListener(new org.eclipse.jface.action.IMenuListener() {
            public void menuAboutToShow(org.eclipse.jface.action.IMenuManager manager) {
                actionGroup.fillContextMenu(manager);
                // trims do not have a workbench part so there is no simple way of registering the
                // context menu
                if (!contributeObjectActionsOld(manager)) {
                    contributeObjectActionsNew(manager);
                }
            }
        });
    }

    public void indicateActiveTask() {
        if ((activeTaskLabel == null) || activeTaskLabel.isDisposed()) {
            return;
        }
        // activeTaskLabel.setText(shortenText(activeTask.getSummary()));
        activeTaskLabel.setText(activeTask.getSummary());
        activeTaskLabel.setUnderlined(true);
        activeTaskLabel.setToolTipText(activeTask.getSummary());
        activeTaskSelectionProvider.setSelection(new org.eclipse.jface.viewers.StructuredSelection(activeTask));
    }

    public void indicateNoActiveTask() {
        if ((activeTaskLabel == null) || activeTaskLabel.isDisposed()) {
            return;
        }
        activeTaskLabel.setText(Messages.TaskTrimWidget__no_active_task_);
        activeTaskLabel.setUnderlined(false);
        activeTaskLabel.setToolTipText("");// $NON-NLS-1$

        activeTaskSelectionProvider.setSelection(org.eclipse.jface.viewers.StructuredSelection.EMPTY);
    }

    private boolean contributeObjectActionsOld(org.eclipse.jface.action.IMenuManager manager) {
        try {
            // $NON-NLS-1$
            org.apache.commons.lang.reflect.MethodUtils.invokeExactMethod(org.eclipse.ui.internal.ObjectActionContributorManager.getManager(), "contributeObjectActions", new java.lang.Object[]{ null, manager, activeTaskSelectionProvider }, new java.lang.Class[]{ org.eclipse.ui.IWorkbenchPart.class, org.eclipse.jface.action.IMenuManager.class, org.eclipse.jface.viewers.ISelectionProvider.class });
        } catch (java.lang.NoSuchMethodException e) {
            return false;
        } catch (java.lang.IllegalAccessException e) {
            return false;
        } catch (java.lang.reflect.InvocationTargetException e) {
            return false;
        }
        return true;
    }

    private boolean contributeObjectActionsNew(org.eclipse.jface.action.IMenuManager manager) {
        try {
            // $NON-NLS-1$
            org.apache.commons.lang.reflect.MethodUtils.invokeExactMethod(org.eclipse.ui.internal.ObjectActionContributorManager.getManager(), "contributeObjectActions", new java.lang.Object[]{ null, manager, activeTaskSelectionProvider, java.util.Collections.EMPTY_SET }, new java.lang.Class[]{ org.eclipse.ui.IWorkbenchPart.class, org.eclipse.jface.action.IMenuManager.class, org.eclipse.jface.viewers.ISelectionProvider.class, java.util.Set.class });
        } catch (java.lang.NoSuchMethodException e) {
            return false;
        } catch (java.lang.IllegalAccessException e) {
            return false;
        } catch (java.lang.reflect.InvocationTargetException e) {
            return false;
        }
        return true;
    }
}