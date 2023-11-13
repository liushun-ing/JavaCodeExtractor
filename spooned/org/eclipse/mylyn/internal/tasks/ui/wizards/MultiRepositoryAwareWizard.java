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
 *     Brock Janiczak - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.wizards;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
/**
 *
 * @author Mik Kersten
 * @author Brock Janiczak
 */
public class MultiRepositoryAwareWizard extends org.eclipse.jface.wizard.Wizard implements org.eclipse.ui.INewWizard {
    private final org.eclipse.mylyn.internal.tasks.ui.wizards.SelectRepositoryPage selectRepositoryPage;

    public MultiRepositoryAwareWizard(org.eclipse.mylyn.internal.tasks.ui.wizards.SelectRepositoryPage page, java.lang.String title) {
        selectRepositoryPage = page;
        setForcePreviousAndNextButtons(true);
        setNeedsProgressMonitor(true);
        setWindowTitle(title);
        setDefaultPageImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.BANNER_REPOSITORY);
    }

    public void init(org.eclipse.ui.IWorkbench workbench, org.eclipse.jface.viewers.IStructuredSelection selection) {
        // ignore
    }

    @java.lang.Override
    public void addPages() {
        addPage(selectRepositoryPage);
    }

    @java.lang.Override
    public boolean canFinish() {
        return selectRepositoryPage.canFinish();
    }

    @java.lang.Override
    public boolean performFinish() {
        return selectRepositoryPage.performFinish();
    }
}