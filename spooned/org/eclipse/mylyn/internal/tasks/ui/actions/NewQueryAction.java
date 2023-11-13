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
 *     Eugene Kuleshov - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.actions;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.internal.tasks.ui.LocalRepositoryConnectorUi;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.wizards.NewQueryWizard;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
/**
 *
 * @author Mik Kersten
 * @author Eugene Kuleshov
 */
public class NewQueryAction extends org.eclipse.jface.action.Action implements org.eclipse.ui.IViewActionDelegate , org.eclipse.core.runtime.IExecutableExtension {
    private final java.lang.String ID = "org.eclipse.mylyn.tasks.ui.new.query";// $NON-NLS-1$


    private final java.lang.String LABEL_NEW_QUERY = Messages.NewQueryAction_new_query_;

    private boolean skipRepositoryPage;

    public NewQueryAction() {
        setText(LABEL_NEW_QUERY);
        setToolTipText(LABEL_NEW_QUERY);
        setId(ID);
        setImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.QUERY_NEW);
    }

    public void run(org.eclipse.jface.action.IAction action) {
        run();
    }

    @java.lang.Override
    public void run() {
        org.eclipse.jface.wizard.IWizard wizard = null;
        /* Disabled for bug 275204 to make it more simple to discover ui for installing additional connectors
        List<TaskRepository> repositories = TasksUi.getRepositoryManager().getAllRepositories();
        if (repositories.size() == 2) {
        // NOTE: this click-saving should be generalized
        for (TaskRepository taskRepository : repositories) {
        AbstractRepositoryConnectorUi connectorUi = TasksUiPlugin.getConnectorUi(taskRepository.getConnectorKind());
        if (!(connectorUi instanceof LocalRepositoryConnectorUi)) {
        wizard = connectorUi.getQueryWizard(taskRepository, null);
        if (wizard == null) {
        continue;
        }
        ((Wizard) wizard).setForcePreviousAndNextButtons(true);
        }
        }
        } else
         */
        if (skipRepositoryPage) {
            org.eclipse.mylyn.tasks.core.TaskRepository taskRepository = org.eclipse.mylyn.tasks.ui.TasksUiUtil.getSelectedRepository();
            org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnectorUi(taskRepository.getConnectorKind());
            wizard = connectorUi.getQueryWizard(taskRepository, null);
            if (wizard instanceof org.eclipse.jface.wizard.Wizard) {
                ((org.eclipse.jface.wizard.Wizard) (wizard)).setForcePreviousAndNextButtons(true);
            }
            if (connectorUi instanceof org.eclipse.mylyn.internal.tasks.ui.LocalRepositoryConnectorUi) {
                wizard.performFinish();
                return;
            }
        } else {
            wizard = new org.eclipse.mylyn.internal.tasks.ui.wizards.NewQueryWizard();
        }
        org.eclipse.jface.wizard.WizardDialog dialog = new org.eclipse.jface.wizard.WizardDialog(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell(), wizard);
        dialog.create();
        dialog.setBlockOnOpen(true);
        dialog.open();
    }

    public void setInitializationData(org.eclipse.core.runtime.IConfigurationElement config, java.lang.String propertyName, java.lang.Object data) throws org.eclipse.core.runtime.CoreException {
        if ("skipFirstPage".equals(data)) {
            // $NON-NLS-1$
            this.skipRepositoryPage = true;
        }
    }

    public void init(org.eclipse.ui.IViewPart view) {
    }

    public void selectionChanged(org.eclipse.jface.action.IAction action, org.eclipse.jface.viewers.ISelection selection) {
    }
}