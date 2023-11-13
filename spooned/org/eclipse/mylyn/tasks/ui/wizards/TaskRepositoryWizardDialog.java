/**
 * *****************************************************************************
 * Copyright (c) 2004, 2011 Helen Bershadskaya and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Helen Bershadskaya - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.tasks.ui.wizards;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.commons.ui.dialogs.EnhancedWizardDialog;
import org.eclipse.mylyn.internal.tasks.ui.wizards.Messages;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
/**
 * Wizard dialog for displaying repository settings page. Necessary so we can add a validate button in the button bar.
 *
 * @author Helen Bershadskaya
 * @since 3.1
 */
public class TaskRepositoryWizardDialog extends org.eclipse.mylyn.commons.ui.dialogs.EnhancedWizardDialog {
    private static final java.lang.String VALIDATE_BUTTON_KEY = "validate";// $NON-NLS-1$


    private org.eclipse.swt.widgets.Button validateServerButton;

    private static final int VALIDATE_BUTTON_ID = 2000;

    /**
     *
     * @see WizardDialog#WizardDialog(Shell, IWizard)
     * @since 3.1
     */
    public TaskRepositoryWizardDialog(org.eclipse.swt.widgets.Shell parentShell, org.eclipse.jface.wizard.IWizard newWizard) {
        super(parentShell, newWizard);
        setHelpAvailable(false);
    }

    /**
     * Overridden so we can add a validate button to the wizard button bar, if a repository settings page requires it.
     * Validate button is added left justified at button bar bottom (next to help image).
     *
     * @since 3.7
     */
    @java.lang.Override
    protected void createExtraButtons(org.eclipse.swt.widgets.Composite composite) {
        validateServerButton = createButton(composite, org.eclipse.mylyn.tasks.ui.wizards.TaskRepositoryWizardDialog.VALIDATE_BUTTON_ID, org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Validate_Settings, false);
        validateServerButton.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.REPOSITORY_VALIDATE));
        validateServerButton.setVisible(false);
        setButtonLayoutData(validateServerButton);
    }

    /**
     *
     * @since 3.7
     */
    @java.lang.Override
    public void updateExtraButtons() {
        if ((getCurrentPage() instanceof org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage) && ((org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage) (getCurrentPage())).needsValidation()) {
            if (!validateServerButton.isVisible()) {
                validateServerButton.setVisible(true);
            }
            validateServerButton.setEnabled(((org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage) (getCurrentPage())).canValidate());
        } else if ((validateServerButton != null) && validateServerButton.isVisible()) {
            validateServerButton.setVisible(false);
        }
    }

    /**
     * Overridden so we can react to the validate button being pressed. This could have been done with a straight
     * selection listener in the creation method above, but this is more consistent with how the other buttons work in
     * the wizard dialog.
     *
     * @since 3.7
     */
    @java.lang.Override
    protected boolean handleExtraButtonPressed(int buttonId) {
        if (buttonId == org.eclipse.mylyn.tasks.ui.wizards.TaskRepositoryWizardDialog.VALIDATE_BUTTON_ID) {
            if (getCurrentPage() instanceof org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage) {
                ((org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage) (getCurrentPage())).validateSettings();
                return true;
            }
        }
        return false;
    }

    /**
     * Overridden to be able to set proper state for our validate button
     */
    @java.lang.Override
    public void run(boolean fork, boolean cancelable, org.eclipse.jface.operation.IRunnableWithProgress runnable) throws java.lang.reflect.InvocationTargetException, java.lang.InterruptedException {
        java.util.HashMap<java.lang.String, java.lang.Boolean> savedEnabledState = null;
        try {
            savedEnabledState = saveAndSetEnabledStateMylyn();
            super.run(fork, cancelable, runnable);
        } finally {
            if (savedEnabledState != null) {
                restoreEnabledStateMylyn(savedEnabledState);
            }
        }
    }

    /**
     * Modeled after super.saveAndSetEnabledState(), but that one is private, so create our own
     *
     * @since 3.7
     */
    @java.lang.Override
    protected java.util.HashMap<java.lang.String, java.lang.Boolean> saveAndSetEnabledStateMylyn() {
        java.util.HashMap<java.lang.String, java.lang.Boolean> savedEnabledState = null;
        if (getShell() != null) {
            savedEnabledState = new java.util.HashMap<java.lang.String, java.lang.Boolean>();
            if ((validateServerButton != null) && (validateServerButton.getShell() == getShell())) {
                savedEnabledState.put(org.eclipse.mylyn.tasks.ui.wizards.TaskRepositoryWizardDialog.VALIDATE_BUTTON_KEY, validateServerButton.getEnabled());
                validateServerButton.setEnabled(false);
            }
        }
        return savedEnabledState;
    }

    /**
     * Modeled after super.restoreEnabledState() and super.restoreUIState() -- couldn't override those since they are
     * private, so create our own. Currently only single button to work with, so don't create two separate methods
     *
     * @since 3.7
     */
    @java.lang.Override
    protected void restoreEnabledStateMylyn(java.util.HashMap<java.lang.String, java.lang.Boolean> savedEnabledState) {
        if (savedEnabledState != null) {
            java.lang.Boolean savedValidateEnabledState = savedEnabledState.get(org.eclipse.mylyn.tasks.ui.wizards.TaskRepositoryWizardDialog.VALIDATE_BUTTON_KEY);
            if ((validateServerButton != null) && (savedValidateEnabledState != null)) {
                validateServerButton.setEnabled(savedValidateEnabledState);
            }
        }
    }
}