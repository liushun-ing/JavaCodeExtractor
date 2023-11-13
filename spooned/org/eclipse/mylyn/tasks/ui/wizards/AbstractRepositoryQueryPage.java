/**
 * *****************************************************************************
 * Copyright (c) 2004, 2013 Tasktop Technologies and others.
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
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.wizards.Messages;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
/**
 * Extend to provide repository-specific query page to the Workbench search dialog.
 * <p>
 * It is recommended that clients extend {@link AbstractRepositoryQueryPage2} instead.
 *
 * @author Rob Elves
 * @author Steffen Pingel
 * @since 3.0
 */
public abstract class AbstractRepositoryQueryPage extends org.eclipse.jface.wizard.WizardPage implements org.eclipse.mylyn.tasks.ui.wizards.ITaskSearchPage {
    private org.eclipse.mylyn.tasks.ui.wizards.ITaskSearchPageContainer searchContainer;

    private final org.eclipse.mylyn.tasks.core.TaskRepository taskRepository;

    private final org.eclipse.mylyn.tasks.core.IRepositoryQuery query;

    public AbstractRepositoryQueryPage(java.lang.String pageName, org.eclipse.mylyn.tasks.core.TaskRepository taskRepository, org.eclipse.mylyn.tasks.core.IRepositoryQuery query) {
        super(pageName);
        // see http://ekkescorner.wordpress.com/2009/07/07/galileo-enter-the-twilight-zone-between-target-platform-runtime-and-ide-development/
        if (!java.beans.Beans.isDesignTime()) {
            org.eclipse.core.runtime.Assert.isNotNull(taskRepository);
        }
        this.taskRepository = taskRepository;
        this.query = query;
        setTitle(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositoryQueryPage_Enter_query_parameters);
        setDescription(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositoryQueryPage_If_attributes_are_blank_or_stale_press_the_Update_button);
        // see http://ekkescorner.wordpress.com/2009/07/07/galileo-enter-the-twilight-zone-between-target-platform-runtime-and-ide-development/
        if (!java.beans.Beans.isDesignTime()) {
            setImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.BANNER_REPOSITORY);
        }
        setPageComplete(false);
    }

    public AbstractRepositoryQueryPage(java.lang.String pageName, org.eclipse.mylyn.tasks.core.TaskRepository taskRepository) {
        this(pageName, taskRepository, null);
    }

    public org.eclipse.mylyn.tasks.core.IRepositoryQuery getQuery() {
        return query;
    }

    public abstract java.lang.String getQueryTitle();

    @java.lang.Override
    public boolean isPageComplete() {
        // reset error message to maintain backward compatibility: bug 288892
        setErrorMessage(null);
        java.lang.String queryTitle = getQueryTitle();
        if ((queryTitle == null) || queryTitle.equals("")) {
            // $NON-NLS-1$
            setMessage(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositoryQueryPage_Please_specify_a_title_for_the_query);
            return false;
        } else {
            java.util.Set<org.eclipse.mylyn.internal.tasks.core.RepositoryQuery> queries = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().getQueries();
            java.util.Set<org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory> categories = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().getCategories();
            java.lang.String oldSummary = null;
            if (query != null) {
                oldSummary = query.getSummary();
            }
            if ((oldSummary == null) || (!queryTitle.equals(oldSummary))) {
                for (org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory category : categories) {
                    if (queryTitle.equals(category.getSummary())) {
                        setMessage(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositoryQueryPage_A_category_with_this_name_already_exists, org.eclipse.jface.dialogs.IMessageProvider.ERROR);
                        return false;
                    }
                }
                for (org.eclipse.mylyn.internal.tasks.core.RepositoryQuery repositoryQuery : queries) {
                    if ((query == null) || (!query.equals(repositoryQuery))) {
                        if (queryTitle.equals(repositoryQuery.getSummary()) && repositoryQuery.getRepositoryUrl().equals(getTaskRepository().getRepositoryUrl())) {
                            setMessage(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositoryQueryPage_A_query_with_this_name_already_exists, org.eclipse.jface.dialogs.IMessageProvider.WARNING);
                            return true;
                        }
                    }
                }
            }
        }
        setMessage(null);
        return true;
    }

    public org.eclipse.mylyn.tasks.core.IRepositoryQuery createQuery() {
        org.eclipse.mylyn.tasks.core.IRepositoryQuery query = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryModel().createRepositoryQuery(getTaskRepository());
        applyTo(query);
        return query;
    }

    public abstract void applyTo(@org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.core.IRepositoryQuery query);

    public void saveState() {
        // empty
    }

    public void setContainer(org.eclipse.mylyn.tasks.ui.wizards.ITaskSearchPageContainer container) {
        searchContainer = container;
    }

    public boolean inSearchContainer() {
        return searchContainer != null;
    }

    public boolean performSearch() {
        org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(taskRepository.getConnectorKind());
        if (connector != null) {
            try {
                org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil.runSearchQuery(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList(), taskRepository, createQuery(), true);
            } catch (java.lang.UnsupportedOperationException e) {
                org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil.runSearchQuery(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList(), taskRepository, getQuery(), true);
            }
        }
        return true;
    }

    /**
     *
     * @since 2.1
     */
    public void setControlsEnabled(boolean enabled) {
        setControlsEnabled(getControl(), enabled);
    }

    // TODO: make reusable or find better API, task editor has similar functionality
    private void setControlsEnabled(org.eclipse.swt.widgets.Control control, boolean enabled) {
        if (control instanceof org.eclipse.swt.widgets.Composite) {
            for (org.eclipse.swt.widgets.Control childControl : ((org.eclipse.swt.widgets.Composite) (control)).getChildren()) {
                childControl.setEnabled(enabled);
                setControlsEnabled(childControl, enabled);
            }
        }
        setPageComplete(isPageComplete());
    }

    public org.eclipse.mylyn.tasks.ui.wizards.ITaskSearchPageContainer getSearchContainer() {
        return searchContainer;
    }

    public org.eclipse.mylyn.tasks.core.TaskRepository getTaskRepository() {
        return taskRepository;
    }
}