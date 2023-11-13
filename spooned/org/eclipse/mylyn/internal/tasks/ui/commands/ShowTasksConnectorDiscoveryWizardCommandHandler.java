/**
 * *****************************************************************************
 * Copyright (c) 2009, 2011 Tasktop Technologies.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.commands;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.internal.discovery.ui.wizards.ConnectorDiscoveryWizard;
import org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
/**
 * A command that causes the {@link ConnectorDiscoveryWizard} to appear in a dialog.
 *
 * @author David Green
 * @author Steffen Pingel
 */
public class ShowTasksConnectorDiscoveryWizardCommandHandler extends org.eclipse.core.commands.AbstractHandler {
    private static final java.lang.String ID_P2_INSTALL_UI = "org.eclipse.equinox.p2.ui.sdk/org.eclipse.equinox.p2.ui.sdk.install";// $NON-NLS-1$


    public java.lang.Object execute(org.eclipse.core.commands.ExecutionEvent event) throws org.eclipse.core.commands.ExecutionException {
        // check to make sure that the p2 install ui is enabled
        if (org.eclipse.mylyn.commons.workbench.WorkbenchUtil.allowUseOf(org.eclipse.mylyn.internal.tasks.ui.commands.ShowTasksConnectorDiscoveryWizardCommandHandler.ID_P2_INSTALL_UI)) {
            org.eclipse.mylyn.internal.discovery.ui.wizards.ConnectorDiscoveryWizard wizard = new org.eclipse.mylyn.internal.discovery.ui.wizards.ConnectorDiscoveryWizard();
            org.eclipse.jface.wizard.WizardDialog dialog = new org.eclipse.jface.wizard.WizardDialog(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell(), wizard) {
                @java.lang.Override
                protected void createButtonsForButtonBar(org.eclipse.swt.widgets.Composite parent) {
                    super.createButtonsForButtonBar(parent);
                    ((org.eclipse.swt.layout.GridLayout) (parent.getLayout())).numColumns++;
                    final org.eclipse.swt.widgets.Button button = new org.eclipse.swt.widgets.Button(parent, org.eclipse.swt.SWT.CHECK);
                    button.setSelection(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.SERVICE_MESSAGES_ENABLED));
                    button.setText(Messages.ShowTasksConnectorDiscoveryWizardCommandHandler_Notify_when_updates_are_available_Text);
                    button.setFont(org.eclipse.jface.resource.JFaceResources.getDialogFont());
                    button.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                        @java.lang.Override
                        public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
                            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().setValue(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.SERVICE_MESSAGES_ENABLED, button.getSelection());
                        }
                    });
                    button.moveAbove(null);
                }
            };
            dialog.open();
        } else {
            org.eclipse.jface.dialogs.MessageDialog.openWarning(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell(), Messages.ShowTasksConnectorDiscoveryWizardCommandHandler_Install_Connectors, Messages.ShowTasksConnectorDiscoveryWizardCommandHandler_Unable_to_launch_connector_install);
        }
        return null;
    }
}