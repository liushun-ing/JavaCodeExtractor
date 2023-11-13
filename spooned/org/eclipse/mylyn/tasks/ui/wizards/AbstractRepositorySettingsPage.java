/**
 * *****************************************************************************
 * Copyright (c) 2004, 2016 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Frank Becker - improvements
 *     Helen Bershadskaya - improvements for bug 242445
 *     Atlassian - fixes for bug 316113
 * *****************************************************************************
 */
package org.eclipse.mylyn.tasks.ui.wizards;
import org.apache.commons.httpclient.URI;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.commons.ui.dialogs.IValidatable;
import org.eclipse.mylyn.commons.ui.dialogs.ValidatableWizardDialog;
import org.eclipse.mylyn.commons.workbench.browser.BrowserUtil;
import org.eclipse.mylyn.commons.workbench.forms.SectionComposite;
import org.eclipse.mylyn.internal.commons.repositories.ui.RepositoryUiUtil;
import org.eclipse.mylyn.internal.tasks.core.IRepositoryConstants;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.internal.tasks.core.RepositoryTemplateManager;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.internal.tasks.ui.IBrandManager;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.wizards.Messages;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.RepositoryInfo;
import org.eclipse.mylyn.tasks.core.RepositoryTemplate;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.statushandlers.StatusManager;
/**
 * Extend to provide custom repository settings. This page is typically invoked by the user requesting properties via
 * the Task Repositories view.
 *
 * @author Mik Kersten
 * @author Rob Elves
 * @author Steffen Pingel
 * @author Frank Becker
 * @author David Green
 * @author Helen Bershadskaya
 * @author Benjamin Muskalla
 * @since 2.0
 */
public abstract class AbstractRepositorySettingsPage extends org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPage implements org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage , org.eclipse.core.runtime.IAdaptable {
    protected static final java.lang.String PREFS_PAGE_ID_NET_PROXY = "org.eclipse.ui.net.NetPreferences";// $NON-NLS-1$


    protected static final java.lang.String LABEL_REPOSITORY_LABEL = org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Label_;

    protected static final java.lang.String LABEL_SERVER = org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Server_;

    protected static final java.lang.String LABEL_USER = org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_User_ID_;

    protected static final java.lang.String LABEL_PASSWORD = org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Password_;

    protected static final java.lang.String URL_PREFIX_HTTPS = "https://";// $NON-NLS-1$


    protected static final java.lang.String URL_PREFIX_HTTP = "http://";// $NON-NLS-1$


    protected static final java.lang.String INVALID_REPOSITORY_URL = org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Repository_url_is_invalid;

    protected static final java.lang.String INVALID_LOGIN = org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Unable_to_authenticate_with_repository;

    protected org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector;

    private final org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi;

    protected org.eclipse.jface.preference.StringFieldEditor repositoryLabelEditor;

    protected org.eclipse.swt.widgets.Combo serverUrlCombo;

    private boolean serverUrlReadOnly = false;

    private java.lang.String serverVersion = org.eclipse.mylyn.tasks.core.TaskRepository.NO_VERSION_SPECIFIED;

    protected org.eclipse.jface.preference.StringFieldEditor repositoryUserNameEditor;

    protected org.eclipse.jface.preference.StringFieldEditor repositoryPasswordEditor;

    protected org.eclipse.jface.preference.StringFieldEditor httpAuthUserNameEditor;

    protected org.eclipse.jface.preference.StringFieldEditor httpAuthPasswordEditor;

    private org.eclipse.jface.preference.StringFieldEditor certAuthFileNameEditor;

    private org.eclipse.swt.widgets.Button certBrowseButton;

    private org.eclipse.jface.preference.StringFieldEditor certAuthPasswordEditor;

    protected org.eclipse.jface.preference.StringFieldEditor proxyHostnameEditor;

    protected org.eclipse.jface.preference.StringFieldEditor proxyPortEditor;

    protected org.eclipse.jface.preference.StringFieldEditor proxyUserNameEditor;

    protected org.eclipse.jface.preference.StringFieldEditor proxyPasswordEditor;

    // FIXME shadows declaration in super
    protected org.eclipse.mylyn.tasks.core.TaskRepository repository;

    private org.eclipse.swt.widgets.Combo otherEncodingCombo;

    private org.eclipse.swt.widgets.Button defaultEncoding;

    // private Combo timeZonesCombo;
    protected org.eclipse.swt.widgets.Button anonymousButton;

    private java.lang.String oldUsername;

    private java.lang.String oldPassword;

    private java.lang.String oldHttpAuthUserId;

    private java.lang.String oldHttpAuthPassword;

    private java.lang.String oldCertAuthFileName;

    private java.lang.String oldCertAuthPassword;

    private boolean needsAnonymousLogin;

    private boolean needsTimeZone;

    private boolean needsEncoding;

    private boolean needsCertAuth;

    private boolean needsHttpAuth;

    private boolean needsValidation;

    private boolean needsValidateOnFinish;

    private boolean needsAdvanced;

    private boolean needsRepositoryCredentials;

    protected org.eclipse.swt.widgets.Composite compositeContainer;

    private org.eclipse.swt.widgets.Composite advancedComp;

    private org.eclipse.swt.widgets.Composite certAuthComp;

    private org.eclipse.swt.widgets.Composite httpAuthComp;

    private org.eclipse.swt.widgets.Composite proxyAuthComp;

    private java.util.Set<java.lang.String> repositoryUrls;

    private java.lang.String originalUrl;

    private org.eclipse.swt.widgets.Button otherEncoding;

    private org.eclipse.swt.widgets.Button certAuthButton;

    private org.eclipse.swt.widgets.Button httpAuthButton;

    private boolean needsProxy;

    /**
     *
     * @since 3.11
     */
    protected org.eclipse.swt.widgets.Button systemProxyButton;

    /**
     *
     * @since 3.11
     */
    protected org.eclipse.ui.forms.widgets.Hyperlink changeProxySettingsLink;

    private java.lang.String oldProxyUsername = "";// $NON-NLS-1$


    private java.lang.String oldProxyPassword = "";// $NON-NLS-1$


    // private Button proxyAuthButton;
    private java.lang.String oldProxyHostname = "";// $NON-NLS-1$


    private java.lang.String oldProxyPort = "";// $NON-NLS-1$


    private org.eclipse.swt.widgets.Button proxyAuthButton;

    private org.eclipse.ui.forms.widgets.Hyperlink createAccountHyperlink;

    private org.eclipse.ui.forms.widgets.Hyperlink manageAccountHyperlink;

    /**
     *
     * @since 3.1
     */
    protected org.eclipse.swt.widgets.Button savePasswordButton;

    private org.eclipse.swt.widgets.Button saveHttpPasswordButton;

    private org.eclipse.swt.widgets.Button saveCertPasswordButton;

    private org.eclipse.swt.widgets.Button saveProxyPasswordButton;

    private org.eclipse.swt.widgets.Button disconnectedButton;

    private org.eclipse.swt.widgets.Button validateOnFinishButton;

    private boolean isValid;

    /**
     *
     * @since 3.9
     */
    protected org.eclipse.mylyn.commons.workbench.forms.SectionComposite innerComposite;

    private org.eclipse.mylyn.tasks.core.TaskRepository validatedTaskRepository;

    private final java.util.Map<org.eclipse.mylyn.commons.net.AuthenticationType, org.eclipse.mylyn.commons.net.AuthenticationCredentials> validatedAuthenticationCredentials = new java.util.HashMap<org.eclipse.mylyn.commons.net.AuthenticationType, org.eclipse.mylyn.commons.net.AuthenticationCredentials>();

    private java.lang.String brand;

    /**
     *
     * @since 3.10
     */
    public AbstractRepositorySettingsPage(java.lang.String title, java.lang.String description, org.eclipse.mylyn.tasks.core.TaskRepository taskRepository, org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector) {
        this(title, description, taskRepository, connector, null);
    }

    /**
     *
     * @since 3.14
     */
    public AbstractRepositorySettingsPage(java.lang.String title, java.lang.String description, org.eclipse.mylyn.tasks.core.TaskRepository taskRepository, org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector, org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi) {
        super(title, description, taskRepository);
        repository = taskRepository;
        if (connector == null) {
            connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(getConnectorKind());
        }
        this.connector = connector;
        if (connectorUi == null) {
            connectorUi = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryConnectorUi(getConnectorKind());
        }
        this.connectorUi = connectorUi;
        if ((repository != null) && (!repository.getConnectorKind().equals(getConnectorKind()))) {
            throw new java.lang.IllegalArgumentException(((("connectorKind of repository does not match connectorKind of page, expected '" + getConnectorKind())// $NON-NLS-1$
             + "', got '") + repository.getConnectorKind()) + "'");// $NON-NLS-1$ //$NON-NLS-2$

        }
        updateBrandFromRepository();
        setNeedsRepositoryCredentials(true);
        setNeedsAnonymousLogin(false);
        setNeedsEncoding(true);
        setNeedsTimeZone(true);
        setNeedsProxy(true);
        setNeedsValidation(true);
        setNeedsAdvanced(true);
        setNeedsValidateOnFinish(false);
    }

    /**
     *
     * @since 3.0
     */
    public AbstractRepositorySettingsPage(java.lang.String title, java.lang.String description, org.eclipse.mylyn.tasks.core.TaskRepository taskRepository) {
        this(title, description, taskRepository, null);
    }

    /**
     *
     * @since 3.0
     */
    @java.lang.Override
    public abstract java.lang.String getConnectorKind();

    @java.lang.Override
    public void dispose() {
        super.dispose();
    }

    // /**
    // * @since 2.0
    // */
    // @Override
    // protected Control createContents(Composite parent) {
    // compositeContainer = new Composite(parent, SWT.NONE);
    // GridLayout layout = new GridLayout(3, false);
    // compositeContainer.setLayout(layout);
    // 
    // createSettingControls(parent);
    // }
    @java.lang.Override
    public void createControl(org.eclipse.swt.widgets.Composite parent) {
        initializeDialogUnits(parent);
        toolkit = new org.eclipse.ui.forms.widgets.FormToolkit(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getFormColors(parent.getDisplay()));
        innerComposite = new org.eclipse.mylyn.commons.workbench.forms.SectionComposite(parent, org.eclipse.swt.SWT.V_SCROLL | org.eclipse.swt.SWT.H_SCROLL);
        createSettingControls(innerComposite.getContent());
        createValidationControls(innerComposite.getContent());
        if (needsValidateOnFinish()) {
            validateOnFinishButton = new org.eclipse.swt.widgets.Button(innerComposite.getContent(), org.eclipse.swt.SWT.CHECK);
            validateOnFinishButton.setText(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Validate_on_Finish);
            validateOnFinishButton.setSelection(true);
        }
        org.eclipse.swt.graphics.Point p = innerComposite.getContent().computeSize(org.eclipse.swt.SWT.DEFAULT, org.eclipse.swt.SWT.DEFAULT, true);
        innerComposite.setMinSize(p);
        innerComposite.getShell().layout();
        if (needsRepositoryCredentials()) {
            swapUserNameWithAnonymousInTabList();
        }
        org.eclipse.jface.dialogs.Dialog.applyDialogFont(innerComposite);
        setControl(innerComposite);
    }

    private void swapUserNameWithAnonymousInTabList() {
        if (((compositeContainer != null) && (anonymousButton != null)) && (repositoryUserNameEditor != null)) {
            java.util.List<org.eclipse.swt.widgets.Control> tabList = java.util.Arrays.asList(compositeContainer.getTabList());
            if (tabList.contains(repositoryUserNameEditor.getTextControl(compositeContainer)) && tabList.contains(anonymousButton)) {
                int userNameIndex = tabList.indexOf(repositoryUserNameEditor.getTextControl(compositeContainer));
                int anonymousIndex = tabList.indexOf(anonymousButton);
                java.util.Collections.swap(tabList, userNameIndex, anonymousIndex);
                compositeContainer.setTabList(tabList.toArray(new org.eclipse.swt.widgets.Control[tabList.size()]));
            }
        }
    }

    /**
     *
     * @since 3.5
     */
    protected void createValidationControls(org.eclipse.swt.widgets.Composite parent) {
        if (((!needsValidation()) || (getContainer() instanceof org.eclipse.mylyn.commons.ui.dialogs.ValidatableWizardDialog)) || (getContainer() instanceof org.eclipse.mylyn.tasks.ui.wizards.TaskRepositoryWizardDialog)) {
            return;
        }
        org.eclipse.swt.widgets.Composite composite = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NONE);
        org.eclipse.swt.layout.GridLayout layout = new org.eclipse.swt.layout.GridLayout(1, false);
        layout.marginHeight = 10;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 10;
        composite.setLayout(layout);
        composite.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.LEFT, org.eclipse.swt.SWT.TOP, false, false, 3, 1));
        // show validation control on page since it's not provided by container
        org.eclipse.swt.widgets.Button validateButton = new org.eclipse.swt.widgets.Button(composite, org.eclipse.swt.SWT.PUSH);
        validateButton.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.VALIDATE));
        validateButton.setText(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Validate_Settings_Button_Label);
        validateButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                validateSettings();
            }
        });
    }

    /**
     *
     * @since 2.0
     */
    @java.lang.Override
    protected void createSettingControls(org.eclipse.swt.widgets.Composite parent) {
        compositeContainer = parent;
        initializeOldValues();
        org.eclipse.swt.widgets.Label serverLabel = new org.eclipse.swt.widgets.Label(compositeContainer, org.eclipse.swt.SWT.NONE);
        serverLabel.setText(org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage.LABEL_SERVER);
        serverUrlCombo = new org.eclipse.swt.widgets.Combo(compositeContainer, org.eclipse.swt.SWT.DROP_DOWN);
        if (serverUrlReadOnly) {
            serverUrlCombo.setEnabled(false);
        } else {
            serverUrlCombo.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
                public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
                    if (getWizard() != null) {
                        getWizard().getContainer().updateButtons();
                    }
                }
            });
            serverUrlCombo.addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
                @java.lang.Override
                public void focusLost(org.eclipse.swt.events.FocusEvent e) {
                    updateHyperlinks();
                }
            });
            serverUrlCombo.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                @java.lang.Override
                public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                    if (getWizard() != null) {
                        getWizard().getContainer().updateButtons();
                    }
                }
            });
        }
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().hint(300, org.eclipse.swt.SWT.DEFAULT).grab(true, false).span(2, org.eclipse.swt.SWT.DEFAULT).applyTo(serverUrlCombo);
        repositoryLabelEditor = // $NON-NLS-1$
        new org.eclipse.jface.preference.StringFieldEditor("", org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage.LABEL_REPOSITORY_LABEL, org.eclipse.jface.preference.StringFieldEditor.UNLIMITED, compositeContainer) {
            @java.lang.Override
            protected boolean doCheckState() {
                return true;
                // return isValidUrl(getStringValue());
            }

            @java.lang.Override
            protected void valueChanged() {
                super.valueChanged();
                if (getWizard() != null) {
                    getWizard().getContainer().updateButtons();
                }
            }

            @java.lang.Override
            public int getNumberOfControls() {
                return 2;
            }
        };
        disconnectedButton = new org.eclipse.swt.widgets.Button(compositeContainer, org.eclipse.swt.SWT.CHECK);
        disconnectedButton.setText(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Disconnected);
        disconnectedButton.setSelection(repository != null ? repository.isOffline() : false);
        disconnectedButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                super.widgetSelected(e);
                isPageComplete();
                if (getWizard() != null) {
                    getWizard().getContainer().updateButtons();
                }
            }
        });
        if (needsRepositoryCredentials()) {
            createRepositoryCredentialsSection();
        }
        if (needsAdvanced() || needsEncoding()) {
            createAdvancedSection();
        }
        if (needsCertAuth()) {
            createCertAuthSection();
        }
        if (needsHttpAuth()) {
            createHttpAuthSection();
        }
        if (needsProxy()) {
            createProxySection();
        }
        createContributionControls(innerComposite);
        org.eclipse.swt.widgets.Composite managementComposite = new org.eclipse.swt.widgets.Composite(compositeContainer, org.eclipse.swt.SWT.NULL);
        org.eclipse.swt.layout.GridLayout managementLayout = new org.eclipse.swt.layout.GridLayout(4, false);
        managementLayout.marginHeight = 0;
        managementLayout.marginWidth = 0;
        managementLayout.horizontalSpacing = 10;
        managementComposite.setLayout(managementLayout);
        managementComposite.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.LEFT, org.eclipse.swt.SWT.TOP, false, false, 2, 1));
        createAccountHyperlink = toolkit.createHyperlink(managementComposite, org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Create_new_account, org.eclipse.swt.SWT.NONE);
        createAccountHyperlink.setBackground(managementComposite.getBackground());
        createAccountHyperlink.addHyperlinkListener(new org.eclipse.ui.forms.events.HyperlinkAdapter() {
            @java.lang.Override
            public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                // TaskRepository repository = getRepository();
                org.eclipse.mylyn.tasks.core.TaskRepository repository = createTaskRepository();
                // if (repository == null && getServerUrl() != null && getServerUrl().length() > 0) {
                // repository = createTaskRepository();
                // }
                if (repository != null) {
                    java.lang.String accountCreationUrl = connectorUi.getAccountCreationUrl(repository);
                    if (accountCreationUrl != null) {
                        org.eclipse.mylyn.commons.workbench.browser.BrowserUtil.openUrl(accountCreationUrl, org.eclipse.ui.browser.IWorkbenchBrowserSupport.AS_EXTERNAL);
                    }
                }
            }
        });
        manageAccountHyperlink = toolkit.createHyperlink(managementComposite, org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Change_account_settings, org.eclipse.swt.SWT.NONE);
        manageAccountHyperlink.setBackground(managementComposite.getBackground());
        manageAccountHyperlink.addHyperlinkListener(new org.eclipse.ui.forms.events.HyperlinkAdapter() {
            @java.lang.Override
            public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                org.eclipse.mylyn.tasks.core.TaskRepository repository = getRepository();
                if (((repository == null) && (getRepositoryUrl() != null)) && (getRepositoryUrl().length() > 0)) {
                    repository = createTaskRepository();
                }
                if (repository != null) {
                    java.lang.String accountManagementUrl = connectorUi.getAccountManagementUrl(repository);
                    if (accountManagementUrl != null) {
                        org.eclipse.mylyn.commons.workbench.browser.BrowserUtil.openUrl(accountManagementUrl, org.eclipse.ui.browser.IWorkbenchBrowserSupport.AS_EXTERNAL);
                    }
                }
            }
        });
        if (repositoryPasswordEditor != null) {
            // bug 131656: must set echo char after setting value on Mac
            ((org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage.RepositoryStringFieldEditor) (repositoryPasswordEditor)).getTextControl().setEchoChar('*');
        }
        if (needsRepositoryCredentials()) {
            if (needsAnonymousLogin()) {
                // do this after username and password widgets have been intialized
                if (repository != null) {
                    setAnonymous(isAnonymousAccess());
                }
            }
        }
        updateHyperlinks();
        if (repository != null) {
            updateLabel();
            updateUrl();
            saveToValidatedProperties(createTaskRepository());
        }
        org.eclipse.swt.layout.GridLayout layout = new org.eclipse.swt.layout.GridLayout(3, false);
        compositeContainer.setLayout(layout);
    }

    private void createRepositoryCredentialsSection() {
        repositoryUserNameEditor = // $NON-NLS-1$
        new org.eclipse.jface.preference.StringFieldEditor("", org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage.LABEL_USER, org.eclipse.jface.preference.StringFieldEditor.UNLIMITED, compositeContainer) {
            @java.lang.Override
            protected boolean doCheckState() {
                return true;
            }

            @java.lang.Override
            protected void valueChanged() {
                super.valueChanged();
                isPageComplete();
                if (getWizard() != null) {
                    getWizard().getContainer().updateButtons();
                }
            }

            @java.lang.Override
            public int getNumberOfControls() {
                // always 2 columns -- if no anonymous checkbox, just leave 3rd column empty
                return 2;
            }
        };
        if (needsAnonymousLogin()) {
            // need to increase column number here, because above string editor will use them if declared beforehand
            // ((GridLayout) (compositeContainer.getLayout())).numColumns++;
            anonymousButton = new org.eclipse.swt.widgets.Button(compositeContainer, org.eclipse.swt.SWT.CHECK);
            anonymousButton.setText(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Anonymous_Access);
            anonymousButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                @java.lang.Override
                public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                    setAnonymous(anonymousButton.getSelection());
                    isPageComplete();
                }
            });
        } else {
            org.eclipse.swt.widgets.Label dummyLabel = new org.eclipse.swt.widgets.Label(compositeContainer, org.eclipse.swt.SWT.NONE);// dummy control to fill 3rd column when no anonymous login

            org.eclipse.jface.layout.GridDataFactory.fillDefaults().applyTo(dummyLabel);// not really necessary, but to be on the safe side

        }
        repositoryPasswordEditor = // $NON-NLS-1$
        new org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage.RepositoryStringFieldEditor("", org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage.LABEL_PASSWORD, org.eclipse.jface.preference.StringFieldEditor.UNLIMITED, compositeContainer) {
            @java.lang.Override
            protected boolean doCheckState() {
                return true;
            }

            @java.lang.Override
            protected void valueChanged() {
                super.valueChanged();
                isPageComplete();
                if (getWizard() != null) {
                    getWizard().getContainer().updateButtons();
                }
            }

            @java.lang.Override
            public int getNumberOfControls() {
                return 2;
            }
        };
        savePasswordButton = new org.eclipse.swt.widgets.Button(compositeContainer, org.eclipse.swt.SWT.CHECK);
        savePasswordButton.setText(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Save_Password);
        savePasswordButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                super.widgetSelected(e);
                isPageComplete();
                if (getWizard() != null) {
                    getWizard().getContainer().updateButtons();
                }
            }
        });
        if (repository != null) {
            try {
                org.eclipse.mylyn.commons.net.AuthenticationCredentials credentials = repository.getCredentials(org.eclipse.mylyn.commons.net.AuthenticationType.REPOSITORY);
                if (credentials != null) {
                    repositoryUserNameEditor.setStringValue(credentials.getUserName());
                    repositoryPasswordEditor.setStringValue(credentials.getPassword());
                } else {
                    repositoryUserNameEditor.setStringValue("");// $NON-NLS-1$

                    repositoryPasswordEditor.setStringValue("");// $NON-NLS-1$

                }
            } catch (java.lang.Throwable t) {
                org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Could not set field value", t));// $NON-NLS-1$

            }
        }
        if (needsAnonymousLogin()) {
            if (repository != null) {
                setAnonymous(repository.getCredentials(org.eclipse.mylyn.commons.net.AuthenticationType.REPOSITORY) == null);
            } else {
                setAnonymous(true);
            }
        }
        if (repository != null) {
            savePasswordButton.setSelection(repository.getSavePassword(org.eclipse.mylyn.commons.net.AuthenticationType.REPOSITORY));
        } else {
            savePasswordButton.setSelection(false);
        }
        org.eclipse.mylyn.internal.commons.repositories.ui.RepositoryUiUtil.testCredentialsStore(getRepositoryUrl(), this);
    }

    private void updateLabel() {
        java.lang.String repositoryLabel = repository.getProperty(org.eclipse.mylyn.internal.tasks.core.IRepositoryConstants.PROPERTY_LABEL);
        if ((repositoryLabel != null) && (repositoryLabel.length() > 0)) {
            repositoryLabelEditor.setStringValue(repositoryLabel);
        }
    }

    private void updateUrl() {
        setUrl(repository.getRepositoryUrl());
    }

    private void createAdvancedSection() {
        org.eclipse.ui.forms.widgets.ExpandableComposite section = createSection(innerComposite, org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Additional_Settings);
        advancedComp = toolkit.createComposite(section, org.eclipse.swt.SWT.NONE);
        org.eclipse.swt.layout.GridLayout gridLayout2 = new org.eclipse.swt.layout.GridLayout();
        gridLayout2.numColumns = 2;
        gridLayout2.verticalSpacing = 5;
        gridLayout2.marginWidth = 0;
        advancedComp.setLayout(gridLayout2);
        advancedComp.setBackground(compositeContainer.getBackground());
        section.setClient(advancedComp);
        createAdditionalControls(advancedComp);
        if (needsEncoding()) {
            org.eclipse.swt.widgets.Label encodingLabel = new org.eclipse.swt.widgets.Label(advancedComp, org.eclipse.swt.SWT.HORIZONTAL);
            encodingLabel.setText(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Character_encoding);
            org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.BEGINNING, org.eclipse.swt.SWT.TOP).applyTo(encodingLabel);
            org.eclipse.swt.widgets.Composite encodingContainer = new org.eclipse.swt.widgets.Composite(advancedComp, org.eclipse.swt.SWT.NONE);
            org.eclipse.swt.layout.GridLayout gridLayout = new org.eclipse.swt.layout.GridLayout(2, false);
            gridLayout.marginWidth = 0;
            gridLayout.marginHeight = 0;
            encodingContainer.setLayout(gridLayout);
            defaultEncoding = new org.eclipse.swt.widgets.Button(encodingContainer, org.eclipse.swt.SWT.RADIO);
            defaultEncoding.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.LEFT, org.eclipse.swt.SWT.CENTER, false, false, 2, 1));
            defaultEncoding.setText((org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Default__ + org.eclipse.mylyn.tasks.core.TaskRepository.DEFAULT_CHARACTER_ENCODING) + ")");// $NON-NLS-1$

            defaultEncoding.setSelection(true);
            otherEncoding = new org.eclipse.swt.widgets.Button(encodingContainer, org.eclipse.swt.SWT.RADIO);
            otherEncoding.setText(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Other);
            otherEncodingCombo = new org.eclipse.swt.widgets.Combo(encodingContainer, org.eclipse.swt.SWT.READ_ONLY);
            try {
                for (java.lang.String encoding : java.nio.charset.Charset.availableCharsets().keySet()) {
                    if (!encoding.equals(org.eclipse.mylyn.tasks.core.TaskRepository.DEFAULT_CHARACTER_ENCODING)) {
                        otherEncodingCombo.add(encoding);
                    }
                }
            } catch (java.lang.LinkageError e) {
                org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Problems_encountered_determining_available_charsets, e));
                // bug 237972: 3rd party encodings can cause availableCharsets() to fail
                otherEncoding.setEnabled(false);
                otherEncodingCombo.setEnabled(false);
            }
            setDefaultEncoding();
            otherEncoding.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                @java.lang.Override
                public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                    if (otherEncoding.getSelection()) {
                        defaultEncoding.setSelection(false);
                        otherEncodingCombo.setEnabled(true);
                    } else {
                        defaultEncoding.setSelection(true);
                        otherEncodingCombo.setEnabled(false);
                    }
                }
            });
            if (repository != null) {
                try {
                    java.lang.String repositoryEncoding = repository.getCharacterEncoding();
                    if (repositoryEncoding != null) {
                        // &&
                        // !repositoryEncoding.equals(defaultEncoding))
                        // {
                        if ((otherEncodingCombo.getItemCount() > 0) && (otherEncodingCombo.indexOf(repositoryEncoding) > (-1))) {
                            otherEncodingCombo.setEnabled(true);
                            otherEncoding.setSelection(true);
                            defaultEncoding.setSelection(false);
                            otherEncodingCombo.select(otherEncodingCombo.indexOf(repositoryEncoding));
                        } else {
                            setDefaultEncoding();
                        }
                    }
                } catch (java.lang.Throwable t) {
                    org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Could not set field value", t));// $NON-NLS-1$

                }
            }
        }
    }

    private void createCertAuthSection() {
        org.eclipse.ui.forms.widgets.ExpandableComposite section = createSection(innerComposite, org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_certificate_settings);
        certAuthComp = toolkit.createComposite(section, org.eclipse.swt.SWT.NONE);
        certAuthComp.setBackground(compositeContainer.getBackground());
        section.setClient(certAuthComp);
        certAuthButton = new org.eclipse.swt.widgets.Button(certAuthComp, org.eclipse.swt.SWT.CHECK);
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().indent(0, 5).align(org.eclipse.swt.SWT.LEFT, org.eclipse.swt.SWT.TOP).span(3, org.eclipse.swt.SWT.DEFAULT).applyTo(certAuthButton);
        certAuthButton.setText(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Enable_certificate_authentification);
        certAuthButton.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                setCertAuth(certAuthButton.getSelection());
            }

            public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
                // ignore
            }
        });
        certAuthFileNameEditor = // $NON-NLS-1$
        new org.eclipse.jface.preference.StringFieldEditor("", org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_CertificateFile_, org.eclipse.jface.preference.StringFieldEditor.UNLIMITED, certAuthComp) {
            @java.lang.Override
            protected boolean doCheckState() {
                return true;
            }

            @java.lang.Override
            protected void valueChanged() {
                super.valueChanged();
                if (getWizard() != null) {
                    getWizard().getContainer().updateButtons();
                }
            }

            @java.lang.Override
            public int getNumberOfControls() {
                return 2;
            }
        };
        certBrowseButton = new org.eclipse.swt.widgets.Button(certAuthComp, org.eclipse.swt.SWT.PUSH);
        certBrowseButton.setText(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_ChooseCertificateFile_);
        certBrowseButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                org.eclipse.swt.widgets.FileDialog fileDialog = new org.eclipse.swt.widgets.FileDialog(getShell(), org.eclipse.swt.SWT.OPEN);
                fileDialog.setFilterPath(java.lang.System.getProperty("user.home", "."));// $NON-NLS-1$ //$NON-NLS-2$

                java.lang.String returnFile = fileDialog.open();
                if (returnFile != null) {
                    certAuthFileNameEditor.setStringValue(returnFile);
                }
            }
        });
        certAuthPasswordEditor = // $NON-NLS-1$
        new org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage.RepositoryStringFieldEditor("", org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_CertificatePassword_, org.eclipse.jface.preference.StringFieldEditor.UNLIMITED, certAuthComp) {
            @java.lang.Override
            public int getNumberOfControls() {
                return 2;
            }
        };
        ((org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage.RepositoryStringFieldEditor) (certAuthPasswordEditor)).getTextControl().setEchoChar('*');
        saveCertPasswordButton = new org.eclipse.swt.widgets.Button(certAuthComp, org.eclipse.swt.SWT.CHECK);
        saveCertPasswordButton.setText(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Save_Password);
        certAuthFileNameEditor.setEnabled(certAuthButton.getSelection(), certAuthComp);
        certBrowseButton.setEnabled(certAuthButton.getSelection());
        certAuthPasswordEditor.setEnabled(certAuthButton.getSelection(), certAuthComp);
        saveCertPasswordButton.setEnabled(certAuthButton.getSelection());
        if (repository != null) {
            saveCertPasswordButton.setSelection(repository.getSavePassword(org.eclipse.mylyn.commons.net.AuthenticationType.CERTIFICATE));
        } else {
            saveCertPasswordButton.setSelection(false);
        }
        setCertAuth((oldCertAuthPassword != null) || (oldCertAuthFileName != null));
        section.setExpanded(certAuthButton.getSelection());
        org.eclipse.swt.layout.GridLayout gridLayout2 = new org.eclipse.swt.layout.GridLayout();
        gridLayout2.numColumns = 3;
        gridLayout2.marginWidth = 0;
        certAuthComp.setLayout(gridLayout2);
    }

    private void createHttpAuthSection() {
        org.eclipse.ui.forms.widgets.ExpandableComposite section = createSection(innerComposite, org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Http_Authentication);
        httpAuthComp = toolkit.createComposite(section, org.eclipse.swt.SWT.NONE);
        httpAuthComp.setBackground(compositeContainer.getBackground());
        section.setClient(httpAuthComp);
        httpAuthButton = new org.eclipse.swt.widgets.Button(httpAuthComp, org.eclipse.swt.SWT.CHECK);
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().indent(0, 5).align(org.eclipse.swt.SWT.LEFT, org.eclipse.swt.SWT.TOP).span(3, org.eclipse.swt.SWT.DEFAULT).applyTo(httpAuthButton);
        httpAuthButton.setText(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Enable_http_authentication);
        httpAuthButton.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                setHttpAuth(httpAuthButton.getSelection());
            }

            public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
                // ignore
            }
        });
        httpAuthUserNameEditor = // $NON-NLS-1$
        new org.eclipse.jface.preference.StringFieldEditor("", org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_User_ID_, org.eclipse.jface.preference.StringFieldEditor.UNLIMITED, httpAuthComp) {
            @java.lang.Override
            protected boolean doCheckState() {
                return true;
            }

            @java.lang.Override
            protected void valueChanged() {
                super.valueChanged();
                if (getWizard() != null) {
                    getWizard().getContainer().updateButtons();
                }
            }

            @java.lang.Override
            public int getNumberOfControls() {
                return 3;
            }
        };
        httpAuthPasswordEditor = // $NON-NLS-1$
        new org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage.RepositoryStringFieldEditor("", org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Password_, org.eclipse.jface.preference.StringFieldEditor.UNLIMITED, httpAuthComp) {
            @java.lang.Override
            public int getNumberOfControls() {
                return 2;
            }
        };
        ((org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage.RepositoryStringFieldEditor) (httpAuthPasswordEditor)).getTextControl().setEchoChar('*');
        saveHttpPasswordButton = new org.eclipse.swt.widgets.Button(httpAuthComp, org.eclipse.swt.SWT.CHECK);
        saveHttpPasswordButton.setText(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Save_Password);
        httpAuthUserNameEditor.setEnabled(httpAuthButton.getSelection(), httpAuthComp);
        httpAuthPasswordEditor.setEnabled(httpAuthButton.getSelection(), httpAuthComp);
        saveHttpPasswordButton.setEnabled(httpAuthButton.getSelection());
        if (repository != null) {
            saveHttpPasswordButton.setSelection(repository.getSavePassword(org.eclipse.mylyn.commons.net.AuthenticationType.HTTP));
        } else {
            saveHttpPasswordButton.setSelection(false);
        }
        setHttpAuth((oldHttpAuthPassword != null) || (oldHttpAuthUserId != null));
        section.setExpanded(httpAuthButton.getSelection());
        org.eclipse.swt.layout.GridLayout gridLayout2 = new org.eclipse.swt.layout.GridLayout();
        gridLayout2.numColumns = 3;
        gridLayout2.marginWidth = 0;
        httpAuthComp.setLayout(gridLayout2);
    }

    private void initializeOldValues() {
        if (repository != null) {
            originalUrl = repository.getRepositoryUrl();
            org.eclipse.mylyn.commons.net.AuthenticationCredentials oldCredentials = repository.getCredentials(org.eclipse.mylyn.commons.net.AuthenticationType.REPOSITORY);
            if (oldCredentials != null) {
                oldUsername = oldCredentials.getUserName();
                oldPassword = oldCredentials.getPassword();
            } else {
                oldUsername = "";// $NON-NLS-1$

                oldPassword = "";// $NON-NLS-1$

            }
            org.eclipse.mylyn.commons.net.AuthenticationCredentials oldCertCredentials = repository.getCredentials(org.eclipse.mylyn.commons.net.AuthenticationType.CERTIFICATE);
            if (oldCertCredentials != null) {
                oldCertAuthFileName = oldCertCredentials.getUserName();
                oldCertAuthPassword = oldCertCredentials.getPassword();
            } else {
                oldCertAuthPassword = null;
                oldCertAuthFileName = null;
            }
            org.eclipse.mylyn.commons.net.AuthenticationCredentials oldHttpCredentials = repository.getCredentials(org.eclipse.mylyn.commons.net.AuthenticationType.HTTP);
            if (oldHttpCredentials != null) {
                oldHttpAuthUserId = oldHttpCredentials.getUserName();
                oldHttpAuthPassword = oldHttpCredentials.getPassword();
            } else {
                oldHttpAuthPassword = null;
                oldHttpAuthUserId = null;
            }
            oldProxyHostname = repository.getProperty(org.eclipse.mylyn.tasks.core.TaskRepository.PROXY_HOSTNAME);
            oldProxyPort = repository.getProperty(org.eclipse.mylyn.tasks.core.TaskRepository.PROXY_PORT);
            if (oldProxyHostname == null) {
                oldProxyHostname = "";// $NON-NLS-1$

            }
            if (oldProxyPort == null) {
                oldProxyPort = "";// $NON-NLS-1$

            }
            org.eclipse.mylyn.commons.net.AuthenticationCredentials oldProxyCredentials = repository.getCredentials(org.eclipse.mylyn.commons.net.AuthenticationType.PROXY);
            if (oldProxyCredentials != null) {
                oldProxyUsername = oldProxyCredentials.getUserName();
                oldProxyPassword = oldProxyCredentials.getPassword();
            } else {
                oldProxyUsername = null;
                oldProxyPassword = null;
            }
        } else {
            oldUsername = "";// $NON-NLS-1$

            oldPassword = "";// $NON-NLS-1$

            oldHttpAuthPassword = null;
            oldHttpAuthUserId = null;
            oldCertAuthFileName = null;
            oldCertAuthPassword = null;
        }
    }

    private void createProxySection() {
        org.eclipse.ui.forms.widgets.ExpandableComposite section = createSection(innerComposite, org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Proxy_Server_Configuration);
        proxyAuthComp = toolkit.createComposite(section, org.eclipse.swt.SWT.NONE);
        org.eclipse.swt.layout.GridLayout gridLayout2 = new org.eclipse.swt.layout.GridLayout();
        gridLayout2.verticalSpacing = 0;
        gridLayout2.numColumns = 3;
        proxyAuthComp.setLayout(gridLayout2);
        proxyAuthComp.setBackground(compositeContainer.getBackground());
        section.setClient(proxyAuthComp);
        org.eclipse.swt.widgets.Composite systemSettingsComposite = new org.eclipse.swt.widgets.Composite(proxyAuthComp, org.eclipse.swt.SWT.NULL);
        org.eclipse.swt.layout.GridLayout gridLayout3 = new org.eclipse.swt.layout.GridLayout();
        gridLayout3.verticalSpacing = 0;
        gridLayout3.numColumns = 2;
        gridLayout3.marginWidth = 0;
        systemSettingsComposite.setLayout(gridLayout3);
        systemProxyButton = new org.eclipse.swt.widgets.Button(systemSettingsComposite, org.eclipse.swt.SWT.CHECK);
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.LEFT, org.eclipse.swt.SWT.TOP).span(3, org.eclipse.swt.SWT.DEFAULT).applyTo(systemSettingsComposite);
        systemProxyButton.setText(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Use_global_Network_Connections_preferences);
        systemProxyButton.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                setUseDefaultProxy(systemProxyButton.getSelection());
            }

            public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
                // ignore
            }
        });
        changeProxySettingsLink = toolkit.createHyperlink(systemSettingsComposite, org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Change_Settings, org.eclipse.swt.SWT.NULL);
        changeProxySettingsLink.setBackground(compositeContainer.getBackground());
        changeProxySettingsLink.addHyperlinkListener(new org.eclipse.ui.forms.events.IHyperlinkListener() {
            public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                org.eclipse.jface.preference.PreferenceDialog dlg = org.eclipse.ui.dialogs.PreferencesUtil.createPreferenceDialogOn(getShell(), org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage.PREFS_PAGE_ID_NET_PROXY, new java.lang.String[]{ org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage.PREFS_PAGE_ID_NET_PROXY }, null);
                dlg.open();
            }

            public void linkEntered(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                // ignore
            }

            public void linkExited(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                // ignore
            }
        });
        proxyHostnameEditor = // $NON-NLS-1$
        new org.eclipse.jface.preference.StringFieldEditor("", org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Proxy_host_address_, org.eclipse.jface.preference.StringFieldEditor.UNLIMITED, proxyAuthComp) {
            @java.lang.Override
            protected boolean doCheckState() {
                return true;
            }

            @java.lang.Override
            protected void valueChanged() {
                super.valueChanged();
                if (getWizard() != null) {
                    getWizard().getContainer().updateButtons();
                }
            }

            @java.lang.Override
            public int getNumberOfControls() {
                return 3;
            }
        };
        proxyHostnameEditor.setStringValue(oldProxyHostname);
        proxyPortEditor = // $NON-NLS-1$
        new org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage.RepositoryStringFieldEditor("", org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Proxy_host_port_, org.eclipse.jface.preference.StringFieldEditor.UNLIMITED, proxyAuthComp) {
            @java.lang.Override
            protected boolean doCheckState() {
                return true;
            }

            @java.lang.Override
            protected void valueChanged() {
                super.valueChanged();
                if (getWizard() != null) {
                    getWizard().getContainer().updateButtons();
                }
            }

            @java.lang.Override
            public int getNumberOfControls() {
                return 3;
            }
        };
        proxyPortEditor.setStringValue(oldProxyPort);
        proxyHostnameEditor.setEnabled(systemProxyButton.getSelection(), proxyAuthComp);
        proxyPortEditor.setEnabled(systemProxyButton.getSelection(), proxyAuthComp);
        // ************* PROXY AUTHENTICATION **************
        proxyAuthButton = new org.eclipse.swt.widgets.Button(proxyAuthComp, org.eclipse.swt.SWT.CHECK);
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().span(3, org.eclipse.swt.SWT.DEFAULT).applyTo(proxyAuthButton);
        proxyAuthButton.setText(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Enable_proxy_authentication);
        proxyAuthButton.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                setProxyAuth(proxyAuthButton.getSelection());
            }

            public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
                // ignore
            }
        });
        proxyUserNameEditor = // $NON-NLS-1$
        new org.eclipse.jface.preference.StringFieldEditor("", org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_User_ID_, org.eclipse.jface.preference.StringFieldEditor.UNLIMITED, proxyAuthComp) {
            @java.lang.Override
            protected boolean doCheckState() {
                return true;
            }

            @java.lang.Override
            protected void valueChanged() {
                super.valueChanged();
                if (getWizard() != null) {
                    getWizard().getContainer().updateButtons();
                }
            }

            @java.lang.Override
            public int getNumberOfControls() {
                return 3;
            }
        };
        proxyPasswordEditor = // $NON-NLS-1$
        new org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage.RepositoryStringFieldEditor("", org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Password_, org.eclipse.jface.preference.StringFieldEditor.UNLIMITED, proxyAuthComp) {
            @java.lang.Override
            public int getNumberOfControls() {
                return 2;
            }
        };
        ((org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage.RepositoryStringFieldEditor) (proxyPasswordEditor)).getTextControl().setEchoChar('*');
        // proxyPasswordEditor.setEnabled(httpAuthButton.getSelection(),
        // advancedComp);
        // ((StringFieldEditor)
        // httpAuthPasswordEditor).setEnabled(httpAuthButton.getSelection(),
        // advancedComp);
        // need to increase column number here, because above string editor will use them if declared beforehand
        ((org.eclipse.swt.layout.GridLayout) (proxyAuthComp.getLayout())).numColumns++;
        saveProxyPasswordButton = new org.eclipse.swt.widgets.Button(proxyAuthComp, org.eclipse.swt.SWT.CHECK);
        saveProxyPasswordButton.setText(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Save_Password);
        saveProxyPasswordButton.setEnabled(proxyAuthButton.getSelection());
        if (repository != null) {
            saveProxyPasswordButton.setSelection(repository.getSavePassword(org.eclipse.mylyn.commons.net.AuthenticationType.PROXY));
        } else {
            saveProxyPasswordButton.setSelection(false);
        }
        setProxyAuth((oldProxyUsername != null) || (oldProxyPassword != null));
        setUseDefaultProxy(repository != null ? repository.isDefaultProxyEnabled() : true);
        section.setExpanded(!systemProxyButton.getSelection());
    }

    // private void addContributionSection() {
    // Composite composite = toolkit.createComposite(compositeContainer);
    // GridDataFactory.fillDefaults().grab(true, false).span(3, SWT.DEFAULT).applyTo(composite);
    // 
    // GridLayout layout = new GridLayout(1, false);
    // layout.marginWidth = 0;
    // layout.marginTop = -5;
    // composite.setLayout(layout);
    // 
    // composite.setBackground(compositeContainer.getBackground());
    // 
    // createContributionControls(composite);
    // }
    protected void setEncoding(java.lang.String encoding) {
        if (encoding.equals(org.eclipse.mylyn.tasks.core.TaskRepository.DEFAULT_CHARACTER_ENCODING)) {
            setDefaultEncoding();
        } else if (otherEncodingCombo.indexOf(encoding) != (-1)) {
            defaultEncoding.setSelection(false);
            otherEncodingCombo.setEnabled(true);
            otherEncoding.setSelection(true);
            otherEncodingCombo.select(otherEncodingCombo.indexOf(encoding));
        } else {
            setDefaultEncoding();
        }
    }

    private void setDefaultEncoding() {
        defaultEncoding.setSelection(true);
        otherEncoding.setSelection(false);
        otherEncodingCombo.setEnabled(false);
        if (otherEncodingCombo.getItemCount() > 0) {
            otherEncodingCombo.select(0);
        }
    }

    /**
     *
     * @since 2.0
     */
    public void setAnonymous(boolean selected) {
        if (!needsAnonymousLogin) {
            return;
        }
        anonymousButton.setSelection(selected);
        updateCredentialsEditors();
        if (getWizard() != null) {
            getWizard().getContainer().updateButtons();
        }
    }

    private void updateCredentialsEditors() {
        if ((repositoryUserNameEditor != null) && (repositoryPasswordEditor != null)) {
            boolean shouldEnable = needsRepositoryCredentials() && (!isAnonymousAccess());
            if (shouldEnable) {
                repositoryUserNameEditor.setStringValue(oldUsername);
                repositoryPasswordEditor.setStringValue(oldPassword);
            } else {
                oldUsername = repositoryUserNameEditor.getStringValue();
                oldPassword = repositoryPasswordEditor.getStringValue();
                repositoryUserNameEditor.setStringValue("");// $NON-NLS-1$

                repositoryPasswordEditor.setStringValue("");// $NON-NLS-1$

            }
            repositoryUserNameEditor.setEnabled(shouldEnable, compositeContainer);
            repositoryPasswordEditor.setEnabled(shouldEnable, compositeContainer);
            savePasswordButton.setEnabled(shouldEnable);
        }
    }

    /**
     *
     * @since 2.0
     */
    public void setHttpAuth(boolean selected) {
        if (!needsHttpAuth) {
            return;
        }
        httpAuthButton.setSelection(selected);
        if (!selected) {
            oldHttpAuthUserId = httpAuthUserNameEditor.getStringValue();
            oldHttpAuthPassword = httpAuthPasswordEditor.getStringValue();
            httpAuthUserNameEditor.setStringValue(null);
            httpAuthPasswordEditor.setStringValue(null);
        } else {
            httpAuthUserNameEditor.setStringValue(oldHttpAuthUserId);
            httpAuthPasswordEditor.setStringValue(oldHttpAuthPassword);
        }
        httpAuthUserNameEditor.setEnabled(selected, httpAuthComp);
        httpAuthPasswordEditor.setEnabled(selected, httpAuthComp);
        saveHttpPasswordButton.setEnabled(selected);
    }

    /**
     *
     * @since 3.6
     */
    public void setCertAuth(boolean selected) {
        if (!needsCertAuth) {
            return;
        }
        certAuthButton.setSelection(selected);
        if (!selected) {
            oldCertAuthFileName = certAuthFileNameEditor.getStringValue();
            oldCertAuthPassword = certAuthPasswordEditor.getStringValue();
            certAuthFileNameEditor.setStringValue(null);
            certAuthPasswordEditor.setStringValue(null);
        } else {
            certAuthFileNameEditor.setStringValue(oldCertAuthFileName);
            certAuthPasswordEditor.setStringValue(oldCertAuthPassword);
        }
        certAuthFileNameEditor.setEnabled(selected, certAuthComp);
        certBrowseButton.setEnabled(selected);
        certAuthPasswordEditor.setEnabled(selected, certAuthComp);
        saveCertPasswordButton.setEnabled(selected);
    }

    /**
     *
     * @since 2.2
     */
    public boolean getHttpAuth() {
        return httpAuthButton.getSelection();
    }

    /**
     *
     * @since 3.6
     */
    public boolean getCertAuth() {
        return certAuthButton.getSelection();
    }

    /**
     *
     * @since 2.0
     */
    public void setUseDefaultProxy(boolean selected) {
        if (!needsProxy) {
            return;
        }
        systemProxyButton.setSelection(selected);
        if (selected) {
            oldProxyHostname = proxyHostnameEditor.getStringValue();
            oldProxyPort = proxyPortEditor.getStringValue();
            // proxyHostnameEditor.setStringValue(null);
            // proxyPortEditor.setStringValue(null);
        } else {
            proxyHostnameEditor.setStringValue(oldProxyHostname);
            proxyPortEditor.setStringValue(oldProxyPort);
        }
        proxyHostnameEditor.setEnabled(!selected, proxyAuthComp);
        proxyPortEditor.setEnabled(!selected, proxyAuthComp);
        proxyAuthButton.setEnabled(!selected);
        setProxyAuth(proxyAuthButton.getSelection());
    }

    /**
     *
     * @since 2.0
     */
    public void setProxyAuth(boolean selected) {
        proxyAuthButton.setSelection(selected);
        proxyAuthButton.setEnabled(!systemProxyButton.getSelection());
        if (!selected) {
            oldProxyUsername = proxyUserNameEditor.getStringValue();
            oldProxyPassword = proxyPasswordEditor.getStringValue();
            proxyUserNameEditor.setStringValue(null);
            proxyPasswordEditor.setStringValue(null);
        } else {
            proxyUserNameEditor.setStringValue(oldProxyUsername);
            proxyPasswordEditor.setStringValue(oldProxyPassword);
        }
        proxyUserNameEditor.setEnabled(selected && (!systemProxyButton.getSelection()), proxyAuthComp);
        proxyPasswordEditor.setEnabled(selected && (!systemProxyButton.getSelection()), proxyAuthComp);
        saveProxyPasswordButton.setEnabled(selected && (!systemProxyButton.getSelection()));
    }

    /**
     *
     * @since 2.2
     */
    public boolean getProxyAuth() {
        return proxyAuthButton.getSelection();
    }

    /**
     *
     * @since 3.0
     */
    protected void addRepositoryTemplatesToServerUrlCombo() {
        final org.eclipse.mylyn.internal.tasks.core.RepositoryTemplateManager templateManager = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryTemplateManager();
        for (org.eclipse.mylyn.tasks.core.RepositoryTemplate template : templateManager.getTemplates(connector.getConnectorKind())) {
            serverUrlCombo.add(template.label);
        }
        serverUrlCombo.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                java.lang.String text = serverUrlCombo.getText();
                org.eclipse.mylyn.tasks.core.RepositoryTemplate template = templateManager.getTemplate(connector.getConnectorKind(), text);
                if (template != null) {
                    repositoryTemplateSelected(template);
                    return;
                }
            }
        });
    }

    /**
     *
     * @since 3.0
     */
    protected void repositoryTemplateSelected(org.eclipse.mylyn.tasks.core.RepositoryTemplate template) {
    }

    /**
     *
     * @since 2.0
     */
    protected abstract void createAdditionalControls(@org.eclipse.jdt.annotation.NonNull
    org.eclipse.swt.widgets.Composite parent);

    /**
     *
     * @since 2.0
     */
    protected boolean isValidUrl(java.lang.String url) {
        if (url.startsWith(org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage.URL_PREFIX_HTTPS) || url.startsWith(org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage.URL_PREFIX_HTTP)) {
            try {
                new org.apache.commons.httpclient.URI(url, true, "UTF-8");// $NON-NLS-1$

                return true;
            } catch (java.lang.Exception e) {
                return false;
            }
        }
        return false;
    }

    private void updateHyperlinks() {
        if ((getRepositoryUrl() != null) && (getRepositoryUrl().length() > 0)) {
            org.eclipse.mylyn.tasks.core.TaskRepository repository = new org.eclipse.mylyn.tasks.core.TaskRepository(connector.getConnectorKind(), getRepositoryUrl());
            java.lang.String accountCreationUrl = connectorUi.getAccountCreationUrl(repository);
            createAccountHyperlink.setEnabled(accountCreationUrl != null);
            createAccountHyperlink.setVisible(accountCreationUrl != null);
            java.lang.String accountManagementUrl = connectorUi.getAccountManagementUrl(repository);
            manageAccountHyperlink.setEnabled(accountManagementUrl != null);
            manageAccountHyperlink.setVisible(accountManagementUrl != null);
        } else {
            createAccountHyperlink.setEnabled(false);
            createAccountHyperlink.setVisible(false);
            manageAccountHyperlink.setEnabled(false);
            manageAccountHyperlink.setVisible(false);
        }
    }

    /**
     *
     * @since 2.0
     */
    public java.lang.String getRepositoryLabel() {
        return repositoryLabelEditor.getStringValue();
    }

    /**
     *
     * @since 3.0
     */
    public java.lang.String getRepositoryUrl() {
        return org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager.stripSlashes(serverUrlCombo.getText());
    }

    /**
     *
     * @since 2.0
     */
    public java.lang.String getUserName() {
        if (needsRepositoryCredentials()) {
            return repositoryUserNameEditor.getStringValue();
        } else {
            return "";// $NON-NLS-1$

        }
    }

    /**
     *
     * @since 2.0
     */
    public java.lang.String getPassword() {
        if (needsRepositoryCredentials()) {
            return repositoryPasswordEditor.getStringValue();
        } else {
            return "";// $NON-NLS-1$

        }
    }

    /**
     *
     * @since 2.0
     */
    public java.lang.String getHttpAuthUserId() {
        if (needsHttpAuth()) {
            return httpAuthUserNameEditor.getStringValue();
        } else {
            return "";// $NON-NLS-1$

        }
    }

    /**
     *
     * @since 2.0
     */
    public java.lang.String getHttpAuthPassword() {
        if (needsHttpAuth()) {
            return httpAuthPasswordEditor.getStringValue();
        } else {
            return "";// $NON-NLS-1$

        }
    }

    /**
     *
     * @since 3.6
     */
    public java.lang.String getCertAuthFileName() {
        if (needsCertAuth()) {
            return certAuthFileNameEditor.getStringValue();
        } else {
            return "";// $NON-NLS-1$

        }
    }

    /**
     *
     * @since 3.6
     */
    public java.lang.String getCertAuthPassword() {
        if (needsCertAuth()) {
            return certAuthPasswordEditor.getStringValue();
        } else {
            return "";// $NON-NLS-1$

        }
    }

    /**
     *
     * @since 2.0
     */
    public java.lang.String getProxyHostname() {
        if (needsProxy()) {
            return proxyHostnameEditor.getStringValue();
        } else {
            return "";// $NON-NLS-1$

        }
    }

    /**
     *
     * @since 2.0
     */
    public java.lang.String getProxyPort() {
        if (needsProxy()) {
            return proxyPortEditor.getStringValue();
        } else {
            return "";// $NON-NLS-1$

        }
    }

    /**
     *
     * @since 2.0
     */
    public java.lang.Boolean getUseDefaultProxy() {
        if (needsProxy()) {
            return systemProxyButton.getSelection();
        } else {
            return true;
        }
    }

    /**
     *
     * @since 2.0
     */
    public java.lang.String getProxyUserName() {
        if (needsProxy()) {
            return proxyUserNameEditor.getStringValue();
        } else {
            return "";// $NON-NLS-1$

        }
    }

    /**
     *
     * @since 2.0
     */
    public java.lang.String getProxyPassword() {
        if (needsProxy()) {
            return proxyPasswordEditor.getStringValue();
        } else {
            return "";// $NON-NLS-1$

        }
    }

    /**
     *
     * @since 2.0
     */
    public void init(org.eclipse.ui.IWorkbench workbench) {
        // ignore
    }

    /**
     *
     * @since 2.0
     */
    public boolean isAnonymousAccess() {
        if (anonymousButton != null) {
            return anonymousButton.getSelection();
        } else {
            return false;
        }
    }

    /**
     * Exposes StringFieldEditor.refreshValidState() TODO: is there a better way?
     */
    private static class RepositoryStringFieldEditor extends org.eclipse.jface.preference.StringFieldEditor {
        public RepositoryStringFieldEditor(java.lang.String name, java.lang.String labelText, int style, org.eclipse.swt.widgets.Composite parent) {
            super(name, labelText, style, parent);
        }

        @java.lang.Override
        public void refreshValidState() {
            try {
                super.refreshValidState();
            } catch (java.lang.Exception e) {
                org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Problem refreshing password field", e));// $NON-NLS-1$

            }
        }

        @java.lang.Override
        public org.eclipse.swt.widgets.Text getTextControl() {
            return super.getTextControl();
        }
    }

    @java.lang.Override
    public boolean isPageComplete() {
        java.lang.String errorMessage = null;
        java.lang.String url = getRepositoryUrl();
        // check for errors
        errorMessage = isUniqueUrl(url);
        if (errorMessage == null) {
            for (org.eclipse.mylyn.tasks.core.TaskRepository repository : org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getAllRepositories()) {
                if ((!repository.equals(getRepository())) && getRepositoryLabel().equals(repository.getRepositoryLabel())) {
                    errorMessage = org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_A_repository_with_this_name_already_exists;
                    break;
                }
            }
        }
        if (errorMessage == null) {
            // check for messages
            if (!isValidUrl(url)) {
                errorMessage = org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Enter_a_valid_server_url;
            }
            if ((((errorMessage == null) && needsRepositoryCredentials()) && ((!needsAnonymousLogin()) || (!anonymousButton.getSelection()))) && isMissingCredentials()) {
                errorMessage = org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Enter_a_user_id_Message0;
            }
            setMessage(errorMessage, repository == null ? org.eclipse.jface.dialogs.IMessageProvider.NONE : org.eclipse.jface.dialogs.IMessageProvider.ERROR);
        } else {
            setMessage(errorMessage, org.eclipse.jface.dialogs.IMessageProvider.ERROR);
        }
        return (errorMessage == null) && super.isPageComplete();
    }

    /**
     * Returns true, if credentials are incomplete. Clients may override this method.
     *
     * @since 3.4
     */
    protected boolean isMissingCredentials() {
        return (needsRepositoryCredentials() && repositoryUserNameEditor.getStringValue().trim().equals(""))// $NON-NLS-1$
         || (getSavePassword() && repositoryPasswordEditor.getStringValue().trim().equals(""));// $NON-NLS-1$

    }

    /**
     *
     * @since 2.0
     */
    protected java.lang.String isUniqueUrl(java.lang.String urlString) {
        if (!urlString.equals(originalUrl)) {
            if (repositoryUrls == null) {
                java.util.List<org.eclipse.mylyn.tasks.core.TaskRepository> repositories = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getAllRepositories();
                repositoryUrls = new java.util.HashSet<java.lang.String>(repositories.size());
                for (org.eclipse.mylyn.tasks.core.TaskRepository repository : repositories) {
                    repositoryUrls.add(repository.getRepositoryUrl());
                }
            }
            if (repositoryUrls.contains(urlString)) {
                return org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Repository_already_exists;
            }
        }
        return null;
    }

    /**
     *
     * @since 2.0
     */
    @java.lang.Deprecated
    public void setRepository(org.eclipse.mylyn.tasks.core.TaskRepository repository) {
        this.repository = repository;
    }

    /**
     *
     * @since 2.0
     */
    public void setVersion(java.lang.String previousVersion) {
        if (previousVersion == null) {
            serverVersion = org.eclipse.mylyn.tasks.core.TaskRepository.NO_VERSION_SPECIFIED;
        } else {
            serverVersion = previousVersion;
        }
    }

    /**
     *
     * @since 2.0
     */
    public java.lang.String getVersion() {
        return serverVersion;
    }

    /**
     *
     * @since 2.0
     */
    public org.eclipse.mylyn.tasks.core.TaskRepository getRepository() {
        return repository;
    }

    /**
     *
     * @since 2.0
     */
    public java.lang.String getCharacterEncoding() {
        if (defaultEncoding == null) {
            return null;
        }
        if (defaultEncoding.getSelection()) {
            return org.eclipse.mylyn.tasks.core.TaskRepository.DEFAULT_CHARACTER_ENCODING;
        } else if (otherEncodingCombo.getSelectionIndex() > (-1)) {
            return otherEncodingCombo.getItem(otherEncodingCombo.getSelectionIndex());
        } else {
            return org.eclipse.mylyn.tasks.core.TaskRepository.DEFAULT_CHARACTER_ENCODING;
        }
    }

    /**
     * Creates a {@link TaskRepository} based on the current settings.
     * <p>
     * Note: The credentials of the created repository are not persisted in the platform keystore. When overriding,
     * subclasses must either call super or call {@link TaskRepository#setShouldPersistCredentials(boolean)
     * setShouldPersistCredentials(false)} before calling {@link #applyTo(TaskRepository)}.
     *
     * @since 2.0
     */
    public org.eclipse.mylyn.tasks.core.TaskRepository createTaskRepository() {
        org.eclipse.mylyn.tasks.core.TaskRepository repository = new org.eclipse.mylyn.tasks.core.TaskRepository(connector.getConnectorKind(), getRepositoryUrl());
        // do not modify the secure storage for a temporary repository
        repository.setShouldPersistCredentials(false);
        applyTo(repository);
        return repository;
    }

    /**
     *
     * @since 2.2
     */
    @java.lang.Override
    public void applyTo(@org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.core.TaskRepository repository) {
        java.lang.String category = repository.getCategory();
        if ((category == null) || (category.length() == 0)) {
            connector.applyDefaultCategory(repository);
        }
        repository.setVersion(getVersion());
        if (brand != null) {
            repository.setProperty(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.PROPERTY_BRAND_ID, brand);
        }
        if (needsEncoding()) {
            repository.setCharacterEncoding(getCharacterEncoding());
        }
        if (needsRepositoryCredentials()) {
            if (isAnonymousAccess()) {
                repository.setCredentials(org.eclipse.mylyn.commons.net.AuthenticationType.REPOSITORY, null, getSavePassword());
            } else {
                org.eclipse.mylyn.commons.net.AuthenticationCredentials credentials = new org.eclipse.mylyn.commons.net.AuthenticationCredentials(getUserName(), getPassword());
                repository.setCredentials(org.eclipse.mylyn.commons.net.AuthenticationType.REPOSITORY, credentials, getSavePassword());
            }
        }
        repository.setRepositoryLabel(getRepositoryLabel());
        if (needsCertAuth()) {
            if (getCertAuth()) {
                org.eclipse.mylyn.commons.net.AuthenticationCredentials webCredentials = new org.eclipse.mylyn.commons.net.AuthenticationCredentials(getCertAuthFileName(), getCertAuthPassword());
                repository.setCredentials(org.eclipse.mylyn.commons.net.AuthenticationType.CERTIFICATE, webCredentials, getSaveCertPassword());
            } else {
                repository.setCredentials(org.eclipse.mylyn.commons.net.AuthenticationType.CERTIFICATE, null, getSaveCertPassword());
            }
        }
        if (needsHttpAuth()) {
            if (getHttpAuth()) {
                org.eclipse.mylyn.commons.net.AuthenticationCredentials webCredentials = new org.eclipse.mylyn.commons.net.AuthenticationCredentials(getHttpAuthUserId(), getHttpAuthPassword());
                repository.setCredentials(org.eclipse.mylyn.commons.net.AuthenticationType.HTTP, webCredentials, getSaveHttpPassword());
            } else {
                repository.setCredentials(org.eclipse.mylyn.commons.net.AuthenticationType.HTTP, null, getSaveHttpPassword());
            }
        }
        if (needsProxy()) {
            repository.setDefaultProxyEnabled(getUseDefaultProxy());
            repository.setProperty(org.eclipse.mylyn.tasks.core.TaskRepository.PROXY_HOSTNAME, getProxyHostname());
            repository.setProperty(org.eclipse.mylyn.tasks.core.TaskRepository.PROXY_PORT, getProxyPort());
            if (getProxyAuth()) {
                org.eclipse.mylyn.commons.net.AuthenticationCredentials webCredentials = new org.eclipse.mylyn.commons.net.AuthenticationCredentials(getProxyUserName(), getProxyPassword());
                repository.setCredentials(org.eclipse.mylyn.commons.net.AuthenticationType.PROXY, webCredentials, getSaveProxyPassword());
            } else {
                repository.setCredentials(org.eclipse.mylyn.commons.net.AuthenticationType.PROXY, null, getSaveProxyPassword());
            }
        }
        if (disconnectedButton != null) {
            repository.setOffline(disconnectedButton.getSelection());
        }
        super.applyTo(repository);
    }

    /**
     *
     * @since 2.0
     */
    public org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector getConnector() {
        return connector;
    }

    /**
     *
     * @since 2.0
     */
    public boolean needsEncoding() {
        return needsEncoding;
    }

    /**
     *
     * @since 2.0
     */
    public boolean needsTimeZone() {
        return needsTimeZone;
    }

    /**
     *
     * @since 2.0
     */
    public boolean needsAnonymousLogin() {
        return needsAnonymousLogin;
    }

    /**
     *
     * @since 3.11
     */
    public boolean needsRepositoryCredentials() {
        return needsRepositoryCredentials;
    }

    /**
     *
     * @since 2.0
     */
    public boolean needsAdvanced() {
        return needsAdvanced;
    }

    /**
     *
     * @since 2.0
     */
    public void setNeedsEncoding(boolean needsEncoding) {
        this.needsEncoding = needsEncoding;
    }

    /**
     *
     * @since 2.0
     */
    public void setNeedsTimeZone(boolean needsTimeZone) {
        this.needsTimeZone = needsTimeZone;
    }

    /**
     *
     * @since 2.0
     */
    public void setNeedsAdvanced(boolean needsAdvanced) {
        this.needsAdvanced = needsAdvanced;
    }

    /**
     *
     * @since 3.6
     */
    public boolean needsCertAuth() {
        return this.needsCertAuth;
    }

    /**
     *
     * @since 3.6
     */
    public void setNeedsCertAuth(boolean needsCertificate) {
        this.needsCertAuth = needsCertificate;
    }

    /**
     *
     * @since 2.0
     */
    public boolean needsHttpAuth() {
        return this.needsHttpAuth;
    }

    /**
     *
     * @since 2.0
     */
    public void setNeedsHttpAuth(boolean needsHttpAuth) {
        this.needsHttpAuth = needsHttpAuth;
    }

    /**
     *
     * @since 2.0
     */
    public void setNeedsProxy(boolean needsProxy) {
        this.needsProxy = needsProxy;
    }

    /**
     *
     * @since 2.0
     */
    public boolean needsProxy() {
        return this.needsProxy;
    }

    /**
     *
     * @since 2.0
     */
    public void setNeedsAnonymousLogin(boolean needsAnonymousLogin) {
        this.needsAnonymousLogin = needsAnonymousLogin;
    }

    /**
     *
     * @since 3.11
     */
    public void setNeedsRepositoryCredentials(boolean needsRepositoryCredentials) {
        this.needsRepositoryCredentials = needsRepositoryCredentials;
        updateCredentialsEditors();
    }

    public void setNeedsValidation(boolean needsValidation) {
        this.needsValidation = needsValidation;
    }

    /**
     * Returns whether this page can be validated or not.
     * <p>
     * This information is typically used by the wizard to set the enablement of the validation UI affordance.
     * </p>
     *
     * @return <code>true</code> if this page can be validated, and <code>false</code> otherwise
     * @see #needsValidation()
     * @see IWizardContainer#updateButtons()
     * @since 3.4
     */
    public boolean canValidate() {
        return true;
    }

    /**
     *
     * @since 2.0
     */
    public boolean needsValidation() {
        return needsValidation;
    }

    /**
     * Public for testing.
     *
     * @since 2.0
     */
    public void setUrl(java.lang.String url) {
        serverUrlCombo.setText(url);
    }

    /**
     * Sets the URL control is read only, or can be edited.
     *
     * @since 3.18
     */
    public void setUrlReadOnly(boolean value) {
        serverUrlReadOnly = value;
    }

    /**
     *
     * @return if the URL control is read-only.
     * @since 3.18
     */
    public boolean isUrlReadOnly() {
        return serverUrlReadOnly;
    }

    /**
     * Public for testing.
     *
     * @since 2.0
     */
    public void setUserId(java.lang.String id) {
        repositoryUserNameEditor.setStringValue(id);
    }

    /**
     * Public for testing.
     *
     * @since 2.0
     */
    public void setPassword(java.lang.String pass) {
        repositoryPasswordEditor.setStringValue(pass);
    }

    /**
     *
     * @since 2.2
     */
    public java.lang.Boolean getSavePassword() {
        return savePasswordButton.getSelection();
    }

    /**
     *
     * @since 2.2
     */
    public java.lang.Boolean getSaveProxyPassword() {
        if (needsProxy()) {
            return saveProxyPasswordButton.getSelection();
        } else {
            return false;
        }
    }

    /**
     *
     * @since 3.6
     */
    public java.lang.Boolean getSaveCertPassword() {
        if (needsCertAuth()) {
            return saveCertPasswordButton.getSelection();
        } else {
            return false;
        }
    }

    /**
     *
     * @since 2.2
     */
    public java.lang.Boolean getSaveHttpPassword() {
        if (needsHttpAuth()) {
            return saveHttpPasswordButton.getSelection();
        } else {
            return false;
        }
    }

    /**
     * Validate settings provided by the {@link #getValidator(TaskRepository) validator}, typically the server settings.
     *
     * @since 2.0
     */
    protected void validateSettings() {
        org.eclipse.mylyn.tasks.core.TaskRepository newTaskRepository = createTaskRepository();
        final org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage.Validator validator = getValidator(newTaskRepository);
        if (validator == null) {
            return;
        }
        try {
            getWizard().getContainer().run(true, true, new org.eclipse.jface.operation.IRunnableWithProgress() {
                public void run(org.eclipse.core.runtime.IProgressMonitor monitor) throws java.lang.reflect.InvocationTargetException, java.lang.InterruptedException {
                    monitor.beginTask(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Validating_server_settings, org.eclipse.core.runtime.IProgressMonitor.UNKNOWN);
                    try {
                        validator.run(monitor);
                        if (validator.getStatus() == null) {
                            validator.setStatus(org.eclipse.core.runtime.Status.OK_STATUS);
                        }
                    } catch (org.eclipse.core.runtime.CoreException e) {
                        validator.setStatus(e.getStatus());
                    } catch (org.eclipse.core.runtime.OperationCanceledException e) {
                        validator.setStatus(org.eclipse.core.runtime.Status.CANCEL_STATUS);
                        throw new java.lang.InterruptedException();
                    } catch (java.lang.Exception e) {
                        throw new java.lang.reflect.InvocationTargetException(e);
                    } finally {
                        monitor.done();
                    }
                }
            });
        } catch (java.lang.reflect.InvocationTargetException e) {
            org.eclipse.ui.statushandlers.StatusManager.getManager().handle(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Internal_error_validating_repository, e), org.eclipse.ui.statushandlers.StatusManager.SHOW | org.eclipse.ui.statushandlers.StatusManager.LOG);
            return;
        } catch (java.lang.InterruptedException e) {
            // canceled
            return;
        }
        getWizard().getContainer().updateButtons();
        applyValidatorResult(validator);
        if (isValid) {
            saveToValidatedProperties(newTaskRepository);
        }
    }

    /**
     *
     * @since 3.1
     */
    @java.lang.Override
    protected org.eclipse.core.runtime.IStatus validate() {
        return null;
    }

    /**
     *
     * @since 2.0
     */
    protected void applyValidatorResult(org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage.Validator validator) {
        org.eclipse.core.runtime.IStatus status = validator.getStatus();
        java.lang.String message = status.getMessage();
        if ((message == null) || (message.length() == 0)) {
            message = null;
        }
        switch (status.getSeverity()) {
            case org.eclipse.core.runtime.IStatus.OK :
                if (status == org.eclipse.core.runtime.Status.OK_STATUS) {
                    if (getUserName().length() > 0) {
                        message = org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Authentication_credentials_are_valid;
                    } else {
                        message = org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Repository_is_valid;
                    }
                }
                setMessage(message, org.eclipse.jface.dialogs.IMessageProvider.INFORMATION);
                break;
            case org.eclipse.core.runtime.IStatus.INFO :
                setMessage(message, org.eclipse.jface.dialogs.IMessageProvider.INFORMATION);
                break;
            case org.eclipse.core.runtime.IStatus.WARNING :
                setMessage(message, org.eclipse.jface.dialogs.IMessageProvider.WARNING);
                break;
            default :
                setMessage(message, org.eclipse.jface.dialogs.IMessageProvider.ERROR);
                break;
        }
        setErrorMessage(null);
        isValid = (status.getSeverity() == org.eclipse.core.runtime.IStatus.OK) || (status.getSeverity() == org.eclipse.core.runtime.IStatus.INFO);
    }

    /**
     * For version 3.11 we change the abstract implementation to a default implementation. The default implementation
     * creates an {@link Validator} and deligate the work to
     * {@link AbstractRepositoryConnector#validateRepository(TaskRepository, IProgressMonitor)}
     *
     * @since 2.0
     */
    protected org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage.Validator getValidator(@org.eclipse.jdt.annotation.NonNull
    final org.eclipse.mylyn.tasks.core.TaskRepository repository) {
        return new org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage.Validator() {
            @java.lang.Override
            public void run(org.eclipse.core.runtime.IProgressMonitor monitor) throws org.eclipse.core.runtime.CoreException {
                org.eclipse.mylyn.tasks.core.RepositoryInfo repositoryInfo = connector.validateRepository(repository, monitor);
                setStatus(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.OK, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.osgi.util.NLS.bind(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Settings_are_valid_version, repositoryInfo.getVersion())));
            }
        };
    }

    /**
     * Public for testing.
     *
     * @since 2.0
     */
    public abstract class Validator {
        private org.eclipse.core.runtime.IStatus status;

        public abstract void run(@org.eclipse.jdt.annotation.NonNull
        org.eclipse.core.runtime.IProgressMonitor monitor) throws org.eclipse.core.runtime.CoreException;

        public org.eclipse.core.runtime.IStatus getStatus() {
            return status;
        }

        public void setStatus(org.eclipse.core.runtime.IStatus status) {
            this.status = status;
        }
    }

    /**
     * Provides an adapter for the {@link IValidatable} interface.
     *
     * @since 3.7
     * @see IAdaptable#getAdapter(Class)
     */
    public java.lang.Object getAdapter(@java.lang.SuppressWarnings("rawtypes")
    java.lang.Class adapter) {
        if (adapter == org.eclipse.mylyn.commons.ui.dialogs.IValidatable.class) {
            return new org.eclipse.mylyn.commons.ui.dialogs.IValidatable() {
                public void validate() {
                    AbstractRepositorySettingsPage.this.validateSettings();
                }

                public boolean needsValidation() {
                    return AbstractRepositorySettingsPage.this.needsValidation();
                }

                public boolean canValidate() {
                    return AbstractRepositorySettingsPage.this.canValidate();
                }
            };
        }
        return null;
    }

    /**
     *
     * @since 3.7
     */
    public boolean needsValidateOnFinish() {
        return needsValidateOnFinish;
    }

    /**
     *
     * @since 3.7
     */
    public void setNeedsValidateOnFinish(boolean needsValidateOnFinish) {
        this.needsValidateOnFinish = needsValidateOnFinish;
    }

    @java.lang.Override
    public boolean preFinish(org.eclipse.mylyn.tasks.core.TaskRepository repository) {
        if (shouldValidateOnFinish() && (!propertiesUnchanged())) {
            isValid = false;
            validateSettings();
        } else {
            isValid = true;
        }
        if (isValid) {
            isValid = super.preFinish(repository);
        }
        return isValid;
    }

    boolean shouldValidateOnFinish() {
        return (validateOnFinishButton != null) && validateOnFinishButton.getSelection();
    }

    @java.lang.Override
    protected org.eclipse.ui.forms.widgets.ExpandableComposite createSection(org.eclipse.swt.widgets.Composite parentControl, java.lang.String title) {
        if (parentControl instanceof org.eclipse.mylyn.commons.workbench.forms.SectionComposite) {
            return ((org.eclipse.mylyn.commons.workbench.forms.SectionComposite) (parentControl)).createSection(title);
        } else {
            return super.createSection(parentControl, title);
        }
    }

    /**
     * Returns the toolkit used to construct sections and hyperlinks.
     *
     * @return the toolkit
     * @throws IllegalStateException
     * 		if the toolkit has not been initialized
     * @since 3.9
     */
    protected org.eclipse.ui.forms.widgets.FormToolkit getToolkit() {
        if (toolkit == null) {
            throw new java.lang.IllegalStateException("Toolkit is not initialized, createControl() must be invoked first");// $NON-NLS-1$

        }
        return toolkit;
    }

    private boolean propertiesUnchanged() {
        org.eclipse.mylyn.tasks.core.TaskRepository newRepository = createTaskRepository();
        boolean propertiesUnchanged = false;
        if (validatedTaskRepository != null) {
            propertiesUnchanged = validatedTaskRepository.getProperties().equals(newRepository.getProperties());
            if (propertiesUnchanged) {
                for (org.eclipse.mylyn.commons.net.AuthenticationType authenticationType : org.eclipse.mylyn.commons.net.AuthenticationType.values()) {
                    org.eclipse.mylyn.commons.net.AuthenticationCredentials credentialsOld = validatedAuthenticationCredentials.get(authenticationType);
                    org.eclipse.mylyn.commons.net.AuthenticationCredentials credentialsNew = newRepository.getCredentials(authenticationType);
                    if (credentialsOld != null) {
                        propertiesUnchanged = credentialsOld.equals(credentialsNew);
                        if (!propertiesUnchanged) {
                            break;
                        }
                    } else if (credentialsNew != null) {
                        propertiesUnchanged = false;
                        break;
                    }
                }
            }
        }
        return propertiesUnchanged;
    }

    private void saveToValidatedProperties(org.eclipse.mylyn.tasks.core.TaskRepository taskRepository) {
        validatedTaskRepository = taskRepository;
        validatedAuthenticationCredentials.clear();
        for (org.eclipse.mylyn.commons.net.AuthenticationType authenticationType : org.eclipse.mylyn.commons.net.AuthenticationType.values()) {
            org.eclipse.mylyn.commons.net.AuthenticationCredentials ra = validatedTaskRepository.getCredentials(authenticationType);
            validatedAuthenticationCredentials.put(authenticationType, ra);
        }
    }

    /**
     * Updates the branding of this repository settings page. This also updates the title and wizard banner for the
     * given brand.
     *
     * @param brand
     * 		new connector branding ID
     * @since 3.17
     */
    public void setBrand(@org.eclipse.jdt.annotation.NonNull
    java.lang.String brand) {
        this.brand = brand;
        updateTitle(brand);
        updateBanner(brand);
    }

    private void updateBrandFromRepository() {
        if ((repository != null) && repository.hasProperty(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.PROPERTY_BRAND_ID)) {
            setBrand(repository.getProperty(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.PROPERTY_BRAND_ID));
        }
    }

    /**
     * Called when the page's branding is set. Implementors may change the wizard's title to one with branding specific
     * information. Sets the title to the branding's connector title by default.
     *
     * @param brand
     * 		The current connector branding ID
     * @since 3.17
     */
    protected void updateTitle(java.lang.String brand) {
        org.eclipse.mylyn.internal.tasks.ui.IBrandManager brandManager = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getBrandManager();
        setTitle(brandManager.getConnectorLabel(getConnector(), brand));
    }

    /**
     * Called when the page's branding is set. Implementors may change the wizard's banner to one with branding specific
     * information. Does nothing by default.
     *
     * @param brand
     * 		The current connector branding ID
     * @since 3.17
     */
    protected void updateBanner(java.lang.String brand) {
        // do nothing
    }
}