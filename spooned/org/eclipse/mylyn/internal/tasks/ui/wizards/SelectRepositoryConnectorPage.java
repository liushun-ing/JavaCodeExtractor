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
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.wizards;
import org.eclipse.core.commands.Command;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.ui.ConnectorBrand;
import org.eclipse.mylyn.internal.tasks.ui.IBrandManager;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoriesSorter;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoryLabelProvider;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
/**
 *
 * @author Mik Kersten
 * @author David Green
 */
public class SelectRepositoryConnectorPage extends org.eclipse.jface.wizard.WizardPage {
    private org.eclipse.jface.viewers.TableViewer viewer;

    private org.eclipse.mylyn.internal.tasks.ui.ConnectorBrand connectorBrand;

    static class ConnectorBrandContentProvider implements org.eclipse.jface.viewers.IStructuredContentProvider {
        private final java.util.Collection<? extends org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector> connectors;

        private final org.eclipse.mylyn.internal.tasks.ui.IBrandManager brandManager;

        public ConnectorBrandContentProvider(org.eclipse.mylyn.internal.tasks.ui.IBrandManager brandManager, java.util.Collection<? extends org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector> connectors) {
            this.brandManager = brandManager;
            this.connectors = connectors;
        }

        public void inputChanged(org.eclipse.jface.viewers.Viewer v, java.lang.Object oldInput, java.lang.Object newInput) {
        }

        public void dispose() {
        }

        public java.lang.Object[] getElements(java.lang.Object parent) {
            java.util.List<org.eclipse.mylyn.internal.tasks.ui.ConnectorBrand> connectorBrands = new java.util.ArrayList<org.eclipse.mylyn.internal.tasks.ui.ConnectorBrand>();
            for (org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector : connectors) {
                if (connector.isUserManaged() && connector.canCreateRepository()) {
                    connectorBrands.add(new org.eclipse.mylyn.internal.tasks.ui.ConnectorBrand(connector, null));
                    for (java.lang.String brand : brandManager.getBrands(connector.getConnectorKind())) {
                        connectorBrands.add(new org.eclipse.mylyn.internal.tasks.ui.ConnectorBrand(connector, brand));
                    }
                }
            }
            return connectorBrands.toArray();
        }
    }

    public SelectRepositoryConnectorPage() {
        super(Messages.SelectRepositoryConnectorPage_Select_a_task_repository_type);
        setTitle(Messages.SelectRepositoryConnectorPage_Select_a_task_repository_type);
        setDescription(Messages.SelectRepositoryConnectorPage_You_can_connect_to_an_existing_account_using_one_of_the_installed_connectors);
    }

    @java.lang.Override
    public boolean canFlipToNextPage() {
        return connectorBrand != null;
    }

    public void createControl(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.widgets.Composite container = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NULL);
        org.eclipse.jface.layout.GridLayoutFactory.fillDefaults().numColumns(1).spacing(0, 3).applyTo(container);
        viewer = new org.eclipse.jface.viewers.TableViewer(container, ((org.eclipse.swt.SWT.SINGLE | org.eclipse.swt.SWT.BORDER) | org.eclipse.swt.SWT.H_SCROLL) | org.eclipse.swt.SWT.V_SCROLL);
        viewer.setContentProvider(new org.eclipse.mylyn.internal.tasks.ui.wizards.SelectRepositoryConnectorPage.ConnectorBrandContentProvider(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getBrandManager(), org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnectors()));
        viewer.setSorter(new org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoriesSorter());
        viewer.setLabelProvider(new org.eclipse.jface.viewers.DecoratingLabelProvider(new org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoryLabelProvider(), org.eclipse.ui.PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator()));
        viewer.setInput(org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnectors());
        viewer.addSelectionChangedListener(new org.eclipse.jface.viewers.ISelectionChangedListener() {
            public void selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent event) {
                org.eclipse.jface.viewers.IStructuredSelection selection = ((org.eclipse.jface.viewers.IStructuredSelection) (event.getSelection()));
                if (selection.getFirstElement() instanceof org.eclipse.mylyn.internal.tasks.ui.ConnectorBrand) {
                    setConnectorBrand(((org.eclipse.mylyn.internal.tasks.ui.ConnectorBrand) (selection.getFirstElement())));
                    setPageComplete(true);
                }
            }
        });
        viewer.addOpenListener(new org.eclipse.jface.viewers.IOpenListener() {
            public void open(org.eclipse.jface.viewers.OpenEvent event) {
                getContainer().showPage(getNextPage());
            }
        });
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getControl());
        // add a hyperlink for connector discovery if it's available and enabled.
        // we integrate with discovery via a well-known command, which when invoked launches the discovery wizard
        final org.eclipse.core.commands.Command discoveryWizardCommand = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getConfiguredDiscoveryWizardCommand();
        if ((discoveryWizardCommand != null) && discoveryWizardCommand.isEnabled()) {
            org.eclipse.swt.widgets.Button discoveryButton = new org.eclipse.swt.widgets.Button(container, org.eclipse.swt.SWT.PUSH);
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

    void setConnectorBrand(org.eclipse.mylyn.internal.tasks.ui.ConnectorBrand connectorBrand) {
        this.connectorBrand = connectorBrand;
    }

    public org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector getConnector() {
        return connectorBrand == null ? null : connectorBrand.getConnector();
    }

    public java.lang.String getBrand() {
        return connectorBrand == null ? null : connectorBrand.getBrandId();
    }
}