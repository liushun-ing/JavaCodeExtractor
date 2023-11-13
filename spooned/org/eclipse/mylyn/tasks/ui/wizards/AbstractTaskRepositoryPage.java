/**
 * *****************************************************************************
 * Copyright (c) 2004, 2013 David Green and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *     Tasktop Technologies - improvements
 *     Helen Bershadskaya - improvements for bug 242445
 * *****************************************************************************
 */
package org.eclipse.mylyn.tasks.ui.wizards;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.wizards.Messages;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
/**
 * An abstract base class for repository settings page that supports the <code>taskRepositoryPageContribution</code>
 * extension point. {@link ITaskRepositoryPage} implementations are encouraged to extend
 * {@link AbstractRepositorySettingsPage} if possible as it provides a standard UI for managing server settings.
 *
 * @see AbstractRepositorySettingsPage
 * @author David Green
 * @author Steffen Pingel
 * @since 3.1
 */
public abstract class AbstractTaskRepositoryPage extends org.eclipse.jface.wizard.WizardPage implements org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage {
    private static final java.lang.String CLASS = "class";// $NON-NLS-1$


    private static final java.lang.String ID = "id";// $NON-NLS-1$


    private static final java.lang.String KIND = "connectorKind";// $NON-NLS-1$


    private static final java.lang.String TASK_REPOSITORY_PAGE_CONTRIBUTION = "taskRepositoryPageContribution";// $NON-NLS-1$


    private static final java.lang.String TASK_REPOSITORY_PAGE_CONTRIBUTION_EXTENSION = "org.eclipse.mylyn.tasks.ui.taskRepositoryPageContribution";// $NON-NLS-1$


    private static final java.util.Comparator<org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPageContribution> CONTRIBUTION_COMPARATOR = new org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPage.ContributionComparator();

    private final org.eclipse.mylyn.tasks.core.TaskRepository repository;

    private final java.util.List<org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPageContribution> contributions;

    org.eclipse.ui.forms.widgets.FormToolkit toolkit;

    private final AbstractTaskRepositoryPageContribution.Listener contributionListener = new org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPageContribution.Listener() {
        public void validationRequired(org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPageContribution contribution) {
            validatePageSettings();
        }
    };

    /**
     *
     * @since 3.1
     */
    public AbstractTaskRepositoryPage(java.lang.String title, java.lang.String description, org.eclipse.mylyn.tasks.core.TaskRepository repository) {
        super(title);
        this.repository = repository;
        this.contributions = new java.util.ArrayList<org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPageContribution>();
        setTitle(title);
        setDescription(description);
    }

    /**
     * Get the kind of connector supported by this page.
     *
     * @return the kind of connector, never null
     * @since 3.1
     */
    public abstract java.lang.String getConnectorKind();

    @java.lang.Override
    public void dispose() {
        if (toolkit != null) {
            toolkit.dispose();
            toolkit = null;
        }
        super.dispose();
    }

    /**
     * Creates the contents of the page. Subclasses may override this method to change where the contributions are
     * added.
     *
     * @since 2.0
     */
    public void createControl(org.eclipse.swt.widgets.Composite parent) {
        initializeDialogUnits(parent);
        toolkit = new org.eclipse.ui.forms.widgets.FormToolkit(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getFormColors(parent.getDisplay()));
        org.eclipse.swt.widgets.Composite compositeContainer = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NONE);
        org.eclipse.swt.layout.GridLayout layout = new org.eclipse.swt.layout.GridLayout(3, false);
        compositeContainer.setLayout(layout);
        // Composite compositeContainer = new Composite(parent, SWT.NULL);
        // Layout layout = new FillLayout();
        // compositeContainer.setLayout(layout);
        createSettingControls(compositeContainer);
        createContributionControls(compositeContainer);
        org.eclipse.jface.dialogs.Dialog.applyDialogFont(compositeContainer);
        setControl(compositeContainer);
        // getControl().getShell().pack();
    }

    /**
     * Creates the controls of this page.
     *
     * @since 3.1
     */
    protected abstract void createSettingControls(org.eclipse.swt.widgets.Composite parent);

    @java.lang.Override
    public boolean isPageComplete() {
        return super.isPageComplete() && conributionsIsPageComplete();
    }

    @java.lang.Override
    public boolean canFlipToNextPage() {
        return super.canFlipToNextPage() && contributionsCanFlipToNextPage();
    }

    private boolean contributionsCanFlipToNextPage() {
        for (org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPageContribution contribution : contributions) {
            if (!contribution.canFlipToNextPage()) {
                return false;
            }
        }
        return true;
    }

    private boolean conributionsIsPageComplete() {
        for (org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPageContribution contribution : contributions) {
            if (!contribution.isPageComplete()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Subclasses should only call this method if they override {@link #createContents(Composite)}.
     *
     * @param parentControl
     * 		the container into which the contributions will create their UI
     * @since 3.1
     */
    protected void createContributionControls(final org.eclipse.swt.widgets.Composite parentControl) {
        contributions.clear();
        contributions.addAll(findApplicableContributors());
        if (!contributions.isEmpty()) {
            final java.util.List<org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPageContribution> badContributions = new java.util.ArrayList<org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPageContribution>();
            for (final org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPageContribution contribution : contributions) {
                org.eclipse.jface.util.SafeRunnable.run(new org.eclipse.jface.util.SafeRunnable() {
                    public void run() throws java.lang.Exception {
                        contribution.init(getConnectorKind(), repository);
                        contribution.addListener(contributionListener);
                    }

                    @java.lang.Override
                    public void handleException(java.lang.Throwable e) {
                        badContributions.add(contribution);
                        org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.osgi.util.NLS.bind("Problems occured when initializing contribution \"{0}\"", contribution.getId()), e));// $NON-NLS-1$

                    }
                });
            }
            contributions.removeAll(badContributions);
            java.util.Collections.sort(contributions, org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPage.CONTRIBUTION_COMPARATOR);
            for (final org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPageContribution contribution : contributions) {
                final org.eclipse.ui.forms.widgets.ExpandableComposite section = createSection(parentControl, contribution.getTitle());
                section.setToolTipText(contribution.getDescription());
                org.eclipse.jface.util.SafeRunnable.run(new org.eclipse.jface.util.SafeRunnable() {
                    public void run() throws java.lang.Exception {
                        org.eclipse.swt.widgets.Control control = contribution.createControl(section);
                        section.setClient(control);
                    }

                    @java.lang.Override
                    public void handleException(java.lang.Throwable e) {
                        section.dispose();
                        org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, // $NON-NLS-1$
                        org.eclipse.osgi.util.NLS.bind("Problems occured when creating control for contribution \"{0}\"", contribution.getId()), e));
                    }
                });
            }
        }
    }

    /**
     *
     * @since 3.1
     */
    protected org.eclipse.ui.forms.widgets.ExpandableComposite createSection(final org.eclipse.swt.widgets.Composite parentControl, java.lang.String title) {
        final org.eclipse.ui.forms.widgets.ExpandableComposite section = toolkit.createExpandableComposite(parentControl, (org.eclipse.ui.forms.widgets.ExpandableComposite.TWISTIE | org.eclipse.ui.forms.widgets.ExpandableComposite.CLIENT_INDENT) | org.eclipse.ui.forms.widgets.ExpandableComposite.COMPACT);
        section.clientVerticalSpacing = 0;
        section.setBackground(parentControl.getBackground());
        section.setFont(org.eclipse.jface.resource.JFaceResources.getFontRegistry().getBold(org.eclipse.jface.resource.JFaceResources.DIALOG_FONT));
        section.addExpansionListener(new org.eclipse.ui.forms.events.ExpansionAdapter() {
            @java.lang.Override
            public void expansionStateChanged(org.eclipse.ui.forms.events.ExpansionEvent e) {
                parentControl.layout(true);
                getControl().getShell().pack();
            }
        });
        section.setText(title);
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().indent(0, 5).grab(true, false).span(3, org.eclipse.swt.SWT.DEFAULT).applyTo(section);
        return section;
    }

    /**
     * Validate the settings of this page, not including contributions. This method should not be called directly by
     * page implementations. Always run on a UI thread.
     *
     * @return the status, or null if there are no messages.
     * @see #validatePageSettings()
     * @since 3.1
     */
    protected abstract org.eclipse.core.runtime.IStatus validate();

    /**
     * Overriding methods should call <code>super.applyTo(repository)</code>
     *
     * @since 3.1
     */
    public void applyTo(org.eclipse.mylyn.tasks.core.TaskRepository repository) {
        applyContributionSettingsTo(repository);
    }

    private void applyContributionSettingsTo(org.eclipse.mylyn.tasks.core.TaskRepository repository) {
        for (org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPageContribution contribution : contributions) {
            contribution.applyTo(repository);
        }
    }

    /**
     * {@inheritDoc }
     * <p>
     * Invokes {@link #applyTo(TaskRepository)} by default. Client may override.
     *
     * @since 3.6
     */
    public void performFinish(org.eclipse.mylyn.tasks.core.TaskRepository repository) {
        applyTo(repository);
    }

    /**
     * {@inheritDoc }
     * <p>
     * Invokes {@link #applyTo(TaskRepository)} by default. Client may override.
     *
     * @since 3.7
     * @return true to indicate the finish request was accepted, and false to indicate that the finish request was
    refused
     */
    public boolean preFinish(org.eclipse.mylyn.tasks.core.TaskRepository repository) {
        return true;
    }

    /**
     * Returns a status if there is a message to display, otherwise null.
     */
    private org.eclipse.core.runtime.IStatus computeValidation() {
        final org.eclipse.core.runtime.MultiStatus cumulativeResult = new org.eclipse.core.runtime.MultiStatus(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.core.runtime.IStatus.OK, org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractTaskRepositoryPage_Validation_failed, null);
        // validate the page
        org.eclipse.core.runtime.IStatus result = validate();
        if (result != null) {
            cumulativeResult.add(result);
        }
        // validate contributions
        for (final org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPageContribution contribution : contributions) {
            org.eclipse.jface.util.SafeRunnable.run(new org.eclipse.jface.util.SafeRunnable() {
                public void run() throws java.lang.Exception {
                    org.eclipse.core.runtime.IStatus result = contribution.validate();
                    if (result != null) {
                        cumulativeResult.add(result);
                    }
                }

                @java.lang.Override
                public void handleException(java.lang.Throwable e) {
                    org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.osgi.util.NLS.bind("Problems occured when validating contribution \"{0}\"", contribution.getId()), e));// $NON-NLS-1$

                }
            });
        }
        return cumulativeResult;
    }

    /**
     * Validate all settings in the page including contributions. This method should be called whenever a setting is
     * changed on the page. The results of validation are applied and the buttons of the page are updated.
     *
     * @see #validate(IProgressMonitor)
     * @see #applyValidationResult(IStatus[])
     */
    private void validatePageSettings() {
        org.eclipse.core.runtime.IStatus validationStatus = computeValidation();
        applyValidationResult(validationStatus);
        getWizard().getContainer().updateButtons();
    }

    /**
     * Apply the results of validation to the page. The implementation finds the most {@link IStatus#getSeverity()
     * severe} status and {@link #setMessage(String, int) applies the message} to the page.
     *
     * @param status
     * 		the status of the validation, or null
     */
    private void applyValidationResult(org.eclipse.core.runtime.IStatus status) {
        if ((status == null) || status.isOK()) {
            setMessage(null, org.eclipse.jface.dialogs.IMessageProvider.INFORMATION);
            setErrorMessage(null);
        } else {
            // find the most severe status
            int messageType;
            switch (status.getSeverity()) {
                case org.eclipse.core.runtime.IStatus.OK :
                case org.eclipse.core.runtime.IStatus.INFO :
                    messageType = org.eclipse.jface.dialogs.IMessageProvider.INFORMATION;
                    break;
                case org.eclipse.core.runtime.IStatus.WARNING :
                    messageType = org.eclipse.jface.dialogs.IMessageProvider.WARNING;
                    break;
                case org.eclipse.core.runtime.IStatus.ERROR :
                default :
                    messageType = org.eclipse.jface.dialogs.IMessageProvider.ERROR;
                    break;
            }
            setErrorMessage(null);
            setMessage(status.getMessage(), messageType);
        }
    }

    private java.util.List<org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPageContribution> findApplicableContributors() {
        java.util.List<org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPageContribution> contributors = new java.util.ArrayList<org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPageContribution>();
        org.eclipse.core.runtime.IExtensionRegistry registry = org.eclipse.core.runtime.Platform.getExtensionRegistry();
        org.eclipse.core.runtime.IExtensionPoint editorExtensionPoint = registry.getExtensionPoint(org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPage.TASK_REPOSITORY_PAGE_CONTRIBUTION_EXTENSION);
        org.eclipse.core.runtime.IExtension[] editorExtensions = editorExtensionPoint.getExtensions();
        for (org.eclipse.core.runtime.IExtension extension : editorExtensions) {
            org.eclipse.core.runtime.IConfigurationElement[] elements = extension.getConfigurationElements();
            for (org.eclipse.core.runtime.IConfigurationElement element : elements) {
                if (element.getName().equals(org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPage.TASK_REPOSITORY_PAGE_CONTRIBUTION)) {
                    java.lang.String kind = element.getAttribute(org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPage.KIND);
                    if (((kind == null) || (kind.length() == 0)) || kind.equals(getConnectorKind())) {
                        java.lang.String id = element.getAttribute(org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPage.ID);
                        try {
                            if ((id == null) || (id.length() == 0)) {
                                throw new java.lang.IllegalStateException(((org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPage.TASK_REPOSITORY_PAGE_CONTRIBUTION + "/@") + org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPage.ID)// $NON-NLS-1$
                                 + " is required");// $NON-NLS-1$

                            }
                            java.lang.Object contributor = element.createExecutableExtension(org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPage.CLASS);
                            org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPageContribution pageContributor = ((org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPageContribution) (contributor));
                            pageContributor.setId(id);
                            if (pageContributor.isEnabled()) {
                                contributors.add(pageContributor);
                            }
                        } catch (java.lang.Throwable e) {
                            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, (((("Could not load "// $NON-NLS-1$
                             + org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPage.TASK_REPOSITORY_PAGE_CONTRIBUTION) + " '") + id) + "' from plug-in ")// $NON-NLS-1$//$NON-NLS-2$
                             + element.getContributor().getName(), e));
                        }
                    }
                }
            }
        }
        return contributors;
    }

    private static class ContributionComparator implements java.util.Comparator<org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPageContribution> {
        public int compare(org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPageContribution o1, org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPageContribution o2) {
            if (o1 == o2) {
                return 0;
            }
            java.lang.String s1 = o1.getTitle();
            java.lang.String s2 = o2.getTitle();
            int i = s1.compareTo(s2);
            if (i == 0) {
                i = o1.getId().compareTo(o2.getId());
            }
            return i;
        }
    }
}