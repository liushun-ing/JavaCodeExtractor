/**
 * *****************************************************************************
 * Copyright (c) 2004, 2015 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *     Tasktop Technologies - improvements
 *     BREDEX GmbH - fix for bug 295050
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.dialogs;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.plugin.AbstractUIPlugin;
/**
 *
 * @author Frank Becker
 * @author Steffen Pingel
 * @author Torsten Kalix
 */
public class TaskRepositoryCredentialsDialog extends org.eclipse.jface.dialogs.TitleAreaDialog {
    private static final java.lang.String DIALOG_TITLE = Messages.TaskRepositoryCredentialsDialog_Enter_Credentials;

    private static final java.lang.String IMAGE_FILE_KEYLOCK = "icons/wizban/secur_role_wiz.gif";// $NON-NLS-1$


    public static final int TASK_REPOSITORY_CHANGED = 1000;

    private static final java.lang.String MESSAGE = Messages.TaskRepositoryCredentialsDialog_Enter_repository_credentials;

    private static final java.lang.String TITLE = Messages.TaskRepositoryCredentialsDialog_Repository_Authentication;

    public static org.eclipse.mylyn.internal.tasks.ui.dialogs.TaskRepositoryCredentialsDialog createDialog(org.eclipse.swt.widgets.Shell shell) {
        return new org.eclipse.mylyn.internal.tasks.ui.dialogs.TaskRepositoryCredentialsDialog(shell);
    }

    private org.eclipse.swt.graphics.Image keyLockImage;

    private java.lang.String message;

    private java.lang.String password = "";// $NON-NLS-1$


    private boolean savePassword;

    private org.eclipse.mylyn.tasks.core.TaskRepository taskRepository;

    private java.lang.String username = "";// $NON-NLS-1$


    private org.eclipse.swt.widgets.Button certBrowseButton;

    private boolean isFileDialog;

    private TaskRepositoryCredentialsDialog(org.eclipse.swt.widgets.Shell parentShell) {
        super(parentShell);
    }

    @java.lang.Override
    public boolean close() {
        if (keyLockImage != null) {
            keyLockImage.dispose();
        }
        return super.close();
    }

    private void createLinkArea(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.widgets.Composite composite = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NONE);
        composite.setLayout(new org.eclipse.swt.layout.GridLayout(1, false));
        composite.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_HORIZONTAL));
        org.eclipse.swt.widgets.Link link = new org.eclipse.swt.widgets.Link(composite, org.eclipse.swt.SWT.WRAP);
        link.setText(Messages.TaskRepositoryCredentialsDialog_HTML_Open_Repository_Properties);
        link.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                close();
                int returnCode = org.eclipse.mylyn.tasks.ui.TasksUiUtil.openEditRepositoryWizard(taskRepository);
                if (returnCode == org.eclipse.jface.window.Window.OK) {
                    setReturnCode(org.eclipse.mylyn.internal.tasks.ui.dialogs.TaskRepositoryCredentialsDialog.TASK_REPOSITORY_CHANGED);
                } else {
                    setReturnCode(returnCode);
                }
            }
        });
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.CENTER).hint(convertHorizontalDLUsToPixels(org.eclipse.jface.dialogs.IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH), org.eclipse.swt.SWT.DEFAULT).grab(true, false).applyTo(link);
    }

    private void createCenterArea(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.widgets.Composite composite = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NONE);
        composite.setLayout(new org.eclipse.swt.layout.GridLayout(3, false));
        composite.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_HORIZONTAL));
        if (taskRepository != null) {
            org.eclipse.swt.widgets.Composite labelComposite = new org.eclipse.swt.widgets.Composite(composite, org.eclipse.swt.SWT.NONE);
            org.eclipse.swt.layout.GridLayout layout = new org.eclipse.swt.layout.GridLayout(3, false);
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            labelComposite.setLayout(layout);
            org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.CENTER).grab(true, false).span(3, 1).applyTo(labelComposite);
            org.eclipse.swt.widgets.Label label = new org.eclipse.swt.widgets.Label(labelComposite, org.eclipse.swt.SWT.NONE);
            label.setImage(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getBrandManager().getBrandingIcon(taskRepository));
            label = new org.eclipse.swt.widgets.Label(labelComposite, org.eclipse.swt.SWT.NONE);
            label.setText(Messages.TaskRepositoryCredentialsDialog_Task_Repository);
            label = new org.eclipse.swt.widgets.Label(labelComposite, org.eclipse.swt.SWT.NONE);
            label.setText(taskRepository.getRepositoryLabel());
        }
        if (isFileDialog) {
            new org.eclipse.swt.widgets.Label(composite, org.eclipse.swt.SWT.NONE).setText(Messages.TaskRepositoryCredentialsDialog_Filename);
        } else {
            new org.eclipse.swt.widgets.Label(composite, org.eclipse.swt.SWT.NONE).setText(Messages.TaskRepositoryCredentialsDialog_User_ID);
        }
        final org.eclipse.swt.widgets.Text usernameField = new org.eclipse.swt.widgets.Text(composite, org.eclipse.swt.SWT.BORDER);
        usernameField.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
            public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
                username = usernameField.getText();
            }
        });
        usernameField.setText(username);
        if (username.length() == 0) {
            usernameField.setFocus();
        }
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.CENTER).hint(convertHorizontalDLUsToPixels(org.eclipse.jface.dialogs.IDialogConstants.ENTRY_FIELD_WIDTH), org.eclipse.swt.SWT.DEFAULT).grab(true, false).applyTo(usernameField);
        if (isFileDialog) {
            certBrowseButton = new org.eclipse.swt.widgets.Button(composite, org.eclipse.swt.SWT.PUSH);
            certBrowseButton.setText(Messages.TaskRepositoryCredentialsDialog_ChooseCertificateFile);
            certBrowseButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                @java.lang.Override
                public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                    org.eclipse.swt.widgets.FileDialog fileDialog = new org.eclipse.swt.widgets.FileDialog(getShell(), org.eclipse.swt.SWT.OPEN);
                    fileDialog.setFilterPath(java.lang.System.getProperty("user.home", "."));// $NON-NLS-1$ //$NON-NLS-2$

                    java.lang.String returnFile = fileDialog.open();
                    if (returnFile != null) {
                        username = returnFile;
                        usernameField.setText(returnFile);
                    }
                }
            });
        } else {
            new org.eclipse.swt.widgets.Label(composite, org.eclipse.swt.SWT.NONE).setText(" ");// $NON-NLS-1$

        }
        new org.eclipse.swt.widgets.Label(composite, org.eclipse.swt.SWT.NONE).setText(Messages.TaskRepositoryCredentialsDialog_Password);
        final org.eclipse.swt.widgets.Text passwordField = new org.eclipse.swt.widgets.Text(composite, org.eclipse.swt.SWT.BORDER | org.eclipse.swt.SWT.PASSWORD);
        passwordField.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
            public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
                password = passwordField.getText();
            }
        });
        passwordField.setText(password);
        if (username.length() > 0) {
            passwordField.setFocus();
        }
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.CENTER).hint(convertHorizontalDLUsToPixels(org.eclipse.jface.dialogs.IDialogConstants.ENTRY_FIELD_WIDTH), org.eclipse.swt.SWT.DEFAULT).grab(true, false).applyTo(passwordField);
        final org.eclipse.swt.widgets.Button savePasswordButton = new org.eclipse.swt.widgets.Button(composite, org.eclipse.swt.SWT.CHECK);
        savePasswordButton.setText(Messages.TaskRepositoryCredentialsDialog_Save_Password);
        savePasswordButton.setSelection(savePassword);
        savePasswordButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                savePassword = savePasswordButton.getSelection();
            }
        });
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().span(3, 1).applyTo(savePasswordButton);
        createWarningMessage(composite);
    }

    @java.lang.Override
    protected org.eclipse.swt.widgets.Control createContents(org.eclipse.swt.widgets.Composite parent) {
        getShell().setText(org.eclipse.mylyn.internal.tasks.ui.dialogs.TaskRepositoryCredentialsDialog.DIALOG_TITLE);
        setTitle(org.eclipse.mylyn.internal.tasks.ui.dialogs.TaskRepositoryCredentialsDialog.TITLE);
        org.eclipse.swt.widgets.Control control = super.createContents(parent);
        if (taskRepository != null) {
            org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(taskRepository.getConnectorKind());
            if (connector != null) {
                setTitle((connector.getShortLabel() + " ") + org.eclipse.mylyn.internal.tasks.ui.dialogs.TaskRepositoryCredentialsDialog.TITLE);// $NON-NLS-1$

            }
        }
        org.eclipse.jface.resource.ImageDescriptor descriptor = org.eclipse.ui.plugin.AbstractUIPlugin.imageDescriptorFromPlugin(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.mylyn.internal.tasks.ui.dialogs.TaskRepositoryCredentialsDialog.IMAGE_FILE_KEYLOCK);
        if (descriptor != null) {
            keyLockImage = descriptor.createImage();
            setTitleImage(keyLockImage);
        }
        if (message != null) {
            super.setMessage(message);
        } else {
            super.setMessage(org.eclipse.mylyn.internal.tasks.ui.dialogs.TaskRepositoryCredentialsDialog.MESSAGE);
        }
        applyDialogFont(control);
        return control;
    }

    @java.lang.Override
    protected org.eclipse.swt.widgets.Control createDialogArea(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.widgets.Composite parent2 = ((org.eclipse.swt.widgets.Composite) (super.createDialogArea(parent)));
        org.eclipse.swt.widgets.Composite composite = new org.eclipse.swt.widgets.Composite(parent2, org.eclipse.swt.SWT.NONE);
        composite.setLayout(new org.eclipse.swt.layout.GridLayout(1, false));
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.BEGINNING).applyTo(composite);
        createCenterArea(composite);
        if (taskRepository != null) {
            createLinkArea(composite);
        }
        composite.pack();
        return parent;
    }

    private void createWarningMessage(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.widgets.Composite composite = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NONE);
        org.eclipse.swt.layout.GridLayout layout = new org.eclipse.swt.layout.GridLayout();
        layout.numColumns = 2;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.CENTER).grab(true, false).span(2, 1).applyTo(composite);
        org.eclipse.swt.widgets.Label label = new org.eclipse.swt.widgets.Label(composite, org.eclipse.swt.SWT.NONE);
        label.setImage(org.eclipse.jface.dialogs.Dialog.getImage(org.eclipse.mylyn.internal.tasks.ui.dialogs.DLG_IMG_MESSAGE_WARNING));
        label.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.VERTICAL_ALIGN_BEGINNING | org.eclipse.swt.layout.GridData.HORIZONTAL_ALIGN_BEGINNING));
        label = new org.eclipse.swt.widgets.Label(composite, org.eclipse.swt.SWT.WRAP);
        label.setText(Messages.TaskRepositoryCredentialsDialog_Saved_passwords_are_stored_that_is_difficult);
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.CENTER).hint(convertHorizontalDLUsToPixels(org.eclipse.jface.dialogs.IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH), org.eclipse.swt.SWT.DEFAULT).grab(true, false).applyTo(label);
    }

    @java.lang.Override
    public java.lang.String getMessage() {
        return message;
    }

    public java.lang.String getPassword() {
        return password;
    }

    public boolean getSavePassword() {
        return savePassword;
    }

    public org.eclipse.mylyn.tasks.core.TaskRepository getTaskRepository() {
        return taskRepository;
    }

    public java.lang.String getUserName() {
        return username;
    }

    @java.lang.Override
    public void setMessage(java.lang.String message) {
        this.message = message;
    }

    public void setPassword(java.lang.String password) {
        if (password == null) {
            throw new java.lang.IllegalArgumentException();
        }
        this.password = password;
    }

    public void setSavePassword(boolean savePassword) {
        this.savePassword = savePassword;
    }

    public void setTaskRepository(org.eclipse.mylyn.tasks.core.TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public void setUsername(java.lang.String username) {
        if (username == null) {
            throw new java.lang.IllegalArgumentException();
        }
        this.username = username;
    }

    /**
     * switch from asking for username / password to asking for certificate-file / password
     *
     * @param isFileDialog
     */
    public void setFileDialog(boolean isFileDialog) {
        this.isFileDialog = isFileDialog;
    }
}