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
 *     Frank Becker - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.tasks.ui.wizards;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.mylyn.commons.ui.CommonUiUtil;
import org.eclipse.mylyn.commons.ui.ProgressContainer;
import org.eclipse.mylyn.commons.workbench.forms.SectionComposite;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.ui.wizards.Messages;
import org.eclipse.mylyn.internal.tasks.ui.wizards.QueryWizardDialog;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
/**
 *
 * @author Steffen Pingel
 * @author Frank Becker
 * @since 3.7
 */
public abstract class AbstractRepositoryQueryPage2 extends org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositoryQueryPage {
    private org.eclipse.swt.widgets.Button cancelButton;

    private final org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector;

    private boolean firstTime = true;

    private org.eclipse.mylyn.commons.workbench.forms.SectionComposite innerComposite;

    /**
     * Determines whether a 'Clear Fields' button is shown on the page.
     */
    private boolean needsClear;

    /**
     * Determines whether a 'Refresh' button is shown on the page.
     */
    private boolean needsRefresh = true;

    private org.eclipse.mylyn.commons.ui.ProgressContainer progressContainer;

    private org.eclipse.swt.widgets.Button refreshButton;

    private org.eclipse.swt.widgets.Text titleText;

    private boolean titleWasSuggested;

    private boolean editQueryTitleInProgress;

    public AbstractRepositoryQueryPage2(java.lang.String pageName, org.eclipse.mylyn.tasks.core.TaskRepository repository, org.eclipse.mylyn.tasks.core.IRepositoryQuery query) {
        super(pageName, repository, query);
        this.connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryConnector(getTaskRepository().getConnectorKind());
        setTitle(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositoryQueryPage2_Enter_query_parameters);
    }

    public void createControl(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.widgets.Composite composite = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NONE);
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.FILL).grab(true, true).applyTo(composite);
        org.eclipse.swt.layout.GridLayout layout = new org.eclipse.swt.layout.GridLayout(2, false);
        if (inSearchContainer()) {
            layout.marginWidth = 0;
            layout.marginHeight = 0;
        }
        composite.setLayout(layout);
        createTitleGroup(composite);
        innerComposite = new org.eclipse.mylyn.commons.workbench.forms.SectionComposite(composite, org.eclipse.swt.SWT.NONE);
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.FILL).grab(true, true).span(2, 1).applyTo(innerComposite);
        createPageContent(innerComposite);
        createButtonGroup(composite);
        if (!needsRefresh) {
            setDescription(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositoryQueryPage2_Create_a_Query_Page_Description);
        }
        org.eclipse.jface.dialogs.Dialog.applyDialogFont(composite);
        setControl(composite);
    }

    @java.lang.Override
    public java.lang.String getQueryTitle() {
        return titleText != null ? titleText.getText() : null;
    }

    public boolean handleExtraButtonPressed(int buttonId) {
        if (buttonId == org.eclipse.mylyn.internal.tasks.ui.wizards.QueryWizardDialog.REFRESH_BUTTON_ID) {
            if (getTaskRepository() != null) {
                refreshConfiguration(true);
            } else {
                org.eclipse.jface.dialogs.MessageDialog.openInformation(org.eclipse.swt.widgets.Display.getCurrent().getActiveShell(), org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositoryQueryPage2_Update_Attributes_Failed, org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositoryQueryPage2_No_repository_available_please_add_one_using_the_Task_Repositories_view);
            }
            return true;
        } else if (buttonId == org.eclipse.mylyn.internal.tasks.ui.wizards.QueryWizardDialog.CLEAR_BUTTON_ID) {
            doClearControls();
            return true;
        }
        return false;
    }

    @java.lang.Override
    public boolean isPageComplete() {
        if ((titleText != null) && (titleText.getText().length() > 0)) {
            return true;
        }
        setMessage(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositoryQueryPage2_Enter_a_title);
        return false;
    }

    /**
     * Allows connectors to suggest a query title. As long as the title field does not contain edits made by the user,
     * the field will be updated with the suggestion whenever the button enablement is updated.
     *
     * @return a query title suggested based on the query parameters, or the empty string
     * @since 3.18
     */
    @org.eclipse.jdt.annotation.NonNull
    protected java.lang.String suggestQueryTitle() {
        return "";// $NON-NLS-1$

    }

    /**
     * Called by the framework to update the title from the suggestion.
     */
    void updateTitleFromSuggestion() {
        if (editQueryTitleInProgress) {
            titleWasSuggested = false;
        } else if ((titleWasSuggested || com.google.common.base.Strings.isNullOrEmpty(getQueryTitle())) || (((getQuery() != null) && com.google.common.base.Objects.equal(getQuery().getSummary(), getQueryTitle())) && suggestQueryTitle().equals(getQueryTitle()))) {
            setQueryTitle(suggestQueryTitle());
            titleWasSuggested = true;
        }
    }

    public boolean needsClear() {
        return needsClear;
    }

    public boolean needsRefresh() {
        return needsRefresh;
    }

    @java.lang.Override
    public boolean performSearch() {
        if (inSearchContainer()) {
            saveState();
        }
        return super.performSearch();
    }

    @java.lang.Override
    public void saveState() {
        if (inSearchContainer()) {
            org.eclipse.mylyn.internal.tasks.core.RepositoryQuery query = new org.eclipse.mylyn.internal.tasks.core.RepositoryQuery(getTaskRepository().getConnectorKind(), "handle");// $NON-NLS-1$

            applyTo(query);
            org.eclipse.jface.dialogs.IDialogSettings settings = getDialogSettings();
            if (settings != null) {
                settings.put(getSavedStateSettingKey(), query.getUrl());
            }
        }
    }

    public void setExtraButtonState(org.eclipse.swt.widgets.Button button) {
        java.lang.Integer obj = ((java.lang.Integer) (button.getData()));
        if (obj == org.eclipse.mylyn.internal.tasks.ui.wizards.QueryWizardDialog.REFRESH_BUTTON_ID) {
            if (needsRefresh) {
                if (!button.isVisible()) {
                    button.setVisible(true);
                }
                button.setEnabled(true);
            } else if ((button != null) && button.isVisible()) {
                button.setVisible(false);
            }
        } else if (obj == org.eclipse.mylyn.internal.tasks.ui.wizards.QueryWizardDialog.CLEAR_BUTTON_ID) {
            if (!button.isVisible()) {
                button.setVisible(true);
            }
            button.setEnabled(true);
        }
    }

    public void setNeedsClear(boolean needsClearButton) {
        this.needsClear = needsClearButton;
    }

    public void setNeedsRefresh(boolean needsRefresh) {
        this.needsRefresh = needsRefresh;
    }

    public void setQueryTitle(java.lang.String text) {
        if (titleText != null) {
            titleText.setText(text);
        }
    }

    @java.lang.Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (getSearchContainer() != null) {
            getSearchContainer().setPerformActionEnabled(true);
        }
        if (visible && firstTime) {
            firstTime = false;
            if ((!hasRepositoryConfiguration()) && needsRefresh) {
                // delay the execution so the dialog's progress bar is visible
                // when the attributes are updated
                org.eclipse.swt.widgets.Display.getDefault().asyncExec(new java.lang.Runnable() {
                    public void run() {
                        if ((getControl() != null) && (!getControl().isDisposed())) {
                            initializePage();
                        }
                    }
                });
            } else {
                // no remote connection is needed to get attributes therefore do
                // not use delayed execution to avoid flickering
                initializePage();
            }
        }
    }

    private void createButtonGroup(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.widgets.Composite buttonComposite = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NONE);
        org.eclipse.swt.layout.GridLayout layout = new org.eclipse.swt.layout.GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        buttonComposite.setLayout(layout);
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.TOP).grab(true, false).span(2, 1).applyTo(buttonComposite);
        createButtons(buttonComposite);
        if (buttonComposite.getChildren().length > 0) {
            layout.numColumns = buttonComposite.getChildren().length;
        } else {
            // remove composite to avoid spacing
            buttonComposite.dispose();
        }
    }

    private void createTitleGroup(org.eclipse.swt.widgets.Composite control) {
        if (inSearchContainer()) {
            return;
        }
        org.eclipse.swt.widgets.Label titleLabel = new org.eclipse.swt.widgets.Label(control, org.eclipse.swt.SWT.NONE);
        titleLabel.setText(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositoryQueryPage2__Title_);
        titleText = new org.eclipse.swt.widgets.Text(control, org.eclipse.swt.SWT.BORDER);
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.TOP).grab(true, false).applyTo(titleText);
        titleText.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
            public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
                try {
                    editQueryTitleInProgress = true;
                    getContainer().updateButtons();
                } finally {
                    editQueryTitleInProgress = false;
                }
            }
        });
    }

    private void initializePage() {
        if (needsRefresh) {
            boolean refreshed = refreshConfiguration(false);
            if (!refreshed) {
                // always do a refresh when page is initially shown
                if (!innerComposite.isDisposed()) {
                    doRefreshControls();
                }
            }
        }
        boolean restored = false;
        if (getQuery() != null) {
            titleText.setText(getQuery().getSummary());
            restored |= restoreState(getQuery());
        } else if (inSearchContainer()) {
            restored |= restoreSavedState();
        }
        if (!restored) {
            // initialize with default values
            if (!innerComposite.isDisposed()) {
                doClearControls();
            }
        }
    }

    protected boolean refreshConfiguration(final boolean force) {
        if (force || (!hasRepositoryConfiguration())) {
            setErrorMessage(null);
            try {
                doRefreshConfiguration();
                if (!innerComposite.isDisposed()) {
                    doRefreshControls();
                }
                return true;
            } catch (java.lang.reflect.InvocationTargetException e) {
                if (e.getCause() instanceof org.eclipse.core.runtime.CoreException) {
                    setErrorMessage(((org.eclipse.core.runtime.CoreException) (e.getCause())).getStatus().getMessage());
                } else {
                    setErrorMessage(e.getCause().getMessage());
                }
            } catch (java.lang.InterruptedException e) {
                // canceled
            }
        }
        return false;
    }

    private void doRefreshConfiguration() throws java.lang.reflect.InvocationTargetException, java.lang.InterruptedException {
        org.eclipse.jface.operation.IRunnableWithProgress runnable = new org.eclipse.jface.operation.IRunnableWithProgress() {
            public void run(org.eclipse.core.runtime.IProgressMonitor monitor) throws java.lang.reflect.InvocationTargetException, java.lang.InterruptedException {
                monitor = org.eclipse.core.runtime.SubMonitor.convert(monitor);
                monitor.beginTask(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositoryQueryPage2_Refresh_Configuration_Button_Label, org.eclipse.core.runtime.IProgressMonitor.UNKNOWN);
                try {
                    connector.updateRepositoryConfiguration(getTaskRepository(), monitor);
                } catch (org.eclipse.core.runtime.CoreException e) {
                    throw new java.lang.reflect.InvocationTargetException(e);
                } catch (org.eclipse.core.runtime.OperationCanceledException e) {
                    throw new java.lang.InterruptedException();
                } finally {
                    monitor.done();
                }
            }
        };
        if (getContainer() != null) {
            getContainer().run(true, true, runnable);
        } else if (progressContainer != null) {
            progressContainer.run(true, true, runnable);
        } else if (getSearchContainer() != null) {
            getSearchContainer().getRunnableContext().run(true, true, runnable);
        } else {
            org.eclipse.ui.progress.IProgressService service = org.eclipse.ui.PlatformUI.getWorkbench().getProgressService();
            service.busyCursorWhile(runnable);
        }
    }

    protected void createButtons(final org.eclipse.swt.widgets.Composite composite) {
        if (getContainer() instanceof org.eclipse.mylyn.internal.tasks.ui.wizards.QueryWizardDialog) {
            // refresh and clear buttons are provided by the dialog
            return;
        }
        if (needsRefresh) {
            refreshButton = new org.eclipse.swt.widgets.Button(composite, org.eclipse.swt.SWT.PUSH);
            refreshButton.setText(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositoryQueryPage2__Refresh_From_Repository);
            refreshButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                @java.lang.Override
                public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                    if (getTaskRepository() != null) {
                        refreshConfiguration(true);
                    } else {
                        org.eclipse.jface.dialogs.MessageDialog.openInformation(org.eclipse.swt.widgets.Display.getCurrent().getActiveShell(), org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositoryQueryPage2_Update_Attributes_Failed, org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositoryQueryPage2_No_repository_available_please_add_one_using_the_Task_Repositories_view);
                    }
                }
            });
        }
        if (needsClear) {
            org.eclipse.swt.widgets.Button clearButton = new org.eclipse.swt.widgets.Button(composite, org.eclipse.swt.SWT.PUSH);
            clearButton.setText(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositoryQueryPage2_Clear_Fields);
            clearButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                @java.lang.Override
                public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                    doClearControls();
                }
            });
        }
        if (getContainer() == null) {
            final org.eclipse.jface.wizard.ProgressMonitorPart progressMonitorPart = new org.eclipse.jface.wizard.ProgressMonitorPart(composite, null);
            org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.BEGINNING).grab(true, false).applyTo(progressMonitorPart);
            progressMonitorPart.setVisible(false);
            progressContainer = new org.eclipse.mylyn.commons.ui.ProgressContainer(composite.getShell(), progressMonitorPart) {
                @java.lang.Override
                protected void restoreUiState(java.util.Map<java.lang.Object, java.lang.Object> state) {
                    cancelButton.setVisible(false);
                    org.eclipse.mylyn.commons.ui.CommonUiUtil.setEnabled(innerComposite, true);
                    for (org.eclipse.swt.widgets.Control control : composite.getChildren()) {
                        if (control instanceof org.eclipse.jface.wizard.ProgressMonitorPart) {
                            break;
                        }
                        control.setEnabled(true);
                    }
                }

                @java.lang.Override
                protected void saveUiState(java.util.Map<java.lang.Object, java.lang.Object> savedState) {
                    org.eclipse.mylyn.commons.ui.CommonUiUtil.setEnabled(innerComposite, false);
                    for (org.eclipse.swt.widgets.Control control : composite.getChildren()) {
                        if (control instanceof org.eclipse.jface.wizard.ProgressMonitorPart) {
                            break;
                        }
                        control.setEnabled(false);
                    }
                    cancelButton.setEnabled(true);
                    cancelButton.setVisible(true);
                }
            };
            cancelButton = new org.eclipse.swt.widgets.Button(composite, org.eclipse.swt.SWT.PUSH);
            cancelButton.setText(org.eclipse.jface.dialogs.IDialogConstants.CANCEL_LABEL);
            cancelButton.setVisible(false);
            progressContainer.setCancelButton(cancelButton);
        }
    }

    protected abstract void createPageContent(@org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.commons.workbench.forms.SectionComposite parent);

    protected void doClearControls() {
    }

    protected abstract void doRefreshControls();

    protected org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector getConnector() {
        return connector;
    }

    protected java.lang.String getSavedStateSettingKey() {
        return (getName() + ".") + getTaskRepository().getRepositoryUrl();// $NON-NLS-1$

    }

    protected abstract boolean hasRepositoryConfiguration();

    protected boolean restoreSavedState() {
        org.eclipse.jface.dialogs.IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            java.lang.String queryUrl = settings.get(getSavedStateSettingKey());
            if (queryUrl != null) {
                org.eclipse.mylyn.internal.tasks.core.RepositoryQuery query = new org.eclipse.mylyn.internal.tasks.core.RepositoryQuery(getTaskRepository().getConnectorKind(), "handle");// $NON-NLS-1$

                query.setUrl(queryUrl);
                return restoreState(query);
            }
        }
        return false;
    }

    protected abstract boolean restoreState(@org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.core.IRepositoryQuery query);

    /**
     * Reflows the page and resizes the shell as necessary. Clients should invoke this if the content of the page is
     * changed dynamically.
     *
     * @see SectionComposite#resizeAndReflow()
     * @since 3.10
     */
    public void reflow() {
        innerComposite.resizeAndReflow();
    }
}