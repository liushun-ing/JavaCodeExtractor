/**
 * *****************************************************************************
 * Copyright (c) 2010, 2014 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Flavio Donze - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.search;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.ExtensionPointReader;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.core.ITaskList;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.ui.IWorkbenchWindow;
/**
 * Provides static utility methods to access the implementation that is contributed by the searchProvider extension
 * point.
 *
 * @author Flavio Donze
 * @author David Green
 */
public class SearchUtil {
    private static class NullSearchProvider extends org.eclipse.mylyn.internal.tasks.ui.search.AbstractSearchProvider {
        @java.lang.Override
        public void openSearchDialog(org.eclipse.ui.IWorkbenchWindow window) {
        }

        @java.lang.Override
        public void runSearchQuery(org.eclipse.mylyn.internal.tasks.core.ITaskList tasklist, org.eclipse.mylyn.tasks.core.TaskRepository repository, org.eclipse.mylyn.tasks.core.IRepositoryQuery query, boolean activateResultView) {
        }
    }

    /**
     * searchProvider extension point id
     */
    private static final java.lang.String EXTENSION_SEARCH_PROVIDER = "searchProvider";// $NON-NLS-1$


    private static final java.lang.String EXTENSION_SEARCH_HANDLER = "searchHandler";// $NON-NLS-1$


    private static org.eclipse.mylyn.internal.tasks.ui.search.AbstractSearchProvider provider;

    /**
     * Creates the search provider according to the defined extension point. Not synchronized since all invocations are
     * from UI thread.
     */
    private static final org.eclipse.mylyn.internal.tasks.ui.search.AbstractSearchProvider getSearchProvider() {
        if (org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil.provider != null) {
            return org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil.provider;
        }
        org.eclipse.mylyn.commons.core.ExtensionPointReader<org.eclipse.mylyn.internal.tasks.ui.search.AbstractSearchProvider> reader = new org.eclipse.mylyn.commons.core.ExtensionPointReader<org.eclipse.mylyn.internal.tasks.ui.search.AbstractSearchProvider>(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil.EXTENSION_SEARCH_PROVIDER, org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil.EXTENSION_SEARCH_PROVIDER, org.eclipse.mylyn.internal.tasks.ui.search.AbstractSearchProvider.class);
        reader.read();
        java.util.List<org.eclipse.mylyn.internal.tasks.ui.search.AbstractSearchProvider> providers = reader.getItems();
        if (providers.size() == 0) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.WARNING, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "No search provider was registered. Tasks search is not available."));// $NON-NLS-1$

        } else if (providers.size() > 1) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.WARNING, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "More than one search provider was registered."));// $NON-NLS-1$

        }
        org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil.provider = reader.getItem();
        if (org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil.provider == null) {
            org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil.provider = new org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil.NullSearchProvider();
        }
        return org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil.provider;
    }

    public static org.eclipse.mylyn.internal.tasks.ui.search.AbstractSearchHandler createSearchHandler() {
        org.eclipse.mylyn.commons.core.ExtensionPointReader<org.eclipse.mylyn.internal.tasks.ui.search.AbstractSearchHandler> reader = new org.eclipse.mylyn.commons.core.ExtensionPointReader<org.eclipse.mylyn.internal.tasks.ui.search.AbstractSearchHandler>(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil.EXTENSION_SEARCH_HANDLER, org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil.EXTENSION_SEARCH_HANDLER, org.eclipse.mylyn.internal.tasks.ui.search.AbstractSearchHandler.class);
        reader.read();
        if (reader.getItems().size() > 1) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.WARNING, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "More than one task list search handler was registered."));// $NON-NLS-1$

        }
        org.eclipse.mylyn.internal.tasks.ui.search.AbstractSearchHandler searchHandler = reader.getItem();
        if (searchHandler == null) {
            searchHandler = new org.eclipse.mylyn.internal.tasks.ui.search.DefaultSearchHandler();
        }
        return searchHandler;
    }

    public static boolean supportsTaskSearch() {
        return !(org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil.getSearchProvider() instanceof org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil.NullSearchProvider);
    }

    public static void openSearchDialog(org.eclipse.ui.IWorkbenchWindow window) {
        org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil.getSearchProvider().openSearchDialog(window);
    }

    public static void runSearchQuery(org.eclipse.mylyn.internal.tasks.core.ITaskList tasklist, org.eclipse.mylyn.tasks.core.TaskRepository repository, org.eclipse.mylyn.tasks.core.IRepositoryQuery repositoryQuery) {
        org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil.getSearchProvider().runSearchQuery(tasklist, repository, repositoryQuery, false);
    }

    public static void runSearchQuery(org.eclipse.mylyn.internal.tasks.core.ITaskList tasklist, org.eclipse.mylyn.tasks.core.TaskRepository repository, org.eclipse.mylyn.tasks.core.IRepositoryQuery repositoryQuery, boolean activateResultView) {
        org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil.getSearchProvider().runSearchQuery(tasklist, repository, repositoryQuery, activateResultView);
    }
}