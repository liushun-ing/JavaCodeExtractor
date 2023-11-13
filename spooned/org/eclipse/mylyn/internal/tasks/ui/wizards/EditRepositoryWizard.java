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
package org.eclipse.mylyn.internal.tasks.ui.wizards;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryDelta;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryDelta.Type;
import org.eclipse.mylyn.internal.tasks.ui.RefactorRepositoryUrlOperation;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.statushandlers.StatusManager;
/**
 *
 * @author Mik Kersten
 */
public class EditRepositoryWizard extends org.eclipse.jface.wizard.Wizard implements org.eclipse.ui.INewWizard {
    private org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage settingsPage;

    private final org.eclipse.mylyn.tasks.core.TaskRepository repository;

    private final org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi;

    public EditRepositoryWizard(org.eclipse.mylyn.tasks.core.TaskRepository repository) {
        this(repository, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnectorUi(repository.getConnectorKind()));
    }

    public EditRepositoryWizard(org.eclipse.mylyn.tasks.core.TaskRepository repository, org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi) {
        org.eclipse.core.runtime.Assert.isNotNull(repository);
        org.eclipse.core.runtime.Assert.isNotNull(connectorUi);
        this.repository = repository;
        this.connectorUi = connectorUi;
        setNeedsProgressMonitor(true);
        setDefaultPageImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.BANNER_REPOSITORY_SETTINGS);
        setWindowTitle(Messages.EditRepositoryWizard_Properties_for_Task_Repository);
    }

    /**
     * Custom properties should be set on the repository object to ensure they are saved.
     */
    @java.lang.Override
    public boolean performFinish() {
        if (canFinish()) {
            boolean finishAccepted = settingsPage.preFinish(repository);
            if (finishAccepted) {
                java.lang.String oldUrl = repository.getRepositoryUrl();
                java.lang.String newUrl = settingsPage.getRepositoryUrl();
                if (((oldUrl != null) && (newUrl != null)) && (!oldUrl.equals(newUrl))) {
                    org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().deactivateActiveTask();
                    org.eclipse.mylyn.internal.tasks.ui.RefactorRepositoryUrlOperation operation = new org.eclipse.mylyn.internal.tasks.ui.RefactorRepositoryUrlOperation(repository, oldUrl, newUrl);
                    try {
                        getContainer().run(true, false, operation);
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        org.eclipse.ui.statushandlers.StatusManager.getManager().handle(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.WARNING, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, Messages.EditRepositoryWizard_Failed_to_refactor_repository_urls, e), org.eclipse.ui.statushandlers.StatusManager.SHOW | org.eclipse.ui.statushandlers.StatusManager.LOG);
                        return false;
                    } catch (java.lang.InterruptedException e) {
                        // should not get here
                    }
                }
                if (!repository.getConnectorKind().equals(org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector.CONNECTOR_KIND)) {
                    repository.setRepositoryUrl(newUrl);
                }
                settingsPage.performFinish(repository);
                if (((oldUrl != null) && (newUrl != null)) && (!oldUrl.equals(newUrl))) {
                    org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryManager().notifyRepositoryUrlChanged(repository, oldUrl);
                }
                org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryManager().notifyRepositorySettingsChanged(repository, new org.eclipse.mylyn.internal.tasks.core.TaskRepositoryDelta(org.eclipse.mylyn.internal.tasks.core.TaskRepositoryDelta.Type.ALL));
                org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getExternalizationManager().requestSave();
            }
            return finishAccepted;
        }
        return false;
    }

    public void init(org.eclipse.ui.IWorkbench workbench, org.eclipse.jface.viewers.IStructuredSelection selection) {
    }

    @java.lang.Override
    public void addPages() {
        settingsPage = connectorUi.getSettingsPage(repository);
        if (settingsPage instanceof org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage) {
            ((org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage) (settingsPage)).setRepository(repository);
            ((org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage) (settingsPage)).setVersion(repository.getVersion());
        }
        settingsPage.setWizard(this);
        addPage(settingsPage);
    }

    @java.lang.Override
    public boolean canFinish() {
        return settingsPage.isPageComplete();
    }

    /**
     * public for testing
     */
    public org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage getSettingsPage() {
        return settingsPage;
    }

    public org.eclipse.mylyn.tasks.core.TaskRepository getRepository() {
        return repository;
    }
}