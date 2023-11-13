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
package org.eclipse.mylyn.tasks.ui.wizards;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.wizards.Messages;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.osgi.util.NLS;
/**
 * Extend to provide a custom edit query dialog, typically invoked by the user requesting properties on a query node in
 * the Task List.
 *
 * @author Mik Kersten
 * @author Steffen Pingel
 * @since 3.0
 */
public class RepositoryQueryWizard extends org.eclipse.jface.wizard.Wizard {
    private final org.eclipse.mylyn.tasks.core.TaskRepository repository;

    /**
     *
     * @since 3.0
     */
    public RepositoryQueryWizard(org.eclipse.mylyn.tasks.core.TaskRepository repository) {
        org.eclipse.core.runtime.Assert.isNotNull(repository);
        this.repository = repository;
        setNeedsProgressMonitor(true);
        setWindowTitle(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.RepositoryQueryWizard_Edit_Repository_Query);
        setDefaultPageImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.BANNER_REPOSITORY);
    }

    @java.lang.Override
    public boolean canFinish() {
        org.eclipse.jface.wizard.IWizardPage currentPage = getContainer().getCurrentPage();
        if (currentPage instanceof org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositoryQueryPage2) {
            ((org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositoryQueryPage2) (currentPage)).updateTitleFromSuggestion();
        }
        if (currentPage instanceof org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositoryQueryPage) {
            return currentPage.isPageComplete();
        }
        return false;
    }

    @java.lang.Override
    public boolean performFinish() {
        org.eclipse.jface.wizard.IWizardPage currentPage = getContainer().getCurrentPage();
        if (!(currentPage instanceof org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositoryQueryPage)) {
            throw new java.lang.AssertionError(org.eclipse.osgi.util.NLS.bind("Current wizard page ''{0}'' does not extends AbstractRepositoryQueryPage", currentPage.getClass()));// $NON-NLS-1$

        }
        org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositoryQueryPage page = ((org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositoryQueryPage) (currentPage));
        org.eclipse.mylyn.tasks.core.IRepositoryQuery query = page.getQuery();
        if (query != null) {
            java.lang.String oldSummary = query.getSummary();
            page.applyTo(query);
            if (query instanceof org.eclipse.mylyn.internal.tasks.core.RepositoryQuery) {
                org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().notifyElementChanged(((org.eclipse.mylyn.internal.tasks.core.RepositoryQuery) (query)));
            }
            if ((oldSummary == null) || (!oldSummary.equals(query.getSummary()))) {
                // XXX trigger a full refresh to ensure correct sorting
                org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().notifyElementsChanged(null);
            }
        } else {
            query = page.createQuery();
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().addQuery(((org.eclipse.mylyn.internal.tasks.core.RepositoryQuery) (query)));
        }
        org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(getTaskRepository().getConnectorKind());
        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.synchronizeQuery(connector, ((org.eclipse.mylyn.internal.tasks.core.RepositoryQuery) (query)), null, true);
        return true;
    }

    public org.eclipse.mylyn.tasks.core.TaskRepository getTaskRepository() {
        return repository;
    }
}