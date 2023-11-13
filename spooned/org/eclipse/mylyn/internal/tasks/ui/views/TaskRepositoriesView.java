/**
 * *****************************************************************************
 * Copyright (c) 2004, 2011 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.views;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.mylyn.commons.workbench.GradientDrawer;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.internal.tasks.core.Category;
import org.eclipse.mylyn.internal.tasks.core.IRepositoryModelListener;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryAdapter;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.actions.AddRepositoryAction;
import org.eclipse.mylyn.internal.tasks.ui.actions.DeleteTaskRepositoryAction;
import org.eclipse.mylyn.internal.tasks.ui.actions.DisconnectRepositoryAction;
import org.eclipse.mylyn.tasks.core.IRepositoryListener;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.ITasksUiConstants;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.themes.IThemeManager;
/**
 *
 * @author Mik Kersten
 */
public class TaskRepositoriesView extends org.eclipse.ui.part.ViewPart {
    /**
     *
     * @deprecated Use {@link ITasksUiConstants#ID_VIEW_REPOSITORIES} instead
     */
    @java.lang.Deprecated
    public static final java.lang.String ID = org.eclipse.mylyn.tasks.ui.ITasksUiConstants.ID_VIEW_REPOSITORIES;

    private org.eclipse.jface.viewers.TreeViewer viewer;

    private final org.eclipse.jface.action.Action addRepositoryAction = new org.eclipse.mylyn.internal.tasks.ui.actions.AddRepositoryAction();

    private org.eclipse.ui.actions.BaseSelectionListenerAction deleteRepositoryAction;

    private org.eclipse.ui.actions.BaseSelectionListenerAction resetConfigurationAction;

    private org.eclipse.mylyn.internal.tasks.ui.actions.DisconnectRepositoryAction offlineAction;

    private void asyncExec(java.lang.Runnable runnable) {
        if (org.eclipse.swt.widgets.Display.getCurrent() != null) {
            runnable.run();
        } else {
            org.eclipse.swt.widgets.Display.getDefault().asyncExec(runnable);
        }
    }

    private final org.eclipse.mylyn.internal.tasks.core.IRepositoryModelListener MODEL_LISTENER = new org.eclipse.mylyn.internal.tasks.core.IRepositoryModelListener() {
        public void loaded() {
            asyncExec(new java.lang.Runnable() {
                public void run() {
                    refresh();
                }
            });
        }
    };

    private final org.eclipse.mylyn.tasks.core.IRepositoryListener REPOSITORY_LISTENER = new org.eclipse.mylyn.internal.tasks.core.TaskRepositoryAdapter() {
        @java.lang.Override
        public void repositoryAdded(org.eclipse.mylyn.tasks.core.TaskRepository repository) {
            asyncExec(new java.lang.Runnable() {
                public void run() {
                    refresh();
                }
            });
        }

        @java.lang.Override
        public void repositoryRemoved(org.eclipse.mylyn.tasks.core.TaskRepository repository) {
            asyncExec(new java.lang.Runnable() {
                public void run() {
                    refresh();
                }
            });
        }

        @java.lang.Override
        public void repositorySettingsChanged(org.eclipse.mylyn.tasks.core.TaskRepository repository) {
            asyncExec(new java.lang.Runnable() {
                public void run() {
                    refresh();
                }
            });
        }
    };

    private final org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager manager;

    private org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoriesContentProvider contentProvider;

    public TaskRepositoriesView() {
        manager = ((org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager) (org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager()));
        manager.addListener(REPOSITORY_LISTENER);
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().addModelListener(MODEL_LISTENER);
    }

    @java.lang.Override
    public void dispose() {
        super.dispose();
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryManager().removeListener(REPOSITORY_LISTENER);
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().removeModelListener(MODEL_LISTENER);
    }

    public static org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoriesView getFromActivePerspective() {
        if (org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null) {
            org.eclipse.ui.IWorkbenchPage activePage = org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            if (activePage == null) {
                return null;
            }
            org.eclipse.ui.IViewPart view = activePage.findView(org.eclipse.mylyn.tasks.ui.ITasksUiConstants.ID_VIEW_REPOSITORIES);
            if (view instanceof org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoriesView) {
                return ((org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoriesView) (view));
            }
        }
        return null;
    }

    public static org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoriesView openInActivePerspective() {
        try {
            return ((org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoriesView) (org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(org.eclipse.mylyn.tasks.ui.ITasksUiConstants.ID_VIEW_REPOSITORIES)));
        } catch (java.lang.Exception e) {
            return null;
        }
    }

    @java.lang.Override
    public void createPartControl(org.eclipse.swt.widgets.Composite parent) {
        contentProvider = new org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoriesContentProvider();
        viewer = new org.eclipse.jface.viewers.TreeViewer(parent, ((org.eclipse.swt.SWT.MULTI | org.eclipse.swt.SWT.H_SCROLL) | org.eclipse.swt.SWT.V_SCROLL) | org.eclipse.swt.SWT.FULL_SELECTION);
        viewer.setContentProvider(contentProvider);
        viewer.setUseHashlookup(true);
        org.eclipse.jface.viewers.ViewerFilter[] filters = new org.eclipse.jface.viewers.ViewerFilter[]{ new org.eclipse.mylyn.internal.tasks.ui.views.EmptyCategoriesFilter(contentProvider) };
        viewer.setFilters(filters);
        viewer.setLabelProvider(new org.eclipse.jface.viewers.DecoratingLabelProvider(new org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoryLabelProvider(), org.eclipse.ui.PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator()));
        viewer.setSorter(new org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoriesViewSorter());
        viewer.setInput(getViewSite());
        viewer.addDoubleClickListener(new org.eclipse.jface.viewers.IDoubleClickListener() {
            public void doubleClick(org.eclipse.jface.viewers.DoubleClickEvent event) {
                org.eclipse.mylyn.commons.workbench.WorkbenchUtil.openProperties(getSite());
            }
        });
        final org.eclipse.ui.themes.IThemeManager themeManager = getSite().getWorkbenchWindow().getWorkbench().getThemeManager();
        new org.eclipse.mylyn.commons.workbench.GradientDrawer(themeManager, getViewer()) {
            @java.lang.Override
            protected boolean shouldApplyGradient(org.eclipse.swt.widgets.Event event) {
                return event.item.getData() instanceof org.eclipse.mylyn.internal.tasks.core.Category;
            }
        };
        makeActions();
        hookContextMenu();
        hookGlobalActions();
        contributeToActionBars();
        getViewer().expandAll();
        getSite().setSelectionProvider(getViewer());
    }

    private void hookGlobalActions() {
        org.eclipse.ui.IActionBars bars = getViewSite().getActionBars();
        bars.setGlobalActionHandler(org.eclipse.ui.actions.ActionFactory.DELETE.getId(), deleteRepositoryAction);
        bars.setGlobalActionHandler(org.eclipse.ui.actions.ActionFactory.REFRESH.getId(), resetConfigurationAction);
    }

    private void makeActions() {
        deleteRepositoryAction = new org.eclipse.mylyn.internal.tasks.ui.actions.DeleteTaskRepositoryAction();
        viewer.addSelectionChangedListener(deleteRepositoryAction);
        resetConfigurationAction = new org.eclipse.mylyn.internal.tasks.ui.views.UpdateRepositoryConfigurationAction();
        resetConfigurationAction.setActionDefinitionId("org.eclipse.ui.file.refresh");// $NON-NLS-1$

        viewer.addSelectionChangedListener(resetConfigurationAction);
        offlineAction = new org.eclipse.mylyn.internal.tasks.ui.actions.DisconnectRepositoryAction();
        viewer.addSelectionChangedListener(offlineAction);
    }

    private void hookContextMenu() {
        org.eclipse.jface.action.MenuManager menuMgr = new org.eclipse.jface.action.MenuManager("#PopupMenu");// $NON-NLS-1$

        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new org.eclipse.jface.action.IMenuListener() {
            public void menuAboutToShow(org.eclipse.jface.action.IMenuManager manager) {
                TaskRepositoriesView.this.fillContextMenu(manager);
            }
        });
        org.eclipse.swt.widgets.Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }

    private void contributeToActionBars() {
        org.eclipse.ui.IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown(bars.getMenuManager());
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalPullDown(org.eclipse.jface.action.IMenuManager manager) {
        manager.add(addRepositoryAction);
    }

    private void fillContextMenu(org.eclipse.jface.action.IMenuManager manager) {
        manager.add(new org.eclipse.jface.action.Separator("new"));// $NON-NLS-1$

        manager.add(addRepositoryAction);
        manager.add(new org.eclipse.jface.action.Separator("edit"));// $NON-NLS-1$

        manager.add(deleteRepositoryAction);
        manager.add(resetConfigurationAction);
        manager.add(new org.eclipse.jface.action.Separator("operations"));// $NON-NLS-1$

        manager.add(offlineAction);
        manager.add(new org.eclipse.jface.action.Separator("repository"));// $NON-NLS-1$

        manager.add(new org.eclipse.jface.action.Separator(org.eclipse.ui.IWorkbenchActionConstants.MB_ADDITIONS));
        manager.add(new org.eclipse.jface.action.Separator());
        manager.add(new org.eclipse.jface.action.Separator("properties"));// $NON-NLS-1$

    }

    private void fillLocalToolBar(org.eclipse.jface.action.IToolBarManager manager) {
        manager.add(addRepositoryAction);
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    @java.lang.Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    public void refresh() {
        if ((viewer != null) && (!viewer.getControl().isDisposed())) {
            viewer.refresh();
            viewer.expandAll();
        }
    }

    public org.eclipse.jface.viewers.TreeViewer getViewer() {
        return viewer;
    }
}