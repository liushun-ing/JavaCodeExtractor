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
 *     Balazs Brinkus - bug 174473
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.actions;
import org.eclipse.core.commands.Command;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.wizards.NewRepositoryWizard;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.wizards.TaskRepositoryWizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
/**
 *
 * @author Mik Kersten
 * @author Steffen Pingel
 * @author David Green
 */
public class AddRepositoryAction extends org.eclipse.jface.action.Action {
    @java.lang.Deprecated
    private static final java.lang.String PREF_ADD_QUERY = "org.eclipse.mylyn.internal.tasks.add.query";// $NON-NLS-1$


    private static final java.lang.String ID = "org.eclipse.mylyn.tasklist.repositories.add";// $NON-NLS-1$


    public static final java.lang.String TITLE = Messages.AddRepositoryAction_Add_Task_Repository;

    private boolean promptToAddQuery = true;

    public AddRepositoryAction() {
        setImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.REPOSITORY_NEW);
        setText(org.eclipse.mylyn.internal.tasks.ui.actions.AddRepositoryAction.TITLE);
        setId(org.eclipse.mylyn.internal.tasks.ui.actions.AddRepositoryAction.ID);
        boolean enabled = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryManager().hasUserManagedRepositoryConnectors();
        if (!enabled) {
            // bug 279054 enable the action if connector discovery is present/enabled
            org.eclipse.core.commands.Command command = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getConfiguredDiscoveryWizardCommand();
            enabled = (command != null) && command.isEnabled();
        }
        setEnabled(enabled);
    }

    public boolean getPromptToAddQuery() {
        return promptToAddQuery;
    }

    public void setPromptToAddQuery(boolean promptToAddQuery) {
        this.promptToAddQuery = promptToAddQuery;
    }

    @java.lang.Override
    public void run() {
        showWizard();
    }

    public org.eclipse.mylyn.tasks.core.TaskRepository showWizard() {
        return showWizard(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell(), null);
    }

    public org.eclipse.mylyn.tasks.core.TaskRepository showWizard(org.eclipse.swt.widgets.Shell shell, java.lang.String connectorKind) {
        org.eclipse.mylyn.internal.tasks.ui.wizards.NewRepositoryWizard repositoryWizard = new org.eclipse.mylyn.internal.tasks.ui.wizards.NewRepositoryWizard(connectorKind);
        repositoryWizard.setShowNewQueryPromptOnFinish(getPromptToAddQuery());
        org.eclipse.jface.wizard.WizardDialog dialog = new org.eclipse.mylyn.tasks.ui.wizards.TaskRepositoryWizardDialog(shell, repositoryWizard);
        dialog.create();
        dialog.setBlockOnOpen(true);
        dialog.open();
        if (dialog.getReturnCode() == org.eclipse.jface.window.Window.OK) {
            return repositoryWizard.getTaskRepository();
        }
        return null;
    }

    @java.lang.Deprecated
    public void promptToAddQuery(org.eclipse.mylyn.tasks.core.TaskRepository taskRepository) {
        org.eclipse.jface.preference.IPreferenceStore preferenceStore = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore();
        if (!preferenceStore.getBoolean(org.eclipse.mylyn.internal.tasks.ui.actions.AddRepositoryAction.PREF_ADD_QUERY)) {
            org.eclipse.swt.widgets.Shell shell = org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().getActiveShell();
            org.eclipse.jface.dialogs.MessageDialogWithToggle messageDialog = org.eclipse.jface.dialogs.MessageDialogWithToggle.openYesNoQuestion(shell, Messages.AddRepositoryAction_Add_new_query, Messages.AddRepositoryAction_Add_a_query_to_the_Task_List, Messages.AddRepositoryAction_Do_not_show_again, false, preferenceStore, org.eclipse.mylyn.internal.tasks.ui.actions.AddRepositoryAction.PREF_ADD_QUERY);
            preferenceStore.setValue(org.eclipse.mylyn.internal.tasks.ui.actions.AddRepositoryAction.PREF_ADD_QUERY, messageDialog.getToggleState());
            if (messageDialog.getReturnCode() == org.eclipse.jface.dialogs.IDialogConstants.YES_ID) {
                org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnectorUi(taskRepository.getConnectorKind());
                org.eclipse.jface.wizard.IWizard queryWizard = connectorUi.getQueryWizard(taskRepository, null);
                if (queryWizard instanceof org.eclipse.jface.wizard.Wizard) {
                    ((org.eclipse.jface.wizard.Wizard) (queryWizard)).setForcePreviousAndNextButtons(true);
                }
                org.eclipse.jface.wizard.WizardDialog queryDialog = new org.eclipse.jface.wizard.WizardDialog(shell, queryWizard);
                queryDialog.create();
                queryDialog.setBlockOnOpen(true);
                queryDialog.open();
            }
        }
    }
}