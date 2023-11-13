/**
 * *****************************************************************************
 * Copyright (c) 2015 Tasktop Technologies.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.migrator;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoryLabelProvider;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.TaskElementLabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import static com.google.common.collect.Iterables.any;
import static org.eclipse.mylyn.internal.tasks.ui.migrator.TaskPredicates.isQueryForConnector;
import static org.eclipse.mylyn.internal.tasks.ui.migrator.TaskPredicates.isSynchronizing;
public class CompleteConnectorMigrationWizard extends org.eclipse.jface.wizard.Wizard {
    final class MapContentProvider implements org.eclipse.jface.viewers.ITreeContentProvider {
        private final java.util.Map<?, ? extends java.util.Collection<?>> map;

        private MapContentProvider(java.util.Map<?, ? extends java.util.Collection<?>> map) {
            this.map = map;
        }

        @java.lang.Override
        public void inputChanged(org.eclipse.jface.viewers.Viewer viewer, java.lang.Object oldInput, java.lang.Object newInput) {
        }

        @java.lang.Override
        public void dispose() {
        }

        @java.lang.Override
        public boolean hasChildren(java.lang.Object element) {
            return map.containsKey(element);
        }

        @java.lang.Override
        public java.lang.Object getParent(java.lang.Object element) {
            return null;
        }

        @java.lang.Override
        public java.lang.Object[] getElements(java.lang.Object inputElement) {
            if (inputElement instanceof java.util.Map) {
                return ((java.util.Map<?, ?>) (inputElement)).keySet().toArray();
            }
            return null;
        }

        @java.lang.Override
        public java.lang.Object[] getChildren(java.lang.Object parentElement) {
            if (map.get(parentElement) == null) {
                return null;
            }
            return map.get(parentElement).toArray();
        }
    }

    private final org.eclipse.mylyn.internal.tasks.ui.migrator.ConnectorMigrator migrator;

    public CompleteConnectorMigrationWizard(org.eclipse.mylyn.internal.tasks.ui.migrator.ConnectorMigrator migrator) {
        this.migrator = migrator;
    }

    @java.lang.Override
    public void addPages() {
        setWindowTitle(Messages.CompleteConnectorMigrationWizard_Complete_Connector_Migration);
        if (!migrator.allQueriesMigrated()) {
            addPage(new org.eclipse.jface.wizard.WizardPage(Messages.CompleteConnectorMigrationWizard_Migrate_Queries) {
                @java.lang.Override
                public void createControl(org.eclipse.swt.widgets.Composite parent) {
                    setTitle(Messages.CompleteConnectorMigrationWizard_Have_You_Recreated_Your_Queries);
                    java.lang.String message = Messages.CompleteConnectorMigrationWizard_ensure_created_queries;
                    if (migrator.anyQueriesMigrated()) {
                        message = Messages.CompleteConnectorMigrationWizard_Queries_not_migrated;
                    }
                    setMessage(org.eclipse.osgi.util.NLS.bind(Messages.CompleteConnectorMigrationWizard_first_page_message, message), org.eclipse.jface.dialogs.IMessageProvider.INFORMATION);
                    org.eclipse.swt.widgets.Composite c = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NONE);
                    org.eclipse.jface.layout.GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true).applyTo(c);
                    org.eclipse.swt.widgets.Label oldQueriesLabel = new org.eclipse.swt.widgets.Label(c, org.eclipse.swt.SWT.NONE);
                    oldQueriesLabel.setFont(org.eclipse.jface.resource.JFaceResources.getFontRegistry().getBold(org.eclipse.jface.resource.JFaceResources.DEFAULT_FONT));
                    oldQueriesLabel.setText(Messages.CompleteConnectorMigrationWizard_Queries_Using_Old_Connectors);
                    org.eclipse.swt.widgets.Label newQueriesLabel = new org.eclipse.swt.widgets.Label(c, org.eclipse.swt.SWT.NONE);
                    newQueriesLabel.setFont(org.eclipse.jface.resource.JFaceResources.getFontRegistry().getBold(org.eclipse.jface.resource.JFaceResources.DEFAULT_FONT));
                    newQueriesLabel.setText(Messages.CompleteConnectorMigrationWizard_Queries_Using_New_Connectors);
                    createQueryTree(c, createRepositoryQueryMap(migrator.getSelectedConnectors().keySet()));
                    createQueryTree(c, createRepositoryQueryMap(migrator.getSelectedConnectors().values()));
                    setControl(c);
                }
            });
        }
        addPage(new org.eclipse.jface.wizard.WizardPage(Messages.CompleteConnectorMigrationWizard_Complete_Migration) {
            @java.lang.Override
            public void createControl(org.eclipse.swt.widgets.Composite parent) {
                setTitle(Messages.CompleteConnectorMigrationWizard_Complete_Migration);
                setMessage(Messages.CompleteConnectorMigrationWizard_second_page_message, org.eclipse.jface.dialogs.IMessageProvider.INFORMATION);
                org.eclipse.swt.widgets.Composite c = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NONE);
                org.eclipse.jface.layout.GridLayoutFactory.fillDefaults().applyTo(c);
                org.eclipse.swt.widgets.Text text = new org.eclipse.swt.widgets.Text(c, (org.eclipse.swt.SWT.READ_ONLY | org.eclipse.swt.SWT.MULTI) | org.eclipse.swt.SWT.WRAP);
                text.setText(Messages.CompleteConnectorMigrationWizard_second_page_text);
                org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.FILL).grab(true, true).hint(600, org.eclipse.swt.SWT.DEFAULT).applyTo(text);
                setControl(c);
            }

            @java.lang.Override
            public boolean isPageComplete() {
                return super.isPageComplete() && isCurrentPage();
            }
        });
    }

    protected org.eclipse.jface.viewers.TreeViewer createQueryTree(org.eclipse.swt.widgets.Composite parent, final java.util.Map<org.eclipse.mylyn.tasks.core.TaskRepository, ? extends java.util.Set<org.eclipse.mylyn.internal.tasks.core.RepositoryQuery>> queries) {
        final org.eclipse.jface.viewers.TreeViewer viewer = new org.eclipse.jface.viewers.TreeViewer(parent);
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().grab(false, true).hint(500, org.eclipse.swt.SWT.DEFAULT).applyTo(viewer.getControl());
        viewer.setContentProvider(new org.eclipse.mylyn.internal.tasks.ui.migrator.CompleteConnectorMigrationWizard.MapContentProvider(queries));
        viewer.setInput(queries);
        viewer.setSorter(new org.eclipse.jface.viewers.ViewerSorter());
        viewer.setLabelProvider(new org.eclipse.mylyn.tasks.ui.TaskElementLabelProvider() {
            private final org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoryLabelProvider repositoryLabelProvider = new org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoryLabelProvider();

            @java.lang.Override
            public org.eclipse.swt.graphics.Image getImage(java.lang.Object element) {
                if (element instanceof org.eclipse.mylyn.tasks.core.TaskRepository) {
                    return repositoryLabelProvider.getImage(element);
                }
                return super.getImage(element);
            }

            @java.lang.Override
            public java.lang.String getText(java.lang.Object object) {
                if (object instanceof org.eclipse.mylyn.tasks.core.TaskRepository) {
                    return repositoryLabelProvider.getText(object);
                }
                return super.getText(object);
            }
        });
        viewer.addDoubleClickListener(new org.eclipse.jface.viewers.IDoubleClickListener() {
            @java.lang.Override
            public void doubleClick(org.eclipse.jface.viewers.DoubleClickEvent event) {
                if (viewer.getSelection() instanceof org.eclipse.jface.viewers.IStructuredSelection) {
                    java.lang.Object element = ((org.eclipse.jface.viewers.IStructuredSelection) (viewer.getSelection())).getFirstElement();
                    if (element instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery) {
                        org.eclipse.mylyn.tasks.core.IRepositoryQuery query = ((org.eclipse.mylyn.tasks.core.IRepositoryQuery) (element));
                        org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnectorUi(query.getConnectorKind());
                        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.openEditQueryDialog(connectorUi, query);
                    }
                }
            }
        });
        org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
            @java.lang.Override
            public void run() {
                viewer.expandAll();
            }
        });
        return viewer;
    }

    @java.lang.Override
    public boolean performFinish() {
        org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job(Messages.CompleteConnectorMigrationWizard_Migrating_Tasks_and_Private_Data) {
            @java.lang.Override
            protected org.eclipse.core.runtime.IStatus run(org.eclipse.core.runtime.IProgressMonitor monitor) {
                monitor.beginTask(Messages.CompleteConnectorMigrationWizard_Completing_connector_migration, org.eclipse.core.runtime.IProgressMonitor.UNKNOWN);
                java.util.Collection<java.lang.String> newConnectors = migrator.getSelectedConnectors().values();
                waitForQueriesToSynchronize(newConnectors, monitor);
                migrator.migrateTasks(monitor);
                return org.eclipse.core.runtime.Status.OK_STATUS;
            }
        };
        job.setUser(true);
        job.setSystem(false);
        job.schedule();
        return true;
    }

    protected void waitForQueriesToSynchronize(java.util.Collection<java.lang.String> newConnectors, org.eclipse.core.runtime.IProgressMonitor monitor) {
        monitor.subTask(Messages.CompleteConnectorMigrationWizard_Waiting_for_queries_to_synchronize);
        java.lang.Iterable<org.eclipse.mylyn.internal.tasks.core.RepositoryQuery> queries = com.google.common.collect.Iterables.concat(createRepositoryQueryMap(newConnectors).values());
        long start = java.lang.System.currentTimeMillis();
        while (any(queries, org.eclipse.mylyn.internal.tasks.ui.migrator.TaskPredicates.isSynchronizing()) && ((java.lang.System.currentTimeMillis() - start) < java.util.concurrent.TimeUnit.MILLISECONDS.convert(20, java.util.concurrent.TimeUnit.MINUTES))) {
            try {
                java.lang.Thread.sleep(java.util.concurrent.TimeUnit.MILLISECONDS.convert(3, java.util.concurrent.TimeUnit.SECONDS));
            } catch (java.lang.InterruptedException e) {
                // NOSONAR
            }
        } 
    }

    protected java.util.Map<org.eclipse.mylyn.tasks.core.TaskRepository, java.util.Set<org.eclipse.mylyn.internal.tasks.core.RepositoryQuery>> createRepositoryQueryMap(java.util.Collection<java.lang.String> kinds) {
        com.google.common.collect.ImmutableMap.Builder<org.eclipse.mylyn.tasks.core.TaskRepository, java.util.Set<org.eclipse.mylyn.internal.tasks.core.RepositoryQuery>> repositories = com.google.common.collect.ImmutableMap.builder();
        for (final java.lang.String kind : kinds) {
            for (org.eclipse.mylyn.tasks.core.TaskRepository repository : migrator.getRepositoryManager().getRepositories(kind)) {
                java.util.Set<org.eclipse.mylyn.internal.tasks.core.RepositoryQuery> queriesForUrl = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().getRepositoryQueries(repository.getRepositoryUrl());
                java.util.Set<org.eclipse.mylyn.internal.tasks.core.RepositoryQuery> queries = com.google.common.collect.Sets.filter(queriesForUrl, org.eclipse.mylyn.internal.tasks.ui.migrator.TaskPredicates.isQueryForConnector(kind));
                if (!queries.isEmpty()) {
                    repositories.put(repository, queries);
                }
            }
        }
        return repositories.build();
    }
}