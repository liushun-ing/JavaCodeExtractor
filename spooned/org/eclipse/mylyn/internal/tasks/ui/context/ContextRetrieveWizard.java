/**
 * *****************************************************************************
 * Copyright (c) 2004, 2012 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.context;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskAttachment;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
/**
 *
 * @author Rob Elves
 * @author Mik Kersten
 * @author Steffen Pingel
 */
public class ContextRetrieveWizard extends org.eclipse.jface.wizard.Wizard {
    private final org.eclipse.mylyn.tasks.core.TaskRepository repository;

    private final org.eclipse.mylyn.tasks.core.ITask task;

    private org.eclipse.mylyn.internal.tasks.ui.context.ContextRetrieveWizardPage wizardPage;

    public ContextRetrieveWizard(org.eclipse.mylyn.tasks.core.ITask task) {
        this.task = task;
        this.repository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(task.getConnectorKind(), task.getRepositoryUrl());
        setWindowTitle(Messages.ContextRetrieveWizard_Retrieve_Context);
        setDefaultPageImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.BANNER_REPOSITORY_CONTEXT);
        setNeedsProgressMonitor(true);
    }

    @java.lang.Override
    public void addPages() {
        wizardPage = new org.eclipse.mylyn.internal.tasks.ui.context.ContextRetrieveWizardPage(repository, task);
        addPage(wizardPage);
        super.addPages();
    }

    @java.lang.Override
    public final boolean performFinish() {
        org.eclipse.mylyn.tasks.core.ITaskAttachment attachment = wizardPage.getSelectedContext();
        return org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.downloadContext(task, attachment, getContainer());
    }
}