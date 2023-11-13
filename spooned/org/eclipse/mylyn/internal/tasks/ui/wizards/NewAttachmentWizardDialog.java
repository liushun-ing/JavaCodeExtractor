/**
 * *****************************************************************************
 * Copyright (c) 2004, 2008 Jeff Pound and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jeff Pound - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.wizards;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
/**
 *
 * @author Jeff Pound
 */
public class NewAttachmentWizardDialog extends org.eclipse.jface.wizard.WizardDialog {
    private static final java.lang.String ATTACHMENT_WIZARD_SETTINGS_SECTION = "PatchWizard";// $NON-NLS-1$


    public NewAttachmentWizardDialog(org.eclipse.swt.widgets.Shell parent, org.eclipse.jface.wizard.IWizard wizard, boolean modal) {
        super(parent, wizard);
        if (modal) {
            setShellStyle(getShellStyle() | org.eclipse.swt.SWT.RESIZE);
        } else {
            setShellStyle((((org.eclipse.swt.SWT.MODELESS | org.eclipse.swt.SWT.CLOSE) | org.eclipse.swt.SWT.TITLE) | org.eclipse.swt.SWT.BORDER) | org.eclipse.swt.SWT.RESIZE);
        }
        setMinimumPageSize(600, 300);
        setPageSize(600, 300);
        setBlockOnOpen(modal);
    }

    @java.lang.Override
    protected org.eclipse.jface.dialogs.IDialogSettings getDialogBoundsSettings() {
        org.eclipse.jface.dialogs.IDialogSettings settings = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getDialogSettings();
        org.eclipse.jface.dialogs.IDialogSettings section = settings.getSection(org.eclipse.mylyn.internal.tasks.ui.wizards.NewAttachmentWizardDialog.ATTACHMENT_WIZARD_SETTINGS_SECTION);
        if (section == null) {
            section = settings.addNewSection(org.eclipse.mylyn.internal.tasks.ui.wizards.NewAttachmentWizardDialog.ATTACHMENT_WIZARD_SETTINGS_SECTION);
        }
        return section;
    }
}