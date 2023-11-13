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
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector;
import org.eclipse.mylyn.internal.tasks.ui.wizards.LocalRepositorySettingsPage;
import org.eclipse.mylyn.internal.tasks.ui.wizards.NewLocalTaskWizard;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage;
/**
 *
 * @author Rob Elves
 * @author Mik Kersten
 */
public class LocalRepositoryConnectorUi extends org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi {
    @java.lang.Override
    public org.eclipse.jface.resource.ImageDescriptor getTaskKindOverlay(org.eclipse.mylyn.tasks.core.ITask task) {
        return super.getTaskKindOverlay(task);
    }

    @java.lang.Override
    public org.eclipse.jface.wizard.IWizard getNewTaskWizard(org.eclipse.mylyn.tasks.core.TaskRepository taskRepository, org.eclipse.mylyn.tasks.core.ITaskMapping selection) {
        return new org.eclipse.mylyn.internal.tasks.ui.wizards.NewLocalTaskWizard(selection);
    }

    @java.lang.Override
    public org.eclipse.jface.wizard.IWizard getQueryWizard(org.eclipse.mylyn.tasks.core.TaskRepository repository, org.eclipse.mylyn.tasks.core.IRepositoryQuery queryToEdit) {
        return null;
    }

    @java.lang.Override
    public java.lang.String getConnectorKind() {
        return org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector.CONNECTOR_KIND;
    }

    @java.lang.Override
    public org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage getSettingsPage(org.eclipse.mylyn.tasks.core.TaskRepository taskRepository) {
        return new org.eclipse.mylyn.internal.tasks.ui.wizards.LocalRepositorySettingsPage(taskRepository);
    }

    @java.lang.Override
    public boolean hasSearchPage() {
        return false;
    }
}