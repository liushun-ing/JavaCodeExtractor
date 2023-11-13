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
 *     Brock Janiczak - improvements
 *     Eugene Kuleshov - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.wizards;
import org.eclipse.core.commands.Command;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardSelectionPage;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.commons.workbench.GradientDrawer;
import org.eclipse.mylyn.internal.tasks.core.Category;
import org.eclipse.mylyn.internal.tasks.core.ITaskRepositoryFilter;
import org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.actions.AddRepositoryAction;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoriesViewSorter;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoryLabelProvider;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.themes.IThemeManager;
/**
 *
 * @author Mik Kersten
 * @author Brock Janiczak
 * @author Steffen Pingel
 * @author Eugene Kuleshov
 */
public abstract class SelectRepositoryPage extends org.eclipse.jface.wizard.WizardSelectionPage {
    private org.eclipse.jface.viewers.TreeViewer viewer;

    protected org.eclipse.mylyn.internal.tasks.ui.wizards.MultiRepositoryAwareWizard wizard;

    private java.util.List<org.eclipse.mylyn.tasks.core.TaskRepository> repositories = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.TaskRepository>();

    private final org.eclipse.mylyn.internal.tasks.core.ITaskRepositoryFilter taskRepositoryFilter;

    // private TaskRepositoriesContentProvider contentProvider;
    class RepositoryContentProvider implements org.eclipse.jface.viewers.ITreeContentProvider {
        public java.lang.Object[] getChildren(java.lang.Object parentElement) {
            return null;
        }

        public java.lang.Object getParent(java.lang.Object element) {
            return null;
        }

        public boolean hasChildren(java.lang.Object element) {
            return false;
        }

        public java.lang.Object[] getElements(java.lang.Object inputElement) {
            return repositories.toArray();
        }

        public void dispose() {
            // ignore
        }

        public void inputChanged(org.eclipse.jface.viewers.Viewer viewer, java.lang.Object oldInput, java.lang.Object newInput) {
            // ignore
        }
    }

    public SelectRepositoryPage(org.eclipse.mylyn.internal.tasks.core.ITaskRepositoryFilter taskRepositoryFilter) {
        super(Messages.SelectRepositoryPage_Select_a_repository);
        setTitle(Messages.SelectRepositoryPage_Select_a_repository);
        setDescription(Messages.SelectRepositoryPage_Add_new_repositories_using_the_X_view);
        this.taskRepositoryFilter = taskRepositoryFilter;
        this.repositories = getTaskRepositories();
    }

    public java.util.List<org.eclipse.mylyn.tasks.core.TaskRepository> getTaskRepositories() {
        java.util.List<org.eclipse.mylyn.tasks.core.TaskRepository> repositories = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.TaskRepository>();
        org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager repositoryManager = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryManager();
        for (org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector : repositoryManager.getRepositoryConnectors()) {
            java.util.Set<org.eclipse.mylyn.tasks.core.TaskRepository> connectorRepositories = repositoryManager.getRepositories(connector.getConnectorKind());
            for (org.eclipse.mylyn.tasks.core.TaskRepository repository : connectorRepositories) {
                if (taskRepositoryFilter.accept(repository, connector)) {
                    repositories.add(repository);
                }
            }
        }
        return repositories;
    }

    public void createControl(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.widgets.Composite container = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NULL);
        org.eclipse.swt.layout.GridLayout layout = new org.eclipse.swt.layout.GridLayout(1, true);
        container.setLayout(layout);
        org.eclipse.swt.widgets.Tree tree = createTableViewer(container);
        viewer.setSorter(new org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoriesViewSorter());
        org.eclipse.swt.layout.GridData gridData = new org.eclipse.swt.layout.GridData((org.eclipse.swt.layout.GridData.FILL_BOTH | org.eclipse.swt.layout.GridData.GRAB_HORIZONTAL) | org.eclipse.swt.layout.GridData.GRAB_VERTICAL);
        tree.setLayoutData(gridData);
        org.eclipse.swt.widgets.Composite buttonContainer = new org.eclipse.swt.widgets.Composite(container, org.eclipse.swt.SWT.NULL);
        org.eclipse.swt.layout.GridLayout buttonLayout = new org.eclipse.swt.layout.GridLayout(2, false);
        buttonContainer.setLayout(buttonLayout);
        final org.eclipse.mylyn.internal.tasks.ui.actions.AddRepositoryAction action = new org.eclipse.mylyn.internal.tasks.ui.actions.AddRepositoryAction();
        action.setPromptToAddQuery(false);
        org.eclipse.swt.widgets.Button button = new org.eclipse.swt.widgets.Button(buttonContainer, org.eclipse.swt.SWT.NONE);
        button.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.HORIZONTAL_ALIGN_BEGINNING | org.eclipse.swt.layout.GridData.VERTICAL_ALIGN_BEGINNING));
        button.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.REPOSITORY_NEW));
        button.setText(org.eclipse.mylyn.internal.tasks.ui.actions.AddRepositoryAction.TITLE);
        button.setEnabled(action.isEnabled());
        button.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                org.eclipse.mylyn.tasks.core.TaskRepository taskRepository = action.showWizard();
                if (taskRepository != null) {
                    SelectRepositoryPage.this.repositories = getTaskRepositories();
                    viewer.setInput(org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnectors());
                    viewer.setSelection(new org.eclipse.jface.viewers.StructuredSelection(taskRepository));
                }
            }
        });
        final org.eclipse.core.commands.Command discoveryWizardCommand = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getConfiguredDiscoveryWizardCommand();
        if ((discoveryWizardCommand != null) && discoveryWizardCommand.isEnabled()) {
            org.eclipse.swt.widgets.Button discoveryButton = new org.eclipse.swt.widgets.Button(buttonContainer, org.eclipse.swt.SWT.PUSH);
            org.eclipse.jface.layout.GridDataFactory.swtDefaults().align(org.eclipse.swt.SWT.BEGINNING, org.eclipse.swt.SWT.CENTER).applyTo(discoveryButton);
            discoveryButton.setText(Messages.SelectRepositoryConnectorPage_activateDiscovery);
            discoveryButton.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.DISCOVERY));
            discoveryButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                @java.lang.Override
                public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
                    org.eclipse.ui.handlers.IHandlerService handlerService = ((org.eclipse.ui.handlers.IHandlerService) (org.eclipse.ui.PlatformUI.getWorkbench().getService(org.eclipse.ui.handlers.IHandlerService.class)));
                    try {
                        handlerService.executeCommand(discoveryWizardCommand.getId(), null);
                    } catch (java.lang.Exception e) {
                        org.eclipse.core.runtime.IStatus status = new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.osgi.util.NLS.bind(Messages.SelectRepositoryConnectorPage_discoveryProblemMessage, new java.lang.Object[]{ e.getMessage() }), e);
                        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.logAndDisplayStatus(Messages.SelectRepositoryConnectorPage_discoveryProblemTitle, status);
                    }
                }
            });
        }
        org.eclipse.jface.dialogs.Dialog.applyDialogFont(container);
        setControl(container);
    }

    protected org.eclipse.swt.widgets.Tree createTableViewer(org.eclipse.swt.widgets.Composite container) {
        viewer = new org.eclipse.jface.viewers.TreeViewer(container, ((org.eclipse.swt.SWT.SINGLE | org.eclipse.swt.SWT.BORDER) | org.eclipse.swt.SWT.H_SCROLL) | org.eclipse.swt.SWT.V_SCROLL);
        // contentProvider = new TeamRepositoriesContentProvider();
        viewer.setContentProvider(new org.eclipse.mylyn.internal.tasks.ui.wizards.SelectRepositoryPage.RepositoryContentProvider());
        // ViewerFilter[] filters = { new EmptyCategoriesFilter(contentProvider) };
        // viewer.setFilters(filters);
        // viewer.setLabelProvider(new TaskRepositoryLabelProvider());
        viewer.setLabelProvider(new org.eclipse.jface.viewers.DecoratingLabelProvider(new org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoryLabelProvider(), org.eclipse.ui.PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator()));
        viewer.setInput(org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnectors());
        viewer.addSelectionChangedListener(new org.eclipse.jface.viewers.ISelectionChangedListener() {
            public void selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent event) {
                org.eclipse.jface.viewers.IStructuredSelection selection = ((org.eclipse.jface.viewers.IStructuredSelection) (event.getSelection()));
                if (selection.getFirstElement() instanceof org.eclipse.mylyn.tasks.core.TaskRepository) {
                    setSelectedNode(new org.eclipse.mylyn.internal.tasks.ui.wizards.SelectRepositoryPage.CustomWizardNode(((org.eclipse.mylyn.tasks.core.TaskRepository) (selection.getFirstElement()))));
                    setPageComplete(true);
                } else {
                    setSelectedNode(null);
                    setPageComplete(false);
                }
            }
        });
        org.eclipse.mylyn.tasks.core.TaskRepository selectedRepository = org.eclipse.mylyn.tasks.ui.TasksUiUtil.getSelectedRepository(null);
        if (selectedRepository != null) {
            org.eclipse.mylyn.internal.tasks.core.Category category = ((org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager) (org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager())).getCategory(selectedRepository);
            java.lang.Object[] path = new java.lang.Object[]{ category, selectedRepository };
            viewer.setSelection(new org.eclipse.jface.viewers.TreeSelection(new org.eclipse.jface.viewers.TreePath(path)));
        } else {
            org.eclipse.mylyn.tasks.core.TaskRepository localRepository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector.CONNECTOR_KIND, org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector.REPOSITORY_URL);
            viewer.setSelection(new org.eclipse.jface.viewers.StructuredSelection(localRepository));
        }
        final org.eclipse.ui.themes.IThemeManager themeManager = org.eclipse.ui.PlatformUI.getWorkbench().getThemeManager();
        new org.eclipse.mylyn.commons.workbench.GradientDrawer(themeManager, getViewer()) {
            @java.lang.Override
            protected boolean shouldApplyGradient(org.eclipse.swt.widgets.Event event) {
                return event.item.getData() instanceof org.eclipse.mylyn.internal.tasks.core.Category;
            }
        };
        viewer.addOpenListener(new org.eclipse.jface.viewers.IOpenListener() {
            public void open(org.eclipse.jface.viewers.OpenEvent event) {
                if (canFlipToNextPage()) {
                    try {
                        getContainer().showPage(getNextPage());
                    } catch (java.lang.RuntimeException e) {
                        org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.WARNING, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Exception while opening the next wizard page", e));// $NON-NLS-1$

                    }
                } else if (canFinish()) {
                    if (getWizard().performFinish()) {
                        ((org.eclipse.jface.wizard.WizardDialog) (getContainer())).close();
                    }
                }
            }
        });
        viewer.expandAll();
        viewer.getTree().showSelection();
        viewer.getTree().setFocus();
        return viewer.getTree();
    }

    protected abstract org.eclipse.jface.wizard.IWizard createWizard(org.eclipse.mylyn.tasks.core.TaskRepository taskRepository);

    @java.lang.Override
    public boolean canFlipToNextPage() {
        return (getSelectedNode() != null) && (getNextPage() != null);
    }

    public boolean canFinish() {
        return (getSelectedNode() != null) && (getNextPage() == null);
    }

    public boolean performFinish() {
        if ((getSelectedNode() == null) || (getNextPage() != null)) {
            // finish event will get forwarded to nested wizard
            // by container
            return false;
        }
        return getSelectedNode().getWizard().performFinish();
    }

    private class CustomWizardNode implements org.eclipse.jface.wizard.IWizardNode {
        private final org.eclipse.mylyn.tasks.core.TaskRepository repository;

        private org.eclipse.jface.wizard.IWizard wizard;

        public CustomWizardNode(org.eclipse.mylyn.tasks.core.TaskRepository repository) {
            this.repository = repository;
        }

        public void dispose() {
            if (wizard != null) {
                wizard.dispose();
            }
        }

        public org.eclipse.swt.graphics.Point getExtent() {
            return new org.eclipse.swt.graphics.Point(-1, -1);
        }

        public org.eclipse.jface.wizard.IWizard getWizard() {
            if (wizard == null) {
                wizard = SelectRepositoryPage.this.createWizard(repository);
                if (wizard != null) {
                    wizard.setContainer(getContainer());
                }
            }
            return wizard;
        }

        public boolean isContentCreated() {
            return wizard != null;
        }

        @java.lang.Override
        public boolean equals(java.lang.Object obj) {
            if (!(obj instanceof org.eclipse.mylyn.internal.tasks.ui.wizards.SelectRepositoryPage.CustomWizardNode)) {
                return false;
            }
            org.eclipse.mylyn.internal.tasks.ui.wizards.SelectRepositoryPage.CustomWizardNode that = ((org.eclipse.mylyn.internal.tasks.ui.wizards.SelectRepositoryPage.CustomWizardNode) (obj));
            if (this == that) {
                return true;
            }
            return this.repository.getConnectorKind().equals(that.repository.getConnectorKind()) && this.repository.getRepositoryUrl().equals(that.repository.getRepositoryUrl());
        }

        @java.lang.Override
        public int hashCode() {
            return (31 * this.repository.getRepositoryUrl().hashCode()) + this.repository.getConnectorKind().hashCode();
        }
    }

    /**
     * Public for testing.
     */
    public org.eclipse.jface.viewers.TreeViewer getViewer() {
        return viewer;
    }

    /**
     * Public for testing.
     */
    public java.util.List<org.eclipse.mylyn.tasks.core.TaskRepository> getRepositories() {
        return repositories;
    }
}