/**
 * *****************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Eugene Kuleshov - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.wizards;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.mylyn.internal.tasks.core.ITaskRepositoryFilter;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
/**
 *
 * @author Mik Kersten
 * @author Eugene Kuleshov
 */
public class NewQueryWizard extends org.eclipse.mylyn.internal.tasks.ui.wizards.MultiRepositoryAwareWizard {
    public NewQueryWizard() {
        super(new org.eclipse.mylyn.internal.tasks.ui.wizards.NewQueryWizard.SelectRepositoryPageForNewQuery(), Messages.NewQueryWizard_New_Repository_Query);
    }

    private static final class SelectRepositoryPageForNewQuery extends org.eclipse.mylyn.internal.tasks.ui.wizards.SelectRepositoryPage {
        public SelectRepositoryPageForNewQuery() {
            super(org.eclipse.mylyn.internal.tasks.core.ITaskRepositoryFilter.CAN_QUERY);
        }

        @java.lang.Override
        protected org.eclipse.jface.wizard.IWizard createWizard(org.eclipse.mylyn.tasks.core.TaskRepository taskRepository) {
            org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi repositoryUi = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnectorUi(taskRepository.getConnectorKind());
            return repositoryUi.getQueryWizard(taskRepository, null);
        }
    }
}