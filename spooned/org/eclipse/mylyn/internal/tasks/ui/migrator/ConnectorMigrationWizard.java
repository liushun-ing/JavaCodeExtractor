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
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.workbench.browser.BrowserUtil;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryManager;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
public class ConnectorMigrationWizard extends org.eclipse.jface.wizard.Wizard {
    private static class CollectionContentProvider implements org.eclipse.jface.viewers.ITreeContentProvider {
        @java.lang.Override
        public java.lang.Object[] getChildren(java.lang.Object parentElement) {
            return new java.lang.Object[0];
        }

        @java.lang.Override
        public java.lang.Object getParent(java.lang.Object element) {
            return null;
        }

        @java.lang.Override
        public boolean hasChildren(java.lang.Object element) {
            return false;
        }

        @java.lang.Override
        public java.lang.Object[] getElements(java.lang.Object inputElement) {
            if (inputElement instanceof java.util.Collection<?>) {
                return ((java.util.Collection<?>) (inputElement)).toArray();
            }
            return new java.lang.Object[0];
        }

        @java.lang.Override
        public void dispose() {
        }

        @java.lang.Override
        public void inputChanged(org.eclipse.jface.viewers.Viewer viewer, java.lang.Object oldInput, java.lang.Object newInput) {
        }
    }

    private java.lang.Object[] selectedConnectors = new java.lang.Object[0];

    private final org.eclipse.mylyn.internal.tasks.ui.migrator.ConnectorMigrator migrator;

    public ConnectorMigrationWizard(org.eclipse.mylyn.internal.tasks.ui.migrator.ConnectorMigrator migrator) {
        this.migrator = migrator;
        setNeedsProgressMonitor(true);
    }

    @java.lang.Override
    public void addPages() {
        setWindowTitle(Messages.ConnectorMigrationWizard_Connector_Migration);
        addPage(new org.eclipse.jface.wizard.WizardPage(Messages.ConnectorMigrationWizard_End_of_Connector_Support) {
            @java.lang.Override
            public void createControl(org.eclipse.swt.widgets.Composite parent) {
                setTitle(Messages.ConnectorMigrationWizard_End_of_Connector_Support);
                setMessage(Messages.ConnectorMigrationWizard_Message, org.eclipse.jface.dialogs.IMessageProvider.INFORMATION);
                org.eclipse.swt.widgets.Composite c = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NONE);
                org.eclipse.jface.layout.GridLayoutFactory.fillDefaults().applyTo(c);
                org.eclipse.swt.widgets.Link text = new org.eclipse.swt.widgets.Link(c, (org.eclipse.swt.SWT.READ_ONLY | org.eclipse.swt.SWT.MULTI) | org.eclipse.swt.SWT.WRAP);
                text.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                    @java.lang.Override
                    public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                        org.eclipse.mylyn.commons.workbench.browser.BrowserUtil.openUrl(e.text, org.eclipse.ui.browser.IWorkbenchBrowserSupport.AS_EXTERNAL);
                    }
                });
                text.setText(org.eclipse.osgi.util.NLS.bind(Messages.ConnectorMigrationWizard_Body, migrator.getExplanatoryText()));
                org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.FILL).grab(true, true).hint(600, org.eclipse.swt.SWT.DEFAULT).applyTo(text);
                setControl(c);
            }
        });
        addPage(new org.eclipse.jface.wizard.WizardPage(Messages.ConnectorMigrationWizard_Select_Connectors) {
            @java.lang.Override
            public void createControl(org.eclipse.swt.widgets.Composite parent) {
                setTitle(Messages.ConnectorMigrationWizard_Select_Connectors);
                setDescription(Messages.ConnectorMigrationWizard_Select_the_connectors_to_migrate);
                org.eclipse.swt.widgets.Composite c = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NONE);
                org.eclipse.jface.layout.GridLayoutFactory.fillDefaults().applyTo(c);
                java.util.List<java.lang.String> kinds = getRelevantConnectorKinds(migrator.getConnectorKinds().keySet());
                final org.eclipse.jface.viewers.CheckboxTreeViewer viewer = createConnectorList(c, kinds);
                selectedConnectors = kinds.toArray();
                viewer.setCheckedElements(selectedConnectors);
                viewer.addCheckStateListener(new org.eclipse.jface.viewers.ICheckStateListener() {
                    @java.lang.Override
                    public void checkStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent event) {
                        selectedConnectors = viewer.getCheckedElements();
                        setPageComplete(selectedConnectors.length > 0);
                    }
                });
                org.eclipse.jface.layout.GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getControl());
                setControl(c);
            }

            @java.lang.Override
            public boolean isPageComplete() {
                return super.isPageComplete() && isCurrentPage();
            }
        });
    }

    protected org.eclipse.jface.viewers.CheckboxTreeViewer createConnectorList(org.eclipse.swt.widgets.Composite parent, java.util.List<java.lang.String> kinds) {
        final org.eclipse.jface.viewers.CheckboxTreeViewer viewer = new org.eclipse.jface.viewers.CheckboxTreeViewer(parent);
        viewer.setContentProvider(new org.eclipse.mylyn.internal.tasks.ui.migrator.ConnectorMigrationWizard.CollectionContentProvider());
        viewer.setInput(kinds);
        viewer.setLabelProvider(new org.eclipse.jface.viewers.LabelProvider() {
            @java.lang.Override
            public java.lang.String getText(java.lang.Object element) {
                if (element instanceof java.lang.String) {
                    java.lang.String kind = ((java.lang.String) (element));
                    org.eclipse.mylyn.tasks.core.IRepositoryManager manager = migrator.getRepositoryManager();
                    org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = manager.getRepositoryConnector(kind);
                    if (connector != null) {
                        return connector.getLabel() + org.eclipse.osgi.util.NLS.bind(Messages.ConnectorMigrationWizard_used_by_X_repositories, manager.getRepositories(kind).size());
                    }
                }
                return super.getText(element);
            }
        });
        return viewer;
    }

    private java.util.List<java.lang.String> getRelevantConnectorKinds(java.util.Set<java.lang.String> connectorKinds) {
        org.eclipse.mylyn.tasks.core.IRepositoryManager manager = migrator.getRepositoryManager();
        java.util.List<java.lang.String> relevantConnectorKinds = new java.util.LinkedList<>();
        for (java.lang.String connectorKind : connectorKinds) {
            if (!manager.getRepositories(connectorKind).isEmpty()) {
                relevantConnectorKinds.add(connectorKind);
            }
        }
        return relevantConnectorKinds;
    }

    @java.lang.Override
    public boolean performFinish() {
        try {
            getContainer().run(true, true, new org.eclipse.jface.operation.IRunnableWithProgress() {
                @java.lang.Override
                public void run(org.eclipse.core.runtime.IProgressMonitor monitor) throws java.lang.reflect.InvocationTargetException, java.lang.InterruptedException {
                    com.google.common.collect.ImmutableList<java.lang.String> connectors = com.google.common.collect.FluentIterable.from(com.google.common.collect.ImmutableList.copyOf(selectedConnectors)).filter(java.lang.String.class).toList();
                    try {
                        migrator.setConnectorsToMigrate(connectors);
                        migrator.migrateConnectors(monitor);
                    } catch (java.io.IOException e) {
                        throw new java.lang.reflect.InvocationTargetException(e);
                    }
                }
            });
        } catch (java.lang.reflect.InvocationTargetException | java.lang.InterruptedException e) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, e.getMessage(), e));
            org.eclipse.jface.wizard.IWizardPage page = getContainer().getCurrentPage();
            if ((page instanceof org.eclipse.jface.wizard.WizardPage) && (e.getCause() != null)) {
                ((org.eclipse.jface.wizard.WizardPage) (page)).setErrorMessage(e.getCause().getMessage());
            }
            return false;
        }
        return true;
    }
}