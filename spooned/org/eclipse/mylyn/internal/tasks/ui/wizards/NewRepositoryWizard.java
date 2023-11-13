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
 *     Peter Stibrany - fix for bug 247077
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.wizards;
import com.google.common.base.Strings;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.actions.AddRepositoryAction;
import org.eclipse.mylyn.internal.tasks.ui.actions.Messages;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
/**
 *
 * @author Mik Kersten
 * @author Steffen Pingel
 */
public class NewRepositoryWizard extends org.eclipse.jface.wizard.Wizard implements org.eclipse.ui.INewWizard {
    private static final java.lang.String PREF_ADD_QUERY = "org.eclipse.mylyn.internal.tasks.add.query";// $NON-NLS-1$


    private org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector;

    /**
     * If not null, indicates that the wizard will initially jump to a specific connector page
     */
    private final java.lang.String connectorKind;

    private org.eclipse.mylyn.tasks.core.TaskRepository taskRepository;

    private org.eclipse.mylyn.internal.tasks.ui.wizards.SelectRepositoryConnectorPage selectConnectorPage;

    private org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage settingsPage;

    private java.lang.String lastConnectorKind;

    private boolean showNewQueryPromptOnFinish;

    private java.lang.String brand;

    public NewRepositoryWizard() {
        this(null);
    }

    public NewRepositoryWizard(java.lang.String connectorKind) {
        this.connectorKind = connectorKind;
        setDefaultPageImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.BANNER_REPOSITORY);
        setForcePreviousAndNextButtons(connectorKind == null);
        setNeedsProgressMonitor(true);
        setWindowTitle(org.eclipse.mylyn.internal.tasks.ui.actions.AddRepositoryAction.TITLE);
        setShowNewQueryPromptOnFinish(true);
    }

    @java.lang.Override
    public void addPages() {
        if ((connectorKind != null) && org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(connectorKind).canCreateRepository()) {
            connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(connectorKind);
            updateSettingsPage();
            if (settingsPage != null) {
                addPage(settingsPage);
            }
        } else {
            selectConnectorPage = new org.eclipse.mylyn.internal.tasks.ui.wizards.SelectRepositoryConnectorPage();
            addPage(selectConnectorPage);
        }
    }

    @java.lang.Override
    public boolean canFinish() {
        return ((((selectConnectorPage == null) || selectConnectorPage.isPageComplete()) && (!(getContainer().getCurrentPage() == selectConnectorPage))) && (settingsPage != null)) && settingsPage.isPageComplete();
    }

    @java.lang.Override
    public org.eclipse.jface.wizard.IWizardPage getNextPage(org.eclipse.jface.wizard.IWizardPage page) {
        if (page == selectConnectorPage) {
            connector = selectConnectorPage.getConnector();
            brand = selectConnectorPage.getBrand();
            updateSettingsPage();
            return settingsPage;
        }
        return super.getNextPage(page);
    }

    public void init(org.eclipse.ui.IWorkbench workbench, org.eclipse.jface.viewers.IStructuredSelection selection) {
    }

    @java.lang.Override
    public boolean performFinish() {
        if (canFinish()) {
            taskRepository = new org.eclipse.mylyn.tasks.core.TaskRepository(connector.getConnectorKind(), settingsPage.getRepositoryUrl());
            boolean finishAccepted = settingsPage.preFinish(taskRepository);
            if (finishAccepted) {
                settingsPage.performFinish(taskRepository);
                org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().addRepository(taskRepository);
                if (showNewQueryPromptOnFinish()) {
                    if (connector.canQuery(taskRepository)) {
                        promptToAddQuery(taskRepository);
                    }
                }
                return true;
            }
        }
        return false;
    }

    public org.eclipse.mylyn.tasks.core.TaskRepository getTaskRepository() {
        return taskRepository;
    }

    void updateSettingsPage() {
        assert connector != null;
        if (!connector.getConnectorKind().equals(lastConnectorKind)) {
            org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnectorUi(connector.getConnectorKind());
            settingsPage = connectorUi.getSettingsPage(null);
            if (settingsPage == null) {
                org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayFrameworkError(// $NON-NLS-1$
                org.eclipse.osgi.util.NLS.bind("The connector implementation is incomplete: AbstractRepositoryConnectorUi.getSettingsPage() for connector ''{0}'' returned null. Please contact the vendor of the connector to resolve the problem.", connector.getConnectorKind()));
            }
            settingsPage.setWizard(this);
            lastConnectorKind = connector.getConnectorKind();
        }
        if (settingsPage instanceof org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage) {
            ((org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage) (settingsPage)).setBrand(com.google.common.base.Strings.nullToEmpty(brand));
        }
    }

    public void promptToAddQuery(org.eclipse.mylyn.tasks.core.TaskRepository taskRepository) {
        org.eclipse.jface.preference.IPreferenceStore preferenceStore = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore();
        if (!preferenceStore.getBoolean(org.eclipse.mylyn.internal.tasks.ui.wizards.NewRepositoryWizard.PREF_ADD_QUERY)) {
            org.eclipse.jface.dialogs.MessageDialogWithToggle messageDialog = org.eclipse.jface.dialogs.MessageDialogWithToggle.openYesNoQuestion(getShell(), org.eclipse.mylyn.internal.tasks.ui.actions.Messages.AddRepositoryAction_Add_new_query, org.eclipse.mylyn.internal.tasks.ui.actions.Messages.AddRepositoryAction_Add_a_query_to_the_Task_List, org.eclipse.mylyn.internal.tasks.ui.actions.Messages.AddRepositoryAction_Do_not_show_again, false, preferenceStore, org.eclipse.mylyn.internal.tasks.ui.wizards.NewRepositoryWizard.PREF_ADD_QUERY);
            preferenceStore.setValue(org.eclipse.mylyn.internal.tasks.ui.wizards.NewRepositoryWizard.PREF_ADD_QUERY, messageDialog.getToggleState());
            if (messageDialog.getReturnCode() == org.eclipse.jface.dialogs.IDialogConstants.YES_ID) {
                org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnectorUi(taskRepository.getConnectorKind());
                final org.eclipse.jface.wizard.IWizard queryWizard = connectorUi.getQueryWizard(taskRepository, null);
                if (queryWizard instanceof org.eclipse.jface.wizard.Wizard) {
                    ((org.eclipse.jface.wizard.Wizard) (queryWizard)).setForcePreviousAndNextButtons(true);
                }
                // execute delayed to avoid stacking dialogs
                getShell().getDisplay().asyncExec(new java.lang.Runnable() {
                    public void run() {
                        org.eclipse.jface.wizard.WizardDialog queryDialog = new org.eclipse.jface.wizard.WizardDialog(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell(), queryWizard);
                        queryDialog.create();
                        queryDialog.setBlockOnOpen(true);
                        queryDialog.open();
                    }
                });
            }
        }
    }

    public boolean showNewQueryPromptOnFinish() {
        return showNewQueryPromptOnFinish;
    }

    public void setShowNewQueryPromptOnFinish(boolean showNewQueryPromptOnFinish) {
        this.showNewQueryPromptOnFinish = showNewQueryPromptOnFinish;
    }

    @java.lang.Deprecated
    public java.lang.String getBrand() {
        return brand;
    }
}